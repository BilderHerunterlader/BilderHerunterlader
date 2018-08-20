import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.containerpage.ContainerPage;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.rules.RuleMode;
import ch.supertomcat.bh.rules.RulePipeline;
import ch.supertomcat.bh.rules.RulePipelineURLRegex;
import ch.supertomcat.bh.rules.RuleRegExp;

/**
 * Host class for <code>http://www.flickr.com</code> <br>
 * <br>
 * This class handles two container types:
 * <ul>
 * <li>(main) container site (medium sized image):<br>
 * <code>http://www.flickr.com/photos/[username]/[photoID]/</code></li>
 * <li>subcontainer site (same image in a different resolution):<br>
 * <code>http://www.flickr.com/photos/[username]/[photoID]/sizes/[sizeID]/(in/[photoset]/)</code></li>
 * </ul>
 *
 * In either case this class tries to grab the original sized image first.
 * If that fails (e.g. original image is not available for download) it
 * will load the embedded image from the container site.
 *
 * @version 2.9
 */
public class HostFlickr extends Host implements IHoster {
	/** the version of this class **/
	public static final String VERSION = "2.9";

	/** the name of this class **/
	public static final String NAME = "HostFlickr";

	/** flickr photo size id: square (sq) **/
	private static final int SIZE_SQUARE_75 = 0;

	/** flickr photo size id: square (q) **/
	private static final int SIZE_SQUARE_150 = 1;

	/** flickr photo size id: thumbnail (t) **/
	private static final int SIZE_THUMBNAIL = 2;

	/** flickr photo size id: small (s) **/
	private static final int SIZE_SMALL_240 = 3;

	/** flickr photo size id: small (n) **/
	private static final int SIZE_SMALL_320 = 4;

	/** flickr photo size id: medium (m) **/
	private static final int SIZE_MEDIUM_500 = 5;

	/** flickr photo size id: medium (z) **/
	private static final int SIZE_MEDIUM_640 = 6;

	/** flickr photo size id: medium (c) **/
	private static final int SIZE_MEDIUM_800 = 7;

	/** flickr photo size id: large (l) **/
	private static final int SIZE_LARGE_1024 = 8;

	/** flickr photo size id: large (h) **/
	private static final int SIZE_LARGE_1600 = 9;

	/** flickr photo size id: large (k) **/
	private static final int SIZE_LARGE_2048 = 10;

	/** flickr photo size id: original (o) **/
	private static final int SIZE_ORIGINAL = 11;

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/** pattern that recognizes URLs of flickr container sites **/
	private final Pattern containerUrlPattern;

	/** pattern that recognizes URLs of flickr subcontainer sites **/
	private final Pattern subContainerUrlPattern;

	/** pattern that recognizes links to flickr subcontainer sites **/
	private final Pattern subContainerLinkPattern;

	/**
	 * pattern that recognizes the embedded image on flickr
	 * (sub)container sites
	 **/
	private final Pattern embeddedImagePattern;

	/**
	 * pipeOriginalDateTime
	 */
	private final RulePipeline pipeOriginalDateTime;

	/**
	 * regexOriginalDateTime1
	 */
	private final RuleRegExp regexOriginalDateTime1;

	/**
	 * regexOriginalDateTime2
	 */
	private final RuleRegExp regexOriginalDateTime2;

	/**
	 * regexOriginalDate
	 */
	private final RuleRegExp regexOriginalDate;

	private final RuleRegExp regexOriginalDate2;

	/**
	 * regexTitle
	 */
	private final RuleRegExp regexTitle;

	/**
	 * regexAlbumTitle
	 */
	private final RuleRegExp regexAlbumTitle;

	/**
	 * Default constructor. Initializes all required patterns.
	 */
	public HostFlickr() {
		super(NAME, VERSION);
		String container = "(https?://www\\.flickr\\.com/photos/(?!tags/)[^/]+?/([0-9]+?)/?)";
		String size = "sizes/(sq|q|t|s|n|m|z|c|l|h|k|o)/?";
		String inset = "(in/.+?/?)??";

		this.containerUrlPattern = Pattern.compile("^" + container + inset + "$", Pattern.CASE_INSENSITIVE);
		this.subContainerUrlPattern = Pattern.compile("^" + container + size + inset + "$", Pattern.CASE_INSENSITIVE);

		this.subContainerLinkPattern = Pattern.compile("<a.*?href=\"(/photos/(?!tags/)[^/]+/[0-9]+?/?)" + size + inset + "\"", Pattern.CASE_INSENSITIVE);

		this.embeddedImagePattern = Pattern.compile("<img src=\"(https?://.*?flickr\\.com/.+?/[0-9]+?[^/]*?\\.[a-z]*?(\\?[a-z]+=[0-9]+)??)\"", Pattern.CASE_INSENSITIVE);

		this.regexTitle = new RuleRegExp();
		this.regexTitle.setSearch("<meta name=\"title\" content=\"(.+?)\"[^>]*>");
		this.regexTitle.setReplace("$1");

		pipeOriginalDateTime = new RulePipelineURLRegex(RuleMode.RULE_MODE_CONTAINER_PAGE_SOURCECODE);

		// TODO not working anymore
		this.regexOriginalDateTime1 = new RuleRegExp();
		this.regexOriginalDateTime1.setSearch("(<th>Date and Time \\(Original\\)</th>|<td.*?>.*?Date and Time \\(Original\\).*?</td>)");

		// TODO not working anymore
		this.regexOriginalDateTime2 = new RuleRegExp();
		this.regexOriginalDateTime2.setSearch("<td>([0-9]+):([0-9]+):([0-9]+) ([0-9]+):([0-9]+):([0-9]+).*?</td>");
		this.regexOriginalDateTime2.setReplace("$1-$2-$3_$4-$5-$6");

		pipeOriginalDateTime.addRegExp(regexOriginalDateTime1);
		pipeOriginalDateTime.addRegExp(regexOriginalDateTime2);

		// If exif data are not available, it looks like there is always another date information (probably just the upload date, but that's better than
		// nothing)
		this.regexOriginalDate = new RuleRegExp();
		this.regexOriginalDate.setSearch("<a href=\"/photos/(?!tags/)[^/]+/archives/date-taken/([0-9]+)/([0-9]+)/([0-9]+)/\"");
		this.regexOriginalDate.setReplace("$1-$2-$3");

		this.regexOriginalDate2 = new RuleRegExp();
		this.regexOriginalDate2.setSearch("\"dateTaken\":\"([0-9]{4})-([0-9]{2})-([0-9]{2}) [0-9]{2}:[0-9]{2}:[0-9]{2}\",");
		this.regexOriginalDate2.setReplace("$1-$2-$3");

		// TODO not working anymore
		this.regexAlbumTitle = new RuleRegExp();
		this.regexAlbumTitle.setSearch("(?s)type: 'set',.*?title: '(.*?)',");
		this.regexAlbumTitle.setReplace("$1");
	}

	@Override
	public String getFilenameFromURL(String url) {
		Matcher urlMatcher = this.containerUrlPattern.matcher(url);
		if (urlMatcher.matches()) {
			return urlMatcher.replaceAll("$2");
		}

		urlMatcher = this.subContainerUrlPattern.matcher(url);
		if (urlMatcher.matches()) {
			return urlMatcher.replaceAll("$2");
		}
		return "";
	}

	@Override
	public boolean isFromThisHoster(String url) {
		Matcher matcher = this.containerUrlPattern.matcher(url);
		if (matcher.matches()) {
			return true;
		}
		matcher = this.subContainerUrlPattern.matcher(url);
		return matcher.matches();
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		String baseContainerUrl = "";
		String imgID = "";

		Matcher matcher = this.containerUrlPattern.matcher(upo.getContainerURL());
		if (matcher.matches()) {
			baseContainerUrl = matcher.replaceAll("$1");
			imgID = matcher.replaceAll("$2");
		} else {
			matcher = this.subContainerUrlPattern.matcher(upo.getContainerURL());
			if (matcher.matches()) {
				baseContainerUrl = matcher.replaceAll("$1");
				imgID = matcher.replaceAll("$2");
			}
		}

		if (baseContainerUrl.length() > 0) {
			String allSizesContainerUrl = baseContainerUrl + "/sizes/";
			String metaContainerUrl = baseContainerUrl + "/meta/";

			String allSizesContainerPage = "";
			String redirectedUrl = allSizesContainerUrl;
			try {
				ContainerPage result = downloadContainerPageEx(allSizesContainerUrl, "");
				allSizesContainerPage = result.getPage();
				redirectedUrl = result.getRedirectedURL();
			} catch (HostException e) {
				if (e.getMessage().contains("https")) {
					logger.error(NAME + " Could not download all sizes container page: Access Denied");
				} else {
					logger.error(NAME + " Could not download all sizes container page: " + e.getMessage());
				}
			}

			String directLink = parseAllSizesContainerPage(allSizesContainerPage, baseContainerUrl, redirectedUrl);

			String baseContainerPage = downloadContainerPage(baseContainerUrl, "");
			if (directLink.length() == 0) {
				directLink = parseEmbeddedImage(baseContainerPage);
			}

			if (directLink.length() > 0) {
				String adResult[] = parseAlbumTitleAndDate(baseContainerUrl, baseContainerPage);
				String albumTitle = adResult[0];
				String date = adResult[1];
				String title = regexTitle.doPageSourcecodeReplace(baseContainerPage, 0, baseContainerUrl, null);
				String dateTime = getOriginalDateTime(metaContainerUrl);
				if (dateTime.length() == 0 && date.length() > 0) {
					dateTime = date;
				}

				upo.setDirectLink(directLink);
				upo.setCorrectedFilename(createFilename(imgID, title, dateTime, directLink, albumTitle));
			}
		}
	}

	/**
	 * Returns the biggest available version of the image and the album title if available
	 * 
	 * @param allSizesContainerPage ContainerPage
	 * @param baseContainerUrl Base Container URL
	 * @param allSizesContainerUrl AllSizesContainerURL
	 * @return Biggest available version of the image and the album title if available
	 */
	private String parseAllSizesContainerPage(String allSizesContainerPage, String baseContainerUrl, String allSizesContainerUrl) {
		String directLink = "";

		String bestSizeSubContainerUrl = allSizesContainerUrl;
		int minSizeID = parseSizeIDfromURL(bestSizeSubContainerUrl);

		int maxSizeId = this.findMaxSizeIDinPage(minSizeID, allSizesContainerPage);
		if (maxSizeId != -1) {
			bestSizeSubContainerUrl = baseContainerUrl + "/sizes/" + this.sizeIDtoString(maxSizeId) + "/";
		}

		try {
			if (minSizeID == maxSizeId) {
				directLink = parseEmbeddedImage(allSizesContainerPage);
			} else {
				String bestSizeSubContainerPage = downloadContainerPage(bestSizeSubContainerUrl, "");
				directLink = parseEmbeddedImage(bestSizeSubContainerPage);
			}
		} catch (HostException e) {
			logger.error(NAME + " Could not parse embedded image", e);
		}

		return directLink;
	}

	/**
	 * Retrieves the flickr size ID of the specified subcontainer URL.
	 * Returns <code>-1</code> if <code>subContainerUrl</code> does
	 * not match the syntax for a subcontainer. <br>
	 * <br>
	 * The flickr size ID of each subcontainer can be derived from its URL:<br>
	 * http://www.flickr.com/photos/[username]/[photoID]/sizes/<b>[sizeID]</b>/
	 *
	 * @param subContainerUrl Sub Contrainer-URL
	 * @return the flickr size ID of <code>subContainerUrl</code>
	 */
	private int parseSizeIDfromURL(String subContainerUrl) {
		Matcher urlMatcher = this.subContainerUrlPattern.matcher(subContainerUrl);
		if (urlMatcher.matches()) {
			return this.parseSizeID(urlMatcher.replaceAll("$3"));
		} else {
			return -1;
		}
	}

	/**
	 * Returns the largest size ID of all subcontainer links in <code>page</code> that is larger than or equal to <code>minSizeId</code>.<br>
	 *
	 * @see #parseSizeIDfromURL(String)
	 * @param minSizeId minimum flickr size id
	 * @param page the source page to parse
	 * @return minSizeId if there is no link to
	 *         a subcontainer with a larger sized ID
	 */
	private int findMaxSizeIDinPage(int minSizeId, String page) {
		int maxSizeId = minSizeId;
		Matcher linkMatcher = this.subContainerLinkPattern.matcher(page);
		while (linkMatcher.find()) {
			int sizeId = this.parseSizeID(linkMatcher.group(2));
			if (maxSizeId < sizeId) {
				maxSizeId = sizeId;
			}
		}
		return maxSizeId;
	}

	/**
	 * Returns a String representation of <code>sizeID</code>.<br>
	 * i.e. 'sq', 't', 's', 'm', 'l' or 'o'
	 * 
	 * @param sizeID Size ID
	 * @return <code>null</code> if <code>int</code> argument
	 *         is not a valid flickr size ID
	 */
	private String sizeIDtoString(int sizeID) {
		switch (sizeID) {
			case SIZE_SQUARE_75:
				return "sq";
			case SIZE_SQUARE_150:
				return "q";
			case SIZE_THUMBNAIL:
				return "t";
			case SIZE_SMALL_240:
				return "s";
			case SIZE_SMALL_320:
				return "n";
			case SIZE_MEDIUM_500:
				return "m";
			case SIZE_MEDIUM_640:
				return "z";
			case SIZE_MEDIUM_800:
				return "c";
			case SIZE_LARGE_1024:
				return "l";
			case SIZE_LARGE_1600:
				return "h";
			case SIZE_LARGE_2048:
				return "k";
			case SIZE_ORIGINAL:
				return "o";
			default:
				return null;
		}
	}

	/**
	 * Parses the String argument as a flickr size ID.
	 * 
	 * @see #sizeIDtoString(int)
	 * @param sizeStr Size String
	 * @return <code>-1</code> if <code>sizeStr</code> does not represent a valid size ID
	 */
	private int parseSizeID(String sizeStr) {
		if (sizeStr.compareToIgnoreCase("Square") == 0 || sizeStr.compareToIgnoreCase("sq") == 0) {
			return SIZE_SQUARE_75;
		} else if (sizeStr.compareToIgnoreCase("Square") == 0 || sizeStr.compareToIgnoreCase("q") == 0) {
			return SIZE_SQUARE_150;
		} else if (sizeStr.compareToIgnoreCase("Thumbnail") == 0 || sizeStr.compareToIgnoreCase("t") == 0) {
			return SIZE_THUMBNAIL;
		} else if (sizeStr.compareToIgnoreCase("Small") == 0 || sizeStr.compareToIgnoreCase("s") == 0) {
			return SIZE_SMALL_240;
		} else if (sizeStr.compareToIgnoreCase("Small") == 0 || sizeStr.compareToIgnoreCase("n") == 0) {
			return SIZE_SMALL_320;
		} else if (sizeStr.compareToIgnoreCase("Medium") == 0 || sizeStr.compareToIgnoreCase("m") == 0) {
			return SIZE_MEDIUM_500;
		} else if (sizeStr.compareToIgnoreCase("Medium") == 0 || sizeStr.compareToIgnoreCase("z") == 0) {
			return SIZE_MEDIUM_640;
		} else if (sizeStr.compareToIgnoreCase("Medium") == 0 || sizeStr.compareToIgnoreCase("c") == 0) {
			return SIZE_MEDIUM_800;
		} else if (sizeStr.compareToIgnoreCase("Large") == 0 || sizeStr.compareToIgnoreCase("l") == 0) {
			return SIZE_LARGE_1024;
		} else if (sizeStr.compareToIgnoreCase("Large") == 0 || sizeStr.compareToIgnoreCase("h") == 0) {
			return SIZE_LARGE_1600;
		} else if (sizeStr.compareToIgnoreCase("Large") == 0 || sizeStr.compareToIgnoreCase("k") == 0) {
			return SIZE_LARGE_2048;
		} else if (sizeStr.compareToIgnoreCase("Original") == 0 || sizeStr.compareToIgnoreCase("o") == 0) {
			return SIZE_ORIGINAL;
		} else {
			return -1;
		}
	}

	/**
	 * Returns the album title of the image and the date (not as exact as from getOriginalDateTime-Method)
	 * 
	 * @param baseContainerUrl base container url
	 * @param baseContainerPage base container sourcecode
	 * @return Album title of the image and the date (not as exact as from getOriginalDateTime-Method)
	 */
	private String[] parseAlbumTitleAndDate(String baseContainerUrl, String baseContainerPage) {
		String albumTitle = regexAlbumTitle.doPageSourcecodeReplace(baseContainerPage, 0, baseContainerUrl, null);
		String date = regexOriginalDate2.doPageSourcecodeReplace(baseContainerPage, 0, baseContainerUrl, null);
		return new String[] { albumTitle, date };
	}

	/**
	 * Returns the direct link to the embedded image
	 * 
	 * @param containerPage container or subcontainer
	 * @return Direct link to the embedded image
	 */
	private String parseEmbeddedImage(String containerPage) {
		String directLink = this.getMatchingGroup(containerPage, this.embeddedImagePattern, 1);
		if (directLink == null) {
			directLink = "";
		}
		return directLink;
	}

	/**
	 * Gets the specified capture group of <code>input</code> if <code>pattern</code> was found in String argument. Returns <code>null</code> if
	 * <code>input</code> does not match <code>pattern</code>.
	 * 
	 * @param input Input
	 * @param pattern Pattern
	 * @param group Group
	 * @return the specified capture group of the first match
	 */
	private String getMatchingGroup(String input, Pattern pattern, int group) {
		Matcher matcher = pattern.matcher(input);
		if (matcher.find()) {
			try {
				return matcher.group(group);
			} catch (IndexOutOfBoundsException ioobe) {
			}
		}
		return null;
	}

	/**
	 * Creates a file name for the image file.<br>
	 * Pattern for the Filename: [photoID]_[albumTitle]_[photoTitle]_[Date]_[Time].[suffix]<br>
	 * If not all information are available at least [photoID].[suffix] is returned.
	 * 
	 * @param imgID flickr photo ID
	 * @param title flickr photo title
	 * @param dateTime flickr original date and time
	 * @param directLink provides the file suffix
	 * @param albumTitle flickr album title
	 * @return the new file name: [photoID]_[photoTitle]_[Date]_[Time].[suffix]
	 */
	private String createFilename(String imgID, String title, String dateTime, String directLink, String albumTitle) {
		String filename = imgID;
		String suffix = getFileSuffix(directLink);

		if (albumTitle.length() > 0) {
			filename += "_" + albumTitle;
		}

		if (title.length() > 0) {
			filename += "_" + title;
		}

		if (dateTime.length() > 0) {
			filename += "_" + dateTime;
		}

		filename += suffix;

		return filterFilename(filename);
	}

	/**
	 * Gets the suffix of the file name specified by <code>directLink</code> (including the dot character).
	 * Returns an empty String if no valid file suffix was found.
	 *
	 * @param directLink direct link to the image
	 * @return the file suffix
	 */
	private String getFileSuffix(String directLink) {
		int slashInd = directLink.lastIndexOf('/');
		int dotInd = directLink.lastIndexOf('.');
		int questionInd = directLink.lastIndexOf('?');
		if (dotInd == -1 || dotInd < slashInd) {
			return "";
		} else {
			if (questionInd > dotInd) {
				return directLink.substring(dotInd, questionInd);
			} else {
				return directLink.substring(dotInd);
			}
		}
	}

	/**
	 * Returns the original date and time of the image from the meta-Container-Page.
	 * 
	 * @param metaContainerUrl the container url of the meta page
	 * @return Date and Time if available
	 */
	private String getOriginalDateTime(String metaContainerUrl) {
		String dateTime = "";
		String metaContainerPage = "";
		try {
			metaContainerPage = downloadContainerPage(metaContainerUrl, "");
		} catch (HostException e) {
			logger.warn(NAME + " Could not download meta container page", e);
			return dateTime;
		}

		try {
			dateTime = this.pipeOriginalDateTime.getURL(metaContainerUrl, "", metaContainerPage, null);
		} catch (HostException e) {
			logger.warn(NAME + " Could not get date and time from meta container URL", e);
		}

		if (dateTime.isEmpty()) {
			dateTime = this.regexOriginalDate.doPageSourcecodeReplace(metaContainerPage, 0, metaContainerUrl, null);
		}

		return dateTime;
	}
}

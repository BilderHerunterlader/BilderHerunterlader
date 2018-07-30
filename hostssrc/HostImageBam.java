import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostFileTemporaryOfflineException;
import ch.supertomcat.bh.exceptions.HostImageUrlNotFoundException;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.queue.Restriction;
import ch.supertomcat.bh.rules.RuleRegExp;
import ch.supertomcat.supertomcattools.guitools.Localization;

/**
 * Host class for ImageBam
 * 
 * @version 3.7
 */
public class HostImageBam extends Host implements IHoster {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "3.7";

	/**
	 * Name dieser Klasse
	 */
	public static final String NAME = "HostImageBam";

	/**
	 * Kompiliertes Muster
	 */
	private Pattern urlPattern;

	private String[] errorMessages = new String[] { "This server will be right back!" };

	private RuleRegExp regexServerMaintenance = null;

	private RuleRegExp regexImage = null;

	private RuleRegExp regexFilename = null;

	/**
	 * Konstruktor
	 */
	public HostImageBam() {
		super(NAME, VERSION);
		urlPattern = Pattern.compile("^http://(www\\.)?imagebam\\.com/image/[a-z0-9]+(/)?");

		regexServerMaintenance = new RuleRegExp();
		regexServerMaintenance.setSearch("<b>(ImageBam is currently offline.)</b><br><br>(.+?)<br><br>(.+?)<br><br>");
		regexServerMaintenance.setReplace("$1 $2 $3");

		regexImage = new RuleRegExp();
		regexImage.setSearch("(?i)<img.+?class=\"image\".+?id=\"i[0-9]+\".+?src=\"(.+?)\"");
		regexImage.setReplace("$1");

		regexFilename = new RuleRegExp();
		regexFilename.setSearch(".+?filename=(.+)");
		regexFilename.setReplace("$1");

		DownloadQueueManager.instance().addRestriction(new Restriction("imagebam.com", 6));
	}

	@Override
	public boolean isFromThisHoster(String url) {
		Matcher urlMatcher = urlPattern.matcher(url);
		if (urlMatcher.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * URL parsen
	 * 
	 * @param url Container-URL
	 * @return URL
	 * @throws HostException
	 */
	private String parseURL(String url) throws HostException {
		String page = downloadContainerPage(url, url);

		String downloadURL = regexImage.doPageSourcecodeReplace(page, 0, url, null);

		if (downloadURL.length() == 0) {
			for (int i = 0; i < errorMessages.length; i++) {
				if (page.indexOf(errorMessages[i]) > -1) {
					throw new HostFileTemporaryOfflineException(errorMessages[i]);
				}
			}

			String errorMessage = regexServerMaintenance.doPageSourcecodeReplace(page, 0, url, null);
			if (errorMessage.length() > 0) {
				throw new HostFileTemporaryOfflineException(errorMessage);
			}
		}

		return downloadURL;
	}

	@Override
	public String getFilenameFromURL(String url) {
		if (!isFromThisHoster(url)) {
			return "";
		}
		return "";
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		if (isFromThisHoster(upo.getContainerURL())) {
			String s = parseURL(upo.getContainerURL());
			s = s.replaceAll("&amp;", "&");
			if (s.length() > 0) {
				upo.setDirectLink(s);
				String correctedFilename = "";
				if (s.contains("filename")) {
					// Read out filename from url, but we still use the filename from content-disposition if available
					correctedFilename = regexFilename.doURLReplace(s, null);
					URLCodec decoder = new URLCodec("utf-8");
					try {
						correctedFilename = decoder.decode(correctedFilename);
					} catch (DecoderException e) {
					}
					/*
					 * Imagebam does cut long filenames and also the extension is removed then.
					 * So we just add jpg in that case.
					 * This is not a good fix, as it would also add jpg in case it's a png for example.
					 * But there is not any information on the container-page or the http-header what
					 * type it is.
					 * So this fix is better than nothing.
					 */
					if (correctedFilename.length() > 0 && correctedFilename.matches(".+\\.(bmp|gif|jpe|jpg|jpeg|png|tif|tiff)$") == false) {
						correctedFilename += ".jpg";
					}
					upo.setCorrectedFilename(correctedFilename);
				} else {
					correctedFilename = getFilenamePart(s);
					URLCodec decoder = new URLCodec("utf-8");
					try {
						correctedFilename = decoder.decode(correctedFilename);
					} catch (DecoderException e) {
					}
					if (correctedFilename.length() > 0 && correctedFilename.matches(".+\\.(bmp|gif|jpe|jpg|jpeg|png|tif|tiff)$") == false) {
						correctedFilename += ".jpg";
					}
					upo.setCorrectedFilename(correctedFilename);
				}
				if (correctedFilename.length() == 0) {
					upo.getPic().setRenameWithContentDisposition(true);
				}
			} else {
				throw new HostImageUrlNotFoundException(Localization.getString("ErrorImageURL"));
			}
		}
	}

	private String getFilenamePart(String url) {
		int pos = url.lastIndexOf("/");
		if (pos > -1) {
			try {
				return url.substring(pos + 1);
			} catch (Exception e) {
			}
		}
		return "";
	}
}

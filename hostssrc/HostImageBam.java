import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostFileTemporaryOfflineException;
import ch.supertomcat.bh.exceptions.HostIOException;
import ch.supertomcat.bh.exceptions.HostImageUrlNotFoundException;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.queue.DownloadRestriction;
import ch.supertomcat.bh.rules.RuleRegExp;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Host class for ImageBam
 * 
 * @version 4.3
 */
public class HostImageBam extends Host implements IHoster {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "4.3";

	/**
	 * Name dieser Klasse
	 */
	public static final String NAME = "HostImageBam";

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Kompiliertes Muster
	 */
	private Pattern urlPattern;

	private String[] errorMessages = new String[] { "This server will be right back!" };

	private RuleRegExp regexServerMaintenance;

	private RuleRegExp regexImage;

	private RuleRegExp regexImage2;

	private RuleRegExp regexImage3;

	private RuleRegExp regexFilename2;

	private RuleRegExp regexFilename;

	private Pattern fileExtensionPattern = Pattern.compile("(?i).+\\.(bmp|gif|jpe|jpg|jpeg|png|tif|tiff|webp)$");

	/**
	 * Konstruktor
	 */
	public HostImageBam() {
		super(NAME, VERSION);
		urlPattern = Pattern.compile("^https?://(www\\.)?imagebam\\.com/(?:image|view)/[a-zA-Z0-9]+(/)?");

		regexServerMaintenance = new RuleRegExp();
		regexServerMaintenance.setSearch("<b>(ImageBam is currently offline.)</b><br><br>(.+?)<br><br>(.+?)<br><br>");
		regexServerMaintenance.setReplace("$1 $2 $3");

		regexImage = new RuleRegExp();
		regexImage.setSearch("(?i)<img.+?class=\"image\".+?id=\"i[0-9]+\".+?src=\"(.+?)\"");
		regexImage.setReplace("$1");

		regexImage2 = new RuleRegExp();
		regexImage2.setSearch("<meta property=\"og:image\" content=\"(.+?)\"/>");
		regexImage2.setReplace("$1");

		regexFilename = new RuleRegExp();
		regexFilename.setSearch(".+?filename=(.+)");
		regexFilename.setReplace("$1");

		regexImage3 = new RuleRegExp();
		regexImage3.setSearch("(?i)<img src=\"(.+?)\".+?alt=\"(.+?)\".+?class=\"main-image");
		regexImage3.setReplace("$1");

		regexFilename2 = new RuleRegExp();
		regexFilename2.setSearch("(?i)<img src=\"(.+?)\".+?alt=\"(.+?)\".+?class=\"main-image");
		regexFilename2.setReplace("$2");

		addRestriction(new DownloadRestriction("imagebam.com", 6));
	}

	@Override
	public boolean isFromThisHoster(String url) {
		Matcher urlMatcher = urlPattern.matcher(url);
		if (urlMatcher.matches()) {
			return true;
		}
		return false;
	}

	@Override
	public String getFilenameFromURL(String url) {
		if (!isFromThisHoster(url)) {
			return "";
		}
		return "";
	}

	/**
	 * URL parsen
	 * 
	 * @param upo URL Parse Object
	 * @param cookieStore Cookie Store
	 * @return URL or empty String if not found or null if continue
	 * @throws HostException
	 */
	private String parseURL(URLParseObject upo, BasicCookieStore cookieStore) throws HostException {
		try (CloseableHttpClient client = getProxyManager().getHTTPClientBuilder().setDefaultCookieStore(cookieStore).build()) {
			String page = downloadContainerPage(getName(), upo.getContainerURL(), upo.getContainerURL(), null, client);

			String correctedFilename = "";

			String downloadURL = regexImage.doPageSourcecodeReplace(page, 0, upo.getContainerURL(), null);

			if (downloadURL.isEmpty()) {
				for (String errorMessage : errorMessages) {
					if (page.indexOf(errorMessage) > -1) {
						throw new HostFileTemporaryOfflineException(errorMessage);
					}
				}

				String errorMessage = regexServerMaintenance.doPageSourcecodeReplace(page, 0, upo.getContainerURL(), null);
				if (!errorMessage.isEmpty()) {
					throw new HostFileTemporaryOfflineException(errorMessage);
				}

				downloadURL = regexImage2.doPageSourcecodeReplace(page, 0, upo.getContainerURL(), null);
				if (!downloadURL.isEmpty()) {
					logger.info("Download-URL found with alternative pattern: {}", downloadURL);
				} else {
					downloadURL = regexImage3.doPageSourcecodeReplace(page, 0, upo.getContainerURL(), null);
					if (!downloadURL.isEmpty()) {
						logger.info("Download-URL found with alternative 3 pattern: {}", downloadURL);

						correctedFilename = regexFilename2.doPageSourcecodeReplace(page, 0, upo.getContainerURL(), null);
					}
				}
			}

			downloadURL = downloadURL.replace("&amp;", "&");
			if (!downloadURL.isEmpty()) {
				upo.setDirectLink(downloadURL);
				upo.setCorrectedFilename(correctedFilename);
			} else {
				if (page.contains("Continue to your image")) {
					return null;
				}
			}

			return downloadURL;
		} catch (Exception e) {
			throw new HostIOException(getName() + ": Container-Page: " + e.getMessage(), e);
		}
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		if (isFromThisHoster(upo.getContainerURL())) {
			BasicCookieStore cookieStore = new BasicCookieStore();

			String downloadURL = parseURL(upo, cookieStore);
			if (downloadURL == null) {
				downloadURL = parseURL(upo, cookieStore);
			}

			if (downloadURL != null && !downloadURL.isEmpty()) {
				upo.setDirectLink(downloadURL);

				String correctedFilename = upo.getCorrectedFilename();

				if (correctedFilename.isEmpty()) {
					// Read out filename from url
					if (downloadURL.contains("filename")) {
						correctedFilename = decodeFilename(regexFilename.doURLReplace(downloadURL, null));
					} else {
						correctedFilename = decodeFilename(getFilenamePart(downloadURL));
					}
				}

				if (correctedFilename.isEmpty()) {
					// Filename not found in URL, try to use content disposition
					upo.getPic().setRenameWithContentDisposition(true);
				} else {
					/*
					 * Imagebam does in some cases cut long filenames and also the extension is removed then.
					 * So we just add jpg in that case.
					 * This is not a good fix, as it would also add jpg in case it's a png for example.
					 * But there is not any information on the container-page or the http-header what
					 * type it is.
					 * So this fix is better than nothing.
					 */
					if (!correctedFilename.isEmpty() && !fileExtensionPattern.matcher(correctedFilename).matches()) {
						correctedFilename += ".jpg";
					}

					upo.setCorrectedFilename(correctedFilename);
				}
			} else {
				if (downloadURL == null) {
					throw new HostImageUrlNotFoundException("Failed to bypass \"Continue to your image\"");
				} else {
					throw new HostImageUrlNotFoundException(Localization.getString("ErrorImageURL"));
				}
			}
		}
	}

	private String decodeFilename(String filename) {
		URLCodec decoder = new URLCodec("utf-8");
		try {
			return decoder.decode(filename);
		} catch (DecoderException e) {
			return filename;
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

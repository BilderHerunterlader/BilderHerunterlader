package ch.supertomcat.bh.hoster;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostWrongContentTypeException;
import ch.supertomcat.bh.hoster.hosteroptions.IHosterOptions;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcattools.applicationtool.ApplicationProperties;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.httptools.HTTPTool;

/**
 * Host-Klasse fuer beliebige Dateien und speziell fuer Bilder die nicht auf einem Image-Hoster gehostet sind.
 * 
 * @version 3.7
 */
public class HostzDefaultFiles extends Host implements IHoster, IHosterOptions {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "3.7";

	/**
	 * Name dieser Klasse
	 */
	public static final String NAME = "HostDefaultFiles";

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Kompiliertes Muster
	 */
	private Pattern urlPatternImages;

	private Pattern urlPatternVideo;

	private Pattern urlPatternAudio;

	private Pattern urlPatternArchive;

	/**
	 * Kompilierte Muster (Aus Text-Datei geladen)
	 */
	private List<Pattern> urlPatterns = new ArrayList<>();

	private boolean checkContentType = false;

	private boolean allFileTypes = false;

	private boolean images = true;

	private boolean video = true;

	private boolean audio = true;

	private boolean archive = true;

	/**
	 * Konstruktor
	 */
	public HostzDefaultFiles() {
		super(NAME, VERSION);
		String strImages = "(?:bmp|gif|jpe|jpg|jpeg|png|tif|tiff)";
		String strVideo = "(?:3g2|3gp|3gp2|3gpp|amr|asf|divx|evo|flv|hdmov|m2t|m2ts|m2v|m4v|mkv|m1v|mov|mp2v|mp4|mpe|mpeg|mpg|mts|ogm|ogv|pva|pss|qt|rm|ram|rpm|rmm|ts|tp|tpr|vob|wmv|wmp)";
		String strAudio = "(?:aac|ac3|au|dts|flac|m1a|m2a|m4a|m4b|mid|midi|mka|mp2|mp3|mpa|oga|ogg|ra|rmi|snd|wav|wma)";
		String strArchive = "(?:7z|arj|bz2|bzip2|cab|cpio|deb|dmg|gz|gzip|hfs|iso|lha|lzh|lzma|rar|rpm|split|swm|tar|taz|tbz|tbz2|tgz|tpz|wim|xar|z|zip)";
		String strPatternPrefix = "^.+?\\.";

		urlPatternImages = Pattern.compile(strPatternPrefix + strImages + "$", Pattern.CASE_INSENSITIVE);
		urlPatternVideo = Pattern.compile(strPatternPrefix + strVideo + "$", Pattern.CASE_INSENSITIVE);
		urlPatternAudio = Pattern.compile(strPatternPrefix + strAudio + "$", Pattern.CASE_INSENSITIVE);
		urlPatternArchive = Pattern.compile(strPatternPrefix + strArchive + "$", Pattern.CASE_INSENSITIVE);

		File file = new File(ApplicationProperties.getProperty("ApplicationPath"), "hosts/HostzDefaultImages.txt");
		urlPatterns.addAll(BHUtil.readPatternsFromTextFile(file, Charset.defaultCharset(), true));

		try {
			checkContentType = getBooleanOptionValue("Hosts.CheckContentTypeDefaultImages");
		} catch (Exception e) {
			try {
				setBooleanOptionValue("Hosts.CheckContentTypeDefaultImages", false);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			allFileTypes = getBooleanOptionValue(NAME + ".allFileTypes");
		} catch (Exception e) {
			try {
				setBooleanOptionValue(NAME + ".allFileTypes", false);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			images = getBooleanOptionValue(NAME + ".images");
		} catch (Exception e) {
			try {
				setBooleanOptionValue(NAME + ".images", true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			video = getBooleanOptionValue(NAME + ".video");
		} catch (Exception e) {
			try {
				setBooleanOptionValue(NAME + ".video", true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			audio = getBooleanOptionValue(NAME + ".audio");
		} catch (Exception e) {
			try {
				setBooleanOptionValue(NAME + ".audio", true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			archive = getBooleanOptionValue(NAME + ".archive");
		} catch (Exception e) {
			try {
				setBooleanOptionValue(NAME + ".archive", true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
	}

	@Override
	public boolean isFromThisHoster(String url) {
		try {
			return isDefaultFile(url, false);
		} catch (HostWrongContentTypeException e) {
			logger.debug("URL has wrong content type: {}", url, e);
			return false;
		} catch (HostException e) {
			logger.error("Could not check content type of URL: {}", url, e);
			return false;
		}
	}

	/**
	 * Checks if the URL is from this hoster
	 * 
	 * @param url URL
	 * 
	 * @param checkType True if content type should be checked
	 * @return True if URL is from this hoster, false otherwise
	 * @throws HostException
	 */
	private boolean isDefaultFile(String url, boolean checkType) throws HostException {
		if (!isEnabled()) {
			return false;
		}

		if (allFileTypes) {
			return true;
		}

		String filename = getFilenameFromURL(url);

		// Check for images, videos, audio and archives
		if (images && !filename.isEmpty()) {
			if (urlPatternImages.matcher(filename).matches()) {
				if (checkType) {
					checkContentType(url, "image/", Localization.getString("FileIsNotImage"));
				}
				return true;
			}
		}
		if (video && !filename.isEmpty()) {
			if (urlPatternVideo.matcher(filename).matches()) {
				if (checkType) {
					checkContentType(url, "video/", Localization.getString("FileIsNotVideo"));
				}
				return true;
			}
		}
		if (audio && !filename.isEmpty()) {
			if (urlPatternAudio.matcher(filename).matches()) {
				if (checkType) {
					checkContentType(url, "audio/", Localization.getString("FileIsNotAudio"));
				}
				return true;
			}
		}
		if (archive && !filename.isEmpty()) {
			if (urlPatternArchive.matcher(filename).matches()) {
				return true;
			}
		}

		for (Pattern pattern : urlPatterns) {
			if (pattern.matcher(url).matches()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String getFilenameFromURL(String url) {
		return HTTPTool.getFilenameFromURL(url, "");
	}

	/**
	 * Requests header from URL
	 * 
	 * @param url URL
	 * @return Content Type
	 * @throws HostException
	 */
	private String requestContentType(String url) throws HostException {
		String cookies = CookieManager.getCookies(url);
		url = HTTPTool.encodeURL(url);
		HttpHead method = null;
		try (CloseableHttpClient client = ProxyManager.instance().getHTTPClient()) {
			method = new HttpHead(url);
			method.setHeader("User-Agent", SettingsManager.instance().getUserAgent());
			if (!cookies.isEmpty()) {
				method.setHeader("Cookie", cookies);
			}
			try (CloseableHttpResponse response = client.execute(method)) {
				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode < 200 && statusCode >= 400) {
					method.abort();
					throw new HostException("HTTP-Error: " + statusCode);
				}

				// Abfrage des MIME Types
				String contentType = response.getFirstHeader("Content-Type").getValue();
				EntityUtils.consume(response.getEntity());
				return contentType;
			}
		} catch (Exception e) {
			throw new HostException("Could not request header for URL: " + url, e);
		} finally {
			if (method != null) {
				method.abort();
			}
		}
	}

	/**
	 * Checks if the content type is the expected type. For the check the Header of the URL is requested. If the content type is not the excepted type an
	 * Exception is thrown. Also if the type could not be checked an Exception is thrown.
	 * 
	 * @param url URL
	 * @param expectedType Expected Type
	 * @param errorMessage Error Message
	 * @throws HostException
	 */
	private void checkContentType(String url, String expectedType, String errorMessage) throws HostException {
		String contentType = requestContentType(url);
		if (!contentType.startsWith(expectedType)) {
			throw new HostWrongContentTypeException(errorMessage, expectedType, contentType);
		}
	}

	@Override
	public synchronized void openOptionsDialog() {
		HostzDefaultFilesOptionsDialog dialog = new HostzDefaultFilesOptionsDialog(checkContentType, allFileTypes, images, video, audio, archive);
		if (dialog.isOkPressed()) {
			checkContentType = dialog.isCheckContentType();
			allFileTypes = dialog.isAllFileTypes();
			images = dialog.isImage();
			video = dialog.isVideo();
			audio = dialog.isAudio();
			archive = dialog.isArchive();

			try {
				setBooleanOptionValue("Hosts.CheckContentTypeDefaultImages", checkContentType);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
			try {
				setBooleanOptionValue(NAME + ".allFileTypes", allFileTypes);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
			try {
				setBooleanOptionValue(NAME + ".images", images);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
			try {
				setBooleanOptionValue(NAME + ".video", video);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
			try {
				setBooleanOptionValue(NAME + ".audio", audio);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
			try {
				setBooleanOptionValue(NAME + ".archive", archive);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
			SettingsManager.instance().writeSettings(true);
		}
	}

	private boolean getBooleanOptionValue(String name) throws Exception {
		return SettingsManager.instance().getBooleanValue(name);
	}

	private void setBooleanOptionValue(String name, boolean value) throws Exception {
		SettingsManager.instance().setOptionValue(name, value);
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		if (isDefaultFile(upo.getContainerURL(), checkContentType)) {
			upo.setDirectLink(upo.getContainerURL()); // URL
			if (!urlPatterns.isEmpty()) {
				upo.getPic().setRenameWithContentDisposition(true);
			}
		}
	}
}

package ch.supertomcat.bh.hoster.hostimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.StatusLine;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostHttpIOException;
import ch.supertomcat.bh.exceptions.HostWrongContentTypeException;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.settings.BHSettingsListener;
import ch.supertomcat.bh.settings.xml.DetectionSettings;
import ch.supertomcat.bh.settings.xml.LookAndFeelSetting;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;
import ch.supertomcat.supertomcatutils.regex.RegexSearch;

/**
 * Host-Klasse fuer beliebige Dateien und speziell fuer Bilder die nicht auf einem Image-Hoster gehostet sind.
 * 
 * @version 3.8
 */
public class HostDefaultFiles extends Host implements IHoster {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "3.8";

	/**
	 * Name dieser Klasse
	 */
	public static final String NAME = "HostDefaultFiles";

	/**
	 * Kompiliertes Muster
	 */
	private Pattern urlPatternImages;

	private Pattern urlPatternVideo;

	private Pattern urlPatternAudio;

	private Pattern urlPatternArchive;

	private List<RegexSearch> urlPatterns = new ArrayList<>();

	private boolean checkContentType = false;

	private boolean allFileTypes = false;

	private boolean images = true;

	private boolean video = true;

	private boolean audio = true;

	private boolean archive = true;

	/**
	 * Konstruktor
	 */
	public HostDefaultFiles() {
		super(NAME, VERSION);
		String strImages = "(?:avif|bmp|gif|jpe|jpg|jpeg|jfif|pjpeg|pjp|apng|png|tif|tiff|webp)";
		String strVideo = "(?:3g2|3gp|3gp2|3gpp|amr|asf|divx|evo|flv|hdmov|m2t|m2ts|m2v|m4v|mkv|m1v|mov|mp2v|mp4|mpe|mpeg|mpg|mts|ogm|ogv|pva|pss|qt|rm|ram|rpm|rmm|ts|tp|tpr|vob|wmv|wmp)";
		String strAudio = "(?:aac|ac3|au|dts|flac|m1a|m2a|m4a|m4b|mid|midi|mka|mp2|mp3|mpa|oga|ogg|ra|rmi|snd|wav|wma)";
		String strArchive = "(?:7z|arj|bz2|bzip2|cab|cpio|deb|dmg|gz|gzip|hfs|iso|lha|lzh|lzma|rar|rpm|split|swm|tar|taz|tbz|tbz2|tgz|tpz|wim|xar|z|zip)";
		String strPatternPrefix = "^.+?\\.";

		urlPatternImages = Pattern.compile(strPatternPrefix + strImages + "$", Pattern.CASE_INSENSITIVE);
		urlPatternVideo = Pattern.compile(strPatternPrefix + strVideo + "$", Pattern.CASE_INSENSITIVE);
		urlPatternAudio = Pattern.compile(strPatternPrefix + strAudio + "$", Pattern.CASE_INSENSITIVE);
		urlPatternArchive = Pattern.compile(strPatternPrefix + strArchive + "$", Pattern.CASE_INSENSITIVE);

		initFromSettings();

		getSettingsManager().addSettingsListener(new BHSettingsListener() {

			@Override
			public void settingsChanged() {
				initFromSettings();
			}

			@Override
			public void lookAndFeelChanged(LookAndFeelSetting lookAndFeel) {
				// Nothing to do
			}
		});
	}

	/**
	 * Init from settings
	 */
	private void initFromSettings() {
		DetectionSettings detectionSettings = getSettingsManager().getDetectionSettings();
		checkContentType = detectionSettings.isCheckContentType();
		allFileTypes = detectionSettings.isAllFileTypes();
		images = detectionSettings.isImage();
		video = detectionSettings.isVideo();
		audio = detectionSettings.isAudio();
		archive = detectionSettings.isArchive();

		urlPatterns = new ArrayList<>(getSettingsManager().getDetectionPatterns());
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
		if (images && !filename.isEmpty() && urlPatternImages.matcher(filename).matches()) {
			if (checkType) {
				checkContentType(url, "image/", Localization.getString("FileIsNotImage"));
			}
			return true;
		}

		if (video && !filename.isEmpty() && urlPatternVideo.matcher(filename).matches()) {
			if (checkType) {
				checkContentType(url, "video/", Localization.getString("FileIsNotVideo"));
			}
			return true;
		}

		if (audio && !filename.isEmpty() && urlPatternAudio.matcher(filename).matches()) {
			if (checkType) {
				checkContentType(url, "audio/", Localization.getString("FileIsNotAudio"));
			}
			return true;
		}

		if (archive && !filename.isEmpty() && urlPatternArchive.matcher(filename).matches()) {
			return true;
		}

		return urlPatterns.stream().anyMatch(x -> x.match(url));
	}

	@Override
	public String getFilenameFromURL(String url) {
		return HTTPUtil.getFilenameFromURL(url, "");
	}

	/**
	 * Requests header from URL
	 * 
	 * @param url URL
	 * @return Content Type
	 * @throws HostException
	 */
	private String requestContentType(String url) throws HostException {
		String encodedURL = HTTPUtil.encodeURL(url);
		try (CloseableHttpClient client = getProxyManager().getHTTPClient()) {
			HttpHead method = new HttpHead(encodedURL);
			method.setHeader(HttpHeaders.USER_AGENT, getSettingsManager().getUserAgent());

			CookieStore cookieStore = getCookieManager().getCookieStore();
			HttpClientContext context = ContextBuilder.create().useCookieStore(cookieStore).build();
			getCookieManager().fillCookies(url, cookieStore);

			return client.execute(method, context, response -> {
				StatusLine statusLine = new StatusLine(response);
				int statusCode = statusLine.getStatusCode();

				if (statusCode < 200 && statusCode >= 400) {
					throw new HostHttpIOException("HTTP-Error: " + statusCode);
				}

				// Abfrage des MIME Types
				return response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
			});
		} catch (Exception e) {
			throw new HostException("Could not request header for URL: " + url, e);
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
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		if (isDefaultFile(upo.getContainerURL(), checkContentType)) {
			upo.setDirectLink(upo.getContainerURL()); // URL
			if (!urlPatterns.isEmpty()) {
				upo.getPic().setRenameWithContentDisposition(true);
			}
		}
	}
}

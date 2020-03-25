import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostIOException;
import ch.supertomcat.bh.gui.queue.DownloadAddDialog;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.IHosterURLAdder;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.rules.RuleRegExp;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Host class for BabesBoard
 * 
 * @version 3.0
 */
public class HostBabesBoard extends Host implements IHoster, IHosterURLAdder {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "3.0";

	/**
	 * Name dieser Klasse
	 */
	public static final String NAME = "HostBabesBoard";

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Kompilierte Muster (Aus Text-Datei geladen)
	 */
	private Pattern urlPatterns[] = null;

	private Pattern urlContainerPattern = null;

	private Pattern urlInternalContainerPattern = null;

	private RuleRegExp regexBabeName = new RuleRegExp();

	private RuleRegExp regexDate = new RuleRegExp();

	private RuleRegExp regexImage = new RuleRegExp();

	private RuleRegExp regexAjaxFirstReturn = new RuleRegExp();

	private RuleRegExp regexBabeID = new RuleRegExp();

	private RuleRegExp regexPerPageStorage = new RuleRegExp();

	private RuleRegExp regexAjaxURI = new RuleRegExp();

	/**
	 * Konstruktor
	 */
	public HostBabesBoard() {
		super(NAME, VERSION);
		urlPatterns = new Pattern[2];
		urlPatterns[0] = Pattern.compile("^http://babeshack([0-9])?\\.babes-board\\.ch/shack.php\\?image=[0-9a-z]+\\..*$", Pattern.CASE_INSENSITIVE);
		urlPatterns[1] = Pattern.compile("^http://babeshack([0-9])?\\.babes-board\\.ch/f/[0-9]+/[0-9]+/.*$", Pattern.CASE_INSENSITIVE);

		urlContainerPattern = Pattern.compile("^http://(www\\.)?babes-board.ch/babegallery/[^/]+/(\\?sid=[^#]*)?(#[0-9]+)?", Pattern.CASE_INSENSITIVE);

		urlInternalContainerPattern = Pattern.compile("^(http://(www\\.)?babes-board.ch/babegallery/.*?)babeID=(.*?)&pageNumber=(.*?)&imagesPerPage=(.*)", Pattern.CASE_INSENSITIVE);
		regexBabeName.setSearch("<cmd n=\"[0-9a-zA-Z]+\" t=\"babename\" p=\"innerHTML\">(.*?)</cmd>");
		regexBabeName.setReplace("$1");
		regexDate.setSearch("<!\\[CDATA\\[Bilder vom <b>([0-9]{2}).([0-9]{2}).([0-9]{4})</b>( bis <b>([0-9]{2}).([0-9]{2}).([0-9]{4})</b>)?\\]\\]></cmd>");
		regexDate.setReplace("$3.$2.$1_$7.$6.$5");
		regexImage.setSearch("<a href=\"([0-9]+(\\.[0-9]+)?kb[:])?(.*?)\" class=\"");
		regexImage.setReplace("$3");
		regexAjaxFirstReturn.setSearch("<cmd n=\"[a-z]+\" t=\"imageXY\"><xjxobj><e><k>[0-9]+</k><v>[0-9]+</v></e><e><k>[0-9]+</k><v>[0-9]+</v></e><e><k>[0-9]+</k><v>([0-9]+)</v></e></xjxobj></cmd>");
		regexAjaxFirstReturn.setReplace("$1");

		regexBabeID.setSearch("<input id=\"babeid\" type=\"hidden\" value=\"(.*?)\">");
		regexBabeID.setReplace("$1");
		regexPerPageStorage.setSearch("<input id=\"perpagestorage\" type=\"hidden\" value=\"(.*?)\">");
		regexPerPageStorage.setReplace("$1");
		regexAjaxURI.setSearch("var xajaxRequestUri=\"(.*)\";");
		regexAjaxURI.setReplace("$URL$1");
	}

	@Override
	public boolean isFromThisHoster(String url) {
		Matcher urlMatcher;
		for (int i = 0; i < urlPatterns.length; i++) {
			urlMatcher = urlPatterns[i].matcher(url);
			if (urlMatcher.matches()) {
				return true;
			}
		}
		urlMatcher = urlContainerPattern.matcher(url);
		if (urlMatcher.matches()) {
			return true;
		}
		urlMatcher = urlInternalContainerPattern.matcher(url);
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
		String retval = url.substring(url.lastIndexOf("/") + 1);
		return retval;
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		if (isFromThisHoster(upo.getContainerURL())) {
			upo.setDirectLink(upo.getContainerURL());
			upo.getPic().setRenameWithContentDisposition(true);
		}
	}

	@Override
	public List<URL> isFromThisHoster(URL url, AtomicBoolean isFromThisHoster, ProgressObserver progress) throws Exception {
		Matcher urlMatcherInternalContainer = urlInternalContainerPattern.matcher(url.getURL());
		boolean bInternalContainer = urlMatcherInternalContainer.matches();
		boolean bContainer = false;
		if (bInternalContainer == false) {
			Matcher urlMatcherContainer = urlContainerPattern.matcher(url.getURL());
			bContainer = urlMatcherContainer.matches();
		}

		if (bContainer == false && bInternalContainer == false) {
			isFromThisHoster.set(true);
			return null;
		}

		isFromThisHoster.set(false);

		if ((bContainer == true) && (url.getThreadURL().equals(DownloadAddDialog.DOWNLOAD_ADD_DIALOG_THREAD_URL) == false)) {
			return null;
		}

		if (bContainer) {
			String fristPageSourceCode = downloadContainerPage(url.getURL(), "");

			String babeID = regexBabeID.doPageSourcecodeReplace(fristPageSourceCode, 0, url.getURL(), null);

			String imagesPerPage = regexPerPageStorage.doPageSourcecodeReplace(fristPageSourceCode, 0, url.getURL(), null);

			String requestURL = regexAjaxURI.doPageSourcecodeReplace(fristPageSourceCode, 0, url.getURL(), null);

			AtomicInteger iwMax = new AtomicInteger(0);
			getLinksFromPage(requestURL, babeID, "1", imagesPerPage, iwMax, true);

			List<URL> internalContainerURLs = new ArrayList<>();

			for (int i = 1; i <= iwMax.get(); i++) {
				String pageNumber = String.valueOf(i);
				String internalContainerURL = requestURL + "babeID=" + babeID + "&pageNumber=" + pageNumber + "&imagesPerPage=" + imagesPerPage;
				internalContainerURLs.add(new URL(internalContainerURL));
			}

			return internalContainerURLs;
		} else if (bInternalContainer) {
			String requestURL = urlMatcherInternalContainer.replaceAll("$1");
			String babeID = urlMatcherInternalContainer.replaceAll("$3");
			String pageNumber = urlMatcherInternalContainer.replaceAll("$4");
			String imagesPerPage = urlMatcherInternalContainer.replaceAll("$5");

			List<URL> downloadURLs = new ArrayList<>();
			AtomicInteger iwMax1 = new AtomicInteger(0);

			try {
				ArrayList<URL> urlsFromPage = getLinksFromPage(requestURL, babeID, pageNumber, imagesPerPage, iwMax1, false);
				downloadURLs.addAll(urlsFromPage);
			} catch (HostIOException hioe) {
				logger.error(hioe.getMessage(), hioe);
			}

			return downloadURLs;
		}
		return null;
	}

	private ArrayList<URL> getLinksFromPage(String url, String babeID, String page, String imagesPerPage, AtomicInteger iwMax, boolean firstLoad) throws HostException {
		String cookies = CookieManager.getCookies(url);
		String encodedURL = HTTPUtil.encodeURL(url);
		HttpClientBuilder clientBuilder = ProxyManager.instance().getHTTPClientBuilder();
		clientBuilder.disableRedirectHandling();
		HttpPost method = null;
		try (CloseableHttpClient client = clientBuilder.build()) {
			method = new HttpPost(encodedURL);
			method.setHeader("User-Agent", SettingsManager.instance().getUserAgent());
			if (cookies.length() > 0) {
				method.setHeader("Cookie", cookies);
			}

			List<NameValuePair> data = new ArrayList<>();
			data.add(new BasicNameValuePair("xajax", "loadImages"));
			data.add(new BasicNameValuePair("xajaxr", "1234454788978")); // Number to generate for each request
			data.add(new BasicNameValuePair("xajaxargs[]", babeID)); // Babeindex
			data.add(new BasicNameValuePair("xajaxargs[]", page)); // Page
			data.add(new BasicNameValuePair("xajaxargs[]", imagesPerPage)); // Images per page
			data.add(new BasicNameValuePair("xajaxargs[]", "0")); // showhidden
			data.add(new BasicNameValuePair("xajaxargs[]", "")); // qualitystorage
			method.setEntity(new UrlEncodedFormEntity(data, StandardCharsets.UTF_8));

			String pageCode = "";
			try (CloseableHttpResponse response = client.execute(method)) {
				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode < 200 || statusCode >= 300) {
					method.abort();
					throw new HostIOException("HTTP-Error: " + statusCode);
				}

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					pageCode = EntityUtils.toString(entity);
					EntityUtils.consume(response.getEntity());
				}
			}

			if (firstLoad) {
				String max = regexAjaxFirstReturn.doPageSourcecodeReplace(pageCode, 0, url, null);
				int iMax = 0;
				try {
					int iMaxImage = Integer.parseInt(max);
					int iImagesPerPage = Integer.parseInt(imagesPerPage);
					iMax = (int)Math.ceil((double)iMaxImage / (double)iImagesPerPage);
				} catch (NumberFormatException nfe) {
					logger.error(nfe.getMessage(), nfe);
				}
				iwMax.set(iMax);
				return null;
			}

			String strBabeName = regexBabeName.doPageSourcecodeReplace(pageCode, 0, url, null);

			String strDate = regexDate.doPageSourcecodeReplace(pageCode, 0, url, null);

			String strRootDir = SettingsManager.instance().getSavePath() + strBabeName + FileUtil.FILE_SEPERATOR;

			String strDirectory = strRootDir + "BabesBoard" + FileUtil.FILE_SEPERATOR + strDate + FileUtil.FILE_SEPERATOR;

			ArrayList<URL> downloadURLs = new ArrayList<>();

			String imageURL = "";
			int start = 0;

			do {
				int pos = regexImage.doPageSourcecodeSearch(pageCode, start);
				imageURL = regexImage.doPageSourcecodeReplace(pageCode, pos, url, null);
				if (imageURL.length() > 0) {
					URL urlToAdd = new URL(imageURL);
					urlToAdd.setThreadURL(url);
					urlToAdd.setTargetPath(strDirectory);
					downloadURLs.add(urlToAdd);
					start = pos + 1;
				}
			} while (imageURL.length() > 0);

			return downloadURLs;
		} catch (Exception e) {
			throw new HostIOException(NAME + ": Container-Page: " + e.getMessage(), e);
		} finally {
			if (method != null) {
				method.abort();
			}
		}
	}
}

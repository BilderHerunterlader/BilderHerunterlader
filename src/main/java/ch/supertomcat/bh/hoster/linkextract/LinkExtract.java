package ch.supertomcat.bh.hoster.linkextract;

import static ch.supertomcat.bh.hoster.linkextract.ExtractTools.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.message.StatusLine;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import ch.supertomcat.bh.cookies.CookieManager;
import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostHttpIOException;
import ch.supertomcat.bh.exceptions.HostIOException;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;

/**
 * Utility class for link extraction
 */
public final class LinkExtract {
	/**
	 * Constructor
	 */
	private LinkExtract() {
	}

	/**
	 * Adds links to the vector
	 * 
	 * @param url URL
	 * @param filter Filter
	 * @param extractedURLs URLs
	 * @param in HTML-Inputstream
	 */
	public static void addLinks(String url, ILinkExtractFilter filter, List<URL> extractedURLs, InputStream in) {
		Tidy tidy = new Tidy();
		tidy.setShowWarnings(false);
		tidy.setShowErrors(0);
		tidy.setQuiet(true);
		tidy.setInputEncoding("UTF-8");
		Document node = tidy.parseDOM(in, null);
		addLinks(url, filter, extractedURLs, node);
	}

	/**
	 * Adds links to the vector
	 * 
	 * @param url URL
	 * @param filter Filter
	 * @param extractedURLs URLs
	 * @param node Document
	 */
	public static void addLinks(String url, ILinkExtractFilter filter, List<URL> extractedURLs, Document node) {
		// button is used for youtube-search
		String[] tagNames = { "a", "button" };

		/*
		 * Get the links
		 */
		for (String tagName : tagNames) {
			NodeList nl = node.getElementsByTagName(tagName);
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				String link = getAttributeValueFromNode(n, "href");
				if (link != null && link.length() > 0) {
					URL extractedURL = new URL(link);
					if (!link.startsWith("http://") && !link.startsWith("https://")) {
						// If the link was relative we have to correct that
						extractedURL = convertURLFromRelativeToAbsolute(url, extractedURL);
					}
					extractedURL.setThreadURL(url);
					if (filter == null || filter.isLinkAccepted(n, node, extractedURL, url)) {
						// Get the childnodes
						NodeList thl = n.getChildNodes();
						Node th = null;
						int thi = 0;
						for (int x = 0; x < thl.getLength(); x++) {
							if (thl.item(x).getNodeName().equals("img")) {
								// Get the img-elemnt inside the a-element if available
								th = thl.item(x);
								// Count them!
								thi++;
							}
						}

						/*
						 * Get the src from the img-element inside the a-element
						 * We do this if there is only 1 img-element!
						 * This is for the thumbnail-URL and if they were 2 or
						 * more img-elements, we can't know which of them is
						 * the right.
						 */
						String tt = "";
						if ((th != null) && (thi == 1)) {
							String st = getAttributeValueFromNode(th, "src");
							tt = st;
						}
						extractedURL.setThumb(tt);

						extractedURLs.add(extractedURL);
					}
				}
			}
		}
	}

	/**
	 * Method to get all links from a page
	 * If the filter is null all Links are accepted
	 * 
	 * @param url URL
	 * @param referrer Referrer
	 * @param filter Filter
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param cookieManager Cookie Manager
	 * @return Array with links
	 * @throws HostException
	 */
	public static List<URL> getLinks(String url, String referrer, ILinkExtractFilter filter, ProxyManager proxyManager, SettingsManager settingsManager,
			CookieManager cookieManager) throws HostException {
		try (CloseableHttpClient client = proxyManager.getHTTPClient()) {
			String encodedURL = HTTPUtil.encodeURL(url);
			HttpGet method = new HttpGet(encodedURL);
			// Verbindung oeffnen
			RequestConfig.Builder requestConfigBuilder = proxyManager.getDefaultRequestConfigBuilder();
			requestConfigBuilder.setMaxRedirects(10);
			method.setConfig(requestConfigBuilder.build());
			method.setHeader("User-Agent", settingsManager.getUserAgent());
			if (referrer.length() > 0) {
				method.setHeader("Referer", referrer);
			}

			BasicCookieStore cookieStore = new BasicCookieStore();
			HttpClientContext context = ContextBuilder.create().useCookieStore(cookieStore).build();
			cookieManager.fillCookies(url, cookieStore);

			return client.execute(method, context, response -> {
				StatusLine statusLine = new StatusLine(response);
				int statusCode = statusLine.getStatusCode();

				if (statusCode != 200) {
					throw new HostHttpIOException("LinkExtract: Container-Page: HTTP-Error: " + statusCode);
				}

				List<URL> extractedURLs = new ArrayList<>();

				// Inputstream oeffnen
				try (@SuppressWarnings("resource")
				InputStream in = response.getEntity().getContent()) {
					addLinks(encodedURL, filter, extractedURLs, in);
				}

				return extractedURLs;
			});
		} catch (IOException e) {
			throw new HostIOException("LinkExtract: Container-Page: " + url + " :" + e.getMessage(), e);
		}
	}

	/**
	 * @param url URL
	 * @param referrer Referrer
	 * @param extractConfig ExtractConfig
	 * @param progress Progress
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param cookieManager Cookie Manager
	 * @return URLs
	 * @throws Exception
	 */
	public static List<URL> getLinksRecursive(URL url, String referrer, ExtractConfig extractConfig, ProgressObserver progress, ProxyManager proxyManager, SettingsManager settingsManager,
			CookieManager cookieManager) throws Exception {
		List<URL> links = new ArrayList<>();

		List<ExtractConfigWhitelist> whitelists = extractConfig.getWhitelists();
		ILinkExtractFilter filter = getFilterForContainerURL(url.getURL(), whitelists);
		if (filter == null) {
			return links;
		}

		links.add(url);

		List<URL> downloadedLinks = new ArrayList<>();
		for (int i = 0; i < links.size(); i++) {
			filter = getFilterForContainerURL(links.get(i).getURL(), whitelists);
			if (filter != null) {
				if (!downloadedLinks.contains(links.get(i))) {
					progress.progressChanged("Extracting Links from " + links.get(i).getURL() + " (" + (downloadedLinks.size() + i) + "/" + (downloadedLinks.size() + links.size()) + ")");

					List<URL> foundLinks = getLinks(links.get(i).getURL(), referrer, filter, proxyManager, settingsManager, cookieManager);
					for (int x = 0; x < foundLinks.size(); x++) {
						if (!links.contains(foundLinks.get(x))) {
							links.add(foundLinks.get(x));
						}
					}
					downloadedLinks.add(links.get(i));
				}
				links.remove(i);
				i--;
				progress.progressChanged(0, links.size(), (downloadedLinks.size() + i));
				progress.progressChanged("Extracting Links from " + url + " (" + (downloadedLinks.size() + i) + "/" + (downloadedLinks.size() + links.size()) + ")");
			} else {
				progress.progressChanged((downloadedLinks.size() + i));
				progress.progressChanged("Extracting Links from " + url + " (" + (downloadedLinks.size() + i) + "/" + (downloadedLinks.size() + links.size()) + ")");
			}
		}

		return links;
	}
}

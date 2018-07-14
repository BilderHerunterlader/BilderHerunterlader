package ch.supertomcat.bh.hoster.linkextract;

import static ch.supertomcat.bh.hoster.linkextract.ExtractTools.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostIOException;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.guitools.progressmonitor.ProgressObserver;
import ch.supertomcat.supertomcattools.httptools.HTTPTool;

/**
 * 
 *
 */
public abstract class LinkExtract {

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
		String tagNames[] = { "a", "button" };
		// button is used for youtube-search

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
					if (link.startsWith("http://") == false && link.startsWith("https://") == false) {
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
	 * @return Array with links
	 * @throws HostException
	 */
	public static List<URL> getLinks(String url, String referrer, ILinkExtractFilter filter) throws HostException {
		List<URL> extractedURLs = new ArrayList<>();

		HttpGet method = null;
		try (CloseableHttpClient client = ProxyManager.instance().getHTTPClient()) {
			String cookies = CookieManager.getCookies(url);
			url = HTTPTool.encodeURL(url);
			method = new HttpGet(url);
			// Verbindung oeffnen
			RequestConfig.Builder requestConfigBuilder = ProxyManager.instance().getDefaultRequestConfigBuilder();
			requestConfigBuilder.setMaxRedirects(10);
			method.setConfig(requestConfigBuilder.build());
			method.setHeader("User-Agent", SettingsManager.instance().getUserAgent());
			if (cookies.length() > 0) {
				method.setHeader("Cookie", cookies);
			}
			if (referrer.length() > 0) {
				method.setHeader("Referer", referrer);
			}
			try (CloseableHttpResponse response = client.execute(method)) {
				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode != 200) {
					method.abort();
					throw new HostIOException("LinkExtract: Container-Page: HTTP-Error: " + statusCode);
				}

				// Inputstream oeffnen
				try (InputStream in = response.getEntity().getContent()) {
					addLinks(url, filter, extractedURLs, in);
					EntityUtils.consume(response.getEntity());
				}

				return extractedURLs;
			}
		} catch (MalformedURLException e) {
			throw new HostIOException("LinkExtract: Container-Page: " + url + " :" + e.getMessage(), e);
		} catch (IOException e) {
			throw new HostIOException("LinkExtract: Container-Page: " + url + " :" + e.getMessage(), e);
		} finally {
			if (method != null) {
				method.abort();
			}
		}
	}

	/**
	 * @param url URL
	 * @param referrer Referrer
	 * @param extractConfig ExtractConfig
	 * @param progress Progress
	 * @return URLs
	 * @throws Exception
	 */
	public static List<URL> getLinksRecursive(URL url, String referrer, ExtractConfig extractConfig, ProgressObserver progress) throws Exception {
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
				if (downloadedLinks.contains(links.get(i)) == false) {
					progress.progressChanged("Extracting Links from " + links.get(i).getURL() + " (" + (downloadedLinks.size() + i) + "/" + (downloadedLinks.size() + links.size()) + ")");

					List<URL> foundLinks = getLinks(links.get(i).getURL(), referrer, filter);
					for (int x = 0; x < foundLinks.size(); x++) {
						if (links.contains(foundLinks.get(x)) == false) {
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

package ch.supertomcat.bh.hoster;

import java.net.URI;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostIOException;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.fileiotools.FileTool;
import ch.supertomcat.supertomcattools.httptools.HTTPTool;

/**
 * Host and Rule extends this class, so we have a class
 * which could be a Host or a Rule. We need this on the URLParseObject
 * 
 * @see ch.supertomcat.bh.hoster.URLParseObject
 */
public abstract class Hoster {
	private boolean developer = false;

	/**
	 * Returns the developer
	 * 
	 * @return developer
	 */
	public boolean isDeveloper() {
		return developer;
	}

	/**
	 * Sets the developer
	 * 
	 * @param developer developer
	 */
	public void setDeveloper(boolean developer) {
		this.developer = developer;
	}

	/**
	 * Returns the enabled
	 * 
	 * @return enabled
	 */
	public abstract boolean isEnabled();

	/**
	 * Sets the enabled
	 * 
	 * @param enabled enabled
	 */
	public abstract void setEnabled(boolean enabled);

	/**
	 * Method to compare to URLs for removing duplicate URLs
	 * Attention: This Method is called always when the equals-Method
	 * on an URL-Object is called. And this happens as example on
	 * contains-Method on Collections and so on.
	 * So don't overwrite this Method until it is really necessary!
	 * If you overwrite this Method, then you have to know what you are
	 * doing!
	 * 
	 * @param url1 URL
	 * @param url2 URL
	 * @return True if url1 equals url2
	 */
	public boolean removeDuplicateEqualsMethod(ch.supertomcat.bh.pic.URL url1, ch.supertomcat.bh.pic.URL url2) {
		return url1.getURL().equals(url2.getURL());
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param hosterName Hoster Name for logging
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @return Sourcecode
	 * @throws HostException
	 */
	public final String downloadContainerPage(String hosterName, String url, String referrer) throws HostException {
		return downloadContainerPageEx(hosterName, url, referrer).getPage();
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param hosterName Hoster Name for logging
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @param options Options
	 * @return Sourcecode
	 * @throws HostException
	 */
	public final String downloadContainerPage(String hosterName, String url, String referrer, DownloadContainerPageOptions options) throws HostException {
		return downloadContainerPageEx(hosterName, url, referrer, options).getPage();
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param hosterName Hoster Name for logging
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @param options Options
	 * @param client HttpClient
	 * @return Sourcecode
	 * @throws HostException
	 */
	public final String downloadContainerPage(String hosterName, String url, String referrer, DownloadContainerPageOptions options, CloseableHttpClient client) throws HostException {
		return downloadContainerPageEx(hosterName, url, referrer, options, client).getPage();
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param hosterName Hoster Name for logging
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @return Sourcecode
	 * @throws HostException
	 */
	public final ContainerPage downloadContainerPageEx(String hosterName, String url, String referrer) throws HostException {
		return downloadContainerPageEx(hosterName, url, referrer, null);
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param hosterName Hoster Name for logging
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @param options Options
	 * @return Sourcecode
	 * @throws HostException
	 */
	public final ContainerPage downloadContainerPageEx(String hosterName, String url, String referrer, DownloadContainerPageOptions options) throws HostException {
		try (CloseableHttpClient client = ProxyManager.instance().getHTTPClient()) {
			return downloadContainerPageEx(hosterName, url, referrer, options, client);
		} catch (Exception e) {
			throw new HostIOException(hosterName + ": Container-Page: " + e.getMessage(), e);
		}
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param hosterName Hoster Name for logging
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @param options Options
	 * @param client HttpClient
	 * @return Sourcecode
	 * @throws HostException
	 */
	public final ContainerPage downloadContainerPageEx(String hosterName, String url, String referrer, DownloadContainerPageOptions options, CloseableHttpClient client) throws HostException {
		HttpGet method = null;
		try {
			String cookies = null;
			if (options == null || options.isSendCookies()) {
				cookies = CookieManager.getCookies(url);
			}

			url = HTTPTool.encodeURL(url);
			method = new HttpGet(url);

			RequestConfig.Builder requestConfigBuilder = ProxyManager.instance().getDefaultRequestConfigBuilder();
			requestConfigBuilder.setMaxRedirects(10);
			method.setConfig(requestConfigBuilder.build());
			method.setHeader("User-Agent", SettingsManager.instance().getUserAgent());
			if (cookies != null && !cookies.isEmpty()) {
				method.setHeader("Cookie", cookies);
			}
			if (referrer != null && !referrer.isEmpty()) {
				method.setHeader("Referer", referrer);
			}

			HttpContext context = new BasicHttpContext();
			try (CloseableHttpResponse response = client.execute(method, context)) {
				int statusCode = response.getStatusLine().getStatusCode();

				if ((options == null || options.isCheckStatusCode()) && statusCode < 200 || statusCode >= 300) {
					method.abort();
					throw new HostIOException(hosterName + ": Container-Page: HTTP-Error: " + statusCode + " URL: " + url);
				}

				String redirectedURL = null;

				HttpClientContext clientContext = HttpClientContext.adapt(context);
				List<URI> redirectedLocations = clientContext.getRedirectLocations();
				if (redirectedLocations != null && !redirectedLocations.isEmpty()) {
					Object redirectedRequest = context.getAttribute(HttpCoreContext.HTTP_REQUEST);
					Object redirectedHost = context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
					if (redirectedRequest instanceof HttpUriRequest && redirectedHost instanceof HttpHost) {
						URI redirectedURI = ((HttpUriRequest)redirectedRequest).getURI();
						HttpHost redirectedHttpHost = (HttpHost)redirectedHost;
						if (redirectedURI.isAbsolute()) {
							redirectedURL = redirectedURI.toString();
						} else {
							redirectedURL = redirectedHttpHost.toURI() + redirectedURI;
						}
					}
				}

				String page;

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					page = EntityUtils.toString(entity);
					EntityUtils.consume(response.getEntity());
				} else {
					page = "";
				}
				return new ContainerPage(page, redirectedURL, response.getStatusLine());
			}
		} catch (Exception e) {
			throw new HostIOException(hosterName + ": Container-Page: " + e.getMessage(), e);
		} finally {
			if (method != null) {
				method.abort();
			}
		}
	}

	/**
	 * This method filters not allowed chars in filenames
	 * 
	 * @param filename Filename
	 * @return Filtered Filename
	 */
	protected final String filterFilename(String filename) {
		return FileTool.filterFilename(filename, SettingsManager.instance().getAllowedFilenameChars());
	}

	/**
	 * This method filters not allowed chars in paths (including filename if available)
	 * 
	 * @param path Path
	 * @return Filtered path
	 */
	protected final String filterPath(String path) {
		return FileTool.filterPath(path, SettingsManager.instance().getAllowedFilenameChars());
	}

	/**
	 * Trims an url
	 * 
	 * @param url URL
	 * @return Trimmed URL
	 */
	protected final String trimURL(String url) {
		return HTTPTool.trimURL(url);
	}

	@Override
	public abstract String toString();
}

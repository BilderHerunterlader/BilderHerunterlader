package ch.supertomcat.bh.hoster;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.swing.JFrame;

import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostHttpIOException;
import ch.supertomcat.bh.exceptions.HostIOException;
import ch.supertomcat.bh.hoster.containerpage.ContainerPage;
import ch.supertomcat.bh.hoster.containerpage.DownloadContainerPageOptions;
import ch.supertomcat.bh.queue.DownloadRestriction;
import ch.supertomcat.bh.queue.RestrictionAccess;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Host and Rule extends this class, so we have a class
 * which could be a Host or a Rule. We need this on the URLParseObject
 * 
 * @see ch.supertomcat.bh.hoster.parser.URLParseObject
 */
public abstract class Hoster {
	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Developer Flag
	 */
	private boolean developer = false;

	/**
	 * Main Window or null
	 */
	private static JFrame mainWindow = null;

	/**
	 * Restriction Access
	 */
	private static RestrictionAccess restrictionAccess = null;

	/**
	 * Proxy Manager
	 */
	private static ProxyManager proxyManager = null;

	/**
	 * Settings Manager
	 */
	private static SettingsManager settingsManager = null;

	/**
	 * Cookie Manager
	 */
	private static CookieManager cookieManager = null;

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
	 * Returns the mainWindow
	 * 
	 * @return mainWindow
	 */
	public JFrame getMainWindow() {
		return mainWindow;
	}

	/**
	 * Sets the mainWindow
	 * 
	 * @param mainWindow mainWindow
	 */
	static void setMainWindow(JFrame mainWindow) {
		Hoster.mainWindow = mainWindow;
	}

	/**
	 * Sets the restrictionAccess
	 * 
	 * @param restrictionAccess restrictionAccess
	 */
	static void setRestrictionAccess(RestrictionAccess restrictionAccess) {
		Hoster.restrictionAccess = restrictionAccess;
	}

	/**
	 * Returns the proxyManager
	 * 
	 * @return proxyManager
	 */
	public ProxyManager getProxyManager() {
		return proxyManager;
	}

	/**
	 * Sets the proxyManager
	 * 
	 * @param proxyManager proxyManager
	 */
	static void setProxyManager(ProxyManager proxyManager) {
		Hoster.proxyManager = proxyManager;
	}

	/**
	 * Returns the settingsManager
	 * 
	 * @return settingsManager
	 */
	public SettingsManager getSettingsManager() {
		return settingsManager;
	}

	/**
	 * Sets the settingsManager
	 * 
	 * @param settingsManager settingsManager
	 */
	static void setSettingsManager(SettingsManager settingsManager) {
		Hoster.settingsManager = settingsManager;
	}

	/**
	 * Returns the cookieManager
	 * 
	 * @return cookieManager
	 */
	public CookieManager getCookieManager() {
		return cookieManager;
	}

	/**
	 * Sets the cookieManager
	 * 
	 * @param cookieManager cookieManager
	 */
	static void setCookieManager(CookieManager cookieManager) {
		Hoster.cookieManager = cookieManager;
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
	 * @return True if hoster can be disabled, false otherwise
	 */
	public abstract boolean canBeDisabled();

	/**
	 * Adds a restriction
	 * If there is already a restriction for a domain the restriction
	 * is not added. But the value of maximum simultanious downloads from
	 * the restriction to add is taken and set to the existing restriction.
	 * So this method can be used to update a restriction
	 * 
	 * @param restriction Restriction
	 */
	public void addRestriction(DownloadRestriction restriction) {
		if (restrictionAccess != null) {
			restrictionAccess.addRestriction(restriction);
		} else {
			LoggerFactory.getLogger(getClass()).error("Could not add restriction, because restrictionAccess is not initialized");
		}
	}

	/**
	 * Removes a restriction
	 * 
	 * @param restriction Restriction
	 */
	public void removeRestriction(DownloadRestriction restriction) {
		if (restrictionAccess != null) {
			restrictionAccess.removeRestriction(restriction);
		} else {
			LoggerFactory.getLogger(getClass()).error("Could not remove restriction, because restrictionAccess is not initialized");
		}
	}

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
		return downloadContainerPage(hosterName, url, referrer, (HttpContext)null);
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param hosterName Hoster Name for logging
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @param httpContext HTTP Context or null
	 * @return Sourcecode
	 * @throws HostException
	 */
	public final String downloadContainerPage(String hosterName, String url, String referrer, HttpContext httpContext) throws HostException {
		return downloadContainerPageEx(hosterName, url, referrer, httpContext).getPage();
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
		return downloadContainerPage(hosterName, url, referrer, options, (HttpContext)null);
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param hosterName Hoster Name for logging
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @param options Options
	 * @param httpContext HTTP Context or null
	 * @return Sourcecode
	 * @throws HostException
	 */
	public final String downloadContainerPage(String hosterName, String url, String referrer, DownloadContainerPageOptions options, HttpContext httpContext) throws HostException {
		return downloadContainerPageEx(hosterName, url, referrer, options, httpContext).getPage();
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
		return downloadContainerPage(hosterName, url, referrer, options, client, null);
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param hosterName Hoster Name for logging
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @param options Options
	 * @param client HttpClient
	 * @param httpContext HTTP Context or null
	 * @return Sourcecode
	 * @throws HostException
	 */
	public final String downloadContainerPage(String hosterName, String url, String referrer, DownloadContainerPageOptions options, CloseableHttpClient client,
			HttpContext httpContext) throws HostException {
		return downloadContainerPageEx(hosterName, url, referrer, options, client, httpContext).getPage();
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
		return downloadContainerPageEx(hosterName, url, referrer, (HttpContext)null);
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param hosterName Hoster Name for logging
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @param httpContext HTTP Context or null
	 * @return Sourcecode
	 * @throws HostException
	 */
	public final ContainerPage downloadContainerPageEx(String hosterName, String url, String referrer, HttpContext httpContext) throws HostException {
		return downloadContainerPageEx(hosterName, url, referrer, httpContext);
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
		return downloadContainerPageEx(hosterName, url, referrer, options, (HttpContext)null);
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param hosterName Hoster Name for logging
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @param options Options
	 * @param httpContext HTTP Context or null
	 * @return Sourcecode
	 * @throws HostException
	 */
	public final ContainerPage downloadContainerPageEx(String hosterName, String url, String referrer, DownloadContainerPageOptions options, HttpContext httpContext) throws HostException {
		try (CloseableHttpClient client = proxyManager.getHTTPClient()) {
			return downloadContainerPageEx(hosterName, url, referrer, options, client, httpContext);
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
		return downloadContainerPageEx(hosterName, url, referrer, options, client, null);
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param hosterName Hoster Name for logging
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @param options Options
	 * @param client HttpClient
	 * @param httpContext HTTP Context or null
	 * @return Sourcecode
	 * @throws HostException
	 */
	@SuppressWarnings("resource")
	public final ContainerPage downloadContainerPageEx(String hosterName, String url, String referrer, DownloadContainerPageOptions options, CloseableHttpClient client,
			HttpContext httpContext) throws HostException {
		try {
			HttpUriRequestBase method;
			String encodedURL = HTTPUtil.encodeURL(url);
			if (options != null && "POST".equals(options.getHttpMethod())) {
				HttpPost postMethod = new HttpPost(encodedURL);
				if (!options.getPostData().isEmpty()) {
					postMethod.setEntity(new UrlEncodedFormEntity(options.getPostData(), StandardCharsets.UTF_8));
				}
				method = postMethod;
			} else {
				method = new HttpGet(encodedURL);
			}

			RequestConfig.Builder requestConfigBuilder = proxyManager.getDefaultRequestConfigBuilder();
			requestConfigBuilder.setMaxRedirects(10);
			method.setConfig(requestConfigBuilder.build());
			String userAgentConfig = options != null ? options.getRequestConfig("User-Agent") : null;
			if (userAgentConfig != null) {
				method.setHeader("User-Agent", userAgentConfig);
			} else {
				method.setHeader("User-Agent", settingsManager.getUserAgent());
			}
			if (referrer != null && !referrer.isEmpty()) {
				method.setHeader("Referer", referrer);
			}

			CookieStore cookieStore;
			HttpContext context;
			if (httpContext == null) {
				cookieStore = new BasicCookieStore();
				context = ContextBuilder.create().useCookieStore(cookieStore).build();
			} else {
				if (httpContext instanceof HttpClientContext) {
					HttpClientContext httpClientContext = (HttpClientContext)httpContext;
					CookieStore contextCookieStore = httpClientContext.getCookieStore();
					if (contextCookieStore != null) {
						cookieStore = contextCookieStore;
						context = httpContext;
					} else {
						cookieStore = new BasicCookieStore();
						HttpClientContext wrapperContext = new HttpClientContext(httpClientContext);
						wrapperContext.setCookieStore(cookieStore);
						context = wrapperContext;
					}
				} else {
					cookieStore = new BasicCookieStore();
					HttpClientContext wrapperContext = new HttpClientContext(httpContext);
					wrapperContext.setCookieStore(cookieStore);
					context = wrapperContext;
				}
			}

			if (options == null || options.isSendCookies()) {
				cookieManager.fillCookies(url, cookieStore);
			}

			return client.execute(method, context, response -> {
				StatusLine statusLine = new StatusLine(response);
				int statusCode = statusLine.getStatusCode();

				if ((options == null || options.isCheckStatusCode()) && (statusCode < 200 || statusCode >= 300)) {
					method.abort();
					throw new HostHttpIOException(hosterName + ": Container-Page: HTTP-Error: " + statusCode + " URL: " + encodedURL);
				}

				String redirectedURL = getRedirectedURL(context);

				String page;

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					page = EntityUtils.toString(entity);
				} else {
					page = "";
				}
				return new ContainerPage(page, redirectedURL, statusLine);
			});
		} catch (Exception e) {
			throw new HostIOException(hosterName + ": Container-Page: " + e.getMessage(), e);
		}
	}

	/**
	 * Get Redirected URL
	 * 
	 * @param context Context
	 * @return Redirected URL or null
	 */
	protected String getRedirectedURL(HttpContext context) {
		String redirectedURL = null;
		HttpClientContext clientContext = HttpClientContext.adapt(context);
		RedirectLocations redirectedLocations = clientContext.getRedirectLocations();
		if (redirectedLocations != null) {
			List<URI> redirectedLocationsURIList = redirectedLocations.getAll();
			if (!redirectedLocationsURIList.isEmpty()) {
				Object redirectedRequest = context.getAttribute(HttpCoreContext.HTTP_REQUEST);
				if (redirectedRequest instanceof HttpUriRequest) {
					try {
						URI redirectedURI = ((HttpUriRequest)redirectedRequest).getUri();
						if (redirectedURI.isAbsolute()) {
							redirectedURL = redirectedURI.toString();
						} else {
							/*
							 * TODO Implement with httpclient5. Same code as for Version 4 does not work anymore.
							 */
							logger.error("Could not determine redirect URI, because it is not absolute: {}", redirectedURI);
						}
					} catch (URISyntaxException e) {
						logger.error("Could not determine redirection", e);
					}
				}
			}
		}
		return redirectedURL;
	}

	/**
	 * This method filters not allowed chars in filenames
	 * 
	 * @param filename Filename
	 * @return Filtered Filename
	 */
	protected final String filterFilename(String filename) {
		return FileUtil.filterFilename(filename, settingsManager.getAllowedFilenameChars());
	}

	/**
	 * This method filters not allowed chars in paths (including filename if available)
	 * 
	 * @param path Path
	 * @return Filtered path
	 */
	protected final String filterPath(String path) {
		return FileUtil.filterPath(path, settingsManager.getAllowedFilenameChars());
	}

	/**
	 * Trims an url
	 * 
	 * @param url URL
	 * @return Trimmed URL
	 */
	protected final String trimURL(String url) {
		return HTTPUtil.trimURL(url);
	}

	@Override
	public abstract String toString();
}

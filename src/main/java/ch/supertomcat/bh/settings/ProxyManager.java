package ch.supertomcat.bh.settings;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Class which provides method to get a preconfigured HttpClient, so that
 * in the hole program the proxysettings can be used.
 */
public class ProxyManager {
	/**
	 * Direct connection
	 */
	public static final int DIRECT_CONNECTION = 0;

	/**
	 * Connection over a proxy
	 */
	public static final int HTTP_PROXY = 1;

	/**
	 * Singleton
	 */
	private static ProxyManager instance = null;

	/**
	 * Mode
	 */
	private int mode = ProxyManager.DIRECT_CONNECTION;

	/**
	 * Name
	 */
	private String proxyname = "127.0.0.1";

	/**
	 * Port
	 */
	private int proxyport = 0;

	/**
	 * Username
	 */
	private String proxyuser = "";

	/**
	 * Password
	 */
	private String proxypassword = "";

	/**
	 * Flag if authetification is required or not
	 */
	private boolean auth = false;

	/**
	 * Mutli Threaded Connection Manager
	 */
	private final PoolingHttpClientConnectionManager conManager;

	/**
	 * Constructor
	 */
	private ProxyManager() {
		// Get the configuration
		readFromSettings();

		RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create();
		registryBuilder.register("http", PlainConnectionSocketFactory.getSocketFactory());
		registryBuilder.register("https", SSLConnectionSocketFactory.getSocketFactory());
		Registry<ConnectionSocketFactory> registry = registryBuilder.build();
		conManager = new PoolingHttpClientConnectionManager(registry);
		conManager.setMaxTotal(60);
		conManager.setDefaultMaxPerRoute(30);
	}

	/**
	 * Returns the Singleton
	 * 
	 * @return Singleton
	 */
	public static synchronized ProxyManager instance() {
		if (instance == null) {
			instance = new ProxyManager();
		}
		return instance;
	}

	/**
	 * Returns a preconfigured RequestConfig Builder
	 * 
	 * @return RequestConfig.Builder
	 */
	public RequestConfig.Builder getDefaultRequestConfigBuilder() {
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
		requestConfigBuilder.setSocketTimeout(SettingsManager.instance().getTimeout());
		requestConfigBuilder.setConnectionRequestTimeout(SettingsManager.instance().getTimeout());
		requestConfigBuilder.setConnectTimeout(SettingsManager.instance().getTimeout());
		requestConfigBuilder.setCookieSpec(CookieSpecs.DEFAULT);
		return requestConfigBuilder;
	}

	private void configureHttpClientBuilder(HttpClientBuilder clientBuilder) {
		if (mode != ProxyManager.DIRECT_CONNECTION) {
			HttpHost proxy = new HttpHost(proxyname, proxyport);
			if (auth) {
				AuthScope authScope = new AuthScope(proxyname, proxyport);
				Credentials credentials = new UsernamePasswordCredentials(proxyuser, proxypassword);

				CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(authScope, credentials);

				clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
			}
			clientBuilder.setProxy(proxy);
		}

		DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(0, false);
		clientBuilder.setRetryHandler(retryHandler);

		clientBuilder.setDefaultRequestConfig(getDefaultRequestConfigBuilder().build());
	}

	/**
	 * Returns a preconfigured HttpClientBuilder
	 * 
	 * @return HttpClientBuilder
	 */
	public HttpClientBuilder getHTTPClientBuilder() {
		HttpClientBuilder clientBuilder = HttpClientBuilder.create();
		clientBuilder.setConnectionManagerShared(true);
		clientBuilder.setConnectionManager(conManager);
		configureHttpClientBuilder(clientBuilder);
		return clientBuilder;
	}

	/**
	 * Returns a preconfigured HttpClientBuilder, but without usage of the Mutli Threaded Connection Manager
	 * I need this, because of some problems on download and import html files.
	 * 
	 * @see ch.supertomcat.bh.importexport.ImportHTML#importHTML(String, String, boolean)
	 * @return HttpClientBuilder
	 */
	public HttpClientBuilder getNonMultithreadedHTTPClientBuilder() {
		HttpClientBuilder clientBuilder = HttpClientBuilder.create();
		configureHttpClientBuilder(clientBuilder);
		return clientBuilder;
	}

	/**
	 * Returns a preconfigured HttpClient
	 * 
	 * @return HttpClient
	 */
	public CloseableHttpClient getHTTPClient() {
		return getHTTPClientBuilder().build();
	}

	/**
	 * Returns a preconfigured HttpClient, but without usage of the Mutli Threaded Connection Manager
	 * I need this, because of some problems on download and import html files.
	 * 
	 * @see ch.supertomcat.bh.importexport.ImportHTML#importHTML(String, String, boolean)
	 * @return HttpClient
	 */
	public CloseableHttpClient getNonMultithreadedHTTPClient() {
		return getNonMultithreadedHTTPClientBuilder().build();
	}

	/**
	 * Save the settings
	 */
	public void writeToSettings() {
		SettingsManager.instance().setProxymode(mode);
		SettingsManager.instance().setProxyname(proxyname);
		SettingsManager.instance().setProxyport(proxyport);
		SettingsManager.instance().setProxyuser(proxyuser);
		SettingsManager.instance().setProxypassword(proxypassword);
		SettingsManager.instance().setProxyauth(auth);
		SettingsManager.instance().writeSettings(true);
	}

	/**
	 * Read the settings
	 */
	public void readFromSettings() {
		mode = SettingsManager.instance().getProxymode();
		proxyname = SettingsManager.instance().getProxyname();
		proxyport = SettingsManager.instance().getProxyport();
		proxyuser = SettingsManager.instance().getProxyuser();
		proxypassword = SettingsManager.instance().getProxypassword();
		auth = SettingsManager.instance().isProxyauth();
	}

	/**
	 * Returns the proxyuser
	 * 
	 * @return proxyuser
	 */
	public String getProxyuser() {
		return proxyuser;
	}

	/**
	 * Sets the proxyuser
	 * 
	 * @param proxyuser proxyuser
	 */
	public void setProxyuser(String proxyuser) {
		this.proxyuser = proxyuser;
	}

	/**
	 * Returns the mode
	 * 
	 * @return mode
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * Sets the mode
	 * 
	 * @param mode mode
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

	/**
	 * Returns the proxyname
	 * 
	 * @return proxyname
	 */
	public String getProxyname() {
		return proxyname;
	}

	/**
	 * Sets the proxyname
	 * 
	 * @param proxyname proxyname
	 */
	public void setProxyname(String proxyname) {
		this.proxyname = proxyname;
	}

	/**
	 * Returns the proxyport
	 * 
	 * @return proxyport
	 */
	public int getProxyport() {
		return proxyport;
	}

	/**
	 * Sets the proxyport
	 * 
	 * @param proxyport proxyport
	 */
	public void setProxyport(int proxyport) {
		this.proxyport = proxyport;
	}

	/**
	 * Returns the proxypassword
	 * 
	 * @return proxypassword
	 */
	public String getProxypassword() {
		return proxypassword;
	}

	/**
	 * Sets the proxypassword
	 * 
	 * @param proxypassword proxypassword
	 */
	public void setProxypassword(String proxypassword) {
		this.proxypassword = proxypassword;
	}

	/**
	 * Returns the auth
	 * 
	 * @return auth
	 */
	public boolean isAuth() {
		return auth;
	}

	/**
	 * Sets the auth
	 * 
	 * @param auth auth
	 */
	public void setAuth(boolean auth) {
		this.auth = auth;
	}
}

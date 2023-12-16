package ch.supertomcat.bh.settings;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

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
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Constructor
	 * 
	 * @param settingsManager SettingsManager
	 */
	public ProxyManager(SettingsManager settingsManager) {
		this.settingsManager = settingsManager;
		// Get the configuration
		readFromSettings();

		PoolingHttpClientConnectionManagerBuilder conManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();
		conManagerBuilder.setMaxConnTotal(60);
		conManagerBuilder.setMaxConnPerRoute(30);
		conManagerBuilder.setDefaultSocketConfig(SocketConfig.custom().setSndBufSize(65536).build());

		conManagerBuilder.setDefaultConnectionConfig(createConnectionConfig());

		conManager = conManagerBuilder.build();

		settingsManager.addSettingsListener(new BHSettingsListener() {

			@Override
			public void settingsChanged() {
				conManager.setDefaultConnectionConfig(createConnectionConfig());
			}

			@Override
			public void lookAndFeelChanged(LookAndFeelSetting lookAndFeel) {
				// Nothing to do
			}
		});
	}

	/**
	 * Create Connection Config
	 * 
	 * @return Connection Config
	 */
	private ConnectionConfig createConnectionConfig() {
		ConnectionConfig.Builder defaultConnectionConfigBuilder = ConnectionConfig.custom();
		defaultConnectionConfigBuilder.setSocketTimeout(Timeout.ofMilliseconds(settingsManager.getTimeout()));
		defaultConnectionConfigBuilder.setConnectTimeout(Timeout.ofMilliseconds(settingsManager.getTimeout()));
		return defaultConnectionConfigBuilder.build();
	}

	/**
	 * Returns a preconfigured RequestConfig Builder
	 * 
	 * @return RequestConfig.Builder
	 */
	public RequestConfig.Builder getDefaultRequestConfigBuilder() {
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
		requestConfigBuilder.setConnectionRequestTimeout(Timeout.ofMilliseconds(settingsManager.getTimeout()));
		requestConfigBuilder.setCookieSpec(StandardCookieSpec.RELAXED);
		return requestConfigBuilder;
	}

	private void configureHttpClientBuilder(HttpClientBuilder clientBuilder) {
		if (mode != ProxyManager.DIRECT_CONNECTION) {
			HttpHost proxy = new HttpHost(proxyname, proxyport);
			if (auth) {
				AuthScope authScope = new AuthScope(proxyname, proxyport);
				Credentials credentials = new UsernamePasswordCredentials(proxyuser, proxypassword.toCharArray());

				BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(authScope, credentials);

				clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
			}
			clientBuilder.setProxy(proxy);
		}

		DefaultHttpRequestRetryStrategy retryStrategy = new DefaultHttpRequestRetryStrategy(0, TimeValue.ofSeconds(1L));
		clientBuilder.setRetryStrategy(retryStrategy);

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
		settingsManager.setProxymode(mode);
		settingsManager.setProxyname(proxyname);
		settingsManager.setProxyport(proxyport);
		settingsManager.setProxyuser(proxyuser);
		settingsManager.setProxypassword(proxypassword);
		settingsManager.setProxyauth(auth);
		settingsManager.writeSettings(true);
	}

	/**
	 * Read the settings
	 */
	public void readFromSettings() {
		mode = settingsManager.getProxymode();
		proxyname = settingsManager.getProxyname();
		proxyport = settingsManager.getProxyport();
		proxyuser = settingsManager.getProxyuser();
		proxypassword = settingsManager.getProxypassword();
		auth = settingsManager.isProxyauth();
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

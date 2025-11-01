package ch.supertomcat.bh.downloader;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import ch.supertomcat.bh.cookies.CookieManager;
import ch.supertomcat.bh.settings.BHSettingsListener;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.xml.ConnectionSettings;
import ch.supertomcat.bh.settings.xml.LookAndFeelSetting;
import ch.supertomcat.bh.settings.xml.ProxyMode;
import ch.supertomcat.bh.settings.xml.ProxySettings;

/**
 * Class which provides method to get a preconfigured HttpClient, so that
 * in the hole program the proxysettings can be used.
 */
public class ProxyManager {
	/**
	 * Buffer Size
	 */
	public static final int BUFFER_SIZE = 65536;

	/**
	 * Mode
	 */
	private ProxyMode mode = ProxyMode.DIRECT_CONNECTION;

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
	 * Shared HTTP Client
	 */
	private CloseableHttpClient sharedHttpClient;

	/**
	 * Shared HTTP Client Wrapper
	 */
	private SharedHttpClient sharedHttpClientWrapper;

	/**
	 * SharedHttpClient Lock
	 */
	private final ReadWriteLock sharedHttpClientLock = new ReentrantReadWriteLock();

	/**
	 * SharedHttpClient Read Lock
	 */
	private final Lock sharedHttpClientReadLock = sharedHttpClientLock.readLock();

	/**
	 * SharedHttpClient Write Lock
	 */
	private final Lock sharedHttpClientWriteLock = sharedHttpClientLock.writeLock();

	/**
	 * Use Shared Http Client Flag
	 */
	private boolean useSharedHttpClient = true;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * cookieManager
	 */
	private final CookieManager cookieManager;

	/**
	 * Constructor
	 * 
	 * @param settingsManager SettingsManager
	 * @param cookieManager Cookie Manager
	 */
	public ProxyManager(SettingsManager settingsManager, CookieManager cookieManager) {
		this.settingsManager = settingsManager;
		this.cookieManager = cookieManager;
		// Get the configuration
		readFromSettings();

		/*
		 * Custom Socket and Connection config to fix slow uploads. By default apache httpclient only uses 8192 byte frames for sending, which makes uploads
		 * slow.
		 */
		Http1Config http1Config = Http1Config.custom().setBufferSize(BUFFER_SIZE).setChunkSizeHint(BUFFER_SIZE).build();
		ManagedHttpClientConnectionFactory connectionFactory = ManagedHttpClientConnectionFactory.builder().http1Config(http1Config).build();

		PoolingHttpClientConnectionManagerBuilder conManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();
		conManagerBuilder.setMaxConnTotal(120);
		conManagerBuilder.setMaxConnPerRoute(30);
		conManagerBuilder.setDefaultSocketConfig(SocketConfig.custom().setSndBufSize(BUFFER_SIZE).setRcvBufSize(BUFFER_SIZE).build());
		conManagerBuilder.setConnectionFactory(connectionFactory);

		conManagerBuilder.setDefaultConnectionConfig(createConnectionConfig());

		conManager = conManagerBuilder.build();

		createSharedHTTPClient();

		settingsManager.addSettingsListener(new BHSettingsListener() {

			@Override
			public void settingsChanged() {
				conManager.setDefaultConnectionConfig(createConnectionConfig());
				ConnectionSettings conSettings = settingsManager.getConnectionSettings();
				useSharedHttpClient = conSettings.isSharedHttpClient();
				createSharedHTTPClient();
			}

			@Override
			public void lookAndFeelChanged(LookAndFeelSetting lookAndFeel) {
				// Nothing to do
			}
		});
	}

	/**
	 * Create shared HTTP Client
	 */
	private void createSharedHTTPClient() {
		try {
			sharedHttpClientWriteLock.lock();
			if (sharedHttpClient != null) {
				sharedHttpClient.close(CloseMode.GRACEFUL);
			}
			sharedHttpClient = getHTTPClientBuilder().setDefaultCookieStore(cookieManager.getCookieStore()).build();
			sharedHttpClientWrapper = new SharedHttpClient(sharedHttpClient);
		} finally {
			sharedHttpClientWriteLock.unlock();
		}
	}

	/**
	 * Create Connection Config
	 * 
	 * @return Connection Config
	 */
	private ConnectionConfig createConnectionConfig() {
		ConnectionSettings conSettings = settingsManager.getConnectionSettings();
		ConnectionConfig.Builder defaultConnectionConfigBuilder = ConnectionConfig.custom();
		defaultConnectionConfigBuilder.setSocketTimeout(Timeout.ofMilliseconds(conSettings.getSocketTimeout()));
		defaultConnectionConfigBuilder.setConnectTimeout(Timeout.ofMilliseconds(conSettings.getConnectTimeout()));
		return defaultConnectionConfigBuilder.build();
	}

	/**
	 * Returns a preconfigured RequestConfig Builder
	 * 
	 * @return RequestConfig.Builder
	 */
	public RequestConfig.Builder getDefaultRequestConfigBuilder() {
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
		ConnectionSettings conSettings = settingsManager.getConnectionSettings();
		requestConfigBuilder.setConnectionRequestTimeout(Timeout.ofMilliseconds(conSettings.getConnectionRequestTimeout()));
		requestConfigBuilder.setCookieSpec(StandardCookieSpec.RELAXED);
		return requestConfigBuilder;
	}

	private void configureHttpClientBuilder(HttpClientBuilder clientBuilder) {
		if (mode != ProxyMode.DIRECT_CONNECTION) {
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
	 * Returns a preconfigured HttpClient
	 * 
	 * @return HttpClient
	 */
	public CloseableHttpClient getHTTPClient() {
		if (useSharedHttpClient) {
			return getSharedHTTPClient();
		} else {
			return getHTTPClientBuilder().build();
		}
	}

	/**
	 * Returns a preconfigured shared HttpClient
	 * 
	 * Note: The returned must not be closed!
	 * 
	 * @return HttpClient
	 */
	public CloseableHttpClient getSharedHTTPClient() {
		try {
			sharedHttpClientReadLock.lock();
			return sharedHttpClientWrapper;
		} finally {
			sharedHttpClientReadLock.unlock();
		}
	}

	/**
	 * Save the settings
	 */
	public void writeToSettings() {
		ConnectionSettings conSettings = settingsManager.getConnectionSettings();
		ProxySettings proxySettings = conSettings.getProxy();
		proxySettings.setMode(mode);
		proxySettings.setHost(proxyname);
		proxySettings.setPort(proxyport);
		proxySettings.setUser(proxyuser);
		proxySettings.setPassword(proxypassword);
		proxySettings.setAuth(auth);
		settingsManager.writeSettings(true);
	}

	/**
	 * Read the settings
	 */
	public void readFromSettings() {
		ConnectionSettings conSettings = settingsManager.getConnectionSettings();
		ProxySettings proxySettings = conSettings.getProxy();
		mode = proxySettings.getMode();
		proxyname = proxySettings.getHost();
		proxyport = proxySettings.getPort();
		proxyuser = proxySettings.getUser();
		proxypassword = proxySettings.getPassword();
		auth = proxySettings.isAuth();
		useSharedHttpClient = conSettings.isSharedHttpClient();
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
	public ProxyMode getMode() {
		return mode;
	}

	/**
	 * Sets the mode
	 * 
	 * @param mode mode
	 */
	public void setMode(ProxyMode mode) {
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

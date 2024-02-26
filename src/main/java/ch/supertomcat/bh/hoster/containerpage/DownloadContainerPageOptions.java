package ch.supertomcat.bh.hoster.containerpage;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.core5.http.NameValuePair;

/**
 * Options for downloading container page
 */
public class DownloadContainerPageOptions {
	/**
	 * User Agent
	 */
	public static final String USER_AGENT = "User-Agent";

	/**
	 * Send Cookies
	 */
	private final boolean sendCookies;

	/**
	 * Check Status Code
	 */
	private final boolean checkStatusCode;

	/**
	 * HTTP Method or null
	 */
	private final String httpMethod;

	/**
	 * Data for HTTP Post or empty List
	 */
	private final List<NameValuePair> postData;

	/**
	 * Request Config
	 */
	private final Map<String, String> requestConfig = new HashMap<>();

	/**
	 * Constructor
	 * 
	 * @param sendCookies Send Cookies
	 * @param checkStatusCode Check Status Code
	 */
	public DownloadContainerPageOptions(boolean sendCookies, boolean checkStatusCode) {
		this(sendCookies, checkStatusCode, null, Collections.emptyList());
	}

	/**
	 * Constructor
	 * 
	 * @param sendCookies Send Cookies
	 * @param checkStatusCode Check Status Code
	 * @param httpMethod HTTP Method or null
	 */
	public DownloadContainerPageOptions(boolean sendCookies, boolean checkStatusCode, String httpMethod) {
		this(sendCookies, checkStatusCode, httpMethod, Collections.emptyList());
	}

	/**
	 * Constructor
	 * 
	 * @param sendCookies Send Cookies
	 * @param checkStatusCode Check Status Code
	 * @param httpMethod HTTP Method or null
	 * @param postData Data for HTTP Post or empty List
	 */
	public DownloadContainerPageOptions(boolean sendCookies, boolean checkStatusCode, String httpMethod, List<NameValuePair> postData) {
		this.sendCookies = sendCookies;
		this.checkStatusCode = checkStatusCode;
		this.httpMethod = httpMethod;
		this.postData = postData;
	}

	/**
	 * @return Check Status Code
	 */
	public boolean isSendCookies() {
		return sendCookies;
	}

	/**
	 * @return Send Cookies
	 */
	public boolean isCheckStatusCode() {
		return checkStatusCode;
	}

	/**
	 * Returns the httpMethod
	 * 
	 * @return httpMethod
	 */
	public String getHttpMethod() {
		return httpMethod;
	}

	/**
	 * Returns the postData
	 * 
	 * @return postData
	 */
	public List<NameValuePair> getPostData() {
		return postData;
	}

	/**
	 * Get Request Config
	 * 
	 * @param key Key
	 * @return Value or null
	 */
	public String getRequestConfig(String key) {
		return requestConfig.get(key);
	}

	/**
	 * Put Request Config
	 * 
	 * @param key Key
	 * @param value Value
	 */
	public void putRequestConfig(String key, String value) {
		requestConfig.put(key, value);
	}

	/**
	 * Returns the requestConfig
	 * 
	 * @return requestConfig
	 */
	public Map<String, String> getRequestConfig() {
		return requestConfig;
	}
}

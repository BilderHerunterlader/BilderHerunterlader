package ch.supertomcat.bh.hoster.containerpage;

import java.util.Collections;
import java.util.List;

import org.apache.http.NameValuePair;

/**
 * Options for downloading container page
 */
public class DownloadContainerPageOptions {
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
}

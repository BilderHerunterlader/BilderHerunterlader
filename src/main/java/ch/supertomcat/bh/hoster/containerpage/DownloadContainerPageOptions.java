package ch.supertomcat.bh.hoster.containerpage;

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
	 * Constructor
	 * 
	 * @param sendCookies Send Cookies
	 * @param checkStatusCode Check Status Code
	 */
	public DownloadContainerPageOptions(boolean sendCookies, boolean checkStatusCode) {
		this.sendCookies = sendCookies;
		this.checkStatusCode = checkStatusCode;
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
}

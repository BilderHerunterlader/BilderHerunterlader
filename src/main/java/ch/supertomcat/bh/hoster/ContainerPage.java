package ch.supertomcat.bh.hoster;

import org.apache.http.StatusLine;

/**
 * Container Page
 */
public class ContainerPage {
	/**
	 * Page Source Code
	 */
	private final String page;

	/**
	 * Redirected URL if request was redirected or null
	 */
	private final String redirectedURL;

	/**
	 * Status Line
	 */
	private final StatusLine statusLine;

	/**
	 * Constructor
	 * 
	 * @param page Page Source Code
	 * @param redirectedURL Redirected URL if request was redirected or null
	 * @param statusLine Status Line
	 */
	public ContainerPage(String page, String redirectedURL, StatusLine statusLine) {
		this.page = page;
		this.redirectedURL = redirectedURL;
		this.statusLine = statusLine;
	}

	/**
	 * @return Page Source Code
	 */
	public String getPage() {
		return page;
	}

	/**
	 * @return True if the request was redirected, false otherwise
	 */
	public boolean isRedirected() {
		return redirectedURL != null;
	}

	/**
	 * @return Redirected URL if request was redirected or null
	 */
	public String getRedirectedURL() {
		return redirectedURL;
	}

	/**
	 * Returns the statusLine
	 * 
	 * @return statusLine
	 */
	public StatusLine getStatusLine() {
		return statusLine;
	}
}

package ch.supertomcat.bh.rules;

/**
 * Container class for HTML Code
 */
public class RuleHtmlCode {
	/**
	 * Flag if HTML Code is available
	 */
	private boolean available = false;

	/**
	 * HTML Code
	 */
	private String htmlCode = "";

	/**
	 * URL
	 */
	private String url = "";

	/**
	 * Referrer
	 */
	private String referrer = "";

	/**
	 * Constructor
	 */
	public RuleHtmlCode() {
	}

	/**
	 * Constructor
	 * Available Flag will be set to true
	 * 
	 * @param htmlCode HTML Code
	 * @param url URL
	 * @param referrer Referrer
	 */
	public RuleHtmlCode(String htmlCode, String url, String referrer) {
		setData(htmlCode, url, referrer);
	}

	/**
	 * Set Data (Available Flag will be set to true)
	 * 
	 * @param htmlCode HTML Code
	 * @param url URL
	 * @param referrer Referrer
	 */
	public void setData(String htmlCode, String url, String referrer) {
		this.htmlCode = htmlCode;
		this.url = url;
		this.referrer = referrer;
		available = true;
	}

	/**
	 * Returns the available
	 * 
	 * @return available
	 */
	public boolean isAvailable() {
		return available;
	}

	/**
	 * Sets the available
	 * 
	 * @param available available
	 */
	public void setAvailable(boolean available) {
		this.available = available;
	}

	/**
	 * Returns the htmlCode
	 * 
	 * @return htmlCode
	 */
	public String getHtmlCode() {
		return htmlCode;
	}

	/**
	 * Sets the htmlCode
	 * 
	 * @param htmlCode htmlCode
	 */
	public void setHtmlCode(String htmlCode) {
		this.htmlCode = htmlCode;
	}

	/**
	 * Returns the url
	 * 
	 * @return url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the url
	 * 
	 * @param url url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Returns the referrer
	 * 
	 * @return referrer
	 */
	public String getReferrer() {
		return referrer;
	}

	/**
	 * Sets the referrer
	 * 
	 * @param referrer referrer
	 */
	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}
}

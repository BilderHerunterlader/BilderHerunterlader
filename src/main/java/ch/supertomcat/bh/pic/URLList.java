package ch.supertomcat.bh.pic;

import java.util.List;

/**
 * 
 *
 */
public class URLList {
	private String title = "";
	private String referrer = "";
	private List<URL> urls = null;
	
	/**
	 * Constructor
	 * @param title Title
	 * @param referrer Referrer
	 * @param urls URLs
	 */
	public URLList(String title, String referrer, List<URL> urls) {
		super();
		this.title = title;
		this.referrer = referrer;
		this.urls = urls;
	}

	/**
	 * Returns the title
	 * @return title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Sets the title
	 * @param title title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Returns the referrer
	 * @return referrer
	 */
	public String getReferrer() {
		return referrer;
	}
	
	/**
	 * Sets the referrer
	 * @param referrer referrer
	 */
	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}
	
	/**
	 * Returns the urls
	 * @return urls
	 */
	public List<URL> getUrls() {
		return urls;
	}
	
	/**
	 * Sets the urls
	 * @param urls urls
	 */
	public void setUrls(List<URL> urls) {
		this.urls = urls;
	}
}

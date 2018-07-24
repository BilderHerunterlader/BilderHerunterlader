package ch.supertomcat.bh.downloader;

import ch.supertomcat.bh.hoster.URLParseObject;

/**
 * Class containing information about the parsed URL
 */
public class FileDownloaderParsedInfo {
	private URLParseObject result;

	private String referrer;

	/**
	 * Returns the result
	 * 
	 * @return result
	 */
	public URLParseObject getResult() {
		return result;
	}

	/**
	 * Sets the result
	 * 
	 * @param result result
	 */
	public void setResult(URLParseObject result) {
		this.result = result;
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

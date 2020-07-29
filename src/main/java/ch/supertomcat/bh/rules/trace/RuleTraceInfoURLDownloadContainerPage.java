package ch.supertomcat.bh.rules.trace;

/**
 * URL Pipeline Download Container Page Rule Trace Info
 */
public class RuleTraceInfoURLDownloadContainerPage extends RuleTraceInfoURLStep {
	/**
	 * URL
	 */
	private final String url;

	/**
	 * HTML Code
	 */
	private final String htmlCode;

	/**
	 * Constructor
	 * 
	 * @param step Step
	 * @param url URL
	 * @param htmlCode HTML Code
	 */
	public RuleTraceInfoURLDownloadContainerPage(int step, String url, String htmlCode) {
		super(step);
		this.url = url;
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
	 * Returns the htmlCode
	 * 
	 * @return htmlCode
	 */
	public String getHtmlCode() {
		return htmlCode;
	}

	@Override
	public String toString() {
		return step + " -> Download Container-Page done -> URL: " + url;
	}
}

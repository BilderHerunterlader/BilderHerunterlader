package ch.supertomcat.bh.rules.trace;

import java.util.ArrayList;
import java.util.List;

/**
 * Filename Pipeline Rule Trace Info
 */
public class RuleTraceInfoFailures {
	/**
	 * URL
	 */
	private final String url;

	/**
	 * Thumb URL
	 */
	private final String thumbURL;

	/**
	 * HTML Code
	 */
	private final String htmlCode;

	/**
	 * Before Replace Flag
	 */
	private final boolean beforeReplace;

	/**
	 * Steps
	 */
	private final List<RuleTraceInfoFailuresStep> steps = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param url URL
	 * @param thumbURL Thumb URL
	 * @param htmlCode HTML Code
	 * @param beforeReplace Before Replace Flag
	 */
	public RuleTraceInfoFailures(String url, String thumbURL, String htmlCode, boolean beforeReplace) {
		this.url = url;
		this.thumbURL = thumbURL;
		this.htmlCode = htmlCode;
		this.beforeReplace = beforeReplace;
	}

	/**
	 * Add Step
	 * 
	 * @param step Step
	 */
	public void addStep(RuleTraceInfoFailuresStep step) {
		steps.add(step);
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
	 * Returns the thumbURL
	 * 
	 * @return thumbURL
	 */
	public String getThumbURL() {
		return thumbURL;
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
	 * Returns the steps
	 * 
	 * @return steps
	 */
	public List<RuleTraceInfoFailuresStep> getSteps() {
		return steps;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Failures Pipeline");
		if (beforeReplace) {
			sb.append(" (Before Replace) ");
		} else {
			sb.append(" (After Replace) ");
		}
		sb.append("Done:\n-> URL: ");
		sb.append(url);
		sb.append("\n");
		if (beforeReplace) {
			sb.append("Thumb-URL: ");
			sb.append(thumbURL);
			sb.append("\n");
		}
		for (RuleTraceInfoFailuresStep step : steps) {
			sb.append("-> ");
			sb.append(step.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}

package ch.supertomcat.bh.rules.trace;

import java.util.ArrayList;
import java.util.List;

/**
 * URL Pipeline Rule Trace Info
 */
public class RuleTraceInfoURL {
	/**
	 * URL
	 */
	private final String url;

	/**
	 * Steps
	 */
	private final List<RuleTraceInfoURLStep> steps = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param url URL
	 */
	public RuleTraceInfoURL(String url) {
		this.url = url;
	}

	/**
	 * Add Step
	 * 
	 * @param step Step
	 */
	public void addStep(RuleTraceInfoURLStep step) {
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
	 * Returns the steps
	 * 
	 * @return steps
	 */
	public List<RuleTraceInfoURLStep> getSteps() {
		return steps;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("URL Pipeline Done:\n-> URL: ");
		sb.append(url);
		sb.append("\n");
		for (RuleTraceInfoURLStep step : steps) {
			sb.append("-> ");
			sb.append(step.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}

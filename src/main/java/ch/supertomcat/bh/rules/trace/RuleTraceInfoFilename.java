package ch.supertomcat.bh.rules.trace;

import java.util.ArrayList;
import java.util.List;

/**
 * Filename Pipeline Rule Trace Info
 */
public class RuleTraceInfoFilename {
	/**
	 * Download Selection Flag
	 */
	private final boolean downloadSelection;

	/**
	 * URL
	 */
	private final String url;

	/**
	 * Input String which was used
	 */
	private final String input;

	/**
	 * Steps
	 */
	private final List<RuleTraceInfoFilenameStep> steps = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param downloadSelection Download Selection Flag
	 * @param url URL
	 * @param input Input
	 */
	public RuleTraceInfoFilename(boolean downloadSelection, String url, String input) {
		this.downloadSelection = downloadSelection;
		this.url = url;
		this.input = input;
	}

	/**
	 * Add Step
	 * 
	 * @param step Step
	 */
	public void addStep(RuleTraceInfoFilenameStep step) {
		steps.add(step);
	}

	/**
	 * Returns the downloadSelection
	 * 
	 * @return downloadSelection
	 */
	public boolean isDownloadSelection() {
		return downloadSelection;
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
	 * Returns the input
	 * 
	 * @return input
	 */
	public String getInput() {
		return input;
	}

	/**
	 * Returns the steps
	 * 
	 * @return steps
	 */
	public List<RuleTraceInfoFilenameStep> getSteps() {
		return steps;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (downloadSelection) {
			sb.append("Filename on Download Selection Pipeline Done:\n-> URL: ");
		} else {
			sb.append("Filename Pipeline Done:\n-> URL: ");
		}
		sb.append(url);
		sb.append("\n");
		for (RuleTraceInfoFilenameStep step : steps) {
			sb.append("-> ");
			sb.append(step.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}

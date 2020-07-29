package ch.supertomcat.bh.rules.trace;

/**
 * URL Regex Search Rule Replace Info
 */
public class RuleTraceInfoURLRegexReplace extends RuleTraceInfoURLStep {
	/**
	 * Start Position
	 */
	private final int start;

	/**
	 * Result
	 */
	private final String result;

	/**
	 * Constructor
	 * 
	 * @param step Step
	 * @param start Start Position
	 * @param result Result
	 */
	public RuleTraceInfoURLRegexReplace(int step, int start, String result) {
		super(step);
		this.start = start;
		this.result = result;
	}

	/**
	 * Returns the start
	 * 
	 * @return start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Returns the result
	 * 
	 * @return result
	 */
	public String getResult() {
		return result;
	}

	@Override
	public String toString() {
		return "Regex Replace done -> Step " + step + " -> Result: " + result;
	}
}

package ch.supertomcat.bh.rules.trace;

/**
 * Filename Search Rule Replace Info
 */
public class RuleTraceInfoFilenameReplace extends RuleTraceInfoFilenameStep {
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
	public RuleTraceInfoFilenameReplace(int step, int start, String result) {
		super(step, start);
		this.result = result;
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
		return "Filename Replace done -> Step " + step + " -> Result: " + result;
	}
}

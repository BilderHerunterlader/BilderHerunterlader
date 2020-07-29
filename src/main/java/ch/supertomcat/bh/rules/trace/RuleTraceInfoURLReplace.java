package ch.supertomcat.bh.rules.trace;

/**
 * URL Pipeline Download Container Page Rule Trace Info
 */
public class RuleTraceInfoURLReplace extends RuleTraceInfoURLStep {
	/**
	 * Result
	 */
	private final String result;

	/**
	 * Constructor
	 * 
	 * @param step Step
	 * @param result Result
	 */
	public RuleTraceInfoURLReplace(int step, String result) {
		super(step);
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
		return step + " -> Result: " + result;
	}
}

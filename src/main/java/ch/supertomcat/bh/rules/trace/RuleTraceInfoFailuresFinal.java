package ch.supertomcat.bh.rules.trace;

/**
 * Failures Search Rule Final Info
 */
public class RuleTraceInfoFailuresFinal extends RuleTraceInfoFailuresStep {
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
	public RuleTraceInfoFailuresFinal(int step, int start, String result) {
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
		return "Failure Final Search done -> Step " + step + " -> Result: " + result;
	}
}

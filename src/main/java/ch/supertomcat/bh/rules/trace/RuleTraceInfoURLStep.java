package ch.supertomcat.bh.rules.trace;

/**
 * Base class for URL Pipeline rule trace info steps
 */
public abstract class RuleTraceInfoURLStep {
	/**
	 * Step
	 */
	protected final int step;

	/**
	 * Constructor
	 * 
	 * @param step Step
	 */
	public RuleTraceInfoURLStep(int step) {
		this.step = step;
	}

	/**
	 * Returns the step
	 * 
	 * @return step
	 */
	public int getStep() {
		return step;
	}
}

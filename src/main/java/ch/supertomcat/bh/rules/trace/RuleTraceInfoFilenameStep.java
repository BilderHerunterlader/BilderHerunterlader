package ch.supertomcat.bh.rules.trace;

/**
 * Base class for filename rule trace info step
 */
public abstract class RuleTraceInfoFilenameStep {
	/**
	 * Step
	 */
	protected final int step;

	/**
	 * Start Position
	 */
	protected final int start;

	/**
	 * Constructor
	 * 
	 * @param step Step
	 * @param start Start Position
	 */
	public RuleTraceInfoFilenameStep(int step, int start) {
		this.step = step;
		this.start = start;
	}

	/**
	 * Returns the step
	 * 
	 * @return step
	 */
	public int getStep() {
		return step;
	}

	/**
	 * Returns the start
	 * 
	 * @return start
	 */
	public int getStart() {
		return start;
	}
}

package ch.supertomcat.bh.rules.trace;

/**
 * Failures Search Rule Trace Info
 */
public class RuleTraceInfoFailuresSearch extends RuleTraceInfoFailuresStep {
	/**
	 * Found position or -1
	 */
	private final int position;

	/**
	 * Constructor
	 * 
	 * @param step Step
	 * @param start Start Position
	 * @param position Found position or -1
	 */
	public RuleTraceInfoFailuresSearch(int step, int start, int position) {
		super(step, start);
		this.position = position;
	}

	/**
	 * Returns the position
	 * 
	 * @return position
	 */
	public int getPosition() {
		return position;
	}

	@Override
	public String toString() {
		if (position >= 0) {
			return "Failure Search done -> Step " + step + " -> Pattern found at: " + position;
		} else {
			return "Failure Search done -> Step " + step + " -> Pattern not found!";
		}
	}
}

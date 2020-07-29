package ch.supertomcat.bh.rules.trace;

/**
 * URL Regex Search Rule Trace Info
 */
public class RuleTraceInfoURLRegexSearch extends RuleTraceInfoURLStep {
	/**
	 * Start Position
	 */
	private final int start;

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
	public RuleTraceInfoURLRegexSearch(int step, int start, int position) {
		super(step);
		this.start = start;
		this.position = position;
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
			return "Regex Search done -> Step " + step + " -> Pattern found at: " + position;
		} else {
			return "Regex Search done -> Step " + step + " -> Pattern not found!";
		}
	}
}

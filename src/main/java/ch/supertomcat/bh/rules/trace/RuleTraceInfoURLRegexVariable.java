package ch.supertomcat.bh.rules.trace;

/**
 * URL Regex Search Rule Replace Info
 */
public class RuleTraceInfoURLRegexVariable extends RuleTraceInfoURLStep {
	/**
	 * Variable Name
	 */
	private final String name;

	/**
	 * Value
	 */
	private final String value;

	/**
	 * Constructor
	 * 
	 * @param step Step
	 * @param name Variable Name
	 * @param value Value
	 */
	public RuleTraceInfoURLRegexVariable(int step, String name, String value) {
		super(step);
		this.name = name;
		this.value = value;
	}

	/**
	 * Returns the name
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the value
	 * 
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "Regex Variable stored -> Step " + step + " -> Name: " + name + ", Value: " + value;
	}
}

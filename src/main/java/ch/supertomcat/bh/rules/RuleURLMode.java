package ch.supertomcat.bh.rules;

/**
 * Duplicate Remove Mode
 */
public enum RuleURLMode {
	/**
	 * Replace in Container-URL
	 */
	RULEPIPELINE_MODE_CONTAINER_URL(0),

	/**
	 * Replace in Thumbnail-URL
	 */
	RULEPIPELINE_MODE_THUMBNAIL_URL(1);

	/**
	 * Value
	 */
	private final int value;

	/**
	 * Constructor
	 * 
	 * @param value Value
	 */
	private RuleURLMode(int value) {
		this.value = value;
	}

	/**
	 * Returns the value
	 * 
	 * @return value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @param value Value
	 * @return Mode
	 */
	public static RuleURLMode getByValue(int value) {
		for (RuleURLMode mode : RuleURLMode.values()) {
			if (mode.getValue() == value) {
				return mode;
			}
		}
		return RULEPIPELINE_MODE_CONTAINER_URL;
	}
}

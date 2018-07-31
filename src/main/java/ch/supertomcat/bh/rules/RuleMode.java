package ch.supertomcat.bh.rules;

/**
 * Duplicate Remove Mode
 */
public enum RuleMode {
	/**
	 * Replace in Container-URL or Thumbnail-URL
	 */
	RULE_MODE_CONTAINER_OR_THUMBNAIL_URL(0),

	/**
	 * Replace in Container-Page-Sourcecode
	 */
	RULE_MODE_CONTAINER_PAGE_SOURCECODE(1),

	/**
	 * Replace Filename
	 */
	RULE_MODE_FILENAME(2),

	/**
	 * Replace Filename
	 */
	RULE_MODE_FILENAME_ON_DOWNLOAD_SELECTION(3),

	/**
	 * RULE_MODE_FAILURES
	 */
	RULE_MODE_FAILURES(4),

	/**
	 * RULE_MODE_JAVASCRIPT
	 */
	RULE_MODE_JAVASCRIPT(5);

	/**
	 * Value
	 */
	private final int value;

	/**
	 * Constructor
	 * 
	 * @param value Value
	 */
	private RuleMode(int value) {
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
	public static RuleMode getByValue(int value) {
		for (RuleMode mode : RuleMode.values()) {
			if (mode.getValue() == value) {
				return mode;
			}
		}
		return RULE_MODE_CONTAINER_OR_THUMBNAIL_URL;
	}
}

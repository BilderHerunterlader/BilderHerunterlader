package ch.supertomcat.bh.rules;

/**
 * Duplicate Remove Mode
 */
public enum RuleFilenameMode {
	/**
	 * Use Container-URL-Filename-Part for Filename Search and Replace
	 */
	RULEPIPELINE_MODE_FILENAME_CONTAINER_URL_FILENAME_PART(0),

	/**
	 * Use Container-URL for Filename Search and Replace
	 */
	RULEPIPELINE_MODE_FILENAME_CONTAINER_URL(1),

	/**
	 * Use Thumbnail-URL-Filename-Part for Filename Search and Replace
	 */
	RULEPIPELINE_MODE_FILENAME_THUMBNAIL_URL_FILENAME_PART(2),

	/**
	 * Use Thumbnail-URL for Filename Search and Replace
	 */
	RULEPIPELINE_MODE_FILENAME_THUMBNAIL_URL(3),

	/**
	 * Use Container-Page-Sourcecode from first URL for Filename Search and Replace
	 */
	RULEPIPELINE_MODE_FILENAME_CONTAINER_PAGE_SOURCECODE(4),

	/**
	 * Use Download-URL for Filename Search and Replace
	 */
	RULEPIPELINE_MODE_FILENAME_DOWNLOAD_URL(5),

	/**
	 * Use Download-URL-Filename-Part for Filename Search and Replace
	 */
	RULEPIPELINE_MODE_FILENAME_DOWNLOAD_URL_FILENAME_PART(6),

	/**
	 * Use Last Container-URL-Filename-Part for Filename Search and Replace
	 */
	RULEPIPELINE_MODE_FILENAME_LAST_CONTAINER_URL_FILENAME_PART(7),

	/**
	 * Use Last Container-URL for Filename Search and Replace
	 */
	RULEPIPELINE_MODE_FILENAME_LAST_CONTAINER_URL(8),

	/**
	 * Use First Container-Page-Sourcecode for Filename Search and Replace
	 */
	RULEPIPELINE_MODE_FILENAME_FIRST_CONTAINER_PAGE_SOURCECODE(9),

	/**
	 * Use Last Container-Page-Sourcecode for Filename Search and Replace
	 */
	RULEPIPELINE_MODE_FILENAME_LAST_CONTAINER_PAGE_SOURCECODE(10);

	/**
	 * Value
	 */
	private final int value;

	/**
	 * Constructor
	 * 
	 * @param value Value
	 */
	private RuleFilenameMode(int value) {
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
	public static RuleFilenameMode getByValue(int value) {
		for (RuleFilenameMode mode : RuleFilenameMode.values()) {
			if (mode.getValue() == value) {
				return mode;
			}
		}
		return RULEPIPELINE_MODE_FILENAME_CONTAINER_URL_FILENAME_PART;
	}
}

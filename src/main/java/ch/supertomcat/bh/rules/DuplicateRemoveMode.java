package ch.supertomcat.bh.rules;

/**
 * Duplicate Remove Mode
 */
public enum DuplicateRemoveMode {
	/**
	 * DUPLICATES_BH_DEFAULT
	 */
	DUPLICATES_BH_DEFAULT(0),

	/**
	 * DUPLICATES_CONTAINER_URL_ONLY
	 */
	DUPLICATES_CONTAINER_URL_ONLY(1),

	/**
	 * DUPLICATES_CONTAINER_URL_AND_THUMBNAIL_URL
	 */
	DUPLICATES_CONTAINER_URL_AND_THUMBNAIL_URL(2),

	/**
	 * DUPLICATES_CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_FIRST
	 */
	DUPLICATES_CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_FIRST(3),

	/**
	 * DUPLICATES_CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_LAST
	 */
	DUPLICATES_CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_LAST(4),

	/**
	 * DUPLICATES_CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_FIRST
	 */
	DUPLICATES_CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_FIRST(5),

	/**
	 * DUPLICATES_CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_LAST
	 */
	DUPLICATES_CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_LAST(6);

	/**
	 * Value
	 */
	private final int value;

	/**
	 * Constructor
	 * 
	 * @param value Value
	 */
	private DuplicateRemoveMode(int value) {
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
	public static DuplicateRemoveMode getByValue(int value) {
		for (DuplicateRemoveMode mode : DuplicateRemoveMode.values()) {
			if (mode.getValue() == value) {
				return mode;
			}
		}
		return DUPLICATES_BH_DEFAULT;
	}
}

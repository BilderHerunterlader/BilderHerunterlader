package ch.supertomcat.bh.queue;

/**
 * Download Compare Type
 */
public enum DownloadCompareType {
	/**
	 * Sort first by container url, then by directory
	 */
	BY_BOTH_CONTAINER_URL_FIRST(0),

	/**
	 * Sort first by directory, then by container url
	 */
	BY_BOTH_DIRECTORY_FIRST(1),

	/**
	 * Sort only by container url
	 */
	BY_CONTAINER_URL_ONLY(2),

	/**
	 * Sort only by directory
	 */
	BY_TARGET_DIRECTORY_ONLY(3),

	/**
	 * Do not Sort
	 */
	NO_SORT(4),

	/**
	 * Sort only by directory
	 */
	BY_DATE_TIME_ADDED_ONLY(5);

	/**
	 * Value
	 */
	private final int value;

	/**
	 * Constructor
	 * 
	 * @param value Value
	 */
	private DownloadCompareType(int value) {
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
	 * @return Type
	 */
	public static DownloadCompareType getByValue(int value) {
		for (DownloadCompareType type : DownloadCompareType.values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		return NO_SORT;
	}
}

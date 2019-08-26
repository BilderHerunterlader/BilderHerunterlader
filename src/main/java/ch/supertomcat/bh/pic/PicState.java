package ch.supertomcat.bh.pic;

import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Pic State Enum
 */
public enum PicState {
	/**
	 * Download is sleeping
	 */
	SLEEPING(0, "Sleeping"),

	/**
	 * Download is waiting for a free slot
	 */
	WAITING(1, "Waiting"),

	/**
	 * Download is downloading
	 */
	DOWNLOADING(2, "Downloading"),

	/**
	 * Download is complete
	 */
	COMPLETE(3, "Complete"),

	/**
	 * Download failed
	 */
	FAILED(4, "Failed"),

	/**
	 * Download failed
	 */
	ABORTING(5, "Aborting"),

	/**
	 * Download failed
	 */
	FAILED_FILE_NOT_EXIST(6, "FileNotExistsOnTheServer"),

	/**
	 * Download failed
	 */
	FAILED_FILE_TEMPORARY_OFFLINE(7, "FileTemporaryOffline");

	/**
	 * Value in config file
	 */
	private final int value;

	/**
	 * Locale Key
	 */
	private final String localeKey;

	/**
	 * Constructor
	 * 
	 * @param value Value in config file
	 * @param localeKey Locale Key
	 */
	private PicState(int value, String localeKey) {
		this.value = value;
		this.localeKey = localeKey;
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
	 * Returns the localeKey
	 * 
	 * @return localeKey
	 */
	public String getLocaleKey() {
		return localeKey;
	}

	/**
	 * @return Text
	 */
	public String getText() {
		return Localization.getString(localeKey);
	}

	/**
	 * Get PicState by value
	 * 
	 * @param value Value
	 * @return PicState
	 */
	public static PicState getByValue(int value) {
		for (PicState state : PicState.values()) {
			if (state.getValue() == value) {
				return state;
			}
		}
		LoggerFactory.getLogger(PicState.class).error("Unknown value: {}", value);
		return SLEEPING;
	}
}

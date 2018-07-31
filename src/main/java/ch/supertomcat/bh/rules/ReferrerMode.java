package ch.supertomcat.bh.rules;

/**
 * Referrer Mode
 */
public enum ReferrerMode {
	/**
	 * No referrer
	 */
	REFERRER_NO_REFERRER(0),

	/**
	 * Last Container-URL as referrer
	 */
	REFERRER_LAST_CONTAINER_URL(1),

	/**
	 * Last Container-URL as referrer
	 */
	REFERRER_FIRST_CONTAINER_URL(2),

	/**
	 * Origin-Page (Referrer-URL) as referrer
	 */
	REFERRER_ORIGIN_PAGE(3),

	/**
	 * Custom string as referrer
	 */
	REFERRER_CUSTOM(4);

	/**
	 * Value
	 */
	private final int value;

	/**
	 * Constructor
	 * 
	 * @param value Value
	 */
	private ReferrerMode(int value) {
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
	public static ReferrerMode getByValue(int value) {
		for (ReferrerMode mode : ReferrerMode.values()) {
			if (mode.getValue() == value) {
				return mode;
			}
		}
		return REFERRER_NO_REFERRER;
	}
}

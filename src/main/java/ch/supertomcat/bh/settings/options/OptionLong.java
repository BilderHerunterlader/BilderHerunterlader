package ch.supertomcat.bh.settings.options;

/**
 * Long Option
 */
public class OptionLong extends Option<Long> {
	/**
	 * Constructor
	 * @param path Path
	 */
	public OptionLong(String path) {
		this(path, 0L);
	}
	
	/**
	 * Constructor
	 * @param path Path
	 * @param value Value
	 */
	public OptionLong(String path, Long value) {
		super(Long.class, "long", path, value);
	}

	@Override
	public String getValueAsString() {
		return String.valueOf((long)super.getValue());
	}
}

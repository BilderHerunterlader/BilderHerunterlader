package ch.supertomcat.bh.settings.options;

/**
 * Boolean Option
 */
public class OptionBoolean extends Option<Boolean> {
	/**
	 * Constructor
	 * @param path Path
	 */
	public OptionBoolean(String path) {
		this(path, false);
	}
	
	/**
	 * Constructor
	 * @param path Path
	 * @param value Value
	 */
	public OptionBoolean(String path, Boolean value) {
		super(Boolean.class, "boolean", path, value);
	}

	@Override
	public String getValueAsString() {
		return String.valueOf((boolean)super.getValue());
	}
}

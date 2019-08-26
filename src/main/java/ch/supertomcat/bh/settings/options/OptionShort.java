package ch.supertomcat.bh.settings.options;

/**
 * Short Option
 */
public class OptionShort extends Option<Short> {
	/**
	 * Constructor
	 * @param path Path
	 */
	public OptionShort(String path) {
		this(path, (short)0);
	}
	
	/**
	 * Constructor
	 * @param path Path
	 * @param value Value
	 */
	public OptionShort(String path, Short value) {
		super(Short.class, "short", path, value);
	}

	@Override
	public String getValueAsString() {
		return String.valueOf((short)super.getValue());
	}
}

package ch.supertomcat.bh.settings.options;

/**
 * Byte Option
 */
public class OptionByte extends Option<Byte> {
	/**
	 * Constructor
	 * @param path Path
	 */
	public OptionByte(String path) {
		this(path, (byte)0);
	}
	
	/**
	 * Constructor
	 * @param path Path
	 * @param value Value
	 */
	public OptionByte(String path, Byte value) {
		super(Byte.class, "byte", path, value);
	}

	@Override
	public String getValueAsString() {
		return String.valueOf((byte)super.getValue());
	}
}

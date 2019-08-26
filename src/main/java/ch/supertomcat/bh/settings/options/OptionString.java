package ch.supertomcat.bh.settings.options;

/**
 * String Option
 */
public class OptionString extends Option<String> {
	/**
	 * Constructor
	 * @param path Path
	 */
	public OptionString(String path) {
		this(path, "");
	}
	
	/**
	 * Constructor
	 * @param path Path
	 * @param value Value
	 */
	public OptionString(String path, String value) {
		super(String.class, "string", path, value);
	}

	@Override
	public String getValueAsString() {
		return super.getValue();
	}
}

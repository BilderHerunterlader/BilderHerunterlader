package ch.supertomcat.bh.settings.options;

/**
 * Int Option
 */
public class OptionInt extends Option<Integer> {
	/**
	 * Constructor
	 * @param path Path
	 */
	public OptionInt(String path) {
		this(path, 0);
	}
	
	/**
	 * Constructor
	 * @param path Path
	 * @param value Value
	 */
	public OptionInt(String path, Integer value) {
		super(Integer.class, "int", path, value);
	}

	@Override
	public String getValueAsString() {
		return String.valueOf((int)super.getValue());
	}
}

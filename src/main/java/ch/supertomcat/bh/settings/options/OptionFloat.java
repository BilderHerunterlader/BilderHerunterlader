package ch.supertomcat.bh.settings.options;

/**
 * Float Option
 */
public class OptionFloat extends Option<Float> {
	/**
	 * Constructor
	 * @param path Path
	 */
	public OptionFloat(String path) {
		this(path, 0f);
	}
	
	/**
	 * Constructor
	 * @param path Path
	 * @param value Value
	 */
	public OptionFloat(String path, Float value) {
		super(Float.class, "float", path, value);
	}

	@Override
	public String getValueAsString() {
		return String.valueOf((float)super.getValue());
	}
}

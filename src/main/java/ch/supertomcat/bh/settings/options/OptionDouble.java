package ch.supertomcat.bh.settings.options;

/**
 * Double Option
 */
public class OptionDouble extends Option<Double> {
	/**
	 * Constructor
	 * @param path Path
	 */
	public OptionDouble(String path) {
		this(path, 0d);
	}
	
	/**
	 * Constructor
	 * @param path Path
	 * @param value Value
	 */
	public OptionDouble(String path, Double value) {
		super(Double.class, "double", path, value);
	}

	@Override
	public String getValueAsString() {
		return String.valueOf((double)super.getValue());
	}
}

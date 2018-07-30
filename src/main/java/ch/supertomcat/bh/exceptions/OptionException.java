package ch.supertomcat.bh.exceptions;

/**
 * This exception is thrown if a option is not available in the SettingsManager
 */
public class OptionException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param s Message
	 */
	public OptionException(String s) {
		super(s);
	}
}

package ch.supertomcat.bh.exceptions;

/**
 * This exception is thrown if a option is not available in the SettingsManager
 */
public class OptionException extends Exception {
	
	/**
	 * UID
	 */
	private static final long serialVersionUID = 3508697526063360721L;
	
	/**
	 * Constructor
	 * @param s Message
	 */
	public OptionException(String s) {
		super(s);
	}
}

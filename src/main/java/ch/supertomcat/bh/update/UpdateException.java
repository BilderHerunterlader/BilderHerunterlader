package ch.supertomcat.bh.update;

/**
 * 
 *
 */
public class UpdateException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param message Message
	 * @param cause Cause
	 */
	public UpdateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor
	 * 
	 * @param message Message
	 */
	public UpdateException(String message) {
		super(message);
	}
}

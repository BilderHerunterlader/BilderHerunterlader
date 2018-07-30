package ch.supertomcat.bh.exceptions;

/**
 * This exception will be thrown by hostclasses
 */
public class HostException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param message Message
	 */
	public HostException(String message) {
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param message Message
	 * @param cause Cause
	 */
	public HostException(String message, Throwable cause) {
		super(message, cause);
	}
}

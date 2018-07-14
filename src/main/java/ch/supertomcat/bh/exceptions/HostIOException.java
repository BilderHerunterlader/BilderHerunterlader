package ch.supertomcat.bh.exceptions;

/**
 * This exception is thrown when a hostclass gets an error while downloading a container-page
 */
public class HostIOException extends HostException {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1846879131264654095L;
	
	/**
	 * Constructor
	 * @param message Message
	 */
	public HostIOException(String message) {
		super(message);
	}

	/**
	 * Constructor
	 * @param message Message
	 * @param cause Cause
	 */
	public HostIOException(String message, Throwable cause) {
		super(message, cause);
	}
}

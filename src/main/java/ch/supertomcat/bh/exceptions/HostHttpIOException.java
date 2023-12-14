package ch.supertomcat.bh.exceptions;

import java.io.IOException;

/**
 * This exception is thrown when a hostclass gets an error while downloading a container-page
 */
public class HostHttpIOException extends IOException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param message Message
	 */
	public HostHttpIOException(String message) {
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param message Message
	 * @param cause Cause
	 */
	public HostHttpIOException(String message, Throwable cause) {
		super(message, cause);
	}
}

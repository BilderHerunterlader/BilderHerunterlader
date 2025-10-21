package ch.supertomcat.bh.update;

import java.io.IOException;

/**
 * Update IO Exception
 */
public class UpdateIOException extends IOException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param message Message
	 * @param cause Cause
	 */
	public UpdateIOException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor
	 * 
	 * @param message Message
	 */
	public UpdateIOException(String message) {
		super(message);
	}
}

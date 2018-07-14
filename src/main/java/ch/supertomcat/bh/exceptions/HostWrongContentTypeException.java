package ch.supertomcat.bh.exceptions;

/**
 * This exception is thrown when a specific content type is expected, but the content type in the HTTP Response header is different than the expected type
 */
public class HostWrongContentTypeException extends HostException {
	private static final long serialVersionUID = 1L;

	/**
	 * Expected Type
	 */
	private final String expectedType;

	/**
	 * Actual Type
	 */
	private final String actualType;

	/**
	 * Constructor
	 * 
	 * @param message Message
	 * @param expectedType Expected Type
	 * @param actualType Actual Type
	 */
	public HostWrongContentTypeException(String message, String expectedType, String actualType) {
		super(message);
		this.expectedType = expectedType;
		this.actualType = actualType;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + ": Expected: " + expectedType + ", Actual: " + actualType;
	}

	/**
	 * Returns the expectedType
	 * 
	 * @return expectedType
	 */
	public String getExpectedType() {
		return expectedType;
	}

	/**
	 * Returns the actualType
	 * 
	 * @return actualType
	 */
	public String getActualType() {
		return actualType;
	}
}

package ch.supertomcat.bh.exceptions;

/**
 * This exception is for hostclasses if they didn't find the url of a file
 */
public class HostImageUrlNotFoundException extends HostException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param s Message
	 */
	public HostImageUrlNotFoundException(String s) {
		super(s);
	}
}

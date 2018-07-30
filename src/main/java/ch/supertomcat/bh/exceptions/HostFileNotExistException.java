package ch.supertomcat.bh.exceptions;

/**
 * This is exception is for every download where the file is not exist
 * anymore on the server. This exception should only be used when this
 * is permanent.
 * When the file only not exists for a known or unkown duration then
 * a normal HostException should be thrown.
 */
public class HostFileNotExistException extends HostException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param s Message
	 */
	public HostFileNotExistException(String s) {
		super(s);
	}
}

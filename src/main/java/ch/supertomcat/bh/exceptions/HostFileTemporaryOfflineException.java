package ch.supertomcat.bh.exceptions;

/**
 * This is exception is for every download where the file is not exist
 * anymore on the server. This exception should only be used when this
 * is permanent.
 * When the file only not exists for a known or unkown duration then
 * a normal HostException should be thrown.
 */
public class HostFileTemporaryOfflineException extends HostException {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1846879131264654095L;
	
	/**
	 * Constructor
	 * @param s Message
	 */
	public HostFileTemporaryOfflineException(String s) {
		super(s);
	}
}

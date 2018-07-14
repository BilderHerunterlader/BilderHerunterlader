package ch.supertomcat.bh.exceptions;

/**
 * This exception is to let the pic now, that the download was aborted while parsing the url.
 */
public class HostAbortedException extends HostException {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1846879131264654095L;
	
	/**
	 * Constructor
	 * @param s Message
	 */
	public HostAbortedException(String s) {
		super(s);
	}
}

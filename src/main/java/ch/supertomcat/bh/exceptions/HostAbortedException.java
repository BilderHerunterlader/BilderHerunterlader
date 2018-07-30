package ch.supertomcat.bh.exceptions;

/**
 * This exception is to let the pic now, that the download was aborted while parsing the url.
 */
public class HostAbortedException extends HostException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param s Message
	 */
	public HostAbortedException(String s) {
		super(s);
	}
}

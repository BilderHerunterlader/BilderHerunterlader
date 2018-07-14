package ch.supertomcat.bh.exceptions;

/**
 * This exception is to let the pic now, that it not should download anything
 * and just delete itself from the queue.
 * I added this for a host-class, which downloads a coppermine-gallery recursivly.
 * There the category- and thumbnail-pages are downloaded in the class itself
 * and the links added to the queue. So the pic should then not download the page
 * again and write it to a file.
 */
public class HostCompletedException extends HostException {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1846879131264654095L;
	
	/**
	 * Constructor
	 * @param s Message
	 */
	public HostCompletedException(String s) {
		super(s);
	}
}

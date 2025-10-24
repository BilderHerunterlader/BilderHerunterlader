package ch.supertomcat.bh.log;

/**
 * Blacklist Entry
 */
public class BlacklistEntry {
	/**
	 * ID
	 */
	private int id;

	/**
	 * URL
	 */
	private final String url;

	/**
	 * Constructor
	 * 
	 * @param url URL
	 */
	public BlacklistEntry(String url) {
		this.url = url;
	}

	/**
	 * Constructor
	 * 
	 * @param id ID
	 * @param url URL
	 */
	public BlacklistEntry(int id, String url) {
		this.id = id;
		this.url = url;
	}

	/**
	 * Returns the id
	 * 
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id
	 * 
	 * @param id id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns the url
	 * 
	 * @return url
	 */
	public String getUrl() {
		return url;
	}
}

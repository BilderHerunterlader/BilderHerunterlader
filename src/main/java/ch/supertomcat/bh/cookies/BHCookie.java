package ch.supertomcat.bh.cookies;

import org.apache.hc.client5.http.cookie.Cookie;

/**
 * Cookie
 */
public class BHCookie {
	/**
	 * ID
	 */
	private int id;

	/**
	 * Cookie
	 */
	private final Cookie cookie;

	/**
	 * Constructor
	 * 
	 * @param cookie Cookie
	 */
	public BHCookie(Cookie cookie) {
		this.cookie = cookie;
	}

	/**
	 * Constructor
	 * 
	 * @param id ID
	 * @param cookie Cookie
	 */
	public BHCookie(int id, Cookie cookie) {
		this.id = id;
		this.cookie = cookie;
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
	 * Returns the cookie
	 * 
	 * @return cookie
	 */
	public Cookie getCookie() {
		return cookie;
	}

}

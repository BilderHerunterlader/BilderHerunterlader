package ch.supertomcat.bh.rules;

import org.apache.hc.core5.http.protocol.HttpContext;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.Hoster;

/**
 * Dummy Hoster used to have access to downloadContainerPage methods
 */
public class JavascriptDummyHoster extends Hoster {
	/**
	 * Name
	 */
	private final String name;

	/**
	 * HTTP Context
	 */
	private final HttpContext httpContext;

	/**
	 * Constructor
	 * 
	 * @param name Name
	 * @param httpContext HTTP Context
	 */
	public JavascriptDummyHoster(String name, HttpContext httpContext) {
		this.name = name;
		this.httpContext = httpContext;
	}

	/**
	 * Method invoked from javascript to download container page
	 * 
	 * @param url URL
	 * @param referrer Referrer
	 * @return Container Page
	 * @throws HostException
	 */
	public String downloadContainerPage(String url, String referrer) throws HostException {
		return downloadContainerPage(name, url, referrer, httpContext);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public void setEnabled(boolean enabled) {
		// Nothing to do
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean canBeDisabled() {
		return false;
	}
}

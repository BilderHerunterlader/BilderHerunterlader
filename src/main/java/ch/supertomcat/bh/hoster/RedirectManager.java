package ch.supertomcat.bh.hoster;

import java.util.ArrayList;
import java.util.List;

import ch.supertomcat.bh.hoster.classloader.HostClassesLoader;
import ch.supertomcat.bh.hoster.hostimpl.HostRules;
import ch.supertomcat.bh.pic.URL;

/**
 * Class which holds the redirect-classes
 */
public class RedirectManager {
	/**
	 * Redirects
	 */
	private List<IRedirect> redirects = HostClassesLoader.loadRedirectClasses();

	/**
	 * Constructor
	 */
	public RedirectManager() {
	}

	/**
	 * @param hr HostRules
	 */
	void setHR(HostRules hr) {
		// Rules can now also be redirects
		redirects.add(hr);
	}

	/**
	 * Returns an array of all hostclasses
	 * 
	 * @return Hostclasses-array
	 */
	public List<IRedirect> getRedirects() {
		return new ArrayList<>(redirects);
	}

	/**
	 * Get-Methode
	 * 
	 * @param index Index
	 * @return Rule
	 */
	public IRedirect getRedirect(int index) {
		if (index < 0 || index >= redirects.size()) {
			return null;
		}
		return redirects.get(index);
	}

	/**
	 * Returns the version of a Redirect-class
	 * 
	 * @param name Name of the Redirect-class
	 * @return Version
	 */
	public String getRedirectVersion(String name) {
		for (IRedirect redirect : redirects) {
			if (redirect.getName().equals(name)) {
				return redirect.getVersion();
			}
		}
		return "";
	}

	/**
	 * Returns the redirected URL or the original URL if there was no
	 * redirect-class available for the URL
	 * 
	 * @param url Redirect-URL
	 * @return Container-URL
	 */
	public String checkURLForRedirect(URL url) {
		for (IRedirect redirect : redirects) {
			if (redirect.isEnabled() && redirect.isFromThisRedirect(url)) {
				return redirect.getURL(url);
			}
		}
		return url.getURL();
	}
}

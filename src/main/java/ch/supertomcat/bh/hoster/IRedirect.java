package ch.supertomcat.bh.hoster;

import java.util.EventListener;

import ch.supertomcat.bh.pic.URL;


/**
 * Interface which all redirect-classes have to implement
 */
public interface IRedirect extends EventListener{
	/**
	 * Check if the redirect-class wants to redirect the URL 
	 * @param url URL
	 * @return True/False
	 */
	public boolean isFromThisRedirect(URL url);
	
	/**
	 * Returns the redirected URL
	 * @param url URL
	 * @return Redirected-URL
	 */
	public String getURL(URL url);
	
	/**
	 * Returns the version of this class
	 * @return Version
	 */
	public String getVersion();
	
	/**
	 * Returns the name of this class
	 * @return Name
	 */
	public String getName();
	
	/**
	 * Returns the enabled
	 * @return enabled
	 */
	public boolean isEnabled();

	/**
	 * Sets the enabled
	 * @param enabled enabled
	 */
	public void setEnabled(boolean enabled);
}

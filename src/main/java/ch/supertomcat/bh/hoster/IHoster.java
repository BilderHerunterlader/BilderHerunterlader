package ch.supertomcat.bh.hoster;

import ch.supertomcat.bh.exceptions.HostException;

/**
 * Interface which all host-classes have to implement
 */
public interface IHoster {
	/**
	 * Returns the name of this class
	 * @return Name
	 */
	public String getName();
	
	/**
	 * Returns the version of this class
	 * @return Version
	 */
	public String getVersion();
	
	/**
	 * Parses the URL and returns the direct URL
	 * @param upo URLParseObject
	 * @throws HostException 
	 */
	public void parseURLAndFilename(URLParseObject upo) throws HostException;
	
	/**
	 * Check if the host-class will parse the URL
	 * @param url Container-URL
	 * @return True/False
	 */
	public boolean isFromThisHoster(String url);
	
	/**
	 * Returns the filename from the URL
	 * This method is to display a nice filename in the Download-Selection-Dialog 
	 * @param url Container-URL
	 * @return Filename
	 */
	public String getFilenameFromURL(String url);
}

package ch.supertomcat.bh.update;

import ch.supertomcat.bh.update.containers.UpdateList;


/**
 * 
 */
public interface UpdateSource {
	/**
	 * Checks for Updates and returns a list of available updates
	 * @return UpdateList
	 * @throws UpdateException 
	 */
	public UpdateList checkForUpdates() throws UpdateException;
}

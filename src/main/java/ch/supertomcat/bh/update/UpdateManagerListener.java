package ch.supertomcat.bh.update;

import ch.supertomcat.bh.update.containers.UpdateObject;

/**
 * Listener for UpdateManager
 */
public interface UpdateManagerListener {
	/**
	 * Updates started
	 */
	public void updatesStarted();
	
	/**
	 * Updates installed
	 * @param updateCount Number of installed updates
	 */
	public void updatesInstalled(int updateCount);
	
	/**
	 * New Program Version installed
	 */
	public void newProgramVersionInstalled();
	
	/**
	 * New Program Version Install failed
	 */
	public void newProgramVersionInstallFailed();
	
	/**
	 * Updates complete
	 */
	public void updatesComplete();
	
	/**
	 * Updates failed
	 */
	public void updatesFailed();
	
	/**
	 * @param updateType Update Type
	 * @param updateActionType Action Type
	 * @param source Source
	 * @param target Target
	 */
	public void updateInstallStarted(UpdateObject.UpdateType updateType, UpdateObject.UpdateActionType updateActionType, String source, String target);
	
	/**
	 * @param updateType Update Type
	 * @param updateActionType Action Type
	 */
	public void updateInstallComplete(UpdateObject.UpdateType updateType, UpdateObject.UpdateActionType updateActionType);
	
	/**
	 * @param updateType Update Type
	 * @param updateActionType Action Type
	 */
	public void updateInstallFailed(UpdateObject.UpdateType updateType, UpdateObject.UpdateActionType updateActionType);
	
	/**
	 * An error occured
	 * @param message Message
	 */
	public void errorOccured(String message);
}

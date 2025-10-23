package ch.supertomcat.bh.update;

import ch.supertomcat.bh.update.containers.UpdateActionType;
import ch.supertomcat.bh.update.containers.UpdateType;

/**
 * Listener for UpdateManager
 */
public interface UpdateManagerListener {
	/**
	 * Updates started
	 */
	public void updatesStarted();

	/**
	 * Updates complete
	 */
	public void updatesComplete();

	/**
	 * Updates failed
	 */
	public void updatesFailed();

	/**
	 * New Program Version installed
	 */
	public void newProgramVersionInstalled();

	/**
	 * New Program Version Install failed
	 */
	public void newProgramVersionInstallFailed();

	/**
	 * Update download started
	 * 
	 * @param updateType Update Type
	 * @param updateActionType Action Type
	 * @param source Source
	 * @param target Target
	 */
	public void updateDownloadStarted(UpdateType updateType, UpdateActionType updateActionType, String source, String target);

	/**
	 * Update download complete
	 * 
	 * @param updateType Update Type
	 * @param updateActionType Action Type
	 */
	public void updateDownloadComplete(UpdateType updateType, UpdateActionType updateActionType);

	/**
	 * Update unpack started
	 * 
	 * @param updateType Update Type
	 * @param updateActionType Action Type
	 * @param source Source
	 * @param target Target
	 */
	public void updateUnpackStarted(UpdateType updateType, UpdateActionType updateActionType, String source, String target);

	/**
	 * Update unpack complete
	 * 
	 * @param updateType Update Type
	 * @param updateActionType Action Type
	 * 
	 */
	public void updateUnpackComplete(UpdateType updateType, UpdateActionType updateActionType);

	/**
	 * Update copy started
	 * 
	 * @param source Source
	 * @param target Target
	 */
	public void updateCopyStarted(String source, String target);

	/**
	 * Update copy complete
	 */
	public void updateCopyComplete();

	/**
	 * An error occured
	 * 
	 * @param message Message
	 * @param ex Exception
	 */
	public void errorOccured(String message, Exception ex);
}

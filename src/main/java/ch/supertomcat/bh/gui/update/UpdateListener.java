package ch.supertomcat.bh.gui.update;

/**
 * Update Listener
 */
public interface UpdateListener {
	/**
	 * Update-Window was opened and will be open until updateWindowClosed-Method is called
	 */
	public void updateWindowOpened();

	/**
	 * Update-Window was closed
	 * If an update was downloaded and installed updateRunned is true otherwise false.
	 * updateSuccessfull is true if the update was successfull otherwise it will be false.
	 * 
	 * @param updateRunned Update runned
	 * @param updateSuccessfull Update successfull
	 */
	public void updateWindowClosed(boolean updateRunned, boolean updateSuccessfull);
}

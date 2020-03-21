package ch.supertomcat.bh.gui;

import java.awt.Component;

import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;

/**
 * Main Window Access Interface
 */
public interface MainWindowAccess {
	/**
	 * Get-Method
	 * 
	 * @return Message
	 */
	public String getMessage();

	/**
	 * Set-Method
	 * 
	 * @param message Message
	 */
	public void setMessage(String message);

	/**
	 * @param progress Progress
	 */
	public void addProgressObserver(ProgressObserver progress);

	/**
	 * @param progress Progress
	 */
	public void removeProgressObserver(ProgressObserver progress);

	/**
	 * Checks if the tab with the given component is selected
	 * 
	 * @param tabComponent Tab Component
	 * @return True if the tab with the given component is selected, false otherwise
	 */
	public boolean isTabSelected(Component tabComponent);

	/**
	 * Clear Keyword filters
	 */
	public void clearKeywordFilters();
}

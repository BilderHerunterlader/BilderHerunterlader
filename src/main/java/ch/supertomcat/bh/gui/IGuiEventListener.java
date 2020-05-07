package ch.supertomcat.bh.gui;

/**
 * Interface for the GUI
 */
public interface IGuiEventListener {
	/**
	 * Exit program
	 * 
	 * @param restart Restart
	 * @param update True if exit is done, because of installed updates, false otherwise
	 */
	public void exitApp(boolean restart, boolean update);

	/**
	 * Exit program forced
	 * 
	 * @param restart Restart
	 * @param update True if exit is done, because of installed updates, false otherwise
	 */
	public void exitAppForced(boolean restart, boolean update);

	/**
	 * Hide Main-Window
	 */
	public void hideWindow();
}

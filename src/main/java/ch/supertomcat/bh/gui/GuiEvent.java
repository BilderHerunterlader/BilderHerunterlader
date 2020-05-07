package ch.supertomcat.bh.gui;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JOptionPane;

import ch.supertomcat.bh.gui.update.UpdateListener;

/**
 * Class which contains Listeners for GUI-Events
 */
public class GuiEvent {
	/**
	 * Listeners
	 */
	private List<IGuiEventListener> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Listeners
	 */
	private List<UpdateListener> listenersUpdate = new CopyOnWriteArrayList<>();

	/**
	 * Constructor
	 */
	public GuiEvent() {
	}

	/**
	 * Hide Main-Window
	 */
	public void hideWindow() {
		for (IGuiEventListener listener : listeners) {
			listener.hideWindow();
		}
	}

	/**
	 * Exit program
	 * 
	 * @param restart Restart
	 * @param update True if exit is done, because of installed updates, false otherwise
	 */
	public void exitApp(boolean restart, boolean update) {
		for (IGuiEventListener listener : listeners) {
			listener.exitApp(restart, update);
		}
	}

	/**
	 * Exit programm forced
	 * 
	 * @param message Message
	 * @param title Title
	 * @param owner Owner
	 * @param restart Restart
	 * @param update True if exit is done, because of installed updates, false otherwise
	 */
	public void exitAppForced(String message, String title, Component owner, boolean restart, boolean update) {
		JOptionPane.showMessageDialog(owner, message, title, JOptionPane.WARNING_MESSAGE);
		for (IGuiEventListener listener : listeners) {
			listener.exitAppForced(restart, update);
		}
	}

	/**
	 * @param l Listener
	 */
	public void addListener(IGuiEventListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	/**
	 * @param l Listener
	 */
	public void removeListener(IGuiEventListener l) {
		listeners.remove(l);
	}

	/**
	 * Update-Window was opened and will be open until updateWindowClosed-Method is called
	 */
	public void updateWindowOpened() {
		for (UpdateListener listener : listenersUpdate) {
			listener.updateWindowOpened();
		}
	}

	/**
	 * Update-Window was closed
	 * If an update was downloaded and installed updateRunned is true otherwise false.
	 * updateSuccessfull is true if the update was successfull otherwise it will be false.
	 * 
	 * @param updateRunned Update runned
	 * @param updateSuccessfull Update successfull
	 */
	public void updateWindowClosed(boolean updateRunned, boolean updateSuccessfull) {
		for (UpdateListener listener : listenersUpdate) {
			listener.updateWindowClosed(updateRunned, updateSuccessfull);
		}
	}

	/**
	 * @param l Listener
	 */
	public void addUpdateListener(UpdateListener l) {
		if (!listenersUpdate.contains(l)) {
			listenersUpdate.add(l);
		}
	}

	/**
	 * @param l Listener
	 */
	public void removeUpdateListener(UpdateListener l) {
		listenersUpdate.remove(l);
	}
}

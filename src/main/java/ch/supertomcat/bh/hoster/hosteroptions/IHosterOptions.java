package ch.supertomcat.bh.hoster.hosteroptions;

/**
 * This interface can be implemented by host-classes, which provides
 * some options to set. So in the Rule-Tab on the GUI there will be a button
 * which will fire the openOptionsDialog-Method on the class.
 */
public interface IHosterOptions {
	/**
	 * When this Method is fired, then the host-class should display
	 * a dialog to let the user set options of the host-class.
	 */
	public void openOptionsDialog();
}

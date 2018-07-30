package ch.supertomcat.bh.settings;

/**
 * Interface of SettingsManager
 */
public interface ISettingsListener {
	/**
	 * This method is fired when settings had changed
	 */
	public void settingsChanged();
}

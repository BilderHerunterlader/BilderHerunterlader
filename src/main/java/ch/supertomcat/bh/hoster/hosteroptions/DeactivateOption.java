package ch.supertomcat.bh.hoster.hosteroptions;

import ch.supertomcat.bh.settings.SettingsManager;

/**
 * OverrideDirectoryOption
 */
public class DeactivateOption {
	private final String optionPrefix;

	private boolean deactivated;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Constructor
	 * 
	 * @param optionPrefix Prefix
	 * @param settingsManager Settings Manager
	 */
	public DeactivateOption(String optionPrefix, SettingsManager settingsManager) {
		this.optionPrefix = optionPrefix;
		this.settingsManager = settingsManager;
		deactivated = settingsManager.isHostDeactivated(optionPrefix);
	}

	/**
	 * Returns the deactivated
	 * 
	 * @return deactivated
	 */
	public boolean isDeactivated() {
		return deactivated;
	}

	/**
	 * Sets the deactivated
	 * 
	 * @param deactivated deactivated
	 */
	public void setDeactivated(boolean deactivated) {
		this.deactivated = deactivated;
	}

	/**
	 * Returns the optionPrefix
	 * 
	 * @return optionPrefix
	 */
	public String getOptionPrefix() {
		return optionPrefix;
	}

	/**
	 * Save Option
	 * Note: It only saves the option in SettingsManager, but it does not write the settings to the settings-file
	 */
	public void saveOption() {
		settingsManager.setHostDeactivated(optionPrefix, deactivated);
	}
}

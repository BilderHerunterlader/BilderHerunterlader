package ch.supertomcat.bh.hoster.hosteroptions;

import ch.supertomcat.bh.settings.SettingsManager;

/**
 * OverrideDirectoryOption
 */
public class DeactivateOption {
	private final String optionPrefix;

	private boolean deactivated;

	/**
	 * Constructor
	 * 
	 * @param optionPrefix Prefix
	 */
	public DeactivateOption(String optionPrefix) {
		this.optionPrefix = optionPrefix;
		deactivated = SettingsManager.instance().isHostDeactivated(optionPrefix);
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
		SettingsManager.instance().setHostDeactivated(optionPrefix, deactivated);
	}
}

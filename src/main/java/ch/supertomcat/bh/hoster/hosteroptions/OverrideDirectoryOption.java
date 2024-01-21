package ch.supertomcat.bh.hoster.hosteroptions;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.settings.SettingsManager;

/**
 * OverrideDirectoryOption
 */
public class OverrideDirectoryOption {
	private static final Pattern PREFIX_REPLACE_PATTERN = Pattern.compile("[.]+$");

	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	private final String optionPrefix;

	private boolean pathOverride = false;

	private String pathOverrideVal;

	private boolean pathOverrideSubdirsAllowed = true;

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
	public OverrideDirectoryOption(String optionPrefix, SettingsManager settingsManager) {
		optionPrefix = PREFIX_REPLACE_PATTERN.matcher(optionPrefix).replaceAll("");
		this.optionPrefix = optionPrefix;
		this.settingsManager = settingsManager;
		this.pathOverrideVal = settingsManager.getSavePath();

		try {
			pathOverride = settingsManager.getBooleanValue(optionPrefix, "pathOverride");
		} catch (Exception e) {
			try {
				settingsManager.setHosterSettingValue(optionPrefix, "pathOverride", false);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
		try {
			pathOverrideSubdirsAllowed = settingsManager.getBooleanValue(optionPrefix, "pathOverrideSubdirsAllowed");
		} catch (Exception e) {
			try {
				settingsManager.setHosterSettingValue(optionPrefix, "pathOverrideSubdirsAllowed", true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
		try {
			pathOverrideVal = settingsManager.getStringValue(optionPrefix, "pathOverrideVal");
		} catch (Exception e) {
			try {
				settingsManager.setHosterSettingValue(optionPrefix, "pathOverrideVal", pathOverrideVal);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
	}

	/**
	 * Returns the pathOverride
	 * 
	 * @return pathOverride
	 */
	public boolean isPathOverride() {
		return pathOverride;
	}

	/**
	 * Sets the pathOverride
	 * 
	 * @param pathOverride pathOverride
	 */
	public void setPathOverride(boolean pathOverride) {
		this.pathOverride = pathOverride;
	}

	/**
	 * Returns the pathOverrideVal
	 * 
	 * @return pathOverrideVal
	 */
	public String getPathOverrideVal() {
		return pathOverrideVal;
	}

	/**
	 * Sets the pathOverrideVal
	 * 
	 * @param pathOverrideVal pathOverrideVal
	 */
	public void setPathOverrideVal(String pathOverrideVal) {
		this.pathOverrideVal = pathOverrideVal;
	}

	/**
	 * Returns the pathOverrideSubdirsAllowed
	 * 
	 * @return pathOverrideSubdirsAllowed
	 */
	public boolean isPathOverrideSubdirsAllowed() {
		return pathOverrideSubdirsAllowed;
	}

	/**
	 * Sets the pathOverrideSubdirsAllowed
	 * 
	 * @param pathOverrideSubdirsAllowed pathOverrideSubdirsAllowed
	 */
	public void setPathOverrideSubdirsAllowed(boolean pathOverrideSubdirsAllowed) {
		this.pathOverrideSubdirsAllowed = pathOverrideSubdirsAllowed;
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
	 * Save Options
	 */
	public void saveOptions() {
		try {
			settingsManager.setHosterSettingValue(optionPrefix, "pathOverride", this.pathOverride);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}
		try {
			settingsManager.setHosterSettingValue(optionPrefix, "pathOverrideSubdirsAllowed", this.pathOverrideSubdirsAllowed);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}
		try {
			settingsManager.setHosterSettingValue(optionPrefix, "pathOverrideVal", this.pathOverrideVal);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}
	}
}

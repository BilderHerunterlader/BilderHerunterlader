package ch.supertomcat.bh.hoster.hosteroptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.settings.SettingsManager;

/**
 * OverrideDirectoryOption
 */
public class OverrideDirectoryOption {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(OverrideDirectoryOption.class);
	
	private String optionPrefix = "";
	
	private boolean pathOverride = false;
	
	private String pathOverrideVal = SettingsManager.instance().getSavePath();
	
	private boolean pathOverrideSubdirsAllowed = true;
	
	/**
	 * Constructor
	 * @param optionPrefix Prefix
	 */
	public OverrideDirectoryOption(String optionPrefix) {
		optionPrefix = optionPrefix.replaceAll("[.]+$", "");
		this.optionPrefix = optionPrefix;
		
		try {
			pathOverride = SettingsManager.instance().getBooleanValue(optionPrefix + ".pathOverride");
		} catch (Exception e) {
			try {
				SettingsManager.instance().setOptionValue(optionPrefix + ".pathOverride", false);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
		try {
			pathOverrideSubdirsAllowed = SettingsManager.instance().getBooleanValue(optionPrefix + ".pathOverrideSubdirsAllowed");
		} catch (Exception e) {
			try {
				SettingsManager.instance().setOptionValue(optionPrefix + ".pathOverrideSubdirsAllowed", true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
		try {
			pathOverrideVal = SettingsManager.instance().getStringValue(optionPrefix + ".pathOverrideVal");
		} catch (Exception e) {
			try {
				SettingsManager.instance().setOptionValue(optionPrefix + ".pathOverrideVal", pathOverrideVal);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
	}

	/**
	 * Returns the pathOverride
	 * @return pathOverride
	 */
	public boolean isPathOverride() {
		return pathOverride;
	}

	/**
	 * Sets the pathOverride
	 * @param pathOverride pathOverride
	 */
	public void setPathOverride(boolean pathOverride) {
		this.pathOverride = pathOverride;
	}

	/**
	 * Returns the pathOverrideVal
	 * @return pathOverrideVal
	 */
	public String getPathOverrideVal() {
		return pathOverrideVal;
	}

	/**
	 * Sets the pathOverrideVal
	 * @param pathOverrideVal pathOverrideVal
	 */
	public void setPathOverrideVal(String pathOverrideVal) {
		this.pathOverrideVal = pathOverrideVal;
	}

	/**
	 * Returns the pathOverrideSubdirsAllowed
	 * @return pathOverrideSubdirsAllowed
	 */
	public boolean isPathOverrideSubdirsAllowed() {
		return pathOverrideSubdirsAllowed;
	}

	/**
	 * Sets the pathOverrideSubdirsAllowed
	 * @param pathOverrideSubdirsAllowed pathOverrideSubdirsAllowed
	 */
	public void setPathOverrideSubdirsAllowed(boolean pathOverrideSubdirsAllowed) {
		this.pathOverrideSubdirsAllowed = pathOverrideSubdirsAllowed;
	}
	
	/**
	 * Returns the optionPrefix
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
			SettingsManager.instance().setOptionValue(optionPrefix + ".pathOverride", this.pathOverride);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}
		try {
			SettingsManager.instance().setOptionValue(optionPrefix + ".pathOverrideSubdirsAllowed", this.pathOverrideSubdirsAllowed);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}
		try {
			SettingsManager.instance().setOptionValue(optionPrefix + ".pathOverrideVal", this.pathOverrideVal);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}
	}
}

package ch.supertomcat.bh.settings;

import ch.supertomcat.supertomcatutils.settings.SettingsListener;

/**
 * Interface of BH SettingsManager
 */
public interface BHSettingsListener extends SettingsListener {
	/**
	 * Look and Feel changed
	 * 
	 * @param lookAndFeel Look And Feel
	 */
	public void lookAndFeelChanged(int lookAndFeel);
}

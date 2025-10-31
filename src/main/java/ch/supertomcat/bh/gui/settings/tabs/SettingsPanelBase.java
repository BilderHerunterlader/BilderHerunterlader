package ch.supertomcat.bh.gui.settings.tabs;

import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;

/**
 * Settings Panel Base Class
 */
public abstract class SettingsPanelBase extends JPanel {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Logger
	 */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * GridBagLayout
	 */
	protected final GridBagLayout gbl = new GridBagLayout();

	/**
	 * GridBagLayoutUtil
	 */
	protected final GridBagLayoutUtil gblt = new GridBagLayoutUtil(5, 10, 5, 5);

	/**
	 * Settings Manager
	 */
	protected final SettingsManager settingsManager;

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 */
	public SettingsPanelBase(SettingsManager settingsManager) {
		this.settingsManager = settingsManager;
		setLayout(gbl);
	}

	/**
	 * Initialize
	 */
	public abstract void init();

	/**
	 * Apply Settings
	 */
	public abstract void applySettings();
}

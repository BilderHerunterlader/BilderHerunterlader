package ch.supertomcat.bh.gui.settings.tabs;

import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.xml.GUISettings;
import ch.supertomcat.bh.settings.xml.HostsSettings;
import ch.supertomcat.bh.settings.xml.LogLevelSetting;
import ch.supertomcat.bh.settings.xml.Settings;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;

/**
 * Other Settings Panel
 */
public class OtherSettingsPanel extends SettingsPanelBase {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Host Manager
	 */
	private final HostManager hostManager;

	/**
	 * Label
	 */
	private JLabel lblUpdates = new JLabel(Localization.getString("Updates"));

	/**
	 * Checkbox
	 */
	private JCheckBox chkUpdates = new JCheckBox(Localization.getString("CheckForUpdates"), false);

	/**
	 * Label
	 */
	private JLabel lblCheckClipboard = new JLabel(Localization.getString("Clipboard"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkCheckClipboard = new JCheckBox(Localization.getString("CheckClipboard"), false);

	/**
	 * Label
	 */
	private JLabel lblWebExtensionPort = new JLabel(Localization.getString("WebExtensionPort"));

	/**
	 * TextFields
	 */
	private JTextField txtWebExtensionPort = new JTextField("35990", 20);

	/**
	 * Label
	 */
	private JLabel lblAlwaysAddTitle = new JLabel(Localization.getString("TargetFolder"));

	/**
	 * Checkbox
	 */
	private JCheckBox chkAlwaysAddTitle = new JCheckBox(Localization.getString("AlwaysAddTitle"), false);

	/**
	 * Label
	 */
	private JLabel lblRulesBefore = new JLabel(Localization.getString("Rules"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkRulesBefore = new JCheckBox(Localization.getString("RulesBeforeClasses"), false);

	/**
	 * Label
	 */
	private JLabel lblBackupDB = new JLabel(Localization.getString("Backup"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkBackupDB = new JCheckBox(Localization.getString("BackupDB"), false);

	/**
	 * Label
	 */
	private JLabel lblDefragDB = new JLabel(Localization.getString("Defrag"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkDefragDB = new JCheckBox(Localization.getString("DefragDB"), false);

	/**
	 * Label
	 */
	private JLabel lblDefragMinFilesize = new JLabel(Localization.getString("DefragMinFilesize"));

	/**
	 * TextField
	 */
	private JTextField txtDefragMinFilesize = new JTextField("5000000");

	/**
	 * Label
	 */
	private JLabel lblLogLevel = new JLabel("Log-Level");

	/**
	 * ComboBox
	 */
	private JComboBox<LogLevelSetting> cmbLogLevel = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblThreadCount = new JLabel(Localization.getString("ThreadCount"));

	/**
	 * TextField
	 */
	private JTextField txtThreadCount = new JTextField("", 3);

	/**
	 * Slider
	 */
	private JSlider sldThreadCount = new JSlider();

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 * @param hostManager Host Manager
	 */
	public OtherSettingsPanel(SettingsManager settingsManager, HostManager hostManager) {
		super(settingsManager);
		this.hostManager = hostManager;

		txtThreadCount.setEditable(false);

		sldThreadCount.setSnapToTicks(true);
		sldThreadCount.setMajorTickSpacing(1);
		sldThreadCount.setMinorTickSpacing(1);
		sldThreadCount.setPaintTicks(true);
		sldThreadCount.setPaintLabels(true);
		sldThreadCount.setMinimum(1);
		sldThreadCount.setMaximum(Runtime.getRuntime().availableProcessors());
		sldThreadCount.addChangeListener(e -> txtThreadCount.setText(String.valueOf(sldThreadCount.getValue())));

		cmbLogLevel.addItem(LogLevelSetting.TRACE);
		cmbLogLevel.addItem(LogLevelSetting.DEBUG);
		cmbLogLevel.addItem(LogLevelSetting.INFO);
		cmbLogLevel.addItem(LogLevelSetting.WARN);
		cmbLogLevel.addItem(LogLevelSetting.ERROR);
		cmbLogLevel.addItem(LogLevelSetting.FATAL);
		cmbLogLevel.setSelectedItem(LogLevelSetting.INFO);

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtDefragMinFilesize);

		GridBagConstraints gbc = new GridBagConstraints();

		int i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblUpdates, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkUpdates, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblCheckClipboard, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkCheckClipboard, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblWebExtensionPort, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtWebExtensionPort, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblAlwaysAddTitle, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkAlwaysAddTitle, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblRulesBefore, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkRulesBefore, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblBackupDB, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkBackupDB, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblDefragDB, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkDefragDB, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblDefragMinFilesize, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtDefragMinFilesize, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblLogLevel, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cmbLogLevel, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblThreadCount, this);
		gbc = gblt.getGBC(1, i, 1, 1, 1.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, sldThreadCount, this);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtThreadCount, this);
	}

	@Override
	public void init() {
		Settings settings = settingsManager.getSettings();

		GUISettings guiSettings = settings.getGuiSettings();
		chkAlwaysAddTitle.setSelected(guiSettings.isAlwaysAddTitle());

		chkUpdates.setSelected(settings.isCheckForUpdatesOnStart());

		chkCheckClipboard.setSelected(settings.isCheckClipboard());
		txtWebExtensionPort.setText(String.valueOf(settings.getWebExtensionPort()));

		chkDefragDB.setSelected(settings.isDefragDBOnStart());
		chkBackupDB.setSelected(settings.isBackupDbOnStart());
		txtDefragMinFilesize.setText(String.valueOf(settings.getDefragMinFilesize()));

		txtThreadCount.setText(String.valueOf(settings.getThreadCount()));
		sldThreadCount.setValue(settings.getThreadCount());

		cmbLogLevel.setSelectedItem(settings.getLogLevel());

		HostsSettings hostsSettings = settings.getHostsSettings();

		chkRulesBefore.setSelected(hostsSettings.isRulesBeforeClasses());
	}

	@Override
	public void applySettings() {
		Settings settings = settingsManager.getSettings();

		GUISettings guiSettings = settings.getGuiSettings();

		guiSettings.setAlwaysAddTitle(chkAlwaysAddTitle.isSelected());

		settingsManager.applyRegexReplacePipelinePageTitleToXMLSettings();

		settings.setCheckForUpdatesOnStart(chkUpdates.isSelected());

		settings.setCheckClipboard(chkCheckClipboard.isSelected());
		try {
			int val = Integer.parseInt(txtWebExtensionPort.getText());
			if (val < 0 || val > 65535) {
				txtWebExtensionPort.setText(String.valueOf(settings.getWebExtensionPort()));
			} else {
				settings.setWebExtensionPort(val);
			}
		} catch (NumberFormatException nfe) {
			logger.error("WebExtensionPort is not an integer: {}", txtWebExtensionPort.getText(), nfe);
			txtWebExtensionPort.setText(String.valueOf(settings.getWebExtensionPort()));
		}

		settings.setDefragDBOnStart(chkDefragDB.isSelected());
		settings.setBackupDbOnStart(chkBackupDB.isSelected());
		try {
			settings.setDefragMinFilesize(Integer.parseInt(txtDefragMinFilesize.getText()));
		} catch (NumberFormatException nfe) {
			logger.error(nfe.getMessage(), nfe);
			txtDefragMinFilesize.setText(String.valueOf(settings.getDefragMinFilesize()));
		}

		settings.setThreadCount(sldThreadCount.getValue());

		settingsManager.setLogLevel((LogLevelSetting)cmbLogLevel.getSelectedItem());

		HostsSettings hostsSettings = settings.getHostsSettings();

		hostsSettings.setRulesBeforeClasses(chkRulesBefore.isSelected());
		hostManager.reInitHosterList();
	}
}

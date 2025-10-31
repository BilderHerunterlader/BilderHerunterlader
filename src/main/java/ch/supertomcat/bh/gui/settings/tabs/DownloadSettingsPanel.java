package ch.supertomcat.bh.gui.settings.tabs;

import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.supertomcat.bh.gui.settings.AllowedFilenameCharactersComboBoxRenderer;
import ch.supertomcat.bh.gui.settings.RegexReplacePanel;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.xml.AllowedFilenameCharacters;
import ch.supertomcat.bh.settings.xml.DownloadSettings;
import ch.supertomcat.bh.settings.xml.Settings;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;

/**
 * Download Settings Panel
 */
public class DownloadSettingsPanel extends SettingsPanelBase {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Label
	 */
	private JLabel lblSaveLogs = new JLabel(Localization.getString("Logs"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkSaveLogs = new JCheckBox(Localization.getString("SaveLogs"), false);

	/**
	 * Label
	 */
	private JLabel lblAutoStartDownloads = new JLabel(Localization.getString("Downloads"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkAutoStartDownloads = new JCheckBox(Localization.getString("AutoStartDownloads"), false);

	/**
	 * Label
	 */
	private JLabel lblAutoRetryAfterDownloadsComplete = new JLabel(Localization.getString("Downloads"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkAutoRetryAfterDownloadsComplete = new JCheckBox(Localization.getString("AutoRetryAfterDownloadsComplete"), false);

	/**
	 * Label
	 */
	private JLabel lblAllowedFilenameChars = new JLabel(Localization.getString("Filename"));

	/**
	 * ComboBox
	 */
	private JComboBox<AllowedFilenameCharacters> cmbAllowedFilenameChars = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblReduceFilenameLength = new JLabel(Localization.getString("Filename"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkReduceFilenameLength = new JCheckBox(Localization.getString("ReduceFilenameLength"), true);

	/**
	 * Label
	 */
	private JLabel lblReducePathLength = new JLabel(Localization.getString("Folder"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkReducePathLength = new JCheckBox(Localization.getString("ReducePathLength"), true);

	/**
	 * Panel
	 */
	private JPanel pnlMaxFailedCount = new JPanel();

	/**
	 * Label
	 */
	private JLabel lblMaxFailedCount = new JLabel(Localization.getString("Downloads"));

	/**
	 * TextField
	 */
	private JTextField txtMaxFailedCount = new JTextField("");

	/**
	 * Button
	 */
	private JButton btnMaxFailedCountPlus = new JButton("", Icons.getTangoSVGIcon("actions/list-add.svg", 16));

	/**
	 * Button
	 */
	private JButton btnMaxFailedCountMinus = new JButton("", Icons.getTangoSVGIcon("actions/list-remove.svg", 16));

	/**
	 * Label
	 */
	private JLabel lblMinFilesize = new JLabel(Localization.getString("MinFilesize"));

	/**
	 * TextField
	 */
	private JTextField txtMinFilesize = new JTextField("0");

	/**
	 * CheckBox
	 */
	private final RegexReplacePanel pnlRegexReplaceFilename;

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 */
	public DownloadSettingsPanel(SettingsManager settingsManager) {
		super(settingsManager);

		this.pnlRegexReplaceFilename = new RegexReplacePanel(settingsManager.getRegexReplacePipelineFilename(), settingsManager);
		pnlRegexReplaceFilename.setBorder(BorderFactory.createTitledBorder(Localization.getString("RegexReplaceFilename")));

		btnMaxFailedCountPlus.addActionListener(e -> maxFailedPlus());
		btnMaxFailedCountMinus.addActionListener(e -> maxFailedMinus());
		txtMaxFailedCount.setEditable(false);
		txtMaxFailedCount.setColumns(5);
		pnlMaxFailedCount.add(new JLabel(Localization.getString("MaxFailedCountT1")));
		pnlMaxFailedCount.add(txtMaxFailedCount);
		pnlMaxFailedCount.add(btnMaxFailedCountPlus);
		pnlMaxFailedCount.add(btnMaxFailedCountMinus);
		pnlMaxFailedCount.add(new JLabel(Localization.getString("MaxFailedCountT2")));
		pnlMaxFailedCount.setToolTipText(Localization.getString("MaxFailedCountToolTip"));

		txtMinFilesize.setToolTipText(Localization.getString("MinFilesizeToolTip"));

		cmbAllowedFilenameChars.addItem(AllowedFilenameCharacters.ASCII_ONLY);
		cmbAllowedFilenameChars.addItem(AllowedFilenameCharacters.ASCII_UMLAUT);
		cmbAllowedFilenameChars.addItem(AllowedFilenameCharacters.ALL);
		cmbAllowedFilenameChars.setRenderer(new AllowedFilenameCharactersComboBoxRenderer());

		chkSaveLogs.setToolTipText(Localization.getString("SaveLogsTooltip"));

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtMaxFailedCount);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtMinFilesize);

		GridBagConstraints gbc = new GridBagConstraints();

		int i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblSaveLogs, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkSaveLogs, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblAutoStartDownloads, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkAutoStartDownloads, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblAutoRetryAfterDownloadsComplete, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkAutoRetryAfterDownloadsComplete, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblAllowedFilenameChars, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cmbAllowedFilenameChars, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblReduceFilenameLength, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkReduceFilenameLength, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblReducePathLength, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkReducePathLength, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblMaxFailedCount, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, pnlMaxFailedCount, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblMinFilesize, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtMinFilesize, this);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, pnlRegexReplaceFilename, this);
	}

	@Override
	public void init() {
		Settings settings = settingsManager.getSettings();

		DownloadSettings downloadSettings = settings.getDownloadSettings();

		chkAutoStartDownloads.setSelected(downloadSettings.isAutoStartDownloads());
		chkSaveLogs.setSelected(downloadSettings.isSaveLogs());
		txtMaxFailedCount.setText(String.valueOf(downloadSettings.getMaxFailedCount()));
		txtMinFilesize.setText(String.valueOf(downloadSettings.getMinFileSize()));
		chkAutoRetryAfterDownloadsComplete.setSelected(downloadSettings.isAutoRetryAfterDownloadsComplete());
		cmbAllowedFilenameChars.setSelectedItem(downloadSettings.getAllowedFilenameCharacters());
		chkReduceFilenameLength.setSelected(downloadSettings.isReduceFilenameLength());
		chkReducePathLength.setSelected(downloadSettings.isReducePathLength());
	}

	@Override
	public void applySettings() {
		Settings settings = settingsManager.getSettings();

		DownloadSettings downloadSettings = settings.getDownloadSettings();

		downloadSettings.setAutoStartDownloads(chkAutoStartDownloads.isSelected());
		downloadSettings.setSaveLogs(chkSaveLogs.isSelected());
		downloadSettings.setMaxFailedCount(Integer.parseInt(txtMaxFailedCount.getText()));
		try {
			downloadSettings.setMinFileSize(Integer.parseInt(txtMinFilesize.getText()));
		} catch (NumberFormatException nfe) {
			logger.error(nfe.getMessage(), nfe);
			txtMinFilesize.setText(String.valueOf(downloadSettings.getMinFileSize()));
		}
		downloadSettings.setAutoRetryAfterDownloadsComplete(chkAutoRetryAfterDownloadsComplete.isSelected());
		downloadSettings.setAllowedFilenameCharacters((AllowedFilenameCharacters)cmbAllowedFilenameChars.getSelectedItem());
		downloadSettings.setReduceFilenameLength(chkReduceFilenameLength.isSelected());
		downloadSettings.setReducePathLength(chkReducePathLength.isSelected());

		settingsManager.applyRegexReplacePipelineFilenameToXMLSettings();
	}

	private void maxFailedPlus() {
		int val = Integer.parseInt(txtMaxFailedCount.getText());
		if (val < 20) {
			val += 1;
			txtMaxFailedCount.setText(String.valueOf(val));
		}
	}

	private void maxFailedMinus() {
		int val = Integer.parseInt(txtMaxFailedCount.getText());
		if (val > 0) {
			val -= 1;
			txtMaxFailedCount.setText(String.valueOf(val));
		}
	}
}

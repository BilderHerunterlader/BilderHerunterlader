package ch.supertomcat.bh.gui.settings.tabs;

import java.awt.GridBagConstraints;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ch.supertomcat.bh.gui.renderer.LookAndFeelComboBoxRenderer;
import ch.supertomcat.bh.gui.settings.ProgressDisplayModeComboBoxRenderer;
import ch.supertomcat.bh.gui.settings.RegexReplacePanel;
import ch.supertomcat.bh.gui.settings.SizeDisplayModeComboBoxRenderer;
import ch.supertomcat.bh.settings.MappedLookAndFeelSetting;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.xml.GUISettings;
import ch.supertomcat.bh.settings.xml.LookAndFeelSetting;
import ch.supertomcat.bh.settings.xml.ProgressDisplayMode;
import ch.supertomcat.bh.settings.xml.Settings;
import ch.supertomcat.bh.settings.xml.SizeDisplayMode;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;

/**
 * GUI Settings Panel
 */
public class GUISettingsPanel extends SettingsPanelBase {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Owner
	 */
	private final Window owner;

	/**
	 * Label
	 */
	private JLabel lblLAF = new JLabel(Localization.getString("LookAndFeel"));

	/**
	 * ComboBox
	 */
	private JComboBox<MappedLookAndFeelSetting> cmbLAF = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblLanguage = new JLabel(Localization.getString("Language"));

	/**
	 * ComboBox
	 */
	private JComboBox<String> cmbLanguage = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblSizeView = new JLabel(Localization.getString("SizeView"));

	/**
	 * ComboBox
	 */
	private JComboBox<SizeDisplayMode> cmbSizeView = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblProgressView = new JLabel(Localization.getString("ProgressView"));

	/**
	 * ComboBox
	 */
	private JComboBox<ProgressDisplayMode> cmbProgressView = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblDownloadRate = new JLabel(Localization.getString("ProgressView"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkDownloadRate = new JCheckBox(Localization.getString("DownloadRate"));

	/**
	 * Label
	 */
	private JLabel lblWindowSizePos = new JLabel(Localization.getString("MainWindow"));

	/**
	 * Checkbox
	 */
	private JCheckBox chkWindowSizePos = new JCheckBox(Localization.getString("WindowSizePos"), false);

	/**
	 * Label
	 */
	private JLabel lblDownloadSelectionWindowSizePos = new JLabel(Localization.getString("DownloadSelectionWindow"));

	/**
	 * Checkbox
	 */
	private JCheckBox chkDownloadSelectionWindowSizePos = new JCheckBox(Localization.getString("WindowSizePos"), false);

	/**
	 * Label
	 */
	private JLabel lblSaveTableColumnSizes = new JLabel(Localization.getString("Tables"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkSaveTableColumnSizes = new JCheckBox(Localization.getString("SaveTableColumnSizes"), false);

	/**
	 * Label
	 */
	private JLabel lblSaveTableSortOrders = new JLabel(Localization.getString("Tables"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkSaveTableSortOrders = new JCheckBox(Localization.getString("SaveTableSortOrders"), false);

	/**
	 * Label
	 */
	private JLabel lblDownloadsCompleteNotification = new JLabel(Localization.getString("Notification"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkDownloadsCompleteNotification = new JCheckBox(Localization.getString("DownloadsCompleteNotification"), false);

	/**
	 * Label
	 */
	private JLabel lblDownloadPreviews = new JLabel(Localization.getString("Preview"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkDownloadPreviews = new JCheckBox(Localization.getString("DownloadPreviews"), false);

	/**
	 * Label
	 */
	private JLabel lblPreviewSize = new JLabel(Localization.getString("PreviewSize"));

	/**
	 * TextField
	 */
	private JTextField txtPreviewSize = new JTextField("100", 4);

	/**
	 * CheckBox
	 */
	private final RegexReplacePanel pnlRegexReplacePageTitle;

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 * @param owner Owner
	 */
	public GUISettingsPanel(SettingsManager settingsManager, Window owner) {
		super(settingsManager);
		this.owner = owner;

		this.pnlRegexReplacePageTitle = new RegexReplacePanel(settingsManager.getRegexReplacePipelinePageTitle(), settingsManager);
		pnlRegexReplacePageTitle.setBorder(BorderFactory.createTitledBorder(Localization.getString("RegexReplacePageTitle")));

		cmbSizeView.addItem(SizeDisplayMode.AUTO_CHANGE_SIZE);
		cmbSizeView.addItem(SizeDisplayMode.ONLY_B);
		cmbSizeView.addItem(SizeDisplayMode.ONLY_KIB);
		cmbSizeView.addItem(SizeDisplayMode.ONLY_MIB);
		cmbSizeView.addItem(SizeDisplayMode.ONLY_GIB);
		cmbSizeView.addItem(SizeDisplayMode.ONLY_TIB);
		cmbSizeView.setRenderer(new SizeDisplayModeComboBoxRenderer());

		cmbProgressView.addItem(ProgressDisplayMode.PROGRESSBAR_PERCENT);
		cmbProgressView.addItem(ProgressDisplayMode.PROGRESSBAR_SIZE);
		cmbProgressView.setRenderer(new ProgressDisplayModeComboBoxRenderer());

		cmbLanguage.addItem(Localization.getString("German"));
		cmbLanguage.addItem(Localization.getString("English"));
		cmbLanguage.setToolTipText(Localization.getString("LanguageTooltip"));

		for (MappedLookAndFeelSetting lookAndFeel : MappedLookAndFeelSetting.values()) {
			if (lookAndFeel.isAvailable()) {
				cmbLAF.addItem(lookAndFeel);
			}
		}
		cmbLAF.setRenderer(new LookAndFeelComboBoxRenderer());

		GridBagConstraints gbc = new GridBagConstraints();

		int i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblLAF, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cmbLAF, this);

		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblLanguage, this);
		gbc = gblt.getGBC(3, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cmbLanguage, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblSizeView, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cmbSizeView, this);

		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblProgressView, this);
		gbc = gblt.getGBC(3, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cmbProgressView, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblDownloadRate, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkDownloadRate, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblWindowSizePos, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkWindowSizePos, this);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblDownloadSelectionWindowSizePos, this);
		gbc = gblt.getGBC(3, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkDownloadSelectionWindowSizePos, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblSaveTableColumnSizes, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkSaveTableColumnSizes, this);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblSaveTableSortOrders, this);
		gbc = gblt.getGBC(3, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkSaveTableSortOrders, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblDownloadsCompleteNotification, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkDownloadsCompleteNotification, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblDownloadPreviews, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkDownloadPreviews, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblPreviewSize, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtPreviewSize, this);
		i++;
		gbc = gblt.getGBC(0, i, 4, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, pnlRegexReplacePageTitle, this);
	}

	@Override
	public void init() {
		Settings settings = settingsManager.getSettings();

		GUISettings guiSettings = settings.getGuiSettings();

		String lang = guiSettings.getLanguage();
		if (lang.equals("de_DE")) {
			cmbLanguage.setSelectedIndex(0);
		} else {
			cmbLanguage.setSelectedIndex(1);
		}

		chkSaveTableColumnSizes.setSelected(guiSettings.isSaveTableColumnSizes());
		chkSaveTableSortOrders.setSelected(guiSettings.isSaveTableSortOrders());

		cmbSizeView.setSelectedItem(guiSettings.getSizeDisplayMode());
		cmbProgressView.setSelectedItem(guiSettings.getProgressDisplayMode());
		chkDownloadRate.setSelected(guiSettings.isDownloadRate());
		cmbLAF.setSelectedItem(MappedLookAndFeelSetting.getByXMLValue(guiSettings.getLookAndFeel()));

		chkDownloadsCompleteNotification.setSelected(guiSettings.isDownloadsCompleteNotification());

		chkDownloadPreviews.setSelected(guiSettings.isDownloadPreviews());
		txtPreviewSize.setText(String.valueOf(guiSettings.getPreviewSize()));

		chkWindowSizePos.setSelected(guiSettings.getMainWindow().isSave());
		chkDownloadSelectionWindowSizePos.setSelected(guiSettings.getDownloadSelectionWindow().isSave());
	}

	@Override
	public void applySettings() {
		Settings settings = settingsManager.getSettings();

		GUISettings guiSettings = settings.getGuiSettings();

		if (cmbLanguage.getSelectedIndex() == 0) {
			guiSettings.setLanguage("de_DE");
		} else {
			guiSettings.setLanguage("en_EN");
		}

		guiSettings.setSaveTableColumnSizes(chkSaveTableColumnSizes.isSelected());
		guiSettings.setSaveTableSortOrders(chkSaveTableSortOrders.isSelected());

		guiSettings.setSizeDisplayMode((SizeDisplayMode)cmbSizeView.getSelectedItem());
		guiSettings.setProgressDisplayMode((ProgressDisplayMode)cmbProgressView.getSelectedItem());
		guiSettings.setDownloadRate(chkDownloadRate.isSelected());

		MappedLookAndFeelSetting selectedMappedLookAndFeel = (MappedLookAndFeelSetting)cmbLAF.getSelectedItem();
		LookAndFeelSetting selectedLookAndFeel = selectedMappedLookAndFeel.getXMLValue();
		boolean lookAndFeelChanged = settingsManager.setLookAndFeel(selectedLookAndFeel);

		guiSettings.setDownloadsCompleteNotification(chkDownloadsCompleteNotification.isSelected());

		guiSettings.setDownloadPreviews(chkDownloadPreviews.isSelected());
		try {
			int val = Integer.parseInt(txtPreviewSize.getText());
			if (val < 100 || val > 1000) {
				txtPreviewSize.setText(String.valueOf(guiSettings.getPreviewSize()));
			} else {
				guiSettings.setPreviewSize(val);
			}
		} catch (NumberFormatException nfe) {
			logger.error(nfe.getMessage(), nfe);
			txtPreviewSize.setText(String.valueOf(guiSettings.getPreviewSize()));
		}

		guiSettings.getMainWindow().setSave(chkWindowSizePos.isSelected());
		guiSettings.getDownloadSelectionWindow().setSave(chkDownloadSelectionWindowSizePos.isSelected());

		if (lookAndFeelChanged) {
			SwingUtilities.updateComponentTreeUI(owner);
		}
	}
}

package ch.supertomcat.bh.gui.settings.tabs;

import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import ch.supertomcat.bh.gui.settings.KeywordMatchModeComboBoxRenderer;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.xml.KeywordMatchMode;
import ch.supertomcat.bh.settings.xml.KeywordsSettings;
import ch.supertomcat.bh.settings.xml.Settings;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;

/**
 * Keywords Settings Panel
 */
public class KeywordsSettingsPanel extends SettingsPanelBase {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Label
	 */
	private JLabel lblKeywordMatchMode = new JLabel(Localization.getString("KeywordSearch"));

	/**
	 * ComboBox
	 */
	private JComboBox<KeywordMatchMode> cmbKeywordMatchMode = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblDeselectNoKeyword = new JLabel(Localization.getString("KeywordSearch"));

	/**
	 * Checkbox
	 */
	private JCheckBox chkDeselectNoKeyword = new JCheckBox(Localization.getString("DeselectNoKeyword"), false);

	/**
	 * Label
	 */
	private JLabel lblDisplayKeywordsWhenNoMatches = new JLabel(Localization.getString("KeywordSearch"));

	/**
	 * Checkbox
	 */
	private JCheckBox chkDisplayKeywordsWhenNoMatches = new JCheckBox(Localization.getString("DisplayKeywordsWhenNoMatches"), true);

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 */
	public KeywordsSettingsPanel(SettingsManager settingsManager) {
		super(settingsManager);

		cmbKeywordMatchMode.addItem(KeywordMatchMode.MATCH_ONLY_EXACT);
		cmbKeywordMatchMode.addItem(KeywordMatchMode.MATCH_ALL_STRICT);
		cmbKeywordMatchMode.addItem(KeywordMatchMode.MATCH_ALL);
		cmbKeywordMatchMode.setRenderer(new KeywordMatchModeComboBoxRenderer());
		cmbKeywordMatchMode.setToolTipText(Localization.getString("KeywordSearchTooltip"));

		chkDeselectNoKeyword.setToolTipText(Localization.getString("DeselectNoKeywordTooltip"));

		GridBagConstraints gbc = new GridBagConstraints();

		int i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblKeywordMatchMode, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cmbKeywordMatchMode, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblDeselectNoKeyword, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkDeselectNoKeyword, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblDisplayKeywordsWhenNoMatches, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkDisplayKeywordsWhenNoMatches, this);
		i++;
	}

	@Override
	public void init() {
		Settings settings = settingsManager.getSettings();

		KeywordsSettings keywordsSettings = settings.getKeywordsSettings();

		cmbKeywordMatchMode.setSelectedItem(keywordsSettings.getMatchMode());
		chkDisplayKeywordsWhenNoMatches.setSelected(keywordsSettings.isDisplayKeywordsWhenNoMatches());
		chkDeselectNoKeyword.setSelected(keywordsSettings.isDeselectNoKeyword());
	}

	@Override
	public void applySettings() {
		Settings settings = settingsManager.getSettings();

		KeywordsSettings keywordsSettings = settings.getKeywordsSettings();

		keywordsSettings.setMatchMode((KeywordMatchMode)cmbKeywordMatchMode.getSelectedItem());
		keywordsSettings.setDisplayKeywordsWhenNoMatches(chkDisplayKeywordsWhenNoMatches.isSelected());
		keywordsSettings.setDeselectNoKeyword(chkDeselectNoKeyword.isSelected());
	}
}

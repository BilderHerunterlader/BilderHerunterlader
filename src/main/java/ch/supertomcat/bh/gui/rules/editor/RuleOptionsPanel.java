package ch.supertomcat.bh.gui.rules.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorPart;
import ch.supertomcat.bh.rules.xml.DuplicateRemoveMode;
import ch.supertomcat.bh.rules.xml.ReferrerMode;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.combobox.renderer.LocalizedEnumComboBoxRenderer;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;

/**
 * Rule-Referrer-Panel
 */
public class RuleOptionsPanel extends JPanel implements RuleEditorPart {
	private static final long serialVersionUID = 1L;

	/**
	 * Send Cookies CheckBox
	 */
	private JCheckBox chkSendCookies = new JCheckBox(Localization.getString("RulePipelineSendCookies"), true);

	/**
	 * ComboBox
	 */
	private JComboBox<DuplicateRemoveMode> cbDuplicateRemoveMode = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblDuplicateRemoveMode = new JLabel(Localization.getString("DuplicateRemoveMode"));

	/**
	 * ComboBox
	 */
	private JComboBox<ReferrerMode> cbReferrerMode = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblReferrer = new JLabel(Localization.getString("ReferrerContainerPage"));

	/**
	 * Label
	 */
	private JLabel lblCustomReferrer = new JLabel(Localization.getString("CustomReferrer"));

	/**
	 * TextField
	 */
	private JTextField txtCustomReferrer = new JTextField(10);

	/**
	 * ComboBox
	 */
	private JComboBox<ReferrerMode> cbReferrerModeDownload = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblReferrerDownload = new JLabel(Localization.getString("ReferrerDownload"));

	/**
	 * Label
	 */
	private JLabel lblCustomReferrerDownload = new JLabel(Localization.getString("CustomReferrerDownload"));

	/**
	 * TextField
	 */
	private JTextField txtCustomReferrerDownload = new JTextField(10);

	/**
	 * GridBagLayout
	 */
	private GridBagLayout gbl = new GridBagLayout();

	/**
	 * GridBagLayoutUtil
	 */
	private GridBagLayoutUtil gblt = new GridBagLayoutUtil(5, 10, 5, 5);

	/**
	 * Rule
	 */
	private final RuleDefinition rule;

	/**
	 * Constructor
	 * 
	 * @param rule Rule
	 */
	public RuleOptionsPanel(RuleDefinition rule) {
		this.rule = rule;

		txtCustomReferrer.setText(rule.getCustomReferrer());
		txtCustomReferrerDownload.setText(rule.getDownloadCustomReferrer());

		chkSendCookies.setSelected(this.rule.isSendCookies());

		Map<DuplicateRemoveMode, String> duplicateRemoveModeLocalizationStrings = new HashMap<>();
		duplicateRemoveModeLocalizationStrings.put(DuplicateRemoveMode.DEFAULT, "DuplicatesBHDefault");
		duplicateRemoveModeLocalizationStrings.put(DuplicateRemoveMode.CONTAINER_URL_ONLY, "DuplicatesContainerURLOnly");
		duplicateRemoveModeLocalizationStrings.put(DuplicateRemoveMode.CONTAINER_URL_AND_THUMBNAIL_URL, "DuplicatesContainerURLAndThumbnailURL");
		duplicateRemoveModeLocalizationStrings.put(DuplicateRemoveMode.CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_FIRST, "DuplicatesContainerURLOnlyRemoveWithThumbThumbsAlwaysFirst");
		duplicateRemoveModeLocalizationStrings.put(DuplicateRemoveMode.CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_LAST, "DuplicatesContainerURLOnlyRemoveWithThumbThumbsAlwaysLast");
		duplicateRemoveModeLocalizationStrings.put(DuplicateRemoveMode.CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_FIRST, "DuplicatesContainerURLOnlyRemoveWithoutThumbThumbsAlwaysFirst");
		duplicateRemoveModeLocalizationStrings.put(DuplicateRemoveMode.CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_LAST, "DuplicatesContainerURLOnlyRemoveWithoutThumbThumbsAlwaysLast");
		LocalizedEnumComboBoxRenderer<DuplicateRemoveMode> duplicateRemoveModeRenderer = new LocalizedEnumComboBoxRenderer<>(DuplicateRemoveMode.class, duplicateRemoveModeLocalizationStrings);

		cbDuplicateRemoveMode.setRenderer(duplicateRemoveModeRenderer);
		for (DuplicateRemoveMode duplicateRemoveMode : DuplicateRemoveMode.values()) {
			cbDuplicateRemoveMode.addItem(duplicateRemoveMode);
		}
		cbDuplicateRemoveMode.setSelectedItem(rule.getDuplicateRemoveMode());

		Map<ReferrerMode, String> referrerModeLocalizationStrings = new HashMap<>();
		referrerModeLocalizationStrings.put(ReferrerMode.NO_REFERRER, "ReferrerNoReferrer");
		referrerModeLocalizationStrings.put(ReferrerMode.LAST_CONTAINER_URL, "ReferrerLastContainerURL");
		referrerModeLocalizationStrings.put(ReferrerMode.FIRST_CONTAINER_URL, "ReferrerFirstContainerURL");
		referrerModeLocalizationStrings.put(ReferrerMode.ORIGIN_PAGE, "ReferrerOriginPage");
		referrerModeLocalizationStrings.put(ReferrerMode.CUSTOM, "ReferrerCustom");
		LocalizedEnumComboBoxRenderer<ReferrerMode> referrerModeRenderer = new LocalizedEnumComboBoxRenderer<>(ReferrerMode.class, referrerModeLocalizationStrings);

		cbReferrerMode.setRenderer(referrerModeRenderer);
		for (ReferrerMode referrerMode : ReferrerMode.values()) {
			cbReferrerMode.addItem(referrerMode);
		}
		cbReferrerMode.setSelectedItem(rule.getReferrerMode());
		cbReferrerMode.addItemListener(e -> updateCustomerReferrerComponents());

		cbReferrerModeDownload.setRenderer(referrerModeRenderer);
		for (ReferrerMode referrerMode : ReferrerMode.values()) {
			cbReferrerModeDownload.addItem(referrerMode);
		}
		cbReferrerModeDownload.setSelectedItem(rule.getDownloadReferrerMode());
		cbReferrerModeDownload.addItemListener(e -> updateCustomerReferrerComponents());

		updateCustomerReferrerComponents();

		setLayout(gbl);

		GridBagConstraints gbc = new GridBagConstraints();

		int i = 0;
		gbc = gblt.getGBC(0, i, 2, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkSendCookies, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblDuplicateRemoveMode, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cbDuplicateRemoveMode, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblReferrer, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cbReferrerMode, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblCustomReferrer, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtCustomReferrer, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblReferrerDownload, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cbReferrerModeDownload, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblCustomReferrerDownload, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtCustomReferrerDownload, this);
	}

	private void updateCustomerReferrerComponents() {
		if ((ReferrerMode)cbReferrerMode.getSelectedItem() == ReferrerMode.CUSTOM) {
			txtCustomReferrer.setEnabled(true);
		} else {
			txtCustomReferrer.setEnabled(false);
		}

		if ((ReferrerMode)cbReferrerModeDownload.getSelectedItem() == ReferrerMode.CUSTOM) {
			txtCustomReferrerDownload.setEnabled(true);
		} else {
			txtCustomReferrerDownload.setEnabled(false);
		}
	}

	@Override
	public boolean apply() {
		rule.setSendCookies(chkSendCookies.isSelected());
		rule.setReferrerMode((ReferrerMode)cbReferrerMode.getSelectedItem());
		rule.setDownloadReferrerMode((ReferrerMode)cbReferrerModeDownload.getSelectedItem());
		rule.setCustomReferrer(txtCustomReferrer.getText());
		rule.setDownloadCustomReferrer(txtCustomReferrerDownload.getText());
		rule.setDuplicateRemoveMode((DuplicateRemoveMode)cbDuplicateRemoveMode.getSelectedItem());
		return true;
	}

	@Override
	public void redirectEnabled(boolean enabled) {
		chkSendCookies.setEnabled(!enabled);

		cbDuplicateRemoveMode.setEnabled(!enabled);
		lblDuplicateRemoveMode.setEnabled(!enabled);

		cbReferrerModeDownload.setEnabled(!enabled);
		lblReferrerDownload.setEnabled(!enabled);
		lblCustomReferrerDownload.setEnabled(!enabled);
		txtCustomReferrerDownload.setEnabled(!enabled);

		if (!enabled) {
			if ((ReferrerMode)cbReferrerModeDownload.getSelectedItem() == ReferrerMode.CUSTOM) {
				txtCustomReferrerDownload.setEnabled(true);
			} else {
				txtCustomReferrerDownload.setEnabled(false);
			}
		}
	}
}

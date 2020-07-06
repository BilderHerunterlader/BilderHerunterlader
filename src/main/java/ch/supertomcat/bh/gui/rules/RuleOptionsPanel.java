package ch.supertomcat.bh.gui.rules;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.supertomcat.bh.gui.renderer.LocalizedEnumComboBoxRenderer;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.xml.DuplicateRemoveMode;
import ch.supertomcat.bh.rules.xml.ReferrerMode;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;

/**
 * Rule-Referrer-Panel
 */
public class RuleOptionsPanel extends JPanel implements ItemListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Rule
	 */
	private Rule rule = null;

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
	 * Constructor
	 * 
	 * @param rule Rule
	 */
	@SuppressWarnings("unchecked")
	public RuleOptionsPanel(Rule rule) {
		this.rule = rule;

		txtCustomReferrer.setText(rule.getDefinition().getCustomReferrer());
		txtCustomReferrerDownload.setText(rule.getDefinition().getDownloadCustomReferrer());

		chkSendCookies.setSelected(this.rule.getDefinition().isSendCookies());

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
		cbDuplicateRemoveMode.setSelectedItem(rule.getDefinition().getDuplicateRemoveMode());

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
		cbReferrerMode.setSelectedItem(rule.getDefinition().getReferrerMode());
		if ((ReferrerMode)cbReferrerMode.getSelectedItem() == ReferrerMode.CUSTOM) {
			txtCustomReferrer.setEnabled(true);
		} else {
			txtCustomReferrer.setEnabled(false);
		}
		cbReferrerMode.addItemListener(this);

		cbReferrerModeDownload.setRenderer(referrerModeRenderer);
		for (ReferrerMode referrerMode : ReferrerMode.values()) {
			cbReferrerModeDownload.addItem(referrerMode);
		}
		cbReferrerModeDownload.setSelectedItem(rule.getDefinition().getDownloadReferrerMode());
		if ((ReferrerMode)cbReferrerModeDownload.getSelectedItem() == ReferrerMode.CUSTOM) {
			txtCustomReferrerDownload.setEnabled(true);
		} else {
			txtCustomReferrerDownload.setEnabled(false);
		}
		cbReferrerModeDownload.addItemListener(this);

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

	/**
	 * Apply
	 */
	public void applyRuleOptions() {
		rule.getDefinition().setSendCookies(chkSendCookies.isSelected());
		rule.getDefinition().setReferrerMode((ReferrerMode)cbReferrerMode.getSelectedItem());
		rule.getDefinition().setDownloadReferrerMode((ReferrerMode)cbReferrerModeDownload.getSelectedItem());
		rule.getDefinition().setCustomReferrer(txtCustomReferrer.getText());
		rule.getDefinition().setDownloadCustomReferrer(txtCustomReferrerDownload.getText());
		rule.getDefinition().setDuplicateRemoveMode((DuplicateRemoveMode)cbDuplicateRemoveMode.getSelectedItem());
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == cbReferrerMode) {
			if ((ReferrerMode)cbReferrerMode.getSelectedItem() == ReferrerMode.CUSTOM) {
				txtCustomReferrer.setEnabled(true);
			} else {
				txtCustomReferrer.setEnabled(false);
			}
		} else if (e.getSource() == cbReferrerModeDownload) {
			if ((ReferrerMode)cbReferrerModeDownload.getSelectedItem() == ReferrerMode.CUSTOM) {
				txtCustomReferrerDownload.setEnabled(true);
			} else {
				txtCustomReferrerDownload.setEnabled(false);
			}
		}
	}

	/**
	 * @param enabled Enabled
	 */
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

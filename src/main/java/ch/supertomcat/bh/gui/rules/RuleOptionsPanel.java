package ch.supertomcat.bh.gui.rules;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.supertomcat.bh.rules.DuplicateRemoveMode;
import ch.supertomcat.bh.rules.ReferrerMode;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;
import ch.supertomcat.supertomcatutils.gui.Localization;

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
	private JComboBox<String> cbDuplicateRemoveMode = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblDuplicateRemoveMode = new JLabel(Localization.getString("DuplicateRemoveMode"));

	/**
	 * ComboBox
	 */
	private JComboBox<String> cbReferrerMode = new JComboBox<>();

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
	private JComboBox<String> cbReferrerModeDownload = new JComboBox<>();

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
	public RuleOptionsPanel(Rule rule) {
		this.rule = rule;

		txtCustomReferrer.setText(rule.getCustomReferrer());
		txtCustomReferrerDownload.setText(rule.getCustomReferrerDownload());

		chkSendCookies.setSelected(this.rule.isSendCookies());

		cbDuplicateRemoveMode.addItem(Localization.getString("DuplicatesBHDefault"));
		cbDuplicateRemoveMode.addItem(Localization.getString("DuplicatesContainerURLOnly"));
		cbDuplicateRemoveMode.addItem(Localization.getString("DuplicatesContainerURLAndThumbnailURL"));
		cbDuplicateRemoveMode.addItem(Localization.getString("DuplicatesContainerURLOnlyRemoveWithThumbThumbsAlwaysFirst"));
		cbDuplicateRemoveMode.addItem(Localization.getString("DuplicatesContainerURLOnlyRemoveWithThumbThumbsAlwaysLast"));
		cbDuplicateRemoveMode.addItem(Localization.getString("DuplicatesContainerURLOnlyRemoveWithoutThumbThumbsAlwaysFirst"));
		cbDuplicateRemoveMode.addItem(Localization.getString("DuplicatesContainerURLOnlyRemoveWithoutThumbThumbsAlwaysLast"));
		cbDuplicateRemoveMode.setSelectedIndex(rule.getDuplicateRemoveMode().getValue());

		cbReferrerMode.addItem(Localization.getString("ReferrerNoReferrer"));
		cbReferrerMode.addItem(Localization.getString("ReferrerLastContainerURL"));
		cbReferrerMode.addItem(Localization.getString("ReferrerFirstContainerURL"));
		cbReferrerMode.addItem(Localization.getString("ReferrerOriginPage"));
		cbReferrerMode.addItem(Localization.getString("ReferrerCustom"));
		cbReferrerMode.setSelectedIndex(rule.getReferrerMode().getValue());
		if (cbReferrerMode.getSelectedIndex() == ReferrerMode.REFERRER_CUSTOM.getValue()) {
			txtCustomReferrer.setEnabled(true);
		} else {
			txtCustomReferrer.setEnabled(false);
		}
		cbReferrerMode.addItemListener(this);

		cbReferrerModeDownload.addItem(Localization.getString("ReferrerNoReferrer"));
		cbReferrerModeDownload.addItem(Localization.getString("ReferrerLastContainerURL"));
		cbReferrerModeDownload.addItem(Localization.getString("ReferrerFirstContainerURL"));
		cbReferrerModeDownload.addItem(Localization.getString("ReferrerOriginPage"));
		cbReferrerModeDownload.addItem(Localization.getString("ReferrerCustom"));
		cbReferrerModeDownload.setSelectedIndex(rule.getReferrerModeDownload().getValue());
		if (cbReferrerModeDownload.getSelectedIndex() == ReferrerMode.REFERRER_CUSTOM.getValue()) {
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
		rule.setSendCookies(chkSendCookies.isSelected());
		rule.setReferrerMode(ReferrerMode.getByValue(cbReferrerMode.getSelectedIndex()));
		rule.setReferrerModeDownload(ReferrerMode.getByValue(cbReferrerModeDownload.getSelectedIndex()));
		rule.setCustomReferrer(txtCustomReferrer.getText());
		rule.setCustomReferrerDownload(txtCustomReferrerDownload.getText());
		rule.setDuplicateRemoveMode(DuplicateRemoveMode.getByValue(cbDuplicateRemoveMode.getSelectedIndex()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == cbReferrerMode) {
			if (cbReferrerMode.getSelectedIndex() == ReferrerMode.REFERRER_CUSTOM.getValue()) {
				txtCustomReferrer.setEnabled(true);
			} else {
				txtCustomReferrer.setEnabled(false);
			}
		} else if (e.getSource() == cbReferrerModeDownload) {
			if (cbReferrerModeDownload.getSelectedIndex() == ReferrerMode.REFERRER_CUSTOM.getValue()) {
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
			if (cbReferrerModeDownload.getSelectedIndex() == ReferrerMode.REFERRER_CUSTOM.getValue()) {
				txtCustomReferrerDownload.setEnabled(true);
			} else {
				txtCustomReferrerDownload.setEnabled(false);
			}
		}
	}
}

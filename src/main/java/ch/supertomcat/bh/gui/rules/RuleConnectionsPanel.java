package ch.supertomcat.bh.gui.rules;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;

/**
 * Rule-Referrer-Panel
 */
public class RuleConnectionsPanel extends JPanel implements ActionListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Rule
	 */
	private Rule rule = null;

	/**
	 * Label
	 */
	private JLabel lblMaxConnections = new JLabel(Localization.getString("MaxConnectionCount"));

	/**
	 * TextField
	 */
	private JTextField txtMaxConnections = new JTextField("0", 5);

	/**
	 * Label
	 */
	private JLabel lblMaxConnectionsDomains = new JLabel(Localization.getString("MaxConnectionCountDomains"));

	/**
	 * ListModel
	 */
	private DefaultListModel<String> modelMaxConnectionsDomains = new DefaultListModel<>();

	/**
	 * List
	 */
	private JList<String> lstMaxConnectionsDomains = new JList<>(modelMaxConnectionsDomains);

	/**
	 * Button
	 */
	private JButton btnMaxConnectionsDomainsAdd = new JButton(Localization.getString("MaxConnectionCountDomainsAdd"));

	/**
	 * Button
	 */
	private JButton btnMaxConnectionsDomainsRemove = new JButton(Localization.getString("MaxConnectionCountDomainsRemove"));

	/**
	 * TextField
	 */
	private JTextField txtMaxConnectionsDomainsAdd = new JTextField(10);

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
	public RuleConnectionsPanel(Rule rule) {
		this.rule = rule;

		lstMaxConnectionsDomains.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		txtMaxConnections.setToolTipText(Localization.getString("MaxConnectionCountToolTip"));
		btnMaxConnectionsDomainsAdd.addActionListener(this);
		btnMaxConnectionsDomainsRemove.addActionListener(this);
		txtMaxConnections.setText(String.valueOf(rule.getDefinition().getRestriction().getMaxConnections()));
		List<String> domains = rule.getDefinition().getRestriction().getDomain();
		for (int i = 0; i < domains.size(); i++) {
			modelMaxConnectionsDomains.addElement(domains.get(i));
		}

		setLayout(gbl);

		GridBagConstraints gbc = new GridBagConstraints();

		int i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblMaxConnections, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtMaxConnections, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblMaxConnectionsDomains, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 3.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, new JScrollPane(lstMaxConnectionsDomains), this);
		i++;
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, btnMaxConnectionsDomainsRemove, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtMaxConnectionsDomainsAdd, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, btnMaxConnectionsDomainsAdd, this);
	}

	/**
	 * Apply
	 */
	public void applyMaxConnections() {
		try {
			rule.getDefinition().getRestriction().setMaxConnections(Integer.parseInt(txtMaxConnections.getText()));
		} catch (NumberFormatException nfe) {
		}
		List<String> domains = new ArrayList<>();
		for (int i = 0; i < modelMaxConnectionsDomains.size(); i++) {
			domains.add(modelMaxConnectionsDomains.get(i));
		}
		rule.getDefinition().getRestriction().getDomain().clear();
		rule.getDefinition().getRestriction().getDomain().addAll(domains);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnMaxConnectionsDomainsAdd) {
			String domainToAdd = txtMaxConnectionsDomainsAdd.getText();
			if (domainToAdd.length() == 0) {
				return;
			}
			if (modelMaxConnectionsDomains.contains(domainToAdd) == false) {
				modelMaxConnectionsDomains.addElement(domainToAdd);
			}
		} else if (e.getSource() == btnMaxConnectionsDomainsRemove) {
			if (lstMaxConnectionsDomains.getSelectedIndex() > -1) {
				modelMaxConnectionsDomains.remove(lstMaxConnectionsDomains.getSelectedIndex());
			}
		}
	}

	/**
	 * @param enabled Enabled
	 */
	public void redirectEnabled(boolean enabled) {
		txtMaxConnectionsDomainsAdd.setEnabled(!enabled);
		btnMaxConnectionsDomainsRemove.setEnabled(!enabled);
		btnMaxConnectionsDomainsAdd.setEnabled(!enabled);
		lstMaxConnectionsDomains.setEnabled(!enabled);
		lblMaxConnectionsDomains.setEnabled(!enabled);
		txtMaxConnections.setEnabled(!enabled);
		lblMaxConnections.setEnabled(!enabled);
	}
}

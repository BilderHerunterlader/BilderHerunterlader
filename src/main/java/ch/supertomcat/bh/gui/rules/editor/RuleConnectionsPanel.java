package ch.supertomcat.bh.gui.rules.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorPart;
import ch.supertomcat.bh.rules.xml.Restriction;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;

/**
 * Rule-Connection-Panel
 */
public class RuleConnectionsPanel extends JPanel implements RuleEditorPart {
	private static final long serialVersionUID = 1L;

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

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
	 * Rule
	 */
	private final Restriction restriction;

	/**
	 * Constructor
	 * 
	 * @param restriction Restriction
	 */
	public RuleConnectionsPanel(Restriction restriction) {
		this.restriction = restriction;

		lstMaxConnectionsDomains.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		txtMaxConnections.setToolTipText(Localization.getString("MaxConnectionCountToolTip"));
		btnMaxConnectionsDomainsAdd.addActionListener(e -> actionAddDomain());
		btnMaxConnectionsDomainsRemove.addActionListener(e -> actionRemoveDomain());
		txtMaxConnections.setText(String.valueOf(restriction.getMaxConnections()));
		List<String> domains = restriction.getDomain();
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

	private void actionAddDomain() {
		String domainToAdd = txtMaxConnectionsDomainsAdd.getText();
		if (domainToAdd.length() == 0) {
			return;
		}
		if (modelMaxConnectionsDomains.contains(domainToAdd) == false) {
			modelMaxConnectionsDomains.addElement(domainToAdd);
		}
	}

	private void actionRemoveDomain() {
		if (lstMaxConnectionsDomains.getSelectedIndex() > -1) {
			modelMaxConnectionsDomains.remove(lstMaxConnectionsDomains.getSelectedIndex());
		}
	}

	@Override
	public boolean apply() {
		try {
			restriction.setMaxConnections(Integer.parseInt(txtMaxConnections.getText()));
		} catch (NumberFormatException nfe) {
			logger.error("MaxConnections Text is not an integer: {}", txtMaxConnections.getText());
		}
		List<String> domains = new ArrayList<>();
		for (int i = 0; i < modelMaxConnectionsDomains.size(); i++) {
			domains.add(modelMaxConnectionsDomains.get(i));
		}
		restriction.getDomain().clear();
		restriction.getDomain().addAll(domains);
		return true;
	}

	@Override
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

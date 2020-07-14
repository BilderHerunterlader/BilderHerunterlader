package ch.supertomcat.bh.gui.rules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;

/**
 * Rule-Main-Editor
 */
public class RuleMainEditor extends JDialog implements ActionListener, ItemListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Panel
	 */
	private JPanel pnlGeneral = new JPanel();

	/**
	 * TabbedPane
	 */
	private JTabbedPane tabs = new JTabbedPane();

	private RulePipesPanel pnlPipes;

	private RulePipesFailuresPanel pnlPipesFailures;

	/**
	 * RulePipelinePanel
	 */
	private RulePipelineFilenamePanel pnlFilename;

	/**
	 * RulePipelinePanel
	 */
	private RulePipelineFilenameDownloadSelectionPanel pnlFilenameDownloadSelection;

	/**
	 * RuleOptionsPanel
	 */
	private RuleOptionsPanel pnlRuleOptions;

	/**
	 * RuleOptionsPanel
	 */
	private RuleConnectionsPanel pnlConnections;

	/**
	 * Label
	 */
	private JLabel lblName = new JLabel(Localization.getString("Name"));

	/**
	 * TextField
	 */
	private JTextField txtName = new JTextField(20);

	/**
	 * Label
	 */
	private JLabel lblUrlpattern = new JLabel(Localization.getString("URLPattern"));

	/**
	 * TextField
	 */
	private JTextField txtUrlpattern = new JTextField(30);

	/**
	 * Label
	 */
	private JLabel lblError = new JLabel(Localization.getString("Error") + ":");

	/**
	 * TextPane
	 */
	private JTextArea txtError = new JTextArea(4, 40);

	/**
	 * Label
	 */
	private JLabel lblVersion = new JLabel(Localization.getString("Version"));

	/**
	 * TextField
	 */
	private JTextField txtVersion = new JTextField(40);

	private JCheckBox chkDeveloper = new JCheckBox(Localization.getString("Developer"), false);

	/**
	 * CheckBox
	 */
	private JCheckBox chkRedirect = new JCheckBox(Localization.getString("RuleRedirect"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkResend = new JCheckBox(Localization.getString("RuleResend"));

	/**
	 * GridBagLayout
	 */
	private GridBagLayout gbl = new GridBagLayout();

	/**
	 * GridBagLayoutUtil
	 */
	private GridBagLayoutUtil gblt = new GridBagLayoutUtil(5, 10, 5, 5);

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * Button
	 */
	private JButton btnTest = new JButton(Localization.getString("RuleTest"), Icons.getTangoIcon("apps/utilities-system-monitor.png", 16));

	/**
	 * Button
	 */
	private JButton btnApply = new JButton(Localization.getString("Apply"), Icons.getTangoIcon("actions/media-seek-forward.png", 16));

	/**
	 * Button
	 */
	private JButton btnOK = new JButton(Localization.getString("OK"));

	/**
	 * Button
	 */
	private JButton btnCancel = new JButton(Localization.getString("Cancel"));

	/**
	 * Flag if cancel was pressed
	 */
	private boolean canceled = true;

	/**
	 * Rule
	 */
	private Rule rule = null;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param rule Rule
	 * @param settingsManager Settings Manager
	 * @param hostManager Host Manager
	 */
	public RuleMainEditor(JFrame owner, Rule rule, SettingsManager settingsManager, HostManager hostManager) {
		super(owner);
		this.rule = rule;
		this.settingsManager = settingsManager;
		setTitle(Localization.getString("RuleEditor"));
		setLayout(new BorderLayout());
		pnlButtons.add(btnTest);
		pnlButtons.add(btnOK);
		pnlButtons.add(btnApply);
		pnlButtons.add(btnCancel);

		btnCancel.setMnemonic(KeyEvent.VK_C);

		btnTest.addActionListener(this);
		btnOK.addActionListener(this);
		btnApply.addActionListener(this);
		btnCancel.addActionListener(this);

		txtName.setText(rule.getName());
		txtUrlpattern.setText(rule.getDefinition().getUrlPattern());
		txtVersion.setText(rule.getVersion());
		txtError.setEditable(false);
		txtError.setVisible(false);
		lblError.setVisible(false);
		lblError.setBackground(Color.WHITE);
		txtError.setBackground(Color.WHITE);
		lblError.setForeground(Color.RED);
		txtError.setForeground(Color.RED);
		txtError.setFont(new Font("Monospaced", Font.PLAIN, txtName.getFont().getSize()));

		chkResend.setSelected(rule.getDefinition().isResend());

		chkDeveloper.setSelected(rule.isDeveloper());
		if ((rule.getFile().exists()) && (rule.getFile().getAbsoluteFile().length() > 0)) {
			chkDeveloper.setEnabled(false);
		}

		chkDeveloper.setVisible(hostManager.getHostRules().isDeveloperRulesEnabled());

		pnlGeneral.setLayout(gbl);

		GridBagConstraints gbc = new GridBagConstraints();

		int i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblName, pnlGeneral);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtName, pnlGeneral);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblVersion, pnlGeneral);
		gbc = gblt.getGBC(3, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtVersion, pnlGeneral);
		gbc = gblt.getGBC(4, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkDeveloper, pnlGeneral);
		i++;
		gbc = gblt.getGBC(0, i, 5, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblUrlpattern, pnlGeneral);
		i++;
		gbc = gblt.getGBC(0, i, 5, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtUrlpattern, pnlGeneral);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblError, pnlGeneral);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtError, pnlGeneral);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkRedirect, pnlGeneral);
		gbc = gblt.getGBC(2, i, 2, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkResend, pnlGeneral);

		pnlPipes = new RulePipesPanel(this.rule, this, settingsManager);

		pnlPipesFailures = new RulePipesFailuresPanel(this.rule, this, settingsManager);

		pnlFilename = new RulePipelineFilenamePanel(this.rule, this.rule.getPipelineFilename(), this, settingsManager);
		pnlFilenameDownloadSelection = new RulePipelineFilenameDownloadSelectionPanel(this.rule, this.rule.getPipelineFilenameDownloadSelection(), this, settingsManager);

		pnlRuleOptions = new RuleOptionsPanel(this.rule);
		pnlConnections = new RuleConnectionsPanel(this.rule);

		JScrollPane sp = new JScrollPane(pnlGeneral);

		tabs.addTab(Localization.getString("URLContainerPage"), pnlPipes);
		tabs.addTab(Localization.getString("FilenameAfterDownload"), pnlFilename);
		tabs.addTab(Localization.getString("FilenameDownloadSelection"), pnlFilenameDownloadSelection);
		tabs.addTab(Localization.getString("Options"), pnlRuleOptions);
		tabs.addTab(Localization.getString("MaxConnections"), pnlConnections);
		tabs.addTab(Localization.getString("Failures"), pnlPipesFailures);
		add(sp, BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);

		/*
		 * Everything is now setup, so we can set redirect-checkbox-state
		 */
		chkRedirect.addItemListener(this);
		chkRedirect.setSelected(rule.getDefinition().isRedirect());
		chkRedirect.setToolTipText(Localization.getString("RuleRedirectToolTip"));

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtName);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtUrlpattern);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtVersion);

		setModal(true);
		pack();
		setLocationRelativeTo(owner);

		// Enter und Escape (before setVisible(true)!)
		ActionMap am = getRootPane().getActionMap();
		InputMap im = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		Object windowCloseKey = new Object();
		Object windowOkKey = new Object();
		KeyStroke windowCloseStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		Action windowCloseAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				btnCancel.doClick();
			}
		};
		KeyStroke windowOkStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		Action windowOkAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				btnOK.doClick();
			}
		};
		im.put(windowCloseStroke, windowCloseKey);
		am.put(windowCloseKey, windowCloseAction);
		im.put(windowOkStroke, windowOkKey);
		am.put(windowOkKey, windowOkAction);

		setVisible(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnOK) {
			if (apply()) {
				canceled = false;
				this.dispose();
			}
		} else if (e.getSource() == btnCancel) {
			this.dispose();
		} else if (e.getSource() == btnTest) {
			if (apply()) {
				RuleTest dlg = new RuleTest(rule, this);
				dlg.setVisible(true);
			}
		} else if (e.getSource() == btnApply) {
			apply();
		}
	}

	/**
	 * Apply
	 * 
	 * @return True if successfull
	 */
	private boolean apply() {
		try {
			// Check if pattern compiles, before setting it in rule definition
			Pattern.compile(txtUrlpattern.getText());
			rule.getDefinition().setUrlPattern(txtUrlpattern.getText());
			txtError.setText("");
			txtError.setVisible(false);
			lblError.setVisible(false);
		} catch (PatternSyntaxException pse) {
			txtError.setText(pse.getLocalizedMessage());
			txtError.setVisible(true);
			lblError.setVisible(true);
			return false;
		}
		if (txtName.getText().length() == 0) {
			JOptionPane.showMessageDialog(getOwner(), Localization.getString("PleaseChooseAnotherName"), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		String rulename = txtName.getText();
		rulename = BHUtil.filterFilename(rulename, settingsManager);
		String filename = "Rule" + rulename + ".xml";
		String file = ApplicationProperties.getProperty("ApplicationPath") + "rules/" + filename;
		if (chkDeveloper.isSelected()) {
			file = ApplicationProperties.getProperty("ApplicationPath") + "developerrules/" + filename;
		}
		File f = new File(file);
		if ((f.exists()) && (rule.getFile().getAbsoluteFile().length() == 0)) {
			JOptionPane.showMessageDialog(getOwner(), Localization.getString("PleaseChooseAnotherName"), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		f = null;
		if (rule.getFile() == null || rule.getFile().length() == 0) {
			rule.setFile(new File(file));
		}
		rule.getDefinition().setName(txtName.getText());
		rule.getDefinition().setVersion(txtVersion.getText());
		rule.getDefinition().setRedirect(chkRedirect.isSelected());
		rule.getDefinition().setResend(chkResend.isSelected());
		rule.setDeveloper(chkDeveloper.isSelected());
		pnlPipes.apply();
		pnlPipesFailures.apply();
		pnlFilename.apply();
		pnlFilenameDownloadSelection.apply();
		pnlRuleOptions.applyRuleOptions();
		pnlConnections.applyMaxConnections();
		rule.updateFromDefinition();
		return true;
	}

	/**
	 * Get-Method
	 * 
	 * @return True if canceled
	 */
	public boolean getCanceled() {
		return this.canceled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == chkRedirect) {
			boolean enabled = chkRedirect.isSelected();
			chkResend.setEnabled(!enabled);

			pnlFilename.redirectEnabled(enabled);
			pnlFilenameDownloadSelection.redirectEnabled(enabled);
			pnlRuleOptions.redirectEnabled(enabled);
			pnlConnections.redirectEnabled(enabled);
		}
	}
}

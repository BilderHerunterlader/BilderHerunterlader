package ch.supertomcat.bh.gui.rules.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorPart;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;

/**
 * Rule General Panel
 */
public class RuleGeneralPanel extends JPanel implements RuleEditorPart {
	private static final long serialVersionUID = 1L;

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
	 * Owner
	 */
	private JDialog owner;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Redirect Changed Consumer
	 */
	private final Consumer<Boolean> redirectChangedConsumer;

	/**
	 * Rule
	 */
	private final Rule rule;

	/**
	 * Rule Definition
	 */
	private final RuleDefinition definition;

	/**
	 * Constructor
	 * 
	 * @param rule Rule
	 * @param definition Rule Definition
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 * @param developerCheckboxAvailable True if Developer Checkbox is available, false otherwise
	 * @param redirectChangedConsumer
	 */
	public RuleGeneralPanel(Rule rule, RuleDefinition definition, JDialog owner, SettingsManager settingsManager, boolean developerCheckboxAvailable, Consumer<Boolean> redirectChangedConsumer) {
		this.rule = rule;
		this.definition = definition;
		this.owner = owner;
		this.settingsManager = settingsManager;
		this.redirectChangedConsumer = redirectChangedConsumer;
		setLayout(gbl);

		GridBagConstraints gbc = new GridBagConstraints();

		int i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblName, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtName, this);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblVersion, this);
		gbc = gblt.getGBC(3, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtVersion, this);
		gbc = gblt.getGBC(4, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkDeveloper, this);
		i++;
		gbc = gblt.getGBC(0, i, 5, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblUrlpattern, this);
		i++;
		gbc = gblt.getGBC(0, i, 5, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtUrlpattern, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblError, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtError, this);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkRedirect, this);
		gbc = gblt.getGBC(2, i, 2, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkResend, this);

		txtName.setText(definition.getName());
		txtUrlpattern.setText(definition.getUrlPattern());
		txtVersion.setText(definition.getVersion());
		txtError.setEditable(false);
		txtError.setVisible(false);
		lblError.setVisible(false);
		lblError.setBackground(Color.WHITE);
		txtError.setBackground(Color.WHITE);
		lblError.setForeground(Color.RED);
		txtError.setForeground(Color.RED);
		txtError.setFont(new Font("Monospaced", Font.PLAIN, txtName.getFont().getSize()));

		chkRedirect.setSelected(definition.isRedirect());
		chkRedirect.setToolTipText(Localization.getString("RuleRedirectToolTip"));
		chkRedirect.addItemListener(e -> fireRedirectChangedConsumer());

		chkResend.setSelected(definition.isResend());

		chkDeveloper.setSelected(rule.isDeveloper());
		if (rule.isFileExists()) {
			chkDeveloper.setEnabled(false);
		}

		chkDeveloper.setVisible(developerCheckboxAvailable);

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtName);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtUrlpattern);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtVersion);
	}

	@Override
	public boolean apply() {
		try {
			// Check if pattern compiles, before setting it in rule definition
			Pattern.compile(txtUrlpattern.getText());
			definition.setUrlPattern(txtUrlpattern.getText());
			txtError.setText("");
			txtError.setVisible(false);
			lblError.setVisible(false);
		} catch (PatternSyntaxException pse) {
			txtError.setText(pse.getLocalizedMessage());
			txtError.setVisible(true);
			lblError.setVisible(true);
			return false;
		}

		if (txtName.getText().isEmpty()) {
			JOptionPane.showMessageDialog(owner, Localization.getString("PleaseChooseAnotherName"), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (!rule.isFileExists()) {
			String rulename = txtName.getText();
			rulename = BHUtil.filterFilename(rulename, settingsManager);
			String filename = "Rule" + rulename + ".xml";
			String filePath = ApplicationProperties.getProperty("ApplicationPath") + "rules/" + filename;
			if (chkDeveloper.isSelected()) {
				filePath = ApplicationProperties.getProperty("ApplicationPath") + "developerrules/" + filename;
			}
			File file = new File(filePath);
			if (file.exists()) {
				JOptionPane.showMessageDialog(owner, Localization.getString("PleaseChooseAnotherName"), "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}

			rule.setFile(new File(filePath));
		}

		definition.setName(txtName.getText());
		definition.setVersion(txtVersion.getText());
		definition.setRedirect(chkRedirect.isSelected());
		definition.setResend(chkResend.isSelected());
		rule.setDeveloper(chkDeveloper.isSelected());
		return true;
	}

	@Override
	public void redirectEnabled(boolean enabled) {
		chkResend.setEnabled(!enabled);
	}

	/**
	 * Fire Redirect Changed Consumer
	 */
	public void fireRedirectChangedConsumer() {
		redirectChangedConsumer.accept(chkRedirect.isSelected());
	}
}

package ch.supertomcat.bh.gui.rules.editor.urlpipe.varregex;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import ch.supertomcat.bh.gui.rules.editor.urlpipe.regex.RulePipelineURLRegexTablePanel;
import ch.supertomcat.bh.rules.xml.RuleRegex;
import ch.supertomcat.bh.rules.xml.VarRuleRegex;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.layout.SpringUtilities;

/**
 * Var Rule Regex Panel
 */
public class RulePipelineVarRuleRegexPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * Label
	 */
	private JLabel lblVariableName = new JLabel(Localization.getString("VariableName"));

	/**
	 * TextField
	 */
	private JTextField txtVariableName = new JTextField(80);

	/**
	 * Table Panel
	 */
	private RulePipelineURLRegexTablePanel pnlTable;

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 */
	public RulePipelineVarRuleRegexPanel(JDialog owner, SettingsManager settingsManager) {
		this(owner, settingsManager, "", Collections.emptyList());
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 * @param varRuleRegex Var Rule Regex
	 */
	public RulePipelineVarRuleRegexPanel(JDialog owner, SettingsManager settingsManager, VarRuleRegex varRuleRegex) {
		this(owner, settingsManager, varRuleRegex.getVariableName(), varRuleRegex.getRegexp());
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 * @param variableName Variable Name
	 * @param regexps Regex List
	 */
	public RulePipelineVarRuleRegexPanel(JDialog owner, SettingsManager settingsManager, String variableName, List<RuleRegex> regexps) {
		super(new BorderLayout());
		txtVariableName.setText(variableName);

		JPanel pnlTop = new JPanel(new SpringLayout());
		pnlTop.add(lblVariableName);
		pnlTop.add(txtVariableName);
		SpringUtilities.makeCompactGrid(pnlTop, 1, 2, 5, 5, 5, 5);

		pnlTable = new RulePipelineURLRegexTablePanel(owner, settingsManager, regexps);

		add(pnlTop, BorderLayout.NORTH);
		add(pnlTable, BorderLayout.CENTER);

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtVariableName);
	}

	/**
	 * @return Variable Name
	 */
	public String getVariableName() {
		return txtVariableName.getText();
	}

	/**
	 * Apply
	 * 
	 * @param varRuleRegex Var Rule Regex
	 */
	public void apply(VarRuleRegex varRuleRegex) {
		varRuleRegex.setVariableName(txtVariableName.getText());
		pnlTable.apply(varRuleRegex.getRegexp());
	}
}

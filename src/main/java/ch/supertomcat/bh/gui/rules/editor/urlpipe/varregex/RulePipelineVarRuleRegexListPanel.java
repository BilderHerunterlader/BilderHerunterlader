package ch.supertomcat.bh.gui.rules.editor.urlpipe.varregex;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;

import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorListPanel;
import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorPart;
import ch.supertomcat.bh.gui.rules.editor.base.RuleVarRegexpEditor;
import ch.supertomcat.bh.gui.rules.editor.base.VarRuleRegexListCellRenderer;
import ch.supertomcat.bh.rules.xml.URLRegexPipeline;
import ch.supertomcat.bh.rules.xml.VarRuleRegex;
import ch.supertomcat.bh.settings.SettingsManager;

/**
 * Failure Pipes Panel
 */
public class RulePipelineVarRuleRegexListPanel extends RuleEditorListPanel<RulePipelineVarRuleRegexPanel, DefaultListModel<RulePipelineVarRuleRegexPanel>> implements RuleEditorPart {
	private static final long serialVersionUID = 1L;

	/**
	 * Pipe
	 */
	private final URLRegexPipeline pipe;

	/**
	 * Constructor
	 * 
	 * @param pipe Pipe
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 */
	public RulePipelineVarRuleRegexListPanel(URLRegexPipeline pipe, JDialog owner, SettingsManager settingsManager) {
		super(new DefaultListModel<>(), () -> createVarURLRegexTablePanel(owner, settingsManager), x -> editVarURLRegexTablePanel(owner, settingsManager, x), new VarRuleRegexListCellRenderer());
		this.pipe = pipe;

		for (VarRuleRegex varRuleRegex : pipe.getVarRegexp()) {
			RulePipelineVarRuleRegexPanel varRuleRegexPanel = createVarURLRegexTablePanel(owner, settingsManager, varRuleRegex);
			model.addElement(varRuleRegexPanel);
		}
	}

	@Override
	public boolean apply() {
		boolean success = true;
		pipe.getVarRegexp().clear();
		for (int i = 0; i < model.getSize(); i++) {
			RulePipelineVarRuleRegexPanel varRuleRegexPanel = model.get(i);
			VarRuleRegex varRuleRegex = new VarRuleRegex();
			varRuleRegexPanel.apply(varRuleRegex);
			pipe.getVarRegexp().add(varRuleRegex);
		}
		return success;
	}

	@Override
	public void redirectEnabled(boolean enabled) {
		// Nothing to do
	}

	/**
	 * Create Failure Pipeline Panel
	 * 
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 * @return Failure Pipeline Panel
	 */
	private static RulePipelineVarRuleRegexPanel createVarURLRegexTablePanel(JDialog owner, SettingsManager settingsManager) {
		VarRuleRegex varRuleRegex = new VarRuleRegex();
		varRuleRegex.setVariableName("");
		RulePipelineVarRuleRegexPanel varRuleRegexPanel = new RulePipelineVarRuleRegexPanel(owner, settingsManager, varRuleRegex);
		RuleVarRegexpEditor rme = new RuleVarRegexpEditor(owner, settingsManager, varRuleRegexPanel);
		if (rme.isCanceled()) {
			return null;
		}
		return varRuleRegexPanel;
	}

	private static RulePipelineVarRuleRegexPanel createVarURLRegexTablePanel(JDialog owner, SettingsManager settingsManager, VarRuleRegex varRuleRegex) {
		return new RulePipelineVarRuleRegexPanel(owner, settingsManager, varRuleRegex);
	}

	/**
	 * Create Failure Pipeline Panel
	 * 
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 * @param varRuleRegexPanel Var Rule Regex Panel
	 * @return Failure Pipeline Panel
	 */
	private static RulePipelineVarRuleRegexPanel editVarURLRegexTablePanel(JDialog owner, SettingsManager settingsManager, RulePipelineVarRuleRegexPanel varRuleRegexPanel) {
		RuleVarRegexpEditor rvme = new RuleVarRegexpEditor(owner, settingsManager, varRuleRegexPanel);
		if (rvme.isCanceled()) {
			return null;
		}
		return varRuleRegexPanel;
	}
}

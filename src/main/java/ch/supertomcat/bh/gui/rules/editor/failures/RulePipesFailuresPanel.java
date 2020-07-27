package ch.supertomcat.bh.gui.rules.editor.failures;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;

import ch.supertomcat.bh.gui.rules.editor.base.PipeListCellRenderer;
import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorListSelectionPanel;
import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorPart;
import ch.supertomcat.bh.rules.xml.FailureType;
import ch.supertomcat.bh.rules.xml.FailuresPipeline;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import ch.supertomcat.bh.settings.SettingsManager;

/**
 * Failure Pipes Panel
 */
public class RulePipesFailuresPanel extends RuleEditorListSelectionPanel<RulePipelineFailuresPanel, DefaultListModel<RulePipelineFailuresPanel>> implements RuleEditorPart {
	private static final long serialVersionUID = 1L;

	/**
	 * Rule
	 */
	private final RuleDefinition rule;

	/**
	 * Constructor
	 * 
	 * @param rule Rule
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 */
	public RulePipesFailuresPanel(RuleDefinition rule, JDialog owner, SettingsManager settingsManager) {
		super(new DefaultListModel<>(), () -> createFailurePipelinePanel(owner, settingsManager), new PipeListCellRenderer());
		this.rule = rule;

		for (FailuresPipeline failurePipe : rule.getFailuresPipes()) {
			RulePipelineFailuresPanel pipelinePanel = createFailurePipelinePanel(owner, settingsManager, failurePipe);
			model.addElement(pipelinePanel);
		}
	}

	@Override
	public boolean apply() {
		boolean success = true;
		rule.getFailuresPipes().clear();
		for (int i = 0; i < model.getSize(); i++) {
			RulePipelineFailuresPanel failurePipelinePanel = model.get(i);
			if (!failurePipelinePanel.apply()) {
				success = false;
			}
			rule.getFailuresPipes().add(failurePipelinePanel.getPipe());
		}
		return success;
	}

	@Override
	public void redirectEnabled(boolean enabled) {
		for (int i = 0; i < model.getSize(); i++) {
			RulePipelineFailuresPanel failurePipelinePanel = model.get(i);
			failurePipelinePanel.redirectEnabled(enabled);
		}
	}

	/**
	 * Create Failure Pipeline Panel
	 * 
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 * @return Failure Pipeline Panel
	 */
	private static RulePipelineFailuresPanel createFailurePipelinePanel(JDialog owner, SettingsManager settingsManager) {
		FailuresPipeline pipe = new FailuresPipeline();
		pipe.setFailureType(FailureType.FAILED);
		pipe.setCheckURL(false);
		pipe.setCheckThumbURL(false);
		pipe.setCheckPageSourceCode(false);
		return createFailurePipelinePanel(owner, settingsManager, pipe);
	}

	/**
	 * Create Failure Pipeline Panel
	 * 
	 * @param pipe Pipe Definition
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 * @return Failure Pipeline Panel
	 */
	private static RulePipelineFailuresPanel createFailurePipelinePanel(JDialog owner, SettingsManager settingsManager, FailuresPipeline pipe) {
		return new RulePipelineFailuresPanel(pipe, owner, settingsManager);
	}
}

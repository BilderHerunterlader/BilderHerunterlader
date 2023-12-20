package ch.supertomcat.bh.gui.rules.editor.urlpipe;

import java.util.function.Supplier;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;

import ch.supertomcat.bh.gui.rules.editor.base.PipeListCellRenderer;
import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorListSelectionPanelBase;
import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorPart;
import ch.supertomcat.bh.gui.rules.editor.urlpipe.javascript.RulePipelineURLJavascriptPanel;
import ch.supertomcat.bh.gui.rules.editor.urlpipe.regex.RulePipelineURLRegexPanel;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import ch.supertomcat.bh.rules.xml.URLJavascriptPipeline;
import ch.supertomcat.bh.rules.xml.URLMode;
import ch.supertomcat.bh.rules.xml.URLPipeline;
import ch.supertomcat.bh.rules.xml.URLRegexPipeline;
import ch.supertomcat.bh.rules.xml.URLRegexPipelineMode;
import ch.supertomcat.bh.settings.SettingsManager;

/**
 * URL / Javascript Pipes Panel
 */
public class RulePipesPanel extends RuleEditorListSelectionPanelBase<RulePipelineURLPanelBase<? extends URLPipeline>, DefaultListModel<RulePipelineURLPanelBase<? extends URLPipeline>>, RuleEditorURLPipesButtonPanel> implements RuleEditorPart {
	private static final long serialVersionUID = 1L;

	/**
	 * Supplier for action new
	 */
	protected final Supplier<RulePipelineURLRegexPanel> actionNewRegexSupplier;

	/**
	 * Supplier for action new
	 */
	protected final Supplier<RulePipelineURLJavascriptPanel> actionNewJavascriptSupplier;

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
	public RulePipesPanel(RuleDefinition rule, JDialog owner, SettingsManager settingsManager) {
		super(new DefaultListModel<>(), null, new RuleEditorURLPipesButtonPanel(false), new PipeListCellRenderer());
		this.actionNewRegexSupplier = () -> createRegexPipelinePanel(owner, settingsManager);
		this.actionNewJavascriptSupplier = () -> createJavascriptPipelinePanel(owner);
		this.rule = rule;

		buttonPanel.addNewRegexActionListener(e -> actionNewRegex());
		buttonPanel.addNewJavascriptActionListener(e -> actionNewJavascript());
		buttonPanel.addEditActionListener(e -> actionEdit());
		buttonPanel.addUpActionListener(e -> actionUp());
		buttonPanel.addDownActionListener(e -> actionDown());
		buttonPanel.addDeleteActionListener(e -> actionDelete());

		for (URLPipeline urlPipe : rule.getPipes()) {
			RulePipelineURLPanelBase<? extends URLPipeline> pipelinePanel;
			if (urlPipe instanceof URLRegexPipeline) {
				pipelinePanel = createRegexPipelinePanel(owner, settingsManager, (URLRegexPipeline)urlPipe);
			} else if (urlPipe instanceof URLJavascriptPipeline) {
				pipelinePanel = createJavascriptPipelinePanel(owner, (URLJavascriptPipeline)urlPipe);
			} else {
				throw new RuntimeException("Unsupported URLPipeline Type: " + urlPipe.getClass());
			}
			model.addElement(pipelinePanel);
		}
	}

	@Override
	public boolean apply() {
		boolean success = true;
		rule.getPipes().clear();
		for (int i = 0; i < model.getSize(); i++) {
			RulePipelineURLPanelBase<? extends URLPipeline> urlPipelinePanel = model.get(i);
			if (!urlPipelinePanel.apply()) {
				success = false;
			}
			rule.getPipes().add(urlPipelinePanel.getPipe());
		}
		return success;
	}

	@Override
	public void redirectEnabled(boolean enabled) {
		for (int i = 0; i < model.getSize(); i++) {
			RulePipelineURLPanelBase<? extends URLPipeline> urlPipelinePanel = model.get(i);
			urlPipelinePanel.redirectEnabled(enabled);
		}
	}

	@Override
	protected void actionNew() {
		throw new RuntimeException("actionNew is not supported");
	}

	/**
	 * Action New Regex
	 */
	private void actionNewRegex() {
		RulePipelineURLRegexPanel element = actionNewRegexSupplier.get();
		if (element != null) {
			model.addElement(element);
		}
	}

	/**
	 * Action New Javascript
	 */
	private void actionNewJavascript() {
		RulePipelineURLJavascriptPanel element = actionNewJavascriptSupplier.get();
		if (element != null) {
			model.addElement(element);
		}
	}

	/**
	 * Create Regex Pipeline Panel
	 * 
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 * @return Regex Pipeline Panel
	 */
	private static RulePipelineURLRegexPanel createRegexPipelinePanel(JDialog owner, SettingsManager settingsManager) {
		URLRegexPipeline pipe = new URLRegexPipeline();
		pipe.setWaitBeforeExecute(0);
		pipe.setUrlDecodeResult(false);
		pipe.setSendCookies(true);
		pipe.setMode(URLRegexPipelineMode.CONTAINER_OR_THUMBNAIL_URL);
		pipe.setUrlMode(URLMode.CONTAINER_URL);
		return createRegexPipelinePanel(owner, settingsManager, pipe);
	}

	/**
	 * Create Regex Pipeline Panel
	 * 
	 * @param pipe Pipe Definition
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 * @return Regex Pipeline Panel
	 */
	private static RulePipelineURLRegexPanel createRegexPipelinePanel(JDialog owner, SettingsManager settingsManager, URLRegexPipeline pipe) {
		return new RulePipelineURLRegexPanel(pipe, owner, settingsManager);
	}

	/**
	 * Create Javascript Pipeline Panel
	 * 
	 * @param owner Owner
	 * @return Javascript Pipeline Panel
	 */
	private static RulePipelineURLJavascriptPanel createJavascriptPipelinePanel(JDialog owner) {
		URLJavascriptPipeline pipe = new URLJavascriptPipeline();
		pipe.setWaitBeforeExecute(0);
		pipe.setUrlDecodeResult(false);
		pipe.setSendCookies(true);
		pipe.setJavascriptCode("");
		return createJavascriptPipelinePanel(owner, pipe);
	}

	/**
	 * Create Javascript Pipeline Panel
	 * 
	 * @param pipe Pipe Definition
	 * @param owner Owner
	 * @return Javascript Pipeline Panel
	 */
	private static RulePipelineURLJavascriptPanel createJavascriptPipelinePanel(JDialog owner, URLJavascriptPipeline pipe) {
		return new RulePipelineURLJavascriptPanel(pipe, owner);
	}
}

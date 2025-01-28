package ch.supertomcat.bh.gui.rules.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorPart;
import ch.supertomcat.bh.gui.rules.editor.failures.RulePipesFailuresPanel;
import ch.supertomcat.bh.gui.rules.editor.filename.RulePipelineFilenameDownloadSelectionPanel;
import ch.supertomcat.bh.gui.rules.editor.filename.RulePipelineFilenamePanel;
import ch.supertomcat.bh.gui.rules.editor.urlpipe.RulePipesPanel;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Rule-Main-Editor
 */
public class RuleMainEditor extends JDialog {
	private static final long serialVersionUID = 1L;

	/**
	 * General Panel
	 */
	private RuleGeneralPanel pnlGeneral;

	/**
	 * TabbedPane
	 */
	private JTabbedPane tabs = new JTabbedPane();

	/**
	 * Pipes Panel
	 */
	private RulePipesPanel pnlPipes;

	/**
	 * Failure Pipes Panel
	 */
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
	 * Rule Editor Parts
	 */
	private final List<RuleEditorPart> ruleEditorParts = new ArrayList<>();

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
	 * @param owner Owner
	 * @param rule Rule
	 * @param settingsManager Settings Manager
	 * @param developerCheckboxAvailable True if Developer Checkbox is available, false otherwise
	 */
	public RuleMainEditor(JFrame owner, Rule rule, SettingsManager settingsManager, boolean developerCheckboxAvailable) {
		super(owner);
		this.rule = rule;
		this.definition = rule.getDefinition();
		setTitle(Localization.getString("RuleEditor"));
		setLayout(new BorderLayout());

		pnlButtons.add(btnTest);
		pnlButtons.add(btnOK);
		pnlButtons.add(btnApply);
		pnlButtons.add(btnCancel);

		btnCancel.setMnemonic(KeyEvent.VK_C);

		btnTest.addActionListener(e -> {
			if (apply()) {
				RuleTest dlg = new RuleTest(rule, this);
				dlg.setVisible(true);
			}
		});
		btnOK.addActionListener(e -> {
			if (apply()) {
				canceled = false;
				this.dispose();
			}
		});
		btnApply.addActionListener(e -> apply());
		btnCancel.addActionListener(e -> {
			canceled = true;
			dispose();
		});

		pnlGeneral = new RuleGeneralPanel(rule, definition, this, settingsManager, developerCheckboxAvailable, this::updateRedirectState);

		pnlPipes = new RulePipesPanel(definition, this, settingsManager);

		pnlPipesFailures = new RulePipesFailuresPanel(definition, this, settingsManager);

		pnlFilename = new RulePipelineFilenamePanel(definition, definition.getFilenamePipeline(), this, settingsManager);
		pnlFilenameDownloadSelection = new RulePipelineFilenameDownloadSelectionPanel(definition, definition.getFilenameDownloadSelectionPipeline(), this, settingsManager);

		pnlRuleOptions = new RuleOptionsPanel(definition);
		pnlConnections = new RuleConnectionsPanel(definition.getRestriction());

		tabs.addTab(Localization.getString("URLContainerPage"), pnlPipes);
		tabs.addTab(Localization.getString("FilenameAfterDownload"), pnlFilename);
		tabs.addTab(Localization.getString("FilenameDownloadSelection"), pnlFilenameDownloadSelection);
		tabs.addTab(Localization.getString("Options"), pnlRuleOptions);
		tabs.addTab(Localization.getString("MaxConnections"), pnlConnections);
		tabs.addTab(Localization.getString("Failures"), pnlPipesFailures);
		add(new JScrollPane(pnlGeneral), BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);

		ruleEditorParts.add(pnlGeneral);
		ruleEditorParts.add(pnlPipes);
		ruleEditorParts.add(pnlPipesFailures);
		ruleEditorParts.add(pnlFilename);
		ruleEditorParts.add(pnlFilenameDownloadSelection);
		ruleEditorParts.add(pnlRuleOptions);
		ruleEditorParts.add(pnlConnections);

		/*
		 * Everything is now setup, so we can set redirect-checkbox-state
		 */
		pnlGeneral.fireRedirectChangedConsumer();

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

	/**
	 * Apply
	 * 
	 * @return True if successfull
	 */
	private boolean apply() {
		boolean success = true;
		for (RuleEditorPart ruleEditorPart : ruleEditorParts) {
			if (!ruleEditorPart.apply()) {
				success = false;
			}
		}

		rule.updateFromDefinition();
		return success;
	}

	/**
	 * Returns the canceled
	 * 
	 * @return canceled
	 */
	public boolean isCanceled() {
		return canceled;
	}

	private void updateRedirectState(boolean enabled) {
		for (RuleEditorPart ruleEditorPart : ruleEditorParts) {
			ruleEditorPart.redirectEnabled(enabled);
		}
	}
}

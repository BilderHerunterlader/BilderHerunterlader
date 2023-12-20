package ch.supertomcat.bh.gui.rules.editor.urlpipe.regex;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import ch.supertomcat.bh.gui.rules.editor.urlpipe.RulePipelineURLPanelBase;
import ch.supertomcat.bh.gui.rules.editor.urlpipe.varregex.RulePipelineVarRuleRegexListPanel;
import ch.supertomcat.bh.rules.xml.URLMode;
import ch.supertomcat.bh.rules.xml.URLRegexPipeline;
import ch.supertomcat.bh.rules.xml.URLRegexPipelineMode;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Rule-Pipeline-Panel
 */
public class RulePipelineURLRegexPanel extends RulePipelineURLPanelBase<URLRegexPipeline> {
	private static final long serialVersionUID = 1L;

	/**
	 * Table Panel
	 */
	private RulePipelineURLRegexTablePanel pnlTable;

	/**
	 * Var Rule Regex List
	 */
	private RulePipelineVarRuleRegexListPanel pnlVarRuleRegexList;

	/**
	 * Mode
	 */
	private URLRegexPipelineMode mode = URLRegexPipelineMode.CONTAINER_OR_THUMBNAIL_URL;

	/**
	 * ButtonGroup
	 */
	private ButtonGroup bgMode = new ButtonGroup();

	/**
	 * RadioButton
	 */
	private JRadioButton rbModeZero = new JRadioButton(Localization.getString("RuleModeZeroShort"), true);

	/**
	 * RadioButton
	 */
	private JRadioButton rbModeOne = new JRadioButton(Localization.getString("RuleModeOneShort"), false);

	/**
	 * URL-Mode
	 */
	private URLMode urlMode = URLMode.CONTAINER_URL;

	/**
	 * ButtonGroup
	 */
	private ButtonGroup bgURLMode = new ButtonGroup();

	/**
	 * RadioButton
	 */
	private JRadioButton rbURLModeZero = new JRadioButton(Localization.getString("RuleURLModeZero"), true);

	/**
	 * RadioButton
	 */
	private JRadioButton rbURLModeOne = new JRadioButton(Localization.getString("RuleURLModeOne"), false);

	/**
	 * lblWaitBeforeExecute
	 */
	private JLabel lblWaitBeforeExecute = new JLabel(Localization.getString("RulePipelineWaitBeforeExecute"));

	/**
	 * txtWaitBeforeExecute
	 */
	private JTextField txtWaitBeforeExecute = new JTextField(5);

	private JCheckBox chkURLDecodeResult = new JCheckBox(Localization.getString("RulePipelineURLDecodeResult"), false);

	private JCheckBox chkJavcascriptDecodeResult = new JCheckBox(Localization.getString("RulePipelineJavascriptDecodeResult"), false);

	private JCheckBox chkSendCookies = new JCheckBox(Localization.getString("RulePipelineSendCookies"), true);

	/**
	 * Panel
	 */
	private JPanel pnlRB = new JPanel();

	/**
	 * Constructor
	 * 
	 * @param pipe Pipeline
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 */
	public RulePipelineURLRegexPanel(URLRegexPipeline pipe, JDialog owner, SettingsManager settingsManager) {
		super(pipe);
		this.mode = pipe.getMode();
		this.urlMode = pipe.getUrlMode();
		setLayout(new BorderLayout());

		pnlTable = new RulePipelineURLRegexTablePanel(owner, settingsManager, pipe.getRegexp());

		pnlVarRuleRegexList = new RulePipelineVarRuleRegexListPanel(pipe, owner, settingsManager);
		TitledBorder brdVarRuleRegexList = BorderFactory.createTitledBorder(Localization.getString("VariableAssignmentRegex"));
		pnlVarRuleRegexList.setBorder(brdVarRuleRegexList);

		boolean bMode = (mode == URLRegexPipelineMode.CONTAINER_OR_THUMBNAIL_URL);
		rbModeZero.setSelected(bMode);
		rbModeOne.setSelected(!bMode);
		bgMode.add(rbModeZero);
		bgMode.add(rbModeOne);

		rbURLModeZero.setEnabled(bMode);
		rbURLModeOne.setEnabled(bMode);

		bgURLMode.add(rbURLModeZero);
		bgURLMode.add(rbURLModeOne);

		if (this.mode == URLRegexPipelineMode.CONTAINER_OR_THUMBNAIL_URL) {
			boolean b = (urlMode == URLMode.CONTAINER_URL);
			rbURLModeZero.setSelected(b);
			rbURLModeOne.setSelected(!b);
		}

		txtWaitBeforeExecute.setText(String.valueOf(pipe.getWaitBeforeExecute()));

		chkURLDecodeResult.setToolTipText(Localization.getString("RulePipelineURLDecodeResultToolTip"));
		chkURLDecodeResult.setSelected(pipe.isUrlDecodeResult());

		chkJavcascriptDecodeResult.setSelected(pipe.isJavascriptDecodeResult() != null && pipe.isJavascriptDecodeResult());

		chkSendCookies.setSelected(pipe.isSendCookies());

		pnlRB.setLayout(new GridLayout(5, 3));
		pnlRB.add(rbModeZero);
		pnlRB.add(rbURLModeZero);
		pnlRB.add(lblWaitBeforeExecute);
		pnlRB.add(rbModeOne);
		pnlRB.add(rbURLModeOne);
		pnlRB.add(txtWaitBeforeExecute);
		pnlRB.add(new JLabel());
		pnlRB.add(new JLabel());
		pnlRB.add(chkURLDecodeResult);
		pnlRB.add(new JLabel());
		pnlRB.add(new JLabel());
		pnlRB.add(chkJavcascriptDecodeResult);
		pnlRB.add(new JLabel());
		pnlRB.add(new JLabel());
		pnlRB.add(chkSendCookies);

		rbModeOne.addActionListener(e -> updateURLModeComponents());
		rbModeZero.addActionListener(e -> updateURLModeComponents());

		JPanel pnlPipeline = new JPanel(new BorderLayout());
		TitledBorder brd = BorderFactory.createTitledBorder(Localization.getString("Pipeline"));
		pnlPipeline.setBorder(brd);
		pnlPipeline.add(pnlRB, BorderLayout.NORTH);
		pnlPipeline.add(pnlTable, BorderLayout.CENTER);

		JPanel pnlMain = new JPanel(new BorderLayout());
		pnlMain.add(pnlPipeline, BorderLayout.NORTH);
		pnlMain.add(pnlVarRuleRegexList, BorderLayout.SOUTH);

		JScrollPane scrollPane = new JScrollPane(pnlMain);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, BorderLayout.CENTER);
	}

	private void updateURLModeComponents() {
		rbURLModeZero.setEnabled(rbModeZero.isSelected());
		rbURLModeOne.setEnabled(rbModeZero.isSelected());
		mode = rbModeZero.isSelected() ? URLRegexPipelineMode.CONTAINER_OR_THUMBNAIL_URL : URLRegexPipelineMode.CONTAINER_PAGE_SOURCECODE;
	}

	@Override
	public boolean apply() {
		pipe.setMode(mode);
		if (this.mode == URLRegexPipelineMode.CONTAINER_OR_THUMBNAIL_URL) {
			if (rbURLModeZero.isSelected()) {
				pipe.setUrlMode(URLMode.CONTAINER_URL);
			} else {
				pipe.setUrlMode(URLMode.THUMBNAIL_URL);
			}
		}
		int waitBeforeExecute = 0;
		try {
			waitBeforeExecute = Integer.parseInt(txtWaitBeforeExecute.getText());
		} catch (NumberFormatException nfe) {
			logger.error("WaitBeforeExecute Text is not an integer: {}", txtWaitBeforeExecute.getText());
		}
		pipe.setWaitBeforeExecute(waitBeforeExecute);
		pipe.setUrlDecodeResult(chkURLDecodeResult.isSelected());
		if (chkJavcascriptDecodeResult.isSelected()) {
			pipe.setJavascriptDecodeResult(true);
		} else {
			pipe.setJavascriptDecodeResult(null);
		}
		pipe.setSendCookies(chkSendCookies.isSelected());

		pnlTable.apply(pipe.getRegexp());

		pnlVarRuleRegexList.apply();

		return true;
	}

	@Override
	public void redirectEnabled(boolean enabled) {
		// Nothing to do
	}
}

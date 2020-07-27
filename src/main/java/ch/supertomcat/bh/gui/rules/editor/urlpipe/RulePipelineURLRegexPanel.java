package ch.supertomcat.bh.gui.rules.editor.urlpipe;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorTablePanel;
import ch.supertomcat.bh.gui.rules.editor.base.RuleRegexpEditor;
import ch.supertomcat.bh.rules.xml.RuleRegex;
import ch.supertomcat.bh.rules.xml.URLMode;
import ch.supertomcat.bh.rules.xml.URLRegexPipeline;
import ch.supertomcat.bh.rules.xml.URLRegexPipelineMode;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;

/**
 * Rule-Pipeline-Panel
 */
public class RulePipelineURLRegexPanel extends RulePipelineURLPanelBase<URLRegexPipeline> {
	private static final long serialVersionUID = 1L;

	/**
	 * TableModel
	 */
	private RulePipelineURLRegexTableModel model = new RulePipelineURLRegexTableModel();

	/**
	 * Action New Supplier
	 */
	private Supplier<Object[]> actionNewSupplier = () -> createRegexp();

	/**
	 * Action Edit Function
	 */
	private Function<Object[], Object[]> actionEditFunction = x -> editRegexp(x);

	/**
	 * Table Panel
	 */
	private RuleEditorTablePanel<RulePipelineURLRegexTableModel> pnlTable = new RuleEditorTablePanel<>(model, actionNewSupplier, actionEditFunction);

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

	private JCheckBox chkSendCookies = new JCheckBox(Localization.getString("RulePipelineSendCookies"), true);

	/**
	 * Panel
	 */
	private JPanel pnlRB = new JPanel();

	/**
	 * Owner
	 */
	private JDialog owner = null;

	/**
	 * Constructor
	 * 
	 * @param pipe Pipeline
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 */
	public RulePipelineURLRegexPanel(URLRegexPipeline pipe, JDialog owner, SettingsManager settingsManager) {
		super(pipe);
		this.owner = owner;
		this.mode = pipe.getMode();
		this.urlMode = pipe.getUrlMode();
		setLayout(new BorderLayout());

		TitledBorder brd = BorderFactory.createTitledBorder(Localization.getString("Pipeline"));
		this.setBorder(brd);

		updateColWidthsFromSettingsManager(settingsManager);
		pnlTable.getTable().getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnAdded(TableColumnModelEvent e) {
			}

			@Override
			public void columnMarginChanged(ChangeEvent e) {
				updateColWidthsToSettingsManager(settingsManager);
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
			}
		});

		for (RuleRegex regexp : pipe.getRegexp()) {
			model.addRow(regexp.getPattern(), regexp.getReplacement());
		}

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

		chkSendCookies.setSelected(pipe.isSendCookies());

		pnlRB.setLayout(new GridLayout(4, 3));
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
		pnlRB.add(chkSendCookies);
		add(pnlRB, BorderLayout.NORTH);

		rbModeOne.addActionListener(e -> updateURLModeComponents());
		rbModeZero.addActionListener(e -> updateURLModeComponents());

		add(pnlTable, BorderLayout.CENTER);
	}

	private Object[] createRegexp() {
		RuleRegexpEditor rme = new RuleRegexpEditor(owner, false);
		if (rme.isCanceled()) {
			return null;
		}
		return new Object[] { rme.getSearch(), rme.getReplacement() };
	}

	private Object[] editRegexp(Object[] originalData) {
		RuleRegexpEditor rme = new RuleRegexpEditor(owner, (String)originalData[0], (String)originalData[1]);
		if (rme.isCanceled()) {
			return null;
		}
		return new Object[] { rme.getSearch(), rme.getReplacement() };
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
		pipe.setSendCookies(chkSendCookies.isSelected());

		pipe.getRegexp().clear();
		for (int i = 0; i < model.getRowCount(); i++) {
			RuleRegex ruleRegex = new RuleRegex();
			ruleRegex.setPattern((String)model.getValueAt(i, 0));
			ruleRegex.setReplacement((String)model.getValueAt(i, 1));
			pipe.getRegexp().add(ruleRegex);
		}

		return true;
	}

	@Override
	public void redirectEnabled(boolean enabled) {
		// Nothing to do
	}

	/**
	 * updateColWidthsToSettingsManager
	 * 
	 * @param settingsManager SettingsManager
	 */
	private void updateColWidthsToSettingsManager(SettingsManager settingsManager) {
		if (!settingsManager.isSaveTableColumnSizes()) {
			return;
		}
		settingsManager.setColWidthsRulesEditor(TableUtil.serializeColWidthSetting(pnlTable.getTable()));
		settingsManager.writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 * 
	 * @param settingsManager SettingsManager
	 */
	private void updateColWidthsFromSettingsManager(SettingsManager settingsManager) {
		if (!settingsManager.isSaveTableColumnSizes()) {
			return;
		}
		TableUtil.applyColWidths(pnlTable.getTable(), settingsManager.getColWidthsRulesEditor());
	}
}

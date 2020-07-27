package ch.supertomcat.bh.gui.rules.editor.failures;

import java.awt.BorderLayout;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorPart;
import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorTablePanel;
import ch.supertomcat.bh.gui.rules.editor.base.RuleRegexpEditor;
import ch.supertomcat.bh.rules.xml.FailureType;
import ch.supertomcat.bh.rules.xml.FailuresPipeline;
import ch.supertomcat.bh.rules.xml.RuleRegex;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;

/**
 * Rule-Pipeline-Failures-Panel
 */
public class RulePipelineFailuresPanel extends JPanel implements RuleEditorPart {
	private static final long serialVersionUID = 1L;

	/**
	 * TableModel
	 */
	private RulePipelineFailuresTableModel model = new RulePipelineFailuresTableModel();

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
	private RuleEditorTablePanel<RulePipelineFailuresTableModel> pnlTable = new RuleEditorTablePanel<>(model, actionNewSupplier, actionEditFunction);

	private JComboBox<FailureType> cmbFailureType = new JComboBox<>();

	private JCheckBox cbURL = new JCheckBox(Localization.getString("ContainerURL"));

	private JCheckBox cbThumbURL = new JCheckBox(Localization.getString("ThumbnailURL"));

	private JCheckBox cbContainerPage = new JCheckBox(Localization.getString("ContainerPage"));

	/**
	 * Panel
	 */
	private JPanel pnlRB = new JPanel();

	/**
	 * Owner
	 */
	private JDialog owner = null;

	/**
	 * RulePipeline
	 */
	private final FailuresPipeline pipe;

	/**
	 * Constructor
	 * 
	 * @param pipe Pipeline
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 */
	public RulePipelineFailuresPanel(FailuresPipeline pipe, JDialog owner, SettingsManager settingsManager) {
		this.owner = owner;
		this.pipe = pipe;
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
			model.addRow(regexp.getPattern());
		}

		cmbFailureType.addItem(FailureType.FAILED);
		cmbFailureType.addItem(FailureType.FAILED_FILE_NOT_EXIST);
		cmbFailureType.addItem(FailureType.FAILED_FILE_TEMPORARY_OFFLINE);
		cmbFailureType.addItem(FailureType.SLEEPING);
		cmbFailureType.addItem(FailureType.COMPLETE);
		cmbFailureType.setSelectedItem(pipe.getFailureType());

		cbURL.setSelected(pipe.isCheckURL());
		cbThumbURL.setSelected(pipe.isCheckThumbURL());
		cbContainerPage.setSelected(pipe.isCheckPageSourceCode());

		pnlRB.add(cmbFailureType);
		pnlRB.add(cbURL);
		pnlRB.add(cbThumbURL);
		pnlRB.add(cbContainerPage);

		add(pnlRB, BorderLayout.NORTH);
		add(pnlTable, BorderLayout.CENTER);
	}

	private Object[] createRegexp() {
		RuleRegexpEditor rme = new RuleRegexpEditor(owner, true);
		if (rme.isCanceled()) {
			return null;
		}
		return new Object[] { rme.getSearch() };
	}

	private Object[] editRegexp(Object[] originalData) {
		RuleRegexpEditor rme = new RuleRegexpEditor(owner, (String)originalData[0]);
		if (rme.isCanceled()) {
			return null;
		}
		return new Object[] { rme.getSearch() };
	}

	@Override
	public boolean apply() {
		pipe.setFailureType((FailureType)cmbFailureType.getSelectedItem());
		pipe.setCheckURL(cbURL.isSelected());
		pipe.setCheckThumbURL(cbThumbURL.isSelected());
		pipe.setCheckPageSourceCode(cbContainerPage.isSelected());

		pipe.getRegexp().clear();
		for (int i = 0; i < model.getRowCount(); i++) {
			RuleRegex ruleRegex = new RuleRegex();
			ruleRegex.setPattern((String)model.getValueAt(i, 0));
			ruleRegex.setReplacement("");
			pipe.getRegexp().add(ruleRegex);
		}
		return true;
	}

	@Override
	public void redirectEnabled(boolean enabled) {
		// Nothing to do
	}

	/**
	 * Returns the pipe
	 * 
	 * @return pipe
	 */
	public FailuresPipeline getPipe() {
		return pipe;
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

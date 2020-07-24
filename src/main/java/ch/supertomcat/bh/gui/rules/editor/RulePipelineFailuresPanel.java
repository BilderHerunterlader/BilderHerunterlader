package ch.supertomcat.bh.gui.rules.editor;

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

import ch.supertomcat.bh.gui.rules.RuleRegexpEditor;
import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorPart;
import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorTablePanel;
import ch.supertomcat.bh.rules.RulePipelineFailures;
import ch.supertomcat.bh.rules.RuleRegExp;
import ch.supertomcat.bh.rules.xml.FailureType;
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

	/**
	 * RulePipeline
	 */
	private RulePipelineFailures pipe = null;

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
	 * Constructor
	 * 
	 * @param pipe Pipeline
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 */
	public RulePipelineFailuresPanel(RulePipelineFailures pipe, JDialog owner, SettingsManager settingsManager) {
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

		for (RuleRegExp regexp : pipe.getRegexps()) {
			model.addRow(regexp.getSearch());
		}

		cmbFailureType.addItem(FailureType.FAILED);
		cmbFailureType.addItem(FailureType.FAILED_FILE_NOT_EXIST);
		cmbFailureType.addItem(FailureType.FAILED_FILE_TEMPORARY_OFFLINE);
		cmbFailureType.addItem(FailureType.SLEEPING);
		cmbFailureType.addItem(FailureType.COMPLETE);
		cmbFailureType.setSelectedItem(pipe.getDefinition().getFailureType());

		cbURL.setSelected(pipe.getDefinition().isCheckURL());
		cbThumbURL.setSelected(pipe.getDefinition().isCheckThumbURL());
		cbContainerPage.setSelected(pipe.getDefinition().isCheckPageSourceCode());

		pnlRB.add(cmbFailureType);
		pnlRB.add(cbURL);
		pnlRB.add(cbThumbURL);
		pnlRB.add(cbContainerPage);

		add(pnlRB, BorderLayout.NORTH);
		add(pnlTable, BorderLayout.CENTER);
	}

	private Object[] createRegexp() {
		// TODO RuleRegexpEditor should edit definition model object
		RuleRegExp rre = new RuleRegExp();
		RuleRegexpEditor rme = new RuleRegexpEditor(owner, rre, true);
		if (rme.getCanceled()) {
			return null;
		}
		return new Object[] { rre.getSearch() };
	}

	private Object[] editRegexp(Object[] originalData) {
		// TODO RuleRegexpEditor should edit definition model object
		RuleRegExp rre = new RuleRegExp((String)originalData[0], "");
		RuleRegexpEditor rme = new RuleRegexpEditor(owner, rre, true);
		if (rme.getCanceled()) {
			return null;
		}
		return new Object[] { rre.getSearch() };
	}

	@Override
	public void apply() {
		pipe.getDefinition().setFailureType((FailureType)cmbFailureType.getSelectedItem());
		pipe.getDefinition().setCheckURL(cbURL.isSelected());
		pipe.getDefinition().setCheckThumbURL(cbThumbURL.isSelected());
		pipe.getDefinition().setCheckPageSourceCode(cbContainerPage.isSelected());

		pipe.getDefinition().getRegexp().clear();
		for (int i = 0; i < model.getRowCount(); i++) {
			RuleRegex ruleRegex = new RuleRegex();
			ruleRegex.setPattern((String)model.getValueAt(i, 0));
			ruleRegex.setReplacement("");
			pipe.getDefinition().getRegexp().add(ruleRegex);
		}
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
		if (settingsManager.isSaveTableColumnSizes() == false) {
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
		if (settingsManager.isSaveTableColumnSizes() == false) {
			return;
		}
		TableUtil.applyColWidths(pnlTable.getTable(), settingsManager.getColWidthsRulesEditor());
	}
}

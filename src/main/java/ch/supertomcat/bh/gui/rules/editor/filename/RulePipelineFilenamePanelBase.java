package ch.supertomcat.bh.gui.rules.editor.filename;

import java.awt.BorderLayout;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorPart;
import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorTablePanel;
import ch.supertomcat.bh.gui.rules.editor.base.RuleRegexpEditor;
import ch.supertomcat.bh.gui.rules.editor.urlpipe.RulePipelineURLRegexTableModel;
import ch.supertomcat.bh.rules.xml.Pipeline;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import ch.supertomcat.bh.rules.xml.RuleRegex;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;

/**
 * Base class for Rule-Pipeline-Panels
 * 
 * @param <T> Pipeline Type
 * @param <S> Filename Mode Type
 */
public abstract class RulePipelineFilenamePanelBase<T extends Pipeline, S extends Enum<S>> extends JPanel implements RuleEditorPart {
	private static final long serialVersionUID = 1L;

	/**
	 * TableModel
	 */
	protected RulePipelineURLRegexTableModel model = new RulePipelineURLRegexTableModel();

	/**
	 * Action New Supplier
	 */
	protected Supplier<Object[]> actionNewSupplier = () -> createRegexp();

	/**
	 * Action Edit Function
	 */
	protected Function<Object[], Object[]> actionEditFunction = x -> editRegexp(x);

	/**
	 * Table Panel
	 */
	protected RuleEditorTablePanel<RulePipelineURLRegexTableModel> pnlTable = new RuleEditorTablePanel<>(model, actionNewSupplier, actionEditFunction);

	/**
	 * ComboBox
	 */
	protected JComboBox<S> cbFilenameMode = new JComboBox<>();

	/**
	 * Label
	 */
	protected JLabel lblSource = new JLabel(Localization.getString("Source"));

	/**
	 * Panel
	 */
	protected JPanel pnlRB = new JPanel();

	/**
	 * Redirect Enabled
	 */
	protected boolean redirectEnabled = false;

	/**
	 * Owner
	 */
	protected JDialog owner = null;

	/**
	 * Rule
	 */
	protected final RuleDefinition rule;

	/**
	 * RulePipeline
	 */
	protected final T pipe;

	/**
	 * Constructor
	 * 
	 * @param rule Rule
	 * @param pipe Pipe
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 */
	public RulePipelineFilenamePanelBase(RuleDefinition rule, T pipe, JDialog owner, SettingsManager settingsManager) {
		this.owner = owner;
		this.rule = rule;
		this.pipe = pipe;
		setLayout(new BorderLayout());

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

		redirectEnabled = rule.isRedirect();

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

	@Override
	public boolean apply() {
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
		this.redirectEnabled = enabled;
		updateCompomentEnabledState();
	}

	/**
	 * Updated Component Enabled States
	 */
	protected abstract void updateCompomentEnabledState();

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

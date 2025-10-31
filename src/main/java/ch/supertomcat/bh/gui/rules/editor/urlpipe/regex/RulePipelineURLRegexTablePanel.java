package ch.supertomcat.bh.gui.rules.editor.urlpipe.regex;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorTablePanel;
import ch.supertomcat.bh.gui.rules.editor.base.RuleRegexpEditor;
import ch.supertomcat.bh.gui.settings.RegexSearchReplaceTableModel;
import ch.supertomcat.bh.rules.xml.RuleRegex;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;

/**
 * Panel with buttons to edit a table
 */
public class RulePipelineURLRegexTablePanel extends RuleEditorTablePanel<RegexSearchReplaceTableModel> {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param owner Owner Settings Manager
	 * @param settingsManager
	 * @param regexps Regex List
	 */
	public RulePipelineURLRegexTablePanel(JDialog owner, SettingsManager settingsManager, List<RuleRegex> regexps) {
		super(new RegexSearchReplaceTableModel(), () -> createRegexp(owner), x -> editRegexp(owner, x));

		updateColWidthsFromSettingsManager(settingsManager);
		table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnAdded(TableColumnModelEvent e) {
				// Nothing to do
			}

			@Override
			public void columnMarginChanged(ChangeEvent e) {
				updateColWidthsToSettingsManager(settingsManager);
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
				// Nothing to do
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
				// Nothing to do
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
				// Nothing to do
			}
		});

		for (RuleRegex regexp : regexps) {
			model.addRow(regexp.getPattern(), regexp.getReplacement());
		}
	}

	/**
	 * Apply
	 * 
	 * @param regexps Regex List
	 */
	public void apply(List<RuleRegex> regexps) {
		regexps.clear();
		for (int i = 0; i < model.getRowCount(); i++) {
			RuleRegex ruleRegex = new RuleRegex();
			ruleRegex.setPattern((String)model.getValueAt(i, 0));
			ruleRegex.setReplacement((String)model.getValueAt(i, 1));
			regexps.add(ruleRegex);
		}
	}

	private static Object[] createRegexp(JDialog owner) {
		RuleRegexpEditor rme = new RuleRegexpEditor(owner, false);
		if (rme.isCanceled()) {
			return null;
		}
		return new Object[] { rme.getSearch(), rme.getReplacement() };
	}

	private static Object[] editRegexp(JDialog owner, Object[] originalData) {
		RuleRegexpEditor rme = new RuleRegexpEditor(owner, (String)originalData[0], (String)originalData[1]);
		if (rme.isCanceled()) {
			return null;
		}
		return new Object[] { rme.getSearch(), rme.getReplacement() };
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
		settingsManager.setColWidthsRulesEditor(TableUtil.serializeColWidthSetting(table));
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
		TableUtil.applyColWidths(table, settingsManager.getColWidthsRulesEditor());
	}
}

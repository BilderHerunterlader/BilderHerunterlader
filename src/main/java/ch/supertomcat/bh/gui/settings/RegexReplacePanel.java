package ch.supertomcat.bh.gui.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import ch.supertomcat.bh.gui.rules.editor.urlpipe.regex.RulePipelineURLRegexTableModel;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.layout.SpringUtilities;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;
import ch.supertomcat.supertomcatutils.regex.RegexReplace;
import ch.supertomcat.supertomcatutils.regex.RegexReplacePipeline;

/**
 * Rule-Pipeline-Panel
 */
public class RegexReplacePanel extends JPanel implements ActionListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Table
	 */
	private JTable table;

	/**
	 * ScrollPane
	 */
	private JScrollPane sp;

	/**
	 * TableModel
	 */
	private RulePipelineURLRegexTableModel model = new RulePipelineURLRegexTableModel();

	/**
	 * pipe
	 */
	private RegexReplacePipeline pipe = null;

	/**
	 * Button
	 */
	private JButton btnNew = new JButton(Localization.getString("New"), Icons.getTangoIcon("actions/document-new.png", 16));

	/**
	 * Button
	 */
	private JButton btnEdit = new JButton(Localization.getString("Edit"), Icons.getTangoIcon("apps/accessories-text-editor.png", 16));

	/**
	 * Button
	 */
	private JButton btnUp = new JButton(Localization.getString("Up"), Icons.getTangoIcon("actions/go-up.png", 16));

	/**
	 * Button
	 */
	private JButton btnDown = new JButton(Localization.getString("Down"), Icons.getTangoIcon("actions/go-down.png", 16));

	/**
	 * Button
	 */
	private JButton btnDelete = new JButton(Localization.getString("Delete"), Icons.getTangoIcon("actions/edit-delete.png", 16));

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * Constructor
	 * 
	 * @param pipe Pipeline
	 * @param settingsManager Settings Manager
	 */
	public RegexReplacePanel(RegexReplacePipeline pipe, SettingsManager settingsManager) {
		super();
		this.pipe = pipe;
		setLayout(new BorderLayout());

		table = new JTable(model);

		TableUtil.internationalizeColumns(table);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Object.class, new DefaultStringColorRowRenderer());
		updateColWidthsFromSettingsManager(settingsManager);
		table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
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
		table.getTableHeader().setReorderingAllowed(false);
		table.setRowHeight(TableUtil.calculateRowHeight(table, false, true));

		Iterator<RegexReplace> it = pipe.getRegexps().iterator();
		while (it.hasNext()) {
			RegexReplace rrre = it.next();
			model.addRow(rrre.getSearch(), rrre.getReplace());
		}

		Dimension preferredScrollableTableSize = new Dimension(table.getPreferredScrollableViewportSize().width, 15 * table.getRowHeight());
		table.setPreferredScrollableViewportSize(preferredScrollableTableSize);

		sp = new JScrollPane(table);

		add(sp, BorderLayout.CENTER);

		pnlButtons.setLayout(new SpringLayout());
		pnlButtons.add(btnNew);
		pnlButtons.add(btnEdit);
		pnlButtons.add(btnUp);
		pnlButtons.add(btnDown);
		pnlButtons.add(btnDelete);
		SpringUtilities.makeCompactGrid(pnlButtons, 5, 1, 0, 0, 5, 5);
		add(pnlButtons, BorderLayout.EAST);

		btnNew.addActionListener(this);
		btnEdit.addActionListener(this);
		btnUp.addActionListener(this);
		btnDown.addActionListener(this);
		btnDelete.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JDialog parent = null;
		if (this.getTopLevelAncestor() instanceof JDialog) {
			parent = (JDialog)this.getTopLevelAncestor();
		}
		if (e.getSource() == btnNew) {
			RegexReplace rrre = new RegexReplace();
			RegexReplaceRegexpEditor rme = new RegexReplaceRegexpEditor(parent, rrre);
			if (rme.getCanceled()) {
				return;
			}
			model.addRow(rrre.getSearch(), rrre.getReplace());
			pipe.addRegExp(rrre);
		} else if (e.getSource() == btnEdit) {
			int row = table.getSelectedRow();
			if (row < 0) {
				return;
			}
			RegexReplace rrre = pipe.getRegexp(row);
			RegexReplaceRegexpEditor rme = new RegexReplaceRegexpEditor(parent, rrre);
			if (rme.getCanceled()) {
				return;
			}
			model.setValueAt(rrre.getSearch(), row, 0);
			model.setValueAt(rrre.getReplace(), row, 1);
		} else if (e.getSource() == btnDelete) {
			int row = table.getSelectedRow();
			if (row < 0) {
				return;
			}
			model.removeRow(row);
			pipe.removeRegExp(row);
		} else if (e.getSource() == btnUp) {
			int row = table.getSelectedRow();
			if (row > 0) {
				pipe.swapRegExp(row, row - 1);
				model.moveRow(row, row, row - 1);
				table.setRowSelectionInterval(row - 1, row - 1);
			}
		} else if (e.getSource() == btnDown) {
			int row = table.getSelectedRow();
			if (row > -1 && row < model.getRowCount() - 1) {
				pipe.swapRegExp(row, row + 1);
				model.moveRow(row, row, row + 1);
				table.setRowSelectionInterval(row + 1, row + 1);
			}
		}
	}

	/**
	 * Apply
	 */
	public void apply() {
	}

	/**
	 * updateColWidthsToSettingsManager
	 * 
	 * @param settingsManager Settings Manager
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
	 * @param settingsManager Settings Manager
	 */
	private void updateColWidthsFromSettingsManager(SettingsManager settingsManager) {
		if (!settingsManager.isSaveTableColumnSizes()) {
			return;
		}
		TableUtil.applyColWidths(table, settingsManager.getColWidthsRulesEditor());
	}
}

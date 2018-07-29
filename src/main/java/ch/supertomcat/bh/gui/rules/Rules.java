package ch.supertomcat.bh.gui.rules;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.gui.Main;
import ch.supertomcat.bh.gui.renderer.RulesColorRowRenderer;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.TableTool;
import ch.supertomcat.supertomcattools.guitools.progressmonitor.ProgressObserver;
import ch.supertomcat.supertomcattools.guitools.tablerenderer.DefaultBooleanColorRowRenderer;

/**
 * Rules-Panel
 */
public class Rules extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * TabelModel
	 */
	private RulesTableModel model = new RulesTableModel();

	/**
	 * Table
	 */
	private JTable jtRules = new JTable(model);

	private TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);

	/**
	 * Button
	 */
	private JButton btnSave = new JButton(Localization.getString("Save"), Icons.getTangoIcon("actions/document-save.png", 16));

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
	private JButton btnDelete = new JButton(Localization.getString("Delete"), Icons.getTangoIcon("actions/edit-delete.png", 16));

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * HostManager
	 */
	private HostManager hm = HostManager.instance();

	/**
	 * Constructor
	 */
	public Rules() {
		setLayout(new BorderLayout());

		jtRules = new JTable(model);

		TableTool.internationalizeColumns(jtRules);

		jtRules.setDefaultRenderer(Object.class, new RulesColorRowRenderer());
		jtRules.setDefaultRenderer(Boolean.class, new DefaultBooleanColorRowRenderer());

		sorter.setComparator(0, new Comparator<Rule>() {
			@Override
			public int compare(Rule o1, Rule o2) {
				int compDeveloper = Boolean.compare(o1.isDeveloper(), o2.isDeveloper());
				if (compDeveloper == 0) {
					return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
				}
				return -compDeveloper;
			}
		});
		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		jtRules.setRowSorter(sorter);

		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent tme) {
				if (tme.getColumn() == 3) {
					int firstChangedRow = tme.getFirstRow();
					Rule r = (Rule)model.getValueAt(firstChangedRow, 0);
					boolean b = (Boolean)model.getValueAt(firstChangedRow, 3);
					r.setEnabled(b);
				}
			}
		});

		updateColWidthsFromSettingsManager();
		jtRules.getColumnModel().addColumnModelListener(new TableColumnModelListener() {

			@Override
			public void columnAdded(TableColumnModelEvent e) {
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
			}

			@Override
			public void columnMarginChanged(ChangeEvent e) {
				updateColWidthsToSettingsManager();
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
			}
		});
		jtRules.getTableHeader().setReorderingAllowed(false);
		jtRules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jtRules.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		jtRules.setRowHeight(TableTool.calculateRowHeight(jtRules, false, true));

		add(new JScrollPane(jtRules), BorderLayout.CENTER);

		btnNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (DownloadQueueManager.instance().isDownloading()) {
					JOptionPane.showMessageDialog(Main.instance(), Localization.getString("RuleChangeWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				Rule r = new Rule("", "0.1", "", "");
				RuleMainEditor rme = new RuleMainEditor(Main.instance(), r);
				if (rme.getCanceled()) {
					return;
				}
				hm.getHr().addRule(r);
				r.writeRule();
				model.addRow(r);
				sorter.sort();
			}
		});
		btnEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (DownloadQueueManager.instance().isDownloading()) {
					JOptionPane.showMessageDialog(Main.instance(), Localization.getString("RuleChangeWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (jtRules.getSelectedRows().length == 1) {
					int row = jtRules.getSelectedRow();
					if (row < 0) {
						return;
					}
					Rule r = (Rule)model.getValueAt(jtRules.convertRowIndexToModel(row), 0);
					RuleMainEditor rme = new RuleMainEditor(Main.instance(), r);
					if (rme.getCanceled()) {
						return;
					}
					r.writeRule();
					model.updateRow(row);
					sorter.sort();
				}
			}
		});
		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (DownloadQueueManager.instance().isDownloading()) {
					JOptionPane.showMessageDialog(Main.instance(), Localization.getString("RuleChangeWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (jtRules.getSelectedRows().length == 1) {
					int retval = JOptionPane.showConfirmDialog(Main.instance(), Localization.getString("ReallyDelete"), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (retval == JOptionPane.NO_OPTION) {
						return;
					}
					int row = jtRules.getSelectedRow();
					if (row < 0) {
						return;
					}

					int rowModelIndex = jtRules.convertRowIndexToModel(row);
					Rule r = (Rule)model.getValueAt(rowModelIndex, 0);
					File f = r.getFile();
					if (f.exists()) {
						try {
							Files.delete(f.toPath());
						} catch (IOException ex) {
							logger.error("Could not delete rule: {}", f.getAbsolutePath(), ex);
						}
					}
					model.removeRow(rowModelIndex);
					hm.getHr().removeRule(r);
				}
			}
		});
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ProgressObserver pg = new ProgressObserver();
				try {
					Main.instance().addProgressObserver(pg);
					pg.progressChanged(-1, -1, -1);
					pg.progressChanged(Localization.getString("SavingRules"));
					if (hm.getHr().saveAllRules()) {
						Main.instance().setMessage(Localization.getString("RulesSaved"));
					} else {
						Main.instance().setMessage(Localization.getString("RulesSaveFailed"));
					}
				} finally {
					Main.instance().removeProgressObserver(pg);
				}
			}
		});

		pnlButtons.add(btnNew);
		pnlButtons.add(btnEdit);
		pnlButtons.add(btnDelete);
		pnlButtons.add(btnSave);
		add(pnlButtons, BorderLayout.SOUTH);

		loadRules();
	}

	/**
	 * Load Rules
	 */
	private void loadRules() {
		List<Rule> rules = HostManager.instance().getHr().getRules();
		for (Rule rule : rules) {
			model.addRow(rule);
		}
		sorter.sort();
	}

	/**
	 * updateColWidthsToSettingsManager
	 */
	private void updateColWidthsToSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false) {
			return;
		}
		SettingsManager.instance().setColWidthsRules(TableTool.serializeColWidthSetting(jtRules));
		SettingsManager.instance().writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes()) {
			TableTool.applyColWidths(jtRules, SettingsManager.instance().getColWidthsRules());
		}
	}
}

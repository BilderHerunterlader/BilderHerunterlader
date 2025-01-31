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
import javax.swing.JFrame;
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
import org.xml.sax.SAXException;

import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.gui.renderer.RulesColorRowRenderer;
import ch.supertomcat.bh.gui.rules.editor.RuleMainEditor;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.RuleIO;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultBooleanColorRowRenderer;
import jakarta.xml.bind.JAXBException;

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
	private HostManager hostManager;

	/**
	 * Constructor
	 * 
	 * @param parentWindow Parent Window
	 * @param mainWindowAccess Main Window Access
	 * @param downloadQueueManager Download Queue Manager
	 * @param settingsManager Settings Manager
	 * @param hostManager Host Manager
	 */
	public Rules(JFrame parentWindow, MainWindowAccess mainWindowAccess, DownloadQueueManager downloadQueueManager, SettingsManager settingsManager, HostManager hostManager) {
		this.hostManager = hostManager;
		setLayout(new BorderLayout());

		jtRules = new JTable(model);

		TableUtil.internationalizeColumns(jtRules);

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

		updateColWidthsFromSettingsManager(settingsManager);
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
				updateColWidthsToSettingsManager(settingsManager);
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
			}
		});
		jtRules.getTableHeader().setReorderingAllowed(false);
		jtRules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jtRules.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		jtRules.setRowHeight(TableUtil.calculateRowHeight(jtRules, false, true));

		add(new JScrollPane(jtRules), BorderLayout.CENTER);

		btnNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (downloadQueueManager.isDownloading()) {
					JOptionPane.showMessageDialog(parentWindow, Localization.getString("RuleChangeWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				try {
					RuleIO ruleIO = new RuleIO();
					RuleDefinition ruleDefinition = ruleIO.readDefaultRule();
					Rule r = new Rule("", ruleDefinition, false);
					RuleMainEditor rme = new RuleMainEditor(parentWindow, r, settingsManager, hostManager.getHostRules().isDeveloperRulesEnabled());
					if (rme.isCanceled()) {
						return;
					}
					hostManager.getHostRules().addRule(r);
					hostManager.getHostRules().saveRule(r);
					model.addRow(r);
					sorter.sort();
				} catch (IOException | SAXException | JAXBException ex) {
					logger.error("Could not create new rule, ex");
					JOptionPane.showMessageDialog(parentWindow, "Could not create new rule", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (downloadQueueManager.isDownloading()) {
					JOptionPane.showMessageDialog(parentWindow, Localization.getString("RuleChangeWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (jtRules.getSelectedRows().length == 1) {
					int row = jtRules.getSelectedRow();
					if (row < 0) {
						return;
					}
					Rule r = (Rule)model.getValueAt(jtRules.convertRowIndexToModel(row), 0);
					RuleMainEditor rme = new RuleMainEditor(parentWindow, r, settingsManager, hostManager.getHostRules().isDeveloperRulesEnabled());
					if (rme.isCanceled()) {
						return;
					}
					hostManager.getHostRules().saveRule(r);
					model.updateRow(row);
					sorter.sort();
				}
			}
		});
		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (downloadQueueManager.isDownloading()) {
					JOptionPane.showMessageDialog(parentWindow, Localization.getString("RuleChangeWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (jtRules.getSelectedRows().length == 1) {
					int retval = JOptionPane.showConfirmDialog(parentWindow, Localization.getString("ReallyDelete"), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
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
					hostManager.getHostRules().removeRule(r);
				}
			}
		});
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ProgressObserver pg = new ProgressObserver();
				try {
					mainWindowAccess.addProgressObserver(pg);
					pg.progressChanged(-1, -1, -1);
					pg.progressChanged(Localization.getString("SavingRules"));
					if (hostManager.getHostRules().saveAllRules()) {
						mainWindowAccess.setMessage(Localization.getString("RulesSaved"));
					} else {
						mainWindowAccess.setMessage(Localization.getString("RulesSaveFailed"));
					}
				} finally {
					mainWindowAccess.removeProgressObserver(pg);
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
		List<Rule> rules = hostManager.getHostRules().getRules();
		for (Rule rule : rules) {
			model.addRow(rule);
		}
		sorter.sort();
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
		settingsManager.setColWidthsRules(TableUtil.serializeColWidthSetting(jtRules));
		settingsManager.writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 * 
	 * @param settingsManager Settings Manager
	 */
	private void updateColWidthsFromSettingsManager(SettingsManager settingsManager) {
		if (settingsManager.isSaveTableColumnSizes()) {
			TableUtil.applyColWidths(jtRules, settingsManager.getColWidthsRules());
		}
	}
}

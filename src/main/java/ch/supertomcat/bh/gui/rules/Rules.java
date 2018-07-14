package ch.supertomcat.bh.gui.rules;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

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
public class Rules extends JPanel implements ActionListener, TableColumnModelListener {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(Rules.class);

	/**
	 * UID
	 */
	private static final long serialVersionUID = 6224919821375411585L;

	/**
	 * Table
	 */
	private JTable jtRules;

	/**
	 * TabelModel
	 */
	private RulesTableModel model = new RulesTableModel();

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
		jtRules = new JTable(model);

		TableTool.internationalizeColumns(jtRules);

		jtRules.setDefaultRenderer(Object.class, new RulesColorRowRenderer());
		jtRules.setDefaultRenderer(Boolean.class, new DefaultBooleanColorRowRenderer());

		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent tme) {
				if (tme.getColumn() == 3) {
					int firstRow = tme.getFirstRow();
					Rule r = hm.getHr().getRule(firstRow);
					boolean b = (Boolean)model.getValueAt(firstRow, 3);
					r.setEnabled(b);
				}
			}
		});

		updateColWidthsFromSettingsManager();
		jtRules.getColumnModel().addColumnModelListener(this);
		jtRules.getTableHeader().setReorderingAllowed(false);
		jtRules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jtRules.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		jtRules.setRowHeight(TableTool.calculateRowHeight(jtRules, false, true));
		setLayout(new BorderLayout());

		JScrollPane jsp = new JScrollPane(jtRules);
		add(jsp, BorderLayout.CENTER);
		btnNew.addActionListener(this);
		btnEdit.addActionListener(this);
		btnDelete.addActionListener(this);
		btnSave.addActionListener(this);
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
			int mode = 0;
			if (rule.getPipelines().size() > 0) {
				mode = rule.getPipelines().get(0).getMode();
			}
			model.addRow(rule.getName(), rule.getVersion(), mode, rule.getPipelines().size(), rule.isRedirect(), rule.isEnabled());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnNew) {
			if (DownloadQueueManager.instance().isDownloading()) {
				JOptionPane.showMessageDialog(Main.instance(), Localization.getString("RuleChangeWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			Rule r = new Rule("", "0.1", "", "");
			RuleMainEditor rme = new RuleMainEditor(Main.instance(), r);
			if (rme.getCanceled())
				return;
			hm.getHr().addRule(r);
			File f = r.getFile();
			try {
				f.createNewFile();
				f = null;
			} catch (IOException ex) {
				logger.error(ex.getMessage(), ex);
			}
			f = null;
			r.writeRule();

			int mode = 0;
			if (r.getPipelines().size() > 0) {
				mode = r.getPipelines().get(0).getMode();
			}
			model.addRow(r.getName(), r.getVersion(), mode, r.getPipelines().size(), r.isRedirect(), r.isEnabled());
		} else if (e.getSource() == btnEdit) {
			if (DownloadQueueManager.instance().isDownloading()) {
				JOptionPane.showMessageDialog(Main.instance(), Localization.getString("RuleChangeWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (jtRules.getSelectedRows().length == 1) {
				int row = jtRules.getSelectedRow();
				if (row < 0)
					return;
				Rule r = hm.getHr().getRule(row);
				RuleMainEditor rme = new RuleMainEditor(Main.instance(), r);
				if (rme.getCanceled())
					return;
				model.setValueAt(r.getName(), row, 0);
				model.setValueAt(r.getVersion(), row, 1);

				int mode = 0;
				if (r.getPipelines().size() > 0) {
					mode = r.getPipelines().get(0).getMode();
				}
				String strPipeCount = "";
				if (r.getPipelines().size() > 1) {
					strPipeCount = " + " + (r.getPipelines().size() - 1) + " Pipelines";
				}
				String strRedirect = (r.isRedirect() == true) ? "Redirect | " : "";
				if (r.getPipelines().size() > 0) {
					if (mode == 0) {
						model.setValueAt(strRedirect + Localization.getString("RuleModeZeroShort") + strPipeCount, row, 2);
					} else {
						model.setValueAt(strRedirect + Localization.getString("RuleModeOneShort") + strPipeCount, row, 2);
					}
				} else {
					model.setValueAt(strRedirect + "0 Pipelines", row, 2);
				}
				r.writeRule();
			}
		} else if (e.getSource() == btnDelete) {
			if (DownloadQueueManager.instance().isDownloading()) {
				JOptionPane.showMessageDialog(Main.instance(), Localization.getString("RuleChangeWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			int retval = JOptionPane.showConfirmDialog(Main.instance(), Localization.getString("ReallyDelete"), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (retval == JOptionPane.NO_OPTION) {
				return;
			}
			int s[] = jtRules.getSelectedRows();
			for (int i = s.length - 1; i > -1; i--) {
				Rule r = hm.getHr().getRule(s[i]);
				File f = r.getFile();
				if (f.exists()) {
					f.delete();
				}
				f = null;
				model.removeRow(s[i]);
				hm.getHr().removeRule(s[i]);
			}
		} else if (e.getSource() == btnSave) {
			ProgressObserver pg = new ProgressObserver();
			Main.instance().addProgressObserver(pg);
			pg.progressChanged(-1, -1, -1);
			pg.progressChanged(Localization.getString("SavingRules"));
			hm.getHr().saveAllRules();
			Main.instance().removeProgressObserver(pg);
			Main.instance().setMessage(Localization.getString("RulesSaved"));
		}
	}

	/**
	 * updateColWidthsToSettingsManager
	 */
	private void updateColWidthsToSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false)
			return;
		SettingsManager.instance().setColWidthsRules(TableTool.serializeColWidthSetting(jtRules));
		SettingsManager.instance().writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false)
			return;
		TableTool.applyColWidths(jtRules, SettingsManager.instance().getColWidthsRules());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TableColumnModelListener#columnAdded(javax.swing.event.TableColumnModelEvent)
	 */
	@Override
	public void columnAdded(TableColumnModelEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TableColumnModelListener#columnMarginChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void columnMarginChanged(ChangeEvent e) {
		updateColWidthsToSettingsManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TableColumnModelListener#columnMoved(javax.swing.event.TableColumnModelEvent)
	 */
	@Override
	public void columnMoved(TableColumnModelEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TableColumnModelListener#columnRemoved(javax.swing.event.TableColumnModelEvent)
	 */
	@Override
	public void columnRemoved(TableColumnModelEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TableColumnModelListener#columnSelectionChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void columnSelectionChanged(ListSelectionEvent e) {
	}
}

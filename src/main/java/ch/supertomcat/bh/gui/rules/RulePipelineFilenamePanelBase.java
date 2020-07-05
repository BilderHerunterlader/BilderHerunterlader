package ch.supertomcat.bh.gui.rules;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.gui.SpringUtilities;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.RulePipeline;
import ch.supertomcat.bh.rules.RuleRegExp;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;

/**
 * Base class for Rule-Pipeline-Panels
 * 
 * @param <T> Pipeline Type
 * @param <S> Filename Mode Type
 */
public abstract class RulePipelineFilenamePanelBase<T extends RulePipeline<?>, S extends Enum<S>> extends JPanel implements ActionListener, ItemListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Table
	 */
	protected JTable table;

	/**
	 * ScrollPane
	 */
	protected JScrollPane sp;

	/**
	 * TableModel
	 */
	protected RulePipelineTableModel model = new RulePipelineTableModel();

	/**
	 * Rule
	 */
	protected Rule rule = null;

	/**
	 * CheckBox
	 */
	protected JCheckBox chkUseContentDisposition = new JCheckBox(Localization.getString("UseContentDisposition"));

	/**
	 * CheckBox
	 */
	protected JCheckBox chkReducePathLength = new JCheckBox(Localization.getString("ReducePathLength"), true);

	/**
	 * CheckBox
	 */
	protected JCheckBox chkReduceFilenameLength = new JCheckBox(Localization.getString("ReduceFilenameLength"), true);

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
	 * Button
	 */
	protected JButton btnNew = new JButton(Localization.getString("New"), Icons.getTangoIcon("actions/document-new.png", 16));

	/**
	 * Button
	 */
	protected JButton btnEdit = new JButton(Localization.getString("Edit"), Icons.getTangoIcon("apps/accessories-text-editor.png", 16));

	/**
	 * Button
	 */
	protected JButton btnUp = new JButton(Localization.getString("Up"), Icons.getTangoIcon("actions/go-up.png", 16));

	/**
	 * Button
	 */
	protected JButton btnDown = new JButton(Localization.getString("Down"), Icons.getTangoIcon("actions/go-down.png", 16));

	/**
	 * Button
	 */
	protected JButton btnDelete = new JButton(Localization.getString("Delete"), Icons.getTangoIcon("actions/edit-delete.png", 16));

	/**
	 * Panel
	 */
	protected JPanel pnlButtons = new JPanel();

	/**
	 * Owner
	 */
	protected JDialog owner = null;

	/**
	 * RulePipeline
	 */
	protected T pipe = null;

	/**
	 * Constructor
	 * 
	 * @param rule Rule
	 * @param pipe Pipe
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 */
	public RulePipelineFilenamePanelBase(Rule rule, T pipe, JDialog owner, SettingsManager settingsManager) {
		this.owner = owner;
		this.rule = rule;
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
		table.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		table.setRowHeight(TableUtil.calculateRowHeight(table, false, true));

		Iterator<RuleRegExp> it = pipe.getRegexps().iterator();
		while (it.hasNext()) {
			RuleRegExp rre = it.next();
			model.addRow(rre.getSearch(), rre.getReplace());
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
		chkUseContentDisposition.addItemListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnNew) {
			RuleRegExp rre = new RuleRegExp();
			RuleRegexpEditor rme = new RuleRegexpEditor(owner, rre);
			if (rme.getCanceled()) {
				return;
			}
			model.addRow(rre.getSearch(), rre.getReplace());
			pipe.addRegExp(rre);
		} else if (e.getSource() == btnEdit) {
			int row = table.getSelectedRow();
			if (row < 0) {
				return;
			}
			RuleRegExp rre = pipe.getRegexp(row);
			RuleRegexpEditor rme = new RuleRegexpEditor(owner, rre);
			if (rme.getCanceled()) {
				return;
			}
			model.setValueAt(rre.getSearch(), row, 0);
			model.setValueAt(rre.getReplace(), row, 1);
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
	public abstract void apply();

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == chkUseContentDisposition) {
			table.setEnabled(!chkUseContentDisposition.isSelected());
			btnNew.setEnabled(!chkUseContentDisposition.isSelected());
			btnEdit.setEnabled(!chkUseContentDisposition.isSelected());
			btnUp.setEnabled(!chkUseContentDisposition.isSelected());
			btnDown.setEnabled(!chkUseContentDisposition.isSelected());
			btnDelete.setEnabled(!chkUseContentDisposition.isSelected());
			lblSource.setEnabled(!chkUseContentDisposition.isSelected());
		}
	}

	/**
	 * updateColWidthsToSettingsManager
	 * 
	 * @param settingsManager SettingsManager
	 */
	protected void updateColWidthsToSettingsManager(SettingsManager settingsManager) {
		if (settingsManager.isSaveTableColumnSizes() == false) {
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
	protected void updateColWidthsFromSettingsManager(SettingsManager settingsManager) {
		if (settingsManager.isSaveTableColumnSizes() == false) {
			return;
		}
		TableUtil.applyColWidths(table, settingsManager.getColWidthsRulesEditor());
	}

	/**
	 * @param enabled Enabled
	 */
	public void redirectEnabled(boolean enabled) {
		table.setEnabled(!enabled);
		chkUseContentDisposition.setEnabled(!enabled);
		cbFilenameMode.setEnabled(!enabled);
		lblSource.setEnabled(!enabled);
		btnNew.setEnabled(!enabled);
		btnEdit.setEnabled(!enabled);
		btnUp.setEnabled(!enabled);
		btnDown.setEnabled(!enabled);
		btnDelete.setEnabled(!enabled);

		if (!enabled) {
			table.setEnabled(!chkUseContentDisposition.isSelected());
			btnNew.setEnabled(!chkUseContentDisposition.isSelected());
			btnEdit.setEnabled(!chkUseContentDisposition.isSelected());
			btnUp.setEnabled(!chkUseContentDisposition.isSelected());
			btnDown.setEnabled(!chkUseContentDisposition.isSelected());
			btnDelete.setEnabled(!chkUseContentDisposition.isSelected());
			lblSource.setEnabled(!chkUseContentDisposition.isSelected());
		}
	}
}

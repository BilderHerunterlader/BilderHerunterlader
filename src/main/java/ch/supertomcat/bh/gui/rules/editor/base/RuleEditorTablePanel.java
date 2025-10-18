package ch.supertomcat.bh.gui.rules.editor.base;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;

/**
 * Panel with buttons to edit a table
 * 
 * @param <T> Table Model Type
 */
public class RuleEditorTablePanel<T extends DefaultTableModel> extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * Table Model
	 */
	protected final T model;

	/**
	 * Table
	 */
	protected final JTable table;

	/**
	 * ScrollPane
	 */
	protected final JScrollPane scrollPane;

	/**
	 * Supplier for action new
	 */
	protected final Supplier<Object[]> actionNewSupplier;

	/**
	 * Function for action edit
	 */
	protected final Function<Object[], Object[]> actionEditFunction;

	/**
	 * Button Panel
	 */
	protected final RuleEditorDefaultButtonPanel buttonPanel;

	/**
	 * Constructor
	 * 
	 * @param model Table Model
	 * @param actionNewSupplier Supplier for action new
	 * @param actionEditFunction Function for action edit
	 */
	public RuleEditorTablePanel(T model, Supplier<Object[]> actionNewSupplier, UnaryOperator<Object[]> actionEditFunction) {
		this.model = model;
		this.actionNewSupplier = actionNewSupplier;
		this.actionEditFunction = actionEditFunction;
		this.table = new JTable(model);
		this.scrollPane = new JScrollPane(table);
		this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableUtil.internationalizeColumns(table);

		table.setDefaultRenderer(Object.class, new DefaultStringColorRowRenderer());
		table.getTableHeader().setReorderingAllowed(false);
		table.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		table.setRowHeight(TableUtil.calculateRowHeight(table, false, true));

		Dimension preferredScrollableTableSize = new Dimension(table.getPreferredScrollableViewportSize().width, 15 * table.getRowHeight());
		table.setPreferredScrollableViewportSize(preferredScrollableTableSize);

		this.buttonPanel = new RuleEditorDefaultButtonPanel(true);
		buttonPanel.addNewActionListener(e -> actionNew());
		buttonPanel.addEditActionListener(e -> actionEdit());
		buttonPanel.addUpActionListener(e -> actionUp());
		buttonPanel.addDownActionListener(e -> actionDown());
		buttonPanel.addDeleteActionListener(e -> actionDelete());

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.EAST);
	}

	/**
	 * Returns the table
	 * 
	 * @return table
	 */
	public JTable getTable() {
		return table;
	}

	private void actionNew() {
		Object[] rowData = actionNewSupplier.get();
		if (rowData != null) {
			model.addRow(rowData);
		}
	}

	private void actionEdit() {
		int row = table.getSelectedRow();
		if (row < 0) {
			return;
		}

		int modelRow = table.convertRowIndexToModel(row);

		Object[] originalRowData = ((Vector<?>)model.getDataVector().elementAt(modelRow)).toArray();
		Object[] rowData = actionEditFunction.apply(originalRowData);
		if (rowData == null) {
			return;
		}
		int columnIndex = 0;
		for (Object obj : rowData) {
			model.setValueAt(obj, modelRow, columnIndex);
			columnIndex++;
		}
	}

	private void actionUp() {
		int row = table.getSelectedRow();
		if (row < 0) {
			return;
		}

		int modelRow = table.convertRowIndexToModel(row);
		if (modelRow > 0) {
			model.moveRow(modelRow, modelRow, modelRow - 1);
			table.setRowSelectionInterval(modelRow - 1, modelRow - 1);
		}
	}

	private void actionDown() {
		int row = table.getSelectedRow();
		if (row < 0) {
			return;
		}

		int modelRow = table.convertRowIndexToModel(row);
		if (modelRow > -1 && modelRow < model.getRowCount() - 1) {
			model.moveRow(modelRow, modelRow, modelRow + 1);
			table.setRowSelectionInterval(modelRow + 1, modelRow + 1);
		}
	}

	private void actionDelete() {
		int row = table.getSelectedRow();
		if (row < 0) {
			return;
		}

		int modelRow = table.convertRowIndexToModel(row);
		model.removeRow(modelRow);
	}

	/**
	 * Enable Table
	 */
	public void enableTable() {
		table.setEnabled(true);
		buttonPanel.enableButtons();
	}

	/**
	 * Disable Table
	 */
	public void disableTable() {
		table.setEnabled(false);
		buttonPanel.disableButtons();
	}
}

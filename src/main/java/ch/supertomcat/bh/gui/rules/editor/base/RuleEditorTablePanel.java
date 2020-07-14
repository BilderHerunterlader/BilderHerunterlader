package ch.supertomcat.bh.gui.rules.editor.base;

import java.awt.event.ActionEvent;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

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
	protected final RuleEditorButtonPanel buttonPanel;

	/**
	 * Constructor
	 * 
	 * @param model Table Model
	 * @param actionNewSupplier Supplier for action new
	 * @param actionEditFunction Function for action edit
	 */
	public RuleEditorTablePanel(T model, Supplier<Object[]> actionNewSupplier, Function<Object[], Object[]> actionEditFunction) {
		this.model = model;
		this.actionNewSupplier = actionNewSupplier;
		this.actionEditFunction = actionEditFunction;
		this.table = new JTable(model);
		this.scrollPane = new JScrollPane(this.table);
		this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.buttonPanel = new RuleEditorButtonPanel(true) {

			/**
			 * serialVersionUID
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void actionNew(ActionEvent e) {
				RuleEditorTablePanel.this.actionNew();
			}

			@Override
			protected void actionEdit(ActionEvent e) {
				RuleEditorTablePanel.this.actionEdit();
			}

			@Override
			protected void actionUp(ActionEvent e) {
				RuleEditorTablePanel.this.actionUp();
			}

			@Override
			protected void actionDown(ActionEvent e) {
				RuleEditorTablePanel.this.actionDown();
			}

			@Override
			protected void actionDelete(ActionEvent e) {
				RuleEditorTablePanel.this.actionDelete();
			}
		};
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
}

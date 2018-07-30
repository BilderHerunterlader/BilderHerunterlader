package ch.supertomcat.bh.gui.keywords;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * Editor for Keywords
 */
public class KeywordCellEditor extends AbstractCellEditor implements TableCellEditor {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Editor-Component
	 */
	private KeywordCellEditorComponent kcec = new KeywordCellEditorComponent();

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		kcec.setText(value.toString());
		kcec.setRelative(table.convertColumnIndexToModel(column) == 3);
		return kcec;
	}

	@Override
	public Object getCellEditorValue() {
		return kcec.toString();
	}
}

package ch.supertomcat.bh.gui.settings;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * Editor for Subdirs (Filesize)
 */
public class SubdirCellEditor extends AbstractCellEditor implements TableCellEditor {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Editor-Component
	 */
	private SubdirCellEditorComponent kcec = new SubdirCellEditorComponent();

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		kcec.setText(value.toString());
		return kcec;
	}

	@Override
	public Object getCellEditorValue() {
		return kcec.getLongVal();
	}
}

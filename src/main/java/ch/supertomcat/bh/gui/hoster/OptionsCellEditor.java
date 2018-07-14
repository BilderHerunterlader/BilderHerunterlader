package ch.supertomcat.bh.gui.hoster;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * Editor for hostclasses buttons
 */
public class OptionsCellEditor extends AbstractCellEditor implements TableCellEditor {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * TableModel
	 */
	private HosterTableModel htm = null;

	/**
	 * ComboBox
	 */
	private JPanel pnl = null;

	/**
	 * Constructor
	 * 
	 * @param htm TableModel
	 */
	public OptionsCellEditor(HosterTableModel htm) {
		this.htm = htm;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		pnl = htm.getOptionPanel(row);
		return pnl;
	}

	@Override
	public Object getCellEditorValue() {
		if (pnl == null)
			return "";
		return pnl;
	}
}

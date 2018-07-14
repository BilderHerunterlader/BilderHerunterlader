package ch.supertomcat.bh.gui.adder;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * Editor for Keywords
 */
public class AdderKeywordSelectorFilenameComboBoxCellEditor extends AbstractCellEditor implements TableCellEditor {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * AdderKeywordSelectorFilename
	 */
	private AdderKeywordSelectorFilename aksf = null;

	/**
	 * ComboBox
	 */
	private JComboBox<String> cb = null;

	/**
	 * Constructor
	 * 
	 * @param aksf AdderKeywordSelectorFilename
	 */
	public AdderKeywordSelectorFilenameComboBoxCellEditor(AdderKeywordSelectorFilename aksf) {
		this.aksf = aksf;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		cb = aksf.getComboBox(row);
		return cb;
	}

	@Override
	public Object getCellEditorValue() {
		return cb;
	}
}

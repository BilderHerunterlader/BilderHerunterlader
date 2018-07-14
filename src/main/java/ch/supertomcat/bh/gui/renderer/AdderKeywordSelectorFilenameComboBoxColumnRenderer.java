package ch.supertomcat.bh.gui.renderer;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * AdderKeywordSelectorFilenameComboBoxColumnRenderer
 */
public class AdderKeywordSelectorFilenameComboBoxColumnRenderer implements TableCellRenderer {
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		return (JComboBox<?>)value;
	}
}

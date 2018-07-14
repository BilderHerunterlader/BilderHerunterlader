package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.supertomcattools.guitools.tablerenderer.DefaultStringColorRowRenderer;

/**
 * HosterColorRowRenderer
 */
public class HosterColorRowRenderer extends DefaultStringColorRowRenderer implements TableCellRenderer {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 9050984401350807572L;
	
	/**
	 * Sets the Foreground-Color of the component
	 * @param comp The Component
	 * @param table The Table
	 * @param value The Value
	 * @param isSelected Is the cell selected
	 * @param hasFocus Has the cell the focus
	 * @param row Index of the row
	 * @param column Index of the Column
	 */
	@Override
	public void prepareForegroundColor(Component comp, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Color cf = table.getForeground();
		if (isSelected) {
			cf = table.getSelectionForeground();
		}
		if (value instanceof String) {
			String str = (String)value;
			if (str.startsWith("Error:")) {
				cf = Color.RED;
			} else if (str.startsWith("Developer:")) {
				cf = Color.BLUE;
			} else if (str.startsWith("Redirect:")) {
				cf = Color.RED;
			}
		}
		comp.setForeground(cf);
	}
}

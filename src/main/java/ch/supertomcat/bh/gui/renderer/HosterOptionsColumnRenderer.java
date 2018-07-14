package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer to display option buttons for hostclasses
 */
public class HosterOptionsColumnRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -6810509128294034334L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component comp = null;
		if (value instanceof JPanel) {
			comp = (JPanel)value;
		} else {
			comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
		prepareBackgroundColor(comp, table, value, isSelected, hasFocus, row, column);
		prepareForegroundColor(comp, table, value, isSelected, hasFocus, row, column);
		return comp;
	}

	/**
	 * Sets the Background-Color of the component.
	 * The rows are getting alternate background-colors.
	 * Which color a cell in a row gets is determent by the index of the row.
	 * If the index is even then the cell will be white if not the cell will have a
	 * different color.
	 * 
	 * @param comp The Component
	 * @param table The Table
	 * @param value The Value
	 * @param isSelected Is the cell selected
	 * @param hasFocus Has the cell the focus
	 * @param row Index of the row
	 * @param column Index of the Column
	 */
	public void prepareBackgroundColor(Component comp, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Color c = Color.WHITE;
		if ((row % 2) != 0) {
			c = Color.decode("#F0F8FF");
		}
		if (isSelected) {
			c = table.getSelectionBackground();
		}
		comp.setBackground(c);
	}

	/**
	 * Sets the Foreground-Color of the component
	 * 
	 * @param comp The Component
	 * @param table The Table
	 * @param value The Value
	 * @param isSelected Is the cell selected
	 * @param hasFocus Has the cell the focus
	 * @param row Index of the row
	 * @param column Index of the Column
	 */
	public void prepareForegroundColor(Component comp, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Color cf = table.getForeground();
		if (isSelected) {
			cf = table.getSelectionForeground();
		}
		comp.setForeground(cf);
	}
}

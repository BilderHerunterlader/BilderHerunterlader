package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.bh.gui.BHIcons;
import ch.supertomcat.bh.update.containers.UpdateObject;
import ch.supertomcat.supertomcatutils.gui.Icons;

/**
 * Renderer to display icons for the update action type
 */
public class UpdateActionColumnRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		UpdateObject.UpdateActionType action = UpdateObject.UpdateActionType.ACTION_NONE;
		if (value instanceof UpdateObject.UpdateActionType) {
			action = (UpdateObject.UpdateActionType)value;
		}
		Component comp = null;
		if (value instanceof JLabel) {
			comp = (JLabel)value;
		} else {
			comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
		if (comp instanceof JLabel) {
			JLabel label = (JLabel)comp;
			label.setText("");
			label.setOpaque(true);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			switch (action) {
				case ACTION_NONE:
					label.setIcon(BHIcons.getBHIcon("Dummy16.png"));
					break;
				case ACTION_NEW:
					label.setIcon(Icons.getTangoIcon("actions/list-add.png", 16));
					break;
				case ACTION_UPDATE:
					label.setIcon(Icons.getTangoIcon("apps/system-software-update.png", 16));
					break;
				case ACTION_REMOVE:
					label.setIcon(Icons.getTangoIcon("actions/list-remove.png", 16));
					break;
				default:
					label.setIcon(BHIcons.getBHIcon("Dummy16.png"));
					break;
			}
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
	protected void prepareBackgroundColor(Component comp, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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
	protected void prepareForegroundColor(Component comp, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Color cf = table.getForeground();
		if (isSelected) {
			cf = table.getSelectionForeground();
		}
		comp.setForeground(cf);
	}
}

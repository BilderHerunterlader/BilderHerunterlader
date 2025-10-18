package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.bh.update.containers.UpdateObject;
import ch.supertomcat.supertomcatutils.gui.Icons;

/**
 * Renderer to display icons for the update action type
 */
public class UpdateActionColumnRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	private static final ImageIcon ACTION_NONE_ICON = Icons.getApplIcon("dummy.png", 16);

	private static final ImageIcon ACTION_NEW_ICON = Icons.getTangoSVGIcon("actions/list-add.svg", 16);

	private static final ImageIcon ACTION_UPDATE_ICON = Icons.getTangoSVGIcon("apps/system-software-update.svg", 16);

	private static final ImageIcon ACTION_REMOVE_ICON = Icons.getTangoSVGIcon("actions/list-remove.svg", 16);

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		UpdateObject.UpdateActionType action = UpdateObject.UpdateActionType.ACTION_NONE;
		if (value instanceof UpdateObject.UpdateActionType updateActionType) {
			action = updateActionType;
		}
		Component comp = null;
		if (value instanceof JLabel lbl) {
			comp = lbl;
		} else {
			comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
		if (comp instanceof JLabel lbl) {
			JLabel label = lbl;
			label.setText("");
			label.setOpaque(true);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			switch (action) {
				case ACTION_NEW:
					label.setIcon(ACTION_NEW_ICON);
					break;
				case ACTION_UPDATE:
					label.setIcon(ACTION_UPDATE_ICON);
					break;
				case ACTION_REMOVE:
					label.setIcon(ACTION_REMOVE_ICON);
					break;
				case ACTION_NONE:
				default:
					label.setIcon(ACTION_NONE_ICON);
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

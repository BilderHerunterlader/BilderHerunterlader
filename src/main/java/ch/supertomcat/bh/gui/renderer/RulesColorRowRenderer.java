package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;

/**
 * RulesColorRowRenderer
 */
public class RulesColorRowRenderer extends DefaultStringColorRowRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	private static final Color REDIRECT_COLOR = Color.decode("#FF4500");

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
	@Override
	public void prepareForegroundColor(Component comp, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Color cf;
		if (isSelected) {
			cf = table.getSelectionForeground();
		} else {
			Rule r = (Rule)table.getModel().getValueAt(table.convertRowIndexToModel(row), 0);
			if (r.isDeveloper()) {
				cf = Color.BLUE;
			} else if (r.isRedirect()) {
				cf = REDIRECT_COLOR;
			} else {
				cf = table.getForeground();
			}
		}
		comp.setForeground(cf);
	}
}

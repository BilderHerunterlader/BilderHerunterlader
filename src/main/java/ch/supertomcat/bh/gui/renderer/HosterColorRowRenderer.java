package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IRedirect;
import ch.supertomcat.supertomcattools.guitools.tablerenderer.DefaultStringColorRowRenderer;

/**
 * HosterColorRowRenderer
 */
public class HosterColorRowRenderer extends DefaultStringColorRowRenderer implements TableCellRenderer {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 9050984401350807572L;

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
			if (value instanceof Host && ((Host)value).isDeveloper()) {
				cf = Color.BLUE;
			} else if (value instanceof IRedirect) {
				cf = REDIRECT_COLOR;
			} else {
				cf = table.getForeground();
			}
		}
		comp.setForeground(cf);
	}
}

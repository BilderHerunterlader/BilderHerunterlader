package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.supertomcattools.guitools.tablerenderer.DefaultStringColorRowRenderer;


/**
 * QueueColorRowRenderer
 */
public class QueueColorRowRenderer extends DefaultStringColorRowRenderer implements TableCellRenderer {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -6810509128294034334L;

	/* (non-Javadoc)
	 * @see ch.supertomcat.bh.gui.renderer.DefaultColorRowRenderer#prepareForegroundColor(java.awt.Component, javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public void prepareForegroundColor(Component comp, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Color cf = table.getForeground();
		if (isSelected) {
			cf = table.getSelectionForeground();
		}
		
		Object progressValue = table.getValueAt(row,  3);
		if (progressValue instanceof Pic) {
			Pic p = (Pic)table.getValueAt(row, 3);
			if (p.isDeactivated()) {
				if (isSelected) {
					comp.setForeground(Color.BLACK);
				} else {
					comp.setForeground(Color.RED);
				}
			} else {
				comp.setForeground(cf);
			}
		} else {
			comp.setForeground(cf);
		}
	}
}
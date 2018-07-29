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
	private static final long serialVersionUID = 1L;

	@Override
	public void prepareForegroundColor(Component comp, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Color cf;
		if (isSelected) {
			cf = table.getSelectionForeground();
		} else {
			Object progressValue = table.getModel().getValueAt(table.convertRowIndexToModel(row), 3);
			if (progressValue instanceof Pic && ((Pic)progressValue).isDeactivated()) {
				cf = Color.RED;
			} else {
				cf = table.getForeground();
			}
		}
		comp.setForeground(cf);
	}
}

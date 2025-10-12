package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IRedirect;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;

/**
 * HosterColorRowRenderer
 */
public class HosterColorRowRenderer extends DefaultStringColorRowRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	private static final Color REDIRECT_COLOR = Color.decode("#FF4500");

	@Override
	public void prepareForegroundColor(Component comp, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Color cf;
		if (isSelected) {
			cf = table.getSelectionForeground();
		} else {
			if (value instanceof Host host && host.isDeveloper()) {
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

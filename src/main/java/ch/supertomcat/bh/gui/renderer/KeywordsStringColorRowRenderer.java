package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;

/**
 * TableCellRenderer for alternate coloring.
 * Some Tables in BH are using a extended class of this class.
 * The renderer returns a JLabel.
 */
public class KeywordsStringColorRowRenderer extends DefaultStringColorRowRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public void prepareForegroundColor(Component comp, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		super.prepareForegroundColor(comp, table, value, isSelected, hasFocus, row, column);
		int modelColumnIndex = table.convertColumnIndexToModel(column);
		if (modelColumnIndex == 2 || modelColumnIndex == 3) {
			boolean rpath = (Boolean)table.getModel().getValueAt(table.convertRowIndexToModel(row), 4);
			if (rpath && modelColumnIndex == 2 || !rpath && modelColumnIndex == 3) {
				comp.setForeground(Color.decode("#C0C0C0"));
			}
		}
	}
}

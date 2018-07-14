package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.supertomcattools.guitools.tablerenderer.DefaultStringColorRowRenderer;

/**
 * TableCellRenderer for alternate coloring.
 * Some Tables in BH are using a extended class of this class.
 * The renderer returns a JLabel.
 */
public class KeywordsStringColorRowRenderer extends DefaultStringColorRowRenderer implements TableCellRenderer {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -6810509128294034334L;

	/* (non-Javadoc)
	 * @see ch.supertomcat.bh.gui.renderer.DefaultStringColorRowRenderer#prepareForegroundColor(java.awt.Component, javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public void prepareForegroundColor(Component comp, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		super.prepareForegroundColor(comp, table, value, isSelected, hasFocus, row, column);
		if (column == 2 || column == 3) {
			boolean rpath = (Boolean)table.getValueAt(row, 4);
			if (rpath == true && column == 2) {
				comp.setForeground(Color.decode("#C0C0C0"));
			} else if (rpath == false && column == 3) {
				comp.setForeground(Color.decode("#C0C0C0"));
			}
		}
	}
}
package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * AdderColorRowRenderer
 */
public class AdderColorRowRenderer extends JLabel implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	private static final Color KEYWORD_FOUND_COLOR = new Color(0x006400);

	private static final Color NOT_ALREADY_DOWNLOADED_COLOR = Color.BLUE;

	private static final Color ALREADY_DOWNLOADED_COLOR = Color.RED;

	/**
	 * Constructor
	 */
	public AdderColorRowRenderer() {
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		TableModel model = table.getModel();
		Color fc;
		if (isSelected) {
			fc = table.getSelectionForeground();
		} else {
			int modelRowIndex = table.convertRowIndexToModel(row);

			if (Boolean.TRUE.equals(model.getValueAt(modelRowIndex, table.getColumn("AlreadyDownloaded").getModelIndex()))) {
				fc = ALREADY_DOWNLOADED_COLOR;
			} else {
				fc = NOT_ALREADY_DOWNLOADED_COLOR;
			}

			if (table.convertColumnIndexToModel(column) == 3) {
				boolean keywordFound = model.getValueAt(modelRowIndex, table.getColumn("Keyword").getModelIndex()) != null;
				if (keywordFound) {
					fc = KEYWORD_FOUND_COLOR;
				}
			}
		}
		setForeground(fc);

		prepareBackgroundColor(this, table, value, isSelected, hasFocus, row, column);
		this.setOpaque(true);
		setIcon(null);
		if (value != null) {
			if (value instanceof ImageIcon) {
				setIcon((ImageIcon)value);
				setText("");
				setToolTipText("");
			} else {
				setText(value.toString());
				setToolTipText(value.toString());
			}
		} else {
			setText("");
			setToolTipText("");
		}
		return this;
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
		Color c;
		if (isSelected) {
			c = table.getSelectionBackground();
		} else {
			if ((row % 2) != 0) {
				c = Color.decode("#F0F8FF");
			} else {
				c = table.getBackground();
			}
		}
		comp.setBackground(c);
	}
}

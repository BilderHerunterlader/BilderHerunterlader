package ch.supertomcat.bh.gui.renderer;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.bh.pic.Pic;

/**
 * QueueProgressColumnRenderer
 */
public class QueueProgressColumnRenderer extends QueueColorRowRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof JProgressBar) {
			JProgressBar pg = (JProgressBar)value;
			prepareBackgroundColor(pg, table, value, isSelected, hasFocus, row, column);
			return pg;
		} else if (value instanceof Pic) {
			Pic pic = (Pic)value;
			prepareForegroundColor(this, table, value, isSelected, hasFocus, row, column);
			prepareBackgroundColor(this, table, value, isSelected, hasFocus, row, column);

			this.setText(pic.getStatusText());

			String errMsg = pic.getErrMsg();
			if (errMsg != null && !errMsg.isEmpty()) {
				this.setToolTipText(errMsg);
			} else {
				this.setToolTipText(pic.getStatusText());
			}

			this.setOpaque(true);
			return this;
		} else {
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}
}

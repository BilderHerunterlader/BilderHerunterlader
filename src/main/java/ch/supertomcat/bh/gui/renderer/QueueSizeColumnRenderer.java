package ch.supertomcat.bh.gui.renderer;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.formatter.UnitFormatUtil;

/**
 * Queue Size Column Renderer
 */
public class QueueSizeColumnRenderer extends QueueColorRowRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 */
	public QueueSizeColumnRenderer(SettingsManager settingsManager) {
		super(settingsManager);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof Long) {
			long size = (Long)value;

			String formattedSize;
			if (size <= 0) {
				formattedSize = Localization.getString("Unkown");
			} else {
				formattedSize = UnitFormatUtil.getSizeString(size, settingsManager.getSizeView());
			}

			return super.getTableCellRendererComponent(table, formattedSize, isSelected, hasFocus, row, column);
		} else {
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}
}

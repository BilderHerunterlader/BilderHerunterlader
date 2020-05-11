package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.PicState;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;

/**
 * QueueColorRowRenderer
 */
public class QueueColorRowRenderer extends DefaultStringColorRowRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	/**
	 * Failed Pic States
	 */
	private static final Set<PicState> FAILED_PIC_STATES = new HashSet<>(Arrays.asList(PicState.FAILED, PicState.FAILED_FILE_NOT_EXIST, PicState.FAILED_FILE_TEMPORARY_OFFLINE));

	/**
	 * Settings Manager
	 */
	protected final SettingsManager settingsManager;

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 */
	public QueueColorRowRenderer(SettingsManager settingsManager) {
		this.settingsManager = settingsManager;
	}

	@Override
	public void prepareForegroundColor(Component comp, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (isSelected) {
			super.prepareForegroundColor(comp, table, value, isSelected, hasFocus, row, column);
		} else {
			boolean progressColumn = table.convertColumnIndexToModel(column) == 3;
			Object progressValue = table.getModel().getValueAt(table.convertRowIndexToModel(row), 3);
			if (progressValue instanceof Pic && ((Pic)progressValue).isDeactivated() && (!progressColumn || !FAILED_PIC_STATES.contains(((Pic)progressValue).getStatus()))) {
				comp.setForeground(Color.RED);
			} else {
				super.prepareForegroundColor(comp, table, value, isSelected, hasFocus, row, column);
			}
		}
	}
}

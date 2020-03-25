package ch.supertomcat.bh.gui.queue;

import javax.swing.table.DefaultTableModel;

import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.formatter.UnitFormatUtil;

/**
 * TableModel for Queue
 */
public class QueueTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public QueueTableModel() {
		this.addColumn("URL");
		this.addColumn("Target");
		this.addColumn("Size");
		this.addColumn("Progress");
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Adds a row
	 * 
	 * @param pic Pic
	 */
	public void addRow(Pic pic) {
		Object data[] = new Object[4];
		data[0] = pic.getContainerURL();
		data[1] = pic.getTarget();

		long size = pic.getSize();
		if (size <= 0) {
			data[2] = Localization.getString("Unkown");
		} else {
			data[2] = UnitFormatUtil.getSizeString(size, SettingsManager.instance().getSizeView());
		}

		data[3] = pic;

		this.addRow(data);
	}

	@Override
	public void removeRow(int row) {
		/*
		 * This method is overridden to disable the fireTableRowsDeleted-Call, because
		 * this slows down deleting, when a lot of rows have to be deleted.
		 */
		dataVector.removeElementAt(row);
	}
}

package ch.supertomcat.bh.gui.queue;

import javax.swing.table.DefaultTableModel;

import ch.supertomcat.bh.pic.Pic;

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
		data[2] = pic.getSize();
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

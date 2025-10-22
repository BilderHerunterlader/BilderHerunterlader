package ch.supertomcat.bh.gui.log;

import javax.swing.table.DefaultTableModel;

/**
 * TableModel for DirectoryLog
 */
public class DirectoryLogTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public DirectoryLogTableModel() {
		this.addColumn("DateTime");
		this.addColumn("Folder");
		this.addColumn("FolderExists");
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Adds a directory log
	 * 
	 * @param dateTime Date and Time
	 * @param directory Directory
	 * @param exists Directory exists
	 */
	public void addRow(final String dateTime, final String directory, final boolean exists) {
		Object[] data = new Object[3];
		data[0] = dateTime;
		data[1] = directory;
		data[2] = exists;
		addRow(data);
	}

	/**
	 * Delete all logs
	 */
	public void removeAllRows() {
		for (int i = getRowCount() - 1; i > -1; i--) {
			removeRow(i);
		}
	}
}

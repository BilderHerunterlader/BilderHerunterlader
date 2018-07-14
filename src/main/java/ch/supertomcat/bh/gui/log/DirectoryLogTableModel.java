package ch.supertomcat.bh.gui.log;

import java.awt.EventQueue;

import javax.swing.table.DefaultTableModel;

/**
 * TableModel for DirectoryLog
 */
public class DirectoryLogTableModel extends DefaultTableModel {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -1786920513536756606L;

	/**
	 * Constructor
	 */
	public DirectoryLogTableModel() {
		super();
		this.addColumn("DateTime");
		this.addColumn("Folder");
		this.addColumn("FolderExists");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
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
	public synchronized void addRow(final String dateTime, final String directory, final boolean exists) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				Object data[] = new Object[3];
				data[0] = dateTime;
				data[1] = directory;
				data[2] = exists;
				DirectoryLogTableModel.this.insertRow(0, data);
			}
		});
	}

	/**
	 * Delete all logs
	 */
	public void removeAllRows() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (int i = DirectoryLogTableModel.this.getRowCount() - 1; i > -1; i--) {
					DirectoryLogTableModel.this.removeRow(i);
				}
			}
		});
	}
}

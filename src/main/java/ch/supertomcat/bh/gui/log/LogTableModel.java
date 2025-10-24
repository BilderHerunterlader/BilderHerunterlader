package ch.supertomcat.bh.gui.log;

import javax.swing.table.DefaultTableModel;

/**
 * TableModel for Logs
 */
public class LogTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public LogTableModel() {
		this.addColumn("DateTime");
		this.addColumn("URL");
		this.addColumn("TargetPath");
		this.addColumn("TargetFilename");
		this.addColumn("Size");
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Adds a log
	 * 
	 * @param containerURL ContainerURL
	 * @param targetPath Target Path
	 * @param targetFilename Target Filename
	 * @param dateTime Date and Time
	 * @param filesize Filesize
	 */
	public void addRow(final String containerURL, final String targetPath, final String targetFilename, final String dateTime, final String filesize) {
		Object[] data = new Object[5];
		data[0] = dateTime;
		data[1] = containerURL;
		data[2] = targetPath;
		data[3] = targetFilename;
		data[4] = filesize;
		insertRow(0, data);

		int rc = getRowCount();
		if (rc > 100) {
			removeRow(rc - 1);
		}
	}

	/**
	 * Delete all logs
	 */
	public void removeAllRows() {
		for (int i = getRowCount() - 1; i >= 0; i--) {
			removeRow(i);
		}
	}
}

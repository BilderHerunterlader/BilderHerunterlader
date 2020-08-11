package ch.supertomcat.bh.gui.queue;

import javax.swing.table.DefaultTableModel;

import ch.supertomcat.bh.pic.Pic;

/**
 * TableModel for Queue
 */
public class QueueTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;

	/**
	 * URL Column Index
	 */
	public static final int URL_COLUMN_INDEX = 0;

	/**
	 * Thumb URL Column Index
	 */
	public static final int THUMB_URL_COLUMN_INDEX = 1;

	/**
	 * Host Column Index
	 */
	public static final int HOST_COLUMN_INDEX = 2;

	/**
	 * Thread URL Column Index
	 */
	public static final int THREAD_URL_COLUMN_INDEX = 3;

	/**
	 * Target Column Index
	 */
	public static final int TARGET_COLUMN_INDEX = 4;

	/**
	 * Download URL Column Index
	 */
	public static final int DOWNLOAD_URL_COLUMN_INDEX = 5;

	/**
	 * Added Date Column Index
	 */
	public static final int ADDED_DATE_COLUMN_INDEX = 6;

	/**
	 * Size Column Index
	 */
	public static final int SIZE_COLUMN_INDEX = 7;

	/**
	 * Progress Column Index
	 */
	public static final int PROGRESS_COLUMN_INDEX = 8;

	/**
	 * Constructor
	 */
	public QueueTableModel() {
		this.addColumn("URL");
		this.addColumn("ThumbURL");
		this.addColumn("Host");
		this.addColumn("ThreadURL");
		this.addColumn("Target");
		this.addColumn("DownloadURL");
		this.addColumn("AddedDate");
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
		Object data[] = new Object[9];
		data[URL_COLUMN_INDEX] = pic.getContainerURL();
		data[THUMB_URL_COLUMN_INDEX] = pic.getThumb();
		data[HOST_COLUMN_INDEX] = pic.getHoster();
		data[THREAD_URL_COLUMN_INDEX] = pic.getThreadURL();
		data[TARGET_COLUMN_INDEX] = pic.getTarget();
		data[DOWNLOAD_URL_COLUMN_INDEX] = pic.getDownloadURL();
		data[ADDED_DATE_COLUMN_INDEX] = pic.getDateTime();
		data[SIZE_COLUMN_INDEX] = pic.getSize();
		data[PROGRESS_COLUMN_INDEX] = pic;
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

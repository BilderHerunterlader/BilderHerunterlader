package ch.supertomcat.bh.gui.keywords;

import javax.swing.table.DefaultTableModel;

import ch.supertomcat.bh.keywords.Keyword;

/**
 * TableModel for Keywords
 */
public class KeywordsTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;

	/**
	 * Title Column Index
	 */
	public static final int TITLE_COLUMN_INDEX = 0;

	/**
	 * Keywords Column Index
	 */
	public static final int KEYWORDS_COLUMN_INDEX = 1;

	/**
	 * Folder Column Index
	 */
	public static final int FOLDER_COLUMN_INDEX = 2;

	/**
	 * Relative Folder Column Index
	 */
	public static final int RELATIVE_FOLDER_COLUMN_INDEX = 3;

	/**
	 * Relative Path Column Index
	 */
	public static final int RELATIVE_PATH_COLUMN_INDEX = 4;

	/**
	 * Constructor
	 */
	public KeywordsTableModel() {
		this.addColumn("Title");
		this.addColumn("Keywords");
		this.addColumn("Folder");
		this.addColumn("RelativeFolder");
		this.addColumn("RelativPath");
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex < 0 || columnIndex >= getColumnCount() || getRowCount() <= 0) {
			return super.getColumnClass(columnIndex);
		}
		return getValueAt(0, columnIndex).getClass();
	}

	/**
	 * Adds a row
	 * 
	 * @param k Keyword
	 */
	public void addRow(Keyword k) {
		Object[] data = new Object[5];
		data[TITLE_COLUMN_INDEX] = k.getTitle();
		data[KEYWORDS_COLUMN_INDEX] = k.getKeywords();
		data[FOLDER_COLUMN_INDEX] = k.getDownloadPath();
		data[RELATIVE_FOLDER_COLUMN_INDEX] = k.getRelativeDownloadPath();
		data[RELATIVE_PATH_COLUMN_INDEX] = k.isRelativePath();
		this.addRow(data);
	}

	/**
	 * Inserts a row
	 * 
	 * @param k Keyword
	 * @param position Position
	 */
	public void addRow(Keyword k, int position) {
		Object[] data = new Object[5];
		data[TITLE_COLUMN_INDEX] = k.getTitle();
		data[KEYWORDS_COLUMN_INDEX] = k.getKeywords();
		data[FOLDER_COLUMN_INDEX] = k.getDownloadPath();
		data[RELATIVE_FOLDER_COLUMN_INDEX] = k.getRelativeDownloadPath();
		data[RELATIVE_PATH_COLUMN_INDEX] = k.isRelativePath();
		this.insertRow(position, data);
	}

	/**
	 * Removes all rows
	 */
	public void removeAllRows() {
		for (int i = this.getRowCount() - 1; i > -1; i--) {
			this.removeRow(i);
		}
	}
}

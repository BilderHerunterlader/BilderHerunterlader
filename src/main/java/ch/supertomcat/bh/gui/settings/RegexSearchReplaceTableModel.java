package ch.supertomcat.bh.gui.settings;

import javax.swing.table.DefaultTableModel;

/**
 * TableModel for Regex Search Replace
 */
public class RegexSearchReplaceTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;

	/**
	 * Search Only Flag
	 */
	private boolean searchOnly;

	/**
	 * Constructor
	 */
	public RegexSearchReplaceTableModel() {
		this(false);
	}

	/**
	 * Constructor
	 * 
	 * @param searchOnly True if table model should have only search column, false otherwise
	 */
	public RegexSearchReplaceTableModel(boolean searchOnly) {
		this.searchOnly = searchOnly;
		this.addColumn("SearchPattern");
		if (!searchOnly) {
			this.addColumn("ReplacePattern");
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Add a row
	 * 
	 * @param search Search
	 * @param replace Replace
	 */
	public void addRow(String search, String replace) {
		Object[] data = new Object[searchOnly ? 1 : 2];
		data[0] = search;
		if (!searchOnly) {
			data[1] = replace;
		}
		this.addRow(data);
	}
}

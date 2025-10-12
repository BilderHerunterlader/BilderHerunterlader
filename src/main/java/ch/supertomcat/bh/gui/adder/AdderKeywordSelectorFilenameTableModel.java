package ch.supertomcat.bh.gui.adder;

import javax.swing.table.DefaultTableModel;

/**
 * TableModel for AdderKeywordSelectorFilename
 */
public class AdderKeywordSelectorFilenameTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;

	/**
	 * Konstruktor
	 */
	public AdderKeywordSelectorFilenameTableModel() {
		this.addColumn("Links");
		this.addColumn("Keywords");
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return column == 1;
	}

	@Override
	public Object getValueAt(int row, int column) {
		try {
			return super.getValueAt(row, column);
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
}

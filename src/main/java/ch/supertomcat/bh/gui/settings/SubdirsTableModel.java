package ch.supertomcat.bh.gui.settings;

import javax.swing.table.DefaultTableModel;

import ch.supertomcat.bh.settings.options.Subdir;


/**
 * TableModel for Subdirs
 */
public class SubdirsTableModel extends DefaultTableModel {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 7776217313342054743L;
	
	/**
	 * Constructor
	 */
	public SubdirsTableModel() {
		super();
		this.addColumn("SubdirName");
		this.addColumn("SubdirMinimum");
		this.addColumn("SubdirMaximum");
		this.addColumn("SubdirResolutionMinimum");
		this.addColumn("SubdirResolutionMaximum");
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return true;
	}
	
	/**
	 * Add a row
	 * @param sub Subdir
	 */
	public void addRow(Subdir sub) {
		Object data[] = new Object[5];
		data[0] = sub.getSubdirName();
		data[1] = sub.getMinSize();
		data[2] = sub.getMaxSize();
		data[3] = sub.getMinWidth() + "x" + sub.getMinHeight();
		data[4] = sub.getMaxWidth() + "x" + sub.getMaxHeight();
		this.addRow(data);
	}
	
	/**
	 * Add an empty row
	 */
	public void addEmptyRow() {
		Object data[] = new Object[5];
		data[0] = "Name";
		data[1] = (long)0;
		data[2] = (long)0;
		data[3] = "0x0";
		data[4] = "0x0";
		this.addRow(data);
	}
	
	/**
	 * Remove all rows
	 */
	public void removeAllRows() {
		for (int i = this.getRowCount() - 1; i > -1; i--) {
			this.removeRow(i);
		}
	}
}

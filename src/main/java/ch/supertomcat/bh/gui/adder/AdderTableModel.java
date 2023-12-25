package ch.supertomcat.bh.gui.adder;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * TableModel for the AdderPanel
 */
public class AdderTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;

	/**
	 * bFireTableCellUpdated
	 */
	private boolean fireTableCellUpdatedEnabled = true;

	/**
	 * Constructor
	 */
	public AdderTableModel() {
		this.addColumn("Selection");
		this.addColumn("URL");
		this.addColumn("Filename");
		this.addColumn("TargetFolder");
		this.addColumn("Thumb");
		this.addColumn("FilenameOverride");
		this.addColumn("DeleteFile");
		this.addColumn("LastModified");
		this.addColumn("Host");
		this.addColumn("Preview");
		this.addColumn("Blacklist");
		this.addColumn("Keyword");
		this.addColumn("AlreadyDownloaded");
		this.addColumn("TargetFolderOverride");
		this.addColumn("TargetFolderOverrideValue");
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		switch (column) {
			case 0:
				// Selection
				return true;
			case 6:
				// Delete File
				return true;
			case 10:
				// Blacklist
				return true;
			default:
				return false;
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
			case 0:
				// Selection
				return Boolean.class;
			case 6:
				// Delete File
				return Boolean.class;
			case 10:
				// Blacklist
				return Boolean.class;
			default:
				return super.getColumnClass(columnIndex);
		}
	}

	@Override
	public void setValueAt(Object aValue, int row, int column) {
		/*
		 * I had to override this method, because the one from the superclass
		 * always calls fireTableCellUpdated, which slows the AdderPanel down if
		 * a huge number of links are in the table
		 * 
		 * So classes that use this model can now set if fireTableCellUpdated is
		 * fired or not by the bFireTableCellUpdated-Flag
		 * 
		 * So if there are changes needed on many rows, it might be better to
		 * set the flag to false and execute the changes then. But do not forget,
		 * that you have to fire fireTableChanged after that!
		 * And after the changes you should change the flag again to true, so that
		 * changes that are made by the user directly on the table are recognized!
		 * 
		 * Be aware that this model is not Thread-Safe!
		 */
		Vector<Object> rowVector = dataVector.elementAt(row);
		rowVector.setElementAt(aValue, column);
		if (fireTableCellUpdatedEnabled) {
			fireTableCellUpdated(row, column);
		}
	}

	/**
	 * Returns the fireTableCellUpdatedEnabled
	 * 
	 * @return fireTableCellUpdatedEnabled
	 */
	public boolean isFireTableCellUpdatedEnabled() {
		return fireTableCellUpdatedEnabled;
	}

	/**
	 * Sets the fireTableCellUpdatedEnabled
	 * 
	 * @param fireTableCellUpdatedEnabled fireTableCellUpdatedEnabled
	 */
	public void setFireTableCellUpdatedEnabled(boolean fireTableCellUpdatedEnabled) {
		this.fireTableCellUpdatedEnabled = fireTableCellUpdatedEnabled;
	}
}

package ch.supertomcat.bh.gui.adder;

import javax.swing.table.DefaultTableModel;

/**
 * TableModel for the AdderPanel
 */
public class AdderTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;

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
}

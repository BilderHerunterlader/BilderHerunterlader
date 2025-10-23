package ch.supertomcat.bh.gui.update;

import javax.swing.table.DefaultTableModel;

import ch.supertomcat.bh.update.containers.WrappedUpdateData;

/**
 * TableModel for UpdateWindow
 */
public class UpdateTableModel extends DefaultTableModel {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 903861422474272306L;

	/**
	 * Constructor
	 */
	public UpdateTableModel() {
		this.addColumn("UpdateAction");
		this.addColumn("UpdateType");
		this.addColumn("Name");
		this.addColumn("Version");
		this.addColumn("UpdateSourceNote");
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Adds a row
	 * 
	 * @param wrappedUpdateData WrappedUpdateData
	 */
	public void addRow(WrappedUpdateData wrappedUpdateData) {
		Object[] data = new Object[5];
		data[0] = wrappedUpdateData.getAction();
		data[1] = wrappedUpdateData.getType();
		data[2] = wrappedUpdateData.getName();
		data[3] = wrappedUpdateData.getVersion();
		data[4] = wrappedUpdateData.getComment();
		this.addRow(data);
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

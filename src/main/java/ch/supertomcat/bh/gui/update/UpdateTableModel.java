package ch.supertomcat.bh.gui.update;

import javax.swing.table.DefaultTableModel;

import ch.supertomcat.bh.update.containers.UpdateObject;

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
	 * @param update UpdateObject
	 */
	public void addRow(UpdateObject update) {
		Object[] data = new Object[5];
		data[0] = update.getAction();
		data[1] = update.getTypeAsString();
		data[2] = update.getName();
		data[3] = update.getVersion();
		data[4] = update.getAction() == UpdateObject.UpdateActionType.ACTION_REMOVE ? update.getComment() : update.getSources().get(0).getSource();
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

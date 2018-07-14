package ch.supertomcat.bh.gui.queue;

import javax.swing.table.DefaultTableModel;

import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.guitools.UnitFormatTool;


/**
 * TableModel for Queue
 */
public class QueueTableModel extends DefaultTableModel {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 7776217313342054743L;
	
	/**
	 * Constructor
	 */
	public QueueTableModel() {
		super();
		this.addColumn("URL");
		this.addColumn("Target");
		this.addColumn("Size");
		this.addColumn("Progress");
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
	
	/**
	 * Adds a row
	 * @param pic Pic
	 */
	protected void addRow(Pic pic) {
		Object data[] = new Object[4];
		data[0] = pic.getContainerURL();
		data[1] = pic.getTarget();
		
		long size = pic.getSize();
		if (size < 1) {
			data[2] = Localization.getString("Unkown");
		} else {
			data[2] = UnitFormatTool.getSizeString(size, SettingsManager.instance().getSizeView());
		}
		
		data[3] = pic;
		
		this.addRow(data);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int column) {
		try {
			return super.getValueAt(row, column);
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#removeRow(int)
	 */
	@Override
	public void removeRow(int row) {
		/*
		 * This method is overridden to disable the fireTableRowsDeleted-Call, because
		 * this slows down deleting, when a lot of rows have to be deleted.
		 */
		dataVector.removeElementAt(row);
	}
}

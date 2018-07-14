package ch.supertomcat.bh.gui.log;

import java.awt.EventQueue;

import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TableModel for Logs
 */
public class LogTableModel extends DefaultTableModel {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 7776217313342054743L;

	/**
	 * Logger fuer diese Klasse
	 */
	private static Logger logger = LoggerFactory.getLogger(LogTableModel.class);

	/**
	 * Constructor
	 */
	public LogTableModel() {
		super();
		this.addColumn("DateTime");
		this.addColumn("URL");
		this.addColumn("Target");
		this.addColumn("Size");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Adds a log
	 * 
	 * @param containerURL ContainerURL
	 * @param target Target
	 * @param dateTime Date and Time
	 * @param filesize Filesize
	 */
	public synchronized void addRow(final String containerURL, final String target, final String dateTime, final String filesize) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				Object data[] = new Object[4];
				data[0] = dateTime;
				data[1] = containerURL;
				data[2] = target;
				data[3] = filesize;

				if (containerURL.length() < 1 && LogTableModel.this.getRowCount() >= 100) {
					data[3] = "";
					LogTableModel.this.insertRow(99, data);
				} else if (containerURL.length() > 0) {
					LogTableModel.this.insertRow(0, data);
				}
				int rc = LogTableModel.this.getRowCount();
				try {
					if (rc > 100) {
						LogTableModel.this.removeRow(rc - 1);
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		});
	}

	/**
	 * Delete all logs
	 */
	public void removeAllRows() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (int i = LogTableModel.this.getRowCount() - 1; i > -1; i--) {
					LogTableModel.this.removeRow(i);
				}
			}
		});
	}
}

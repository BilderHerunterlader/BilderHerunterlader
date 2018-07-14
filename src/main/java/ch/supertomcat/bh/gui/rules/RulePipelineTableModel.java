package ch.supertomcat.bh.gui.rules;

import javax.swing.table.DefaultTableModel;


/**
 * TableModel for RulePipelinePanel
 */
public class RulePipelineTableModel extends DefaultTableModel {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 7776217313342054743L;
	
	/**
	 * Constructor
	 */
	public RulePipelineTableModel() {
		super();
		this.addColumn("SearchPattern");
		this.addColumn("ReplacePattern");
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
	
	/**
	 * Add a row
	 * @param search Search
	 * @param replace Replace
	 */
	public void addRow(String search, String replace) {
		Object data[] = new Object[2];
		data[0] = search;
		data[1] = replace;
		this.addRow(data);
	}
	
	/**
	 * Edit row
	 * @param search Search
	 * @param replace Replace
	 * @param row Row
	 */
	public void editRow(String search, String replace, int row) {
		this.setValueAt(search, row, 0);
		this.setValueAt(replace, row, 1);
	}
}

package ch.supertomcat.bh.gui.rules.editor.failures;

import javax.swing.table.DefaultTableModel;

/**
 * TableModel for RulePipelinePanel
 */
public class RulePipelineFailuresTableModel extends DefaultTableModel {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 7776217313342054743L;

	/**
	 * Constructor
	 */
	public RulePipelineFailuresTableModel() {
		this.addColumn("SearchPattern");
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Add a row
	 * 
	 * @param search Search
	 */
	public void addRow(String search) {
		Object data[] = new Object[1];
		data[0] = search;
		this.addRow(data);
	}
}

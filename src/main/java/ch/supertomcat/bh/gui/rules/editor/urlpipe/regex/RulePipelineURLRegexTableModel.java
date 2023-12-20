package ch.supertomcat.bh.gui.rules.editor.urlpipe.regex;

import javax.swing.table.DefaultTableModel;

/**
 * TableModel for RulePipelinePanel
 */
public class RulePipelineURLRegexTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public RulePipelineURLRegexTableModel() {
		this.addColumn("SearchPattern");
		this.addColumn("ReplacePattern");
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Add a row
	 * 
	 * @param search Search
	 * @param replace Replace
	 */
	public void addRow(String search, String replace) {
		Object[] data = new Object[2];
		data[0] = search;
		data[1] = replace;
		this.addRow(data);
	}
}

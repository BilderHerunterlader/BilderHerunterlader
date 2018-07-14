package ch.supertomcat.bh.gui.rules;

import javax.swing.table.DefaultTableModel;

import ch.supertomcat.supertomcattools.guitools.Localization;


/**
 * TableModel for Rules
 */
public class RulesTableModel extends DefaultTableModel {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 7776217313342054743L;
	
	/**
	 * Konstruktor
	 */
	public RulesTableModel() {
		super();
		this.addColumn("Rules");
		this.addColumn("Version");
		this.addColumn("Mode");
		this.addColumn("Enabled");
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 3) return true;
		return false;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int column) {
		if (column == 3) return Boolean.class;
		return super.getColumnClass(column);
	}

	/**
	 * Add a row
	 * @param rulename Rulename
	 * @param version Version
	 * @param mode Mode
	 * @param pipeCount Pipe-Count
	 * @param redirect Is Redirect
	 * @param enabled Enabled
	 */
	public void addRow(String rulename, String version, int mode, int pipeCount, boolean redirect, boolean enabled) {
		Object data[] = new Object[4];
		data[0] = rulename;
		data[1] = version;
		String strRedirect = (redirect == true) ? "Redirect | " : "";
		if (pipeCount > 0) {
			String strPipeCount = "";
			if (pipeCount > 1) {
				strPipeCount = " + " + (pipeCount - 1) + " Pipelines";
			}
			if (mode == 0) {
				data[2] = strRedirect + Localization.getString("RuleModeZeroShort") + strPipeCount;
			} else {
				data[2] = strRedirect + Localization.getString("RuleModeOneShort") + strPipeCount;
			}
		} else {
			data[2] = strRedirect + "0 Pipelines";
		}
		data[3] = enabled;
		this.addRow(data);
	}
}

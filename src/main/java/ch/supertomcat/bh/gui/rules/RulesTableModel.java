package ch.supertomcat.bh.gui.rules;

import javax.swing.table.DefaultTableModel;

import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.RulePipelineURLRegex;
import ch.supertomcat.bh.rules.RuleURLPipeline;
import ch.supertomcat.bh.rules.xml.URLRegexPipelineMode;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * TableModel for Rules
 */
public class RulesTableModel extends DefaultTableModel {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Konstruktor
	 */
	public RulesTableModel() {
		this.addColumn("Rule");
		this.addColumn("Version");
		this.addColumn("Mode");
		this.addColumn("Enabled");
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return column == 3;
	}

	@Override
	public Class<?> getColumnClass(int column) {
		if (column == 3) {
			return Boolean.class;
		}
		return super.getColumnClass(column);
	}

	/**
	 * Add a row
	 * 
	 * @param rule Rule
	 */
	public void addRow(Rule rule) {
		Object[] data = new Object[4];
		data[0] = rule;
		data[1] = rule.getVersion();
		data[2] = getModeString(rule);
		data[3] = rule.isEnabled();
		this.addRow(data);
	}

	/**
	 * Update row
	 * 
	 * @param row Row
	 */
	public void updateRow(int row) {
		Rule rule = (Rule)this.getValueAt(row, 0);
		this.setValueAt(rule.getVersion(), row, 1);
		this.setValueAt(getModeString(rule), row, 2);
		this.setValueAt(rule.isEnabled(), row, 3);
	}

	private String getModeString(Rule rule) {
		String strRedirect = rule.getDefinition().isRedirect() ? "Redirect | " : "";
		if (!rule.getPipelines().isEmpty()) {
			int pipeCount = rule.getPipelines().size();

			String strPipeCount;
			if (pipeCount > 1) {
				strPipeCount = " + " + (pipeCount - 1) + " Pipelines";
			} else {
				strPipeCount = "";
			}

			RuleURLPipeline<?> firstPipe = rule.getPipelines().get(0);
			if (firstPipe instanceof RulePipelineURLRegex rulePipelineURLRegex && rulePipelineURLRegex.getDefinition().getMode() == URLRegexPipelineMode.CONTAINER_OR_THUMBNAIL_URL) {
				return strRedirect + Localization.getString("RuleModeZeroShort") + strPipeCount;
			} else {
				return strRedirect + Localization.getString("RuleModeOneShort") + strPipeCount;
			}
		} else {
			return strRedirect + "0 Pipelines";
		}
	}
}

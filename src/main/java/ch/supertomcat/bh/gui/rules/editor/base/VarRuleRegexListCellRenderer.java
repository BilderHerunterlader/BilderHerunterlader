package ch.supertomcat.bh.gui.rules.editor.base;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import ch.supertomcat.bh.gui.rules.editor.urlpipe.varregex.RulePipelineVarRuleRegexPanel;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * List Cell Renderer for Pipes
 */
public class VarRuleRegexListCellRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (comp instanceof JLabel) {
			String text;
			if (value instanceof RulePipelineVarRuleRegexPanel) {
				text = Localization.getString("VariableAssignment") + ": " + ((RulePipelineVarRuleRegexPanel)value).getVariableName();
			} else {
				text = Localization.getString("VariableAssignment");
			}
			((JLabel)comp).setText(text);
		}
		return comp;
	}
}

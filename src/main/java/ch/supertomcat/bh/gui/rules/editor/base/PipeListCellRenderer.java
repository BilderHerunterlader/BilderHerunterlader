package ch.supertomcat.bh.gui.rules.editor.base;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import ch.supertomcat.bh.gui.rules.editor.failures.RulePipelineFailuresPanel;
import ch.supertomcat.bh.gui.rules.editor.urlpipe.RulePipelineURLJavascriptPanel;
import ch.supertomcat.bh.gui.rules.editor.urlpipe.RulePipelineURLRegexPanel;

/**
 * List Cell Renderer for Pipes
 */
public class PipeListCellRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (comp instanceof JLabel) {
			String text;
			if (value instanceof RulePipelineFailuresPanel) {
				text = "Failure Pipeline";
			} else if (value instanceof RulePipelineURLJavascriptPanel) {
				text = "Javascript Pipeline";
			} else if (value instanceof RulePipelineURLRegexPanel) {
				text = "Regex Pipeline";
			} else {
				text = "Pipeline";
			}
			((JLabel)comp).setText(text);
		}
		return comp;
	}
}

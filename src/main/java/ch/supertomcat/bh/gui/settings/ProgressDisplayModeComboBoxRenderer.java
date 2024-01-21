package ch.supertomcat.bh.gui.settings;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import ch.supertomcat.bh.settings.xml.ProgressDisplayMode;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Renderer for ProgressDisplayMode ComboBox
 */
public class ProgressDisplayModeComboBoxRenderer extends BasicComboBoxRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof ProgressDisplayMode) {
			ProgressDisplayMode progressDisplayMode = (ProgressDisplayMode)value;
			switch (progressDisplayMode) {
				case PROGRESSBAR_PERCENT:
					setText(Localization.getString("ProgressbarPercent"));
					break;
				case PROGRESSBAR_SIZE:
					setText(Localization.getString("ProgressbarSize"));
					break;
			}
		}
		return comp;
	}
}

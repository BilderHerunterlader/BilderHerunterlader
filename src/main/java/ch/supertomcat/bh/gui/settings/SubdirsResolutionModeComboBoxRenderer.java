package ch.supertomcat.bh.gui.settings;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import ch.supertomcat.bh.settings.xml.SubdirsResolutionMode;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Renderer for LookAndFeel ComboBox
 */
public class SubdirsResolutionModeComboBoxRenderer extends BasicComboBoxRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof SubdirsResolutionMode subdirsResolutionMode) {
			switch (subdirsResolutionMode) {
				case RESOLUTION_BOTH:
					setText(Localization.getString("SubdirResolutionModeBoth"));
					break;
				case RESOLUTION_ONLY_WIDTH:
					setText(Localization.getString("SubdirResolutionModeOnlyWidth"));
					break;
				case RESOLUTION_ONLY_HEIGHT:
					setText(Localization.getString("SubdirResolutionModeOnlyHeight"));
					break;
				case RESOLUTION_ONLY_HIGHER:
					setText(Localization.getString("SubdirResolutionModeOnlyHigher"));
					break;
				case RESOLUTION_ONLY_LOWER:
					setText(Localization.getString("SubdirResolutionModeOnlyLower"));
					break;
			}
		}
		return comp;
	}
}

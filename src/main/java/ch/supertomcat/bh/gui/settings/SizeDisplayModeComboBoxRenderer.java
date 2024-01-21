package ch.supertomcat.bh.gui.settings;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import ch.supertomcat.bh.settings.xml.SizeDisplayMode;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Renderer for SizeDisplayMode ComboBox
 */
public class SizeDisplayModeComboBoxRenderer extends BasicComboBoxRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof SizeDisplayMode) {
			SizeDisplayMode sizeDisplayMode = (SizeDisplayMode)value;
			switch (sizeDisplayMode) {
				case AUTO_CHANGE_SIZE:
					setText(Localization.getString("AutoChangeSize"));
					break;
				case ONLY_B:
					setText(Localization.getString("OnlyB"));
					break;
				case ONLY_KIB:
					setText(Localization.getString("OnlyKiB"));
					break;
				case ONLY_MIB:
					setText(Localization.getString("OnlyMiB"));
					break;
				case ONLY_GIB:
					setText(Localization.getString("OnlyGiB"));
					break;
				case ONLY_TIB:
					setText(Localization.getString("OnlyTiB"));
					break;
			}
		}
		return comp;
	}
}

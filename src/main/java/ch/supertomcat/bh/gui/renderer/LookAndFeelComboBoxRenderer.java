package ch.supertomcat.bh.gui.renderer;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import ch.supertomcat.bh.settings.MappedLookAndFeelSetting;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Renderer for LookAndFeel ComboBox
 */
public class LookAndFeelComboBoxRenderer extends BasicComboBoxRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof MappedLookAndFeelSetting) {
			MappedLookAndFeelSetting lookAndFeel = (MappedLookAndFeelSetting)value;
			if (lookAndFeel == MappedLookAndFeelSetting.LAF_DEFAULT) {
				setText(Localization.getString("LAFDefault"));
			} else if (lookAndFeel == MappedLookAndFeelSetting.LAF_OS) {
				setText(Localization.getString("LAFSystem"));
			} else {
				setText(lookAndFeel.getDisplayName());
			}
		}
		return comp;
	}
}

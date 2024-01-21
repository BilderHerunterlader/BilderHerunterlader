package ch.supertomcat.bh.gui.settings;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import ch.supertomcat.bh.settings.xml.LookAndFeelSetting;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Renderer for LookAndFeel ComboBox
 */
public class LookAndFeelComboBoxRenderer extends BasicComboBoxRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof LookAndFeelSetting) {
			LookAndFeelSetting lookAndFeel = (LookAndFeelSetting)value;
			if (lookAndFeel == LookAndFeelSetting.LAF_DEFAULT) {
				setText(Localization.getString("LAFDefault"));
			} else if (lookAndFeel == LookAndFeelSetting.LAF_OS) {
				setText(Localization.getString("LAFSystem"));
			}
		}
		return comp;
	}
}

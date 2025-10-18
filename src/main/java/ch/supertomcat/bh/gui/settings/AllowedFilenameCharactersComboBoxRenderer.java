package ch.supertomcat.bh.gui.settings;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import ch.supertomcat.bh.settings.xml.AllowedFilenameCharacters;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Renderer for LookAndFeel ComboBox
 */
public class AllowedFilenameCharactersComboBoxRenderer extends BasicComboBoxRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof AllowedFilenameCharacters allowedFilenameCharacters) {
			switch (allowedFilenameCharacters) {
				case ALL:
					setText(Localization.getString("FilenameAlle"));
					break;
				case ASCII_ONLY:
					setText(Localization.getString("FilenameAsciiOnly"));
					break;
				case ASCII_UMLAUT:
					setText(Localization.getString("FilenameAsciiUmlaut"));
					break;
			}
		}
		return comp;
	}
}

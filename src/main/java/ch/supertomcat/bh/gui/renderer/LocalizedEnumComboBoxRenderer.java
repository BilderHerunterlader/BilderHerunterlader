package ch.supertomcat.bh.gui.renderer;

import java.awt.Component;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Renderer for Enum ComboBox
 * 
 * @param <T> Enum Type
 */
public class LocalizedEnumComboBoxRenderer<T extends Enum<T>> extends BasicComboBoxRenderer {
	private static final long serialVersionUID = 1L;

	/**
	 * Enum Class
	 */
	private Class<T> enumClass;

	/**
	 * Localization Strings
	 */
	private Map<T, String> localizedStrings;

	/**
	 * Constructor
	 * 
	 * @param enumClass Enum Class
	 */
	public LocalizedEnumComboBoxRenderer(Class<T> enumClass) {
		this.enumClass = enumClass;
	}

	/**
	 * Constructor
	 * 
	 * @param enumClass Enum Class
	 * @param localizedStrings Localization String (needs to be filled to match Enum ordinal)
	 */
	public LocalizedEnumComboBoxRenderer(Class<T> enumClass, Map<T, String> localizedStrings) {
		this.enumClass = enumClass;
		this.localizedStrings = localizedStrings;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (comp instanceof JLabel && enumClass.isInstance(value)) {
			T enumValue = enumClass.cast(value);
			if (localizedStrings != null) {
				String localizationString = localizedStrings.get(enumValue);
				if (localizationString != null) {
					((JLabel)comp).setText(Localization.getString(localizationString));
				}
			} else {
				((JLabel)comp).setText(Localization.getString(enumValue.name()));
			}
		}
		return comp;
	}
}

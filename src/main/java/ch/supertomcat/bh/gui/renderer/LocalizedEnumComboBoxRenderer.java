package ch.supertomcat.bh.gui.renderer;

import java.awt.Component;
import java.util.List;

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
	 * Localization String (needs to be filled to match Enum ordinal)
	 */
	private List<String> localizedStrings;

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
	public LocalizedEnumComboBoxRenderer(Class<T> enumClass, List<String> localizedStrings) {
		this.enumClass = enumClass;
		this.localizedStrings = localizedStrings;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (enumClass.isInstance(value)) {
			T enumValue = enumClass.cast(value);
			if (localizedStrings != null) {
				int ordinal = enumValue.ordinal();
				if (ordinal >= 0 && ordinal < localizedStrings.size()) {
					Localization.getString(localizedStrings.get(ordinal));
				}
			} else {
				Localization.getString(enumValue.name());
			}
		}
		return comp;
	}
}

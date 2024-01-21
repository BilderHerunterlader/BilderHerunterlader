package ch.supertomcat.bh.gui.settings;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import ch.supertomcat.bh.settings.xml.BrowserCookiesMode;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Renderer for LookAndFeel ComboBox
 */
public class BrowserCookiesModeComboBoxRenderer extends BasicComboBoxRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof BrowserCookiesMode) {
			BrowserCookiesMode browserCookiesMode = (BrowserCookiesMode)value;
			switch (browserCookiesMode) {
				case NO_COOKIES:
					setText(Localization.getString("CookiesNo"));
					break;
				case BROWSER_IE:
					setText(Localization.getString("CookiesIE"));
					break;
				case BROWSER_FIREFOX:
					setText(Localization.getString("CookiesFF"));
					break;
				case BROWSER_OPERA:
					setText(Localization.getString("CookiesOP"));
					break;
				case BROWSER_PALE_MOON:
					setText(Localization.getString("CookiesPM"));
					break;
				case BROWSER_OPERA_NEW:
					setText(Localization.getString("CookiesOPNEW"));
					break;
			}
		}
		return comp;
	}
}

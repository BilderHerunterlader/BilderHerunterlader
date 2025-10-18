package ch.supertomcat.bh.gui.settings;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import ch.supertomcat.bh.settings.xml.KeywordMatchMode;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Renderer for LookAndFeel ComboBox
 */
public class KeywordMatchModeComboBoxRenderer extends BasicComboBoxRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof KeywordMatchMode keywordMatchMode) {
			switch (keywordMatchMode) {
				case MATCH_ALL:
					setText(Localization.getString("MatchAll"));
					break;
				case MATCH_ALL_STRICT:
					setText(Localization.getString("MatchAllStrict"));
					break;
				case MATCH_ONLY_EXACT:
					setText(Localization.getString("MatchOnlyExact"));
					break;
			}
		}
		return comp;
	}
}

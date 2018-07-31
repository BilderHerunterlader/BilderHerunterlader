package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.bh.keywords.KeywordMatch.KeywordMatchType;
import ch.supertomcat.supertomcattools.guitools.tablerenderer.DefaultStringColorRowRenderer;

/**
 * TableCellRenderer for AdderKeywordSelectorTitle
 */
public class AdderKeywordSelectorColorRowRenderer extends DefaultStringColorRowRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		prepareForegroundColor(this, table, value, isSelected, hasFocus, row, column);

		KeywordMatchType matchType = (KeywordMatchType)table.getModel().getValueAt(table.convertRowIndexToModel(row), 1);
		Color c;
		switch (matchType) {
			case MATCHED_EXACT:
				c = Color.decode("#ff9966");
				break;
			case MATCHED_ALL_KEYWORDS:
				c = Color.decode("#ccffcc");
				break;
			case MATCHED_SOME_KEYWORDS:
				c = Color.decode("#e6ffff");
				break;
			case MATCHED_NOT:
			default:
				c = Color.WHITE;
		}

		if (isSelected) {
			c = table.getSelectionBackground();
		}
		this.setBackground(c);

		if (table.convertColumnIndexToModel(column) == 1) {
			String valueText = matchType.getText();
			prepareValueText(this, table, valueText, isSelected, hasFocus, row, column);
		} else {
			prepareValueText(this, table, value, isSelected, hasFocus, row, column);
		}
		this.setOpaque(true);
		return this;
	}
}

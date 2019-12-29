package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.bh.keywords.KeywordMatch.KeywordMatchType;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;

/**
 * TableCellRenderer for AdderKeywordSelectorTitle
 */
public class AdderKeywordSelectorColorRowRenderer extends DefaultStringColorRowRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	/**
	 * Flag if match type column is available in model
	 */
	private final boolean includeMatchType;

	/**
	 * Constructor
	 * 
	 * @param includeMatchType True if match type column is available in model, false otherwise
	 */
	public AdderKeywordSelectorColorRowRenderer(boolean includeMatchType) {
		this.includeMatchType = includeMatchType;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		prepareForegroundColor(this, table, value, isSelected, hasFocus, row, column);

		Color c;
		KeywordMatchType matchType = null;
		String matchTypeText = null;
		if (includeMatchType) {
			int matchTypeColumnModelIndex = table.getColumn("MatchType").getModelIndex();
			matchType = (KeywordMatchType)table.getModel().getValueAt(table.convertRowIndexToModel(row), matchTypeColumnModelIndex);
			if (table.convertColumnIndexToModel(column) == matchTypeColumnModelIndex) {
				matchTypeText = matchType.getText();
			}
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
		} else {
			c = Color.WHITE;
		}

		if (isSelected) {
			c = table.getSelectionBackground();
		}
		this.setBackground(c);

		if (includeMatchType && matchTypeText != null) {
			prepareValueText(this, table, matchTypeText, isSelected, hasFocus, row, column);
		} else {
			prepareValueText(this, table, value, isSelected, hasFocus, row, column);
		}
		this.setOpaque(true);
		return this;
	}
}

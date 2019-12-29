package ch.supertomcat.bh.gui.adder;

import javax.swing.table.DefaultTableModel;

import ch.supertomcat.bh.keywords.Keyword;
import ch.supertomcat.bh.keywords.KeywordMatch.KeywordMatchType;

/**
 * TableModel for AdderKeywordSelectorTitle
 */
public class AdderKeywordSelectorTitleTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;

	/**
	 * Flag if match type column should be added to model
	 */
	private final boolean includeMatchType;

	/**
	 * Constructor
	 * 
	 * @param includeMatchType True if match type column should be added to model, false otherwise
	 */
	public AdderKeywordSelectorTitleTableModel(boolean includeMatchType) {
		this.includeMatchType = includeMatchType;
		this.addColumn("Title");
		this.addColumn("Keywords");
		if (includeMatchType) {
			this.addColumn("MatchType");
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Add a row
	 * Use MATCHED_NOT for matchType if there are not types.
	 * e.g. if the dialog was not opened by a
	 * KeywordSearchThread
	 * 
	 * @param keyword Keyword
	 * @param matchType MatchType
	 */
	public void addRow(Keyword keyword, KeywordMatchType matchType) {
		Object data[] = new Object[includeMatchType ? 3 : 2];
		data[0] = keyword;
		data[1] = keyword.getKeywords();
		if (includeMatchType) {
			data[2] = matchType;
		}
		this.addRow(data);
	}
}

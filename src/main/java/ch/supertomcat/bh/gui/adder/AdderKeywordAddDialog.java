package ch.supertomcat.bh.gui.adder;

import javax.swing.JDialog;
import javax.swing.JFrame;

import ch.supertomcat.bh.gui.keywords.KeywordEditDialog;
import ch.supertomcat.bh.keywords.Keyword;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.supertomcattools.guitools.Localization;

/**
 * Dialog for adding a new keyword to the keyword-table
 */
public class AdderKeywordAddDialog extends KeywordEditDialog {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param owner Owner-Dialog
	 * @param title Title
	 */
	public AdderKeywordAddDialog(JDialog owner, String title) {
		this(owner, title, "");
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner-Frame
	 * @param title Title
	 */
	public AdderKeywordAddDialog(JFrame owner, String title) {
		this(owner, title, "");
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner-Dialog
	 * @param title Title
	 * @param keywordTitle Predefined keyword title
	 */
	public AdderKeywordAddDialog(JDialog owner, String title, String keywordTitle) {
		super(owner, title);
		init(keywordTitle);
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner-Frame
	 * @param title Title
	 * @param keywordTitle Predefined keyword title
	 */
	public AdderKeywordAddDialog(JFrame owner, String title, String keywordTitle) {
		super(owner, title);
		init(keywordTitle);
	}

	/**
	 * Initialize GUI-Components
	 * 
	 * @param keywordTitle Keyword title
	 */
	private void init(String keywordTitle) {
		if (keywordTitle != null && !keywordTitle.isEmpty()) {
			/*
			 * If a predefined keyword-title was given, we use it for all the textfields
			 */
			txtTitle.setText(keywordTitle);
			txtKeywords.setText(keywordTitle);
			txtPath.setText(txtPath.getPath() + keywordTitle);
			txtRPath.setText(keywordTitle);
		}
	}

	/**
	 * Open the Dialog
	 * 
	 * @param owner Owner
	 * @return Keyword
	 */
	public static Keyword openAddKeywordDialog(JDialog owner) {
		AdderKeywordAddDialog dialog = new AdderKeywordAddDialog(owner, Localization.getString("Add"));
		dialog.setVisible(true);
		if (dialog.isOkPressed()) {
			Keyword keyword = dialog.getKeyword();
			KeywordManager.instance().addKeyword(keyword);
			return keyword;
		}
		return null;
	}

	/**
	 * Open the Dialog
	 * 
	 * @param owner Owner
	 * @param keywordTitle Keyword-Title
	 * @return Keyword
	 */
	public static Keyword openAddKeywordDialog(JFrame owner, String keywordTitle) {
		AdderKeywordAddDialog dialog = new AdderKeywordAddDialog(owner, Localization.getString("Add"), keywordTitle);
		dialog.setVisible(true);
		if (dialog.isOkPressed()) {
			Keyword keyword = dialog.getKeyword();
			KeywordManager.instance().addKeyword(keyword);
			return keyword;
		}
		return null;
	}
}

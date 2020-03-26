package ch.supertomcat.bh.gui.adder;

import javax.swing.JDialog;
import javax.swing.JFrame;

import ch.supertomcat.bh.gui.keywords.KeywordEditDialog;
import ch.supertomcat.bh.keywords.Keyword;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;

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
	 * @param settingsManager Settings Manager
	 */
	public AdderKeywordAddDialog(JDialog owner, String title, SettingsManager settingsManager) {
		this(owner, title, settingsManager, "");
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner-Frame
	 * @param title Title
	 * @param settingsManager Settings Manager
	 */
	public AdderKeywordAddDialog(JFrame owner, String title, SettingsManager settingsManager) {
		this(owner, title, settingsManager, "");
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner-Dialog
	 * @param title Title
	 * @param settingsManager Settings Manager
	 * @param keywordTitle Predefined keyword title
	 */
	public AdderKeywordAddDialog(JDialog owner, String title, SettingsManager settingsManager, String keywordTitle) {
		super(owner, title, settingsManager);
		init(keywordTitle);
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner-Frame
	 * @param title Title
	 * @param settingsManager Settings Manager
	 * @param keywordTitle Predefined keyword title
	 */
	public AdderKeywordAddDialog(JFrame owner, String title, SettingsManager settingsManager, String keywordTitle) {
		super(owner, title, settingsManager);
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
	 * @param settingsManager Settings Manager
	 * @return Keyword
	 */
	public static Keyword openAddKeywordDialog(JDialog owner, SettingsManager settingsManager) {
		AdderKeywordAddDialog dialog = new AdderKeywordAddDialog(owner, Localization.getString("Add"), settingsManager);
		dialog.setVisible(true);
		if (dialog.isOkPressed()) {
			return dialog.getKeyword();
		}
		return null;
	}

	/**
	 * Open the Dialog
	 * 
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 * @param keywordTitle Keyword-Title
	 * @return Keyword
	 */
	public static Keyword openAddKeywordDialog(JFrame owner, SettingsManager settingsManager, String keywordTitle) {
		AdderKeywordAddDialog dialog = new AdderKeywordAddDialog(owner, Localization.getString("Add"), settingsManager, keywordTitle);
		dialog.setVisible(true);
		if (dialog.isOkPressed()) {
			return dialog.getKeyword();
		}
		return null;
	}
}

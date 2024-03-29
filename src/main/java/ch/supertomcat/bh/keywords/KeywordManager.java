package ch.supertomcat.bh.keywords;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.database.sqlite.KeywordsSQLiteDB;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;

/**
 * Class which holds the Keywords
 */
public class KeywordManager {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Keywords
	 */
	private List<Keyword> keywords = new ArrayList<>();

	/**
	 * listeners
	 */
	private List<KeywordManagerListener> listeners = new CopyOnWriteArrayList<>();

	private final KeywordsSQLiteDB keywordsSQLiteDB;

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 */
	public KeywordManager(SettingsManager settingsManager) {
		this.keywordsSQLiteDB = new KeywordsSQLiteDB(ApplicationProperties.getProperty("DatabasePath") + "/BH-Keywords.sqlite", settingsManager.getSettings().isBackupDbOnStart(), settingsManager
				.getSettings().isDefragDBOnStart(), settingsManager.getSettings().getDefragMinFilesize());
		List<Keyword> keywordsFromDB = keywordsSQLiteDB.getAllEntries();
		keywords.addAll(keywordsFromDB);
	}

	/**
	 * Returns an array containing the Keywords
	 * 
	 * @return Keywords-Array
	 */
	public synchronized List<Keyword> getKeywords() {
		return new ArrayList<>(keywords);
	}

	/**
	 * @param keyword Keyword
	 * @return Index
	 */
	public synchronized int indexOfKeyword(Keyword keyword) {
		return keywords.indexOf(keyword);
	}

	/**
	 * @param index Index
	 * @return Pic
	 */
	public synchronized Keyword getKeywordByIndex(int index) {
		if (index < 0 || index >= keywords.size()) {
			return null;
		}
		return keywords.get(index);
	}

	/**
	 * Add Keywords to the array
	 * 
	 * @param keywords Keywords
	 */
	public synchronized void addKeywords(List<Keyword> keywords) {
		for (Keyword keyword : keywords) {
			this.keywords.add(keyword);
		}
		keywordsSQLiteDB.insertEntries(keywords);
		keywordsChanged();
	}

	/**
	 * Add a Keyword
	 * 
	 * @param k Keyword
	 */
	public synchronized void addKeyword(Keyword k) {
		this.keywords.add(k);
		keywordsSQLiteDB.insertEntry(k);
		keywordsChanged();
	}

	/**
	 * Updates the given Keyword
	 * 
	 * @param keyword Keyword
	 */
	public synchronized void updateKeyword(Keyword keyword) {
		keywordsSQLiteDB.updateEntry(keyword);
		keywordsChanged();
	}

	/**
	 * Updates the given Keywords
	 * 
	 * @param keywords Keywords
	 */
	public synchronized void updateKeywords(List<Keyword> keywords) {
		keywordsSQLiteDB.updateEntries(keywords);
		keywordsChanged();
	}

	/**
	 * @param keyword Keyword
	 */
	public synchronized void removeKeyword(Keyword keyword) {
		int index = keywords.indexOf(keyword);
		if (index >= 0) {
			keywords.remove(keyword);
			keywordsSQLiteDB.deleteEntry(keyword);
			keywordsChanged();
		}
	}

	/**
	 * Removes Keywords from the Keywords based on indices
	 * 
	 * @param indices Indices
	 */
	public void removeKeywords(int[] indices) {
		List<Keyword> keywordsToDelete = new ArrayList<>();
		for (int i = indices.length - 1; i > -1; i--) {
			if ((indices[i] < 0) || (indices[i] >= keywords.size())) {
				continue;
			}
			Keyword keyword = keywords.get(indices[i]);
			keywords.remove(indices[i]);
			keywordsToDelete.add(keyword);
		}
		keywordsSQLiteDB.deleteEntries(keywordsToDelete);
		keywordsChanged();
	}

	/**
	 * Saves the database
	 */
	public void saveDatabase() {
		// Nothing to do
	}

	/**
	 * Saves and closes the database
	 */
	public void closeDatabase() {
		saveDatabase();
		logger.info("Closing Keyword Database");
		keywordsSQLiteDB.closeAllDatabaseConnections();
	}

	/**
	 * KeywordsChanged
	 */
	private void keywordsChanged() {
		for (KeywordManagerListener listener : listeners) {
			listener.keywordsChanged();
		}
	}

	/**
	 * @param l Listener
	 */
	public void addListener(KeywordManagerListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	/**
	 * @param l Listener
	 */
	public void removeListener(KeywordManagerListener l) {
		listeners.remove(l);
	}
}

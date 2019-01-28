package ch.supertomcat.bh.keywords;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.database.sqlite.KeywordsSQLiteDB;
import ch.supertomcat.supertomcattools.applicationtool.ApplicationProperties;

/**
 * Class which holds the Keywords
 */
public class KeywordManager {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Only exact matches
	 */
	public static final int MATCH_ONLY_EXACT = 0;

	/**
	 * All keywords found, but strict search
	 */
	public static final int MATCH_ALL_STRICT = 1;

	/**
	 * All keywords found
	 */
	public static final int MATCH_ALL = 2;

	/**
	 * Singleton
	 */
	private static KeywordManager instance;

	/**
	 * Keywords
	 */
	private List<Keyword> keywords = new ArrayList<>();

	/**
	 * listeners
	 */
	private List<KeywordManagerListener> listeners = new CopyOnWriteArrayList<>();

	private KeywordsSQLiteDB keywordsSQLiteDB = new KeywordsSQLiteDB(ApplicationProperties.getProperty("DatabasePath") + "/BH-Keywords.sqlite");

	/**
	 * Constructor
	 */
	private KeywordManager() {
		List<Keyword> keywordsFromDB = keywordsSQLiteDB.getAllEntries();
		keywords.addAll(keywordsFromDB);
	}

	/**
	 * Returns the instance
	 * 
	 * @return Instance
	 */
	public static synchronized KeywordManager instance() {
		if (instance == null) {
			instance = new KeywordManager();
		}
		return instance;
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
			keywordsSQLiteDB.insertEntry(keyword);
		}
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
	public void removeKeywords(int indices[]) {
		for (int i = indices.length - 1; i > -1; i--) {
			if ((indices[i] < 0) || (indices[i] >= keywords.size())) {
				continue;
			}
			Keyword keyword = keywords.get(indices[i]);
			keywordsSQLiteDB.deleteEntry(keyword);
			keywords.remove(indices[i]);
		}
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

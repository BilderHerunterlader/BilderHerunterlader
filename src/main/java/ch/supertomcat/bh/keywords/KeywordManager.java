package ch.supertomcat.bh.keywords;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.database.sqlite.KeywordsSQLiteDB;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
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
	 * Lock
	 */
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	/**
	 * Read Lock
	 */
	private final Lock readLock = readWriteLock.readLock();

	/**
	 * Write Lock
	 */
	private final Lock writeLock = readWriteLock.writeLock();

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
		this.keywordsSQLiteDB = new KeywordsSQLiteDB(ApplicationProperties.getProperty(ApplicationMain.DATABASE_PATH) + "/BH-Keywords.sqlite", settingsManager.getSettings()
				.isBackupDbOnStart(), settingsManager.getSettings().isDefragDBOnStart(), settingsManager.getSettings().getDefragMinFilesize());
		List<Keyword> keywordsFromDB = keywordsSQLiteDB.getAllEntries();
		keywords.addAll(keywordsFromDB);
	}

	/**
	 * Returns an array containing the Keywords
	 * 
	 * @return Keywords-Array
	 */
	public List<Keyword> getKeywords() {
		try {
			readLock.lock();
			return new ArrayList<>(keywords);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * @param keyword Keyword
	 * @return Index
	 */
	public int indexOfKeyword(Keyword keyword) {
		try {
			readLock.lock();
			return keywords.indexOf(keyword);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * @param index Index
	 * @return Pic
	 */
	public Keyword getKeywordByIndex(int index) {
		try {
			readLock.lock();
			if (index < 0 || index >= keywords.size()) {
				return null;
			}
			return keywords.get(index);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Add Keywords to the array
	 * 
	 * @param keywords Keywords
	 */
	public void addKeywords(List<Keyword> keywords) {
		try {
			writeLock.lock();
			for (Keyword keyword : keywords) {
				this.keywords.add(keyword);
			}
			keywordsSQLiteDB.insertEntries(keywords);
		} finally {
			writeLock.unlock();
		}
		keywordsChanged();
	}

	/**
	 * Add a Keyword
	 * 
	 * @param k Keyword
	 */
	public void addKeyword(Keyword k) {
		try {
			writeLock.lock();
			this.keywords.add(k);
			keywordsSQLiteDB.insertEntry(k);
		} finally {
			writeLock.unlock();
		}
		keywordsChanged();
	}

	/**
	 * Updates the given Keyword
	 * 
	 * @param keyword Keyword
	 */
	public void updateKeyword(Keyword keyword) {
		try {
			writeLock.lock();
			keywordsSQLiteDB.updateEntry(keyword);
		} finally {
			writeLock.unlock();
		}
		keywordsChanged();
	}

	/**
	 * Updates the given Keywords
	 * 
	 * @param keywords Keywords
	 */
	public void updateKeywords(List<Keyword> keywords) {
		try {
			writeLock.lock();
			keywordsSQLiteDB.updateEntries(keywords);
		} finally {
			writeLock.unlock();
		}
		keywordsChanged();
	}

	/**
	 * @param keyword Keyword
	 */
	public void removeKeyword(Keyword keyword) {
		try {
			writeLock.lock();
			int index = keywords.indexOf(keyword);
			if (index < 0) {
				return;
			}

			keywords.remove(keyword);
			keywordsSQLiteDB.deleteEntry(keyword);
		} finally {
			writeLock.unlock();
		}
		keywordsChanged();
	}

	/**
	 * Removes Keywords from the Keywords based on indices
	 * 
	 * @param indices Indices
	 */
	public void removeKeywords(int[] indices) {
		try {
			writeLock.lock();
			List<Keyword> keywordsToDelete = new ArrayList<>();
			for (int i = indices.length - 1; i > -1; i--) {
				if (indices[i] < 0 || indices[i] >= keywords.size()) {
					continue;
				}
				Keyword keyword = keywords.get(indices[i]);
				keywords.remove(indices[i]);
				keywordsToDelete.add(keyword);
			}
			keywordsSQLiteDB.deleteEntries(keywordsToDelete);
		} finally {
			writeLock.unlock();
		}
		keywordsChanged();
	}

	/**
	 * Saves and closes the database
	 */
	public void closeDatabase() {
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

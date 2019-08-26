package ch.supertomcat.bh.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.database.sqlite.QueueSQLiteDB;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.pic.IPicListener;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.PicState;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;

/**
 * Class which holds the Keywords
 * 
 * Important: Do not call any synchronized methods of DownloadQueueManager from within synchronized(pics) blocks.
 * If you need to do it, because there is no other way, lock DownloadQueueManager.instance() first!
 */
public class QueueManager implements IPicListener {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Singleton
	 */
	private static QueueManager instance;

	/**
	 * Synchronization Object for pics
	 */
	private final Object syncObject = new Object();

	/**
	 * Queue
	 */
	private List<Pic> pics = new ArrayList<>();

	/**
	 * listeners
	 */
	private List<QueueManagerListener> listeners = new CopyOnWriteArrayList<>();

	private boolean downloadsStopped = true;

	/**
	 * Count of deleted objects
	 */
	private int deletedObjects = 0;

	private QueueSQLiteDB queueSQLiteDB = new QueueSQLiteDB(ApplicationProperties.getProperty("DatabasePath") + "/BH-Downloads.sqlite");

	/**
	 * Constructor
	 */
	private QueueManager() {
		List<Pic> picsFromDB = queueSQLiteDB.getAllEntries();
		for (Pic pic : picsFromDB) {
			if (pic.getStatus() != PicState.COMPLETE) {
				if ((pic.getStatus() == PicState.WAITING) || (pic.getStatus() == PicState.DOWNLOADING) || (pic.getStatus() == PicState.ABORTING)) {
					pic.setStatus(PicState.SLEEPING);
				}
				pic.removeAllListener();
				pic.addPicListener(this);
				pics.add(pic);
			} else if (pic.getStatus() == PicState.COMPLETE) {
				queueSQLiteDB.deleteEntry(pic);
			}
		}
	}

	/**
	 * Returns the instance
	 * 
	 * @return Instance
	 */
	public static synchronized QueueManager instance() {
		if (instance == null) {
			instance = new QueueManager();
		}
		return instance;
	}

	/**
	 * Saves the database in another thread
	 */
	public void asyncSaveDatabase() {
		// Nothing to do
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
		logger.info("Closing Queue Database");
		queueSQLiteDB.closeAllDatabaseConnections();
	}

	/**
	 * Returns an array containing the Queue
	 * 
	 * @return Queue-Array
	 */
	public List<Pic> getQueue() {
		synchronized (syncObject) {
			return new ArrayList<>(pics);
		}
	}

	/**
	 * @return Queue-Size
	 */
	public int getQueueSize() {
		synchronized (syncObject) {
			return pics.size();
		}
	}

	/**
	 * @param pic Pic
	 * @return Index
	 */
	public int indexOfPic(Pic pic) {
		synchronized (syncObject) {
			return pics.indexOf(pic);
		}
	}

	/**
	 * @param index Index
	 * @return Pic
	 */
	public Pic getPicByIndex(int index) {
		synchronized (syncObject) {
			if (index < 0 || index >= pics.size()) {
				return null;
			}
			return pics.get(index);
		}
	}

	/**
	 * @param pic Pic
	 */
	public void addPic(Pic pic) {
		synchronized (syncObject) {
			if (pics.contains(pic)) {
				return;
			}
			pics.add(pic);
			queueSQLiteDB.insertEntry(pic);
			pic.removeAllListener();
			pic.addPicListener(this);
			for (QueueManagerListener l : listeners) {
				l.picAdded(pic);
			}
		}
		if (SettingsManager.instance().isAutoStartDownloads()) {
			pic.startDownload();
			DownloadQueueManager.instance().manageDLSlots();
		}
	}

	/**
	 * @param picList Pic List
	 */
	public void addPics(List<Pic> picList) {
		List<Pic> picsAdded = new ArrayList<>();
		synchronized (syncObject) {
			for (Pic pic : picList) {
				if (!(pics.contains(pic))) {
					pics.add(pic);
					picsAdded.add(pic);
					queueSQLiteDB.insertEntry(pic);
					pic.removeAllListener();
					pic.addPicListener(this);
				}
			}
			for (QueueManagerListener l : listeners) {
				l.picsAdded(picsAdded);
			}
		}
		if (SettingsManager.instance().isAutoStartDownloads()) {
			for (Pic pic : picsAdded) {
				pic.startDownload();
			}
			DownloadQueueManager.instance().manageDLSlots();
		}
	}

	/**
	 * Updates the given Pic
	 * 
	 * @param pic Pic
	 */
	public void updatePic(Pic pic) {
		queueSQLiteDB.updateEntry(pic);
	}

	/**
	 * Updates the given Pics
	 * 
	 * @param pics Pics
	 */
	public void updatePics(List<Pic> pics) {
		queueSQLiteDB.updateEntries(pics);
	}

	/**
	 * @param pic Pic
	 */
	public void removePic(Pic pic) {
		if (pic.getStatus() == PicState.WAITING || pic.getStatus() == PicState.DOWNLOADING || pic.getStatus() == PicState.ABORTING) {
			return;
		}

		synchronized (syncObject) {
			int index = pics.indexOf(pic);
			if (index >= 0) {
				pics.remove(pic);
				pic.removeAllListener();
				queueSQLiteDB.deleteEntry(pic);
				deletedObjects++;
				for (QueueManagerListener l : listeners) {
					l.picRemoved(pic, index);
				}
			}
			if (deletedObjects > 100) {
				saveDatabase();
				deletedObjects = 0;
			}
		}
	}

	/**
	 * Removes Pics from the queue based on indices
	 * If there are downloads running this method will not do anything!
	 * 
	 * @param indices Indices
	 */
	public void removePics(int indices[]) {
		if (DownloadQueueManager.instance().isDownloading()) {
			return;
		}

		synchronized (syncObject) {
			for (int i = indices.length - 1; i > -1; i--) {
				if ((indices[i] < 0) || (indices[i] >= pics.size())) {
					continue;
				}
				Pic pic = pics.get(indices[i]);
				pic.removeAllListener();
				queueSQLiteDB.deleteEntry(pic);
				pics.remove(indices[i]);
			}

			for (QueueManagerListener l : listeners) {
				l.picsRemoved(indices);
			}
			deletedObjects = 0;
		}
	}

	/**
	 * Start downloads
	 */
	public void startDownload() {
		List<Pic> list;
		downloadsStopped = false;
		synchronized (syncObject) {
			list = new ArrayList<>(pics);
		}
		// request downloadslots for all pics in queue
		for (Pic pic : list) {
			pic.startDownload();
		}
		DownloadQueueManager.instance().manageDLSlots();
	}

	/**
	 * Stop downloads
	 */
	public void stopDownload() {
		List<Pic> list;
		downloadsStopped = true;
		synchronized (syncObject) {
			list = new ArrayList<>(pics);
		}
		// stop all downloads
		for (Pic pic : list) {
			pic.stopDownload();
		}
	}

	/**
	 * Returns the downloadsStopped
	 * 
	 * @return downloadsStopped
	 */
	public boolean isDownloadsStopped() {
		return downloadsStopped;
	}

	/**
	 * @param l Listener
	 */
	public void addListener(QueueManagerListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	/**
	 * @param l Listener
	 */
	public void removeListener(QueueManagerListener l) {
		listeners.remove(l);
	}

	/**
	 * Returns the Synchronization Object for QueueMananger.pics
	 * 
	 * @return Synchronization Object for QueueMananger.pics
	 */
	public Object getSyncObject() {
		return syncObject;
	}

	@Override
	public void progressBarChanged(Pic pic, int min, int max, int val, String s, String errMsg) {
		synchronized (syncObject) {
			int index = pics.indexOf(pic);
			for (QueueManagerListener l : listeners) {
				l.picProgressBarChanged(pic, min, max, val, s, errMsg, index);
			}
		}
	}

	@Override
	public void sizeChanged(Pic pic) {
		synchronized (syncObject) {
			int index = pics.indexOf(pic);
			for (QueueManagerListener l : listeners) {
				l.picSizeChanged(pic, index);
			}
		}
	}

	@Override
	public void targetChanged(Pic pic) {
		synchronized (syncObject) {
			int index = pics.indexOf(pic);
			for (QueueManagerListener l : listeners) {
				l.picTargetChanged(pic, index);
			}
		}
	}

	@Override
	public void statusChanged(Pic pic) {
		synchronized (syncObject) {
			int index = pics.indexOf(pic);
			for (QueueManagerListener l : listeners) {
				l.picStatusChanged(pic, index);
			}
		}
		if (pic.getStatus() == PicState.COMPLETE) {
			if (SettingsManager.instance().isSaveLogs()) {
				LogManager.instance().addPicToLog(pic);
			}
			SettingsManager.instance().increaseOverallDownloadedFiles(1);
			SettingsManager.instance().increaseOverallDownloadedBytes(pic.getSize());
			DownloadQueueManager.instance().increaseSessionDownloadedBytes(pic.getSize());
			DownloadQueueManager.instance().increaseSessionDownloadedFiles();
			SettingsManager.instance().writeSettings(true);
			removePic(pic);
		}
	}

	@Override
	public void deactivatedChanged(Pic pic) {
		synchronized (syncObject) {
			int index = pics.indexOf(pic);
			for (QueueManagerListener l : listeners) {
				l.picDeactivatedChanged(pic, index);
			}
		}
	}
}

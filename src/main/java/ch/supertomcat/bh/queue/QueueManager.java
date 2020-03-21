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
	 * Log Manager
	 */
	private final LogManager logManager;

	/**
	 * Constructor
	 * 
	 * @param logManager Log Manager
	 */
	public QueueManager(LogManager logManager) {
		this.logManager = logManager;
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
			pic.startDownload(DownloadQueueManager.instance(this));
			DownloadQueueManager.instance(this).manageDLSlots();
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
				pic.startDownload(DownloadQueueManager.instance(this));
			}
			DownloadQueueManager.instance(this).manageDLSlots();
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
		if (DownloadQueueManager.instance(this).isDownloading()) {
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
			pic.startDownload(DownloadQueueManager.instance(this));
		}
		DownloadQueueManager.instance(this).manageDLSlots();
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
			pic.stopDownload(DownloadQueueManager.instance(this));
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
	public void progressChanged(Pic pic) {
		synchronized (syncObject) {
			int index = pics.indexOf(pic);
			for (QueueManagerListener l : listeners) {
				l.picProgressBarChanged(pic, index);
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
		updatePic(pic);
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
		if ((pic.getStatus() != PicState.SLEEPING) && (pic.getStatus() != PicState.WAITING) && (pic.getStatus() != PicState.DOWNLOADING) && (pic.getStatus() != PicState.ABORTING)) {
			/*
			 * We can gain some speed by only updating the status in the database in some cases.
			 * When pics are loaded, they reset to SLEEPING if the status is one of the three in the condition above.
			 * So we don't need to save the status in those cases.
			 */
			updatePic(pic);
		}
		if (pic.getStatus() == PicState.COMPLETE) {
			if (SettingsManager.instance().isSaveLogs()) {
				logManager.addPicToLog(pic);
			}
			SettingsManager.instance().increaseOverallDownloadedFiles(1);
			SettingsManager.instance().increaseOverallDownloadedBytes(pic.getSize());
			DownloadQueueManager.instance(this).increaseSessionDownloadedBytes(pic.getSize());
			DownloadQueueManager.instance(this).increaseSessionDownloadedFiles();
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
		updatePic(pic);
	}
}

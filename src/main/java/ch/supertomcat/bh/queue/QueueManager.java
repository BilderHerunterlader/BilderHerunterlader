package ch.supertomcat.bh.queue;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.database.sqlite.QueueSQLiteDB;
import ch.supertomcat.bh.gui.queue.QueueTableModel;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.pic.IPicListener;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.PicState;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.formatter.UnitFormatUtil;

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

	private boolean downloadsStopped = true;

	/**
	 * Count of deleted objects
	 */
	private int deletedObjects = 0;

	private QueueSQLiteDB queueSQLiteDB = new QueueSQLiteDB(ApplicationProperties.getProperty("DatabasePath") + "/BH-Downloads.sqlite");

	/**
	 * TabelModel
	 */
	private final QueueTableModel tableModel = new QueueTableModel();

	/**
	 * Download Queue Manager
	 */
	private final DownloadQueueManager downloadQueueManager;

	/**
	 * Log Manager
	 */
	private final LogManager logManager;

	/**
	 * Constructor
	 * 
	 * @param downloadQueueManager Download Queue Manager
	 * @param logManager Log Manager
	 * @param settingsManager SettingsManager
	 */
	public QueueManager(DownloadQueueManager downloadQueueManager, LogManager logManager, SettingsManager settingsManager) {
		this.downloadQueueManager = downloadQueueManager;
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
				tableModel.addRow(pic);
			} else if (pic.getStatus() == PicState.COMPLETE) {
				queueSQLiteDB.deleteEntry(pic);
			}
		}

		this.downloadQueueManager.addDownloadQueueManagerListener(new IDownloadQueueManagerListener() {
			@Override
			public void totalDownloadRateCalculated(double downloadRate) {
				// Nothing to do
			}

			@Override
			public void sessionDownloadedFilesChanged(int count) {
				// Nothing to do
			}

			@Override
			public void sessionDownloadedBytesChanged(long count) {
				// Nothing to do
			}

			@Override
			public void queueEmpty() {
				// If the queue is now empty, it is a good time to commit the database
				saveDatabase();
				if (!isDownloadsStopped() && settingsManager.isAutoRetryAfterDownloadsComplete()) {
					startDownload();
				}
			}

			@Override
			public void queueChanged(int queue, int openSlots, int maxSlots) {
				// Nothing to do
			}

			@Override
			public void downloadsComplete(int queue, int openSlots, int maxSlots) {
				// Nothing to do
			}
		});
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

			Runnable r = new Runnable() {
				@Override
				public void run() {
					tableModel.addRow(pic);
				}
			};
			executeInEventQueueThread(r);
		}
		if (SettingsManager.instance().isAutoStartDownloads()) {
			pic.startDownload(downloadQueueManager);
			downloadQueueManager.manageDLSlots();
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

			Runnable r = new Runnable() {
				@Override
				public void run() {
					for (Pic pic : picList) {
						tableModel.addRow(pic);
					}
				}
			};
			executeInEventQueueThread(r);
		}
		if (SettingsManager.instance().isAutoStartDownloads()) {
			for (Pic pic : picsAdded) {
				pic.startDownload(downloadQueueManager);
			}
			downloadQueueManager.manageDLSlots();
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

				Runnable r = new Runnable() {
					@Override
					public void run() {
						tableModel.removeRow(index);
						// Manually called, because the model overwrites the removeRow method and does not fire the event.
						tableModel.fireTableDataChanged();
					}
				};
				executeInEventQueueThread(r);
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
		if (downloadQueueManager.isDownloading()) {
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

			Runnable r = new Runnable() {
				@Override
				public void run() {
					for (int i = indices.length - 1; i > -1; i--) {
						tableModel.removeRow(indices[i]);
					}
					// Manually called, because the model overwrites the removeRow method and does not fire the event.
					tableModel.fireTableDataChanged();
				}
			};
			executeInEventQueueThread(r);

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
			pic.startDownload(downloadQueueManager);
		}
		downloadQueueManager.manageDLSlots();
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
			pic.stopDownload(downloadQueueManager);
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

			Runnable r = new Runnable() {
				@Override
				public void run() {
					if ((index > -1) && (tableModel.getRowCount() > index)) {
						tableModel.fireTableCellUpdated(index, 3);
					}
				}
			};
			executeInEventQueueThread(r);
		}
	}

	@Override
	public void sizeChanged(Pic pic) {
		synchronized (syncObject) {
			int index = pics.indexOf(pic);

			Runnable r = new Runnable() {
				@Override
				public void run() {
					if ((index > -1) && (tableModel.getRowCount() > index)) {
						// Change Cell
						long size = pic.getSize();
						if (size < 1) {
							tableModel.setValueAt(Localization.getString("Unkown"), index, 2);
						} else {
							tableModel.setValueAt(UnitFormatUtil.getSizeString(size, SettingsManager.instance().getSizeView()), index, 2);
						}
						tableModel.fireTableCellUpdated(index, 2);
					}
				}
			};
			executeInEventQueueThread(r);
		}
		updatePic(pic);
	}

	@Override
	public void targetChanged(Pic pic) {
		synchronized (syncObject) {
			int index = pics.indexOf(pic);

			Runnable r = new Runnable() {
				@Override
				public void run() {
					if ((index > -1) && (tableModel.getRowCount() > index)) {
						// Change Cell
						tableModel.setValueAt(pic.getTarget(), index, 1);
						tableModel.fireTableCellUpdated(index, 1);
					}
				}
			};
			executeInEventQueueThread(r);
		}
	}

	@Override
	public void statusChanged(Pic pic) {
		synchronized (syncObject) {
			int index = pics.indexOf(pic);

			Runnable r = new Runnable() {
				@Override
				public void run() {
					if (pic.getStatus() == PicState.FAILED || pic.getStatus() == PicState.FAILED_FILE_NOT_EXIST || pic.getStatus() == PicState.SLEEPING || pic.getStatus() == PicState.WAITING
							|| pic.getStatus() == PicState.FAILED_FILE_TEMPORARY_OFFLINE) {
						if ((index > -1) && (tableModel.getRowCount() > index)) {
							tableModel.setValueAt(pic, index, 3);
							tableModel.fireTableCellUpdated(index, 3);
						}
					}
				}
			};
			executeInEventQueueThread(r);
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
			downloadQueueManager.increaseSessionDownloadedBytes(pic.getSize());
			downloadQueueManager.increaseSessionDownloadedFiles();
			SettingsManager.instance().writeSettings(true);
			removePic(pic);
		}
	}

	@Override
	public void deactivatedChanged(Pic pic) {
		synchronized (syncObject) {
			int index = pics.indexOf(pic);

			Runnable r = new Runnable() {
				@Override
				public void run() {
					if ((index > -1) && (tableModel.getRowCount() > index)) {
						tableModel.fireTableRowsUpdated(index, index);
					}
				}
			};
			executeInEventQueueThread(r);
		}
		updatePic(pic);
	}

	/**
	 * Returns the tableModel
	 * 
	 * @return tableModel
	 */
	public QueueTableModel getTableModel() {
		return tableModel;
	}

	/**
	 * Executes the given Runnable in EventQueue Thread
	 * 
	 * @param r Runnable
	 */
	private void executeInEventQueueThread(Runnable r) {
		if (EventQueue.isDispatchThread()) {
			r.run();
		} else {
			try {
				EventQueue.invokeAndWait(r);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}

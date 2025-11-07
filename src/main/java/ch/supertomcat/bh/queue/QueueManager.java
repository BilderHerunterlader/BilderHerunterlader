package ch.supertomcat.bh.queue;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.database.sqlite.QueueSQLiteDB;
import ch.supertomcat.bh.downloader.FileDownloaderFactory;
import ch.supertomcat.bh.gui.queue.QueueTableModel;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.pic.IPicListener;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.PicDownloadListener;
import ch.supertomcat.bh.pic.PicState;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
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
	 * Queue
	 */
	private List<Pic> pics = new ArrayList<>();

	private volatile boolean downloadsStopped = true;

	private final QueueSQLiteDB queueSQLiteDB;

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
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * File Downloader Factory
	 */
	private final FileDownloaderFactory fileDownloaderFactory;

	/**
	 * Constructor
	 * 
	 * @param downloadQueueManager Download Queue Manager
	 * @param logManager Log Manager
	 * @param settingsManager SettingsManager
	 * @param hostManager Host Manager
	 * @param fileDownloaderFactory File Downloader Factory
	 */
	public QueueManager(DownloadQueueManager downloadQueueManager, LogManager logManager, SettingsManager settingsManager, HostManager hostManager, FileDownloaderFactory fileDownloaderFactory) {
		this.downloadQueueManager = downloadQueueManager;
		this.logManager = logManager;
		this.settingsManager = settingsManager;
		this.fileDownloaderFactory = fileDownloaderFactory;
		this.queueSQLiteDB = new QueueSQLiteDB(ApplicationProperties.getProperty(ApplicationMain.DATABASE_PATH) + "/BH-Downloads.sqlite", settingsManager.getSettings()
				.isBackupDbOnStart(), settingsManager.getSettings().isDefragDBOnStart(), settingsManager.getSettings().getDefragMinFilesize());
		List<Pic> picsFromDB = queueSQLiteDB.getAllEntries();
		for (Pic pic : picsFromDB) {
			if (pic.getStatus() != PicState.COMPLETE) {
				if ((pic.getStatus() == PicState.WAITING) || (pic.getStatus() == PicState.DOWNLOADING) || (pic.getStatus() == PicState.ABORTING)) {
					pic.setStatus(PicState.SLEEPING);
				}
				pic.setHoster(hostManager.getHosterForURL(pic.getContainerURL()));
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
				if (!isDownloadsStopped() && settingsManager.getDownloadsSettings().isAutoRetryAfterDownloadsComplete()) {
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
	 * Saves and closes the database
	 */
	public void closeDatabase() {
		logger.info("Closing Queue Database");
		queueSQLiteDB.closeAllDatabaseConnections();
	}

	/**
	 * Returns an array containing the Queue
	 * 
	 * @return Queue-Array
	 */
	public List<Pic> getQueue() {
		try {
			readLock.lock();
			return new ArrayList<>(pics);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * @return Queue-Size
	 */
	public int getQueueSize() {
		try {
			readLock.lock();
			return pics.size();
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * @param pic Pic
	 * @return Index
	 */
	public int indexOfPic(Pic pic) {
		try {
			readLock.lock();
			return pics.indexOf(pic);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * @param index Index
	 * @return Pic
	 */
	public Pic getPicByIndex(int index) {
		try {
			readLock.lock();
			if (index < 0 || index >= pics.size()) {
				return null;
			}
			return pics.get(index);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * @param pic Pic
	 */
	public void addPic(Pic pic) {
		try {
			writeLock.lock();
			if (pics.contains(pic)) {
				return;
			}

			pics.add(pic);
			queueSQLiteDB.insertEntry(pic);
			pic.removeAllListener();
			pic.addPicListener(this);

			executeInEventQueueThread(() -> tableModel.addRow(pic));
		} finally {
			writeLock.unlock();
		}

		if (settingsManager.getDownloadsSettings().isAutoStartDownloads()) {
			startDownload(pic);
		}
	}

	/**
	 * @param picList Pic List
	 */
	public void addPics(List<Pic> picList) {
		List<Pic> picsAdded = new ArrayList<>();
		try {
			writeLock.lock();
			for (Pic pic : picList) {
				if (!(pics.contains(pic))) {
					pics.add(pic);
					picsAdded.add(pic);
					pic.removeAllListener();
					pic.addPicListener(this);
				}
			}
			queueSQLiteDB.insertEntries(picList);

			executeInEventQueueThread(() -> {
				for (Pic pic : picList) {
					tableModel.addRow(pic);
				}
			});
		} finally {
			writeLock.unlock();
		}

		if (settingsManager.getDownloadsSettings().isAutoStartDownloads()) {
			startDownload(picsAdded);
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
	 * @param picList Pics
	 */
	public void updatePics(List<Pic> picList) {
		queueSQLiteDB.updateEntries(picList);
	}

	/**
	 * Removes Pic from the queue based on index
	 * If there are downloads running this method will not do anything!
	 * 
	 * @param pic Pic
	 */
	public void removePic(Pic pic) {
		if (downloadQueueManager.isDownloading() || (pic.getStatus() == PicState.WAITING || pic.getStatus() == PicState.DOWNLOADING || pic.getStatus() == PicState.ABORTING)) {
			return;
		}

		try {
			writeLock.lock();
			int index = pics.indexOf(pic);
			if (index >= 0) {
				pics.remove(pic);
				pic.removeAllListener();
				queueSQLiteDB.deleteEntry(pic);

				executeInEventQueueThread(() -> {
					tableModel.removeRow(index);
					// Manually called, because the model overwrites the removeRow method and does not fire the event.
					tableModel.fireTableDataChanged();
				});
			}
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Removes Pics from the queue
	 * If there are downloads running this method will not do anything!
	 * 
	 * @param picList Pics
	 */
	public void removePics(List<Pic> picList) {
		if (downloadQueueManager.isDownloading()) {
			return;
		}

		try {
			writeLock.lock();

			picList.stream().forEach(Pic::removeAllListener);

			int[] indices = picList.stream().mapToInt(pics::indexOf).filter(index -> index >= 0).sorted().toArray();

			// Remove in reverse order
			for (int i = indices.length - 1; i > -1; i--) {
				pics.remove(indices[i]);
			}

			queueSQLiteDB.deleteEntries(picList);

			executeInEventQueueThread(() -> {
				// Remove in reverse order
				for (int i = indices.length - 1; i > -1; i--) {
					tableModel.removeRow(indices[i]);
				}
				// Manually called, because the model overwrites the removeRow method and does not fire the event.
				tableModel.fireTableDataChanged();
			});
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Start downloads
	 */
	public void startDownload() {
		downloadsStopped = false;
		startDownload(getQueue());
	}

	/**
	 * Prepare Download
	 * 
	 * @param pic Pic
	 * @return PicDownloadListener or null if download should not be started (because Pic is deactivated for example)
	 */
	private PicDownloadListener prepareDownload(Pic pic) {
		return pic.prepareDownload(fileDownloaderFactory);
	}

	/**
	 * Start download
	 * 
	 * @param picList Pics
	 */
	private void startDownload(List<Pic> picList) {
		// request downloadslots for all pics in queue
		List<PicDownloadListener> picDownloadListeners = new ArrayList<>();
		for (Pic pic : picList) {
			PicDownloadListener picDownloadListener = prepareDownload(pic);
			if (picDownloadListener != null) {
				picDownloadListeners.add(picDownloadListener);
			}
		}
		// Request a download slot
		downloadQueueManager.addTasksToQueue(picDownloadListeners);
	}

	/**
	 * Start download
	 * 
	 * @param pic pic
	 */
	private void startDownload(Pic pic) {
		PicDownloadListener picDownloadListener = prepareDownload(pic);
		if (picDownloadListener != null) {
			downloadQueueManager.addTaskToQueue(picDownloadListener);
		}
	}

	/**
	 * Stop downloads
	 */
	public void stopDownload() {
		downloadsStopped = true;
		List<Pic> list = getQueue();

		downloadQueueManager.cancelTasks(false);

		// stop all downloads
		for (Pic pic : list) {
			stopDownload(pic);
		}
	}

	/**
	 * Stop download
	 * 
	 * This does not immediately stop the download, it only changes
	 * the stop-flag to true and the download method will stop if this
	 * flag is true.
	 * When the download is waiting for the parsed URL, this could take some time...
	 * 
	 * @param pic Pic
	 */
	private void stopDownload(Pic pic) {
		pic.stopDownload();
	}

	/**
	 * Returns the downloadsStopped
	 * 
	 * @return downloadsStopped
	 */
	public boolean isDownloadsStopped() {
		return downloadsStopped;
	}

	@Override
	public void progressChanged(Pic pic) {
		try {
			readLock.lock();
			int index = pics.indexOf(pic);

			executeInEventQueueThread(() -> {
				if (index > -1 && tableModel.getRowCount() > index) {
					tableModel.fireTableCellUpdated(index, QueueTableModel.PROGRESS_COLUMN_INDEX);
				}
			});
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void sizeChanged(Pic pic) {
		try {
			readLock.lock();
			int index = pics.indexOf(pic);

			executeInEventQueueThread(() -> {
				if (index > -1 && tableModel.getRowCount() > index) {
					// Change Cell
					tableModel.setValueAt(pic.getSize(), index, QueueTableModel.SIZE_COLUMN_INDEX);
					tableModel.fireTableCellUpdated(index, QueueTableModel.SIZE_COLUMN_INDEX);
				}
			});

			updatePic(pic);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void targetChanged(Pic pic) {
		try {
			readLock.lock();
			int index = pics.indexOf(pic);

			executeInEventQueueThread(() -> {
				if (index > -1 && tableModel.getRowCount() > index) {
					// Change Cell
					tableModel.setValueAt(pic.getTarget(), index, QueueTableModel.TARGET_COLUMN_INDEX);
					tableModel.fireTableCellUpdated(index, QueueTableModel.TARGET_COLUMN_INDEX);
				}
			});
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void statusChanged(Pic pic) {
		PicState status = pic.getStatus();
		if (status == PicState.COMPLETE) {
			try {
				writeLock.lock();
				if (settingsManager.getDownloadsSettings().isSaveLogs()) {
					logManager.addPicToLog(pic);
				}
				settingsManager.increaseOverallDownloadedFiles(1);
				settingsManager.increaseOverallDownloadedBytes(pic.getSize());
				downloadQueueManager.increaseSessionDownloadedBytes(pic.getSize());
				downloadQueueManager.increaseSessionDownloadedFiles();
				settingsManager.writeSettings(true);
				removePic(pic);
			} finally {
				writeLock.unlock();
			}
			return;
		}

		try {
			readLock.lock();
			if (status == PicState.FAILED || status == PicState.FAILED_FILE_NOT_EXIST || status == PicState.SLEEPING || status == PicState.WAITING
					|| status == PicState.FAILED_FILE_TEMPORARY_OFFLINE) {
				int index = pics.indexOf(pic);
				executeInEventQueueThread(() -> {
					if (index > -1 && tableModel.getRowCount() > index) {
						tableModel.fireTableCellUpdated(index, QueueTableModel.PROGRESS_COLUMN_INDEX);
					}
				});
			}

			if (status != PicState.SLEEPING && status != PicState.WAITING && status != PicState.DOWNLOADING && status != PicState.ABORTING) {
				/*
				 * We can gain some speed by only updating the status in the database in some cases.
				 * When pics are loaded, they reset to SLEEPING if the status is one of the three in the condition above.
				 * So we don't need to save the status in those cases.
				 */
				updatePic(pic);
			}
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void deactivatedChanged(Pic pic) {
		try {
			readLock.lock();
			int index = pics.indexOf(pic);

			executeInEventQueueThread(() -> {
				if (index > -1 && tableModel.getRowCount() > index) {
					tableModel.fireTableRowsUpdated(index, index);
				}
			});

			updatePic(pic);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void hosterChanged(Pic pic) {
		try {
			readLock.lock();
			int index = pics.indexOf(pic);

			executeInEventQueueThread(() -> {
				if (index > -1 && tableModel.getRowCount() > index) {
					// Change Cell
					tableModel.setValueAt(pic.getHoster(), index, QueueTableModel.HOST_COLUMN_INDEX);
					tableModel.fireTableCellUpdated(index, QueueTableModel.HOST_COLUMN_INDEX);
				}
			});
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void downloadURLChanged(Pic pic) {
		try {
			readLock.lock();
			int index = pics.indexOf(pic);

			executeInEventQueueThread(() -> {
				if (index > -1 && tableModel.getRowCount() > index) {
					// Change Cell
					tableModel.setValueAt(pic.getDownloadURL(), index, QueueTableModel.DOWNLOAD_URL_COLUMN_INDEX);
					tableModel.fireTableCellUpdated(index, QueueTableModel.DOWNLOAD_URL_COLUMN_INDEX);
				}
			});

			updatePic(pic);
		} finally {
			readLock.unlock();
		}
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
			EventQueue.invokeLater(r);
		}
	}
}

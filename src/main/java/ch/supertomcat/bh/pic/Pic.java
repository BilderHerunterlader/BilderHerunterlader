package ch.supertomcat.bh.pic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.downloader.FileDownloader;
import ch.supertomcat.bh.downloader.impl.HTTPFileDownloader;
import ch.supertomcat.bh.downloader.impl.LocalFileDownloader;
import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.queue.IDownloadListener;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Thread which does the download
 * This class is called Pic, because it is an shortcut for Picture.
 * And as is started programming this program it was only for downloading
 * pictures.
 * But this class can not only download pictures, all kind of files can
 * be downloaded.
 * And no, i'm not willing to rename this class ;-)
 */
public class Pic implements Runnable, IDownloadListener {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Database ID
	 */
	private int id = 0;

	/**
	 * Container-URL
	 */
	private String containerURL = "";

	/**
	 * Thread-URL (referrer)
	 */
	private String threadURL = "";

	/**
	 * Thumbnail-URL
	 */
	private String thumb = "";

	/**
	 * Target path
	 */
	private String targetPath = "";

	/**
	 * Filename
	 */
	private String targetFilename = "";

	/**
	 * This flag causes the class not to use the filename provided by a hostclass.
	 * So if the user changes the filename somewhere in the GUI, then this flag is
	 * set to true!
	 * If the filename is changed by the Set-Method to a empty string, then this flag
	 * is automatically set to false
	 */
	private boolean fixedTargetFilename = false;

	/**
	 * Timestamp of last modification of the file
	 * 
	 * @see ch.supertomcat.bh.importexport.ImportIradaTsv
	 */
	private long lastModified = 0;

	/**
	 * This flag causes the class to set the last modification time of the file
	 */
	private boolean fixedLastModified = false;

	/**
	 * Filesize
	 */
	private long size = 0;

	/**
	 * Listener
	 */
	private transient List<IPicListener> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Status of the download
	 */
	private PicState status = PicState.SLEEPING;

	/**
	 * Error-Message
	 */
	private String errMsg = "";

	/**
	 * Count of failures
	 */
	private int failedCount = 0;

	/**
	 * Download deactivated
	 */
	private boolean deactivated = false;

	/**
	 * Flag to let the download method rename the file with the filename from contentdisposition
	 */
	private boolean renameWithContentDisposition = false;

	/**
	 * Stop the download
	 */
	private boolean stop = false;

	/**
	 * This flag is set to true, when the stop-button on the gui is pressed once.
	 * On the first click, the running downloads are not stopped, only the ones are not running.
	 * On the second click, also running downloads are stopped imidiatly!
	 */
	private boolean stopOncePressed = false;

	/**
	 * Recalculate the downloadrate
	 */
	private boolean recalcutateRate = false;

	/**
	 * Date and Time
	 */
	private long dateTime;

	private String downloadURL = "";

	/**
	 * Progress
	 */
	private PicProgress progress = new PicProgress();

	/**
	 * Constructor
	 * 
	 * @param urlContainer Container-URL
	 * @param targetFilename Filename
	 * @param targetPath Target path
	 */
	public Pic(String urlContainer, String targetFilename, String targetPath) {
		urlContainer = HTTPUtil.trimURL(urlContainer);
		this.containerURL = urlContainer;
		this.targetPath = targetPath;
		if ((this.targetPath.endsWith("/") == false) && (this.targetPath.endsWith("\\") == false)) {
			this.targetPath += FileUtil.FILE_SEPERATOR;
		}
		this.targetFilename = targetFilename;
		this.dateTime = System.currentTimeMillis();
	}

	/**
	 * Constructor
	 * 
	 * @param id Database ID
	 * @param urlContainer Container-URL
	 * @param targetFilename Filename
	 * @param targetPath Target path
	 * @param threadURL Thread-URL
	 * @param thumb Thumbnail-URL
	 * @param downloadURL Download URL
	 * @param fixedTargetFilename Fixed Target Filename Flag
	 * @param lastModified Last Modified
	 * @param fixedLastModified Fixed Last Modified Flag
	 * @param size Size
	 * @param status Status
	 * @param errMsg Error Message
	 * @param deactivated Deactivated
	 * @param renameWithContentDisposition Rename With Content Disposition Flag
	 * @param dateTime DateTime
	 */
	public Pic(int id, String urlContainer, String targetFilename, String targetPath, String threadURL, String thumb, String downloadURL, boolean fixedTargetFilename, long lastModified,
			boolean fixedLastModified, long size, PicState status, String errMsg, boolean deactivated, boolean renameWithContentDisposition, long dateTime) {
		this(urlContainer, targetFilename, targetPath);
		this.id = id;
		this.threadURL = threadURL;
		this.thumb = thumb;
		this.downloadURL = downloadURL;
		this.fixedTargetFilename = fixedTargetFilename;
		this.lastModified = lastModified;
		this.fixedLastModified = fixedLastModified;
		this.size = size;
		this.status = status;
		this.errMsg = errMsg;
		this.deactivated = deactivated;
		this.renameWithContentDisposition = renameWithContentDisposition;
		this.dateTime = dateTime;
	}

	/**
	 * Start the download
	 */
	public synchronized void startDownload() {
		// If the download is deactivated, then we don't start the download
		if (this.deactivated) {
			return;
		}

		// If there is not already a request for a download-slot and the status is sleeping or failed
		if ((!(DownloadQueueManager.instance().isDLSlotListenerRegistered(this))) && ((this.status == PicState.SLEEPING) || (this.status == PicState.FAILED)
				|| (this.status == PicState.FAILED_FILE_NOT_EXIST) || (this.status == PicState.FAILED_FILE_TEMPORARY_OFFLINE))) {
			stop = false;
			stopOncePressed = false;
			setStatus(PicState.WAITING); // Change the status
			DownloadQueueManager.instance().addDLSlotListener(this); // Request a download slot
		}
	}

	/**
	 * Stop the download
	 * This does not immediately stop the download, it only changes
	 * the stop-flag to true and the download method will stop if this
	 * flag is true.
	 * When the download is waiting for the parsed URL, this could take some time...
	 */
	public synchronized void stopDownload() {
		if (stopOncePressed) {
			stop = true;
		}
		if (this.status == PicState.WAITING) {
			stop = true;
			setStatus(PicState.SLEEPING);

			progress.setBytesTotal(size);
			progress.setBytesDownloaded(0);
			progressUpdated();

			/*
			 * Free the download-slot
			 * Maybe i should do this not at this point, because the user could
			 * start the download again, before it has really stopped
			 */
			DownloadQueueManager.instance().removeDLSlotListenerStopping(this);
		} else if (this.status == PicState.DOWNLOADING) {
			if (stop) {
				setStatus(PicState.ABORTING);
			}
		}
		stopOncePressed = true;
	}

	@Override
	public void run() {
		/*
		 * So, first some information what happens in this method.
		 * 
		 * First we have a Container-URL. This is called so because when
		 * downloading images from imagehost, there is almost always a link
		 * to a page, which contains the image.
		 * So we get only the link to that page.
		 * To get the direct link to image, we ask the HostManager to parse the URL.
		 * Note, here also it doesn't have to be an image, the kind of file is
		 * unnessesary!
		 * So, now the HostManager goes trough all hostclasses and checks, if
		 * there is one, which is able to parse the URL. If there is one, the
		 * HostManager calls this class to do that. If this worked we get from the
		 * HostManager the parsed URL. If not we get null or and empty URL.
		 * 
		 * But in this process there could be problems. What happens, wenn in the method
		 * of a hostclass an error occured? Right, the complete download thread stands still!
		 * So, the download-slot is still been registered for this download and the
		 * download is unable to free it.
		 * 
		 * I defined a new type of Exception for this.
		 * See ch.supertomcat.bh.exceptions.HostException
		 * Because every hostclass has to implement the IHoster interface, the method
		 * to parse the URL of every class will throw this exception.
		 * But to really avoid this problem, i think i must define the method in the
		 * interface to throw all exception. Because the HostException is only thrown, when
		 * the programmer of a hostclass, throw one.
		 * Now wat happens, when a hostclass calls a method, which does
		 * not throw an exception?
		 * Right, again the Thread stands still. So, there is not really a possibillity to
		 * avoid this completely.
		 */
		if (stop) {
			// Status aendern und Slot freigeben
			setStatus(PicState.SLEEPING);

			progress.setBytesTotal(size);
			progress.setBytesDownloaded(0);
			progressUpdated();

			DownloadQueueManager.instance().removeDLSlotListener(this);
			return;
		}

		logger.info("Downloading: {}", containerURL);

		FileDownloader downloader;
		String encodedContainerURL = HTTPUtil.encodeURL(containerURL, true);
		if (HTTPUtil.isURL(encodedContainerURL)) {
			downloader = new HTTPFileDownloader();
		} else {
			downloader = new LocalFileDownloader();
		}
		try {
			downloader.downloadFile(this);
		} catch (HostException e) {
			// Nothing to do
		}
	}

	/**
	 * This method fires the targetChanged-Method on all listeners
	 */
	public void targetChanged() {
		for (IPicListener listener : listeners) {
			listener.targetChanged(this);
		}
	}

	/**
	 * Adds a listener
	 * 
	 * @param listener Listener
	 */
	public void addPicListener(IPicListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes a listener
	 * 
	 * @param listener Listener
	 */
	public void removePicListener(IPicListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	/**
	 * Removes all listeners
	 */
	public void removeAllListener() {
		listeners.clear();
	}

	@Override
	public boolean downloadAllowed() {
		/*
		 * When the startDownload-Method is called, the download requests the
		 * Queue for a download-slot. The Queue fires this method, as soon as there is
		 * free download-slot.
		 * This method returns true if the download is really started.
		 * If the status is something else than Pic.WAITING here, then
		 * we don't start the download! So we return false!
		 */

		if (this.status == PicState.WAITING) {
			// Set the status to DOWNLOADING
			setStatus(PicState.DOWNLOADING);

			// Now we make this Pic to a thread and start it
			Thread t = new Thread(this);
			t.setName("Download-Thread-" + t.getId());
			// We set the priority to a minimum, to reduce CPU-Usage
			t.setPriority(Thread.MIN_PRIORITY);
			t.start(); // Now we start the thread
			return true;
		}
		return false;
	}

	/**
	 * Checks if the download failed to often (defined by the user over the options)
	 * If so the download will be deactivated
	 */
	private void checkDeactivate() {
		if (SettingsManager.instance().getMaxFailedCount() <= 0) {
			return;
		}
		if (this.failedCount >= SettingsManager.instance().getMaxFailedCount()) {
			setDeactivated(true);
		}
	}

	/**
	 * Returns the Container-URL
	 * 
	 * @return Container-URL
	 */
	@Override
	public String getContainerURL() {
		return this.containerURL;
	}

	/**
	 * Returns path and filename
	 * Note: There is no set method for this, because path
	 * and filename are hold seperated in the class.
	 * 
	 * @return Path and filename
	 */
	public String getTarget() {
		return this.targetPath + this.targetFilename;
	}

	/**
	 * Returns the filesize
	 * 
	 * @return Filesize
	 */
	public long getSize() {
		return this.size;
	}

	/**
	 * Sets the filesize
	 * This method fires sizeChanged on all listeners and updates
	 * the object in the database, but does not commit the database!
	 * 
	 * @param size Filesize
	 */
	public void setSize(long size) {
		if (size >= 0) {
			this.size = size;
			for (IPicListener listener : listeners) {
				listener.sizeChanged(this);
			}
			QueueManager.instance().updatePic(this);
		}
	}

	/**
	 * Returns the status-text for the actual status of the download
	 * 
	 * @return Statustext
	 */
	public String getStatusText() {
		return status.getText();
	}

	/**
	 * Returns the status
	 * 
	 * @return Status
	 */
	public PicState getStatus() {
		return status;
	}

	/**
	 * Attention: You should not use the method from other classes.
	 * It could produce errors if set while download is running are
	 * so on.
	 * 
	 * I used this method only from other classes when reading them
	 * from the database, because if the program was crashed the
	 * object could have DOWNLOADING as status and that is terrible.
	 * 
	 * Sets the status of the download
	 * If the status-argument is not one of those, the
	 * method will not set anything!
	 * 
	 * This method fires the statusChanged-Method on all listeners.
	 * 
	 * @param status Status
	 */
	public void setStatus(PicState status) {
		setStatus(status, "");
	}

	/**
	 * Sets the status of the download
	 * If the status-argument is not one of those, the
	 * method will not set anything!
	 * 
	 * This method fires the statusChanged-Method on all listeners and
	 * updates the object in the database, but does not commit the database!
	 * 
	 * @param status Status
	 * @param errMsg Error-Message
	 */
	public void setStatus(PicState status, String errMsg) {
		this.status = status;
		this.errMsg = errMsg;
		if (status == PicState.COMPLETE) {
			this.dateTime = System.currentTimeMillis();
		}
		for (IPicListener listener : listeners) {
			listener.statusChanged(this);
		}
		if ((status != PicState.SLEEPING) && (status != PicState.WAITING) && (status != PicState.DOWNLOADING) && (status != PicState.ABORTING)) {
			/*
			 * We can gain some speed by only updating the status in the database in some cases.
			 * When pics are loaded, they reset to SLEEPING if the status is one of the three in the condition above.
			 * So we don't need to save the status in those cases.
			 */
			QueueManager.instance().updatePic(this);
		}
	}

	/**
	 * Returns the timestamp of last modification of the file
	 * 
	 * @return Last modification timestamp
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * Set the timestamp of last modification of the file
	 * 
	 * @param lastModified Last modification timestamp
	 */
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * Returns if lastModified is fixed or not
	 * 
	 * @return Fixed lastModified
	 */
	public boolean isFixedLastModified() {
		return fixedLastModified;
	}

	/**
	 * Sets the last modified timestamp to be fixed.
	 * Only if this is set to true, the download-class will
	 * set the last modified timestamp on the file.
	 * 
	 * @param fixedLastModified Fixed lastModified
	 */
	public void setFixedLastModified(boolean fixedLastModified) {
		this.fixedLastModified = fixedLastModified;
	}

	/**
	 * Returns the path
	 * 
	 * @return Path
	 */
	public String getTargetPath() {
		return targetPath;
	}

	/**
	 * Sets the Path
	 * 
	 * @param targetPath Path
	 */
	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
		if ((this.targetPath.endsWith("/") == false) && (this.targetPath.endsWith("\\") == false)) {
			this.targetPath += FileUtil.FILE_SEPERATOR;
		}
		targetChanged();
	}

	/**
	 * Returns the filename
	 * 
	 * @return Filename
	 */
	public String getTargetFilename() {
		return targetFilename;
	}

	/**
	 * Sets the filename
	 * If the filename was set to be fixed before and the argument for
	 * targetFilename is here an empty string, the filename is set to be
	 * not fixed anymore.
	 * This methods fires the targetChanged-Method on all listeners
	 * 
	 * @param targetFilename Filename
	 */
	public void setTargetFilename(String targetFilename) {
		if (!targetFilename.isEmpty()) {
			this.targetFilename = targetFilename;
		} else {
			this.targetFilename = Localization.getString("Unkown");
			this.fixedTargetFilename = false;
		}
		targetChanged();
	}

	/**
	 * Returns the Thumbnail-URL
	 * 
	 * @return Thumbnail-URL
	 */
	public String getThumb() {
		return thumb;
	}

	/**
	 * Set the Thumbnail-URL
	 * 
	 * @param thumb Thumbnail-URL
	 */
	public void setThumb(String thumb) {
		thumb = HTTPUtil.trimURL(thumb);
		this.thumb = thumb;
	}

	/**
	 * Returns if the filename is fixed or not
	 * 
	 * @see ch.supertomcat.bh.pic.Pic#setFixedTargetFilename(boolean)
	 * @return the fixedTargetFilename
	 */
	public boolean isFixedTargetFilename() {
		return fixedTargetFilename;
	}

	/**
	 * Sets the filename to be fixed. So the download or the hostclasses
	 * are not overwriting it!
	 * Note: When adding URLs to BilderHerunterlader, then the filenames
	 * are most only grabbed from the URL. But maybe hostclasses has methods
	 * to restore the original filename or get a better one. If not, when the
	 * direct link is known by the download, the download-class could, grab a
	 * nicer filename, as the ones in the Download-Selection-Dialog.
	 * So a filename should only set to fixed, when a user changes the filename
	 * in the GUI.
	 * 
	 * @param fixedTargetFilename Fixed filename
	 */
	public void setFixedTargetFilename(boolean fixedTargetFilename) {
		this.fixedTargetFilename = fixedTargetFilename;
	}

	/**
	 * Returns the ThreadURL (Referrer)
	 * 
	 * @return ThreadURL (Referrer)
	 */
	public String getThreadURL() {
		return threadURL;
	}

	/**
	 * Set the threadURL (Referrer)
	 * 
	 * @param threadURL ThreadURL (Referrer)
	 */
	public void setThreadURL(String threadURL) {
		this.threadURL = HTTPUtil.trimURL(threadURL);
	}

	/**
	 * Returns the count of failures
	 * 
	 * @return Count of failures
	 */
	public int getFailedCount() {
		return failedCount;
	}

	/**
	 * Increases the failedCount and deactivates the Pic if necessary
	 */
	public void increaseFailedCount() {
		failedCount++;
		checkDeactivate(); // check if the download has to be deactivated
	}

	/**
	 * Sets the failedCount to maximum failed count and deactivates the Pic if necessary
	 */
	public void setToMaxFailedCount() {
		failedCount = SettingsManager.instance().getMaxFailedCount();
		checkDeactivate(); // check if the download has to be deactivated
	}

	/**
	 * Returns if the download is deactivated or not
	 * 
	 * @return Deactivated
	 */
	public boolean isDeactivated() {
		return deactivated;
	}

	/**
	 * Sets the download to be deactivated or not
	 * This method fires deactivatedChange-Method on all listeners and
	 * updates the object in the database. But does not commit the database!
	 * 
	 * @param deactivated Deactivated
	 */
	public void setDeactivated(boolean deactivated) {
		this.deactivated = deactivated;
		this.failedCount = 0;
		for (IPicListener listener : listeners) {
			listener.deactivatedChanged(this);
		}
		QueueManager.instance().updatePic(this);
	}

	/**
	 * Returns the Error-Message
	 * 
	 * @return Error-Message
	 */
	public String getErrMsg() {
		return errMsg;
	}

	/**
	 * Returns the renameWithContentDisposition
	 * 
	 * @return renameWithContentDisposition
	 */
	public boolean isRenameWithContentDisposition() {
		return renameWithContentDisposition;
	}

	/**
	 * Sets the renameWithContentDisposition
	 * 
	 * @param renameWithContentDisposition renameWithContentDisposition
	 */
	public void setRenameWithContentDisposition(boolean renameWithContentDisposition) {
		this.renameWithContentDisposition = renameWithContentDisposition;
	}

	/**
	 * Get-Method
	 * 
	 * @return Formatted date and time
	 */
	public String getDateTime() {
		String retval = "";
		if (dateTime < 0) {
			return retval;
		}
		DateFormat df = new SimpleDateFormat();
		retval = df.format(new Date(dateTime));
		return retval;
	}

	/**
	 * Get-Method
	 * 
	 * @return Date and time
	 */
	public long getDateTimeSimple() {
		return dateTime;
	}

	/**
	 * Returns the downloadURL
	 * 
	 * @return downloadURL
	 */
	public String getDownloadURL() {
		return downloadURL;
	}

	/**
	 * Sets the downloadURL
	 * 
	 * @param downloadURL downloadURL
	 */
	public void setDownloadURL(String downloadURL) {
		this.downloadURL = downloadURL;
	}

	/**
	 * Returns the id
	 * 
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id
	 * 
	 * @param id id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns the recalcutateRate
	 * 
	 * @return recalcutateRate
	 */
	public boolean isRecalcutateRate() {
		return recalcutateRate;
	}

	@Override
	public void recalcutateRate() {
		/*
		 * Set the recalculate flag to true
		 */
		this.recalcutateRate = true;
	}

	/**
	 * Sets the recalcutateRate to false
	 */
	public void rateRecalculated() {
		this.recalcutateRate = false;
	}

	@Override
	public double getDownloadRate() {
		return progress.getRate();
	}

	/**
	 * Returns the progress
	 * 
	 * @return progress
	 */
	public PicProgress getProgress() {
		return progress;
	}

	/**
	 * Progress Updated
	 */
	public void progressUpdated() {
		// Now let all listeners know, that that they have to update their progressbar
		for (IPicListener listener : listeners) {
			listener.progressChanged(this);
		}
	}

	/**
	 * Returns the stop
	 * 
	 * @return stop
	 */
	public boolean isStop() {
		return stop;
	}
}

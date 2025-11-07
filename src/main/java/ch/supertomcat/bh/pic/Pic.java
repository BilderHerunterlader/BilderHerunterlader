package ch.supertomcat.bh.pic;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ch.supertomcat.bh.downloader.FileDownloaderFactory;
import ch.supertomcat.bh.hoster.Hoster;
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
public class Pic {
	/**
	 * Date Format
	 */
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

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
	private List<IPicListener> listeners = new CopyOnWriteArrayList<>();

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

	/**
	 * Download URL
	 */
	private String downloadURL = "";

	/**
	 * Hoster (This is the last detected hoster for this container URL, this member is only used for displaying the hoster in the queue. But it is not used for
	 * anything else. When the download starts the hoster is evaluated again and this member update.)
	 */
	private Hoster hoster = null;

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
		if (!this.targetPath.endsWith("/") && !this.targetPath.endsWith("\\")) {
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

	/**
	 * Checks if the download failed to often (defined by the user over the options)
	 * If so the download will be deactivated
	 * 
	 * @param maxFailedCount Max Failed Count
	 */
	private void checkDeactivate(int maxFailedCount) {
		if (maxFailedCount <= 0) {
			return;
		}
		if (this.failedCount >= maxFailedCount) {
			setDeactivated(true, true);
		}
	}

	/**
	 * Returns the Container-URL
	 * 
	 * @return Container-URL
	 */
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
		if (!this.targetPath.endsWith("/") && !this.targetPath.endsWith("\\")) {
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
	 * 
	 * @param maxFailedCount Max Failed Count
	 */
	public void increaseFailedCount(int maxFailedCount) {
		failedCount++;
		checkDeactivate(maxFailedCount); // check if the download has to be deactivated
	}

	/**
	 * Sets the failedCount to maximum failed count and deactivates the Pic if necessary
	 * 
	 * @param maxFailedCount Max Failed Count
	 */
	public void setToMaxFailedCount(int maxFailedCount) {
		failedCount = maxFailedCount;
		checkDeactivate(maxFailedCount); // check if the download has to be deactivated
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
	 * @param callListeners True if listeners should be called, false otherwise
	 */
	public void setDeactivated(boolean deactivated, boolean callListeners) {
		this.deactivated = deactivated;
		this.failedCount = 0;
		if (callListeners) {
			for (IPicListener listener : listeners) {
				listener.deactivatedChanged(this);
			}
		}
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
		if (dateTime < 0) {
			return "";
		}
		return DATE_FORMAT.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), ZoneId.systemDefault()));
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
		for (IPicListener listener : listeners) {
			listener.downloadURLChanged(this);
		}
	}

	/**
	 * Returns the hoster
	 * 
	 * @return hoster
	 */
	public Hoster getHoster() {
		return hoster;
	}

	/**
	 * Sets the hoster
	 * 
	 * @param hoster hoster
	 */
	public void setHoster(Hoster hoster) {
		this.hoster = hoster;
		for (IPicListener listener : listeners) {
			listener.hosterChanged(this);
		}
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

	/**
	 * The pic should now recalculate the download-rate
	 */
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

	/**
	 * Returns the current bitrate
	 * 
	 * @return Bitrate
	 */
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

	/**
	 * Sets the stop
	 * 
	 * @param stop stop
	 */
	public void setStop(boolean stop) {
		this.stop = stop;
	}

	/**
	 * Returns the stopOncePressed
	 * 
	 * @return stopOncePressed
	 */
	public boolean isStopOncePressed() {
		return stopOncePressed;
	}

	/**
	 * Sets the stopOncePressed
	 * 
	 * @param stopOncePressed stopOncePressed
	 */
	public void setStopOncePressed(boolean stopOncePressed) {
		this.stopOncePressed = stopOncePressed;
	}

	/**
	 * Prepare Download
	 * 
	 * @param fileDownloaderFactory FileDownloaderFactory
	 * @return PicDownloadListener or null if download is not allowed
	 */
	public PicDownloadListener prepareDownload(FileDownloaderFactory fileDownloaderFactory) {
		if (deactivated) {
			return null;
		}

		if (status == PicState.SLEEPING || status == PicState.FAILED || status == PicState.FAILED_FILE_NOT_EXIST || status == PicState.FAILED_FILE_TEMPORARY_OFFLINE) {
			setStop(false);
			setStopOncePressed(false);
			setStatus(PicState.WAITING);

			return new PicDownloadListener(this, fileDownloaderFactory);
		}
		return null;
	}

	/**
	 * Stop Download
	 */
	public void stopDownload() {
		if (isStopOncePressed()) {
			setStop(true);
		}

		if (status == PicState.WAITING) {
			setStop(true);
			setStatus(PicState.SLEEPING);

			progress.setBytesTotal(size);
			progress.setBytesDownloaded(0);
			progressUpdated();
		} else if (status == PicState.DOWNLOADING && isStop()) {
			setStatus(PicState.ABORTING);
		}
		setStopOncePressed(true);
	}
}

package ch.supertomcat.bh.log;

import ch.supertomcat.bh.pic.Pic;

/**
 * Log Entry
 */
public class LogEntry {
	/**
	 * ID
	 */
	private int id;

	/**
	 * Timestamp
	 */
	private final long timestamp;

	/**
	 * Container URL
	 */
	private final String containerURL;

	/**
	 * Thread URL
	 */
	private final String threadURL;

	/**
	 * Download URL
	 */
	private final String downloadURL;

	/**
	 * Thumb URL
	 */
	private final String thumbURL;

	/**
	 * Target (path and filename)
	 */
	private final String target;

	/**
	 * Target path
	 */
	private final String targetPath;

	/**
	 * Filename
	 */
	private final String targetFilename;

	/**
	 * Size
	 */
	private final long size;

	/**
	 * Constructor
	 * 
	 * @param timestamp Timestamp
	 * @param containerURL Container URL
	 * @param threadURL Thread URL
	 * @param downloadURL Download URL
	 * @param thumbURL Thumb URL
	 * @param target Target (path and filename)
	 * @param targetPath Filename
	 * @param targetFilename Filename
	 * @param size Size
	 */
	public LogEntry(long timestamp, String containerURL, String threadURL, String downloadURL, String thumbURL, String target, String targetPath, String targetFilename, long size) {
		this.timestamp = timestamp;
		this.containerURL = containerURL;
		this.threadURL = threadURL;
		this.downloadURL = downloadURL;
		this.thumbURL = thumbURL;
		this.target = target;
		this.targetPath = targetPath;
		this.targetFilename = targetFilename;
		this.size = size;
	}

	/**
	 * Constructor
	 * 
	 * @param id ID
	 * @param timestamp Timestamp
	 * @param containerURL Container URL
	 * @param threadURL Thread URL
	 * @param downloadURL Download URL
	 * @param thumbURL Thumb URL
	 * @param target Target (path and filename)
	 * @param targetPath Filename
	 * @param targetFilename Filename
	 * @param size Size
	 */
	public LogEntry(int id, long timestamp, String containerURL, String threadURL, String downloadURL, String thumbURL, String target, String targetPath, String targetFilename, long size) {
		this.id = id;
		this.timestamp = timestamp;
		this.containerURL = containerURL;
		this.threadURL = threadURL;
		this.downloadURL = downloadURL;
		this.thumbURL = thumbURL;
		this.target = target;
		this.targetPath = targetPath;
		this.targetFilename = targetFilename;
		this.size = size;
	}

	/**
	 * Constructor
	 * 
	 * @param pic Pic
	 */
	public LogEntry(Pic pic) {
		this.timestamp = pic.getDateTimeSimple();
		this.containerURL = pic.getContainerURL();
		this.threadURL = pic.getThreadURL();
		this.downloadURL = pic.getDownloadURL();
		this.thumbURL = pic.getThumb();
		this.target = pic.getTarget();
		this.targetPath = pic.getTargetPath();
		this.targetFilename = pic.getTargetFilename();
		this.size = pic.getSize();
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
	 * Returns the timestamp
	 * 
	 * @return timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Returns the containerURL
	 * 
	 * @return containerURL
	 */
	public String getContainerURL() {
		return containerURL;
	}

	/**
	 * Returns the threadURL
	 * 
	 * @return threadURL
	 */
	public String getThreadURL() {
		return threadURL;
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
	 * Returns the thumbURL
	 * 
	 * @return thumbURL
	 */
	public String getThumbURL() {
		return thumbURL;
	}

	/**
	 * Returns the targetPath
	 * 
	 * @return targetPath
	 */
	public String getTargetPath() {
		return targetPath;
	}

	/**
	 * Returns the targetFilename
	 * 
	 * @return targetFilename
	 */
	public String getTargetFilename() {
		return targetFilename;
	}

	/**
	 * Returns the target
	 * 
	 * @return target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * Returns the size
	 * 
	 * @return size
	 */
	public long getSize() {
		return size;
	}
}

package ch.supertomcat.bh.log;

/**
 * Directory Log Object
 */
public class DirectoryLogObject {
	private final String directory;
	private long dateTime;
	private final boolean exists;

	/**
	 * Constructor
	 * 
	 * @param directory Directory
	 * @param dateTime DateTime
	 * @param exists Exists
	 */
	public DirectoryLogObject(String directory, long dateTime, boolean exists) {
		this.directory = directory;
		this.dateTime = dateTime;
		this.exists = exists;
	}

	/**
	 * Constructor
	 * 
	 * @param entry LogEntry
	 * @param exists Exists
	 */
	public DirectoryLogObject(LogEntry entry, boolean exists) {
		this.directory = entry.getTargetPath();
		this.dateTime = entry.getTimestamp();
		this.exists = exists;
	}

	/**
	 * Returns the directory
	 * 
	 * @return directory
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * Returns the dateTime
	 * 
	 * @return dateTime
	 */
	public long getDateTime() {
		return dateTime;
	}

	/**
	 * Sets the dateTime
	 * 
	 * @param dateTime dateTime
	 */
	public void setDateTime(long dateTime) {
		if (dateTime > this.dateTime) {
			this.dateTime = dateTime;
		}
	}

	/**
	 * Returns the exists
	 * 
	 * @return exists
	 */
	public boolean isExists() {
		return exists;
	}
}

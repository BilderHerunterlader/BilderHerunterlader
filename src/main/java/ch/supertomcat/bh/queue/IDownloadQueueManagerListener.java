package ch.supertomcat.bh.queue;

/**
 * Interface of QueueData to let listeners know when
 * downloads-slots where changed or the count of
 * downloaded files or bytes has changed.
 */
public interface IDownloadQueueManagerListener {
	
	/**
	 * Download-Slots has changed.
	 * @param queue Amount of downloads in the queue
	 * @param openSlots Free download-slots
	 * @param maxSlots Maximum download-slots
	 */
	public void queueChanged(int queue, int openSlots, int maxSlots);
	
	/**
	 * Changes of the count of downloaded files since application started
	 * @param count Count of downloaded files
	 */
	public void sessionDownloadedFilesChanged(int count);
	
	/**
	 * Changes of the count of downloaded bytes since application started
	 * @param count Count of downloaded bytes
	 */
	public void sessionDownloadedBytesChanged(long count);
	
	/**
	 * @param queue Queue
	 * @param openSlots Open Slots
	 * @param maxSlots Max Slots
	 */
	public void downloadsComplete(int queue, int openSlots, int maxSlots);
	
	/**
	 * @param downloadRate Download Rate
	 */
	public void totalDownloadRateCalculated(double downloadRate);
}

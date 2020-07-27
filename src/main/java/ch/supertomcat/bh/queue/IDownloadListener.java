package ch.supertomcat.bh.queue;

/**
 * Interface of QueueData for downloads, so they will
 * know, when they are allowed to start the download, after
 * they requested a download-slot.
 */
public interface IDownloadListener {
	/**
	 * Returns the Container-URL of a listener
	 * Since QueueData contains counters for the connections per domain
	 * the removeListener-Method needs the Container-URL to decrause the
	 * counter for the domain of the Container-URL. I could also check if
	 * the listener is a Pic and then cast to Pic, but i did it with this
	 * way.
	 * 
	 * @return Container-URL
	 */
	public String getContainerURL();

	/**
	 * The pic should now recalculate the download-rate
	 */
	public void recalcutateRate();

	/**
	 * Returns the current bitrate
	 * 
	 * @return Bitrate
	 */
	public double getDownloadRate();
}

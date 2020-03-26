package ch.supertomcat.bh.queue;

import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;

/**
 * Interface of QueueData for downloads, so they will
 * know, when they are allowed to start the download, after
 * they requested a download-slot.
 */
public interface IDownloadListener {
	/**
	 * Allows a Pic to start the download
	 * The Pic should then return true, if it
	 * has really started the pic or false if not.
	 * If it does return a wrong value, QueueData would
	 * start too many downloads or would not increase the counter
	 * and would not increase the slots used.
	 * 
	 * @param downloadQueueManager Download Queue Manager
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param cookieManager Cookie Manager
	 * @param hostManager Host Manager
	 * 
	 * @return True/False
	 */
	public boolean downloadAllowed(DownloadQueueManager downloadQueueManager, ProxyManager proxyManager, SettingsManager settingsManager, CookieManager cookieManager, HostManager hostManager);

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

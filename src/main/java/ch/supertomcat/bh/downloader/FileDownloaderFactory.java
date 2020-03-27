package ch.supertomcat.bh.downloader;

import ch.supertomcat.bh.downloader.impl.HTTPFileDownloader;
import ch.supertomcat.bh.downloader.impl.LocalFileDownloader;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;

/**
 * Factory to create file downloaders
 */
public class FileDownloaderFactory {
	/**
	 * Download Queue Manager
	 */
	private final DownloadQueueManager downloadQueueManager;

	/**
	 * Proxy Manager
	 */
	private final ProxyManager proxyManager;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Cookie Manager
	 */
	private final CookieManager cookieManager;

	/**
	 * Host Manager
	 */
	private final HostManager hostManager;

	/**
	 * Constructor
	 * 
	 * @param downloadQueueManager Download Queue Manager
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param cookieManager Cookie Manager
	 * @param hostManager Host Manager
	 */
	public FileDownloaderFactory(DownloadQueueManager downloadQueueManager, ProxyManager proxyManager, SettingsManager settingsManager, CookieManager cookieManager, HostManager hostManager) {
		this.downloadQueueManager = downloadQueueManager;
		this.proxyManager = proxyManager;
		this.settingsManager = settingsManager;
		this.cookieManager = cookieManager;
		this.hostManager = hostManager;
	}

	/**
	 * Create HTTP File Downloader
	 * 
	 * @return HTTP File Downloader
	 */
	public FileDownloader createHTTPFileDownloader() {
		return new HTTPFileDownloader(downloadQueueManager, proxyManager, settingsManager, cookieManager, hostManager);
	}

	/**
	 * Create Local File Downloader
	 * 
	 * @return Local File Downloader
	 */
	public FileDownloader createLocalFileDownloader() {
		return new LocalFileDownloader(downloadQueueManager, settingsManager, hostManager);
	}
}

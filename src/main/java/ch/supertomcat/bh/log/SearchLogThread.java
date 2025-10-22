package ch.supertomcat.bh.log;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.database.sqlite.LogsSQLiteDB;
import ch.supertomcat.bh.pic.URL;

/**
 * Thread for searching for already downloaded files
 */
public class SearchLogThread implements Runnable {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final String HTTP = "http";

	private final List<URL> urls;

	private final int threadNumber;

	private final int threadCount;

	private final LogsSQLiteDB database;

	private final CyclicBarrier barrier;

	private final int urlsToCheck;

	private int start = 0;

	/**
	 * Constructor
	 * 
	 * @param urls URLs
	 * @param threadNumber Thread-Number
	 * @param threadCount Thread-Count
	 * @param database Database
	 * @param barrier Barrier
	 * @param urlsToCheck URLs to check per cycle
	 */
	public SearchLogThread(List<URL> urls, int threadNumber, int threadCount, LogsSQLiteDB database, CyclicBarrier barrier, int urlsToCheck) {
		this.urls = urls;
		this.threadNumber = threadNumber;
		this.threadCount = threadCount;
		this.database = database;
		this.barrier = barrier;
		this.urlsToCheck = urlsToCheck;
	}

	@Override
	public void run() {
		for (int i = start + threadNumber; i < urls.size() && i < start + threadNumber + urlsToCheck; i += threadCount) {
			URL currentURL = urls.get(i);

			if (currentURL.isAlreadyDownloaded()) {
				// URL was already downloaded
				continue;
			}

			String url = currentURL.getURL();
			String alternativeURL = url;
			if (currentURL.isHttpsURL()) {
				alternativeURL = HTTP + url.substring(5);
			}

			if (database.checkAlreadyDownloaded(url, alternativeURL)) {
				currentURL.setAlreadyDownloaded(true);
			}
		}

		start += urlsToCheck;

		/*
		 * Wait for all other threads to complete
		 */
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			logger.error("Could not await barrier", e);
		}
	}
}

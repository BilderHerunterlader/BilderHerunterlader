package ch.supertomcat.bh.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private final List<String> currentRows;

	private final CyclicBarrier barrier;

	private final Map<Integer, String> httpsMap = new HashMap<>();

	/**
	 * Constructor
	 * 
	 * @param urls URLs
	 * @param threadNumber Thread-Number
	 * @param threadCount Thread-Count
	 * @param currentRows Current Rows
	 * @param barrier Barrier
	 */
	public SearchLogThread(List<URL> urls, int threadNumber, int threadCount, List<String> currentRows, CyclicBarrier barrier) {
		this.urls = urls;
		this.threadNumber = threadNumber;
		this.threadCount = threadCount;
		this.currentRows = currentRows;
		this.barrier = barrier;
	}

	@Override
	public void run() {
		for (int i = threadNumber; i < urls.size(); i += threadCount) {
			URL currentURL = urls.get(i);

			if (currentURL.isAlreadyDownloaded()) {
				// URL was already downloaded
				continue;
			}

			String url = currentURL.getURL();
			for (int r = 0; r < currentRows.size(); r++) {
				String alreadyDownloadedURL = currentRows.get(r);

				if (alreadyDownloadedURL.contains(url)) {
					currentURL.setAlreadyDownloaded(true);
					break;
				}

				if (currentURL.isHttpsURL()) {
					String httpsURL = httpsMap.get(i);
					if (httpsURL == null) {
						httpsURL = HTTP + url.substring(5);
						httpsMap.put(i, httpsURL);
					}

					if (alreadyDownloadedURL.contains(httpsURL)) {
						currentURL.setAlreadyDownloaded(true);
						break;
					}
				}
			}
		}

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

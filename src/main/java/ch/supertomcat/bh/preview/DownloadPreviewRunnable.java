package ch.supertomcat.bh.preview;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.pic.URL;

/**
 * Thread for downloading previews in the background
 */
public class DownloadPreviewRunnable implements Runnable {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Cache
	 */
	private final PreviewCache previewCache;

	/**
	 * Preview Height
	 */
	private final int previewHeight;

	/**
	 * URLs
	 */
	private final List<URL> urls;

	/**
	 * Stop Flag
	 */
	private boolean stop = false;

	/**
	 * Constructor
	 * 
	 * @param previewCache Cache
	 * @param previewHeight Preview Height
	 * @param urls URLs
	 */
	public DownloadPreviewRunnable(PreviewCache previewCache, int previewHeight, List<URL> urls) {
		this.previewCache = previewCache;
		this.previewHeight = previewHeight;
		this.urls = urls;
	}

	/**
	 * Stop thread
	 */
	public void stop() {
		stop = true;
	}

	@Override
	public void run() {
		try (ExecutorService threadPool = Executors.newFixedThreadPool(6)) {
			CompletionService<PreviewDownloader.DownloadedPreview> completionService = new ExecutorCompletionService<>(threadPool);
			int scheduledTaskCount = 0;
			for (int i = 0; i < urls.size(); i++) {
				URL url = urls.get(i);
				if (!url.getThumb().isEmpty()) {
					completionService.submit(new PreviewDownloader(i, url.getThumb(), previewHeight));
					scheduledTaskCount++;
				}
			}

			threadPool.shutdown();

			for (int i = 0; i < scheduledTaskCount; i++) {
				try {
					Future<PreviewDownloader.DownloadedPreview> future = completionService.take();
					final PreviewDownloader.DownloadedPreview downloadedPreview = future.get();

					if (stop) {
						threadPool.shutdownNow();
						break;
					}

					if (downloadedPreview.getPreview() != null) {
						previewCache.addPreview(downloadedPreview.getUrl(), downloadedPreview.getPreview());
					}
				} catch (InterruptedException e) {
					logger.error("Wait for Preview Download Task was interrupted", e);
				} catch (ExecutionException e) {
					logger.error("Preview Download Task failed", e);
				} catch (CancellationException e) {
					logger.error("Preview Download Task was cancelled", e);
				}

				if (stop) {
					threadPool.shutdownNow();
					break;
				}
			}
		} catch (RejectedExecutionException e) {
			logger.error("Preview Download Task was rejected", e);
		} finally {
			previewCache.notifyAllPreviewsAdded();
			System.gc();
		}
	}
}

package ch.supertomcat.bh.hoster.urlchecker;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.supertomcattools.settingstools.options.OptionBoolean;

/**
 * Thread for removing duplicates from download-selection
 */
public class RemoveDuplicatesRunnable implements Runnable {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	private final List<URL> originalUrls;

	private final OptionBoolean bContains;

	private int currentRow;

	private final int threadNumber;

	private final int threadCount;

	private final CyclicBarrier barrier;

	/**
	 * Constructor
	 * 
	 * @param originalUrls Original URLs
	 * @param threadNumber Thread-Number
	 * @param threadCount Thread-Count
	 * @param bContains Contains
	 * @param currentRow Current Row
	 * @param barrier Barrier
	 */
	public RemoveDuplicatesRunnable(List<URL> originalUrls, int threadNumber, int threadCount, OptionBoolean bContains, int currentRow, CyclicBarrier barrier) {
		this.originalUrls = originalUrls;
		this.threadNumber = threadNumber;
		this.threadCount = threadCount;
		this.bContains = bContains;
		this.currentRow = currentRow;
		this.barrier = barrier;
	}

	@Override
	public synchronized void run() {
		/*
		 * We check here if a duplicate is present
		 * Here could problems appear, because the equals-Method of URL is overwritten,
		 * so that Host-Classes or Rules can define themselves which of the duplicates
		 * should be removed.
		 */
		URL currentURL = originalUrls.get(currentRow);
		for (int x = threadNumber; x < originalUrls.size(); x += threadCount) {
			if (bContains.getValue()) {
				// Another thread already found a duplicate
				break;
			}
			if (currentRow == x) {
				// Same row as row, which is checked, so ignore
				continue;
			}
			if (currentURL.equalsRemoveDuplicates(originalUrls.get(x))) {
				// Duplicate found
				bContains.setValue(true);
				break;
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

	/**
	 * Returns the currentRow
	 * 
	 * @return currentRow
	 */
	public synchronized int getCurrentRow() {
		return currentRow;
	}

	/**
	 * Sets the currentRow
	 * 
	 * @param currentRow currentRow
	 */
	public synchronized void setCurrentRow(int currentRow) {
		this.currentRow = currentRow;
	}
}

package ch.supertomcat.bh.queue;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This timer sets a flag in the Pic to true, causing the Pic to recalculate
 * the downloadrate.
 * The is used to, recalculate the downloadrate after a specific amount of time
 */
public class CalculateRateTimerTask extends TimerTask {
	private List<ICalculateRateTimer> listenersCalculateRate = new CopyOnWriteArrayList<>();

	/**
	 * pics
	 */
	private List<IDownloadListener> listeners = null;

	/**
	 * Download Queue Manager
	 */
	private final DownloadQueueManager downloadQueueManager;

	/**
	 * Constructor
	 * 
	 * @param downloadQueueManager Download Queue Manager
	 * 
	 * @param listeners Listeners
	 */
	public CalculateRateTimerTask(DownloadQueueManager downloadQueueManager, List<IDownloadListener> listeners) {
		this.downloadQueueManager = downloadQueueManager;
		this.listeners = listeners;
	}

	@Override
	public void run() {
		synchronized (downloadQueueManager) {
			double downloadRate = 0;
			boolean rateChanged = false;
			for (IDownloadListener listener : listeners) {
				if (listener != null) {
					double currentRate = listener.getDownloadRate();
					if (currentRate >= 0) {
						downloadRate += currentRate;
						rateChanged = true;
					}
					listener.recalcutateRate();
				}
			}
			if (rateChanged == false) {
				downloadRate = -1;
			}
			for (ICalculateRateTimer listener : listenersCalculateRate) {
				listener.totalDownloadRateCalculated(downloadRate);
			}
		}
	}

	/**
	 * @param listener Listener
	 */
	public synchronized void addCalculateRateListener(ICalculateRateTimer listener) {
		if (listenersCalculateRate.contains(listener) == false) {
			listenersCalculateRate.add(listener);
		}
	}

	/**
	 * @param listener Listener
	 */
	public synchronized void removeCalculateRateListener(ICalculateRateTimer listener) {
		listenersCalculateRate.remove(listener);
	}
}

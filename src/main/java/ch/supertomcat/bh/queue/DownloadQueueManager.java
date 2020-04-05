package ch.supertomcat.bh.queue;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import ch.supertomcat.bh.pic.PicDownloadListener;
import ch.supertomcat.bh.pic.PicDownloadResult;
import ch.supertomcat.bh.pic.PicState;
import ch.supertomcat.bh.settings.BHSettingsListener;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.queue.QueueManagerBase;
import ch.supertomcat.supertomcatutils.queue.QueueTask;
import ch.supertomcat.supertomcatutils.queue.QueueTaskFactory;
import ch.supertomcat.supertomcatutils.queue.Restriction;

/**
 * This class manages the download-slots.
 * It contains also the counters and restrictions for domains.
 * So this class allows a Pic to download or let them wait
 * until a slot is free.
 * 
 * @see ch.supertomcat.bh.queue.DownloadRestriction
 */
public class DownloadQueueManager extends QueueManagerBase<PicDownloadListener, PicDownloadResult> {
	/**
	 * Listener
	 */
	private List<IDownloadQueueManagerListener> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Restrictions
	 */
	private final DownloadQueueManagerRestrictions restrictions;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Total Download Bitrate
	 */
	private double totalDownloadBitrate = -1;

	/**
	 * Download Rate Timer
	 */
	private Timer timer = new Timer("Download-Rate-Timer");

	/**
	 * Calculate Rate Listeners
	 */
	private List<IDownloadListener> calculateRateListeners = new CopyOnWriteArrayList<>();

	/**
	 * Calculate Rate Timer Task
	 */
	private CalculateRateTimerTask calculateRateTimerTask = null;

	/**
	 * Calculate Rate Timer Listener
	 */
	private final ICalculateRateTimer calculateRateTimerListener = new ICalculateRateTimer() {
		@Override
		public void totalDownloadRateCalculated(double downloadRate) {
			totalDownloadBitrate = downloadRate;
			for (IDownloadQueueManagerListener listener : listeners) {
				listener.totalDownloadRateCalculated(downloadRate);
			}
		}
	};

	/**
	 * Constructor
	 * 
	 * @param restrictions Restrictions
	 * @param settingsManager Settings Manager
	 * @param queueTaskFactory Queue Task Factory
	 */
	public DownloadQueueManager(DownloadQueueManagerRestrictions restrictions, SettingsManager settingsManager, QueueTaskFactory<PicDownloadListener, PicDownloadResult> queueTaskFactory) {
		super(queueTaskFactory, settingsManager.getConnections(), settingsManager.getConnectionsPerHost());
		this.restrictions = restrictions;
		this.settingsManager = settingsManager;
		this.settingsManager.addSettingsListener(new BHSettingsListener() {
			@Override
			public void settingsChanged() {
				setMaxConnectionCount(settingsManager.getConnections());
				setMaxConnectionCountPerHost(settingsManager.getConnectionsPerHost());
			}

			@Override
			public void lookAndFeelChanged(int lookAndFeel) {
				// Nothing to do
			}
		});

		init();
	}

	@Override
	public synchronized void increaseSessionFiles() {
		super.increaseSessionFiles();
		for (IDownloadQueueManagerListener listener : listeners) {
			listener.sessionDownloadedFilesChanged(sessionFiles);
		}
	}

	@Override
	public synchronized void increaseSessionBytes(long bytes) {
		super.increaseSessionBytes(bytes);
		for (IDownloadQueueManagerListener listener : listeners) {
			listener.sessionDownloadedBytesChanged(sessionBytes);
		}
	}

	@Override
	protected void updateOpenSlots() {
		super.updateOpenSlots();
		for (IDownloadQueueManagerListener listener : listeners) {
			listener.queueChanged(queue.size(), openSlots, maxConnectionCount);
		}
	}

	@Override
	protected Restriction getRestrictionForTask(PicDownloadListener task) {
		String domain = getDomainFromURL(task.getContainerURL());

		DownloadRestriction restriction = restrictions.getRestrictionForDomain(domain);
		if (restriction != null) {
			return restriction;
		}

		/*
		 * If no restriction found, then just return a restriction, which does not restrict
		 */
		return new DownloadRestriction(domain, 0);
	}

	/**
	 * Returns the domain from a URL
	 * If the domain could not retrieved, an empty
	 * String is returned.
	 * 
	 * @param url URL
	 * @return Domain
	 */
	private String getDomainFromURL(String url) {
		int protocolPos = url.indexOf("://");
		if (protocolPos == -1) {
			return "";
		}
		String urlWithoutProtocol = url.substring(protocolPos + 3);
		int pathPos = urlWithoutProtocol.indexOf("/");
		if (pathPos == -1) {
			return "";
		}

		String domain = urlWithoutProtocol.substring(0, pathPos);

		int lastPoint = domain.lastIndexOf(".");
		if (lastPoint == -1) {
			return domain;
		}
		int lastBeforeLastPoint = domain.lastIndexOf(".", lastPoint - 1);
		if (lastBeforeLastPoint == -1) {
			return domain;
		}
		return domain.substring(lastBeforeLastPoint + 1);
	}

	@Override
	protected int compareTasks(PicDownloadListener t1, PicDownloadListener t2) {
		int comparison = super.compareTasks(t1, t2);
		if (comparison == 0) {
			return Long.compare(t1.getPic().getDateTimeSimple(), t2.getPic().getDateTimeSimple());
		}
		return comparison;
	}

	@Override
	protected void addTaskToExecutingTasks(QueueTask<PicDownloadListener, PicDownloadResult> task) {
		if (calculateRateTimerTask == null) {
			timer.scheduleAtFixedRate(calculateRateTimerTask = new CalculateRateTimerTask(syncObject, calculateRateListeners), 1000, 1000);
			calculateRateTimerTask.addCalculateRateListener(calculateRateTimerListener);
		}
		calculateRateListeners.add(task.getTask());
		super.addTaskToExecutingTasks(task);
	}

	@Override
	protected void removedTaskFromQueue(PicDownloadListener task, boolean executeFailure) {
		// Nothing to do
	}

	@Override
	protected void completedTaskCallable(QueueTask<PicDownloadListener, PicDownloadResult> task) {
		calculateRateListeners.remove(task.getTask());
		try {
			// Call get to be able to catch Exception
			task.getFuture().get();
		} catch (ExecutionException e) {
			logger.error("Download failed", e);
			task.getTask().getPic().setStatus(PicState.FAILED);
		} catch (CancellationException e) {
			logger.info("Download cancelled");
			task.getTask().getPic().setStatus(PicState.SLEEPING);
		} catch (InterruptedException e) {
			logger.error("Interrupt while waiting for result, this should not happen!", e);
			task.getTask().getPic().setStatus(PicState.FAILED);
		}
	}

	/**
	 * Returns the downloadBitrate
	 * 
	 * @return downloadBitrate
	 */
	public double getTotalDownloadBitrate() {
		return totalDownloadBitrate;
	}

	/**
	 * Returns if downloads are running or not
	 * 
	 * @return True if downloads are running
	 */
	public boolean isDownloading() {
		return isExecutingTasks();
	}

	/**
	 * Returns the downloaded files since application started
	 * 
	 * @return Downloaded files
	 */
	public int getSessionDownloadedFiles() {
		return getSessionFiles();
	}

	/**
	 * Returns the downloaded bytes since application started
	 * 
	 * @return Downloaded bytes
	 */
	public long getSessionDownloadedBytes() {
		return getSessionBytes();
	}

	/**
	 * Increases the downloaded files since application started by 1
	 * This method will fire the sessionDownloadedFilesChanged-Method
	 * on all listeners.
	 */
	public synchronized void increaseSessionDownloadedFiles() {
		increaseSessionFiles();
	}

	/**
	 * Increases the downloaded bytes since application started by downloadedBytes
	 * So QueueData will take the actual value and add downloadedBytes.
	 * This method will fire the sessionDownloadedBytesChanged-Method
	 * on all listeners.
	 * 
	 * @param downloadedBytes Downloaded bytes
	 */
	public synchronized void increaseSessionDownloadedBytes(long downloadedBytes) {
		increaseSessionBytes(downloadedBytes);
	}

	/**
	 * Adds a listener
	 * 
	 * @param l Listener
	 */
	public void addDownloadQueueManagerListener(IDownloadQueueManagerListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	/**
	 * Removes a listener
	 * 
	 * @param l Listener
	 */
	public void removeDownloadQueueManagerListener(IDownloadQueueManagerListener l) {
		listeners.remove(l);
	}
}

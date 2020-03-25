package ch.supertomcat.bh.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;

import ch.supertomcat.bh.settings.BHSettingsListener;
import ch.supertomcat.bh.settings.SettingsManager;

/**
 * This class manages the download-slots.
 * It contains also the counters and restrictions for domains.
 * So this class allows a Pic to download or let them wait
 * until a slot is free.
 * 
 * @see ch.supertomcat.bh.queue.Restriction
 */
public class DownloadQueueManager implements BHSettingsListener, ICalculateRateTimer {
	/**
	 * Listener
	 */
	private List<IDownloadQueueManagerListener> listeners = new CopyOnWriteArrayList<>();

	/**
	 * timer
	 */
	private Timer timer = new Timer("Download-Rate-Timer");

	/**
	 * calculateRateListeners
	 */
	private List<IDownloadListener> calculateRateListeners = new CopyOnWriteArrayList<>();

	/**
	 * calculateRateTimerTask
	 */
	private CalculateRateTimerTask calculateRateTimerTask = null;

	/**
	 * Maximum connection count
	 */
	private int connectionCount = SettingsManager.instance().getConnections();

	/**
	 * Maximum connection count per host
	 */
	private int connectionCountPerHost = SettingsManager.instance().getConnectionsPerHost();

	/**
	 * Free download slots
	 */
	private int openDownloadSlots = connectionCount;

	/**
	 * Downloaded files since application started
	 */
	private int sessionDownloadedFiles = 0;

	/**
	 * Downloaded bytes since application started
	 */
	private long sessionDownloadedBytes = 0;

	private double downloadBitrate = -1;

	/**
	 * Array containing the restrictions
	 */
	private List<Restriction> restrictions = new ArrayList<>();

	/**
	 * Hashtable containing the counters
	 */
	private Map<String, Integer> counters = new HashMap<>();

	/**
	 * Array containing listeners which requested a download slot
	 */
	private List<IDownloadListener> queue = new ArrayList<>();

	/**
	 * Constructor
	 */
	public DownloadQueueManager() {
		SettingsManager.instance().addSettingsListener(this);
	}

	/**
	 * Returns the maximum connection count
	 * 
	 * @return Maximmum connection count
	 */
	public int getConnectionCount() {
		return connectionCount;
	}

	/**
	 * Returns the open download slots
	 * 
	 * @return Open download slots
	 */
	public int getOpenDownloadSlots() {
		return openDownloadSlots;
	}

	/**
	 * Returns the count of listeners which requested a download slot
	 * 
	 * @return Queue size
	 */
	public int getQueueSize() {
		return queue.size();
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

	/**
	 * This method allows a Pic to start the download
	 * It checks the restrictions an counters and all needed
	 * things.
	 * This method is fired always when a download slot was
	 * give back to the queue and when a download slot is requested
	 * and the queue was empty before.
	 */
	public synchronized void manageDLSlots() {
		// If there are no open download slots we can just return
		if (openDownloadSlots <= 0) {
			return;
		}
		/*
		 * If the queue is empty, its a good point to check
		 * if the open download slots are set correct ;-)
		 */
		if (queue.isEmpty()) {
			if (openDownloadSlots != connectionCount) {
				openDownloadSlots = connectionCount;
			}
		} else {
			if (calculateRateTimerTask == null) {
				timer.scheduleAtFixedRate(calculateRateTimerTask = new CalculateRateTimerTask(this, calculateRateListeners), 1000, 1000);
				calculateRateTimerTask.addCalculateRateListener(this);
			}
		}

		for (IDownloadListener download : queue) {
			if (openDownloadSlots <= 0) {
				// If there are now no open download slots we can just return
				return;
			} else {
				// We say here the download should be allowed
				boolean bAllowed = true;

				// Now we check if there are a counter and a restriction for the domain
				String domain = getDomainFromURL(download.getContainerURL());

				int countForDomain = 0;
				Integer cfd = counters.get(domain);
				if (cfd != null) {
					countForDomain = cfd;
				}

				Restriction re = getRestrictionForDomain(domain);

				if (re != null) {
					if (re.getMaxSimultaneousDownloads() > 0) {
						/*
						 * If there is a restriction and a counter and the max
						 * simultanious downloads are higher than 0, we check if
						 * the download can be allowed
						 */

						int currentCount = getCurrentCount(re.getDomains());

						if (currentCount >= re.getMaxSimultaneousDownloads()) {
							/*
							 * If there are already too many running downloads for
							 * this domain, we don't allow the download
							 */
							bAllowed = false;
						}
					}
				} else {
					/*
					 * If no restriction is set for the domain, then the default
					 * max connections per host settings is the restriction
					 */
					if (connectionCountPerHost > 0 && countForDomain >= connectionCountPerHost) {
						bAllowed = false;
					}
				}

				// If the download is not allowed, we continue
				if (bAllowed == false) {
					continue;
				}

				// Allow the download and check if the listener really started the download
				boolean b = download.downloadAllowed(this);
				if (b == true) {
					calculateRateListeners.add(download);
					// Only if the listener has started the download, we decrease open download slots
					setOpenDownloadSlots(openDownloadSlots - 1);
					// And increase the counter
					counters.put(domain, countForDomain + 1);
				}
			}
		}
	}

	/**
	 * Requests a download slot
	 * 
	 * @param l Listener
	 */
	public synchronized void addDLSlotListener(IDownloadListener l) {
		if (!queue.contains(l)) {
			// Add the listener to the queue
			queue.add(l);
		}
	}

	/**
	 * Gives the download-slot back to the queue
	 * This method must be used if the Pic has not the status
	 * Pic.WAITING.
	 * This method fires queueChanged on all listeners
	 * 
	 * @param l Listener
	 */
	public synchronized void removeDLSlotListener(IDownloadListener l) {
		if (queue.contains(l)) {
			queue.remove(l);
			calculateRateListeners.remove(l);

			String domain = getDomainFromURL(l.getContainerURL());

			Integer cfd = counters.get(domain);
			if (cfd != null) {
				int countForDomain = cfd;
				if (countForDomain > 0) {
					// Decrease the queue counter for this domain
					counters.put(domain, countForDomain - 1);
				}
			}

			if (openDownloadSlots < connectionCount) {
				/*
				 * We do this only if the open download slots are lower than max connections
				 * Example:
				 * If we would do this always:
				 * openDownloadSlots: 6
				 * connectionCount: 6
				 * So, we would increase openDownloadSlot.
				 * But there can't be more open download slots as max connections
				 */

				// Increase the open download slots
				setOpenDownloadSlots(openDownloadSlots + 1);
			}
			// Run the mangageDLSlots-Method, which will allow the download
			manageDLSlots();
			if (queue.isEmpty()) {
				if (calculateRateTimerTask != null) {
					calculateRateTimerTask.cancel();
					calculateRateTimerTask.removeCalculateRateListener(this);
				}
				calculateRateTimerTask = null;
				timer.purge();
				downloadBitrate = -1;
			}
			int queueSize = this.queue.size();
			for (IDownloadQueueManagerListener listener : listeners) {
				listener.queueChanged(queueSize, this.openDownloadSlots, this.connectionCount);
				if (queueSize == 0) {
					listener.downloadsComplete(queueSize, this.openDownloadSlots, this.connectionCount);
					listener.totalDownloadRateCalculated(downloadBitrate);
				}
				if (queue.isEmpty()) {
					listener.queueEmpty();
				}
			}
		}
	}

	/**
	 * Gives the download-slot back to the queue
	 * This method is only for a Pic with the status of Pic.WAITING.
	 * Because there is no download slot for a waiting pic, so the things done
	 * in removeDLSlotListener-Method are not needed in this case.
	 * 
	 * @param l Listener
	 */
	public synchronized void removeDLSlotListenerStopping(IDownloadListener l) {
		queue.remove(l);
	}

	/**
	 * Returns if the listener is already registered
	 * 
	 * @param l Listener
	 * @return True if the listener is already registered
	 */
	public synchronized boolean isDLSlotListenerRegistered(IDownloadListener l) {
		if (queue.contains(l)) {
			return true;
		}
		return false;
	}

	/**
	 * Sets the open download slots
	 * This method fires queueChanged on all listeners
	 * 
	 * @param count Anzahl Freie Slots
	 */
	private synchronized void setOpenDownloadSlots(int count) {
		this.openDownloadSlots = count;
		for (IDownloadQueueManagerListener listener : listeners) {
			listener.queueChanged(this.queue.size(), this.openDownloadSlots, this.connectionCount);
		}
	}

	/**
	 * Returns if downloads are running or not
	 * 
	 * @return True if downloads are running
	 */
	public boolean isDownloading() {
		if (this.openDownloadSlots < this.connectionCount) {
			return true;
		}
		return false;
	}

	@Override
	public synchronized void settingsChanged() {
		/*
		 * If the settings were changed, we need to look if we
		 * must change the openDownloadSlot.
		 */
		if (!isDownloading()) {
			// If no downloads are running we change only the openDownloadSlots
			this.connectionCount = SettingsManager.instance().getConnections();
			setOpenDownloadSlots(this.connectionCount);
		} else {
			// If downloads are running
			int cc = SettingsManager.instance().getConnections();
			// Calculate the difference between the new max connection value and the old
			int diff = cc - this.connectionCount;
			if (diff != 0) {
				/*
				 * If the value has changed set openDownloadSlots
				 * Note: Because diff could be a negative value, we
				 * can just always use the + operator.
				 * Example: + -1 is the same as -1
				 */
				setOpenDownloadSlots(this.openDownloadSlots + diff);
			}
			// Set the max value
			this.connectionCount = cc;
		}
		this.connectionCountPerHost = SettingsManager.instance().getConnectionsPerHost();
	}

	@Override
	public void lookAndFeelChanged() {
		// Nothing to do
	}

	/**
	 * Returns the downloaded files since application started
	 * 
	 * @return Downloaded files
	 */
	public int getSessionDownloadedFiles() {
		return sessionDownloadedFiles;
	}

	/**
	 * Returns the downloaded bytes since application started
	 * 
	 * @return Downloaded bytes
	 */
	public long getSessionDownloadedBytes() {
		return sessionDownloadedBytes;
	}

	/**
	 * Increases the downloaded files since application started by 1
	 * This method will fire the sessionDownloadedFilesChanged-Method
	 * on all listeners.
	 */
	public synchronized void increaseSessionDownloadedFiles() {
		this.sessionDownloadedFiles++;
		for (IDownloadQueueManagerListener listener : listeners) {
			listener.sessionDownloadedFilesChanged(this.sessionDownloadedFiles);
		}
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
		this.sessionDownloadedBytes += downloadedBytes;
		for (IDownloadQueueManagerListener listener : listeners) {
			listener.sessionDownloadedBytesChanged(this.sessionDownloadedBytes);
		}
	}

	/**
	 * Adds a restriction
	 * If there is already a restriction for a domain the restriction
	 * is not added. But the value of maximum simultanious downloads from
	 * the restriction to add is taken and set to the existing restriction.
	 * So this method can be used to update a restriction
	 * 
	 * @param restriction Restriction
	 */
	public synchronized void addRestriction(Restriction restriction) {
		for (int i = 0; i < restrictions.size(); i++) {
			if (restrictions.get(i).equals(restriction)) {
				restrictions.get(i).setMaxSimultaneousDownloads(restriction.getMaxSimultaneousDownloads());
				return;
			}
		}
		restrictions.add(restriction);
	}

	/**
	 * Removes a restriction
	 * 
	 * @param restriction Restriction
	 */
	public synchronized void removeRestriction(Restriction restriction) {
		restrictions.remove(restriction);
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

	/**
	 * Returns the restriction for a domain
	 * If there is no restriction for a domain null is returned
	 * 
	 * @param domain Domain
	 * @return Restriction
	 */
	private Restriction getRestrictionForDomain(String domain) {
		if (domain.isEmpty()) {
			return null;
		}
		for (Restriction restriction : restrictions) {
			if (restriction.isDomainRestricted(domain)) {
				return restriction;
			}
		}
		return null;
	}

	/**
	 * Returns current Queue Count for given domains
	 * This method should only be called from synchronized methods!!
	 * 
	 * @param domains Domains
	 * @return Current Queue Count for given domains
	 */
	private int getCurrentCount(List<String> domains) {
		int count = 0;
		for (String domain : domains) {
			Integer cfd = counters.get(domain);
			if (cfd != null) {
				count += cfd;
			}
		}
		return count;
	}

	/**
	 * Returns the downloadBitrate
	 * 
	 * @return downloadBitrate
	 */
	public double getDownloadBitrate() {
		return downloadBitrate;
	}

	@Override
	public void totalDownloadRateCalculated(double downloadRate) {
		this.downloadBitrate = downloadRate;
		for (IDownloadQueueManagerListener listener : listeners) {
			listener.totalDownloadRateCalculated(downloadRate);
		}
	}
}

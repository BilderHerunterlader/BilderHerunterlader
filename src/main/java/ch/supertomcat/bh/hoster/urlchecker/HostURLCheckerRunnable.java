package ch.supertomcat.bh.hoster.urlchecker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.pic.URLList;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.IProgressObserver;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;

/**
 * This class is for checking multiple URLs
 */
public class HostURLCheckerRunnable implements Runnable, IProgressObserver {
	/**
	 * URLs
	 */
	private final URLList urlList;

	/**
	 * ProgressObserver
	 */
	private ProgressObserver progress = new ProgressObserver();

	/**
	 * Listeners
	 */
	private List<HostURLCheckerListener> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Is the thread running
	 */
	private boolean running = false;

	/**
	 * Stop checking
	 */
	private boolean stop = false;

	/**
	 * HostManager
	 */
	private final HostManager hostManager;

	/**
	 * Constructor
	 * 
	 * @param urlList URLList
	 * @param hostManager Host Manager
	 */
	public HostURLCheckerRunnable(URLList urlList, HostManager hostManager) {
		this.stop = false;
		this.urlList = urlList;
		this.hostManager = hostManager;
		progress.addProgressListener(this);
	}

	/**
	 * Start checking URLs
	 */
	public void checkURLs() {
		if (!running) {
			Thread t = new Thread(this);
			t.setName("URL-Check-Thread-" + t.threadId());
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}
	}

	@Override
	public void run() {
		running = true;
		List<URL> v = new ArrayList<>();
		List<URL> urls = urlList.getUrls();
		if (urls == null || urls.isEmpty()) {
			// if there are now urls to check
			linksChecked(null);
			running = false;
			return;
		}

		progressChanged(0, urls.size(), 0);

		int progressCounter = 0;
		int progressChangeInterval = urls.size() / 100;
		if (progressChangeInterval <= 0) {
			progressChangeInterval = 1;
		}

		for (int i = 0; i < urls.size(); i++) {
			if (stop) {
				break;
			}

			URL urlObject = urls.get(i);
			// Redirect the url if needed
			urlObject.checkURLForRedirect(hostManager);

			List<URL> additionalURLs = null;
			// Check if there is a hostclass available which accepts the url
			AtomicBoolean bOK = new AtomicBoolean(false);
			additionalURLs = hostManager.checkURL(urlObject, bOK, progress);
			if (bOK.get()) {
				v.add(urlObject);
			}
			if (additionalURLs != null && !additionalURLs.isEmpty()) {
				progressChanged(Localization.getString("CheckingLinks") + "... " + (i + 1) + "/" + urls.size() + " | " + Localization.getString("AddLinksToCheck") + "... ");
				for (int o = 0; o < additionalURLs.size(); o++) {
					if (!urls.contains(additionalURLs.get(o))) {
						urls.add(additionalURLs.get(o));
					}
				}
				progressChanged(0, urls.size(), (i + 1));
				progressChangeInterval = urls.size() / 100;
				if (progressChangeInterval <= 0) {
					progressChangeInterval = 1;
				}
			}
			progressCounter++;
			if (progressCounter >= progressChangeInterval) {
				progressChanged(Localization.getString("CheckingLinks") + "... " + (i + 1) + "/" + urls.size());
				progressChanged((i + 1));
				progressCounter = 0;
			}
		}

		// Let the listeners know that the urls are checked
		linksChecked(v);
		running = false;
		stop = false;
	}

	/**
	 * Stop checking
	 */
	public void stopAdding() {
		this.stop = true;
	}

	/**
	 * @param urls URLs
	 */
	private void linksChecked(List<URL> urls) {
		for (HostURLCheckerListener listener : listeners) {
			listener.linksChecked(urls);
		}
	}

	/**
	 * Add listener
	 * 
	 * @param l Listener
	 */
	public void addHMListener(HostURLCheckerListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	/**
	 * Remove listener
	 * 
	 * @param l Listener
	 */
	public void removeHMListener(HostURLCheckerListener l) {
		if (listeners.contains(l)) {
			listeners.remove(l);
		}
	}

	@Override
	public void progressChanged(int val) {
		for (HostURLCheckerListener listener : listeners) {
			listener.progressChanged(val);
		}
	}

	@Override
	public void progressChanged(int min, int max, int val) {
		for (HostURLCheckerListener listener : listeners) {
			listener.progressChanged(min, max, val);
		}
	}

	@Override
	public void progressChanged(String text) {
		for (HostURLCheckerListener listener : listeners) {
			listener.progressChanged(text);
		}
	}

	@Override
	public void progressChanged(boolean visible) {
		// Nothing to do
	}

	@Override
	public void progressIncreased() {
		// Nothing to do
	}

	@Override
	public void progressModeChanged(boolean indeterminate) {
		// Nothing to do
	}

	@Override
	public void progressCompleted() {
		// Nothing to do
	}
}

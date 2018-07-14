package ch.supertomcat.bh.hoster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.pic.URLList;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.progressmonitor.IProgressObserver;
import ch.supertomcat.supertomcattools.guitools.progressmonitor.ProgressObserver;
import ch.supertomcat.supertomcattools.settingstools.options.OptionBoolean;

/**
 * This class is for checking multiple URLs
 */
public class HostURLChecker implements Runnable, IProgressObserver {

	/**
	 * URLs
	 */
	private URLList urlList;

	/**
	 * Checked URLs
	 */
	private ArrayList<URL> v;

	/**
	 * ProgressObserver
	 */
	private ProgressObserver progress = new ProgressObserver();

	/**
	 * Listeners
	 */
	private List<IHostURLCheckerListener> listeners = new CopyOnWriteArrayList<>();

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
	private HostManager hm = HostManager.instance();

	/**
	 * Constructor
	 * 
	 * @param urlList URLList
	 */
	public HostURLChecker(URLList urlList) {
		this.stop = false;
		this.urlList = urlList;
		progress.addProgressListener(this);
	}

	/**
	 * Start checking URLs
	 */
	public void checkURLs() {
		if (!running) {
			Thread t = new Thread(this);
			t.setName("URL-Check-Thread-" + t.getId());
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
			t = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		running = true;
		v = new ArrayList<>();
		List<URL> urls = urlList.getUrls();
		if (urls == null || urls.size() == 0) {
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
			urlObject.checkURLForRedirect();

			List<URL> additionalURLs = null;
			// Check if there is a hostclass available which accepts the url
			OptionBoolean bOK = new OptionBoolean("", false);
			additionalURLs = hm.checkURL(urlObject, bOK, progress);
			if (bOK.getValue()) {
				v.add(urlObject);
			}
			if (additionalURLs != null && additionalURLs.size() > 0) {
				progressChanged(Localization.getString("CheckingLinks") + "... " + (i + 1) + "/" + urls.size() + " | " + Localization.getString("AddLinksToCheck") + "... ");
				for (int o = 0; o < additionalURLs.size(); o++) {
					if (urls.contains(additionalURLs.get(o)) == false) {
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
		v = null;
		urls = null;
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
		for (IHostURLCheckerListener listener : listeners) {
			listener.linksChecked(urls);
		}
	}

	/**
	 * Add listener
	 * 
	 * @param l Listener
	 */
	public void addHMListener(IHostURLCheckerListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	/**
	 * Remove listener
	 * 
	 * @param l Listener
	 */
	public void removeHMListener(IHostURLCheckerListener l) {
		if (listeners.contains(l)) {
			listeners.remove(l);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.gui.progressmonitor.IProgressObserver#progressChanged(int)
	 */
	@Override
	public void progressChanged(int val) {
		for (IHostURLCheckerListener listener : listeners) {
			listener.progressChanged(val);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.gui.progressmonitor.IProgressObserver#progressChanged(int, int, int)
	 */
	@Override
	public void progressChanged(int min, int max, int val) {
		for (IHostURLCheckerListener listener : listeners) {
			listener.progressChanged(min, max, val);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.gui.progressmonitor.IProgressObserver#progressChanged(java.lang.String)
	 */
	@Override
	public void progressChanged(String text) {
		for (IHostURLCheckerListener listener : listeners) {
			listener.progressChanged(text);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.gui.progressmonitor.IProgressObserver#progressChanged(boolean)
	 */
	@Override
	public void progressChanged(boolean visible) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.supertomcattools.guitools.progressmonitor.IProgressObserver#progressIncreased()
	 */
	@Override
	public void progressIncreased() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.supertomcattools.guitools.progressmonitor.IProgressObserver#progressModeChanged(boolean)
	 */
	@Override
	public void progressModeChanged(boolean indeterminate) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.supertomcattools.guitools.progressmonitor.IProgressObserver#progressCompleted()
	 */
	@Override
	public void progressCompleted() {
	}
}

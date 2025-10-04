package ch.supertomcat.bh.hoster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.StampedLock;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import ch.supertomcat.bh.cookies.CookieManager;
import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.classloader.HostClassesLoader;
import ch.supertomcat.bh.hoster.hostimpl.HostRules;
import ch.supertomcat.bh.hoster.hostimpl.HostSortImages;
import ch.supertomcat.bh.hoster.hostimpl.HostzDefaultFiles;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.hoster.urlchecker.RemoveDuplicatesRunnable;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.queue.RestrictionAccess;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import jakarta.xml.bind.JAXBException;

/**
 * Class which holds the host-classes and provides methods to
 * check and parse the URLs
 * There is also a method which removes duplicates from an URL-Array
 */
public class HostManager {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(HostManager.class);

	/**
	 * Singleton
	 */
	private static boolean alreadyInstanced = false;

	/**
	 * Hostsclasses
	 */
	private List<Host> hosts = null;

	/**
	 * RedirectManager
	 */
	private RedirectManager redirectManager = new RedirectManager();

	/**
	 * HostRules
	 */
	private HostRules hostRules;

	/**
	 * HostSortImages
	 */
	private HostSortImages hostSortImages = new HostSortImages();

	/**
	 * Stamped Lock
	 */
	private StampedLock lock = new StampedLock();

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Constructor
	 * 
	 * @param mainWindow MainWindow
	 * @param restrictionAccess Restriction Access
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param cookieManager Cookie Manager
	 * @throws JAXBException
	 * @throws SAXException
	 * @throws IOException
	 */
	public HostManager(JFrame mainWindow, RestrictionAccess restrictionAccess, ProxyManager proxyManager, SettingsManager settingsManager,
			CookieManager cookieManager) throws IOException, SAXException, JAXBException {
		this.settingsManager = settingsManager;
		Hoster.setMainWindow(mainWindow);
		Hoster.setRestrictionAccess(restrictionAccess);
		Hoster.setProxyManager(proxyManager);
		Hoster.setSettingsManager(settingsManager);
		Hoster.setCookieManager(cookieManager);

		this.hostRules = new HostRules(this);

		// Load the hostclasses
		List<Host> loadedHosts = HostClassesLoader.loadHostClasses();
		loadedHosts.add(hostRules);
		loadedHosts.add(hostSortImages);

		hosts = loadedHosts;

		redirectManager.setHR(hostRules);

		// Now we have a unsorted array, so we have to sort it
		reInitHosterList();

		synchronized (HostManager.class) {
			if (alreadyInstanced) {
				throw new IllegalStateException("HostManager already instanced");
			}
			alreadyInstanced = true;
		}
	}

	/**
	 * Create a sorted hostclass-array
	 */
	public void reInitHosterList() {
		long stamp = lock.writeLock();
		try {
			Collections.sort(hosts, new Comparator<Host>() {
				private boolean bRulesBeforeClasses = settingsManager.getHostsSettings().isRulesBeforeClasses();

				@Override
				public int compare(Host o1, Host o2) {

					if (o2.getName().equals(HostzDefaultFiles.NAME)) {
						// HostDefaultImages has to be at the end of the array!
						return -1;
					} else if (o1.getName().equals(HostzDefaultFiles.NAME)) {
						// HostDefaultImages has to be at the end of the array!
						return 1;
					}

					if (o2 == hostSortImages) {
						return -1;
					} else if (o1 == hostSortImages) {
						return 1;
					}

					if (o2 == hostRules) {
						/*
						 * When rules have higher priority than other hostclasses, then
						 * HostRules has to be at the begin of the array
						 */
						if (bRulesBeforeClasses) {
							return 1;
						} else {
							return -1;
						}
					} else if (o1 == hostRules) {
						/*
						 * When rules have higher priority than other hostclasses, then
						 * HostRules has to be at the begin of the array
						 */
						if (bRulesBeforeClasses) {
							return -1;
						} else {
							return 1;
						}
					}

					if (o2.isDeveloper() && !o1.isDeveloper()) {
						return 1;
					} else if (o1.isDeveloper() && !o2.isDeveloper()) {
						return -1;
					}

					return o1.getName().compareTo(o2.getName());
				}
			});
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * Return the HostRules-hostclass
	 * 
	 * @return HostRules
	 */
	public HostRules getHostRules() {
		return hostRules;
	}

	/**
	 * Returns the RedirectManager
	 * 
	 * @return RedirectManager
	 */
	public RedirectManager getRedirectManager() {
		return redirectManager;
	}

	/**
	 * Returns the version of a hostclass
	 * 
	 * @param name Name
	 * @return Version
	 */
	public String getHostVersion(String name) {
		long stamp = lock.readLock();
		try {
			for (Host host : hosts) {
				if (host.getName().equals(name)) {
					String version = host.getVersion();
					if (version == null) {
						return "";
					}
					return version;
				}
			}
		} finally {
			lock.unlockRead(stamp);
		}
		return "";
	}

	/**
	 * Returns an array of all hostclasses
	 * 
	 * @return Hostclasses-array
	 */
	public List<Host> getHosters() {
		long stamp = lock.readLock();
		try {
			return new ArrayList<>(hosts);
		} finally {
			lock.unlockRead(stamp);
		}
	}

	/**
	 * Get-Methode
	 * 
	 * @param index Index
	 * @return Rule
	 */
	public Host getHost(int index) {
		long stamp = lock.readLock();
		try {
			if (index < 0 || index >= hosts.size()) {
				return null;
			}
			return hosts.get(index);
		} finally {
			lock.unlockRead(stamp);
		}
	}

	/**
	 * Returns the Hoster for the url or null if not found
	 * 
	 * @param url URL
	 * @return Hoster or null
	 */
	public Hoster getHosterForURL(String url) {
		long stamp = lock.readLock();
		try {
			for (Host host : hosts) {
				// Check if the hostclass accepts the url
				if (host.isEnabled() && host.isFromThisHoster(url)) {
					if (host == hostRules) {
						return hostRules.getRuleForURL(url);
					} else {
						return host;
					}
				}
			}
		} finally {
			lock.unlockRead(stamp);
		}
		return null;
	}

	/**
	 * Check if a hostclass accepts the url and return a nice filename for the url.
	 * If there is no hostclass available which would accept the url then null is
	 * returned
	 * 
	 * @param urlObject URL-Object
	 * @param bOK Flag if there is a Hoster for this url
	 * @param progress ProgessObserver
	 * @return List of URL-Objects if additional URLs were added or null
	 */
	public List<URL> checkURL(URL urlObject, AtomicBoolean bOK, ProgressObserver progress) {
		long stamp = lock.readLock();
		try {
			for (Host host : hosts) {
				// Check if the hostclass accepts the url
				if (host.isEnabled() && host.isFromThisHoster(urlObject.getURL())) {
					// Get a nice filename from the url
					String filename = host.getFilenameFromURL(urlObject.getURL());
					if (filename != null && !filename.isEmpty()) {
						urlObject.setFilenameCorrected(filename);
					}

					bOK.set(true);

					if (host instanceof IHosterURLAdder ihua) {
						try {
							List<URL> additionalURLs = ihua.isFromThisHoster(urlObject, bOK, progress);
							if (additionalURLs != null && !additionalURLs.isEmpty()) {
								return additionalURLs;
							}
						} catch (Exception e) {
							logger.error("Could not add additional URLs in host: {} {}", host.getName(), host.getVersion(), e);
						}
					}

					return null;
				}
			}
		} finally {
			lock.unlockRead(stamp);
		}
		return null;
	}

	/**
	 * Parses the URL by a hostclass and return the parsed URL
	 * 
	 * @param upo URLParseObject
	 * @return URLParseObject or null
	 * @throws HostException
	 */
	public URLParseObject parseURL(URLParseObject upo) throws HostException {
		if (upo == null) {
			return null;
		}
		String url = upo.getContainerURL();

		long stamp = lock.readLock();
		try {
			for (Host host : hosts) {
				/*
				 * Check if the hostclass accepts the URL.
				 * Also check if the last Host is the same as this one, then
				 * don't parse the URL because this could be a endless loop.
				 * So if a hostclass will parse the url again, then it must
				 * set the last Host to null.
				 * So a endless loop is still possible, i don't think i can
				 * really avoid them completely, but this simple check is better
				 * then nothing.
				 * The HostRules-hostclass has multiple "classes" (Rules), so
				 * if the lastHost is HostRules and this one also, then let them
				 * parse, because HostRules itselfs checks if the lastRule is the
				 * same as this time.
				 */
				if (host.isEnabled() && host.isFromThisHoster(url)) {
					if (upo.isLoop()) {
						logger.error("Parsing terminated for URL '{}' because upo seems to be parsed in a endless loop!: {}", upo.getContainerURL(), upo.getHosterStackTrace());
						return null;
					}

					// parse the URL
					upo.addHoster(host);
					host.parseURLAndFilename(upo);
					return upo;
				}
			}
		} finally {
			lock.unlockRead(stamp);
		}
		return upo;
	}

	/**
	 * @param urls URLs
	 * @param progress Progress
	 * @param settingsManager Settings Manager
	 */
	public static void removeDuplicates(List<URL> urls, ProgressObserver progress, SettingsManager settingsManager) {
		List<URL> originalUrls = new ArrayList<>(urls);
		urls.clear();

		int threadCount = settingsManager.getSettings().getThreadCount();

		if (threadCount < 1) {
			threadCount = 1;
		}

		if (threadCount > originalUrls.size()) {
			threadCount = originalUrls.size();
		}

		CyclicBarrier barrier = new CyclicBarrier(threadCount + 1);
		try (ExecutorService threadPool = Executors.newFixedThreadPool(threadCount)) {

			AtomicBoolean bContains = new AtomicBoolean(false);

			RemoveDuplicatesRunnable[] rdt = new RemoveDuplicatesRunnable[threadCount];
			for (int t = 0; t < threadCount; t++) {
				rdt[t] = new RemoveDuplicatesRunnable(originalUrls, t, threadCount, bContains, 0, barrier);
			}

			if (progress != null) {
				progress.progressChanged(true);
				progress.progressChanged(0, originalUrls.size(), 0);
				progress.progressChanged(Localization.getString("RemovingDuplicates") + "...");
			}

			int val = 0;

			for (int i = 0; i < originalUrls.size(); i++) {
				// Reset Value
				bContains.set(false);

				/*
				 * Start all threads (runnables)
				 */
				for (int t = 0; t < threadCount; t++) {
					rdt[t].setCurrentRow(i);
					threadPool.execute(rdt[t]);
				}
				/*
				 * Wait for all threads (runnables) to complete
				 */
				try {
					barrier.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					logger.error(e.getMessage(), e);
				}

				if (!bContains.get()) {
					// Add the current URL to the result-Array
					urls.add(originalUrls.get(i));
				} else {
					// Remove the current URL from the original array
					originalUrls.remove(i);
					i--;
				}
				val++;
				if (progress != null) {
					progress.progressChanged(val);
				}
			}
			if (progress != null) {
				progress.progressChanged(false);
			}
		}
	}
}

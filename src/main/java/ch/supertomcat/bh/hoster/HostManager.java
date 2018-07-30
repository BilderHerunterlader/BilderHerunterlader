package ch.supertomcat.bh.hoster;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.classloader.HostClassesLoader;
import ch.supertomcat.bh.hoster.hostimpl.HostRules;
import ch.supertomcat.bh.hoster.hostimpl.HostSortImages;
import ch.supertomcat.bh.hoster.hostimpl.HostzDefaultFiles;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.hoster.urlchecker.RemoveDuplicatesRunnable;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.progressmonitor.ProgressObserver;
import ch.supertomcat.supertomcattools.settingstools.options.OptionBoolean;

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
	private static HostManager instance = null;

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
	private HostRules hostRules = new HostRules();

	/**
	 * HostSortImages
	 */
	private HostSortImages hostSortImages = new HostSortImages();

	/**
	 * Constructor
	 */
	private HostManager() {
		// Load the hostclasses
		List<Host> loadedHosts = HostClassesLoader.loadHostClasses();
		loadedHosts.add(hostRules);
		loadedHosts.add(hostSortImages);

		hosts = loadedHosts;

		redirectManager.setHR(hostRules);

		// Now we have a unsorted array, so we have to sort it
		reInitHosterList();
	}

	/**
	 * Returns the singleton
	 * 
	 * @return Singleton
	 */
	public static synchronized HostManager instance() {
		if (instance == null) {
			instance = new HostManager();
		}
		return instance;
	}

	/**
	 * Create a sorted hostclass-array
	 */
	public synchronized void reInitHosterList() {
		// While downloads are running we can't do this!
		if (DownloadQueueManager.instance().isDownloading()) {
			return;
		}

		Collections.sort(hosts, new Comparator<Host>() {
			private boolean bRulesBeforeClasses = SettingsManager.instance().isRulesBeforeClasses();

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

				if (o2.isDeveloper() && o1.isDeveloper() == false) {
					return 1;
				} else if (o1.isDeveloper() && o2.isDeveloper() == false) {
					return -1;
				}

				return o1.getName().compareTo(o2.getName());
			}
		});
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
		for (Host host : hosts) {
			if (host.getName().equals(name)) {
				String version = host.getVersion();
				if (version == null) {
					return "";
				}
				return version;
			}
		}
		return "";
	}

	/**
	 * Returns an array of all hostclasses
	 * 
	 * @return Hostclasses-array
	 */
	public List<Host> getHosters() {
		return new ArrayList<>(hosts);
	}

	/**
	 * Get-Methode
	 * 
	 * @param index Index
	 * @return Rule
	 */
	public Host getHost(int index) {
		if (index < 0 || index >= hosts.size()) {
			return null;
		}
		return hosts.get(index);
	}

	/**
	 * Returns the Hoster for the url or null if not found
	 * 
	 * @param url URL
	 * @return Hoster or null
	 */
	public Hoster getHosterForURL(String url) {
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
	public List<URL> checkURL(URL urlObject, OptionBoolean bOK, ProgressObserver progress) {
		for (Host host : hosts) {
			// Check if the hostclass accepts the url
			if (host.isEnabled() && host.isFromThisHoster(urlObject.getURL())) {
				// Get a nice filename from the url
				String filename = host.getFilenameFromURL(urlObject.getURL());
				if (filename != null && !filename.isEmpty()) {
					urlObject.setFilenameCorrected(filename);
				}

				bOK.setValue(true);

				if (checkForIHosterURLAdderInterface(host) != null) {
					IHosterURLAdder ihua = (IHosterURLAdder)host;
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
					logger.error("Parsing terminated for URL '" + upo.getContainerURL() + "' because upo seems to be parsed in a endless loop!: {}", upo.getHosterStackTrace());
					return null;
				}

				// parse the URL
				upo.addHoster(host);
				host.parseURLAndFilename(upo);
				return upo;
			}
		}
		return upo;
	}

	/**
	 * Returns the method or null if not found
	 * 
	 * @param host Host-Class
	 * @return Method or null
	 */
	private Method checkForIHosterURLAdderInterface(Host host) {
		if (host == null) {
			return null;
		}

		boolean bIFace = false;
		for (Class<?> iface : host.getClass().getInterfaces()) {
			if (iface.getName().equals("ch.supertomcat.bh.hoster.IHosterURLAdder")) {
				bIFace = true;
				break;
			}
		}
		if (bIFace == false) {
			return null;
		}

		for (Method method : host.getClass().getMethods()) {
			if (method.getName().equals("isFromThisHoster")) {
				if (method.getParameterTypes().length == 3 && method.getParameterTypes()[0].getName().equals("ch.supertomcat.bh.pic.URL")
						&& method.getParameterTypes()[1].getName().equals("ch.supertomcat.supertomcattools.settingstools.options.OptionBoolean")
						&& method.getParameterTypes()[2].getName().equals("ch.supertomcat.supertomcattools.guitools.progressmonitor.ProgressObserver")
						&& method.getReturnType().getName().equals("java.util.List")) {
					return method;
				}
			}
		}
		return null;
	}

	/**
	 * Returns true if the object has the interface
	 * Checks not if the methods really are implemented
	 * 
	 * @param obj Object
	 * @param fullyQualifiedInterfaceName Fully qualified interface name
	 * @return true if the object has the interface
	 */
	public boolean hasInterface(Object obj, String fullyQualifiedInterfaceName) {
		if (obj == null) {
			return false;
		}

		for (Class<?> iface : obj.getClass().getInterfaces()) {
			if (iface.getName().equals(fullyQualifiedInterfaceName)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param urls URLs
	 * @param progress Progress
	 */
	public static void removeDuplicates(List<URL> urls, ProgressObserver progress) {
		List<URL> originalUrls = new ArrayList<>(urls);
		urls.clear();

		int threadCount = SettingsManager.instance().getThreadCount();

		if (threadCount < 1) {
			threadCount = 1;
		}

		if (threadCount > originalUrls.size()) {
			threadCount = originalUrls.size();
		}

		CyclicBarrier barrier = new CyclicBarrier(threadCount + 1);
		ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);

		OptionBoolean bContains = new OptionBoolean("", false);

		RemoveDuplicatesRunnable rdt[] = new RemoveDuplicatesRunnable[threadCount];
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
			bContains.setValue(false);

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
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			} catch (BrokenBarrierException e) {
				logger.error(e.getMessage(), e);
			}

			if (bContains.getValue() == false) {
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
		threadPool.shutdown();
	}
}

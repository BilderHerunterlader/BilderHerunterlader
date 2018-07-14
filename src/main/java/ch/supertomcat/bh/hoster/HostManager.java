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
	private RedirectManager rm = new RedirectManager();

	/**
	 * HostRules
	 */
	private HostRules hr = new HostRules();

	/**
	 * HostSortImages
	 */
	private HostSortImages si = new HostSortImages();

	/**
	 * Constructor
	 */
	private HostManager() {
		// Load the hostclasses
		List<Host> vresult = HostClassesLoader.loadHostClasses();
		vresult.add(hr);
		vresult.add(si);

		hosts = vresult;

		// Now we have a unsorted array, so we have to sort it
		reInitHosterList();

		rm.setHR(hr);
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

				if (o2.getName().equals("HostDefaultFiles")) {
					// HostDefaultImages has to be at the end of the array!
					return -1;
				} else if (o1.getName().equals("HostDefaultFiles")) {
					// HostDefaultImages has to be at the end of the array!
					return 1;
				}

				if (o2 == si) {
					return -1;
				} else if (o1 == si) {
					return 1;
				}

				if (o2 == hr) {
					/*
					 * When rules have higher priority than other hostclasses, then
					 * HostRules has to be at the begin of the array
					 */
					if (bRulesBeforeClasses) {
						return 1;
					} else {
						return -1;
					}
				} else if (o1 == hr) {
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

		for (int i = 0; i < hosts.size(); i++) {
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
			if (hosts.get(i).isEnabled() && hosts.get(i).isFromThisHoster(url)) {
				if (upo.isLoop() == false) {
					// parse the URL
					upo.addHoster(hosts.get(i));
					hosts.get(i).parseURLAndFilename(upo);
					return upo;
				} else {
					logger.error("Parsing terminated for URL '" + upo.getContainerURL() + "' because upo seems to be parsed in a endless loop!");
					return null;
				}
			}
		}
		return upo;
	}

	/**
	 * Check if a hostclass accepts the url and return a nice filename for the url.
	 * If there is no hostclass available which would accept the url then null is
	 * returned
	 * 
	 * @param urlObject URL-Object
	 * @param bOK Flag if there is a Hoster for this url
	 * @param progress ProgessObserver
	 * @return Array of URL-Objects or null
	 */
	public List<URL> checkURL(URL urlObject, OptionBoolean bOK, ProgressObserver progress) {
		List<URL> retval = null;

		for (int i = 0; i < hosts.size(); i++) {
			// Check if the hostclass accepts the url
			if (hosts.get(i).isEnabled() && hosts.get(i).isFromThisHoster(urlObject.getURL())) {

				// Get a nice filename from the url
				String filename = hosts.get(i).getFilenameFromURL(urlObject.getURL());
				if ((filename != null) && (filename.length() > 0)) {
					urlObject.setFilenameCorrected(filename);
				}

				bOK.setValue(true);

				if (checkForIHosterURLAdderInterface(hosts.get(i)) != null) {
					IHosterURLAdder ihua = (IHosterURLAdder)hosts.get(i);
					try {
						List<URL> additionalURLs = ihua.isFromThisHoster(urlObject, bOK, progress);
						if (additionalURLs != null && additionalURLs.size() > 0) {
							retval = additionalURLs;
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}

				break;
			}
		}
		return retval;
	}

	/**
	 * Returns the version of a hostclass
	 * 
	 * @param name Name
	 * @return Version
	 */
	public String getHostVersion(String name) {
		for (int i = 0; i < hosts.size(); i++) {
			if (hosts.get(i).getName().equals(name)) {
				String version = hosts.get(i).getVersion();
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
		if (index >= hosts.size()) {
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
		for (int i = 0; i < hosts.size(); i++) {
			// Check if the hostclass accepts the url
			if (hosts.get(i).isEnabled() && hosts.get(i).isFromThisHoster(url)) {
				if (hosts.get(i) == this.hr) {
					return this.hr.getRuleForURL(url);
				} else {
					return hosts.get(i);
				}
			}
		}
		return null;
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

		RemoveDuplicatesThread rdt[] = new RemoveDuplicatesThread[threadCount];
		for (int t = 0; t < threadCount; t++) {
			rdt[t] = new RemoveDuplicatesThread(originalUrls, t, threadCount, bContains, 0, barrier);
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

	/**
	 * Return the HostRules-hostclass
	 * 
	 * @return HostRules
	 */
	public HostRules getHr() {
		return hr;
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
		Class<?>[] iface = host.getClass().getInterfaces();
		for (int i = 0; i < iface.length; i++) {
			if (iface[i].getName().equals("ch.supertomcat.bh.hoster.IHosterURLAdder")) {
				bIFace = true;
				break;
			}
		}
		if (bIFace == false) {
			return null;
		}

		Method m[] = host.getClass().getMethods();
		for (int i = 0; i < m.length; i++) {
			if (m[i].getName().equals("isFromThisHoster")) {
				if ((m[i].getParameterTypes().length == 3) && (m[i].getParameterTypes()[0].getName().equals("ch.supertomcat.bh.pic.URL"))
						&& (m[i].getParameterTypes()[1].getName().equals("ch.supertomcat.supertomcattools.settingstools.options.OptionBoolean"))
						&& (m[i].getParameterTypes()[2].getName().equals("ch.supertomcat.supertomcattools.guitools.progressmonitor.ProgressObserver"))
						&& m[i].getReturnType().getName().equals("java.util.List")) {
					return m[i];
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

		Class<?>[] ifaces = obj.getClass().getInterfaces();
		for (int i = 0; i < ifaces.length; i++) {
			if (ifaces[i].getName().equals(fullyQualifiedInterfaceName)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the RedirectManager
	 * 
	 * @return RedirectManager
	 */
	public RedirectManager getRedirectManager() {
		return rm;
	}
}

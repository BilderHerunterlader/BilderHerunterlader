package ch.supertomcat.bh.hoster.classloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IRedirect;
import ch.supertomcat.bh.hoster.classloader.impl.HostClassLoader;
import ch.supertomcat.bh.hoster.classloader.impl.RedirectClassLoader;
import ch.supertomcat.bh.hoster.hostimpl.HostzDefaultFiles;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;

/**
 * Classloader for Host or Redirect Classes
 */
public final class HostClassesLoader {
	/**
	 * Constructor
	 */
	private HostClassesLoader() {
	}

	/**
	 * Search for Host Classes and load them
	 * 
	 * @return Loaded Host Classes
	 */
	public static List<Host> loadHostClasses() {
		List<FoundHostClass> foundHostClasses = new ArrayList<>(); // found classes

		HostClassLoader hostClassLoader = new HostClassLoader();

		File folderDeveloperHostClasses = new File(ApplicationProperties.getProperty("ApplicationPath"), "developerhosts");
		foundHostClasses.addAll(hostClassLoader.findHostClasses(folderDeveloperHostClasses, true));

		File folderHostClasses = new File(ApplicationProperties.getProperty("ApplicationPath"), "hosts");
		foundHostClasses.addAll(hostClassLoader.findHostClasses(folderHostClasses, false));

		// HostzDefaultFiles has to be added at the end of the array
		foundHostClasses.add(new FoundHostClass(HostzDefaultFiles.class, false));

		// Get instances of the classes
		return hostClassLoader.loadHostClasses(foundHostClasses);
	}

	/**
	 * Search for Redirect Classes and load them
	 * 
	 * @return Loaded Redirect Classes
	 */
	public static List<IRedirect> loadRedirectClasses() {
		List<FoundHostClass> foundRedirectClasses = new ArrayList<>(); // found classes

		RedirectClassLoader redirectClassLoader = new RedirectClassLoader();

		File folderDeveloperRedirectClasses = new File(ApplicationProperties.getProperty("ApplicationPath"), "developerhosts");
		foundRedirectClasses.addAll(redirectClassLoader.findHostClasses(folderDeveloperRedirectClasses, true));

		File folderRedirectClasses = new File(ApplicationProperties.getProperty("ApplicationPath"), "hosts");
		foundRedirectClasses.addAll(redirectClassLoader.findHostClasses(folderRedirectClasses, false));

		// Get instances of the classes
		return redirectClassLoader.loadHostClasses(foundRedirectClasses);
	}
}

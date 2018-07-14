package ch.supertomcat.bh.hoster.classloader.impl;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

import ch.supertomcat.bh.hoster.IRedirect;
import ch.supertomcat.bh.hoster.classloader.FoundHostClass;

/**
 * Class for loading Redirect classes
 */
public class RedirectClassLoader extends HostClassLoaderBase<IRedirect> {
	private final FileFilter filter = new FileFilter() {
		private final Pattern nestedClassFilenamePattern = Pattern.compile(".+[$][0-9]+\\.class$");

		@Override
		public boolean accept(File pathname) {
			// Only load classes where the name begins with "Redirect" and ends with ".class"
			String filename = pathname.getName();
			return filename.startsWith("Redirect") && filename.endsWith(".class") && !nestedClassFilenamePattern.matcher(filename).matches();
		}
	};

	/**
	 * Constructor
	 */
	public RedirectClassLoader() {
		super("Redirect-Class");
	}

	@Override
	protected String getClassName(String filename) {
		return filename.substring(0, filename.lastIndexOf("."));
	}

	@Override
	protected FileFilter getFileFilter() {
		return filter;
	}

	@Override
	protected boolean isClassCorrectlyImplemented(Class<?> foundHostClass, File file) {
		// Check if the class extends Redirect
		boolean b1 = hasSuperClass(foundHostClass, "ch.supertomcat.bh.hoster.Redirect");
		// Check if the class has the IRedirect-Interface implemented
		boolean b2 = hasInterface(foundHostClass, "ch.supertomcat.bh.hoster.IRedirect");
		// Check if all methods of the interface are existing in the class
		boolean b3 = hasAllMethodsImplemented(foundHostClass, IRedirect.class);
		if (b1 && b2 && b3) {
			return true;
		} else {
			logger.error("Redirect-Class {} is not compatible (Extends Redirect: {}, Has IRedirect Interface: {}, All Methods implemented: {}): {}", foundHostClass.getName(), b1, b2, b3, file
					.getAbsolutePath());
			return false;
		}
	}

	@Override
	protected void initializeLoadedClass(FoundHostClass foundHostClass, IRedirect loadedClass) {
		// Nothing to do
	}

	@Override
	protected void logClassLoaded(FoundHostClass foundHostClass, IRedirect loadedClass) {
		logger.info("{} loaded: {} {}", classTypeName, loadedClass.getName(), loadedClass.getVersion());
	}
}

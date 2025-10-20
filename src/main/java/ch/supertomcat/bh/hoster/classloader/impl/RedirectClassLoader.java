package ch.supertomcat.bh.hoster.classloader.impl;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import ch.supertomcat.bh.hoster.IRedirect;
import ch.supertomcat.bh.hoster.Redirect;
import ch.supertomcat.bh.hoster.classloader.FoundHostClass;
import ch.supertomcat.bh.hoster.hosteroptions.IHosterOptions;

/**
 * Class for loading Redirect classes
 */
public class RedirectClassLoader extends HostClassLoaderBase<IRedirect> {
	private final Pattern nestedClassFilenamePattern = Pattern.compile(".+[$]\\d+\\.class$");

	private final Predicate<Path> filter = x -> {
		String filename = x.getFileName().toString();
		return filename.startsWith("Redirect") && filename.endsWith(".class") && !nestedClassFilenamePattern.matcher(filename).matches();
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
	protected Predicate<Path> getFileFilter() {
		return filter;
	}

	@Override
	protected boolean isClassCorrectlyImplemented(Class<?> foundHostClass, Path file) {
		// Check if the class extends Redirect
		boolean b1 = hasSuperClass(foundHostClass, Redirect.class.getName());
		// Check if the class has the IRedirect-Interface implemented
		boolean b2 = hasInterface(foundHostClass, IRedirect.class.getName());
		// Check if all methods of the interface are existing in the class
		boolean b3 = hasAllMethodsImplemented(foundHostClass, IRedirect.class);
		// Check if IHosterOptions-Interface is implemented and if so, check if all methods are existing
		boolean b4 = true;
		if (hasInterface(foundHostClass, IHosterOptions.class.getName())) {
			b4 = hasAllMethodsImplemented(foundHostClass, IHosterOptions.class);
		}

		if (b1 && b2 && b3 && b4) {
			return true;
		} else {
			logger.error("Redirect-Class {} is not compatible (Extends Redirect: {}, Has IRedirect Interface: {}, All Methods implemented: {}, All IHosterOptions methods implemented: {}): {}", foundHostClass
					.getName(), b1, b2, b3, b4, file);
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

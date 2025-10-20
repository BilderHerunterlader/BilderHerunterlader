package ch.supertomcat.bh.hoster.classloader.impl;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.IHosterURLAdder;
import ch.supertomcat.bh.hoster.classloader.FoundHostClass;
import ch.supertomcat.bh.hoster.hosteroptions.IHosterOptions;

/**
 * Class for loading Host classes
 */
public class HostClassLoader extends HostClassLoaderBase<Host> {
	private final Pattern nestedClassFilenamePattern = Pattern.compile(".+?[$].+?\\.class$");

	private final Predicate<Path> filter = x -> {
		String filename = x.getFileName().toString();
		return filename.startsWith("Host") && filename.endsWith(".class") && !nestedClassFilenamePattern.matcher(filename).matches();
	};

	/**
	 * Constructor
	 */
	public HostClassLoader() {
		super("Host-Class");
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
		// Check if the class extends Host
		boolean b1 = hasSuperClass(foundHostClass, Host.class.getName());
		// Check if the class has the IHoster-Interface implemented
		boolean b2 = hasInterface(foundHostClass, IHoster.class.getName());
		// Check if all methods of the interface are existing in the class
		boolean b3 = hasAllMethodsImplemented(foundHostClass, IHoster.class);
		// Check if IHosterURLAdder-Interface is implemented and if so, check if all methods are existing
		boolean b4 = true;
		if (hasInterface(foundHostClass, IHosterURLAdder.class.getName())) {
			b4 = hasAllMethodsImplemented(foundHostClass, IHosterURLAdder.class);
		}
		// Check if IHosterOptions-Interface is implemented and if so, check if all methods are existing
		boolean b5 = true;
		if (hasInterface(foundHostClass, IHosterOptions.class.getName())) {
			b5 = hasAllMethodsImplemented(foundHostClass, IHosterOptions.class);
		}

		if (b1 && b2 && b3 && b4 && b5) {
			return true;
		} else {
			logger.error("Host-Class {} is not compatible (Extends Host: {}, Has IHoster Interface: {}, All Methods implemented: {}, All IHosterURLAdder methods implemented: {}, All IHosterOptions methods implemented: {}): {}", foundHostClass
					.getName(), b1, b2, b3, b4, b5, file);
			return false;
		}
	}

	@Override
	protected void initializeLoadedClass(FoundHostClass foundHostClass, Host loadedClass) {
		if (foundHostClass.developer()) {
			loadedClass.setDeveloper(true);
		}
	}

	@Override
	protected void logClassLoaded(FoundHostClass foundHostClass, Host loadedClass) {
		logger.info("{} loaded: {} {}", classTypeName, loadedClass.getName(), loadedClass.getVersion());
	}
}

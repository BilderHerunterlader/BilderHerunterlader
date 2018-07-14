package ch.supertomcat.bh.hoster.classloader.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.hoster.classloader.FoundHostClass;

/**
 * Base class for loading Host and Redirect Classes
 * 
 * @param <T> Class Type
 */
public abstract class HostClassLoaderBase<T> {
	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Class Type Name for Logging
	 */
	protected final String classTypeName;

	/**
	 * Constructor
	 * 
	 * @param classTypeName
	 */
	public HostClassLoaderBase(String classTypeName) {
		this.classTypeName = classTypeName;
	}

	/**
	 * Search for Host-Classes in the given folder
	 * 
	 * @param folder Folder
	 * @param developer True if developer classes, false otherwise
	 * @return Found Host-Classes
	 */
	public List<FoundHostClass> findHostClasses(File folder, boolean developer) {
		List<FoundHostClass> foundHostClasses = new ArrayList<>();
		if (!folder.exists()) {
			return foundHostClasses;
		}

		FileFilter filter = getFileFilter();

		File[] files = folder.listFiles(filter);
		if (files == null) {
			logger.error("Could not list files in folder: {}", folder.getAbsolutePath());
			return foundHostClasses;
		}
		// Sort the array, because not every operating system will list the files in alphabetical order
		Arrays.sort(files);

		try (URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { folder.toURI().toURL() });) {
			for (File file : files) {
				String filename = file.getName();
				String classname = getClassName(filename);
				try {
					// try to load the class
					Class<?> cl = classLoader.loadClass(classname);

					// Also load inner / nested / anynomous classes
					File[] nestedClassFiles = folder.listFiles(new FileFilter() {
						Pattern nestedClassFilenamePattern = Pattern.compile("^" + classname + "[$](.+?)\\.class$");

						@Override
						public boolean accept(File pathname) {
							return nestedClassFilenamePattern.matcher(pathname.getName()).matches();
						}
					});
					if (nestedClassFiles == null) {
						logger.error("Could not list files in folder: {}", folder.getAbsolutePath());
					} else {
						for (File nestedClassFile : nestedClassFiles) {
							classLoader.loadClass(getClassName(nestedClassFile.getName()));
						}
					}

					if (isClassCorrectlyImplemented(cl, file)) {
						// add the class to the list
						foundHostClasses.add(new FoundHostClass(cl, developer));
					}
				} catch (Throwable e) {
					logger.error("{} {} could not be loaded: {}", classTypeName, classname, file.getAbsolutePath(), e);
				}
			}
		} catch (IOException e) {
			logger.error("ClassLoader for {} could not be created", classTypeName, e);
		}

		return foundHostClasses;
	}

	/**
	 * Gets the class name from the given filename
	 * 
	 * @param filename Filename
	 * @return Class name
	 */
	protected abstract String getClassName(String filename);

	/**
	 * @return File Filter
	 */
	protected abstract FileFilter getFileFilter();

	/**
	 * Load classes (create instances)
	 * 
	 * @param foundHostClasses Found Host Classes
	 * @return Loaded Classes
	 */
	public List<T> loadHostClasses(List<FoundHostClass> foundHostClasses) {
		List<T> loadedClasses = new ArrayList<>();

		for (FoundHostClass foundHostClass : foundHostClasses) {
			Class<?> hostClass = foundHostClass.getHostClass();
			try {
				@SuppressWarnings("unchecked")
				T loadedClass = (T)hostClass.getConstructor().newInstance();
				initializeLoadedClass(foundHostClass, loadedClass);
				loadedClasses.add(loadedClass);
				logClassLoaded(foundHostClass, loadedClass);
			} catch (Throwable e) {
				/*
				 * I do this, because a class which has errors should not crash the BilderHerunterlader!
				 */
				logger.error("Could not create instance of class: {}", hostClass.getName(), e);
			}
		}

		return loadedClasses;
	}

	/**
	 * Initiliaze loaded class
	 * 
	 * @param foundHostClass Found Host Class
	 * @param loadedClass Loaded Class
	 */
	protected abstract void initializeLoadedClass(FoundHostClass foundHostClass, T loadedClass);

	/**
	 * Log class loaded
	 * 
	 * @param foundHostClass Found Host Class
	 * @param loadedClass Loaded Class
	 */
	protected abstract void logClassLoaded(FoundHostClass foundHostClass, T loadedClass);

	/**
	 * Checks if the class is correctly implemented (like implements needed interface or extends specific class or has all required methods implemented).
	 * 
	 * Important: If the class is not correctly implemented, than this method should log specific errors
	 * 
	 * @param foundHostClass Host Class which was found
	 * @param file File for logging
	 * @return True if class is correctly implemented, false otherwise
	 */
	protected abstract boolean isClassCorrectlyImplemented(Class<?> foundHostClass, File file);

	/**
	 * @param cl Class
	 * @param fullyQualifiedSuperClassName Fully Qualified SuperClass Name
	 * @return True/False
	 */
	protected boolean hasSuperClass(Class<?> cl, String fullyQualifiedSuperClassName) {
		return fullyQualifiedSuperClassName.equals(cl.getSuperclass().getName());
	}

	/**
	 * Check if the class has the given interface implemented
	 * 
	 * @param cl Class
	 * @param fullyQualifiedInterfaceName Fully Qualified Interface Name
	 * @return True/False
	 */
	protected boolean hasInterface(Class<?> cl, String fullyQualifiedInterfaceName) {
		for (Class<?> iface : cl.getInterfaces()) {
			if (iface.getName().equals(fullyQualifiedInterfaceName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if all methods of the interface are existing in the class
	 * 
	 * @param cl Class
	 * @param iface Interface
	 * @return True/False
	 */
	protected boolean hasAllMethodsImplemented(Class<?> cl, Class<?> iface) {
		for (Method ifaceMethod : iface.getMethods()) {
			boolean exists = false;
			for (Method clMethod : cl.getMethods()) {
				if (equalsMethod(ifaceMethod, clMethod)) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compares name, return-type and parameters of two methods
	 * 
	 * @param method1 Method 1
	 * @param method2 Method 2
	 * @return equals
	 */
	private boolean equalsMethod(Method method1, Method method2) {
		if (!method1.getName().equals(method2.getName())) {
			return false;
		}

		if (!method1.getReturnType().equals(method2.getReturnType())) {
			return false;
		}

		Class<?>[] params1 = method1.getParameterTypes();
		Class<?>[] params2 = method2.getParameterTypes();
		if (params1.length == params2.length) {
			for (int i = 0; i < params1.length; i++) {
				if (!params1[i].equals(params2[i])) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
}

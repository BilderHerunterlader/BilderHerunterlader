package ch.supertomcat.bh.hoster.classloader;

/**
 * Found Host Class
 */
public class FoundHostClass {
	private final Class<?> hostClass;

	private final boolean developer;

	/**
	 * Constructor
	 * 
	 * @param hostClass Host Class
	 * @param developer True if developer, false otherwise
	 */
	public FoundHostClass(Class<?> hostClass, boolean developer) {
		this.hostClass = hostClass;
		this.developer = developer;
	}

	/**
	 * Returns the hostClass
	 * 
	 * @return hostClass
	 */
	public Class<?> getHostClass() {
		return hostClass;
	}

	/**
	 * Returns the developer
	 * 
	 * @return developer
	 */
	public boolean isDeveloper() {
		return developer;
	}
}

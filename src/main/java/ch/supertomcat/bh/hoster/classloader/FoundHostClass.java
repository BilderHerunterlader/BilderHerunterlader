package ch.supertomcat.bh.hoster.classloader;

/**
 * Found Host Class
 * 
 * @param hostClass Host Class
 * @param developer True if developer, false otherwise
 */
public record FoundHostClass(Class<?> hostClass, boolean developer) {
}

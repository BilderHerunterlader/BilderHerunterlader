package ch.supertomcat.bh.preview;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;

/**
 * Preview Disk Cache
 */
public class PreviewDiskCache implements AutoCloseable {
	/**
	 * Cache Name
	 */
	private static final String CACHE_NAME = "previews";

	/**
	 * Cache Manager
	 */
	private final CacheManager cacheManager;

	/**
	 * Constructor
	 */
	public PreviewDiskCache() {
		XmlConfiguration xmlConfig = new XmlConfiguration(getClass().getResource("/ehcache.xml"));
		cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
		cacheManager.init();
	}

	/**
	 * @param url URL
	 * @param preview Preview
	 */
	public void addPreview(String url, byte[] preview) {
		cacheManager.getCache(CACHE_NAME, String.class, byte[].class).put(url, preview);
	}

	/**
	 * @param url URL
	 * @return Preview or null if not in cache
	 */
	public byte[] getPreview(String url) {
		return cacheManager.getCache(CACHE_NAME, String.class, byte[].class).get(url);
	}

	@Override
	public void close() throws Exception {
		cacheManager.close();
	}
}

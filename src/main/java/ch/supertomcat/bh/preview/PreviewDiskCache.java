package ch.supertomcat.bh.preview;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

/**
 * 
 *
 */
public class PreviewDiskCache {
	private static final long MAX_DISK_ENTRIES = 100000;
	private static final int MAX_MEMORY_ENTRIES = 1000;
	private static final String CACHE_NAME = "previews";
	
	private static CacheManager cacheManager = new CacheManager();
	
	static {
		cacheManager.addCache(CACHE_NAME);
		Cache cache = cacheManager.getCache(CACHE_NAME);
		CacheConfiguration cacheConfig = cache.getCacheConfiguration();
		cacheConfig.setLogging(false);
		cacheConfig.setMaxEntriesLocalDisk(MAX_DISK_ENTRIES);
		cacheConfig.setMaxEntriesLocalHeap(MAX_MEMORY_ENTRIES);
		cacheConfig.setTimeToIdleSeconds(0);
		cacheConfig.setTimeToLiveSeconds(0);
	}
	
	/**
	 * @param url URL
	 * @param preview Preview
	 */
	public synchronized void addPreview(String url, byte[] preview) {
		cacheManager.getCache(CACHE_NAME).put(new Element(url, preview));
	}
	
	/**
	 * @param url URL
	 * @return Preview or null if not in cache
	 */
	public synchronized byte[] getPreview(String url) {
		Element el = cacheManager.getCache(CACHE_NAME).get(url);
		if (el == null) {
			return null;
		}
		Object val = el.getObjectValue();
		if (val == null) {
			return null;
		} else {
			return (byte[])val;
		}
	}
}

package ch.supertomcat.bh.hoster.linkextract;

import java.util.List;

/**
 * Extract Configuration
 */
public class ExtractConfig {
	private final List<ExtractConfigWhitelist> whitelists;

	/**
	 * Constructor
	 * 
	 * @param whitelists Whitelists
	 */
	public ExtractConfig(List<ExtractConfigWhitelist> whitelists) {
		this.whitelists = whitelists;
	}

	/**
	 * Returns the whitelists
	 * 
	 * @return Whitelists
	 */
	public List<ExtractConfigWhitelist> getWhitelists() {
		return whitelists;
	}
}

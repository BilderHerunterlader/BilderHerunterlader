package ch.supertomcat.bh.hoster.linkextract;

import java.util.List;

/**
 * 
 *
 */
public class ExtractConfig {
	private List<ExtractConfigWhitelist> whitelists = null;
	
	/**
	 * Constructor
	 * @param whitelists Whitelists
	 */
	public ExtractConfig(List<ExtractConfigWhitelist> whitelists) {
		this.whitelists = whitelists;
	}

	/**
	 * Returns the whitelists
	 * @return Whitelists
	 */
	public List<ExtractConfigWhitelist> getWhitelists() {
		return whitelists;
	}
}

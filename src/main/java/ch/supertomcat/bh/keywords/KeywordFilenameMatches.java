package ch.supertomcat.bh.keywords;

import java.util.List;

/**
 * Keyword Filename Matches
 */
public class KeywordFilenameMatches {
	/**
	 * URL
	 */
	private final String url;

	/**
	 * Matches
	 */
	private final List<KeywordMatch> matches;

	/**
	 * Constructor
	 * 
	 * @param url URL
	 * @param matches Matches
	 */
	public KeywordFilenameMatches(String url, List<KeywordMatch> matches) {
		this.url = url;
		this.matches = matches;
	}

	/**
	 * Returns the url
	 * 
	 * @return url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Returns the matches
	 * 
	 * @return matches
	 */
	public List<KeywordMatch> getMatches() {
		return matches;
	}
}

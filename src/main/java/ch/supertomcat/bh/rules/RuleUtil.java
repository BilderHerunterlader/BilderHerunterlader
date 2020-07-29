package ch.supertomcat.bh.rules;

/**
 * Utility Class for Rules
 */
public final class RuleUtil {
	/**
	 * Constructor
	 */
	private RuleUtil() {
	}

	/**
	 * Returns the filename part of the URL
	 * 
	 * @param url URL
	 * @return Filename part of the URL
	 */
	public static String getFilenamePart(String url) {
		String filenamePart = url;
		int posSlash = url.lastIndexOf('/');
		if (posSlash > -1) {
			filenamePart = url.substring(posSlash + 1);
		}

		int posQueryString = filenamePart.lastIndexOf('?');
		if (posQueryString > -1) {
			filenamePart = filenamePart.substring(0, posQueryString);
		}

		int posFragment = filenamePart.lastIndexOf('#');
		if (posFragment > -1) {
			filenamePart = filenamePart.substring(0, posFragment);
		}
		return filenamePart;
	}
}

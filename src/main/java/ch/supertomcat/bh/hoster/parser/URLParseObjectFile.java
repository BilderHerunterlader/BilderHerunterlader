package ch.supertomcat.bh.hoster.parser;

/**
 * Represents a file to be downloaded
 */
public class URLParseObjectFile {
	/**
	 * The parsed URL
	 */
	private String directLink = "";

	/**
	 * The corrected filename
	 */
	private String correctedFilename = "";

	/**
	 * Constructor
	 */
	public URLParseObjectFile() {
	}

	/**
	 * Constructor
	 * 
	 * @param directLink The parsed URL
	 * @param correctedFilename The corrected filename
	 */
	public URLParseObjectFile(String directLink, String correctedFilename) {
		this.directLink = directLink;
		this.correctedFilename = correctedFilename;
	}

	/**
	 * Returns the parsed URL
	 * 
	 * @return the directLink
	 */
	public String getDirectLink() {
		return directLink;
	}

	/**
	 * Set the parsed URL
	 * 
	 * @param directLink the directLink to set
	 */
	public void setDirectLink(String directLink) {
		this.directLink = directLink;
	}

	/**
	 * Returns the corrected filename
	 * 
	 * @return the correctedFilename
	 */
	public String getCorrectedFilename() {
		return correctedFilename;
	}

	/**
	 * Set the corrected filename
	 * 
	 * @param correctedFilename the correctedFilename to set
	 */
	public void setCorrectedFilename(String correctedFilename) {
		this.correctedFilename = correctedFilename;
	}
}

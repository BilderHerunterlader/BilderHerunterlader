package ch.supertomcat.bh.pic;

/**
 * One object of this class represent a line of tsv-file
 * For more information see ch.supertomcat.bh.importexport.ImportIradaTsv
 * 
 * @see ch.supertomcat.bh.importexport.ImportIradaTsv
 */
public class Tsv {
	/**
	 * Relative path
	 */
	private String relativePath = "";
	
	/**
	 * Container-URL
	 */
	private String containerURL = "";
	
	/**
	 * Timestamp (last modified)
	 */
	private long lastModified = 0;

	/**
	 * Constructor
	 * @param relativePath Relative path
	 * @param containerURL Container-URL
	 * @param lastModified Last modified
	 */
	public Tsv(String relativePath, String containerURL, long lastModified) {
		super();
		this.relativePath = relativePath;
		this.containerURL = containerURL;
		this.lastModified = lastModified;
	}

	/**
	 * Get-Method
	 * @return the relativePath
	 */
	public String getRelativePath() {
		return relativePath;
	}

	/**
	 * Set-Method
	 * @param relativePath the relativePath to set
	 */
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	/**
	 * Get-Method
	 * @return the containerURL
	 */
	public String getContainerURL() {
		return containerURL;
	}

	/**
	 * Set-Method
	 * @param containerURL the containerURL to set
	 */
	public void setContainerURL(String containerURL) {
		this.containerURL = containerURL;
	}

	/**
	 * Get-Method
	 * @return the lastModified
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * Set-Method
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
	
	
}

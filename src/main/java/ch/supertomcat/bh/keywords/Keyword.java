package ch.supertomcat.bh.keywords;

import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * A Keyword is an Object with a title, keywords, absolute DownloadPath, relative DownloadPath
 * and a flag which defines if the absolute or relative path is used.
 * 
 * With this keywords the user can define a specific path on the harddisk for specific keywords.
 * 
 * An example:
 * The user wants that all files containing "Nora Tschirner" in the URL downloaded to
 * C:\Images\Nora Tschirner\
 * So the user defines a new Keyword. Sets the title to "Nora Tschirner".
 * Then set the keywords for the Keyword.
 * Keywords can be seperator by a space. Groups of keywords can be seperated by a ;
 * Example for keywords:
 * Nora Tschirner
 * or
 * Nora Tschirner; Nora Marie Tschirner
 * 
 * Then the user defines an absolute path or a relative path.
 * Example:
 * C:\Images\Nora Tschirner\
 * or
 * Nora Tschirner\
 * or
 * N\Nora Tschirner
 * or something else
 * 
 * Then the user has the set the flag to use absolute or relative path.
 */
public class Keyword implements Comparable<Keyword> {
	/**
	 * Database ID
	 */
	private int id = 0;

	/**
	 * Title
	 */
	private String title = "";

	/**
	 * keywords
	 */
	private String keywords = null;

	/**
	 * Target directory (absolute path)
	 */
	private String downloadPath = "";

	/**
	 * Target directory (relative path)
	 */
	private String relativeDownloadPath = "";

	/**
	 * Use relative path
	 */
	private boolean relativePath = false;

	/**
	 * Constructor
	 * 
	 * @param title Title
	 * @param keywords keywords
	 * @param downloadPath absolute Path
	 * @param relativePath use relative Path
	 * @param relativeDownloadPath relative Path
	 */
	public Keyword(String title, String keywords, String downloadPath, boolean relativePath, String relativeDownloadPath) {
		this.title = title;
		this.keywords = keywords;
		this.downloadPath = downloadPath;
		this.relativePath = relativePath;
		this.relativeDownloadPath = relativeDownloadPath;
	}

	/**
	 * Constructor
	 * 
	 * @param id Database ID
	 * @param title Title
	 * @param keywords keywords
	 * @param downloadPath absolute Path
	 * @param relativePath use relative Path
	 * @param relativeDownloadPath relative Path
	 */
	public Keyword(int id, String title, String keywords, String downloadPath, boolean relativePath, String relativeDownloadPath) {
		this.id = id;
		this.title = title;
		this.keywords = keywords;
		this.downloadPath = downloadPath;
		this.relativePath = relativePath;
		this.relativeDownloadPath = relativeDownloadPath;
	}

	/**
	 * Returns the id
	 * 
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id
	 * 
	 * @param id id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns the target path
	 * This method checks the flag an then returns the absolute or relative path
	 * 
	 * @param settingsManager Settings Manager
	 * @return Target path
	 */
	public String getAdderDownloadPath(SettingsManager settingsManager) {
		String retval = "";
		if (relativePath) {
			retval = settingsManager.getSavePath() + relativeDownloadPath;
			if ((retval.endsWith("/") == false) && (retval.endsWith("\\") == false)) {
				retval += FileUtil.FILE_SEPERATOR;
			}
		} else {
			retval = downloadPath;
			if ((retval.endsWith("/") == false) && (retval.endsWith("\\") == false)) {
				retval += FileUtil.FILE_SEPERATOR;
			}
		}
		return retval;
	}

	/**
	 * Returns the absolute target path
	 * 
	 * @return Target path
	 */
	public String getDownloadPath() {
		return downloadPath;
	}

	/**
	 * Sets the absolute target path
	 * The path is only set when there are no forbidden chars in it!
	 * Forbidden chars are: : * ? &lt; &gt; "
	 * 
	 * @param downloadPath absolute Path
	 */
	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}

	/**
	 * Returns the keywords
	 * 
	 * @return keywords
	 */
	public String getKeywords() {
		return keywords;
	}

	/**
	 * Sets the keywords
	 * 
	 * @param keywords keywords
	 */
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	/**
	 * Returns if relative path is used
	 * 
	 * @return Use relative path
	 */
	public boolean isRelativePath() {
		return relativePath;
	}

	/**
	 * Sets if relative path is used or not
	 * 
	 * @param relativePath Use relative path
	 */
	public void setRelativePath(boolean relativePath) {
		this.relativePath = relativePath;
	}

	/**
	 * Returns the title
	 * 
	 * @return Title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title
	 * 
	 * @param title Title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Returns the relative target path
	 * 
	 * @return Relative path
	 */
	public String getRelativeDownloadPath() {
		return relativeDownloadPath;
	}

	/**
	 * Sets the relative target path
	 * The path is only set when there are no forbidden chars in it!
	 * Forbidden chars are: : * ? &lt; &gt; "
	 * 
	 * @param relativeDownloadPath Relative path
	 */
	public void setRelativeDownloadPath(String relativeDownloadPath) {
		this.relativeDownloadPath = relativeDownloadPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Keyword other = (Keyword)obj;
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(Keyword o) {
		return this.getTitle().compareTo(o.getTitle());
	}

	@Override
	public String toString() {
		return title;
	}
}

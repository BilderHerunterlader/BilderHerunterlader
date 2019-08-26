package ch.supertomcat.bh.pic;

import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.hoster.Hoster;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;

/**
 * This class is used to store information about a download,
 * to add them to the Download-Selection
 */
public class URL {
	private static final String HTTPS_PROTOCOL = "https://";

	/**
	 * Container-URL
	 */
	private String url;

	/**
	 * Flag if url is already checked for redirect
	 */
	private boolean urlAlreadyCheckedForRedirect = false;

	/**
	 * Flag if this link was already downloaded
	 */
	private boolean alreadyDownloaded = false;

	/**
	 * Flag if this link is blacklisted
	 */
	private boolean blacklisted = false;

	/**
	 * Target Directory
	 */
	private String targetPath = "";

	/**
	 * Corrected filename
	 */
	private String filenameCorrected;

	/**
	 * Thumbnail-URL
	 */
	private String thumb;

	/**
	 * The host which is responsible to parse this url
	 */
	private Hoster host = null;

	/**
	 * URL of the site which contains this url
	 */
	private String ThreadURL = "";

	private boolean httpsURL;

	/**
	 * Constructor
	 * 
	 * @param url Container-URL
	 */
	public URL(String url) {
		this(url, "");
	}

	/**
	 * Constructor
	 * 
	 * @param url Container-URL
	 * @param thumb Thumbnail-URL
	 */
	public URL(String url, String thumb) {
		url = url.replaceAll("\"", "");
		url = HTTPUtil.trimURL(url);
		this.url = url;
		this.thumb = thumb;
		this.filenameCorrected = Localization.getString("Unkown");
		this.httpsURL = this.url.startsWith(HTTPS_PROTOCOL);
		host = HostManager.instance().getHosterForURL(this.url);
	}

	/**
	 * Set-Method
	 * 
	 * @param filenameCorrected Corrected filename
	 */
	public void setFilenameCorrected(String filenameCorrected) {
		this.filenameCorrected = filenameCorrected;
	}

	/**
	 * Get-Method
	 * 
	 * @return Corrected filename
	 */
	public String getFilenameCorrected() {
		return this.filenameCorrected;
	}

	/**
	 * Get-Method
	 * 
	 * @return Container-URL
	 */
	public String getURL() {
		return this.url;
	}

	/**
	 * GET-Method
	 * 
	 * @return the thumb
	 */
	public String getThumb() {
		return thumb;
	}

	/**
	 * SET-Method
	 * 
	 * @param thumb the thumb to set
	 */
	public void setThumb(String thumb) {
		thumb = HTTPUtil.trimURL(thumb);
		this.thumb = thumb;
	}

	/**
	 * Checks the url for redirect
	 */
	public void checkURLForRedirect() {
		if (urlAlreadyCheckedForRedirect) {
			return;
		}

		// To prevent endless loops we limit the redirects
		int maxRedirects = 50;
		int i = 0;
		String redirectedURL = this.url;
		while (i < maxRedirects) {
			redirectedURL = HostManager.instance().getRedirectManager().checkURLForRedirect(this);
			if (this.url.equals(redirectedURL)) {
				break;
			}
			this.url = redirectedURL;
			i++;
		}
		host = HostManager.instance().getHosterForURL(this.url);

		urlAlreadyCheckedForRedirect = true;
	}

	/**
	 * Returns the targetPath
	 * 
	 * @return targetPath
	 */
	public String getTargetPath() {
		return targetPath;
	}

	/**
	 * Sets the targetPath
	 * 
	 * @param targetPath targetPath
	 */
	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	/**
	 * Returns the host
	 * 
	 * @return host
	 */
	public Hoster getHost() {
		return host;
	}

	/**
	 * Returns the threadURL
	 * 
	 * @return threadURL
	 */
	public String getThreadURL() {
		return ThreadURL;
	}

	/**
	 * Sets the threadURL
	 * 
	 * @param threadURL threadURL
	 */
	public void setThreadURL(String threadURL) {
		threadURL = HTTPUtil.trimURL(threadURL);
		ThreadURL = threadURL;
	}

	/**
	 * Returns the alreadyDownloaded
	 * 
	 * @return alreadyDownloaded
	 */
	public boolean isAlreadyDownloaded() {
		return alreadyDownloaded;
	}

	/**
	 * Sets the alreadyDownloaded
	 * 
	 * @param alreadyDownloaded alreadyDownloaded
	 */
	public void setAlreadyDownloaded(boolean alreadyDownloaded) {
		this.alreadyDownloaded = alreadyDownloaded;
	}

	/**
	 * Returns the blacklisted
	 * 
	 * @return blacklisted
	 */
	public boolean isBlacklisted() {
		return blacklisted;
	}

	/**
	 * Sets the blacklisted
	 * 
	 * @param blacklisted blacklisted
	 */
	public void setBlacklisted(boolean blacklisted) {
		this.blacklisted = blacklisted;
	}

	/**
	 * Returns the httpsURL
	 * 
	 * @return httpsURL
	 */
	public boolean isHttpsURL() {
		return httpsURL;
	}

	/**
	 * Equals method, which respects duplicate remove mode from Hoster
	 * 
	 * @param otherURL URL
	 * @return True if equals, false otherwise
	 */
	public boolean equalsRemoveDuplicates(URL otherURL) {
		if (host != null) {
			return host.removeDuplicateEqualsMethod(this, otherURL);
		} else {
			return this.getURL().equals(otherURL.getURL());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((thumb == null) ? 0 : thumb.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		URL other = (URL)obj;
		if (thumb == null) {
			if (other.thumb != null) {
				return false;
			}
		} else if (!thumb.equals(other.thumb)) {
			return false;
		}
		if (url == null) {
			if (other.url != null) {
				return false;
			}
		} else if (!url.equals(other.url)) {
			return false;
		}
		return true;
	}
}

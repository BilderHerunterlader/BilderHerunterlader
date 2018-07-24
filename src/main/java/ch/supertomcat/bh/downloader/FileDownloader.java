package ch.supertomcat.bh.downloader;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.pic.Pic;

/**
 * Interface for downloading files
 */
public interface FileDownloader {
	/**
	 * Download File
	 * 
	 * @param pic Pic
	 * @throws HostException
	 */
	public void downloadFile(Pic pic) throws HostException;
}

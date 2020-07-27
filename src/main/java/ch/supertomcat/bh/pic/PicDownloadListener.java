package ch.supertomcat.bh.pic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.downloader.FileDownloader;
import ch.supertomcat.bh.downloader.FileDownloaderFactory;
import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.queue.IDownloadListener;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;

/**
 * Download Listener Implementation
 */
public class PicDownloadListener implements IDownloadListener {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Pic
	 */
	private final Pic pic;

	/**
	 * File Downloader Factory
	 */
	private final FileDownloaderFactory fileDownloaderFactory;

	/**
	 * Constructor
	 * 
	 * @param pic Pic
	 * @param fileDownloaderFactory File Downloader Factory
	 */
	public PicDownloadListener(Pic pic, FileDownloaderFactory fileDownloaderFactory) {
		this.pic = pic;
		this.fileDownloaderFactory = fileDownloaderFactory;
	}

	/**
	 * Returns the pic
	 * 
	 * @return pic
	 */
	public Pic getPic() {
		return pic;
	}

	/**
	 * Download
	 * 
	 * @return True if download was started, false otherwise
	 */
	public boolean download() {
		/*
		 * When the startDownload-Method is called, the download requests the
		 * Queue for a download-slot. The Queue fires this method, as soon as there is
		 * free download-slot.
		 * This method returns true if the download is really started.
		 * If the status is something else than Pic.WAITING here, then
		 * we don't start the download! So we return false!
		 */
		PicState status = pic.getStatus();
		if (status == PicState.WAITING) {
			// Set the status to DOWNLOADING
			pic.setStatus(PicState.DOWNLOADING);

			try {
				executeDownload();
				return true;
			} catch (Exception e) {
				pic.setStatus(PicState.FAILED);
				throw e;
			}
		}
		return false;
	}

	/**
	 * Download
	 */
	private void executeDownload() {
		/*
		 * So, first some information what happens in this method.
		 * 
		 * First we have a Container-URL. This is called so because when
		 * downloading images from imagehost, there is almost always a link
		 * to a page, which contains the image.
		 * So we get only the link to that page.
		 * To get the direct link to image, we ask the HostManager to parse the URL.
		 * Note, here also it doesn't have to be an image, the kind of file is
		 * unnessesary!
		 * So, now the HostManager goes trough all hostclasses and checks, if
		 * there is one, which is able to parse the URL. If there is one, the
		 * HostManager calls this class to do that. If this worked we get from the
		 * HostManager the parsed URL. If not we get null or and empty URL.
		 * 
		 * But in this process there could be problems. What happens, wenn in the method
		 * of a hostclass an error occured? Right, the complete download thread stands still!
		 * So, the download-slot is still been registered for this download and the
		 * download is unable to free it.
		 * 
		 * I defined a new type of Exception for this.
		 * See ch.supertomcat.bh.exceptions.HostException
		 * Because every hostclass has to implement the IHoster interface, the method
		 * to parse the URL of every class will throw this exception.
		 * But to really avoid this problem, i think i must define the method in the
		 * interface to throw all exception. Because the HostException is only thrown, when
		 * the programmer of a hostclass, throw one.
		 * Now wat happens, when a hostclass calls a method, which does
		 * not throw an exception?
		 * Right, again the Thread stands still. So, there is not really a possibillity to
		 * avoid this completely.
		 */
		if (pic.isStop()) {
			// Status aendern und Slot freigeben
			pic.setStatus(PicState.SLEEPING);
			PicProgress progress = pic.getProgress();
			progress.setBytesTotal(pic.getSize());
			progress.setBytesDownloaded(0);
			pic.progressUpdated();
			return;
		}

		String containerURL = pic.getContainerURL();

		FileDownloader downloader;
		String encodedContainerURL;
		if (containerURL.startsWith("http:") || containerURL.startsWith("https:")) {
			encodedContainerURL = HTTPUtil.encodeURL(containerURL, true);
		} else {
			encodedContainerURL = containerURL;
		}
		if (HTTPUtil.isURL(encodedContainerURL)) {
			logger.info("Downloading: {}", containerURL);
			downloader = fileDownloaderFactory.createHTTPFileDownloader();
		} else {
			logger.info("Sorting: {}", containerURL);
			downloader = fileDownloaderFactory.createLocalFileDownloader();
		}
		try {
			downloader.downloadFile(pic);
		} catch (HostException e) {
			// Nothing to do
		}
	}

	@Override
	public String getContainerURL() {
		return pic.getContainerURL();
	}

	@Override
	public void recalcutateRate() {
		pic.recalcutateRate();
	}

	@Override
	public double getDownloadRate() {
		return pic.getDownloadRate();
	}

}

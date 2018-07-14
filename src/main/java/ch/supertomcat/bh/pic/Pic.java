package ch.supertomcat.bh.pic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostAbortedException;
import ch.supertomcat.bh.exceptions.HostCompletedException;
import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostFileNotExistException;
import ch.supertomcat.bh.exceptions.HostFileTemporaryOfflineException;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.hoster.Hoster;
import ch.supertomcat.bh.hoster.URLParseObject;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.queue.IDownloadListener;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.options.Subdir;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcattools.fileiotools.FileTool;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.UnitFormatTool;
import ch.supertomcat.supertomcattools.httptools.HTTPTool;
import ch.supertomcat.supertomcattools.imagetools.ImageInfo;
import ch.supertomcat.supertomcattools.regextools.RegexReplacePipeline;

/**
 * Thread which does the download
 * This class is called Pic, because it is an shortcut for Picture.
 * And as is started programming this program it was only for downloading
 * pictures.
 * But this class can not only download pictures, all kind of files can
 * be downloaded.
 * And no, i'm not willing to rename this class ;-)
 * 
 * Make sure to call initializeListenerList method after db4o deserialization
 * to ensure transient listeners member is initialized
 */
public class Pic implements Runnable, IDownloadListener {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(Pic.class);

	/**
	 * Download is sleeping
	 */
	public static final int SLEEPING = 0;

	/**
	 * Download is waiting for a free slot
	 */
	public static final int WAITING = 1;

	/**
	 * Download is downloading
	 */
	public static final int DOWNLOADING = 2;

	/**
	 * Download is complete
	 */
	public static final int COMPLETE = 3;

	/**
	 * Download failed
	 */
	public static final int FAILED = 4;

	/**
	 * Download failed
	 */
	public static final int ABORTING = 5;

	/**
	 * Download failed
	 */
	public static final int FAILED_FILE_NOT_EXIST = 6;

	/**
	 * Download failed
	 */
	public static final int FAILED_FILE_TEMPORARY_OFFLINE = 7;

	/**
	 * Strings for status of the download
	 */
	public static final String[] STATUS_T = { Localization.getString("Sleeping"), Localization.getString("Waiting"), Localization.getString("Downloading"), Localization
			.getString("Complete"), Localization
					.getString("Failed"), Localization.getString("Aborting"), Localization.getString("FileNotExistsOnTheServer"), Localization.getString("FileTemporaryOffline") };

	/**
	 * Pattern for filename in content-disposition
	 */
	private static Pattern contentDispositionFilenamePattern = compileContentDispositionFilenamePattern();

	/**
	 * Database ID
	 */
	private int id = 0;

	/**
	 * Container-URL
	 */
	private String containerURL = "";

	/**
	 * Thread-URL (referrer)
	 */
	private String threadURL = "";

	/**
	 * Thumbnail-URL
	 */
	private String thumb = "";

	/**
	 * Target path
	 */
	private String targetPath = "";

	/**
	 * Filename
	 */
	private String targetFilename = "";

	/**
	 * This flag causes the class not to use the filename provided by a hostclass.
	 * So if the user changes the filename somewhere in the GUI, then this flag is
	 * set to true!
	 * If the filename is changed by the Set-Method to a empty string, then this flag
	 * is automatically set to false
	 */
	private boolean fixedTargetFilename = false;

	/**
	 * Timestamp of last modification of the file
	 * 
	 * @see ch.supertomcat.bh.importexport.ImportIradaTsv
	 */
	private long lastModified = 0;

	/**
	 * This flag causes the class to set the last modification time of the file
	 */
	private boolean fixedLastModified = false;

	/**
	 * Filesize
	 */
	private long size = 0;

	/**
	 * Listener
	 */
	transient private List<IPicListener> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Status of the download
	 */
	private int status = Pic.SLEEPING;

	/**
	 * Error-Message
	 */
	private String errMsg = "";

	/**
	 * Count of failures
	 */
	private int failedCount = 0;

	/**
	 * Download deactivated
	 */
	private boolean deactivated = false;

	/**
	 * Flag to let the download method rename the file with the filename from contentdisposition
	 */
	private boolean renameWithContentDisposition = false;

	/**
	 * Stop the download
	 */
	private boolean stop = false;

	/**
	 * This flag is set to true, when the stop-button on the gui is pressed once.
	 * On the first click, the running downloads are not stopped, only the ones are not running.
	 * On the second click, also running downloads are stopped imidiatly!
	 */
	private boolean stopOncePressed = false;

	/**
	 * Recalculate the downloadrate
	 */
	private boolean recalcutateRate = false;

	private double downloadBitrate = -1;

	/**
	 * Date and Time
	 */
	private long dateTime;

	private String downloadURL = "";

	/**
	 * Constructor
	 * 
	 * @param urlContainer Container-URL
	 * @param targetFilename Filename
	 * @param targetPath Target path
	 */
	public Pic(String urlContainer, String targetFilename, String targetPath) {
		urlContainer = HTTPTool.trimURL(urlContainer);
		this.containerURL = urlContainer;
		this.targetPath = targetPath;
		if ((this.targetPath.endsWith("/") == false) && (this.targetPath.endsWith("\\") == false)) {
			this.targetPath += FileTool.FILE_SEPERATOR;
		}
		this.targetFilename = targetFilename;
		this.dateTime = System.currentTimeMillis();
	}

	/**
	 * Constructor
	 * 
	 * @param id Database ID
	 * @param urlContainer Container-URL
	 * @param targetFilename Filename
	 * @param targetPath Target path
	 * @param threadURL Thread-URL
	 * @param thumb Thumbnail-URL
	 * @param downloadURL Download URL
	 * @param fixedTargetFilename Fixed Target Filename Flag
	 * @param lastModified Last Modified
	 * @param fixedLastModified Fixed Last Modified Flag
	 * @param size Size
	 * @param status Status
	 * @param errMsg Error Message
	 * @param deactivated Deactivated
	 * @param renameWithContentDisposition Rename With Content Disposition Flag
	 * @param dateTime DateTime
	 */
	public Pic(int id, String urlContainer, String targetFilename, String targetPath, String threadURL, String thumb, String downloadURL, boolean fixedTargetFilename, long lastModified,
			boolean fixedLastModified, long size, int status, String errMsg, boolean deactivated, boolean renameWithContentDisposition, long dateTime) {
		this(urlContainer, targetFilename, targetPath);
		this.id = id;
		this.threadURL = threadURL;
		this.thumb = thumb;
		this.downloadURL = downloadURL;
		this.fixedTargetFilename = fixedTargetFilename;
		this.lastModified = lastModified;
		this.fixedLastModified = fixedLastModified;
		this.size = size;
		if (!((status != Pic.SLEEPING) && (status != Pic.WAITING) && (status != Pic.DOWNLOADING) && (status != Pic.COMPLETE) && (status != Pic.FAILED) && (status != Pic.FAILED_FILE_NOT_EXIST)
				&& (status != Pic.ABORTING) && (status != Pic.FAILED_FILE_TEMPORARY_OFFLINE))) {
			this.status = status;
		}
		this.errMsg = errMsg;
		this.deactivated = deactivated;
		this.renameWithContentDisposition = renameWithContentDisposition;
		this.dateTime = dateTime;
	}

	/**
	 * Start the download
	 */
	public synchronized void startDownload() {
		// If the download is deactivated, then we don't start the download
		if (this.deactivated) {
			return;
		}

		// If there is not already a request for a download-slot and the status is sleeping or failed
		if ((!(DownloadQueueManager.instance().isDLSlotListenerRegistered(this)))
				&& ((this.status == Pic.SLEEPING) || (this.status == Pic.FAILED) || (this.status == Pic.FAILED_FILE_NOT_EXIST) || (this.status == FAILED_FILE_TEMPORARY_OFFLINE))) {
			stop = false;
			stopOncePressed = false;
			setStatus(Pic.WAITING); // Change the status
			DownloadQueueManager.instance().addDLSlotListener(this); // Request a download slot
		}
	}

	/**
	 * Stop the download
	 * This does not immediately stop the download, it only changes
	 * the stop-flag to true and the download method will stop if this
	 * flag is true.
	 * When the download is waiting for the parsed URL, this could take some time...
	 */
	public synchronized void stopDownload() {
		if (stopOncePressed) {
			stop = true;
		}
		if (this.status == Pic.WAITING) {
			stop = true;
			setStatus(Pic.SLEEPING);
			progressBarChanged(0, Pic.STATUS_T[Pic.SLEEPING]);
			/*
			 * Free the download-slot
			 * Maybe i should do this not at this point, because the user could
			 * start the download again, before it has really stopped
			 */
			DownloadQueueManager.instance().removeDLSlotListenerStopping(this);
		} else if (this.status == Pic.DOWNLOADING) {
			if (stop) {
				setStatus(Pic.ABORTING);
			}
		}
		stopOncePressed = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
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

		if (stop) {
			// Status aendern und Slot freigeben
			setStatus(Pic.SLEEPING);
			progressBarChanged(0, Pic.STATUS_T[Pic.SLEEPING]);
			DownloadQueueManager.instance().removeDLSlotListener(this);
			return;
		}

		setStatus(Pic.DOWNLOADING);
		progressBarChanged(0, Pic.STATUS_T[Pic.DOWNLOADING]);

		// Direct link to the file
		String url = "";
		/*
		 * localFile is true, when the URL is not a http-link, but
		 * a file on the harddisk, which has to be sort.
		 */
		boolean localFile = false;
		File fLocal = null;

		boolean stopped = false;

		// Absolute path and filename
		String target = "";

		String referrer = this.containerURL;

		// Create a new URLParseObject
		URLParseObject upo = new URLParseObject(containerURL, thumb, this);
		URLParseObject result = null;

		// Now we try to get the direct link
		try {
			// Now let the HostManager parse it
			result = HostManager.instance().parseURL(upo);
			// If we get null back
			if (result == null) {
				throw new HostException(Localization.getString("ErrorImageURL"));
			}
			// if the direct link is not null and not empty
			if (result.getDirectLink() != null && !result.getDirectLink().equals("")) {
				// get the direct link
				url = result.getDirectLink();
				downloadURL = url;

				// Check if the URL is a file
				fLocal = new File(url);
				if (fLocal.exists()) {
					localFile = true;
				}

				referrer = result.getContainerURL();
				Hoster lastHoster = result.getLastHoster();
				if ((lastHoster != null) && (lastHoster instanceof Rule)) {
					Rule lastRule = (Rule)lastHoster;
					switch (lastRule.getReferrerModeDownload()) {
						case Rule.REFERRER_NO_REFERRER:
							referrer = "";
							break;
						case Rule.REFERRER_LAST_CONTAINER_URL:
							referrer = upo.getContainerURL();
							break;
						case Rule.REFERRER_FIRST_CONTAINER_URL:
							referrer = upo.getFirstContainerURL();
							break;
						case Rule.REFERRER_ORIGIN_PAGE:
							referrer = upo.getPic().getThreadURL();
							break;
						case Rule.REFERRER_CUSTOM:
							referrer = lastRule.getCustomReferrerDownload();
							break;
					}
				}

				// if the hostclass gives us a nice filename
				if ((result.getCorrectedFilename() != null) && (!result.getCorrectedFilename().equals("")) && ((this.fixedTargetFilename == false) || (this.targetFilename.length() == 0))) {
					// get it
					this.targetFilename = BHUtil.filterFilename(result.getCorrectedFilename());
				}

				// If no specific filename was set, then get it from URL
				if (this.targetFilename.isEmpty() || this.targetFilename.equals(Localization.getString("Unkown"))) {
					String filenameFromURL;
					if (localFile) {
						filenameFromURL = FileTool.getFilename(url);
					} else {
						filenameFromURL = HTTPTool.getFilenameFromURL(url, Localization.getString("Unkown"));
					}
					this.targetFilename = BHUtil.filterFilename(filenameFromURL);
				}
			} else {
				throw new HostException(Localization.getString("ErrorImageURL"));
			}
		} catch (HostCompletedException e) {
			/*
			 * If we get this exception, we have to set status of
			 * the download to complete and give the download-slot
			 * back to the Queue!
			 */
			progressBarChanged(0, Pic.STATUS_T[Pic.COMPLETE]);
			setStatus(Pic.COMPLETE);
			DownloadQueueManager.instance().removeDLSlotListener(this); // important!
			return;
		} catch (HostAbortedException e) {
			progressBarChanged(0, Pic.STATUS_T[Pic.SLEEPING]);
			setStatus(Pic.SLEEPING);
			DownloadQueueManager.instance().removeDLSlotListener(this); // important!
			return;
		} catch (HostFileTemporaryOfflineException e) {
			/*
			 * If we get this exception, we have to set status of
			 * the download to failed and give the download-slot
			 * back to the Queue!
			 */
			this.failedCount = SettingsManager.instance().getMaxFailedCount(); // increase the failure-counter
			checkDeactivate(); // check if the download has to be deactivated
			setStatus(Pic.FAILED_FILE_TEMPORARY_OFFLINE, e.getMessage());
			progressBarChanged(0, Pic.STATUS_T[Pic.FAILED_FILE_TEMPORARY_OFFLINE]);
			DownloadQueueManager.instance().removeDLSlotListener(this); // important!
			logger.debug("Download failed: " + e.getMessage());
			return;
		} catch (HostFileNotExistException e) {
			/*
			 * If we get this exception, we have to set status of
			 * the download to failed and give the download-slot
			 * back to the Queue!
			 */
			this.failedCount = SettingsManager.instance().getMaxFailedCount(); // increase the failure-counter
			checkDeactivate(); // check if the download has to be deactivated
			setStatus(Pic.FAILED_FILE_NOT_EXIST, e.getMessage());
			progressBarChanged(0, Pic.STATUS_T[Pic.FAILED_FILE_NOT_EXIST]);
			DownloadQueueManager.instance().removeDLSlotListener(this); // important!
			logger.debug("Download failed: " + e.getMessage());
			return;
		} catch (HostException e) {
			/*
			 * If we get this exception, we have to set status of
			 * the download to failed and give the download-slot
			 * back to the Queue!
			 */
			this.failedCount++; // increase the failure-counter
			checkDeactivate(); // check if the download has to be deactivated
			setStatus(Pic.FAILED, e.getMessage());
			progressBarChanged(0, Pic.STATUS_T[Pic.FAILED]);
			DownloadQueueManager.instance().removeDLSlotListener(this); // important!
			StackTraceElement[] stackTrace = e.getStackTrace();
			String stackTraceMessage = stackTrace.length > 0 ? stackTrace[0].getClassName() + ":" + stackTrace[0].getFileName() + ":" + stackTrace[0].getLineNumber() : "";
			String causeMessage = "";
			Throwable cause = e.getCause();
			if (cause != null) {
				StackTraceElement[] causeStackTrace = cause.getStackTrace();
				String causeStackTraceMessage = causeStackTrace.length > 0 ? causeStackTrace[0].getClassName() + ":" + causeStackTrace[0].getFileName() + ":" + causeStackTrace[0].getLineNumber() : "";
				causeMessage = "\n" + cause.getMessage() + " " + causeStackTraceMessage;
			}
			logger.error(e.getMessage() + " " + stackTraceMessage + causeMessage);
			logger.error("Download failed -> " + upo.getHosterStackTrace());
			return;
		}

		/*
		 * Because the parsing could took a lot of time we check here if
		 * the user has stopped the download.
		 */
		if (stop) {
			setStatus(Pic.SLEEPING);
			progressBarChanged(0, Pic.STATUS_T[Pic.SLEEPING]);
			DownloadQueueManager.instance().removeDLSlotListener(this); // important!
			return;
		}

		if (!localFile) {
			// And replace the %20 in the filename, if there are any ;-)
			this.targetFilename = this.targetFilename.replaceAll("%20", " ");

			/*
			 * Now we filter not allowed chars and make sure path and filename
			 * are not too long.
			 */
			this.targetPath = BHUtil.filterPath(this.targetPath);
			this.targetPath = FileTool.pathRTrim(this.targetPath);
			this.targetFilename = BHUtil.filterPath(this.targetFilename);

			boolean bReducePathLength = true;
			if (result.checkExistInfo("ReducePathLength") && result.getInfo("ReducePathLength") instanceof Boolean) {
				bReducePathLength = (Boolean)result.getInfo("ReducePathLength");
			}

			boolean bReduceFilenameLength = true;
			if (result.checkExistInfo("ReduceFilenameLength") && result.getInfo("ReduceFilenameLength") instanceof Boolean) {
				bReduceFilenameLength = (Boolean)result.getInfo("ReduceFilenameLength");
			}

			if (bReducePathLength) {
				this.targetPath = FileTool.reducePathLength(this.targetPath);
			}
			if (bReduceFilenameLength) {
				this.targetFilename = FileTool.reduceFilenameLength(this.targetFilename);
			}
		}

		// Create the target directory if it doesen't exist
		File fTarget = new File(this.targetPath);
		String strRet = Pic.createDirectory(fTarget, this.targetPath);
		if (strRet == null) {
			// If the directory could not be created
			progressBarChanged(0, Pic.STATUS_T[Pic.FAILED]);
			setStatus(Pic.FAILED, Localization.getString("ErrorDirectoryCouldNotBeCreated"));
			fLocal = null;
			fTarget = null;
			DownloadQueueManager.instance().removeDLSlotListener(this); // important!
			return;
		}

		/*
		 * Because the createDirectory-Method could change the pathname
		 * use the result from it.
		 */
		this.targetPath = strRet;
		// New merge path and filename together
		target = this.targetPath + this.targetFilename;
		strRet = null;
		fTarget = null;

		File f = new File(target);
		if (localFile) {
			// if we have a file on the harddisk
			if (f.getAbsolutePath().equals(fLocal.getAbsolutePath())) {
				// And the old path and the path were we should move it is the same, we are done
				progressBarChanged(size, Pic.STATUS_T[Pic.COMPLETE]);
				setStatus(Pic.COMPLETE);
				fLocal = null;
				f = null;
				DownloadQueueManager.instance().removeDLSlotListener(this); // important!
				return;
			}
			// Get the filesize
			setSize((int)fLocal.length());
			progressBarChanged(0, Pic.STATUS_T[Pic.DOWNLOADING]);

			// Move the file
			File fMoved = Pic.moveFile(fLocal, new File(this.targetPath));
			if (fMoved == null) {
				// If the file could not be moved
				logger.error("File '" + fLocal.getAbsolutePath() + "' could not be moved to '" + this.targetPath + "'");
				setStatus(Pic.FAILED, Localization.getString("ErrorFileCouldNotBeMoved"));
				progressBarChanged(0, Pic.STATUS_T[Pic.FAILED]);
			} else {
				this.targetPath = FileTool.getDirectory(fMoved.getAbsolutePath());
				this.targetFilename = fMoved.getName();
				targetChanged();
				progressBarChanged(size, Pic.STATUS_T[Pic.COMPLETE]);
				setStatus(Pic.COMPLETE);
			}
			// Give the slot back to the Queue
			DownloadQueueManager.instance().removeDLSlotListener(this); // important!
			return;
		} else {
			// If we don't have a file on the harddisk here, but an URL

			// Get the cookies for the url
			String cookies = CookieManager.getCookies(url);

			/*
			 * Get a HttpClient
			 * We have to use this method, because otherwise we
			 * have to check here if the user uses a proxy. With
			 * this method we get a configured HttpClient, set to use
			 * a proxy if needed and set user and password of the proxy.
			 */
			boolean nonMultiThreadedHttpClient = false;
			if (result.checkExistInfo("useNonMultithreadedHttpClient") && result.getInfo("useNonMultithreadedHttpClient") instanceof Boolean
					&& (Boolean)result.getInfo("useNonMultithreadedHttpClient") == true) {
				nonMultiThreadedHttpClient = true;
			}

			HttpUriRequest method = null;
			try (CloseableHttpClient client = nonMultiThreadedHttpClient ? ProxyManager.instance().getNonMultithreadedHTTPClient() : ProxyManager.instance().getHTTPClient()) {
				String encodedURL = HTTPTool.encodeURL(url);

				// Create a new GetMethod or PostMethod and set timeouts, cookies, user-agent and so on
				if (result.checkExistInfo("useMethod") && result.getInfo("useMethod") instanceof String && "POST".equals(result.getInfo("useMethod"))) {
					method = new HttpPost(encodedURL);
				} else {
					method = new HttpGet(encodedURL);
				}
				String userAgent = SettingsManager.instance().getUserAgent();
				if (result.checkExistInfo("useUserAgent") && result.getInfo("useUserAgent") instanceof String) {
					userAgent = (String)result.getInfo("useUserAgent");
				}
				method.setHeader("User-Agent", userAgent);
				if (result.checkExistInfo("useReferrer") && result.getInfo("useReferrer") instanceof String) {
					referrer = (String)result.getInfo("useReferrer");
				}
				if (referrer.length() > 0) {
					method.setHeader("Referer", referrer);
				}
				boolean sendCookies = true;
				if (result.checkExistInfo("sendCookies") && result.getInfo("useCookies") instanceof Boolean) {
					sendCookies = (Boolean)result.getInfo("useCookies");
				}
				if (result.checkExistInfo("useCookies") && result.getInfo("useCookies") instanceof String) {
					cookies = (String)result.getInfo("useCookies");
				}
				if (sendCookies && !cookies.isEmpty()) {
					method.setHeader("Cookie", cookies);
				}

				// Good, now open the connection
				try (CloseableHttpResponse response = client.execute(method)) {
					int statusCode = response.getStatusLine().getStatusCode();

					if (statusCode < 200 || statusCode >= 300) {
						this.failedCount++;
						checkDeactivate();
						setStatus(Pic.FAILED, "HTTP-Error: " + response.getStatusLine());
						progressBarChanged(0, Pic.STATUS_T[Pic.FAILED]);
						DownloadQueueManager.instance().removeDLSlotListener(this); // important!
						method.abort();
						return;
					}

					if (renameWithContentDisposition && fixedTargetFilename == false) {
						Header contentDisposition = response.getFirstHeader("content-disposition");
						if (contentDisposition != null) {
							String val = contentDisposition.getValue();
							logger.debug("contentdispval: " + val);
							try {
								val = val.replaceAll("\\\\", "");
								Matcher matcher = contentDispositionFilenamePattern.matcher(val);
								String cdResult = "";
								if (matcher.find()) {
									logger.debug("Matcher found");
									cdResult = matcher.replaceAll("$3");
									logger.debug("Result: " + cdResult);
									if (cdResult.length() > 0) {
										cdResult = BHUtil.filterFilename(cdResult);
										this.targetFilename = cdResult;
										target = this.targetPath + this.targetFilename;
										f = new File(target);
									}
								}
							} catch (Exception e) {
							}
						}
					}

					RegexReplacePipeline regexPipe = SettingsManager.instance().getRegexReplacePipelineFilename();
					this.targetFilename = regexPipe.getReplacedFilename(this.targetFilename);
					this.targetFilename = BHUtil.filterFilename(this.targetFilename);
					target = this.targetPath + this.targetFilename;
					f = new File(target);

					/*
					 * Now we create a new file.
					 * The createFile-Method gives us a File back
					 * because there could already be a file of the same
					 * name, so get back a new filename or another pathname.
					 */
					File fRetval = Pic.createFile(f, this.targetFilename, this.targetPath);
					if (fRetval == null) {
						// If the file coult not be created
						setStatus(Pic.FAILED, Localization.getString("ErrorFileCouldNotBeCreated"));
						progressBarChanged(0, Pic.STATUS_T[Pic.FAILED]);
						DownloadQueueManager.instance().removeDLSlotListener(this); // important!
						method.abort();
						return;
					}
					this.targetFilename = fRetval.getName();
					target = this.targetPath + this.targetFilename;
					fRetval = null;
					f = null;
					targetChanged(); // Let the listeners know, that the target has changed

					/*
					 * Get the filesize
					 * Note: Some servers doesn't give us the filesize, so
					 * the filesize is maybe 0
					 */
					if (method instanceof HttpGet) {
						long contentLength = response.getEntity().getContentLength();
						setSize(contentLength >= 0 ? contentLength : 0);
					} else if (method instanceof HttpPost) {
						long contentLength = response.getEntity().getContentLength();
						setSize(contentLength >= 0 ? contentLength : 0);
					} else {
						setSize(0);
					}

					if ((this.size > 0) && (this.size < SettingsManager.instance().getMinFilesize())) {
						/*
						 * The user can set in an options, which defines a minimum filesize.
						 * If the file here is to small
						 * Note: i check this here, and not above (where we get the filesize)
						 * because some servers don't give the filesize.
						 * So only here, we know the filesize really.
						 */
						this.failedCount = SettingsManager.instance().getMaxFailedCount(); // increase the failure-counter
						checkDeactivate(); // check if the download has to be deactivated
						setStatus(Pic.FAILED, Localization.getString("ErrorFilesizeToSmall"));
						progressBarChanged(0, Pic.STATUS_T[Pic.FAILED]);
						DownloadQueueManager.instance().removeDLSlotListener(this); // important!
						logger.error("Download failed (Filesize is too small): '" + this.containerURL + "'");
						// Now we have to delete the file
						File fileT = new File(target);
						if (fileT.exists()) {
							fileT.delete();
						}
						fileT = null;
						method.abort();
						return;
					}

					progressBarChanged(0, Pic.STATUS_T[Pic.WAITING]);

					long iBW = 0; // the amount of bytes we read since started downloading

					// Now get the inputstream and outputstream
					try (InputStream in = response.getEntity().getContent(); FileOutputStream out = new FileOutputStream(target)) {
						// Read some bytes to a buffer and write them to the outputstream
						int n; // the amount of bytes where were read per loop course
						byte[] buf = new byte[8192]; // The buffer
						int iBWs = 0; // the amount of bytes read since last download rate calculation
						int nReads = 0;
						String bitrate = ""; // the download rate as string
						long timeStarted = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS); // current timestamp
						progressBarChanged(0, Pic.STATUS_T[Pic.DOWNLOADING]);

						/*
						 * Create a new timer, which sets every 10 seconds the recalculate-flag
						 * to true, so we know, we have to recalculate the download rate.
						 * I do this, because calculating the download rate over all bytes
						 * read since start of downloading, doesn't provide the information
						 * of wath the download rate is at this moment.
						 * And we should make the rate depending on the time, not on the bytes
						 * read.
						 */
						boolean downloadRate = SettingsManager.instance().isDownloadRate();
						// Ok, now start downloading
						while ((n = in.read(buf)) > 0) {
							/*
							 * Old:
							 * I commented this out, because when downloading from rapidshare
							 * and we stop downloading before the complete file is downloaded
							 * the close-Method (or was it releaseConnection?, don't remember)
							 * of the inputstream will block this thread for ever.
							 * This happend only on downloads from rapidshare. I didn't
							 * found out why.
							 * It would be not so nice, but maybe an idea to check if we are
							 * downloading from rapidshare, and when not we allow to stop
							 * otherwise not.
							 * 
							 * New:
							 * I found out that with method.abort() it works to close the connection.
							 * But internal the data is still downloaded on connection to some servers.
							 * But the data is not written to a file, but to /dev/nul
							 * I will now see if this is really a good solution...
							 */
							if (stop) {
								method.abort();
								stopped = true;
								break;
							}
							iBW += n;
							iBWs += n;
							out.write(buf, 0, n); // Write the bytes in the buffer to the outputstream
							out.flush();
							nReads++;
							if (nReads > 12) {
								nReads = 0;
								// change the progressbar
								progressBarChanged(iBW, getProgressString(iBW, size) + bitrate);
							}
							if (recalcutateRate && downloadRate) {
								// the flag is set to true, so we recalculate the download rate
								long now = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS); // get current timestamp
								// get the string for the rate
								downloadBitrate = UnitFormatTool.getBitrate(iBWs, size, timeStarted, now);
								bitrate = " " + UnitFormatTool.getBitrateString(downloadBitrate);

								/*
								 * With this, we get always the actual download rate, not
								 * just an average over all the time
								 */
								timeStarted = now; // set this variable to current timestamp
								iBWs = 0; // set bytes read since last calculation to 0
								recalcutateRate = false; // set the flag to false
							}
						}
						// Good, the file is downloaded, now flush the outputstream
						out.flush();
						recalcutateRate = false;
						EntityUtils.consume(response.getEntity());
					}

					// If we have to set last modification timestamp to the file, we do it
					if (this.fixedLastModified && this.lastModified > 0) {
						File fMod = new File(target);
						fMod.setLastModified(this.lastModified);
						fMod = null;
					}

					if (stop == false) {
						// If the user doesn't stopped the download

						boolean downloadFailed = ((this.size > 0) && (iBW != this.size)) || (iBW < SettingsManager.instance().getMinFilesize());
						if (downloadFailed) {
							this.failedCount++;
							checkDeactivate();
							progressBarChanged(0, Pic.STATUS_T[Pic.FAILED]);
							if ((this.size > 0) && (iBW != this.size)) {
								/*
								 * if we got a filesize, but the filesize does
								 * not equals the bytes we read, we didn't read
								 * the complete file, or more bytes than the file
								 * should have.
								 */
								setStatus(Pic.FAILED, Localization.getString("ErrorFilesizeNotMatchBytesRead"));
								logger.error("Download failed (Too many or to less bytes were downloaded): '" + this.containerURL + "'");
							} else if (iBW < SettingsManager.instance().getMinFilesize()) {
								/*
								 * The user can set in an options, which defines a minimum filesize.
								 * If the file here is to small
								 * Note: i check this here, and not above (where we get the filesize)
								 * because some servers don't give the filesize.
								 * So only here, we know the filesize really.
								 */
								this.failedCount = SettingsManager.instance().getMaxFailedCount(); // increase the failure-counter
								checkDeactivate(); // check if the download has to be deactivated
								setStatus(Pic.FAILED, Localization.getString("ErrorFilesizeToSmall"));
								logger.error("Download failed (Filesize is too small): '" + this.containerURL + "'");
							}
							// Now we have to delete the file
							File fileT = new File(target);
							if (fileT.exists()) {
								fileT.delete();
							}
							fileT = null;
						} else {
							/*
							 * If we get here, everything is fine
							 */
							this.setSize(iBW);
							progressBarChanged(size, Pic.STATUS_T[Pic.COMPLETE]);
							setStatus(Pic.COMPLETE);

							/*
							 * The user can set subdirs for specific filesize-ranges
							 * So maybe we have to move the file
							 */
							if (SettingsManager.instance().isSubdirsEnabled()) {
								boolean isImage = false;
								int imageWidth = 0;
								int imageHeight = 0;

								try {
									String strImages = ".*?(\\.(bmp|gif|jpe|jpg|jpeg|png|pcx|iff|ras|pbm|pgm|psd|tif|tiff))$";
									Pattern patternImage = Pattern.compile(strImages, Pattern.CASE_INSENSITIVE);
									Matcher matcherImage = patternImage.matcher(target);
									if (matcherImage.matches()) {
										try (FileInputStream imageinput = new FileInputStream(target)) {
											ImageInfo imageinfo = new ImageInfo();
											imageinfo.setInput(imageinput);
											if (!imageinfo.check()) {
												logger.debug("File ist not an image or the image format is not supported");
											} else {
												imageWidth = imageinfo.getWidth();
												imageHeight = imageinfo.getHeight();
												isImage = true;
											}
										}
									}
								} catch (Exception e) {
								}

								List<Subdir> v = SettingsManager.instance().getSubdirs();
								for (int i = 0; i < v.size(); i++) {
									Subdir sdir = v.get(i);

									if (sdir.isInRange(this.size, imageWidth, imageHeight, isImage)) {
										// If the filesize is in the size-range of this subdir, then we move to file
										File fMoved = Pic.moveFileToSubdir(this.targetFilename, this.targetPath, sdir.getSubdirName());
										if (fMoved == null) {
											// The move failed
											logger.error("File '" + this.targetPath + this.targetFilename + "' could not be moved");
										}
										break;
									}
								}
							}

						}
					} else {
						// If the user stopped the download
						progressBarChanged(0, Pic.STATUS_T[Pic.SLEEPING]);
						setStatus(Pic.SLEEPING);
						// Delete the file
						File fileT = new File(target);
						if (fileT.exists()) {
							fileT.delete();
						}
					}
				}
			} catch (MalformedURLException e) {
				this.failedCount++;
				checkDeactivate();
				setStatus(Pic.FAILED, e.getMessage());
				progressBarChanged(0, Pic.STATUS_T[Pic.FAILED]);
				logger.error("URL: {} (Stopped: {})", url, stopped, e);
			} catch (Exception e) {
				this.failedCount++;
				checkDeactivate();
				setStatus(Pic.FAILED, e.getMessage());
				progressBarChanged(0, Pic.STATUS_T[Pic.FAILED]);
				/*
				 * There are some Exceptions where we don't need to write the stacktrace also to the log,
				 * so we just write the message to the log
				 */
				if (e instanceof IOException) {
					logger.error("URL: {} (Stopped: {}) : {}", url, stopped, e.getMessage());
				} else {
					logger.error("URL: {} (Stopped: {})", url, stopped, e);
				}
				// Delete the file
				File fileT = new File(target);
				if (fileT.exists()) {
					fileT.delete();
				}
			} finally {
				if (method != null) {
					method.abort();
				}
				// Give the slot back to the Queue
				DownloadQueueManager.instance().removeDLSlotListener(this); // important!
			}
		}

	}

	/**
	 * Returns a String with the percent or size read
	 * 
	 * @param size Number of bytes read
	 * @param max Filesize
	 * @return Progress-String
	 */
	private static String getProgressString(long size, long max) {
		String retval = "";
		int progressView = SettingsManager.instance().getProgessView();
		if (max >= size) {
			if (progressView == SettingsManager.PROGRESSBAR_PERCENT) {
				retval = UnitFormatTool.getPercentString(size, max);
			} else if (progressView == SettingsManager.PROGRESSBAR_SIZE) {
				retval = UnitFormatTool.getSizeString(size, SettingsManager.instance().getSizeView());
			} else if (progressView == SettingsManager.NOPROGRESSBAR_PERCENT) {
				retval = UnitFormatTool.getPercentString(size, max);
			} else if (progressView == SettingsManager.NOPROGRESSBAR_SIZE) {
				retval = UnitFormatTool.getSizeString(size, SettingsManager.instance().getSizeView());
			}
		} else {
			retval = UnitFormatTool.getSizeString(size, SettingsManager.instance().getSizeView());
		}
		return retval;
	}

	/**
	 * This method fires the targetChanged-Method on all listeners
	 */
	private void targetChanged() {
		for (IPicListener listener : listeners) {
			listener.targetChanged(this);
		}
	}

	/**
	 * This method fires the progressBarChanged-Method on all listeners
	 * 
	 * @param val Value
	 * @param s Progressbar-Text
	 */
	private void progressBarChanged(long val, String s) {
		int progressView = SettingsManager.instance().getProgessView();
		if (progressView == SettingsManager.NOPROGRESSBAR_PERCENT || progressView == SettingsManager.NOPROGRESSBAR_SIZE) {
			// if the user don't want to see the visual progress, set the value to 0
			val = 0;
		}

		long min = 0; // Set the minimum of the progressbar
		long max = 100; // Set the maximum of the progressbar
		if (size > 0) {
			// if the filesize is known, we set the maximum to the amount of bytes
			max = size;
		} else {
			// if not, we set the maximum to the value
			max = val;
		}

		/*
		 * Because the progressbar accepts only integers, we need to cast the values
		 * First we set some values, which are used if a NumberFormatException occures
		 * because the filesize is bigger than an integer.
		 */
		int iMin = 0;
		int iMax = 2;
		int iVal = 1;
		try {
			iMin = (int)min;
			iMax = (int)max;
			iVal = (int)val;
		} catch (NumberFormatException nfe) {
			logger.error(nfe.getMessage(), nfe);
		}

		// Now let all listeners know, that that they have to update their progressbar
		for (IPicListener listener : listeners) {
			listener.progressBarChanged(this, iMin, iMax, iVal, s, errMsg);
		}
	}

	/**
	 * Adds a listener
	 * 
	 * @param listener Listener
	 */
	public void addPicListener(IPicListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes a listener
	 * 
	 * @param listener Listener
	 */
	public void removePicListener(IPicListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	/**
	 * Removes all listeners
	 */
	public void removeAllListener() {
		listeners.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.queue.IDownloadListener#downloadAllowed()
	 */
	@Override
	public boolean downloadAllowed() {
		/*
		 * When the startDownload-Method is called, the download requests the
		 * Queue for a download-slot. The Queue fires this method, as soon as there is
		 * free download-slot.
		 * This method returns true if the download is really started.
		 * If the status is something else than Pic.WAITING here, then
		 * we don't start the download! So we return false!
		 */

		if (this.status == Pic.WAITING) {
			// Set the status to DOWNLOADING
			setStatus(Pic.DOWNLOADING);

			// Now we make this Pic to a thread and start it
			Thread t = new Thread(this);
			t.setName("Download-Thread-" + t.getId() + "(" + this.containerURL + ")");
			// We set the priority to a minimum, to reduce CPU-Usage
			t.setPriority(Thread.MIN_PRIORITY);
			t.start(); // Now we start the thread
			return true;
		}
		return false;
	}

	/**
	 * Checks if the download failed to often (defined by the user over the options)
	 * If so the download will be deactivated
	 */
	private void checkDeactivate() {
		if (SettingsManager.instance().getMaxFailedCount() <= 0) {
			return;
		}
		if (this.failedCount >= SettingsManager.instance().getMaxFailedCount()) {
			setDeactivated(true);
		}
	}

	/**
	 * Creates a directory
	 * 
	 * If there is already a file of the same name, a number
	 * is added at the end of the foldername.
	 * This method returns a File-Object which contains path.
	 * If the directory could not be created null is returned
	 * 
	 * @param dir Directory
	 * @param targetPath Path
	 * @return Directory
	 */
	public static synchronized String createDirectory(File dir, String targetPath) {
		if (dir.exists()) {
			// if there is a file or directory of the same name
			if (dir.isDirectory()) {
				// if that is a directory, we are done
				return targetPath;
			} else {
				// if not
				boolean bf = true;
				int i = 1;
				/*
				 * Loop while bf is true and the index is lower than 10'000.
				 * I set this limit to avoid endless loop or Numberformat exception.
				 */
				while (bf && i < 10000) {
					String newDir = targetPath;
					// Cut the slash or backslash
					if (newDir.endsWith("/") || newDir.endsWith("\\")) {
						newDir = newDir.substring(0, newDir.length() - 1);
					}
					// add a number (the index)
					newDir += i + FileTool.FILE_SEPERATOR;

					File td = new File(newDir);
					if (!td.exists()) {
						// if there is no file or directory with the same name

						/*
						 * We get true if the directory was created
						 * The mkdirs-Method creates recursivly all directories
						 */
						boolean b = td.mkdirs();
						bf = false;
						if (b) {
							// if directory was created return it
							return newDir;
						}
						// if not return null
						return null;
					} else {
						// if there is already a file or directory of the same name
						if (td.isDirectory()) {
							// if it is a directory, we are done
							return newDir;
						}
						// otherwise we loop again
					}
					i++;
				}
			}
		} else {
			// if there is no file or directory of the same name
			boolean b = false;
			// we create the directory
			b = dir.mkdirs();
			if (b) {
				// if the directory was created return it
				return targetPath;
			}
		}
		// if the directory was not created return null
		return null;
	}

	/**
	 * Creates a new file
	 * We have always to use this method because, so we can be sure
	 * not 2 download-thread are writing to the same file.
	 * 
	 * If there is already a file or directory of the same name, a number
	 * is added at the end of the filename.
	 * This method returns a File-Object which contains path and filename.
	 * If the file could not be created null is returned
	 * 
	 * @param file File
	 * @param targetFilename Filename
	 * @param targetPath Path
	 * @return File
	 * @throws IOException
	 */
	private static synchronized File createFile(File file, String targetFilename, String targetPath) throws IOException {
		if (file.exists()) {
			// If there is already a file or directory of the same name

			// Get position of the last . in the filename
			int iext = targetFilename.lastIndexOf(".");
			String ext = "";
			String filenameWithoutExt = "";
			// Get filename whithout the extension and the extension
			if (iext > 0) {
				ext = targetFilename.substring(iext);
				filenameWithoutExt = targetFilename.substring(0, iext);
			} else {
				filenameWithoutExt = targetFilename;
			}

			boolean bf = true;
			int i = 1;
			/*
			 * Loop while bf is true and the index is lower than 10'000.
			 * I set this limit to avoid endless loop or Numberformat exception.
			 */
			while (bf && i < 10000) {
				// add a number (the index)
				String newFile = filenameWithoutExt + "-" + i + ext;
				File tf = new File(targetPath + newFile);
				if (!tf.exists()) {
					// Ok there is no file with this name, so create it!
					tf.createNewFile();
					bf = false;
					return tf; // and return it
				}
				// Oh, there is also a file with this name, so we loop again
				i++;
			}
		} else {
			// Good, there is no file with this name, so we can create it!
			file.createNewFile();
			return file; // and return it
		}
		// We can't create a file, so we return null
		return null;
	}

	/**
	 * BilderHerunterlader provides options, where the user can set, that files
	 * of a specific size-range should be moved to a specific subdirectory.
	 * So this method moves the file.
	 * The method will check if there is already a file of same name existing
	 * and if so, a number is added to the filename.
	 * This is done by call the createFile-Method.
	 * 
	 * The method returns a File-Object which contains path and filename, of
	 * where the file was moved.
	 * If the file could not be moved null is returned.
	 * 
	 * @see ch.supertomcat.bh.pic.Pic#createDirectory(File, String)
	 * @param filename Filename
	 * @param path Path
	 * @param subdirName Name of the subdir
	 * @return File
	 */
	public static synchronized File moveFileToSubdir(String filename, String path, String subdirName) {
		// Get the complete path of the folder in which we want to move the file
		String mPath = path + subdirName + FileTool.FILE_SEPERATOR;
		File fPath = new File(mPath);

		/*
		 * Create the directory if it is not existing
		 * Note: if there is already a file with the same name as the directory
		 * then a number is added at the end of the subdirname.
		 * The createDirectory returns the path of the folder created
		 */
		String pathResult = Pic.createDirectory(fPath, mPath);
		if (pathResult == null) {
			/*
			 * If we get back null, then the directory could not be created, so
			 * we can't move the file. So we return null.
			 */
			return null;
		} else {
			mPath = pathResult;
		}
		File fMoved = new File(mPath + filename);
		try {
			/*
			 * Create a new file. Here it is the same procedure as above
			 * with the directory.
			 * We need to create a new file, because of the multithreaded
			 * downloading an other download could download to the same file
			 * or move a file to the same file, as we want to do here!
			 */
			File fMovedResult = Pic.createFile(fMoved, filename, mPath);
			if (fMovedResult != null) {
				File fOriginal = new File(path + filename);

				/*
				 * Ok, now we need to delete the file, if we
				 * do not, the file can't be moved.
				 * And now we hope, java is fast enough, so that
				 * not in this time, another download-thread writes to
				 * the file ;-)
				 */
				fMovedResult.delete();
				// We get here true back only if the rename was done!
				boolean b = fOriginal.renameTo(fMovedResult);
				if (b == false) {
					// The file could not be renamed, so return null
					return null;
				}
				// File was renamed and everything is ok :-)
				return fMovedResult;
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		// The file could not be renamed, so return null
		return null;
	}

	/**
	 * This method moves a file.
	 * The method will check if there is already a file of same name existing
	 * and if so, a number is added to the filename.
	 * This is done by call the createFile-Method.
	 * 
	 * The method returns a File-Object which contains path and filename, of
	 * where the file was moved.
	 * If the file could not be moved null is returned.
	 * 
	 * @see ch.supertomcat.bh.pic.Pic#createDirectory(File, String)
	 * @param fileToMove Filename
	 * @param newPath Path
	 * @return File
	 */
	public static synchronized File moveFile(File fileToMove, File newPath) {
		if (FileTool.getDirectory(fileToMove.getAbsolutePath()).equals(newPath.getAbsolutePath())) {
			return fileToMove;
		}
		// Get the complete path of the folder in which we want to move the file
		String mPath = newPath.getAbsolutePath();

		/*
		 * Create the directory if it is not existing
		 * Note: if there is already a file with the same name as the directory
		 * then a number is added at the end of the subdirname.
		 * The createDirectory returns the path of the folder created
		 */
		String pathResult = Pic.createDirectory(newPath, mPath);
		if (pathResult == null) {
			/*
			 * If we get back null, then the directory could not be created, so
			 * we can't move the file. So we return null.
			 */
			return null;
		} else {
			mPath = pathResult;
		}
		String filename = fileToMove.getName();
		File fMoved = new File(mPath + FileTool.FILE_SEPERATOR + filename);
		try {
			/*
			 * Create a new file. Here it is the same procedure as above
			 * with the directory.
			 * We need to create a new file, because of the multithreaded
			 * downloading an other download could download to the same file
			 * or move a file to the same file, as we want to do here!
			 */
			File fMovedResult = Pic.createFile(fMoved, filename, mPath);
			if (fMovedResult != null) {
				/*
				 * Ok, now we need to delete the file, if we
				 * do not, the file can't be moved.
				 * And now we hope, java is fast enough, so that
				 * not in this time, another download-thread writes to
				 * the file ;-)
				 */
				fMovedResult.delete();
				// We get here true back only if the rename was done!
				boolean b = fileToMove.renameTo(fMovedResult);
				if (b == false) {
					// The file could not be renamed, so return null
					return null;
				}
				// File was renamed and everything is ok :-)
				return fMovedResult;
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		// The file could not be renamed, so return null
		return null;
	}

	/**
	 * This method compiles the pattern for the filename in content-disposition
	 * 
	 * @return Pattern
	 */
	public static Pattern compileContentDispositionFilenamePattern() {
		StringBuilder encodings = new StringBuilder("");

		SortedMap<String, Charset> charsets = Charset.availableCharsets();
		Iterator<String> ite = charsets.keySet().iterator();
		Charset charset = null;
		encodings.append("(");
		while (ite.hasNext()) {
			charset = charsets.get(ite.next());
			// encodings.append(charset.name());
			encodings.append(charset.displayName());
			if (ite.hasNext()) {
				encodings.append("|");
			}
		}
		encodings.append(")?");

		return Pattern.compile(".*?filename[*]?=" + encodings.toString() + "('')?[\"]*([^\";]+)[\";]*.*", Pattern.CASE_INSENSITIVE);
	}

	/**
	 * Returns the Container-URL
	 * 
	 * @return Container-URL
	 */
	@Override
	public String getContainerURL() {
		return this.containerURL;
	}

	/**
	 * Returns path and filename
	 * Note: There is no set method for this, because path
	 * and filename are hold seperated in the class.
	 * 
	 * @return Path and filename
	 */
	public String getTarget() {
		return this.targetPath + this.targetFilename;
	}

	/**
	 * Returns the filesize
	 * 
	 * @return Filesize
	 */
	public long getSize() {
		return this.size;
	}

	/**
	 * Sets the filesize
	 * This method fires sizeChanged on all listeners and updates
	 * the object in the database, but does not commit the database!
	 * 
	 * @param size Filesize
	 */
	private void setSize(long size) {
		if (size >= 0) {
			this.size = size;
			for (IPicListener listener : listeners) {
				listener.sizeChanged(this);
			}
			QueueManager.instance().updatePic(this);
		}
	}

	/**
	 * Returns the status-text for the actual status of the download
	 * 
	 * @return Statustext
	 */
	public String getStatusText() {
		return Pic.STATUS_T[this.status];
	}

	/**
	 * Returns the status
	 * Available states:
	 * Pic.SLEEPING
	 * Pic.WAITING
	 * Pic.DOWNLOADING
	 * Pic.COMPLETE
	 * Pic.FAILED
	 * Pic.FAILED_FILE_NOT_EXIST
	 * 
	 * @return Status
	 */
	public int getStatus() {
		return this.status;
	}

	/**
	 * Attention: You should not use the method from other classes.
	 * It could produce errors if set while download is running are
	 * so on.
	 * 
	 * I used this method only from other classes when reading them
	 * from the database, because if the program was crashed the
	 * object could have DOWNLOADING as status and that is terrible.
	 * 
	 * Sets the status of the download
	 * Available states:
	 * Pic.SLEEPING
	 * Pic.WAITING
	 * Pic.DOWNLOADING
	 * Pic.COMPLETE
	 * Pic.FAILED
	 * Pic.FAILED_FILE_NOT_EXIST
	 * If the status-argument is not one of those, the
	 * method will not set anything!
	 * 
	 * This method fires the statusChanged-Method on all listeners.
	 * 
	 * @param status Status
	 */
	public void setStatus(int status) {
		setStatus(status, "");
	}

	/**
	 * Sets the status of the download
	 * Available states:
	 * Pic.SLEEPING
	 * Pic.WAITING
	 * Pic.DOWNLOADING
	 * Pic.COMPLETE
	 * Pic.FAILED
	 * Pic.FAILED_FILE_NOT_EXIST
	 * If the status-argument is not one of those, the
	 * method will not set anything!
	 * 
	 * This method fires the statusChanged-Method on all listeners and
	 * updates the object in the database, but does not commit the database!
	 * 
	 * @param status Status
	 * @param errMsg Error-Message
	 */
	private void setStatus(int status, String errMsg) {
		if ((status != Pic.SLEEPING) && (status != Pic.WAITING) && (status != Pic.DOWNLOADING) && (status != Pic.COMPLETE) && (status != Pic.FAILED) && (status != Pic.FAILED_FILE_NOT_EXIST)
				&& (status != Pic.ABORTING) && (status != Pic.FAILED_FILE_TEMPORARY_OFFLINE)) {
			return;
		}
		this.status = status;
		this.errMsg = errMsg;
		if (status == Pic.COMPLETE) {
			this.dateTime = System.currentTimeMillis();
		}
		for (IPicListener listener : listeners) {
			listener.statusChanged(this);
		}
		if ((status != Pic.SLEEPING) && (status != Pic.WAITING) && (status != Pic.DOWNLOADING) && (status != Pic.ABORTING)) {
			/*
			 * We can gain some speed by only updating the status in the database in some cases.
			 * When pics are loaded, they reset to SLEEPING if the status is one of the three in the condition above.
			 * So we don't need to save the status in those cases.
			 */
			QueueManager.instance().updatePic(this);
		}
	}

	/**
	 * Returns the timestamp of last modification of the file
	 * 
	 * @return Last modification timestamp
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * Set the timestamp of last modification of the file
	 * 
	 * @param lastModified Last modification timestamp
	 */
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * Returns if lastModified is fixed or not
	 * 
	 * @return Fixed lastModified
	 */
	public boolean isFixedLastModified() {
		return fixedLastModified;
	}

	/**
	 * Sets the last modified timestamp to be fixed.
	 * Only if this is set to true, the download-class will
	 * set the last modified timestamp on the file.
	 * 
	 * @param fixedLastModified Fixed lastModified
	 */
	public void setFixedLastModified(boolean fixedLastModified) {
		this.fixedLastModified = fixedLastModified;
	}

	/**
	 * Returns the path
	 * 
	 * @return Path
	 */
	public String getTargetPath() {
		return targetPath;
	}

	/**
	 * Sets the Path
	 * 
	 * @param targetPath Path
	 */
	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
		if ((this.targetPath.endsWith("/") == false) && (this.targetPath.endsWith("\\") == false)) {
			this.targetPath += FileTool.FILE_SEPERATOR;
		}
		targetChanged();
	}

	/**
	 * Returns the filename
	 * 
	 * @return Filename
	 */
	public String getTargetFilename() {
		return targetFilename;
	}

	/**
	 * Sets the filename
	 * If the filename was set to be fixed before and the argument for
	 * targetFilename is here an empty string, the filename is set to be
	 * not fixed anymore.
	 * This methods fires the targetChanged-Method on all listeners
	 * 
	 * @param targetFilename Filename
	 */
	public void setTargetFilename(String targetFilename) {
		if (targetFilename.length() > 0) {
			this.targetFilename = targetFilename;
			this.fixedTargetFilename = true;
		} else {
			this.targetFilename = Localization.getString("Unkown");
			this.fixedTargetFilename = false;
		}
		targetChanged();
	}

	/**
	 * Returns the Thumbnail-URL
	 * 
	 * @return Thumbnail-URL
	 */
	public String getThumb() {
		return thumb;
	}

	/**
	 * Set the Thumbnail-URL
	 * 
	 * @param thumb Thumbnail-URL
	 */
	public void setThumb(String thumb) {
		thumb = HTTPTool.trimURL(thumb);
		this.thumb = thumb;
	}

	/**
	 * Returns if the filename is fixed or not
	 * 
	 * @see ch.supertomcat.bh.pic.Pic#setFixedTargetFilename(boolean)
	 * @return the fixedTargetFilename
	 */
	public boolean isFixedTargetFilename() {
		return fixedTargetFilename;
	}

	/**
	 * Sets the filename to be fixed. So the download or the hostclasses
	 * are not overwriting it!
	 * Note: When adding URLs to BilderHerunterlader, then the filenames
	 * are most only grabbed from the URL. But maybe hostclasses has methods
	 * to restore the original filename or get a better one. If not, when the
	 * direct link is known by the download, the download-class could, grab a
	 * nicer filename, as the ones in the Download-Selection-Dialog.
	 * So a filename should only set to fixed, when a user changes the filename
	 * in the GUI.
	 * 
	 * @param fixedTargetFilename Fixed filename
	 */
	public void setFixedTargetFilename(boolean fixedTargetFilename) {
		this.fixedTargetFilename = fixedTargetFilename;
	}

	/**
	 * Returns the ThreadURL (Referrer)
	 * 
	 * @return ThreadURL (Referrer)
	 */
	public String getThreadURL() {
		return threadURL;
	}

	/**
	 * Set the threadURL (Referrer)
	 * 
	 * @param threadURL ThreadURL (Referrer)
	 */
	public void setThreadURL(String threadURL) {
		this.threadURL = HTTPTool.trimURL(threadURL);
	}

	/**
	 * Returns the count of failures
	 * 
	 * @return Count of failures
	 */
	public int getFailedCount() {
		return failedCount;
	}

	/**
	 * Returns if the download is deactivated or not
	 * 
	 * @return Deactivated
	 */
	public boolean isDeactivated() {
		return deactivated;
	}

	/**
	 * Sets the download to be deactivated or not
	 * This method fires deactivatedChange-Method on all listeners and
	 * updates the object in the database. But does not commit the database!
	 * 
	 * @param deactivated Deactivated
	 */
	public void setDeactivated(boolean deactivated) {
		this.deactivated = deactivated;
		this.failedCount = 0;
		for (IPicListener listener : listeners) {
			listener.deactivatedChanged(this);
		}
		QueueManager.instance().updatePic(this);
	}

	/**
	 * Returns the Error-Message
	 * 
	 * @return Error-Message
	 */
	public String getErrMsg() {
		return errMsg;
	}

	/**
	 * Returns the renameWithContentDisposition
	 * 
	 * @return renameWithContentDisposition
	 */
	public boolean isRenameWithContentDisposition() {
		return renameWithContentDisposition;
	}

	/**
	 * Sets the renameWithContentDisposition
	 * 
	 * @param renameWithContentDisposition renameWithContentDisposition
	 */
	public void setRenameWithContentDisposition(boolean renameWithContentDisposition) {
		this.renameWithContentDisposition = renameWithContentDisposition;
	}

	/**
	 * Get-Method
	 * 
	 * @return Formatted date and time
	 */
	public String getDateTime() {
		String retval = "";
		if (dateTime < 0) {
			return retval;
		}
		DateFormat df = new SimpleDateFormat();
		retval = df.format(new Date(dateTime));
		return retval;
	}

	/**
	 * Get-Method
	 * 
	 * @return Date and time
	 */
	public long getDateTimeSimple() {
		return dateTime;
	}

	/**
	 * Returns the downloadURL
	 * 
	 * @return downloadURL
	 */
	public String getDownloadURL() {
		return downloadURL;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.queue.IDownloadListener#recalcutateRate()
	 */
	@Override
	public void recalcutateRate() {
		/*
		 * Set the recalculate flag to true
		 */
		this.recalcutateRate = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.queue.IDownloadListener#getDownloadRate()
	 */
	@Override
	public double getDownloadRate() {
		return downloadBitrate;
	}

	/**
	 * Initializes transient listeners member if needed
	 */
	public void initializeListenerList() {
		if (listeners == null) {
			listeners = new CopyOnWriteArrayList<>();
		}
	}
}

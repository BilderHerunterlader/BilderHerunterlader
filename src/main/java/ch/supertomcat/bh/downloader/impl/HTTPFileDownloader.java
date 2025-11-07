package ch.supertomcat.bh.downloader.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;

import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.StatusLine;

import ch.supertomcat.bh.cookies.CookieManager;
import ch.supertomcat.bh.downloader.FileDownloaderBase;
import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.hoster.Hoster;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.hoster.parser.URLParseObjectFile;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.PicProgress;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.options.Subdir;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.formatter.UnitFormatUtil;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;
import ch.supertomcat.supertomcatutils.image.ImageInfo;
import ch.supertomcat.supertomcatutils.io.FileUtil;
import ch.supertomcat.supertomcatutils.regex.RegexReplacePipeline;

/**
 * FileDownloader for HTTP URLs
 */
public class HTTPFileDownloader extends FileDownloaderBase {
	/**
	 * Proxy Manager
	 */
	private final ProxyManager proxyManager;

	/**
	 * Cookie Manager
	 */
	private final CookieManager cookieManager;

	/**
	 * Constructor
	 * 
	 * @param downloadQueueManager Download Queue Manager
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param cookieManager Cookie Manager
	 * @param hostManager Host Manager
	 */
	public HTTPFileDownloader(DownloadQueueManager downloadQueueManager, ProxyManager proxyManager, SettingsManager settingsManager, CookieManager cookieManager, HostManager hostManager) {
		super(downloadQueueManager, settingsManager, hostManager);
		this.proxyManager = proxyManager;
		this.cookieManager = cookieManager;
	}

	@Override
	public void downloadFile(Pic pic) throws HostException {
		CookieStore cookieStore = cookieManager.getCookieStore();
		HttpClientContext context = ContextBuilder.create().useCookieStore(cookieStore).build();

		URLParseObject upo = createURLParseObject(pic);
		upo.addInfo(URLParseObject.DOWNLOADER_HTTP_COOKIE_STORE, cookieStore);
		upo.addInfo(URLParseObject.DOWNLOADER_HTTP_CONTEXT, context);

		URLParseObject result = parseURL(pic, upo);

		String referrer = result.getContainerURL();
		Hoster lastHoster = result.getLastHoster();
		if (lastHoster instanceof Rule lastRule) {
			switch (lastRule.getDefinition().getDownloadReferrerMode()) {
				case NO_REFERRER:
					referrer = "";
					break;
				case LAST_CONTAINER_URL:
					referrer = result.getContainerURL();
					break;
				case FIRST_CONTAINER_URL:
					referrer = result.getFirstContainerURL();
					break;
				case ORIGIN_PAGE:
					referrer = pic.getThreadURL();
					break;
				case CUSTOM:
					referrer = lastRule.getDefinition().getDownloadCustomReferrer();
					break;
			}
		}

		boolean bReduceFilenameLength = true;
		if (result.checkExistInfo(URLParseObject.REDUCE_FILENAME_LENGTH, Boolean.class)) {
			bReduceFilenameLength = (Boolean)result.getInfo(URLParseObject.REDUCE_FILENAME_LENGTH);
		}

		// And replace the %20 in the filename, if there are any
		String targetFilename = BHUtil.filterPath(pic.getTargetFilename().replace("%20", " "), settingsManager);
		if (bReduceFilenameLength) {
			targetFilename = BHUtil.reduceFilenameLength(targetFilename, settingsManager);
		}
		pic.setTargetFilename(targetFilename);

		boolean fixedTargetFilename = pic.isFixedTargetFilename();

		List<URLParseObjectFile> allDirectLinks = result.getAllDirectLinks();
		boolean firstURL = true;
		int count = 1;
		int directLinksSize = allDirectLinks.size();
		for (URLParseObjectFile directLink : allDirectLinks) {
			String currentTargetFilename = targetFilename;

			// if the hostclass gives us a nice filename
			String currentCorrectedFilename = directLink.getCorrectedFilename();
			if (currentCorrectedFilename != null && !currentCorrectedFilename.isEmpty() && (!fixedTargetFilename || targetFilename.isEmpty())) {
				currentTargetFilename = BHUtil.filterFilename(currentCorrectedFilename, settingsManager);
				// And replace the %20 in the filename, if there are any
				currentTargetFilename = BHUtil.filterPath(currentTargetFilename.replace("%20", " "), settingsManager);
				if (bReduceFilenameLength) {
					currentTargetFilename = BHUtil.reduceFilenameLength(currentTargetFilename, settingsManager);
				}
			}

			boolean lastURL = count == directLinksSize;
			if (!executeFileDownload(pic, directLink.getDirectLink(), currentTargetFilename, result, referrer, firstURL, lastURL, count, directLinksSize, context, cookieStore)) {
				break;
			}
			firstURL = false;
			count++;
		}
	}

	/**
	 * Execute file download
	 * 
	 * @param pic Pic
	 * @param url URL
	 * @param correctedFilename Corrected Filename
	 * @param result Result
	 * @param referrer Referrer
	 * @param firstURL First URL
	 * @param lastURL Last URL
	 * @param currentURL Current URL Index
	 * @param urlCount Count of URLs
	 * @param context Context
	 * @param cookieStore Cookie Store
	 * @return True if download was successful, false otherwise
	 */
	private boolean executeFileDownload(Pic pic, String url, String correctedFilename, URLParseObject result, String referrer, boolean firstURL, boolean lastURL, int currentURL, int urlCount,
			HttpClientContext context, CookieStore cookieStore) {
		logger.info("Downloading File: {}", url);
		TargetContainer targetContainer = new TargetContainer(pic, firstURL, correctedFilename);

		AtomicBoolean abortedFlag = new AtomicBoolean(false);
		try (CloseableHttpClient client = proxyManager.getHTTPClient()) {
			String encodedURL = HTTPUtil.encodeURL(url);

			HttpUriRequest method;
			// Create a new GetMethod or PostMethod and set timeouts, cookies, user-agent and so on
			if (result.checkExistInfo(URLParseObject.USE_METHOD, String.class) && "POST".equals(result.getInfo(URLParseObject.USE_METHOD))) {
				method = new HttpPost(encodedURL);
			} else {
				method = new HttpGet(encodedURL);
			}
			String userAgent = settingsManager.getUserAgent();
			if (result.checkExistInfo(URLParseObject.USE_USER_AGENT, String.class)) {
				userAgent = (String)result.getInfo(URLParseObject.USE_USER_AGENT);
			}
			method.setHeader(HttpHeaders.USER_AGENT, userAgent);
			if (result.checkExistInfo(URLParseObject.USE_REFERRER, String.class)) {
				referrer = (String)result.getInfo(URLParseObject.USE_REFERRER);
			}
			if (!referrer.isEmpty()) {
				method.setHeader(HttpHeaders.REFERER, referrer);
			}
			boolean sendCookies = true;
			if (result.checkExistInfo(URLParseObject.SEND_COOKIES, Boolean.class)) {
				sendCookies = (Boolean)result.getInfo(URLParseObject.SEND_COOKIES);
			}
			boolean specificCookies = false;
			if (result.checkExistInfo(URLParseObject.USE_COOKIES, String.class)) {
				String cookiesToUse = (String)result.getInfo(URLParseObject.USE_COOKIES);
				method.setHeader(HttpHeaders.COOKIE, cookiesToUse);
				specificCookies = true;
			}
			if (sendCookies && !specificCookies) {
				// Fill cookies to cookie store
				cookieManager.fillCookies(url, cookieStore);
			}

			return client.execute(method, context, response -> handleResponse(method, abortedFlag, response, targetContainer, pic, result, lastURL, currentURL, urlCount));
		} catch (MalformedURLException e) {
			failDownload(pic, result, false, e);
			return false;
		} catch (ConnectionClosedException e) {
			if (abortedFlag.get()) {
				/*
				 * Ignore exception, because download was aborted
				 */
				return false;
			} else {
				failDownload(pic, result, false, e);
				// Delete the file
				deleteFile(targetContainer);
				return false;
			}
		} catch (Exception e) {
			failDownload(pic, result, false, e);
			// Delete the file
			deleteFile(targetContainer);
			return false;
		}
	}

	/**
	 * Handle Response
	 * 
	 * @param method HTTP Method
	 * @param abortedFlag Aborted Flag
	 * @param response Response
	 * @param targetContainer Target Container
	 * @param pic Pic
	 * @param result Result
	 * @param lastURL Last URL
	 * @param currentURL Current URL Index
	 * @param urlCount Count of URLs
	 * @return True if download was successful, false otherwise
	 * @throws IOException
	 */
	private boolean handleResponse(HttpUriRequest method, AtomicBoolean abortedFlag, ClassicHttpResponse response, TargetContainer targetContainer, Pic pic, URLParseObject result, boolean lastURL,
			int currentURL, int urlCount) throws IOException {
		StatusLine statusLine = new StatusLine(response);
		int statusCode = statusLine.getStatusCode();

		if (statusCode < 200 || statusCode >= 300) {
			failDownload(pic, result, false, "HTTP-Error: " + statusLine);
			return false;
		}

		if (pic.isRenameWithContentDisposition() && !pic.isFixedTargetFilename()) {
			determineFilenameByContentDisposition(response, targetContainer);
		}

		// This uses the replacements defined in BH settings
		determineFilenameByRegexReplacePipeline(targetContainer);

		/*
		 * Now we create a new file.
		 * The createFile-Method gives us a File back
		 * because there could already be a file of the same
		 * name, so get back a new filename or another pathname.
		 */
		Path fRetval;
		try {
			fRetval = createFile(Paths.get(targetContainer.getTarget()));
			if (fRetval == null) {
				// If the file coult not be created
				failDownload(pic, result, false, Localization.getString("ErrorFileCouldNotBeCreated"));
				abortedFlag.set(true);
				method.abort();
				return false;
			}
		} catch (IOException e) {
			// If the file coult not be created
			failDownload(pic, result, false, Localization.getString("ErrorFileCouldNotBeCreated"));
			abortedFlag.set(true);
			method.abort();
			return false;
		}

		/*
		 * Create file might change the filename, if the file already exsists, so we have to set the filename of the actually created file to target container
		 */
		targetContainer.setCorrectedFilename(fRetval.getFileName().toString());

		/*
		 * Get the filesize
		 * Note: Some servers doesn't give us the filesize, so
		 * the filesize is maybe 0
		 */
		long size;
		if (method instanceof HttpGet || method instanceof HttpPost) {
			@SuppressWarnings("resource")
			long contentLength = response.getEntity().getContentLength();
			size = contentLength >= 0 ? contentLength : 0;
		} else {
			size = 0;
		}
		pic.setSize(size);

		if (size > 0 && size < settingsManager.getDownloadsSettings().getMinFileSize()) {
			/*
			 * The user can set in an options, which defines a minimum filesize.
			 * If the file here is to small
			 * Note: i check this here, and not above (where we get the filesize)
			 * because some servers don't give the filesize.
			 * So only here, we know the filesize really.
			 */
			failDownload(pic, result, true, Localization.getString("ErrorFilesizeToSmall"));
			logger.error("Download failed (Filesize is too small): '{}'", pic.getContainerURL());
			// Now we have to delete the file
			deleteFile(targetContainer);
			abortedFlag.set(true);
			method.abort();
			return false;
		}

		PicProgress picProgress = pic.getProgress();
		picProgress.setBytesDownloaded(0);
		picProgress.setBytesTotal(size);
		pic.progressUpdated();

		long totalBytesRead = 0; // the amount of bytes we read since started downloading

		// Now get the inputstream and outputstream
		try (@SuppressWarnings("resource")
		InputStream in = response.getEntity().getContent(); ReadableByteChannel inChannel = Channels.newChannel(in)) {
			/*
			 * We need a separate try block for the output file, because if a download is aborted, we need to delete the file, but this is only possible after
			 * the OutputStream is closed. And we have to delete the file and set status and so on, before the entity content is closed.
			 */
			try (FileChannel out = FileChannel.open(Paths.get(targetContainer.getTarget()), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
				// Read some bytes to a buffer and write them to the outputstream
				long bytesRead; // the amount of bytes where were read per loop course

				int maxBytesUntilProgressUpdate = 100 * 1024;
				int maxReadCountUntilProgressUpdate;
				if (maxBytesUntilProgressUpdate >= ProxyManager.BUFFER_SIZE) {
					maxReadCountUntilProgressUpdate = Math.floorDiv(maxBytesUntilProgressUpdate, ProxyManager.BUFFER_SIZE);
					if (maxReadCountUntilProgressUpdate < 1) {
						maxReadCountUntilProgressUpdate = 1;
					}
				} else {
					maxReadCountUntilProgressUpdate = 1;
				}

				int readCountSinceLastProgressUpdate = 0;
				int bytesReadSinceLastDownloadRateCalculation = 0; // the amount of bytes read since last download rate calculation

				long timeStarted = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS); // current timestamp
				picProgress.setBytesDownloaded(0);
				pic.progressUpdated();

				/*
				 * Create a new timer, which sets every 10 seconds the recalculate-flag
				 * to true, so we know, we have to recalculate the download rate.
				 * I do this, because calculating the download rate over all bytes
				 * read since start of downloading, doesn't provide the information
				 * of wath the download rate is at this moment.
				 * And we should make the rate depending on the time, not on the bytes
				 * read.
				 */
				boolean downloadRate = settingsManager.getGUISettings().isDownloadRate();
				// Ok, now start downloading
				while ((bytesRead = out.transferFrom(inChannel, totalBytesRead, ProxyManager.BUFFER_SIZE)) > 0) {
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
					if (pic.isStop()) {
						logger.info("Aborting download: exit download loop");
						break;
					}
					totalBytesRead += bytesRead;
					bytesReadSinceLastDownloadRateCalculation += bytesRead;
					readCountSinceLastProgressUpdate++;
					if (readCountSinceLastProgressUpdate > maxReadCountUntilProgressUpdate) {
						readCountSinceLastProgressUpdate = 0;
						// change the progressbar
						picProgress.setBytesDownloaded(totalBytesRead);
						picProgress.setUrlCount(urlCount);
						picProgress.setCurrentURLIndex(currentURL);
						pic.progressUpdated();
					}
					if (pic.isRecalcutateRate() && downloadRate) {
						// the flag is set to true, so we recalculate the download rate
						long now = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS); // get current timestamp
						// get the string for the rate
						double downloadBitrate = UnitFormatUtil.getBitrate(bytesReadSinceLastDownloadRateCalculation, size, timeStarted, now);
						picProgress.setRate(downloadBitrate);

						/*
						 * With this, we get always the actual download rate, not
						 * just an average over all the time
						 */
						timeStarted = now; // set this variable to current timestamp
						bytesReadSinceLastDownloadRateCalculation = 0; // set bytes read since last calculation to 0
						pic.rateRecalculated();
					}
				}
				pic.rateRecalculated();
			}

			if (pic.isStop()) {
				logger.info("Aborting download: Set status and delete file");
				// If the user stopped the download
				stopDownload(pic);
				// Delete the file
				deleteFile(targetContainer);
				method.abort();
				abortedFlag.set(true);
				return false;
			}
		}

		// If we have to set last modification timestamp to the file, we do it
		if (pic.isFixedLastModified() && pic.getLastModified() > 0) {
			setLastModifiedTime(targetContainer, pic);
		}

		if (pic.isStop()) {
			logger.info("Aborting download: Set status and delete file");
			// If the user stopped the download
			stopDownload(pic);
			// Delete the file
			deleteFile(targetContainer);
			return false;
		}

		// If the user doesn't stopped the download
		boolean downloadFailed = ((size > 0) && (totalBytesRead != size)) || (totalBytesRead < settingsManager.getDownloadsSettings().getMinFileSize());
		if (downloadFailed) {
			if ((size > 0) && (totalBytesRead != size)) {
				/*
				 * if we got a filesize, but the filesize does
				 * not equals the bytes we read, we didn't read
				 * the complete file, or more bytes than the file
				 * should have.
				 */
				failDownload(pic, result, false, Localization.getString("ErrorFilesizeNotMatchBytesRead"));
				logger.error("Download failed (Too many or to less bytes were downloaded): '{}'", pic.getContainerURL());
			} else if (totalBytesRead < settingsManager.getDownloadsSettings().getMinFileSize()) {
				/*
				 * The user can set in an options, which defines a minimum filesize.
				 * If the file here is to small
				 * Note: i check this here, and not above (where we get the filesize)
				 * because some servers don't give the filesize.
				 * So only here, we know the filesize really.
				 */
				failDownload(pic, result, true, Localization.getString("ErrorFilesizeToSmall"));
				logger.error("Download failed (Filesize is too small): '{}'", pic.getContainerURL());
			}
			// Now we have to delete the file
			deleteFile(targetContainer);
			return false;
		}

		/*
		 * If we get here, everything is fine
		 */
		pic.setSize(totalBytesRead);

		/*
		 * The user can set subdirs for specific filesize-ranges
		 * So maybe we have to move the file
		 */
		if (settingsManager.getDirectorySettings().isSubDirsEnabled()) {
			handleFileSubDirectory(targetContainer, size);
		}

		if (lastURL) {
			completeDownload(pic, size);
		}
		return true;
	}

	/**
	 * Determine Filename by Content Disposition
	 * 
	 * @param response Response
	 * @param targetContainer Target Container
	 */
	private void determineFilenameByContentDisposition(ClassicHttpResponse response, TargetContainer targetContainer) {
		String contentDispositionFilename = getContentDispositionFilename(response);
		if (contentDispositionFilename != null) {
			targetContainer.setCorrectedFilename(contentDispositionFilename);
		}
	}

	/**
	 * Determine Filename by Regex Replace Pipeline
	 * 
	 * @param targetContainer Target Container
	 */
	private void determineFilenameByRegexReplacePipeline(TargetContainer targetContainer) {
		RegexReplacePipeline regexPipe = settingsManager.getRegexReplacePipelineFilename();
		String correctedFilename = regexPipe.getReplacedFilename(targetContainer.getCorrectedFilename());
		correctedFilename = BHUtil.filterFilename(correctedFilename, settingsManager);
		targetContainer.setCorrectedFilename(correctedFilename);
	}

	/**
	 * Set last modified time
	 * 
	 * @param targetContainer Target Container
	 * @param pic Pic
	 */
	private void setLastModifiedTime(TargetContainer targetContainer, Pic pic) {
		try {
			Path file = Paths.get(targetContainer.getTarget());
			Files.setLastModifiedTime(file, FileTime.fromMillis(pic.getLastModified()));
		} catch (IOException e) {
			logger.error("Could not set last modified time to file: {}", targetContainer.getTarget());
		}
	}

	/**
	 * Move file to sub directly if needed
	 * 
	 * @param targetContainer Target Container
	 * @param size File Size
	 */
	private void handleFileSubDirectory(TargetContainer targetContainer, long size) {
		boolean isImage = false;
		int imageWidth = 0;
		int imageHeight = 0;

		try {
			Matcher matcherImage = IMAGE_FILE_PATTERN.matcher(targetContainer.getTarget());
			if (matcherImage.matches()) {
				try (FileInputStream imageinput = new FileInputStream(targetContainer.getTarget())) {
					ImageInfo imageinfo = new ImageInfo();
					imageinfo.setInput(imageinput);
					if (!imageinfo.check()) {
						logger.debug("File ist not an image or the image format is not supported: {}", targetContainer.getTarget());
					} else {
						imageWidth = imageinfo.getWidth();
						imageHeight = imageinfo.getHeight();
						isImage = true;
					}
				}
			}
		} catch (Exception e) {
			// Nothing to do
		}

		List<Subdir> v = settingsManager.getSubDirs();
		for (int i = 0; i < v.size(); i++) {
			Subdir sdir = v.get(i);

			if (sdir.isInRange(size, imageWidth, imageHeight, isImage, settingsManager)) {
				// If the filesize is in the size-range of this subdir, then we move to file
				try {
					moveFileToSubdir(targetContainer.getCorrectedFilename(), targetContainer.getTargetPath(), sdir.getSubdirName());
					// TODO Shouldn't new targetPath be set to Pic and TargetContainer?
				} catch (IOException e) {
					// Nothing to do
				}
				break;
			}
		}
	}

	/**
	 * Delete File
	 * 
	 * @param targetContainer TargetContainer
	 */
	private void deleteFile(TargetContainer targetContainer) {
		deleteFile(targetContainer.getTarget());
	}

	/**
	 * Delete File
	 * 
	 * @param target Target
	 */
	private void deleteFile(String target) {
		if (target == null) {
			return;
		}
		try {
			// Delete the file
			Files.deleteIfExists(Paths.get(target));
		} catch (Exception e) {
			logger.error("Could not delete file: {}", target, e);
		}
	}

	@Override
	protected String getFilenameFromURL(String url) {
		return HTTPUtil.getFilenameFromURL(url, Localization.getString("Unkown"));
	}

	@Override
	protected void prepareTargetDirectory(Pic pic, URLParseObject result) {
		/*
		 * Now we filter not allowed chars and make sure path and filename
		 * are not too long.
		 */
		String targetPath = pic.getTargetPath();
		targetPath = BHUtil.filterPath(targetPath, settingsManager);
		targetPath = FileUtil.pathRTrim(targetPath);

		boolean bReducePathLength = true;
		if (result.checkExistInfo(URLParseObject.REDUCE_PATH_LENGTH, Boolean.class)) {
			bReducePathLength = (Boolean)result.getInfo(URLParseObject.REDUCE_PATH_LENGTH);
		}
		if (bReducePathLength) {
			targetPath = BHUtil.reducePathLength(targetPath, settingsManager);
		}

		pic.setTargetPath(targetPath);
	}

	/**
	 * Target Container class containing target file and corrected file name
	 */
	private static class TargetContainer {
		/**
		 * Pic
		 */
		private final Pic pic;

		/**
		 * First URL
		 */
		private final boolean firstURL;

		/**
		 * Target Path
		 */
		private String targetPath;

		/**
		 * Corrected Filename
		 */
		private String correctedFilename;

		/**
		 * Target
		 */
		private String target;

		/**
		 * Constructor
		 * 
		 * @param pic Pic
		 * @param firstURL First URL
		 * @param correctedFilename Corrected Filename
		 */
		public TargetContainer(Pic pic, boolean firstURL, String correctedFilename) {
			this.pic = pic;
			this.firstURL = firstURL;
			this.targetPath = pic.getTargetPath();
			this.correctedFilename = correctedFilename;
			this.target = target + correctedFilename;
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
		@SuppressWarnings("unused")
		public void setTargetPath(String targetPath) {
			this.targetPath = targetPath;
			this.target = targetPath + this.correctedFilename;
		}

		/**
		 * Returns the correctedFilename
		 * 
		 * @return correctedFilename
		 */
		public String getCorrectedFilename() {
			return correctedFilename;
		}

		/**
		 * Sets the correctedFilename
		 * 
		 * @param correctedFilename correctedFilename
		 */
		public void setCorrectedFilename(String correctedFilename) {
			this.correctedFilename = correctedFilename;
			this.target = this.targetPath + correctedFilename;
			if (firstURL) {
				pic.setTargetFilename(correctedFilename);
			}
		}

		/**
		 * Returns the target
		 * 
		 * @return target
		 */
		public String getTarget() {
			return target;
		}
	}
}

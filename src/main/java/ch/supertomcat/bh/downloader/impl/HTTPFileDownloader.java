package ch.supertomcat.bh.downloader.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import ch.supertomcat.bh.downloader.FileDownloaderBase;
import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.hoster.Hoster;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.hoster.parser.URLParseObjectFile;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.PicProgress;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
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
		URLParseObject result = parseURL(pic);

		String referrer = result.getContainerURL();
		Hoster lastHoster = result.getLastHoster();
		if ((lastHoster != null) && (lastHoster instanceof Rule)) {
			Rule lastRule = (Rule)lastHoster;
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
		if (result.checkExistInfo("ReduceFilenameLength") && result.getInfo("ReduceFilenameLength") instanceof Boolean) {
			bReduceFilenameLength = (Boolean)result.getInfo("ReduceFilenameLength");
		}

		// And replace the %20 in the filename, if there are any
		String targetFilename = BHUtil.filterPath(pic.getTargetFilename().replace("%20", " "), settingsManager);
		if (bReduceFilenameLength) {
			targetFilename = FileUtil.reduceFilenameLength(targetFilename);
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
					currentTargetFilename = FileUtil.reduceFilenameLength(currentTargetFilename);
				}
			}

			boolean lastURL = count == directLinksSize;
			if (!executeFileDownload(pic, directLink.getDirectLink(), currentTargetFilename, result, referrer, firstURL, lastURL, count, directLinksSize)) {
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
	 * @return True if download was successful, false otherwise
	 */
	private boolean executeFileDownload(Pic pic, String url, String correctedFilename, URLParseObject result, String referrer, boolean firstURL, boolean lastURL, int currentURL, int urlCount) {
		String target = pic.getTargetPath() + correctedFilename;

		// Get the cookies for the url
		String cookies = cookieManager.getCookies(url);

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
		try (CloseableHttpClient client = nonMultiThreadedHttpClient ? proxyManager.getNonMultithreadedHTTPClient() : proxyManager.getHTTPClient()) {
			String encodedURL = HTTPUtil.encodeURL(url);

			// Create a new GetMethod or PostMethod and set timeouts, cookies, user-agent and so on
			if (result.checkExistInfo("useMethod") && result.getInfo("useMethod") instanceof String && "POST".equals(result.getInfo("useMethod"))) {
				method = new HttpPost(encodedURL);
			} else {
				method = new HttpGet(encodedURL);
			}
			String userAgent = settingsManager.getUserAgent();
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
					failDownload(pic, result, false, "HTTP-Error: " + response.getStatusLine());
					method.abort();
					return false;
				}

				if (pic.isRenameWithContentDisposition() && !pic.isFixedTargetFilename()) {
					String contentDispositionFilename = getContentDispositionFilename(response);
					if (contentDispositionFilename != null) {
						correctedFilename = contentDispositionFilename;
						if (firstURL) {
							pic.setTargetFilename(correctedFilename);
						}
						target = pic.getTargetPath() + correctedFilename;
					}
				}

				RegexReplacePipeline regexPipe = settingsManager.getRegexReplacePipelineFilename();
				correctedFilename = regexPipe.getReplacedFilename(correctedFilename);
				correctedFilename = BHUtil.filterFilename(correctedFilename, settingsManager);
				if (firstURL) {
					pic.setTargetFilename(correctedFilename);
				}
				target = pic.getTargetPath() + correctedFilename;

				/*
				 * Now we create a new file.
				 * The createFile-Method gives us a File back
				 * because there could already be a file of the same
				 * name, so get back a new filename or another pathname.
				 */
				File fRetval = createFile(new File(target), correctedFilename, pic.getTargetPath());
				if (fRetval == null) {
					// If the file coult not be created
					failDownload(pic, result, false, Localization.getString("ErrorFileCouldNotBeCreated"));
					method.abort();
					return false;
				}
				correctedFilename = fRetval.getName();
				if (firstURL) {
					pic.setTargetFilename(correctedFilename);
				}
				target = pic.getTargetPath() + correctedFilename;

				// Let the listeners know, that the target has changed
				pic.targetChanged();

				/*
				 * Get the filesize
				 * Note: Some servers doesn't give us the filesize, so
				 * the filesize is maybe 0
				 */
				long size;
				if (method instanceof HttpGet) {
					long contentLength = response.getEntity().getContentLength();
					size = contentLength >= 0 ? contentLength : 0;
				} else if (method instanceof HttpPost) {
					long contentLength = response.getEntity().getContentLength();
					size = contentLength >= 0 ? contentLength : 0;
				} else {
					size = 0;
				}
				pic.setSize(size);

				if (size > 0 && size < settingsManager.getMinFilesize()) {
					/*
					 * The user can set in an options, which defines a minimum filesize.
					 * If the file here is to small
					 * Note: i check this here, and not above (where we get the filesize)
					 * because some servers don't give the filesize.
					 * So only here, we know the filesize really.
					 */
					failDownload(pic, result, true, Localization.getString("ErrorFilesizeToSmall"));
					logger.error("Download failed (Filesize is too small): '" + pic.getContainerURL() + "'");
					// Now we have to delete the file
					deleteFile(target);
					method.abort();
					return false;
				}

				PicProgress picProgress = pic.getProgress();
				picProgress.setBytesDownloaded(0);
				picProgress.setBytesTotal(size);
				pic.progressUpdated();

				long iBW = 0; // the amount of bytes we read since started downloading

				// Now get the inputstream and outputstream
				try (InputStream in = response.getEntity().getContent(); FileOutputStream out = new FileOutputStream(target)) {
					// Read some bytes to a buffer and write them to the outputstream
					int n; // the amount of bytes where were read per loop course
					byte[] buf = new byte[8192]; // The buffer
					int iBWs = 0; // the amount of bytes read since last download rate calculation
					int nReads = 0;
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
					boolean downloadRate = settingsManager.isDownloadRate();
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
						if (pic.isStop()) {
							method.abort();
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
							picProgress.setBytesDownloaded(iBW);
							picProgress.setUrlCount(urlCount);
							picProgress.setCurrentURLIndex(currentURL);
							pic.progressUpdated();
						}
						if (pic.isRecalcutateRate() && downloadRate) {
							// the flag is set to true, so we recalculate the download rate
							long now = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS); // get current timestamp
							// get the string for the rate
							double downloadBitrate = UnitFormatUtil.getBitrate(iBWs, size, timeStarted, now);
							picProgress.setRate(downloadBitrate);

							/*
							 * With this, we get always the actual download rate, not
							 * just an average over all the time
							 */
							timeStarted = now; // set this variable to current timestamp
							iBWs = 0; // set bytes read since last calculation to 0
							pic.rateRecalculated();
						}
					}
					pic.rateRecalculated();
					EntityUtils.consume(response.getEntity());
				}

				// If we have to set last modification timestamp to the file, we do it
				if (pic.isFixedLastModified() && pic.getLastModified() > 0) {
					File fMod = new File(target);
					fMod.setLastModified(pic.getLastModified());
				}

				if (!pic.isStop()) {
					// If the user doesn't stopped the download

					boolean downloadFailed = ((size > 0) && (iBW != size)) || (iBW < settingsManager.getMinFilesize());
					if (downloadFailed) {
						if ((size > 0) && (iBW != size)) {
							/*
							 * if we got a filesize, but the filesize does
							 * not equals the bytes we read, we didn't read
							 * the complete file, or more bytes than the file
							 * should have.
							 */
							failDownload(pic, result, false, Localization.getString("ErrorFilesizeNotMatchBytesRead"));
							logger.error("Download failed (Too many or to less bytes were downloaded): '" + pic.getContainerURL() + "'");
						} else if (iBW < settingsManager.getMinFilesize()) {
							/*
							 * The user can set in an options, which defines a minimum filesize.
							 * If the file here is to small
							 * Note: i check this here, and not above (where we get the filesize)
							 * because some servers don't give the filesize.
							 * So only here, we know the filesize really.
							 */
							failDownload(pic, result, true, Localization.getString("ErrorFilesizeToSmall"));
							logger.error("Download failed (Filesize is too small): '" + pic.getContainerURL() + "'");
						}
						// Now we have to delete the file
						deleteFile(target);
						return false;
					} else {
						/*
						 * If we get here, everything is fine
						 */
						pic.setSize(iBW);

						/*
						 * The user can set subdirs for specific filesize-ranges
						 * So maybe we have to move the file
						 */
						if (settingsManager.isSubdirsEnabled()) {
							boolean isImage = false;
							int imageWidth = 0;
							int imageHeight = 0;

							try {
								Matcher matcherImage = IMAGE_FILE_PATTERN.matcher(target);
								if (matcherImage.matches()) {
									try (FileInputStream imageinput = new FileInputStream(target)) {
										ImageInfo imageinfo = new ImageInfo();
										imageinfo.setInput(imageinput);
										if (!imageinfo.check()) {
											logger.debug("File ist not an image or the image format is not supported: {}", target);
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

							List<Subdir> v = settingsManager.getSubdirs();
							for (int i = 0; i < v.size(); i++) {
								Subdir sdir = v.get(i);

								if (sdir.isInRange(size, imageWidth, imageHeight, isImage, settingsManager)) {
									// If the filesize is in the size-range of this subdir, then we move to file
									try {
										moveFileToSubdir(correctedFilename, pic.getTargetPath(), sdir.getSubdirName());
									} catch (IOException e) {
										// Nothing to do
									}
									break;
								}
							}
						}

						if (lastURL) {
							completeDownload(pic, size);
						}
						return true;
					}
				} else {
					// If the user stopped the download
					stopDownload(pic);
					// Delete the file
					deleteFile(target);
					return false;
				}
			}
		} catch (MalformedURLException e) {
			failDownload(pic, result, false, e);
			return false;
		} catch (Exception e) {
			failDownload(pic, result, false, e);
			// Delete the file
			deleteFile(target);
			return false;
		} finally {
			if (method != null) {
				method.abort();
			}
		}
	}

	/**
	 * Delete File
	 * 
	 * @param target Target
	 */
	private void deleteFile(String target) {
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
		if (result.checkExistInfo("ReducePathLength") && result.getInfo("ReducePathLength") instanceof Boolean) {
			bReducePathLength = (Boolean)result.getInfo("ReducePathLength");
		}
		if (bReducePathLength) {
			targetPath = FileUtil.reducePathLength(targetPath);
		}

		pic.setTargetPath(targetPath);
	}
}

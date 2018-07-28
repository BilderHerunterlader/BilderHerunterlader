package ch.supertomcat.bh.downloader.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
import ch.supertomcat.bh.hoster.Hoster;
import ch.supertomcat.bh.hoster.URLParseObject;
import ch.supertomcat.bh.hoster.URLParseObjectFile;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.PicState;
import ch.supertomcat.bh.queue.DownloadQueueManager;
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
 * FileDownloader for HTTP URLs
 */
public class HTTPFileDownloader extends FileDownloaderBase {

	@Override
	public void downloadFile(Pic pic) throws HostException {
		URLParseObject result = parseURL(pic);

		String referrer = result.getContainerURL();
		Hoster lastHoster = result.getLastHoster();
		if ((lastHoster != null) && (lastHoster instanceof Rule)) {
			Rule lastRule = (Rule)lastHoster;
			switch (lastRule.getReferrerModeDownload()) {
				case Rule.REFERRER_NO_REFERRER:
					referrer = "";
					break;
				case Rule.REFERRER_LAST_CONTAINER_URL:
					referrer = result.getContainerURL();
					break;
				case Rule.REFERRER_FIRST_CONTAINER_URL:
					referrer = result.getFirstContainerURL();
					break;
				case Rule.REFERRER_ORIGIN_PAGE:
					referrer = pic.getThreadURL();
					break;
				case Rule.REFERRER_CUSTOM:
					referrer = lastRule.getCustomReferrerDownload();
					break;
			}
		}

		boolean bReduceFilenameLength = true;
		if (result.checkExistInfo("ReduceFilenameLength") && result.getInfo("ReduceFilenameLength") instanceof Boolean) {
			bReduceFilenameLength = (Boolean)result.getInfo("ReduceFilenameLength");
		}

		// And replace the %20 in the filename, if there are any
		String targetFilename = BHUtil.filterPath(pic.getTargetFilename().replace("%20", " "));
		if (bReduceFilenameLength) {
			targetFilename = FileTool.reduceFilenameLength(targetFilename);
		}
		pic.setTargetFilename(targetFilename);

		boolean fixedTargetFilename = pic.isFixedTargetFilename();

		boolean firstURL = true;
		for (URLParseObjectFile directLink : result.getAllDirectLinks()) {
			String currentTargetFilename = targetFilename;

			// if the hostclass gives us a nice filename
			String currentCorrectedFilename = directLink.getCorrectedFilename();
			if (currentCorrectedFilename != null && !currentCorrectedFilename.isEmpty() && (!fixedTargetFilename || targetFilename.isEmpty())) {
				currentTargetFilename = BHUtil.filterFilename(currentCorrectedFilename);
				// And replace the %20 in the filename, if there are any
				currentTargetFilename = BHUtil.filterPath(currentTargetFilename.replace("%20", " "));
				if (bReduceFilenameLength) {
					currentTargetFilename = FileTool.reduceFilenameLength(currentTargetFilename);
				}
			}

			executeFileDownload(pic, directLink.getDirectLink(), directLink.getCorrectedFilename(), result, referrer, firstURL);
			firstURL = false;
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
	 */
	private void executeFileDownload(Pic pic, String url, String correctedFilename, URLParseObject result, String referrer, boolean firstURL) {
		String target = pic.getTargetPath() + correctedFilename;

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
					failDownload(pic, result, false, "HTTP-Error: " + response.getStatusLine());
					method.abort();
					return;
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

				RegexReplacePipeline regexPipe = SettingsManager.instance().getRegexReplacePipelineFilename();
				correctedFilename = regexPipe.getReplacedFilename(correctedFilename);
				correctedFilename = BHUtil.filterFilename(correctedFilename);
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
					return;
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

				if (size > 0 && size < SettingsManager.instance().getMinFilesize()) {
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
					File fileT = new File(target);
					if (fileT.exists()) {
						fileT.delete();
					}
					method.abort();
					return;
				}

				pic.progressBarChanged(0, PicState.WAITING.getText());

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
					pic.progressBarChanged(0, PicState.DOWNLOADING.getText());

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
							pic.progressBarChanged(iBW, getProgressString(iBW, size) + bitrate);
						}
						if (pic.isRecalcutateRate() && downloadRate) {
							// the flag is set to true, so we recalculate the download rate
							long now = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS); // get current timestamp
							// get the string for the rate
							double downloadBitrate = UnitFormatTool.getBitrate(iBWs, size, timeStarted, now);
							pic.setDownloadBitrate(downloadBitrate);
							bitrate = " " + UnitFormatTool.getBitrateString(downloadBitrate);

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

					boolean downloadFailed = ((size > 0) && (iBW != size)) || (iBW < SettingsManager.instance().getMinFilesize());
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
						} else if (iBW < SettingsManager.instance().getMinFilesize()) {
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
						File fileT = new File(target);
						if (fileT.exists()) {
							fileT.delete();
						}
						fileT = null;
					} else {
						/*
						 * If we get here, everything is fine
						 */
						pic.setSize(iBW);

						/*
						 * The user can set subdirs for specific filesize-ranges
						 * So maybe we have to move the file
						 */
						if (SettingsManager.instance().isSubdirsEnabled()) {
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

							List<Subdir> v = SettingsManager.instance().getSubdirs();
							for (int i = 0; i < v.size(); i++) {
								Subdir sdir = v.get(i);

								if (sdir.isInRange(size, imageWidth, imageHeight, isImage)) {
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

						completeDownload(pic, size);
					}
				} else {
					// If the user stopped the download
					stopDownload(pic);
					// Delete the file
					File fileT = new File(target);
					if (fileT.exists()) {
						fileT.delete();
					}
				}
			}
		} catch (MalformedURLException e) {
			failDownload(pic, result, false, e);
		} catch (Exception e) {
			failDownload(pic, result, false, e);
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
			DownloadQueueManager.instance().removeDLSlotListener(pic); // important!
		}
	}

	/**
	 * Returns a String with the percent or size read
	 * 
	 * @param size Number of bytes read
	 * @param max Filesize
	 * @return Progress-String
	 */
	private String getProgressString(long size, long max) {
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

	@Override
	protected String getFilenameFromURL(String url) {
		return HTTPTool.getFilenameFromURL(url, Localization.getString("Unkown"));
	}

	@Override
	protected void prepareTargetDirectory(Pic pic, URLParseObject result) {
		/*
		 * Now we filter not allowed chars and make sure path and filename
		 * are not too long.
		 */
		String targetPath = pic.getTargetPath();
		targetPath = BHUtil.filterPath(targetPath);
		targetPath = FileTool.pathRTrim(targetPath);

		boolean bReducePathLength = true;
		if (result.checkExistInfo("ReducePathLength") && result.getInfo("ReducePathLength") instanceof Boolean) {
			bReducePathLength = (Boolean)result.getInfo("ReducePathLength");
		}
		if (bReducePathLength) {
			targetPath = FileTool.reducePathLength(targetPath);
		}

		pic.setTargetPath(targetPath);
	}
}

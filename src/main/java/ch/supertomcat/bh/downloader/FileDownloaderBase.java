package ch.supertomcat.bh.downloader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostAbortedException;
import ch.supertomcat.bh.exceptions.HostCompletedException;
import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostFileNotExistException;
import ch.supertomcat.bh.exceptions.HostFileTemporaryOfflineException;
import ch.supertomcat.bh.exceptions.HostIOException;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.PicState;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Base class for File Downloaders
 */
public abstract class FileDownloaderBase implements FileDownloader {
	/**
	 * Pattern for filename in content-disposition
	 */
	private static final Pattern CONTENT_DISPOSITION_FILENAME_PATTERN;

	/**
	 * Pattern for image files
	 */
	protected static final Pattern IMAGE_FILE_PATTERN = Pattern.compile(".*?(\\.(bmp|gif|jpe|jpg|jpeg|png|pcx|iff|ras|pbm|pgm|psd|tif|tiff))$", Pattern.CASE_INSENSITIVE);

	static {
		StringJoiner sjEncodings = new StringJoiner("|");
		for (Map.Entry<String, Charset> entry : Charset.availableCharsets().entrySet()) {
			sjEncodings.add(entry.getValue().displayName());
		}
		CONTENT_DISPOSITION_FILENAME_PATTERN = Pattern.compile(".*?filename[*]?=(" + sjEncodings.toString() + ")?('')?[\"]*([^\";]+)[\";]*.*", Pattern.CASE_INSENSITIVE);
	}

	/**
	 * Logger for this class
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Download Queue Manager
	 */
	protected final DownloadQueueManager downloadQueueManager;

	/**
	 * Settings Manager
	 */
	protected final SettingsManager settingsManager;

	/**
	 * Host Manager
	 */
	protected final HostManager hostManager;

	/**
	 * Constructor
	 * 
	 * @param downloadQueueManager Download Queue Manager
	 * @param settingsManager Settings Manager
	 * @param hostManager Host Manager
	 */
	public FileDownloaderBase(DownloadQueueManager downloadQueueManager, SettingsManager settingsManager, HostManager hostManager) {
		this.downloadQueueManager = downloadQueueManager;
		this.settingsManager = settingsManager;
		this.hostManager = hostManager;
	}

	/**
	 * Sets the status of the Pic and returns the download slot
	 * 
	 * @param pic Pic
	 * @param status Status
	 * @param progressBarValue ProgressBar Value
	 */
	protected void changeStatusAndReturnSlot(Pic pic, PicState status, long progressBarValue) {
		changeStatusAndReturnSlot(pic, status, progressBarValue, (String)null);
	}

	/**
	 * Sets the status of the Pic and returns the download slot
	 * 
	 * @param pic Pic
	 * @param status Status
	 * @param progressBarValue ProgressBar Value
	 * @param e Throwable or null
	 */
	protected void changeStatusAndReturnSlot(Pic pic, PicState status, long progressBarValue, Throwable e) {
		if (e == null) {
			changeStatusAndReturnSlot(pic, status, progressBarValue, (String)null);
		} else {
			changeStatusAndReturnSlot(pic, status, progressBarValue, e.getMessage());
		}
	}

	/**
	 * Sets the status of the Pic and returns the download slot
	 * 
	 * @param pic Pic
	 * @param status Status
	 * @param progressBarValue ProgressBar Value
	 * @param errorMessage Error message or null
	 */
	protected void changeStatusAndReturnSlot(Pic pic, PicState status, long progressBarValue, String errorMessage) {
		if (errorMessage == null) {
			pic.setStatus(status);
		} else {
			pic.setStatus(status, errorMessage);
		}
		pic.getProgress().setBytesDownloaded(progressBarValue);
		pic.progressUpdated();
	}

	/**
	 * Sets the status of the Pic back to SLEEPING and returns the download slot
	 * 
	 * @param pic Pic
	 */
	protected void stopDownload(Pic pic) {
		changeStatusAndReturnSlot(pic, PicState.SLEEPING, 0);
	}

	/**
	 * Sets the status of the Pic to FAILED and returns the download slot
	 * 
	 * @param pic Pic
	 * @param upo URLParseObject
	 * @param disableDownload True if download should be disabled, false otherwise
	 * @param e Exception
	 */
	protected void failDownload(Pic pic, URLParseObject upo, boolean disableDownload, Throwable e) {
		failDownload(pic, upo, disableDownload, null, e);
	}

	/**
	 * Sets the status of the Pic to FAILED and returns the download slot
	 * 
	 * @param pic Pic
	 * @param upo URLParseObject
	 * @param disableDownload True if download should be disabled, false otherwise
	 * @param errorMessage Error message or null
	 */
	protected void failDownload(Pic pic, URLParseObject upo, boolean disableDownload, String errorMessage) {
		failDownload(pic, upo, disableDownload, errorMessage, null);
	}

	/**
	 * Sets the status of the Pic to FAILED and returns the download slot
	 * 
	 * @param pic Pic
	 * @param upo URLParseObject
	 * @param disableDownload True if download should be disabled, false otherwise
	 * @param errorMessage Error message or null
	 * @param e Exception
	 */
	protected void failDownload(Pic pic, URLParseObject upo, boolean disableDownload, String errorMessage, Throwable e) {
		if (disableDownload) {
			pic.setToMaxFailedCount(settingsManager.getMaxFailedCount());
		} else {
			pic.increaseFailedCount(settingsManager.getMaxFailedCount());
		}
		if (errorMessage != null) {
			changeStatusAndReturnSlot(pic, PicState.FAILED, 0, errorMessage);
		} else {
			changeStatusAndReturnSlot(pic, PicState.FAILED, 0, e);
		}
		if (e != null) {
			if (upo != null) {
				logger.error("Download failed -> {}", upo.getHosterStackTrace(), e);
			} else {
				logger.error("Download failed", e);
			}
		}
	}

	/**
	 * Sets the status of the Pic to FAILED and returns the download slot
	 * 
	 * @param pic Pic
	 * @param e Exception
	 */
	protected void failDownloadTemporaryOffline(Pic pic, HostFileTemporaryOfflineException e) {
		pic.setToMaxFailedCount(settingsManager.getMaxFailedCount());
		changeStatusAndReturnSlot(pic, PicState.FAILED_FILE_TEMPORARY_OFFLINE, 0, e);
		if (e != null) {
			logger.debug("Download failed: {}", e.getMessage());
		}
	}

	/**
	 * Sets the status of the Pic to FAILED and returns the download slot
	 * 
	 * @param pic Pic
	 * @param e Exception
	 */
	protected void failDownloadFileNotExist(Pic pic, HostFileNotExistException e) {
		pic.setToMaxFailedCount(settingsManager.getMaxFailedCount());
		changeStatusAndReturnSlot(pic, PicState.FAILED_FILE_NOT_EXIST, 0, e);
		if (e != null) {
			logger.debug("Download failed: {}", e.getMessage());
		}
	}

	/**
	 * Sets the status of the Pic to COMPLETE and returns the download slot
	 * 
	 * @param pic Pic
	 * @param progressBarValue ProgressBar Value
	 */
	protected void completeDownload(Pic pic, long progressBarValue) {
		changeStatusAndReturnSlot(pic, PicState.COMPLETE, progressBarValue);
	}

	/**
	 * Parses the URL and returns the URLParseObject
	 * 
	 * @param pic Pic
	 * @return URLParseObject
	 * @throws HostException
	 */
	protected URLParseObject parseURL(Pic pic) throws HostException {
		if (pic.isStop()) {
			stopDownload(pic);
			throw new HostAbortedException("Download was aborted");
		}

		pic.setStatus(PicState.DOWNLOADING);
		pic.getProgress().setBytesDownloaded(0);
		pic.progressUpdated();

		// Create a new URLParseObject
		URLParseObject upo = new URLParseObject(pic.getContainerURL(), pic.getThumb(), pic);
		URLParseObject result = null;

		// Now we try to get the direct link
		try {
			// Now let the HostManager parse it
			result = hostManager.parseURL(upo);
			if (result == null) {
				throw new HostException(Localization.getString("ErrorImageURL"));
			}

			// Check if the direct link is not null and not empty
			if (result.getDirectLink() == null && result.getDirectLink().isEmpty()) {
				throw new HostException(Localization.getString("ErrorImageURL"));
			}

			// get the direct link
			String url = result.getDirectLink();
			pic.setDownloadURL(url);

			String correctedFilename = result.getCorrectedFilename();

			// if the hostclass gives us a nice filename
			if (correctedFilename != null && !correctedFilename.isEmpty() && (!pic.isFixedTargetFilename() || pic.getTargetFilename().isEmpty())) {
				pic.setTargetFilename(BHUtil.filterFilename(correctedFilename, settingsManager));
			}

			// If no specific filename was set, then get it from URL
			if (pic.getTargetFilename().isEmpty() || pic.getTargetFilename().equals(Localization.getString("Unkown"))) {
				String filenameFromURL = getFilenameFromURL(url);
				pic.setTargetFilename(BHUtil.filterFilename(filenameFromURL, settingsManager));
			}

			prepareTargetDirectory(pic, result);

			/*
			 * Because the parsing could took a lot of time we check here if
			 * the user has stopped the download.
			 */
			if (pic.isStop()) {
				stopDownload(pic);
				throw new HostAbortedException("Download was aborted");
			}

			File targetDirectory = new File(pic.getTargetPath());
			try {
				Files.createDirectories(targetDirectory.toPath());
			} catch (IOException e) {
				// If the directory could not be created
				failDownload(pic, upo, false, e);
				throw new HostIOException("Could not create directory: " + pic.getTargetPath(), e);
			}

			return result;
		} catch (HostCompletedException e) {
			/*
			 * If we get this exception, we have to set status of
			 * the download to complete and give the download-slot
			 * back to the Queue!
			 */
			completeDownload(pic, 0);
			throw e;
		} catch (HostAbortedException e) {
			stopDownload(pic);
			throw e;
		} catch (HostFileTemporaryOfflineException e) {
			/*
			 * If we get this exception, we have to set status of
			 * the download to failed and give the download-slot
			 * back to the Queue!
			 */
			failDownloadTemporaryOffline(pic, e);
			throw e;
		} catch (HostFileNotExistException e) {
			/*
			 * If we get this exception, we have to set status of
			 * the download to failed and give the download-slot
			 * back to the Queue!
			 */
			failDownloadFileNotExist(pic, e);
			throw e;
		} catch (Exception e) {
			/*
			 * If we get this exception, we have to set status of
			 * the download to failed and give the download-slot
			 * back to the Queue!
			 */
			failDownload(pic, upo, false, e);
			throw e;
		}
	}

	/**
	 * Prepare target directory
	 * 
	 * @param pic Pic
	 * @param result Result
	 */
	protected abstract void prepareTargetDirectory(Pic pic, URLParseObject result);

	/**
	 * Get filename from URL
	 * 
	 * @param url URL
	 * @return Filename
	 */
	protected abstract String getFilenameFromURL(String url);

	/**
	 * Returns the filename from content-disposition or null if not found
	 * 
	 * @param response
	 * @return Filename from content-disposition or null if not found
	 */
	protected String getContentDispositionFilename(HttpResponse response) {
		Header contentDisposition = response.getFirstHeader("content-disposition");
		if (contentDisposition == null) {
			return null;
		}

		String value = contentDisposition.getValue();
		logger.debug("Content Disposition Value: {}", value);

		value = value.replace("\\", "");
		Matcher matcher = CONTENT_DISPOSITION_FILENAME_PATTERN.matcher(value);
		if (matcher.find()) {
			String cdResult = matcher.replaceAll("$3");
			logger.debug("Content Disposition Filename: {}" + cdResult);
			if (cdResult.isEmpty()) {
				return null;
			}

			return BHUtil.filterFilename(cdResult, settingsManager);
		}
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
	protected static synchronized File createFile(File file, String targetFilename, String targetPath) throws IOException {
		if (file.exists()) {
			// If there is already a file or directory of the same name

			// Get position of the last . in the filename
			int iext = targetFilename.lastIndexOf(".");
			String ext;
			String filenameWithoutExt;
			// Get filename whithout the extension and the extension
			if (iext > 0) {
				ext = targetFilename.substring(iext);
				filenameWithoutExt = targetFilename.substring(0, iext);
			} else {
				ext = "";
				filenameWithoutExt = targetFilename;
			}

			/*
			 * Loop while the index is lower than 10'000.
			 * I set this limit to avoid an endless loop.
			 */
			int i = 1;
			while (i < 10000) {
				// add a number (the index)
				String newFilename = filenameWithoutExt + "-" + i + ext;
				File tf = new File(targetPath, newFilename);
				if (!tf.exists()) {
					// Ok there is no file with this name, so create it!
					Files.createFile(tf.toPath());
					return tf; // and return it
				}
				// There is also a file with this name, so we loop again
				i++;
			}

			// We can't create a file, so we return null
			return null;
		} else {
			// There is no file with this name, so we can create it
			Files.createFile(file.toPath());
			return file; // and return it
		}
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
	 * @param filename Filename
	 * @param path Path
	 * @param subdirName Name of the subdir
	 * @return File
	 * @throws IOException
	 */
	protected synchronized File moveFileToSubdir(String filename, String path, String subdirName) throws IOException {
		File fOriginal = new File(path, filename);
		File fTargetPath = new File(path, subdirName);
		return moveFile(fOriginal, fTargetPath);
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
	 * @param fileToMove Filename
	 * @param newPath Path
	 * @return File
	 * @throws IOException
	 */
	protected synchronized File moveFile(File fileToMove, File newPath) throws IOException {
		if (FileUtil.getDirectory(fileToMove.getAbsolutePath()).equals(newPath.getAbsolutePath())) {
			return fileToMove;
		}

		/*
		 * Create the directory if it is not existing
		 */
		try {
			Files.createDirectories(newPath.toPath());
		} catch (IOException e) {
			logger.error("Could not create directory: {}", newPath, e);
			throw e;
		}

		String filename = fileToMove.getName();
		File fMoved = new File(newPath, filename);
		try {
			/*
			 * Create a new file. Here it is the same procedure as above
			 * with the directory.
			 * We need to create a new file, because of the multithreaded
			 * downloading an other download could download to the same file
			 * or move a file to the same file, as we want to do here!
			 */
			File fMovedResult = createFile(fMoved, filename, newPath.getAbsolutePath());
			if (fMovedResult == null) {
				logger.error("Could not create file: {}", fMoved);
				return null;
			}

			Path targetFile = Files.move(fileToMove.toPath(), fMovedResult.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return targetFile.toFile();
		} catch (IOException e) {
			logger.error("Could not move file from '{}' to '{}'", fileToMove, fMoved, e);
			throw e;
		}
	}
}

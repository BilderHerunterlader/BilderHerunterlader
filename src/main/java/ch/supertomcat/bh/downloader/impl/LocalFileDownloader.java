package ch.supertomcat.bh.downloader.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ch.supertomcat.bh.downloader.FileDownloaderBase;
import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * "FileDownloader" for local files, which only sorts local files on the harddisk into different folders and not actually downloads anything
 */
public class LocalFileDownloader extends FileDownloaderBase {
	/**
	 * Constructor
	 * 
	 * @param downloadQueueManager Download Queue Manager
	 * @param settingsManager Settings Manager
	 * @param hostManager Host Manager
	 */
	public LocalFileDownloader(DownloadQueueManager downloadQueueManager, SettingsManager settingsManager, HostManager hostManager) {
		super(downloadQueueManager, settingsManager, hostManager);
	}

	@Override
	public void downloadFile(Pic pic) throws HostException {
		URLParseObject result = parseURL(pic, null);

		Path sourceFile = Paths.get(result.getDirectLink());
		Path targetFolder = Paths.get(pic.getTargetPath());
		moveLocalFile(pic, result, sourceFile, targetFolder);
	}

	/**
	 * Execute local file move
	 * 
	 * @param pic Pic
	 * @param result URLParseObject
	 * @param sourceFile Source File
	 * @param targetFolder Target Folder
	 */
	private void moveLocalFile(Pic pic, URLParseObject result, Path sourceFile, Path targetFolder) {
		try {
			// Get the filesize
			pic.setSize(Files.size(sourceFile));
			pic.getProgress().setBytesDownloaded(0);
			pic.progressUpdated();

			// Move the file
			Path fMoved = moveFile(sourceFile, targetFolder);
			if (fMoved == null) {
				// If the file could not be moved
				failDownload(pic, result, false, Localization.getString("ErrorFileCouldNotBeMoved"));
			} else {
				pic.setTargetPath(FileUtil.getDirectory(fMoved.toAbsolutePath().toString()));
				pic.setTargetFilename(fMoved.getFileName().toString());
				completeDownload(pic, pic.getSize());
			}
		} catch (Exception e) {
			failDownload(pic, result, false, e);
		}
	}

	@Override
	protected String getFilenameFromURL(String url) {
		return FileUtil.getFilename(url);
	}

	@Override
	protected void prepareTargetDirectory(Pic pic, URLParseObject result) {
		// Nothing to do
	}
}

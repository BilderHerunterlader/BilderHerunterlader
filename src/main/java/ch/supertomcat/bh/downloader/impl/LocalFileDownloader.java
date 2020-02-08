package ch.supertomcat.bh.downloader.impl;

import java.io.File;

import ch.supertomcat.bh.downloader.FileDownloaderBase;
import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * "FileDownloader" for local files, which only sorts local files on the harddisk into different folders and not actually downloads anything
 */
public class LocalFileDownloader extends FileDownloaderBase {
	@Override
	public void downloadFile(Pic pic) throws HostException {
		URLParseObject result = parseURL(pic);

		File sourceFile = new File(result.getDirectLink());
		File targetFolder = new File(pic.getTargetPath());
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
	private void moveLocalFile(Pic pic, URLParseObject result, File sourceFile, File targetFolder) {
		// Get the filesize
		pic.setSize(sourceFile.length());
		pic.getProgress().setBytesDownloaded(0);
		pic.progressUpdated();

		// Move the file
		try {
			File fMoved = moveFile(sourceFile, targetFolder);
			if (fMoved == null) {
				// If the file could not be moved
				failDownload(pic, result, false, Localization.getString("ErrorFileCouldNotBeMoved"));
			} else {
				pic.setTargetPath(FileUtil.getDirectory(fMoved.getAbsolutePath()));
				pic.setTargetFilename(fMoved.getName());
				pic.targetChanged();
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

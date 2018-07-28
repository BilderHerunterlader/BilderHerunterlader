package ch.supertomcat.bh.importexport;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.Main;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.PicState;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.fileiotools.FileTool;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.progressmonitor.ProgressObserver;

/**
 * Class for import download-queues from textfiles
 * Works only with textfiles exported by BilderHerunterlader!
 */
public abstract class ImportQueue {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(ImportQueue.class);

	/**
	 * 
	 */
	public static void importQueue() {
		int retval = JOptionPane
				.showConfirmDialog(Main.instance(), Localization.getString("ImportQueueWarning"), Localization.getString("QueueImport"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (retval == JOptionPane.CANCEL_OPTION) {
			return;
		}

		File file = Import.getTextFileFromFileChooserDialog(".*\\.txt", "Textfiles (.txt)", false);
		if (file != null) {
			SettingsManager.instance().setLastUsedImportDialogPath(FileTool.getPathFromFile(file));
			// read the file
			read(file);
			file = null;
		}
	}

	/**
	 * @param strFile File
	 */
	public static void importQueue(String strFile) {
		read(new File(strFile));
	}

	/**
	 * Read the textfile
	 * 
	 * This method imports the downloads directly to the queue
	 * 
	 * @param file File
	 */
	private static void read(File file) {
		ProgressObserver pg = new ProgressObserver();
		try (FileInputStream fIn = new FileInputStream(file); InputStream in = new BufferedInputStream(fIn)) {
			String enc = null;
			try {
				enc = FileTool.getEncodingFromInputStream(in);
			} catch (IOException ioe) {
				logger.error(ioe.getMessage(), ioe);
			}

			if (enc == null) {
				EncodingSelectionDialog esd = new EncodingSelectionDialog(Main.instance());
				enc = esd.getChosenEncoding();
				if (enc == null) {
					return;
				}
			}

			try (BufferedReader br = new BufferedReader(new InputStreamReader(in, enc))) {
				String row = null;

				Main.instance().addProgressObserver(pg);
				pg.progressChanged(-1, -1, -1);
				pg.progressChanged(Localization.getString("QueueImporting") + "...");
				int iRow = 0;
				ArrayList<Pic> picsToAdd = new ArrayList<>();
				while ((row = br.readLine()) != null) {
					if (iRow == 0) {
						iRow++;
						continue;
					}

					String arr[] = row.split("\t");
					if (arr.length == 10) {
						String containerURL = arr[0];
						String thumbnailURL = arr[1];
						String threadURL = arr[2];
						String targetPath = arr[3];
						String targetFilename = arr[4];
						PicState status = PicState.SLEEPING;
						try {
							int iStatus = Integer.parseInt(arr[5]);
							status = PicState.getByValue(iStatus);
						} catch (NumberFormatException nfe) {
							logger.error(nfe.getMessage(), nfe);
						}

						long lastModified = 0;
						try {
							lastModified = Long.parseLong(arr[6]);
							if (lastModified < 0) {
								lastModified = 0;
							}
						} catch (NumberFormatException nfe) {
							logger.error(nfe.getMessage(), nfe);
						}

						boolean fixedTargetFilename = Boolean.parseBoolean(arr[7]);
						boolean fixedLastModified = Boolean.parseBoolean(arr[8]);
						boolean deactivated = Boolean.parseBoolean(arr[9]);

						Pic pic = new Pic(containerURL, targetFilename, targetPath);
						pic.setThumb(thumbnailURL);
						pic.setThreadURL(threadURL);
						pic.setStatus(status);
						pic.setLastModified(lastModified);
						pic.setFixedTargetFilename(fixedTargetFilename);
						pic.setFixedLastModified(fixedLastModified);
						pic.setDeactivated(deactivated);
						picsToAdd.add(pic);
					}

					iRow++;
					if (iRow % 100 == 0) {
						pg.progressChanged(Localization.getString("QueueImporting") + "..." + iRow + " " + Localization.getString("DownloadsImported") + "...");
					}
				}
				QueueManager.instance().addPics(picsToAdd);
				QueueManager.instance().saveDatabase();
				System.gc();

				Main.instance().setMessage(Localization.getString("TextFileImported"));
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			Main.instance().setMessage(Localization.getString("TextFileImportFailed"));
		} finally {
			Main.instance().removeProgressObserver(pg);
		}
	}
}

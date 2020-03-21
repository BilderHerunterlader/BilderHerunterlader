package ch.supertomcat.bh.importexport;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.importexport.base.EncodingSelectionDialog;
import ch.supertomcat.bh.importexport.base.ImportExportBase;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.PicState;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Class for import download-queues from textfiles
 * Works only with textfiles exported by BilderHerunterlader!
 */
public class ImportQueue extends ImportExportBase {
	/**
	 * Queue Manager
	 */
	private final QueueManager queueManager;

	/**
	 * Constructor
	 * 
	 * @param parentComponent Parent Component
	 * @param mainWindowAccess Main Window Access
	 * @param queueManager Queue Manager
	 */
	public ImportQueue(Component parentComponent, MainWindowAccess mainWindowAccess, QueueManager queueManager) {
		super(parentComponent, mainWindowAccess);
		this.queueManager = queueManager;
	}

	/**
	 * 
	 */
	public void importQueue() {
		int retval = JOptionPane
				.showConfirmDialog(parentComponent, Localization.getString("ImportQueueWarning"), Localization.getString("QueueImport"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (retval == JOptionPane.CANCEL_OPTION) {
			return;
		}

		File file = getTextFileFromFileChooserDialog(".*\\.txt", "Textfiles (.txt)", false);
		if (file != null) {
			SettingsManager.instance().setLastUsedImportDialogPath(FileUtil.getPathFromFile(file));
			// read the file
			read(file);
			file = null;
		}
	}

	/**
	 * @param strFile File
	 */
	public void importQueue(String strFile) {
		read(new File(strFile));
	}

	/**
	 * Read the textfile
	 * 
	 * This method imports the downloads directly to the queue
	 * 
	 * @param file File
	 */
	private void read(File file) {
		ProgressObserver pg = new ProgressObserver();
		try (FileInputStream fIn = new FileInputStream(file); InputStream in = new BufferedInputStream(fIn)) {
			String enc = null;
			try {
				enc = BHUtil.getEncodingFromInputStream(in);
			} catch (IOException ioe) {
				logger.error(ioe.getMessage(), ioe);
			}

			if (enc == null) {
				EncodingSelectionDialog esd = getEncodingSelectionDialog();
				enc = esd.getChosenEncoding();
				if (enc == null) {
					return;
				}
			}

			try (BufferedReader br = new BufferedReader(new InputStreamReader(in, enc))) {
				String row = null;

				mainWindowAccess.addProgressObserver(pg);
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
				queueManager.addPics(picsToAdd);
				queueManager.saveDatabase();
				System.gc();

				mainWindowAccess.setMessage(Localization.getString("TextFileImported"));
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			mainWindowAccess.setMessage(Localization.getString("TextFileImportFailed"));
		} finally {
			mainWindowAccess.removeProgressObserver(pg);
		}
	}
}

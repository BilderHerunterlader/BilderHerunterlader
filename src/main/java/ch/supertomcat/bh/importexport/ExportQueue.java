package ch.supertomcat.bh.importexport;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.importexport.base.EncodingSelectionDialog;
import ch.supertomcat.bh.importexport.base.ImportExportBase;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Class for exporting the queue
 */
public class ExportQueue extends ImportExportBase {
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
	 * @param settingsManager Settings Manager
	 */
	public ExportQueue(Component parentComponent, MainWindowAccess mainWindowAccess, QueueManager queueManager, SettingsManager settingsManager) {
		super(parentComponent, mainWindowAccess, settingsManager);
		this.queueManager = queueManager;
	}

	/**
	 * Export keywords to a tab-seperated textfile
	 */
	public void exportQueue() {
		File file = getTextFileFromFileChooserDialog(".+\\.txt", "Tab-seperated Textfiles (.txt)", true);
		if (file != null) {
			settingsManager.setLastUsedExportDialogPath(FileUtil.getPathFromFile(file));
			// export the keywords
			exportQueue(file.getAbsolutePath());
		}
	}

	/**
	 * Export the queue to a tab-seperated textfile
	 * A line looks like this:
	 * containerURL\tthumbnailURL\tthreadURL\ttargetPath\ttargetFilename\tstatus\tlastModified\tfixedTargetFilename\tfixedLastModified\tdeactivated
	 * 
	 * @param file File
	 */
	public void exportQueue(String file) {
		EncodingSelectionDialog esd = getEncodingSelectionDialog();
		String enc = esd.getChosenEncoding();
		if (enc == null) {
			return;
		}

		// Get the queue
		List<Pic> queue = queueManager.getQueue();

		int i = 0;
		ProgressObserver pg = new ProgressObserver();
		try (FileOutputStream out = new FileOutputStream(file); BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, enc))) {
			String row = null;

			mainWindowAccess.addProgressObserver(pg);
			pg.progressChanged(-1, -1, -1);
			pg.progressChanged(Localization.getString("QueueExporting") + "...");
			row = "containerURL\tthumbnailURL\tthreadURL\ttargetPath\ttargetFilename\tstatus\tlastModified\tfixedTargetFilename\tfixedLastModified\tdeactivated\n";
			bw.write(row);
			Iterator<Pic> it = queue.iterator();
			while (it.hasNext()) {
				row = "";
				Pic pic = it.next();

				String containerURL = pic.getContainerURL();
				String thumbnailURL = pic.getThumb();
				String threadURL = pic.getThreadURL();
				String targetPath = pic.getTargetPath();
				String targetFilename = pic.getTargetFilename();
				int status = pic.getStatus().getValue();
				long lastModified = pic.getLastModified();
				boolean fixedTargetFilename = pic.isFixedTargetFilename();
				boolean fixedLastModified = pic.isFixedLastModified();
				boolean deactivated = pic.isDeactivated();

				row = containerURL + "\t" + thumbnailURL + "\t" + threadURL + "\t" + targetPath + "\t" + targetFilename + "\t" + status + "\t" + lastModified + "\t" + fixedTargetFilename + "\t"
						+ fixedLastModified + "\t" + deactivated + "\n";
				bw.write(row);
				i++;
			}

			bw.flush();
			queue = null;
			mainWindowAccess.removeProgressObserver(pg);
			mainWindowAccess.setMessage(Localization.getString("QueueExported"));
			JOptionPane.showMessageDialog(parentComponent, i + " " + Localization.getString("DownloadsExported"), Localization.getString("QueueExport"), JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			mainWindowAccess.removeProgressObserver(pg);
			mainWindowAccess.setMessage(Localization.getString("QueueExportFailed"));
		}
	}
}

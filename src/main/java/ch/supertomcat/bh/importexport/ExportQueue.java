package ch.supertomcat.bh.importexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.Main;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.io.FileUtil;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;

/**
 * Class for exporting the queue
 */
public abstract class ExportQueue {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(ExportQueue.class);

	/**
	 * Export keywords to a tab-seperated textfile
	 */
	public static void exportQueue() {
		File file = Import.getTextFileFromFileChooserDialog(".+\\.txt", "Tab-seperated Textfiles (.txt)", true);
		if (file != null) {
			SettingsManager.instance().setLastUsedExportDialogPath(FileUtil.getPathFromFile(file));
			// export the keywords
			exportQueue(file.getAbsolutePath());
			file = null;
		}
	}

	/**
	 * Export the queue to a tab-seperated textfile
	 * A line looks like this:
	 * containerURL\tthumbnailURL\tthreadURL\ttargetPath\ttargetFilename\tstatus\tlastModified\tfixedTargetFilename\tfixedLastModified\tdeactivated
	 * 
	 * @param file File
	 */
	public static void exportQueue(String file) {
		EncodingSelectionDialog esd = new EncodingSelectionDialog(Main.instance());
		String enc = esd.getChosenEncoding();
		if (enc == null) {
			return;
		}

		// Get the queue
		List<Pic> queue = QueueManager.instance().getQueue();

		int i = 0;
		ProgressObserver pg = new ProgressObserver();
		try (FileOutputStream out = new FileOutputStream(file); BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, enc))) {
			String row = null;

			Main.instance().addProgressObserver(pg);
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
			Main.instance().removeProgressObserver(pg);
			Main.instance().setMessage(Localization.getString("QueueExported"));
			JOptionPane.showMessageDialog(Main.instance(), i + " " + Localization.getString("DownloadsExported"), Localization.getString("QueueExport"), JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			Main.instance().removeProgressObserver(pg);
			Main.instance().setMessage(Localization.getString("QueueExportFailed"));
		}
	}
}

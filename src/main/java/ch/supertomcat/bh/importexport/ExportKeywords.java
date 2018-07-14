package ch.supertomcat.bh.importexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.Main;
import ch.supertomcat.bh.keywords.Keyword;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.fileiotools.FileTool;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.progressmonitor.ProgressObserver;

/**
 * Class for exporting keywords
 */
public abstract class ExportKeywords {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(ExportKeywords.class);

	/**
	 * Export keywords to a tab-seperated textfile
	 */
	public static void exportKeywords() {
		File file = Import.getTextFileFromFileChooserDialog(".+\\.txt", "Textfiles (.txt)", true);
		if (file != null) {
			SettingsManager.instance().setLastUsedExportDialogPath(FileTool.getPathFromFile(file));
			// export the keywords
			exportKeywords(file.getAbsolutePath());
			file = null;
		}
	}

	/**
	 * Export keywords to a tab-seperated textfile
	 * A line looks like this:
	 * Title Keywords AbsolutePath RelativePath isRelativePath
	 * If one of this values is not available, then there will be a #
	 * An example: There is no AbsolutePath then the line looks like this:
	 * Title Keywords # RelativePath isRelativePath
	 * This can be done for all values excepts the title!
	 * 
	 * @param file File
	 */
	public static void exportKeywords(String file) {
		EncodingSelectionDialog esd = new EncodingSelectionDialog(Main.instance());
		String enc = esd.getChosenEncoding();
		if (enc == null) {
			return;
		}

		// Get all keywords
		List<Keyword> keyw = KeywordManager.instance().getKeywords();
		// Sort them
		Collections.sort(keyw);
		int i = 0;
		ProgressObserver pg = new ProgressObserver();
		try (FileOutputStream out = new FileOutputStream(file); BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, enc))) {
			String row = null;

			Main.instance().addProgressObserver(pg);
			pg.progressChanged(-1, -1, -1);
			pg.progressChanged(Localization.getString("KeywordsExporting") + "...");
			Iterator<Keyword> it = keyw.iterator();
			while (it.hasNext()) {
				row = "";
				Keyword k = it.next();
				String title = k.getTitle();
				if (title.length() == 0)
					continue;

				String keywords = k.getKeywords();
				if (keywords.length() == 0) {
					keywords = "#";
				}

				String path = k.getDownloadPath();
				if (path.length() == 0) {
					path = "#";
				}

				String rpath = k.getRelativeDownloadPath();
				if (rpath.length() == 0) {
					rpath = "#";
				}

				boolean brpath = k.isRelativePath();

				row = title + "\t" + keywords + "\t" + path + "\t" + rpath + "\t" + brpath + "\n";
				bw.write(row);
				i++;
			}

			bw.flush();
			keyw = null;
			Main.instance().removeProgressObserver(pg);
			Main.instance().setMessage(Localization.getString("KeywordsExported"));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			Main.instance().removeProgressObserver(pg);
			Main.instance().setMessage(Localization.getString("KeywordExportFailed"));
		}
		JOptionPane.showMessageDialog(Main.instance(), i + " " + Localization.getString("KeywordsExported"), Localization.getString("KeywordExport"), JOptionPane.INFORMATION_MESSAGE);
	}
}

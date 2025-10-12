package ch.supertomcat.bh.importexport;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.importexport.base.EncodingSelectionDialog;
import ch.supertomcat.bh.importexport.base.ImportExportBase;
import ch.supertomcat.bh.keywords.Keyword;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;

/**
 * Class for exporting keywords
 */
public class ExportKeywords extends ImportExportBase {
	/**
	 * Keyword Manager
	 */
	private final KeywordManager keywordManager;

	/**
	 * Constructor
	 * 
	 * @param parentComponent Parent Component
	 * @param mainWindowAccess Main Window Access
	 * @param keywordManager Keyword Manager
	 * @param settingsManager Settings Manager
	 */
	public ExportKeywords(Component parentComponent, MainWindowAccess mainWindowAccess, KeywordManager keywordManager, SettingsManager settingsManager) {
		super(parentComponent, mainWindowAccess, settingsManager);
		this.keywordManager = keywordManager;
	}

	/**
	 * Export keywords to a tab-seperated textfile
	 */
	public void exportKeywords() {
		File file = getTextFileFromFileChooserDialog(".+\\.txt", "Textfiles (.txt)", true);
		if (file != null) {
			setLastUsedExportPath(file);
			// export the keywords
			exportKeywords(file.getAbsolutePath());
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
	public void exportKeywords(String file) {
		EncodingSelectionDialog esd = getEncodingSelectionDialog();
		String enc = esd.getChosenEncoding();
		if (enc == null) {
			return;
		}

		// Get all keywords
		List<Keyword> keyw = keywordManager.getKeywords();
		// Sort them
		Collections.sort(keyw);
		int i = 0;
		ProgressObserver pg = new ProgressObserver();
		try (FileOutputStream out = new FileOutputStream(file); BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, enc))) {
			String row = null;

			mainWindowAccess.addProgressObserver(pg);
			pg.progressChanged(-1, -1, -1);
			pg.progressChanged(Localization.getString("KeywordsExporting") + "...");
			Iterator<Keyword> it = keyw.iterator();
			while (it.hasNext()) {
				row = "";
				Keyword k = it.next();
				String title = k.getTitle();
				if (title.length() == 0) {
					continue;
				}

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
			mainWindowAccess.removeProgressObserver(pg);
			mainWindowAccess.setMessage(Localization.getString("KeywordsExported"));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			mainWindowAccess.removeProgressObserver(pg);
			mainWindowAccess.setMessage(Localization.getString("KeywordExportFailed"));
		}
		JOptionPane.showMessageDialog(parentComponent, i + " " + Localization.getString("KeywordsExported"), Localization.getString("KeywordExport"), JOptionPane.INFORMATION_MESSAGE);
	}
}

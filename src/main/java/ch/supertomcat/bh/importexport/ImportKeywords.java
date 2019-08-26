package ch.supertomcat.bh.importexport;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.Main;
import ch.supertomcat.bh.keywords.Keyword;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Class for importing keywords from a textfile
 * 
 * @see ch.supertomcat.bh.importexport.ExportKeywords
 */
public abstract class ImportKeywords {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(ImportKeywords.class);

	/**
	 * Import keywords from a textfile
	 */
	public static void importKeywords() {
		File file = Import.getTextFileFromFileChooserDialog(".+\\.txt", "Tab-seperated Textfiles (.txt)", false);
		if (file != null) {
			SettingsManager.instance().setLastUsedImportDialogPath(FileUtil.getPathFromFile(file));
			importKeywords(file.getAbsolutePath());
			file = null;
		}
	}

	/**
	 * Import keywords from a textfile
	 * 
	 * @param file File
	 */
	public static void importKeywords(String file) {
		List<Keyword> keyw = KeywordManager.instance().getKeywords();
		List<Keyword> v = new ArrayList<>();

		ProgressObserver pg = new ProgressObserver();
		try (FileInputStream fIn = new FileInputStream(file); InputStream in = new BufferedInputStream(fIn)) {
			String enc = null;
			try {
				enc = BHUtil.getEncodingFromInputStream(in);
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
				pg.progressChanged(Localization.getString("KeywordsImporting") + "...");
				while ((row = br.readLine()) != null) {
					// Split the line
					String line[] = row.split("\t");
					if (line.length == 5) {
						// If there is no title, ignore the whole line!
						if (line[0].length() == 0) {
							continue;
						}
						String title = line[0];

						// Check if there is already a keyword with the same title
						boolean exists = false;
						Iterator<Keyword> it = keyw.iterator();
						while (it.hasNext()) {
							if (it.next().getTitle().equals(title)) {
								exists = true;
								break;
							}
						}
						Iterator<Keyword> itx = v.iterator();
						while (itx.hasNext()) {
							if (itx.next().getTitle().equals(title)) {
								exists = true;
								break;
							}
						}
						it = null;
						if (exists) {
							continue;
						}

						String keywords = line[1];
						if (line[1].equals("#")) {
							keywords = title;
						}

						String path = line[2];
						if (line[2].equals("#")) {
							path = SettingsManager.instance().getSavePath() + title;
						}

						String rpath = line[3];
						if (line[3].equals("#")) {
							rpath = title;
						}

						boolean brpath = true;
						if (line[4].equals("false")) {
							brpath = false;
						}
						Keyword k = new Keyword(title, keywords, path, brpath, rpath);
						v.add(k);
					}
				}
			}
			Main.instance().setMessage(Localization.getString("KeywordsImported"));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			Main.instance().setMessage(Localization.getString("KeywordImportFailed"));
		} finally {
			Main.instance().removeProgressObserver(pg);
		}
		if (!v.isEmpty()) {
			// Add the keywords
			Main.instance().clearKeywordFilters();
			KeywordManager.instance().addKeywords(v);
		}
		JOptionPane.showMessageDialog(Main.instance(), v.size() + " " + Localization.getString("KeywordsImported"), Localization.getString("KeywordImport"), JOptionPane.INFORMATION_MESSAGE);
	}
}

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
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.importexport.base.EncodingSelectionDialog;
import ch.supertomcat.bh.importexport.base.ImportExportBase;
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
public class ImportKeywords extends ImportExportBase {
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
	public ImportKeywords(Component parentComponent, MainWindowAccess mainWindowAccess, KeywordManager keywordManager, SettingsManager settingsManager) {
		super(parentComponent, mainWindowAccess, settingsManager);
		this.keywordManager = keywordManager;
	}

	/**
	 * Import keywords from a textfile
	 */
	public void importKeywords() {
		File file = getTextFileFromFileChooserDialog(".+\\.txt", "Tab-seperated Textfiles (.txt)", false);
		if (file != null) {
			settingsManager.setLastUsedImportDialogPath(FileUtil.getPathFromFile(file));
			importKeywords(file.getAbsolutePath());
		}
	}

	/**
	 * Import keywords from a textfile
	 * 
	 * @param file File
	 */
	public void importKeywords(String file) {
		List<Keyword> keyw = keywordManager.getKeywords();
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
				pg.progressChanged(Localization.getString("KeywordsImporting") + "...");
				while ((row = br.readLine()) != null) {
					// Split the line
					String[] line = row.split("\t");
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
							path = settingsManager.getSavePath() + title;
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
			mainWindowAccess.setMessage(Localization.getString("KeywordsImported"));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			mainWindowAccess.setMessage(Localization.getString("KeywordImportFailed"));
		} finally {
			mainWindowAccess.removeProgressObserver(pg);
		}
		if (!v.isEmpty()) {
			// Add the keywords
			mainWindowAccess.clearKeywordFilters();
			keywordManager.addKeywords(v);
		}
		JOptionPane.showMessageDialog(parentComponent, v.size() + " " + Localization.getString("KeywordsImported"), Localization.getString("KeywordImport"), JOptionPane.INFORMATION_MESSAGE);
	}
}

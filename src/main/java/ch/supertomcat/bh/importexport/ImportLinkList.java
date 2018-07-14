package ch.supertomcat.bh.importexport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.adder.AdderPanel;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.pic.URLList;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.fileiotools.FileTool;
import ch.supertomcat.supertomcattools.guitools.Localization;

/**
 * Class for import links from textfiles
 */
public abstract class ImportLinkList {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(ImportLinkList.class);

	/**
	 * 
	 */
	public static void importLinkList() {
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
	 * @param deleteFile Delete File
	 */
	public static void importLinkList(String strFile, boolean deleteFile) {
		File file = new File(strFile);
		// read the file
		boolean ok = read(file);
		if (ok && deleteFile) {
			file.delete();
		}
		file = null;
	}

	/**
	 * Read the textfile
	 * 
	 * @param file File
	 * @return True if successful, false otherwise
	 */
	private static boolean read(File file) {
		try (FileInputStream in = new FileInputStream(file); BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			return read(br);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Reads the textfile and detects if it's a file generated from a Browser-Plugin or a normal one link per line textfile
	 * 
	 * The textfile generated from Browser-Plugins must have in the first 3 lines have this information:
	 * BH{af2f0750-c598-4826-8e5f-bb98aab519a5}
	 * Title
	 * Referrer
	 * 
	 * Then it can be a link per line or a link then a tab and then a thumbnail-URL per line
	 * Example:
	 * http://img207.imagevenue.com/img.php?image=30493_BJ_Nora_Tschirner_CID__002_122_579lo.jpg
	 * or
	 * http://img207.imagevenue.com/img.php?image=30493_BJ_Nora_Tschirner_CID__002_122_579lo.jpg
	 * http://img207.imagevenue.com/loc579/th_30493_BJ_Nora_Tschirner_CID__002_122_579lo.jpg
	 * 
	 * This method opens the Download-Selection-Dialog
	 * 
	 * @param br BufferedReader
	 * @return True if successful
	 */
	public static boolean read(BufferedReader br) {
		try {
			boolean raw = false;
			String line = null;
			int lineCount = 0;
			String title = Localization.getString("Unkown");
			String referrer = Localization.getString("Unkown");
			ArrayList<URL> urls = new ArrayList<>();

			while ((line = br.readLine()) != null) {
				if (lineCount == 0 && line.startsWith("BH{af2f0750-c598-4826-8e5f-bb98aab519a5}")) {
					raw = true;
				} else if (raw && lineCount == 1) {
					title = line;
				} else if (raw && lineCount == 2) {
					referrer = line;
				} else if (!raw || (raw && lineCount > 2)) {
					int last = line.length();
					int seperator = line.indexOf("\t");
					URL urlToAdd = null;
					if ((seperator > 0) && (seperator < last)) {
						urlToAdd = new URL(line.substring(0, seperator), line.substring(seperator + 1, last));
					} else {
						urlToAdd = new URL(line, "");
					}
					urlToAdd.setThreadURL(referrer);
					urls.add(urlToAdd);
				}
				lineCount++;
			}

			if (urls.size() > 0) {
				// Open Download-Selection-Dialog
				AdderPanel adderpnl = new AdderPanel(new URLList(title, referrer, urls));
				adderpnl.init();
				adderpnl = null;
			}

			br.close();
			br = null;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}
}

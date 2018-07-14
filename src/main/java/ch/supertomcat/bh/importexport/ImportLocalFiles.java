package ch.supertomcat.bh.importexport;

import java.io.File;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.adder.AdderPanel;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.pic.URLList;
import ch.supertomcat.supertomcattools.guitools.Localization;

/**
 * Class for import files on the harddisk which have to be sort
 */
public abstract class ImportLocalFiles {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(ImportLocalFiles.class);

	/**
	 * "Read" the files
	 * Read here means, it is an array of URLs created for the files
	 * 
	 * This method opens the Download-Selection-Dialog
	 * 
	 * @param files Files
	 */
	public static void importLocalFiles(File files[]) {
		if (files == null) {
			return;
		}
		ArrayList<URL> urls = new ArrayList<>();

		for (int i = 0; i < files.length; i++) {
			if (files[i].exists()) {
				urls.add(new URL(files[i].getAbsolutePath()));
			} else {
				logger.error("File '" + files[i].getAbsolutePath() + "' does not exist. So it can not be sorted.");
			}
		}

		if (urls.size() > 0) {
			// Open Download-Selection-Dialog
			AdderPanel adderpnl = new AdderPanel(true, new URLList(Localization.getString("Unkown") + ": " + Localization.getString("Title"), Localization.getString("Unkown") + ": "
					+ Localization.getString("Referrer"), urls));
			adderpnl.init();
			adderpnl = null;
		}
	}
}

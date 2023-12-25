package ch.supertomcat.bh.importexport;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;

import ch.supertomcat.bh.clipboard.ClipboardObserver;
import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.gui.adder.AdderPanel;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.importexport.base.AdderImportBase;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.pic.URLList;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Class for import files on the harddisk which have to be sort
 */
public class ImportLocalFiles extends AdderImportBase {
	/**
	 * Constructor
	 * 
	 * @param parentComponent Parent Component
	 * @param mainWindowAccess Main Window Access
	 * @param logManager Log Manager
	 * @param queueManager Queue Manager
	 * @param keywordManager Keyword Manager
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param hostManager Host Manager
	 * @param clipboardObserver Clipboard Observer
	 */
	public ImportLocalFiles(Component parentComponent, MainWindowAccess mainWindowAccess, LogManager logManager, QueueManager queueManager, KeywordManager keywordManager, ProxyManager proxyManager,
			SettingsManager settingsManager, HostManager hostManager, ClipboardObserver clipboardObserver) {
		super(parentComponent, mainWindowAccess, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver);
	}

	/**
	 * "Read" the files
	 * Read here means, it is an array of URLs created for the files
	 * 
	 * This method opens the Download-Selection-Dialog
	 * 
	 * @param files Files
	 * @param title Title or null
	 */
	public void importLocalFiles(File[] files, String title) {
		if (files == null) {
			return;
		}
		ArrayList<URL> urls = new ArrayList<>();

		for (int i = 0; i < files.length; i++) {
			if (files[i].exists()) {
				urls.add(new URL(files[i].getAbsolutePath()));
			} else {
				logger.error("File '{}' does not exist. So it can not be sorted.", files[i].getAbsolutePath());
			}
		}

		if (title == null) {
			title = Localization.getString("Unkown") + ": " + Localization.getString("Title");
		}
		String referrer = Localization.getString("Unkown") + ": " + Localization.getString("Referrer");

		if (!urls.isEmpty()) {
			// Open Download-Selection-Dialog
			AdderPanel adderpnl = new AdderPanel(parentComponent, true, new URLList(title, referrer, urls), logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver);
			adderpnl.init();
		}
	}
}

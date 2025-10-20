package ch.supertomcat.bh.importexport;

import java.awt.Component;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ch.supertomcat.bh.clipboard.ClipboardObserver;
import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.gui.adder.AdderWindow;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.importexport.base.AdderImportBase;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.pic.URLList;
import ch.supertomcat.bh.queue.QueueManager;
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
	public void importLocalFiles(List<Path> files, String title) {
		if (files.isEmpty()) {
			return;
		}

		List<URL> urls = files.stream().filter(Files::exists).map(Path::toAbsolutePath).map(Path::toString).map(URL::new).collect(Collectors.toCollection(ArrayList::new));

		if (title == null) {
			title = Localization.getString("Unkown") + ": " + Localization.getString("Title");
		}
		String referrer = Localization.getString("Unkown") + ": " + Localization.getString("Referrer");

		if (!urls.isEmpty()) {
			// Open Download-Selection-Dialog
			AdderWindow adderpnl = new AdderWindow(parentComponent, true, new URLList(title, referrer, urls), logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver);
			adderpnl.init();
		}
	}
}

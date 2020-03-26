package ch.supertomcat.bh.importexport.base;

import java.awt.Component;

import ch.supertomcat.bh.clipboard.ClipboardObserver;
import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;

/**
 * Base class for Import classes, which need to use AdderPanel
 */
public abstract class AdderImportBase extends ImportExportBase {
	/**
	 * Log Manager
	 */
	protected final LogManager logManager;

	/**
	 * Queue Manager
	 */
	protected final QueueManager queueManager;

	/**
	 * Keyword Manager
	 */
	protected final KeywordManager keywordManager;

	/**
	 * Proxy Manager
	 */
	protected final ProxyManager proxyManager;

	/**
	 * Host Manager
	 */
	protected final HostManager hostManager;

	/**
	 * Clipboard Observer
	 */
	protected final ClipboardObserver clipboardObserver;

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
	public AdderImportBase(Component parentComponent, MainWindowAccess mainWindowAccess, LogManager logManager, QueueManager queueManager, KeywordManager keywordManager, ProxyManager proxyManager,
			SettingsManager settingsManager, HostManager hostManager, ClipboardObserver clipboardObserver) {
		super(parentComponent, mainWindowAccess, settingsManager);
		this.logManager = logManager;
		this.queueManager = queueManager;
		this.keywordManager = keywordManager;
		this.proxyManager = proxyManager;
		this.hostManager = hostManager;
		this.clipboardObserver = clipboardObserver;
	}
}

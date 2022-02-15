package ch.supertomcat.bh.systemtray;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.clipboard.ClipboardObserver;
import ch.supertomcat.bh.gui.GuiEvent;
import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.gui.queue.DownloadAddDialog;
import ch.supertomcat.bh.gui.queue.ParsePagesDialog;
import ch.supertomcat.bh.gui.update.UpdateWindow;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.importexport.ImportHTML;
import ch.supertomcat.bh.importexport.ImportLinkList;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.queue.IDownloadQueueManagerListener;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.BHSettingsListener;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.LookAndFeelSetting;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.update.UpdateManager;
import ch.supertomcat.bh.update.sources.httpxml.HTTPXMLUpdateSource;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.formatter.UnitFormatUtil;

/**
 * Class which handles the SystemTray
 */
public class SystemTrayTool implements IDownloadQueueManagerListener, BHSettingsListener {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(SystemTrayTool.class);

	/**
	 * Popupmenu
	 */
	private PopupMenu popup = new PopupMenu();

	/**
	 * Menuitem
	 */
	private MenuItem itemOpen = new MenuItem(Localization.getString("Open"));

	/**
	 * Menuitem
	 */
	private MenuItem itemDlStart = new MenuItem(Localization.getString("StartDownloads"));

	/**
	 * Menuitem
	 */
	private MenuItem itemDlStop = new MenuItem(Localization.getString("StopDownloads"));

	/**
	 * Menuitem
	 */
	private CheckboxMenuItem itemClipboard = new CheckboxMenuItem(Localization.getString("Clipboard"));

	/**
	 * Submenu of the popupmenu
	 */
	private Menu submenuImport = new Menu(Localization.getString("Import"));

	/**
	 * Menuitem
	 */
	private MenuItem itemImportHTML = new MenuItem(Localization.getString("ImportHTML"));

	/**
	 * Menuitem
	 */
	private MenuItem itemImportText = new MenuItem(Localization.getString("ImportText"));

	/**
	 * Menuepunkt
	 */
	private MenuItem itemImportLinks = new MenuItem(Localization.getString("ImportLinks"));

	/**
	 * Menuepunkt
	 */
	private MenuItem itemParseLinks = new MenuItem(Localization.getString("ParseLinks"));

	/**
	 * Menuitem
	 */
	private MenuItem itemUpdate = new MenuItem(Localization.getString("Update"));

	/**
	 * Menuitem
	 */
	private MenuItem itemExit = new MenuItem(Localization.getString("Exit"));

	/**
	 * Tray-Icon-Image
	 */
	private Image image = Icons.getBHImage("BH.png");

	/**
	 * Tray-Icon
	 */
	private TrayIcon trayIcon = null;

	/**
	 * SystemTray
	 */
	private SystemTray tray = null;

	/**
	 * Main Window
	 */
	private final JFrame mainWindow;

	/**
	 * Main Window Access
	 */
	private final MainWindowAccess mainWindowAccess;

	/**
	 * Queue Manager
	 */
	private final QueueManager queueManager;

	/**
	 * Download Queue Manager
	 */
	private final DownloadQueueManager downloadQueueManager;

	/**
	 * Keyword Manager
	 */
	private final KeywordManager keywordManager;

	/**
	 * Log Manager
	 */
	private final LogManager logManager;

	/**
	 * Proxy Manager
	 */
	protected final ProxyManager proxyManager;

	/**
	 * Settings Manager
	 */
	protected final SettingsManager settingsManager;

	/**
	 * Cookie Manager
	 */
	private final CookieManager cookieManager;

	/**
	 * Host Manager
	 */
	private final HostManager hostManager;

	/**
	 * Clipboard Observer
	 */
	private final ClipboardObserver clipboardObserver;

	/**
	 * GUI Event
	 */
	private final GuiEvent guiEvent;

	/**
	 * Constructor
	 * 
	 * @param mainWindow Main Window
	 * @param mainWindowAccess Main Window Access
	 * @param queueManager Queue Manager
	 * @param downloadQueueManager Download Queue Manager
	 * @param keywordManager Keyword Manager
	 * @param logManager Log Manager
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param cookieManager Cookie Manager
	 * @param hostManager Host Manager
	 * @param clipboardObserver Clipboard Observer
	 * @param guiEvent GUI Event
	 */
	public SystemTrayTool(JFrame mainWindow, MainWindowAccess mainWindowAccess, QueueManager queueManager, DownloadQueueManager downloadQueueManager, KeywordManager keywordManager,
			LogManager logManager, ProxyManager proxyManager, SettingsManager settingsManager, CookieManager cookieManager, HostManager hostManager, ClipboardObserver clipboardObserver,
			GuiEvent guiEvent) {
		this.mainWindow = mainWindow;
		this.mainWindowAccess = mainWindowAccess;
		this.queueManager = queueManager;
		this.downloadQueueManager = downloadQueueManager;
		this.keywordManager = keywordManager;
		this.logManager = logManager;
		this.proxyManager = proxyManager;
		this.settingsManager = settingsManager;
		this.cookieManager = cookieManager;
		this.hostManager = hostManager;
		this.clipboardObserver = clipboardObserver;
		this.guiEvent = guiEvent;
	}

	/**
	 * Checks if SystemTray is supported or not
	 * 
	 * @return True if supported
	 */
	public static boolean isTraySupported() {
		return SystemTray.isSupported();
	}

	/**
	 * Initializes the SystemTray, but does not show the trayicon
	 */
	public void init() {
		if (SystemTray.isSupported()) {
			// Add menuitems to the popup
			popup.add(itemOpen);
			popup.add(itemDlStart);
			popup.add(itemDlStop);
			popup.add(itemClipboard);
			popup.add(submenuImport);
			submenuImport.add(itemImportHTML);
			submenuImport.add(itemImportText);
			submenuImport.add(itemImportLinks);
			submenuImport.add(itemParseLinks);
			popup.add(itemUpdate);
			popup.add(itemExit);

			itemClipboard.setState(settingsManager.isCheckClipboard());

			// Create the TrayIcon
			trayIcon = new TrayIcon(image, getClipboardStateText() + Localization.getString("SystemTrayTool_Sleeping"), popup);
			trayIcon.setImageAutoSize(true);

			// Get the SystemTray
			tray = SystemTray.getSystemTray();
		}
	}

	/**
	 * Now, here we add the icon to the SystemTray and register Listeners for the menuitems
	 */
	public void showTrayIcon() {
		if (SystemTray.isSupported()) {
			itemOpen.addActionListener(e -> actionOpen());
			itemDlStart.addActionListener(e -> actionDownloadStart());
			itemDlStop.addActionListener(e -> actionDownloadStop());
			itemClipboard.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					actionClipboard();
				}
			});
			itemImportLinks.addActionListener(e -> actionImportLinks());
			itemParseLinks.addActionListener(e -> actionParseLinks());
			itemImportHTML.addActionListener(e -> actionImportHTML());
			itemImportText.addActionListener(e -> actionImportText());
			itemUpdate.addActionListener(e -> actionUpdate());
			itemExit.addActionListener(e -> actionExit());

			trayIcon.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 1 && e.getButton() == 1) {
						if (!mainWindow.isVisible()) {
							mainWindow.setVisible(true);
							mainWindow.toFront();
						} else {
							guiEvent.hideWindow();
						}
					}
				}
			});

			try {
				tray.add(trayIcon);
				downloadQueueManager.addDownloadQueueManagerListener(this);
				settingsManager.addSettingsListener(this);
			} catch (AWTException e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
	}

	/**
	 * Removes the icon from the SystemTray
	 */
	public void remove() {
		if (tray != null) {
			tray.remove(trayIcon);
			downloadQueueManager.removeDownloadQueueManagerListener(this);
		}
	}

	/**
	 * Returns the statustext for the clipboard monitoring
	 * 
	 * @return Statustext for the clipboard monitoring
	 */
	private String getClipboardStateText() {
		String retval = Localization.getString("ClipboardObserve") + " ";
		if (settingsManager.isCheckClipboard()) {
			retval += Localization.getString("On");
		} else {
			retval += Localization.getString("Off");
		}
		retval += "\n";
		return retval;
	}

	private String getSystemTrayToolTipText() {
		DownloadQueueManager queue = downloadQueueManager;
		if (queue.isDownloading()) {
			String downloadRate = UnitFormatUtil.getBitrateString(queue.getTotalDownloadBitrate());
			if (downloadRate.length() == 0) {
				downloadRate = Localization.getString("NotAvailable");
			}
			return getClipboardStateText() + Localization.getString("Queue") + ": " + queue.getQueueSize() + " | " + Localization.getString("FreeSlots") + ": " + queue.getOpenSlots() + "/"
					+ queue.getMaxConnectionCount() + " | " + Localization.getString("DownloadBitrate") + ": " + downloadRate;
		} else {
			return getClipboardStateText() + Localization.getString("SystemTrayTool_Sleeping");
		}
	}

	private void actionOpen() {
		mainWindow.setVisible(true);
		mainWindow.toFront();
	}

	private void actionDownloadStart() {
		queueManager.startDownload();
	}

	private void actionDownloadStop() {
		queueManager.stopDownload();
	}

	private void actionClipboard() {
		settingsManager.setCheckClipboard(itemClipboard.getState());
	}

	private void actionUpdate() {
		if (downloadQueueManager.isDownloading()) {
			JOptionPane.showMessageDialog(null, Localization.getString("UpdatesWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
		} else {
			UpdateWindow update = new UpdateWindow(new UpdateManager(new HTTPXMLUpdateSource(proxyManager), guiEvent), mainWindow, queueManager, keywordManager, settingsManager, hostManager, guiEvent);
			update.setVisible(true);
			update.toFront();
		}
	}

	private void actionExit() {
		guiEvent.exitApp(false, false);
	}

	private void actionImportHTML() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				new ImportHTML(mainWindow, mainWindowAccess, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, cookieManager, clipboardObserver).importHTML();
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	private void actionImportLinks() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				DownloadAddDialog dlg = new DownloadAddDialog(mainWindow, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver);
				dlg.setVisible(true);
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	private void actionParseLinks() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				ParsePagesDialog dlg = new ParsePagesDialog(mainWindow, mainWindowAccess, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, cookieManager, clipboardObserver);
				dlg.setVisible(true);
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	private void actionImportText() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				new ImportLinkList(mainWindow, mainWindowAccess, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver).importLinkList();
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	@Override
	public void queueChanged(int queue, int openSlots, int maxSlots) {
		if (trayIcon != null) {
			trayIcon.setToolTip(getSystemTrayToolTipText());
		}
	}

	@Override
	public void downloadsComplete(int queue, int openSlots, int maxSlots) {
		if (trayIcon != null && settingsManager.isDownloadsCompleteNotification()) {
			String text = queueManager.getQueueSize() + " " + Localization.getString("DownloadsLeftInQueue");
			trayIcon.displayMessage(Localization.getString("DownloadsComplete"), text, TrayIcon.MessageType.INFO);
		}
	}

	@Override
	public void settingsChanged() {
		if (trayIcon != null) {
			trayIcon.setToolTip(getSystemTrayToolTipText());
		}
		itemClipboard.setState(settingsManager.isCheckClipboard());
	}

	@Override
	public void lookAndFeelChanged(LookAndFeelSetting lookAndFeel) {
		// Nothing to do
	}

	@Override
	public void sessionDownloadedBytesChanged(long count) {
		// Nothing to do
	}

	@Override
	public void sessionDownloadedFilesChanged(int count) {
		// Nothing to do
	}

	@Override
	public void totalDownloadRateCalculated(double downloadRate) {
		if (trayIcon != null) {
			trayIcon.setToolTip(getSystemTrayToolTipText());
		}
	}

	@Override
	public void queueEmpty() {
		// Nothing to do
	}
}

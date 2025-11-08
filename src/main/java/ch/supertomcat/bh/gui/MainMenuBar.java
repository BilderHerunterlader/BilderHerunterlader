package ch.supertomcat.bh.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import ch.supertomcat.bh.cookies.CookieManager;
import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.gui.settings.SettingsDialog;
import ch.supertomcat.bh.gui.update.UpdateWindow;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.update.UpdateManager;
import ch.supertomcat.bh.update.sources.httpxml.HTTPXMLUpdateSource;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.FileExplorerUtil;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;
import jakarta.xml.bind.JAXBException;

/**
 * Main Menu Bar
 */
public class MainMenuBar {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * MenuBar
	 */
	private JMenuBar mb = new JMenuBar();

	/**
	 * Menu
	 */
	private JMenu menuFile = new JMenu(Localization.getString("File"));

	/**
	 * MenuItem
	 */
	private JMenuItem itemExit = new JMenuItem(Localization.getString("Exit"), Icons.getTangoSVGIcon("actions/system-log-out.svg", 16));

	/**
	 * Menu
	 */
	private JMenu menuSettings = new JMenu(Localization.getString("Settings"));

	/**
	 * MenuItem
	 */
	private JMenuItem itemSettings = new JMenuItem(Localization.getString("Settings"), Icons.getTangoSVGIcon("categories/preferences-system.svg", 16));

	/**
	 * Menu
	 */
	private JMenu menuHelp = new JMenu(Localization.getString("Help"));

	/**
	 * MenuItem
	 */
	private JMenuItem itemUpdate = new JMenuItem(Localization.getString("Update"), Icons.getTangoSVGIcon("apps/system-software-update.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemLogFolder = new JMenuItem(Localization.getString("OpenLogFolder"), Icons.getTangoSVGIcon("places/folder.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemTutorial = new JMenuItem(Localization.getString("Tutorial"), Icons.getTangoSVGIcon("apps/internet-web-browser.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemReportIssueEMail = new JMenuItem(Localization.getString("ReportIssueEMail"), Icons.getTangoSVGIcon("actions/mail-message-new.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemReportIssueGithub = new JMenuItem(Localization.getString("ReportIssueGithub"), Icons.getTangoSVGIcon("apps/internet-web-browser.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemAbout = new JMenuItem(Localization.getString("About"), Icons.getTangoSVGIcon("apps/help-browser.svg", 16));

	/**
	 * Constructor
	 * 
	 * @param parentWindow Parent Window
	 * @param mainWindowAccess Main Window Access
	 * @param logManager Log Manager
	 * @param downloadQueueManager Download Queue Manager
	 * @param queueManager Queue Manager
	 * @param keywordManager Keyword Manager
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param cookieManager Cookie Manager
	 * @param hostManager Host Manager
	 * @param guiEvent GUI Event
	 */
	public MainMenuBar(JFrame parentWindow, MainWindowAccess mainWindowAccess, LogManager logManager, DownloadQueueManager downloadQueueManager, QueueManager queueManager,
			KeywordManager keywordManager, ProxyManager proxyManager, SettingsManager settingsManager, CookieManager cookieManager, HostManager hostManager, GuiEvent guiEvent) {
		menuFile.add(itemExit);

		menuSettings.add(itemSettings);

		menuHelp.add(itemUpdate);
		menuHelp.add(itemLogFolder);
		menuHelp.add(itemTutorial);
		menuHelp.add(itemReportIssueEMail);
		menuHelp.add(itemReportIssueGithub);
		menuHelp.add(itemAbout);

		itemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		itemSettings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
		itemUpdate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.ALT_MASK));
		itemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, ActionEvent.SHIFT_MASK));
		itemTutorial.setAccelerator(KeyStroke.getKeyStroke("F1"));

		itemExit.addActionListener(e -> guiEvent.exitApp(false, false));

		itemSettings.addActionListener(e -> new SettingsDialog(parentWindow, mainWindowAccess, proxyManager, settingsManager, cookieManager, hostManager));

		itemUpdate.addActionListener(e -> {
			if (downloadQueueManager.isDownloading()) {
				JOptionPane.showMessageDialog(null, Localization.getString("UpdatesWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			try {
				HTTPXMLUpdateSource updateSource = new HTTPXMLUpdateSource(proxyManager);
				UpdateManager updateManager = new UpdateManager(updateSource, hostManager, guiEvent);
				UpdateWindow update = new UpdateWindow(updateManager, parentWindow, queueManager, keywordManager, settingsManager, guiEvent);
				update.setVisible(true);
				update.toFront();
			} catch (IOException | SAXException | JAXBException ex) {
				logger.error("Could not initiliaze update source", ex);
				JOptionPane.showMessageDialog(null, "Failed to initialize update source", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		itemLogFolder.addActionListener(e -> {
			Path logDir = Paths.get(ApplicationProperties.getProperty(ApplicationMain.LOGS_PATH));
			FileExplorerUtil.openDirectory(logDir);
		});

		itemTutorial.addActionListener(e -> {
			String url = ApplicationProperties.getProperty("TutorialURL");
			FileExplorerUtil.openURL(url);
		});

		itemReportIssueEMail.addActionListener(e -> {
			String emailAddress = ApplicationProperties.getProperty("MailAddress");
			FileExplorerUtil.openEMail(emailAddress);
		});

		itemReportIssueGithub.addActionListener(e -> {
			String url = ApplicationProperties.getProperty("ReportIssueURL");
			FileExplorerUtil.openURL(url);
		});

		itemAbout.addActionListener(e -> new BHAboutDialog(parentWindow, settingsManager));

		mb.add(menuFile);
		mb.add(menuSettings);
		mb.add(menuHelp);
	}

	/**
	 * @return JMenuBar
	 */
	public JMenuBar getJMenuBar() {
		return mb;
	}
}

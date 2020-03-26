package ch.supertomcat.bh.gui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.settings.Settings;
import ch.supertomcat.bh.gui.update.UpdateWindow;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.update.UpdateManager;
import ch.supertomcat.bh.update.sources.httpxml.HTTPXMLUpdateSource;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Main Menu Bar
 */
public class MainMenuBar {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(MainMenuBar.class);

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
	private JMenuItem itemExit = new JMenuItem(Localization.getString("Exit"), Icons.getTangoIcon("actions/system-log-out.png", 16));

	/**
	 * Menu
	 */
	private JMenu menuSettings = new JMenu(Localization.getString("Settings"));

	/**
	 * MenuItem
	 */
	private JMenuItem itemSettings = new JMenuItem(Localization.getString("Settings"), Icons.getTangoIcon("categories/preferences-system.png", 16));

	/**
	 * Menu
	 */
	private JMenu menuHelp = new JMenu(Localization.getString("Help"));

	/**
	 * MenuItem
	 */
	private JMenuItem itemUpdate = new JMenuItem(Localization.getString("Update"), Icons.getTangoIcon("apps/system-software-update.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemLogFolder = new JMenuItem(Localization.getString("OpenLogFolder"), Icons.getTangoIcon("places/folder.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemTutorial = new JMenuItem(Localization.getString("Tutorial"), Icons.getTangoIcon("apps/internet-web-browser.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemAbout = new JMenuItem(Localization.getString("About"), Icons.getTangoIcon("apps/help-browser.png", 16));

	/**
	 * lblLogFile
	 */
	private JLabel lblLogFile = new JLabel(Localization.getString("ActiveDownloadLogFile") + " ");

	/**
	 * cmbLogFile
	 */
	private JComboBox<String> cmbLogFile = new JComboBox<>();

	/**
	 * Constructor
	 * 
	 * @param parentWindow Parent Window
	 * @param mainWindowAccess Main Window Access
	 * @param logManager Log Manager
	 * @param downloadQueueManager Download Queue Manager
	 * @param queueManager Queue Manager
	 * @param keywordManager Keyword Manager
	 * @param proxyManger Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param cookieManager Cookie Manager
	 * @param hostManager Host Manager
	 * @param guiEvent GUI Event
	 */
	public MainMenuBar(JFrame parentWindow, MainWindowAccess mainWindowAccess, LogManager logManager, DownloadQueueManager downloadQueueManager, QueueManager queueManager,
			KeywordManager keywordManager, ProxyManager proxyManger, SettingsManager settingsManager, CookieManager cookieManager, HostManager hostManager, GuiEvent guiEvent) {
		menuFile.add(itemExit);

		menuSettings.add(itemSettings);

		menuHelp.add(itemUpdate);
		menuHelp.add(itemLogFolder);
		menuHelp.add(itemTutorial);
		menuHelp.add(itemAbout);

		itemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		itemSettings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
		itemUpdate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.ALT_MASK));
		itemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, ActionEvent.SHIFT_MASK));
		itemTutorial.setAccelerator(KeyStroke.getKeyStroke("F1"));

		itemExit.addActionListener(e -> guiEvent.exitApp(false));

		itemSettings.addActionListener(e -> new Settings(parentWindow, mainWindowAccess, proxyManger, settingsManager, cookieManager, hostManager));

		itemUpdate.addActionListener(e -> {
			if (downloadQueueManager.isDownloading()) {
				JOptionPane.showMessageDialog(null, Localization.getString("UpdatesWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				UpdateWindow update = new UpdateWindow(new UpdateManager(new HTTPXMLUpdateSource(proxyManger), guiEvent), parentWindow, queueManager, keywordManager, settingsManager, hostManager, guiEvent);
				update.setVisible(true);
				update.toFront();
			}
		});
		itemLogFolder.addActionListener(e -> {
			File logDir = new File(ApplicationProperties.getProperty("LogsPath"));
			try {
				Desktop.getDesktop().open(logDir);
			} catch (IOException e1) {
				logger.error("Could not open Directory: {}", logDir.getAbsolutePath(), e1);
			}
		});
		itemTutorial.addActionListener(e -> {
			String url = ApplicationProperties.getProperty("TutorialURL");
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (IOException | URISyntaxException ex) {
					logger.error("Could not open URL: {}", url, ex);
				}
			} else {
				logger.error("Could not open URL, because Desktop is not supported: {}", url);
			}
		});
		itemAbout.addActionListener(e -> new About(parentWindow, settingsManager));

		String logFiles[] = logManager.getAvailableLogFileNames();
		for (int i = 0; i < logFiles.length; i++) {
			cmbLogFile.addItem(logFiles[i]);
		}
		int currentLogFileIndex = logManager.getCurrentLogFileIndexForArray(logFiles);
		cmbLogFile.setSelectedIndex(currentLogFileIndex);
		cmbLogFile.addActionListener(e -> {
			settingsManager.setCurrentDownloadLogFile((String)cmbLogFile.getSelectedItem());
			settingsManager.writeSettings(true);
		});
		cmbLogFile.setMaximumSize(new Dimension(133, 20));
		cmbLogFile.setFocusable(false);
		if (logFiles.length == 1) {
			lblLogFile.setVisible(false);
			cmbLogFile.setVisible(false);
		}

		mb.add(menuFile);
		mb.add(menuSettings);
		mb.add(menuHelp);
		mb.add(Box.createGlue());
		mb.add(lblLogFile);
		mb.add(cmbLogFile);
		mb.add(Box.createGlue());
	}

	/**
	 * @return JMenuBar
	 */
	public JMenuBar getJMenuBar() {
		return mb;
	}
}

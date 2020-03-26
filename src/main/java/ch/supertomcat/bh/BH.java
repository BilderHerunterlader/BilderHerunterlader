package ch.supertomcat.bh;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.clipboard.ClipboardObserver;
import ch.supertomcat.bh.clipboard.ClipboardObserverListener;
import ch.supertomcat.bh.gui.GuiEvent;
import ch.supertomcat.bh.gui.IGuiEventListener;
import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.gui.Main;
import ch.supertomcat.bh.gui.update.UpdateListener;
import ch.supertomcat.bh.gui.update.UpdateWindow;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.importexport.ImportURL;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.queue.DownloadQueueManagerRestrictions;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.BHSettingsListener;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.systemtray.SystemTrayTool;
import ch.supertomcat.bh.transmitter.TransmitterHTTP;
import ch.supertomcat.bh.transmitter.TransmitterHelper;
import ch.supertomcat.bh.transmitter.TransmitterSocket;
import ch.supertomcat.bh.update.UpdateManager;
import ch.supertomcat.bh.update.sources.httpxml.HTTPXMLUpdateSource;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.application.ApplicationUtil;
import ch.supertomcat.supertomcatutils.gui.Localization;
import fi.iki.elonen.NanoHTTPD;

/**
 * Class which contains the main-Method
 */
public abstract class BH {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * SystemTray
	 */
	private SystemTrayTool stt;

	/**
	 * Main-Window
	 */
	private Main main;

	/**
	 * Udpate-Window
	 */
	private UpdateWindow update;

	/**
	 * Programm completely started
	 */
	private boolean started = false;

	/**
	 * Flag which will be set to true by exitBH
	 * If the Flag is true, no Shutdown-Thread is created or if it is created it is not started
	 */
	private boolean exited = false;

	/**
	 * TransmitterSocket
	 */
	private TransmitterSocket transmitterSocket = null;

	/**
	 * TransmitterHTTP
	 */
	private TransmitterHTTP transmitterHTTP = null;

	/**
	 * Queue Manager
	 */
	private QueueManager queueManager;

	/**
	 * Download Queue Manager
	 */
	private DownloadQueueManager downloadQueueManager;

	/**
	 * Settings Manager
	 */
	private SettingsManager settingsManager;

	/**
	 * Keyword Manager
	 */
	private KeywordManager keywordManager;

	/**
	 * Host Manager
	 */
	private HostManager hostManager;

	/**
	 * Constructor
	 */
	public BH() {
		GuiEvent guiEvent = new GuiEvent();
		guiEvent.addListener(new IGuiEventListener() {
			@Override
			public void exitApp(boolean restart) {
				if (!started) {
					return;
				}
				if (!checkExitAllowed()) {
					return;
				}
				removeShutdownHook();
				exitBH(restart);
			}

			@Override
			public void hideWindow() {
				if (main != null) {
					main.setVisible(false);
				}
			}
		});
		guiEvent.addUpdateListener(new UpdateListener() {
			@Override
			public void updateWindowOpened() {
				if (transmitterSocket != null) {
					transmitterSocket.setAcceptConnections(false);
				}
				if (transmitterHTTP != null) {
					transmitterHTTP.setAcceptConnections(false);
				}
			}

			@Override
			public void updateWindowClosed(boolean updateRunned, boolean updateSuccessfull) {
				if (transmitterSocket != null && (updateRunned == false || (updateRunned == true && updateSuccessfull == false))) {
					transmitterSocket.setAcceptConnections(true);
				}
				if (transmitterHTTP != null && (updateRunned == false || (updateRunned == true && updateSuccessfull == false))) {
					transmitterHTTP.setAcceptConnections(true);
				}
			}
		});

		// Read the settings from settings file
		settingsManager = new SettingsManager(ApplicationProperties.getProperty("SettingsPath"), "settings.xml");
		settingsManager.readSettings();
		if (settingsManager.isLanguageFirstRun()) {
			// If the application is started at first time, the user must select the language
			String options[] = { "English", "Deutsch" };
			// Display a frame, so that BH already shows up in the taskbar and can be switched to. Otherwise the user might not see that there was a dialog open
			JFrame frame = null;
			try {
				frame = ApplicationUtil.createInvisibleFrame("BH", Icons.getBHImage("BH.png"));
				int ret = JOptionPane.showOptionDialog(frame, "Choose a language", "Language", JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (ret == 0) {
					settingsManager.setLanguage("en_EN");
				} else if (ret == 1) {
					settingsManager.setLanguage("de_DE");
				}
			} finally {
				if (frame != null) {
					frame.dispose();
				}
			}
		}

		/*
		 * No try to change the look and feel if needed
		 */
		int laf = settingsManager.getLookAndFeel();
		if (laf > 0) {
			try {
				String strLAF = SettingsManager.LAF_CLASSPATHES[settingsManager.getLookAndFeel()];
				UIManager.setLookAndFeel(strLAF);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		// Initialize the localized Strings
		String language = "en";
		String country = "EN";
		if (settingsManager.getLanguage().equals("de_DE")) {
			language = "de";
			country = "DE";
		}
		Localization.init("ch.supertomcat.bh.BH", language, country);

		// Initalized too fast as it would be worth to execute parallel
		ProxyManager proxyManager = new ProxyManager(settingsManager);
		CookieManager cookieManager = new CookieManager(settingsManager);
		LogManager logManager = new LogManager(settingsManager);
		DownloadQueueManagerRestrictions restrictions = new DownloadQueueManagerRestrictions();
		hostManager = new HostManager(main, restrictions, proxyManager, settingsManager, cookieManager);
		downloadQueueManager = new DownloadQueueManager(restrictions, proxyManager, settingsManager, cookieManager, hostManager);

		int threadCount = settingsManager.getThreadCount();
		if (threadCount < 1) {
			threadCount = 1;
		} else if (threadCount > 2) {
			threadCount = 2;
		}

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		List<Callable<Object>> tasks = new ArrayList<>();
		tasks.add(Executors.callable(new Runnable() {
			@Override
			public void run() {
				queueManager = new QueueManager(downloadQueueManager, logManager, settingsManager);
			}
		}));
		tasks.add(Executors.callable(new Runnable() {
			@Override
			public void run() {
				keywordManager = new KeywordManager(settingsManager);
			}
		}));

		// Wait for all threads to complete
		try {
			executor.invokeAll(tasks);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		executor.shutdown();

		ClipboardObserver clipboardObserver = new ClipboardObserver(settingsManager);
		main = new Main(settingsManager, logManager, queueManager, downloadQueueManager, keywordManager, proxyManager, cookieManager, hostManager, clipboardObserver, guiEvent);
		if (SystemTrayTool.isTraySupported()) {
			/*
			 * We can only use the SystemTray on Java 1.6 or above and the user wants this
			 * Because there is a parameter for the prorgramm to make sure SystemTray is not used.
			 * Because i was reported by a user, SystemTray could make trouble on some systems.
			 */

			// Init SystemTray
			stt = new SystemTrayTool(main, main, queueManager, downloadQueueManager, keywordManager, logManager, proxyManager, settingsManager, cookieManager, hostManager, clipboardObserver, guiEvent);
			stt.init();
			/*
			 * If SystemTray is used, we show the window but hide it just again.
			 * I can't remember exactly why i do this. But it had to do with the focus
			 * of Download-Selection-Dialogs or the Focus of Main-Window.
			 */
			EventQueue.invokeLater(() -> {
				main.setVisible(true);
				main.setVisible(false);
			});

			// Add icon to the SystemTray
			stt.showTrayIcon();
		} else {
			// If SystemTray is not used bring the main window to the front and request the focus
			EventQueue.invokeLater(() -> {
				main.setVisible(true);
				main.toFront();
				main.requestFocus();
			});
		}

		clipboardObserver.addListener(new ClipboardObserverListener() {
			/**
			 * URL Importer
			 */
			private ImportURL urlImporter = new ImportURL(main, main, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, cookieManager, clipboardObserver);

			@Override
			public void linksDetected(List<String> links) {
				for (String link : links) {
					// let the import class download the container page and get the links from it
					urlImporter.importURL(link, link, false);
				}
			}
		});

		started = true;

		/*
		 * When the program was updated to a new version, there are maybe
		 * new options, but those are not saved at the moment. So we must
		 * do it here. Because we don't save the settings on program exit,
		 * because some users had lost the settings, because they let
		 * the program running all the time, and when they shutdown windows
		 * the shutdown-Thread can't detect that or the exit-Method takes to
		 * long. So there was an empty settings-file. So its better to not
		 * save the settings at program exit, but every time the settings are
		 * changed and definitly here!
		 */
		settingsManager.writeSettings(true);

		/*
		 * Start the TransmitterSocket
		 */
		TransmitterHelper transmitterHelper = new TransmitterHelper(main, main, queueManager, logManager, keywordManager, proxyManager, settingsManager, cookieManager, hostManager, clipboardObserver);
		transmitterSocket = new TransmitterSocket(transmitterHelper);
		Thread tThread = new Thread(transmitterSocket, "TransmitterSocket-Thread");
		tThread.start();

		// Start the TransmitterHTTP
		initializeTransmitterHTTP(transmitterHelper);
		settingsManager.addSettingsListener(new BHSettingsListener() {
			@Override
			public void settingsChanged() {
				initializeTransmitterHTTP(transmitterHelper);
				if (settingsManager.isCheckClipboard()) {
					clipboardObserver.init();
				} else {
					clipboardObserver.stop();
				}
			}

			@Override
			public void lookAndFeelChanged(int lookAndFeel) {
				// Nothing to do
			}
		});

		if (settingsManager.isUpdates()) {
			// If the user wants, we check if updates are available
			UpdateManager updateManager = new UpdateManager(new HTTPXMLUpdateSource(proxyManager), guiEvent);
			update = new UpdateWindow(updateManager, main, queueManager, keywordManager, settingsManager, hostManager, guiEvent);
			if (update.checkForUpdates()) {
				// If Updates are available, show the Update-Window
				EventQueue.invokeLater(() -> {
					update.setVisible(true);
					update.toFront();
				});
			} else {
				// If not, dispose it
				EventQueue.invokeLater(() -> {
					update.dispose();
				});
				update = null;
			}
		}
	}

	/**
	 * Check if exit is allowed (No downloads are running)
	 * 
	 * @return True if exit is allowed, false otherwise
	 */
	private synchronized boolean checkExitAllowed() {
		if (downloadQueueManager != null && downloadQueueManager.isDownloading()) {
			JOptionPane.showMessageDialog(null, Localization.getString("ExitWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * Stop Downloads. This method should only be used when exiting by shutdown hook
	 */
	private synchronized void stopDownloads() {
		if (queueManager != null) {
			// Call stop downloads twice for immediate stopping (stops also running downloads)
			queueManager.stopDownload();
			queueManager.stopDownload();
		}
	}

	/**
	 * Remove Shutdown Hook
	 */
	protected abstract void removeShutdownHook();

	/**
	 * Exit BH Now
	 * 
	 * @param restart True if restart, false otherwise
	 */
	protected abstract void exitNow(boolean restart);

	private synchronized void exitBH(boolean restart) {
		exited = true;

		if (transmitterSocket != null) {
			logger.debug("Stop TransmitterSocket");
			transmitterSocket.setAcceptConnections(false);
			transmitterSocket = null;
		}
		if (transmitterHTTP != null) {
			logger.debug("Stop TransmitterHTTP");
			transmitterHTTP.setAcceptConnections(false);
			transmitterHTTP.stop();
			transmitterHTTP = null;
		}

		if (update != null) {
			update.setVisible(false);
			update.dispose();
		}
		main.setVisible(false);

		if (restart == false) {
			// Save and close databases
			if (queueManager != null) {
				queueManager.closeDatabase();
			}
			keywordManager.closeDatabase();
		}

		if (stt != null) {
			logger.debug("Removing Trayicon");
			// Remove icon from SytemTray
			stt.remove();
		}

		exitNow(restart);
	}

	private synchronized void initializeTransmitterHTTP(TransmitterHelper transmitterHelper) {
		int port = settingsManager.getWebExtensionPort();
		if (transmitterHTTP != null) {
			if (transmitterHTTP.getListeningPort() == port && transmitterHTTP.isAlive()) {
				/*
				 * Transmitter is already open for the correct port and still alive, so nothing to do
				 */
				return;
			}
			transmitterHTTP.stop();
			transmitterHTTP = null;
		}
		transmitterHTTP = new TransmitterHTTP(port, transmitterHelper);
		try {
			transmitterHTTP.start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
		} catch (IOException e) {
			logger.error("Could not start HTTP Server", e);
		}
	}

	private static void executeDeleteUpdates() {
		Logger logger = LoggerFactory.getLogger(BH.class);
		File deleteUpdateFile = new File(ApplicationProperties.getProperty("ApplicationPath"), "delete_update.txt");
		if (!deleteUpdateFile.exists()) {
			return;
		}
		try (FileInputStream in = new FileInputStream(deleteUpdateFile); BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					String deletePath;
					String applicationPath = ApplicationProperties.getProperty("ApplicationPath");
					if (line.startsWith(applicationPath)) {
						deletePath = line;
					} else {
						deletePath = applicationPath + line;
					}

					File fDelete = new File(deletePath);
					if (fDelete.isDirectory()) {
						deleteDirectory(fDelete);
					} else {
						try {
							Files.deleteIfExists(fDelete.toPath());
							logger.info("Delete success: " + line);
						} catch (IOException e) {
							logger.error("Could not delete: {}", fDelete.getAbsolutePath(), e);
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("Could not read file: {}", deleteUpdateFile.getAbsolutePath(), e);
		}
		deleteUpdateFile.delete();
	}

	private static void deleteDirectory(File dir) throws IOException {
		Path directory = dir.toPath();
		if (Files.exists(directory)) {
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
					Files.delete(path);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path directory, IOException ioException) throws IOException {
					Files.delete(directory);
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}

	/**
	 * Main-Method
	 * 
	 * @param args Arguments
	 */
	public static void main(String[] args) {
		List<String> additionalPaths = Arrays.asList("DatabasePath", "SettingsPath", "DownloadLogPath", "DownloadPath");
		ApplicationMain applicationMain = new ApplicationMain("BH", null, true, true, BH.class, additionalPaths) {
			/**
			 * BH
			 */
			private BH bh = null;

			@Override
			protected void main(String[] args) {
				// Delete Updates
				executeDeleteUpdates();

				// Good, now let BH really start
				bh = new BH() {

					@Override
					protected void removeShutdownHook() {
						removeAllShutdownHooks();
					}

					@Override
					protected void exitNow(boolean restart) {
						exit(restart);
					}
				};

				// Create and register the Shutdown-Thread
				addDefaultShutdownHook();
			}

			@Override
			protected void shutdownHookExit() {
				if (bh != null && !bh.exited) {
					bh.stopDownloads();
					bh.exitBH(false);
				}
			}
		};
		applicationMain.start(args);
	}
}

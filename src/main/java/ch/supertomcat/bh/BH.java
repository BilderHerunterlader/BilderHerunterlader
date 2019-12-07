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
import ch.supertomcat.bh.gui.GuiEvent;
import ch.supertomcat.bh.gui.IGuiEventListener;
import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.gui.Main;
import ch.supertomcat.bh.gui.update.UpdateListener;
import ch.supertomcat.bh.gui.update.UpdateWindow;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.ISettingsListener;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.systemtray.SystemTrayTool;
import ch.supertomcat.bh.transmitter.TransmitterHTTP;
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
	 * Constructor
	 */
	public BH() {
		GuiEvent.instance().addListener(new IGuiEventListener() {
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
		});
		GuiEvent.instance().addUpdateListener(new UpdateListener() {
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
		SettingsManager.instance().readSettings();
		if (SettingsManager.instance().isLanguageFirstRun()) {
			// If the application is started at first time, the user must select the language
			String options[] = { "English", "Deutsch" };
			// Display a frame, so that BH already shows up in the taskbar and can be switched to. Otherwise the user might not see that there was a dialog open
			JFrame frame = null;
			try {
				frame = ApplicationUtil.createInvisibleFrame("BH", Icons.getBHImage("BH.png"));
				int ret = JOptionPane.showOptionDialog(frame, "Choose a language", "Language", JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (ret == 0) {
					SettingsManager.instance().setLanguage("en_EN");
				} else if (ret == 1) {
					SettingsManager.instance().setLanguage("de_DE");
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
		int laf = SettingsManager.instance().getLookAndFeel();
		if (laf > 0) {
			try {
				String strLAF = SettingsManager.LAF_CLASSPATHES[SettingsManager.instance().getLookAndFeel()];
				UIManager.setLookAndFeel(strLAF);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		// Initialize the localized Strings
		String language = "en";
		String country = "EN";
		if (SettingsManager.instance().getLanguage().equals("de_DE")) {
			language = "de";
			country = "DE";
		}
		Localization.init("ch.supertomcat.bh.BH", language, country);

		// Initialize Managers
		DownloadQueueManager.instance(); // Don't initialize this parallel, because HostManager will access it.
		LogManager.instance(); // Initalized too fast as it would be worth to execute parallel

		int threadCount = SettingsManager.instance().getThreadCount();
		if (threadCount < 1) {
			threadCount = 1;
		} else if (threadCount > 3) {
			threadCount = 3;
		}

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		List<Callable<Object>> tasks = new ArrayList<>();
		tasks.add(Executors.callable(new Runnable() {
			@Override
			public void run() {
				QueueManager.instance();
			}
		}));
		tasks.add(Executors.callable(new Runnable() {
			@Override
			public void run() {
				KeywordManager.instance();
			}
		}));
		tasks.add(Executors.callable(new Runnable() {
			@Override
			public void run() {
				HostManager.instance();
			}
		}));

		// Wait for all threads to complete
		try {
			executor.invokeAll(tasks);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		executor.shutdown();

		if (SystemTrayTool.isTraySupported()) {
			/*
			 * We can only use the SystemTray on Java 1.6 or above and the user wants this
			 * Because there is a parameter for the prorgramm to make sure SystemTray is not used.
			 * Because i was reported by a user, SystemTray could make trouble on some systems.
			 */

			// Init SystemTray
			stt = new SystemTrayTool();
			stt.init();
			main = Main.instance();
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
			main = Main.instance();
			// If SystemTray is not used bring the main window to the front and request the focus
			EventQueue.invokeLater(() -> {
				main.setVisible(true);
				main.toFront();
				main.requestFocus();
			});
		}

		if (SettingsManager.instance().isCheckClipboard()) {
			// Activate Clipboard Monitoring
			ClipboardObserver.instance();
		}

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
		SettingsManager.instance().writeSettings(true);

		/*
		 * Start the TransmitterSocket
		 */
		transmitterSocket = new TransmitterSocket();
		Thread tThread = new Thread(transmitterSocket, "TransmitterSocket-Thread");
		tThread.start();

		// Start the TransmitterHTTP
		initializeTransmitterHTTP();
		SettingsManager.instance().addSettingsListener(new ISettingsListener() {
			@Override
			public void settingsChanged() {
				initializeTransmitterHTTP();
			}
		});

		if (SettingsManager.instance().isUpdates()) {
			// If the user wants, we check if updates are available
			update = new UpdateWindow(new UpdateManager(new HTTPXMLUpdateSource()), Main.instance());
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
		if (DownloadQueueManager.instance().isDownloading()) {
			JOptionPane.showMessageDialog(null, Localization.getString("ExitWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * Stop Downloads. This method should only be used when exiting by shutdown hook
	 */
	private synchronized void stopDownloads() {
		// Call stop downloads twice for immediate stopping (stops also running downloads)
		QueueManager.instance().stopDownload();
		QueueManager.instance().stopDownload();
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
		Main.instance().setVisible(false);

		if (restart == false) {
			// Save and close databases
			QueueManager.instance().closeDatabase();
			KeywordManager.instance().closeDatabase();
		}

		if (stt != null) {
			logger.debug("Removing Trayicon");
			// Remove icon from SytemTray
			stt.remove();
		}

		exitNow(restart);
	}

	private synchronized void initializeTransmitterHTTP() {
		int port = SettingsManager.instance().getWebExtensionPort();
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
		transmitterHTTP = new TransmitterHTTP(port);
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

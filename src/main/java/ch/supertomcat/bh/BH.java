package ch.supertomcat.bh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
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
import ch.supertomcat.supertomcattools.applicationtool.ApplicationProperties;
import ch.supertomcat.supertomcattools.applicationtool.ApplicationTool;
import ch.supertomcat.supertomcattools.fileiotools.FileTool;
import ch.supertomcat.supertomcattools.guitools.Localization;
import fi.iki.elonen.NanoHTTPD;

/**
 * Class which contains the main-Method
 */
public class BH {
	/**
	 * Logger for this class
	 */
	private static Logger logger;

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
	 * Path of the folder which contains the lockfile
	 */
	private static String strLockFilePath = System.getProperty("user.home") + FileTool.FILE_SEPERATOR + ".BH" + FileTool.FILE_SEPERATOR;

	/**
	 * Path and filename of the lockfile
	 */
	private static String strLockFilename = "BH.lock";

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
	 * Shutdown-Thread
	 */
	private Thread shutdownThread = null;

	/**
	 * Constructor
	 */
	public BH() {
		GuiEvent.instance().addListener(new IGuiEventListener() {
			@Override
			public void exitApp(boolean restart) {
				if (started == false) {
					return;
				}
				exitBH(0, restart);
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
				frame = createInvisibleFrame();
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

		// If path for downloads are overridden by directories.properties file, then we need to set the path in SettingsManager here
		String portableDownloadPath = ApplicationProperties.getProperty("DownloadPath");
		if (portableDownloadPath != null) {
			if (!portableDownloadPath.endsWith("/") && !portableDownloadPath.endsWith("\\")) {
				portableDownloadPath += FileTool.FILE_SEPERATOR;
			}
			SettingsManager.instance().setSavePath(portableDownloadPath);
		}

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
			main.setVisible(true);
			main.setVisible(false);

			// Add icon to the SystemTray
			stt.showTrayIcon();
		} else {
			main = Main.instance();
			// If SystemTray is not used bring the main window to the front and request the focus
			main.setVisible(true);
			main.toFront();
			main.requestFocus();
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
				update.setVisible(true);
				update.toFront();
			} else {
				// If not, dispose it
				update.dispose();
				update = null;
			}
		}

		// Create and register the Shutdown-Thread
		shutdownThread = new Thread("Shutdown-Thread") {
			@Override
			public void run() {
				if (!exited) {
					exitBH(1, false);
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdownThread);
	}

	private synchronized void exitBH(int mode, boolean restart) {
		if (DownloadQueueManager.instance().isDownloading()) {
			JOptionPane.showMessageDialog(null, Localization.getString("ExitWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		exited = true;

		if (mode == 0) {
			logger.debug("Removing Shutdownhook");
			if (shutdownThread != null) {
				Runtime.getRuntime().removeShutdownHook(shutdownThread);
			}
		}

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

		if (stt != null && mode == 0) {
			logger.debug("Removing Trayicon");
			// Remove icon from SytemTray
			stt.remove();
		}

		// Release the lockfile
		logger.debug("Releasing Lockfile");
		ApplicationTool.releaseLockFile();

		// Restart BH
		if (restart && ApplicationProperties.getProperty("JarFilename").length() > 0) {
			try {
				String bhAbsolutePath = new File(ApplicationProperties.getProperty("ApplicationPath")).getAbsolutePath();
				if (bhAbsolutePath.endsWith(FileTool.FILE_SEPERATOR) == false) {
					bhAbsolutePath += FileTool.FILE_SEPERATOR;
				}

				String jre = "";
				String jreJavaw = System.getProperty("java.home") + FileTool.FILE_SEPERATOR + "bin" + FileTool.FILE_SEPERATOR + "javaw";
				String jreJava = System.getProperty("java.home") + FileTool.FILE_SEPERATOR + "bin" + FileTool.FILE_SEPERATOR + "java";

				String os = System.getProperty("os.name").toLowerCase();

				File fJreJavaw = new File(os.contains("windows") ? jreJavaw + ".exe" : jreJavaw);
				File fJreJava = new File(os.contains("windows") ? jreJava + ".exe" : jreJava);

				if (fJreJavaw.exists()) {
					jre = "\"" + jreJavaw + "\" -jar \"" + bhAbsolutePath + ApplicationProperties.getProperty("JarFilename") + "\"";
				} else {
					if (fJreJava.exists()) {
						jre = "\"" + jreJava + "\" -jar \"" + bhAbsolutePath + ApplicationProperties.getProperty("JarFilename") + "\"";
					}
				}

				if (jre.length() > 0) {
					List<String> lProcess = new ArrayList<>(Arrays.asList(jre.split(" ")));
					new ProcessBuilder(lProcess).start();
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
		}

		// Exit
		logger.debug("Exit now!");
		System.exit(0);
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

	private static JFrame createInvisibleFrame() {
		JFrame frame = new JFrame("BH");
		frame.setIconImage(Icons.getBHImage("BH.png"));
		frame.setUndecorated(true);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		return frame;
	}

	private static void parseCommandLine(String[] args) {
		for (String arg : args) {
			if (arg.equalsIgnoreCase("-version")) {
				System.out.print(ApplicationProperties.getProperty("ApplicationVersion"));
				System.exit(0);
			} else if (arg.equalsIgnoreCase("-versionNumber")) {
				System.out.print(ApplicationProperties.getProperty("ApplicationVersion").replaceAll("\\.", ""));
				System.exit(0);
			} else if (arg.equalsIgnoreCase("-help")) {
				String help = ApplicationProperties.getProperty("ApplicationName") + " v" + ApplicationProperties.getProperty("ApplicationVersion") + "\n\n";
				help += "Command Line Arguments:\n";
				help += "-version\t\tPrints the Version of BH (e.g. 1.2.0)\n\n";
				help += "-versionNumber\t\tPrints the VersionNumber of BH (e.g. 120)\n\n";
				System.out.print(help);
				System.exit(0);
			}
		}
	}

	/**
	 * The user can override the path of some directories, such as
	 * Download-Path, Settings-Path and so on.
	 * There must only be a textfile called directories.txt in
	 * the programm folder.
	 * 
	 * A line in the file must look like this
	 * Name Path
	 * Name and Path must be seperated by a tab.
	 * 
	 * Available Names:
	 * Database
	 * Settings
	 * Download-Log
	 * Logs
	 * Downloads
	 * 
	 * @throws IOException
	 */
	private static void readDirectoriesFile() throws IOException {
		File file = new File(ApplicationProperties.getProperty("ApplicationPath") + "directories.properties");
		if (file.exists() == false) {
			return;
		}
		String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

		Properties directoriesProperties = new Properties();
		// We do the replace, because load will treat backslashes as escape characters
		directoriesProperties.load(new StringReader(content.replace("\\", "\\\\")));

		String databaseDir = directoriesProperties.getProperty("DatabasePath");
		String settingsDir = directoriesProperties.getProperty("SettingsPath");
		String downloadLogDir = directoriesProperties.getProperty("DownloadLogPath");
		String logsDir = directoriesProperties.getProperty("LogsPath");
		String downloadDir = directoriesProperties.getProperty("DownloadPath");

		if (databaseDir != null && !databaseDir.isEmpty()) {
			ApplicationProperties.setProperty("DatabasePath", databaseDir);
		}
		if (settingsDir != null && !settingsDir.isEmpty()) {
			ApplicationProperties.setProperty("SettingsPath", settingsDir);
		}
		if (downloadLogDir != null && !downloadLogDir.isEmpty()) {
			ApplicationProperties.setProperty("DownloadLogPath", downloadLogDir);
		}
		if (logsDir != null && !logsDir.isEmpty()) {
			ApplicationProperties.setProperty("LogsPath", logsDir);
		}
		if (downloadDir != null && !downloadDir.isEmpty()) {
			ApplicationProperties.setProperty("DownloadPath", downloadDir);
		}
	}

	private static void executeDeleteUpdates() {
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
	 * Format Stacktrace to String
	 * 
	 * @param throwable Throwable
	 * @return Stacktrace as String
	 */
	private static String formatStackTrace(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

	/**
	 * Main-Method
	 * 
	 * @param args Arguments
	 */
	public static void main(String[] args) {
		try {
			ApplicationProperties.initProperties(BH.class.getResourceAsStream("/Application_Config.properties"));
		} catch (IOException e) {
			// Logger is not initialized at this point
			System.err.println("Could not initialize application properties");
			e.printStackTrace();
			ApplicationTool.writeBasicErrorLogfile(new File("BH-Error.log"), "Could not initialize application properties:\n" + formatStackTrace(e));
			System.exit(1);
		}

		String jarFilename = ApplicationTool.getThisApplicationsJarFilename(BH.class);
		ApplicationProperties.setProperty("JarFilename", jarFilename);

		// Geth the program directory
		String appPath = ApplicationTool.getThisApplicationsPath(!jarFilename.isEmpty() ? jarFilename : ApplicationProperties.getProperty("ApplicationShortName") + ".jar");
		ApplicationProperties.setProperty("ApplicationPath", appPath);

		String programUserDir = System.getProperty("user.home") + FileTool.FILE_SEPERATOR + "." + ApplicationProperties.getProperty("ApplicationShortName") + FileTool.FILE_SEPERATOR;
		ApplicationProperties.setProperty("ProfilePath", programUserDir);
		ApplicationProperties.setProperty("DatabasePath", programUserDir);
		ApplicationProperties.setProperty("SettingsPath", programUserDir);
		ApplicationProperties.setProperty("DownloadLogPath", programUserDir);
		ApplicationProperties.setProperty("LogsPath", programUserDir);

		// Parse Command Line
		parseCommandLine(args);

		/*
		 * read the directories.txt from program folder if exists and
		 * override paths of BH when definded in the file
		 */
		try {
			readDirectoriesFile();
		} catch (IOException e) {
			// Logger is not initialized at this point
			System.err.println("Could not read directories.properties");
			e.printStackTrace();
			ApplicationTool.writeBasicErrorLogfile(new File("BH-Error.log"), "Could not read directories.properties:\n" + formatStackTrace(e));
			System.exit(1);
		}

		String logFilename = ApplicationProperties.getProperty("ApplicationShortName") + ".log";
		// Loggers can be created after this point
		System.setProperty("bhlog4jlogfile", ApplicationProperties.getProperty("LogsPath") + FileTool.FILE_SEPERATOR + logFilename);
		logger = LoggerFactory.getLogger(BH.class);
		ApplicationTool.initializeSLF4JUncaughtExceptionHandler();

		/*
		 * Now try to lock the file
		 * We do this, to make sure, there is only one instance of BH runnig.
		 */
		if (ApplicationTool.lockLockFile(strLockFilePath, strLockFilename) == false) {
			// Display a frame, so that BH already shows up in the taskbar and can be switched to. Otherwise the user might not see that there was a dialog open
			JFrame frame = null;
			try {
				frame = createInvisibleFrame();
				// If the lockfile could not locked, we display a error-message and exit
				JOptionPane.showMessageDialog(frame, "Another Instance of the Application is running. Application is terminating.", "Error", JOptionPane.ERROR_MESSAGE);
			} finally {
				if (frame != null) {
					frame.dispose();
				}
			}
			System.exit(0);
		}

		// Write some useful info to the logfile
		ApplicationTool.logApplicationInfo();

		// Delete old log files
		ApplicationTool.deleteOldLogFiles(7, logFilename, ApplicationProperties.getProperty("LogsPath"));

		// Delete Updates
		executeDeleteUpdates();

		// Good, now let BH really start
		new BH();
	}
}

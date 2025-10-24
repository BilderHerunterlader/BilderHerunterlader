package ch.supertomcat.bh.log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.database.sqlite.BlacklistSQLiteDB;
import ch.supertomcat.bh.database.sqlite.LogsSQLiteDB;
import ch.supertomcat.bh.gui.adder.AdderWindow;
import ch.supertomcat.bh.gui.log.LogTableModel;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.settings.BHSettingsListener;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.xml.LookAndFeelSetting;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressWindow;
import ch.supertomcat.supertomcatutils.io.CountingInputStream;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Class for reading and writing log of downloaded URLs
 */
public class LogManager implements BHSettingsListener {
	private static final String BH_LOGS_FILENAME = "BH_logs.txt";

	private static final String OLD_BH_LOGS_FILENAME_PREFIX = "BH-logs";

	private static final String BH_LOGS_FILENAME_PREFIX = "BH_logs";

	private static final String OLD_BH_BLACKLIST_FILENAME = "BH-Blacklist.txt";

	private static final String BH_BLACKLIST_FILENAME = "BH_Blacklist.txt";

	/**
	 * Date Format
	 */
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(LogManager.class);

	private List<ILogManagerListener> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Path to Text-Log-File
	 */
	private String logFile;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Logs Database
	 */
	private final LogsSQLiteDB logsSQLiteDB;

	/**
	 * Blacklist Database
	 */
	private final BlacklistSQLiteDB blacklistSQLiteDB;

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 */
	public LogManager(SettingsManager settingsManager) {
		this.settingsManager = settingsManager;
		/*
		 * Defragment of the database takes a long time, because this database can be very large. So defragment min file size is set to a high value.
		 */
		this.logsSQLiteDB = new LogsSQLiteDB(ApplicationProperties.getProperty(ApplicationMain.DATABASE_PATH) + "/BH-Logs.sqlite", settingsManager.getSettings().isBackupDbOnStart(), settingsManager
				.getSettings().isDefragDBOnStart(), 2L * 1024 * 1024 * 1024);

		this.blacklistSQLiteDB = new BlacklistSQLiteDB(ApplicationProperties.getProperty(ApplicationMain.DATABASE_PATH) + "/BH-Blacklist.sqlite", settingsManager.getSettings()
				.isBackupDbOnStart(), settingsManager.getSettings().isDefragDBOnStart(), settingsManager.getSettings().getDefragMinFilesize());

		this.logFile = ApplicationProperties.getProperty("DownloadLogPath")
				+ settingsManager.getDownloadsSettings().getCurrentDownloadLogFile().replace(OLD_BH_LOGS_FILENAME_PREFIX, BH_LOGS_FILENAME_PREFIX);

		Path folder = Paths.get(ApplicationProperties.getProperty("DownloadLogPath"));
		if (!Files.exists(folder)) {
			try {
				Files.createDirectories(folder);
			} catch (IOException e) {
				logger.error("Could not create directory: {}", folder, e);
			}
		}

		convertOldFiles();

		settingsManager.addSettingsListener(this);
	}

	/**
	 * Convert old log files
	 */
	private synchronized void convertOldFiles() {
		Path folder = Paths.get(ApplicationProperties.getProperty("DownloadLogPath"));

		Predicate<Path> fileFilter = x -> {
			String filename = x.getFileName().toString();
			return (filename.startsWith(OLD_BH_LOGS_FILENAME_PREFIX) && filename.endsWith(".txt")) || OLD_BH_BLACKLIST_FILENAME.equals(filename);
		};

		try (Stream<Path> stream = Files.list(folder)) {
			stream.filter(Files::isRegularFile).filter(fileFilter).forEach(this::convertOldFile);
		} catch (IOException e) {
			logger.error("Could not list logfile names", e);
		}
	}

	/**
	 * Convert old log file
	 * 
	 * @param oldLogFile Old Log File
	 */
	private synchronized void convertOldFile(Path oldLogFile) {
		/*
		 * The old files were written with native encoding. So they are now converted to always have UTF-8 encoding, so that it's predictable
		 */
		String filename = oldLogFile.getFileName().toString();
		Path newLogFile;
		boolean blacklist;
		if (OLD_BH_BLACKLIST_FILENAME.equals(filename)) {
			newLogFile = oldLogFile.resolveSibling(BH_BLACKLIST_FILENAME);
			blacklist = true;
		} else {
			newLogFile = oldLogFile.resolveSibling(filename.replace(OLD_BH_LOGS_FILENAME_PREFIX, BH_LOGS_FILENAME_PREFIX));
			blacklist = false;
		}
		logger.info("Convert old file {} to {}", oldLogFile, newLogFile);
		ProgressWindow progressWindow = new ProgressWindow("Convert " + oldLogFile, null);
		progressWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		try (InputStream in = Files.newInputStream(oldLogFile);
				CountingInputStream countIn = new CountingInputStream(in);
				BufferedReader reader = new BufferedReader(new InputStreamReader(countIn, Charset.forName(System.getProperty("native.encoding"))));
				BufferedWriter writer = Files.newBufferedWriter(newLogFile, StandardCharsets.UTF_8)) {
			long fileSize = Files.size(oldLogFile);
			long bytesPerPercent = fileSize / 8192;
			int nextPercentValue = 1;
			String line;
			long lineCounter = 0;
			List<LogEntry> entries = new ArrayList<>();
			while ((line = reader.readLine()) != null) {
				writer.write(line);
				writer.write("\n");
				writer.flush();

				if (blacklist) {
					blacklistSQLiteDB.insertEntry(new BlacklistEntry(line));
					lineCounter++;
					if (countIn.getCount() >= nextPercentValue * bytesPerPercent) {
						progressWindow.progressChanged(nextPercentValue);
						nextPercentValue++;
					}
					continue;
				}

				try {
					String[] arr = line.split("\t");
					if (arr.length >= 3) {
						long timestamp = Long.parseLong(arr[0]);
						String containerURL = arr[1];
						String target = arr[2];
						String targetPath = FileUtil.getDirectory(target);
						String targetFilename = FileUtil.getFilename(target);
						long size = 0;
						if (arr.length >= 4) {
							size = Long.parseLong(arr[3]);
						}
						String threadURL = "";
						if (arr.length >= 5) {
							threadURL = arr[4];
						}
						String downloadURL = "";
						if (arr.length >= 6) {
							downloadURL = arr[5];
						}
						String thumbURL = "";
						if (arr.length >= 7) {
							thumbURL = arr[6];
						}
						LogEntry logEntry = new LogEntry(timestamp, containerURL, threadURL, downloadURL, thumbURL, targetPath, targetFilename, size);
						entries.add(logEntry);
					}
				} catch (Exception e) {
					logger.error("Could not add data from line {} to database: {}", lineCounter, line, e);
				}

				if (entries.size() >= 1000) {
					logsSQLiteDB.insertEntries(entries);
					entries.clear();
				}

				lineCounter++;
				if (countIn.getCount() >= nextPercentValue * bytesPerPercent) {
					progressWindow.progressChanged(nextPercentValue);
					nextPercentValue++;
				}
			}

			if (!entries.isEmpty()) {
				logsSQLiteDB.insertEntries(entries);
			}
		} catch (IOException e) {
			logger.error("Could not convert old log file: {}", oldLogFile, e);
			return;
		} finally {
			progressWindow.dispose();
		}

		try {
			Files.move(oldLogFile, oldLogFile.resolveSibling(oldLogFile.getFileName().toString() + ".bak"));
		} catch (IOException e) {
			logger.error("Could not rename old log file: {}", oldLogFile, e);
		}
	}

	/**
	 * @param l Listener
	 */
	public void addLogManagerListener(ILogManagerListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	/**
	 * @param l Listener
	 */
	public void removeLogManagerListener(ILogManagerListener l) {
		if (listeners.contains(l)) {
			listeners.remove(l);
		}
	}

	/**
	 * @return AvailableLogFileNames
	 */
	public List<String> getAvailableLogFileNames() {
		Path folder = Paths.get(ApplicationProperties.getProperty("DownloadLogPath"));

		Predicate<Path> fileFilter = x -> {
			String filename = x.getFileName().toString();
			return filename.startsWith(BH_LOGS_FILENAME_PREFIX) && filename.endsWith(".txt") && !filename.equals(BH_LOGS_FILENAME);
		};

		List<String> logFileNames = new ArrayList<>();
		logFileNames.add(BH_LOGS_FILENAME);
		try (Stream<Path> stream = Files.list(folder)) {
			stream.filter(Files::isRegularFile).filter(fileFilter).map(Path::getFileName).map(Path::toString).sorted().forEach(logFileNames::add);
		} catch (IOException e) {
			logger.error("Could not list logfile names", e);
		}
		return logFileNames;
	}

	/**
	 * @param logFiles Log Files
	 * @return Index of current logfile in the array or -1 if not found
	 */
	public int getCurrentLogFileIndexForArray(List<String> logFiles) {
		if (logFiles != null) {
			String currentLogFileName = settingsManager.getDownloadsSettings().getCurrentDownloadLogFile();
			for (int i = 0; i < logFiles.size(); i++) {
				if (logFiles.get(i).equals(currentLogFileName) || logFiles.get(i).replace(BH_LOGS_FILENAME_PREFIX, OLD_BH_LOGS_FILENAME_PREFIX).equals(currentLogFileName)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Search for blacklisted files and sets the blacklisted flags on the URLs
	 * 
	 * @param urls Container URLs
	 * @param ap AdderPanel
	 */
	public synchronized void searchBlacklist(List<URL> urls, AdderWindow ap) {
		boolean updateProgress = ap != null;
		try {
			if (updateProgress) {
				ap.setPGEnabled(true, 0, urls.size(), 0);
				ap.setPGText(Localization.getString("CheckBlacklisted"));
			}

			int alreadyChecked = 0;
			int progressUpdateCount = 0;
			for (URL url : urls) {
				if (!url.isBlacklisted() && blacklistSQLiteDB.checkBlacklisted(url.getURL())) {
					url.setBlacklisted(true);
				}
				alreadyChecked++;
				progressUpdateCount++;
				if (updateProgress && progressUpdateCount >= 1000) {
					ap.setPGValue(alreadyChecked);
					progressUpdateCount = 0;
				}
			}
		} finally {
			if (updateProgress) {
				ap.setPGEnabled(false);
			}
		}
	}

	/**
	 * Search for already downloaded files and sets the already downloaded flags on the URLs
	 * 
	 * @param urls Container URLs
	 * @param ap AdderPanel
	 */
	public synchronized void searchLogs(List<URL> urls, AdderWindow ap) {
		boolean updateProgress = ap != null;

		if (updateProgress) {
			ap.setPGEnabled(true, 0, urls.size(), 0);
			ap.setPGText(Localization.getString("CheckAlreadyDownloaded"));
		}

		/*
		 * As this method takes a lot of time to complete when there is a big logfile
		 * and a lot of links to check, we use multiple threads to reduce time.
		 * 
		 * We read in first 10000 urls from the logfile, so that the threads
		 * have enough to do.
		 */

		int threadCount = settingsManager.getSettings().getThreadCount();

		if (threadCount < 1) {
			threadCount = 1;
		}

		if (threadCount > urls.size()) {
			threadCount = urls.size();
		}

		int urlsToCheck = 1000;
		int alreadyChecked = 0;

		CyclicBarrier barrier = new CyclicBarrier(threadCount + 1);
		try (ExecutorService threadPool = Executors.newFixedThreadPool(threadCount)) {
			SearchLogThread[] slt = new SearchLogThread[threadCount];
			for (int t = 0; t < threadCount; t++) {
				slt[t] = new SearchLogThread(urls, t, threadCount, logsSQLiteDB, barrier, urlsToCheck);
			}

			for (int i = 0; i < urls.size(); i += urlsToCheck) {
				/*
				 * Start all threads (runnables)
				 */
				for (int t = 0; t < threadCount; t++) {
					threadPool.execute(slt[t]);
				}
				/*
				 * Wait for all threads (runnables) to complete
				 */
				try {
					barrier.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					logger.error(e.getMessage(), e);
				}

				alreadyChecked += urlsToCheck;

				if (updateProgress) {
					ap.setPGValue(alreadyChecked);
				}
			}
		}
	}

	/**
	 * Read logs
	 * 
	 * @param start Index of the line in logfile to start from
	 * @param model Model
	 * @return int array: 0 -&gt; currentStart, 1 -&gt; end, 2 -&gt; lineCount
	 */
	public synchronized int[] readLogs(int start, LogTableModel model) {
		model.removeAllRows();

		int count = logsSQLiteDB.getEntriesCount();
		if (count <= 0) {
			return new int[] { 0, 0, 0 };
		}

		if (start < 0 || start >= count) {
			// Integer division give count of 100 entries for start
			start = (count / 100) * 100;
		}

		logsSQLiteDB.fillTableModelWithEntriesRange(start, 100, model, settingsManager, DATE_FORMAT);

		int entriesCount = model.getRowCount();
		if (entriesCount <= 0) {
			return new int[] { 0, 0, 0 };
		} else {
			return new int[] { start, start + entriesCount, entriesCount };
		}
	}

	/**
	 * Write a log to logfile
	 * 
	 * @param pic Pic
	 */
	public synchronized void writeLog(Pic pic) {
		Path file = Paths.get(logFile);
		try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
			writer.write(Long.toString(pic.getDateTimeSimple()));
			writer.write("\t");
			writer.write(pic.getContainerURL());
			writer.write("\t");
			writer.write(pic.getTarget());
			writer.write("\t");
			writer.write(Long.toString(pic.getSize()));
			writer.write("\t");
			writer.write(pic.getThreadURL());
			writer.write("\t");
			writer.write(pic.getDownloadURL());
			writer.write("\t");
			writer.write(pic.getThumb());
			writer.write("\n");
			writer.flush();
		} catch (IOException e) {
			logger.error("Could not write log file: {}", file, e);
		}

		logsSQLiteDB.insertEntry(new LogEntry(pic));

		for (ILogManagerListener listener : listeners) {
			listener.logChanged();
		}
	}

	/**
	 * Write an url to blacklist
	 * 
	 * @param url URL
	 */
	public synchronized void writeBlacklist(String url) {
		blacklistSQLiteDB.insertEntry(new BlacklistEntry(url));
	}

	/**
	 * Reads the Directory-Log
	 * 
	 * If you don't want to specify a pattern for the directories
	 * then the pattern argument must be null.
	 * 
	 * @param pattern Pattern for Dir
	 * @param onlyExistingDirectories Flag that sets if only existing directories should be added to the list
	 * @param max Maximum Entries
	 * @return Directory-Log
	 */
	public synchronized List<DirectoryLogObject> readDirectoryLog(Pattern pattern, boolean onlyExistingDirectories, int max) {
		List<LogEntry> entries = logsSQLiteDB.getDirectoryLogEntries(max);
		Stream<LogEntry> stream = entries.stream();
		if (pattern != null) {
			stream = stream.filter(x -> pattern.matcher(x.getTargetPath()).find());
		}
		Stream<DirectoryLogObject> dirStream = stream.map(x -> {
			try {
				// Get the directory
				Path directory = Paths.get(x.getTargetPath());
				boolean exists = Files.exists(directory);
				return new DirectoryLogObject(x, exists);
			} catch (InvalidPathException nfe) {
				logger.error(nfe.getMessage(), nfe);
				return new DirectoryLogObject(x, false);
			}
		});
		if (onlyExistingDirectories) {
			dirStream = dirStream.filter(DirectoryLogObject::isExists);
		}
		return dirStream.toList();
	}

	/**
	 * Adds a pic to the log
	 * 
	 * @param pic Pic
	 */
	public void addPicToLog(final Pic pic) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				writeLog(pic);
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * Adds an URL to the blacklist
	 * 
	 * @param url URL
	 */
	public void addUrlToBlacklist(String url) {
		writeBlacklist(url);
	}

	@Override
	public void settingsChanged() {
		String currentLogFile = ApplicationProperties.getProperty("DownloadLogPath")
				+ settingsManager.getDownloadsSettings().getCurrentDownloadLogFile().replace(OLD_BH_LOGS_FILENAME_PREFIX, BH_LOGS_FILENAME_PREFIX);
		if (!logFile.equals(currentLogFile)) {
			logFile = currentLogFile;
			for (ILogManagerListener listener : listeners) {
				listener.currentLogFileChanged();
			}
		}
	}

	@Override
	public void lookAndFeelChanged(LookAndFeelSetting lookAndFeel) {
		// Nothing to do
	}
}

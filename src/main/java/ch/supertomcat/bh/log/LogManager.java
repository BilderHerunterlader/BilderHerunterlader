package ch.supertomcat.bh.log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.adder.AdderWindow;
import ch.supertomcat.bh.gui.log.LogTableModel;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.settings.BHSettingsListener;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.xml.LookAndFeelSetting;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.formatter.UnitFormatUtil;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;

/**
 * Class for reading and writing log of downloaded URLs
 */
public class LogManager implements BHSettingsListener {
	private static final String BH_LOGS_FILENAME = "BH-logs.txt";

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
	 * Path to Text-Blacklist-File
	 */
	private String blacklistFile = ApplicationProperties.getProperty("DownloadLogPath") + "BH-Blacklist.txt";

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 */
	public LogManager(SettingsManager settingsManager) {
		this.settingsManager = settingsManager;
		this.logFile = ApplicationProperties.getProperty("DownloadLogPath") + settingsManager.getDownloadsSettings().getCurrentDownloadLogFile();

		Path folder = Paths.get(ApplicationProperties.getProperty("DownloadLogPath"));
		if (!Files.exists(folder)) {
			try {
				Files.createDirectories(folder);
			} catch (IOException e) {
				logger.error("Could not create directory: {}", folder, e);
			}
		}

		settingsManager.addSettingsListener(this);
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
			return filename.startsWith("BH-logs") && filename.endsWith(".txt") && !filename.equals(BH_LOGS_FILENAME);
		};

		List<String> logFileNames = new ArrayList<>();
		logFileNames.add(BH_LOGS_FILENAME);
		try (Stream<Path> stream = Files.list(folder)) {
			stream.filter(Files::isRegularFile).filter(fileFilter).map(Path::getFileName).map(Path::toString).sorted().forEach(x -> logFileNames.add(x));
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
			for (int i = 0; i < logFiles.size(); i++) {
				if (logFiles.get(i).equals(settingsManager.getDownloadsSettings().getCurrentDownloadLogFile())) {
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
		Path file = Paths.get(blacklistFile);
		if (!Files.exists(file)) {
			return;
		}

		boolean updateProgress = ap != null;

		try (FileInputStream in = new FileInputStream(blacklistFile); BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()))) {
			@SuppressWarnings("resource")
			FileChannel fileChannel = in.getChannel();

			long lFile = Files.size(file);
			long bytesPerPercent = lFile / 100;
			int nextPercentValue = 1;
			boolean lAvailable = lFile > 0;

			if (updateProgress) {
				if (lAvailable) {
					ap.setPGEnabled(true, 0, 100, 0);
				} else {
					ap.setPGEnabled(true);
				}
				ap.setPGText(Localization.getString("CheckBlacklisted"));
			}

			String row = null;
			while ((row = br.readLine()) != null) {
				for (URL url : urls) {
					if (!url.isBlacklisted() && row.equals(url.getURL())) {
						url.setBlacklisted(true);
					}
				}

				if (updateProgress && lAvailable && fileChannel.position() >= nextPercentValue * bytesPerPercent) {
					ap.setPGValue(nextPercentValue);
					nextPercentValue++;
				}
			}
		} catch (IOException e) {
			logger.error("Could not search for blacklisted files", e);
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
		Path file = Paths.get(logFile);
		if (!Files.exists(file)) {
			return;
		}

		boolean updateProgress = ap != null;

		try (FileInputStream in = new FileInputStream(logFile); BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()))) {
			@SuppressWarnings("resource")
			FileChannel fileChannel = in.getChannel();

			long lFile = Files.size(file);
			long bytesPerPercent = lFile / 100;
			int nextPercentValue = 1;
			boolean lAvailable = lFile > 0;

			if (updateProgress) {
				if (lAvailable) {
					ap.setPGEnabled(true, 0, 100, 0);
				} else {
					ap.setPGEnabled(true);
				}
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

			CyclicBarrier barrier = new CyclicBarrier(threadCount + 1);
			try (ExecutorService threadPool = Executors.newFixedThreadPool(threadCount)) {

				List<String> currentRows = new ArrayList<>();

				SearchLogThread[] slt = new SearchLogThread[threadCount];
				for (int t = 0; t < threadCount; t++) {
					slt[t] = new SearchLogThread(urls, t, threadCount, currentRows, barrier);
				}

				int rowsToRead = 1000;

				String row = null;
				while ((row = br.readLine()) != null) {
					currentRows.add(row);

					if (currentRows.size() >= rowsToRead) {
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

						currentRows.clear();

						if (updateProgress && lAvailable && fileChannel.position() >= nextPercentValue * bytesPerPercent) {
							ap.setPGValue(nextPercentValue);
							nextPercentValue++;
						}
					}
				}

				if (!currentRows.isEmpty()) {
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
						logger.error("Could not await barrier", e);
					}

					if (updateProgress && lAvailable && fileChannel.position() >= nextPercentValue * bytesPerPercent) {
						ap.setPGValue(nextPercentValue);
					}
				}
			}
		} catch (IOException e) {
			logger.error("Could not search for already downloaded files", e);
		} finally {
			if (updateProgress) {
				ap.setPGEnabled(false);
			}
		}
	}

	/**
	 * Read logs
	 * 
	 * @param start Index of the line in logfile to start from
	 * @param model Model
	 * @return long array: 0 -&gt; currentStart, 1 -&gt; end, 2 -&gt; lineCount
	 */
	public synchronized long[] readLogs(long start, LogTableModel model) {
		Path file = Paths.get(logFile);
		if (!Files.exists(file)) {
			model.removeAllRows();
			return new long[] { 1, 0, 0 };
		}

		if (start > 0) {
			start--;
		}

		// Count lines
		long lineCount = 0;
		try (BufferedReader reader = Files.newBufferedReader(file)) {
			while (reader.readLine() != null) {
				lineCount++;
			}
		} catch (IOException e) {
			logger.error("Could not count log lines in file: {}", logFile, e);
			return new long[] { 1, 0, 0 };
		}
		if (lineCount > 0) {
			lineCount--;
		}

		long end;
		if (start < 0) {
			end = lineCount;
			start = end - 99;
			if (start < 0) {
				start = 0;
			}
		} else {
			end = start + 99;
			if (end > lineCount) {
				end = lineCount;
			}
			if ((start + 99) > end) {
				start = (end - 99);
			}
		}

		if (lineCount < 100) {
			model.removeAllRows();
		}

		try (BufferedReader reader = Files.newBufferedReader(file)) {
			long lineCounter = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				if (lineCounter >= start && lineCounter <= end) {
					String[] arr = line.split("\t");
					if (arr.length >= 3) {
						try {
							long timestamp = Long.parseLong(arr[0]);
							LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
							String strDateTime = dateTime.format(DATE_FORMAT);
							String containerURL = arr[1];
							String target = arr[2];
							String strFilesize;
							if (arr.length >= 4) {
								long filesize = Long.parseLong(arr[3]);
								strFilesize = UnitFormatUtil.getSizeString(filesize, settingsManager.getSizeView());
							} else {
								strFilesize = Localization.getString("Unkown");
							}
							model.addRow(containerURL, target, strDateTime, strFilesize);
						} catch (NumberFormatException nfe) {
							logger.error("Could not parse timestamp or filesize on line: {}", lineCounter, nfe);
						}
					}
				}
				lineCounter++;
			}
			if (start >= 0) {
				start++;
			}
			return new long[] { start, end, lineCount };
		} catch (IOException e) {
			logger.error("Could not read log lines in file: {}", logFile, e);
			return new long[] { 1, 0, 0 };
		}
	}

	/**
	 * Write a log to logfile
	 * 
	 * @param pic Pic
	 */
	public synchronized void writeLog(Pic pic) {
		Path file = Paths.get(logFile);
		try (BufferedWriter writer = Files.newBufferedWriter(file, Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
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
		Path file = Paths.get(blacklistFile);
		try (BufferedWriter writer = Files.newBufferedWriter(file, Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
			writer.write(url);
			writer.write("\n");
			writer.flush();
		} catch (IOException e) {
			logger.error("Could not write blacklist file: {}", file, e);
		}
	}

	/**
	 * Detects if a directory is already in the ArrayList
	 * 
	 * @param dir Directory
	 * @param dateTime Date and Time
	 * @param dirs Directory-Array
	 * @return Index of directory log object in list or -1 if not found
	 */
	private int isInList(String dir, long dateTime, List<DirectoryLogObject> dirs) {
		for (int i = dirs.size() - 1; i >= 0; i--) {
			if (dirs.get(i).getDirectory().equals(dir) && Math.abs(dateTime - dirs.get(i).getDateTime()) <= 86400000) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Reads the Directory-Log
	 * 
	 * If you don't want to specify a pattern for the directories
	 * then the pattern argument must be null.
	 * 
	 * @param pattern Pattern for Dir
	 * @param onlyExistingDirectories Flag that sets if only existing directories should be added to the list
	 * @param progress Progress-Observer
	 * @return Directory-Log
	 */
	public synchronized List<DirectoryLogObject> readDirectoryLog(Pattern pattern, boolean onlyExistingDirectories, ProgressObserver progress) {
		List<DirectoryLogObject> dirs = new ArrayList<>();

		Path f = Paths.get(logFile);
		if (!Files.exists(f)) {
			return dirs;
		}

		progress.progressChanged(0, 100, 0);
		progress.progressChanged(true);

		try (BufferedReader reader = Files.newBufferedReader(f, Charset.defaultCharset())) {
			long size = Files.size(f);
			String line;
			Path fDir = null;
			long bytesRead = 0;
			Map<Path, Boolean> folderExistsMap = new HashMap<>();
			while ((line = reader.readLine()) != null) {
				bytesRead += line.getBytes().length;
				String[] arr = line.split("\t");
				if (arr.length >= 3) {
					try {
						// Get the directory
						fDir = Paths.get(arr[2]).toAbsolutePath().getParent();
						String dir = fDir.toString();

						// Get the date
						long dateTime = Long.parseLong(arr[0]);
						int index = isInList(dir, dateTime, dirs);
						if (index > -1) {
							dirs.get(index).setDateTime(dateTime);
						} else {
							/*
							 * Wo only need to check if the directory exists
							 * when the directory is not in the list already
							 */

							Boolean cachedExists = folderExistsMap.get(fDir);
							boolean exists;
							if (cachedExists != null) {
								exists = cachedExists;
							} else {
								exists = Files.exists(fDir) && Files.isDirectory(fDir);
								folderExistsMap.put(fDir, exists);
							}

							if (onlyExistingDirectories && !exists) {
								if (size > 0) {
									progress.progressChanged((int)(bytesRead * 100 / size));
								}
								continue;
							}
							/*
							 * Now we have to check if the pattern matches
							 */
							if (pattern != null) {
								Matcher matcher = pattern.matcher(dir);
								if (!matcher.find()) {
									continue;
								}
							}
							dirs.add(new DirectoryLogObject(dir, dateTime, exists));
						}
					} catch (NumberFormatException nfe) {
						logger.error(nfe.getMessage(), nfe);
					}
				}
				if (size > 0) {
					progress.progressChanged((int)(bytesRead * 100 / size));
				}
			}
			return dirs;
		} catch (IOException e) {
			logger.error("Could not read file: {}", f, e);
			return null;
		} finally {
			progress.progressChanged(false);
		}
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
		String currentLogFile = ApplicationProperties.getProperty("DownloadLogPath") + settingsManager.getDownloadsSettings().getCurrentDownloadLogFile();
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

package ch.supertomcat.bh.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * This class provides methods, which are often used.
 */
public final class BHUtil {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(BHUtil.class);

	/**
	 * Constructor
	 */
	private BHUtil() {
	}

	/**
	 * This method filters not allowed chars in filenames
	 * 
	 * @param filename Filename
	 * @param settingsManager Settings Manager
	 * @return Filtered Filename
	 */
	public static String filterFilename(String filename, SettingsManager settingsManager) {
		return FileUtil.filterFilename(filename, settingsManager.getAllowedFilenameChars());
	}

	/**
	 * This method filters not allowed chars in paths (including filename if available)
	 * 
	 * @param path Path
	 * @param settingsManager Settings Manager
	 * @return Filtered path
	 */
	public static String filterPath(String path, SettingsManager settingsManager) {
		return FileUtil.filterPath(path, settingsManager.getAllowedFilenameChars());
	}

	/**
	 * Method to reduce length of filename, because paths in Windows Explorer can only be have 255 chars.
	 * 
	 * @param filename Filename
	 * @param settingsManager Settings Manager
	 * @return Reduced Filename
	 */
	public static String reduceFilenameLength(String filename, SettingsManager settingsManager) {
		if (Boolean.TRUE.equals(settingsManager.getDownloadsSettings().isReduceFilenameLength())) {
			return FileUtil.reduceFilenameLength(filename);
		}
		return filename;
	}

	/**
	 * Method to reduce length of path (without filename), because paths in Windows Explorer can only be have 255 chars.
	 * 
	 * @param folder Path without Filename
	 * @param settingsManager Settings Manager
	 * @return Reduced Path
	 */
	public static String reducePathLength(String folder, SettingsManager settingsManager) {
		if (Boolean.TRUE.equals(settingsManager.getDownloadsSettings().isReducePathLength())) {
			return FileUtil.reducePathLength(folder);
		}
		return folder;
	}

	/**
	 * Returns the encoding by using UniversalDetector which reads the InputStream
	 * to detect the encoding. If the encoding could not be detected an empty String
	 * is returned.
	 * If the InputStream supports mark and reset then that is also done by this method.
	 * Reading and mark and reset will throw an IOException if something went wrong.
	 * 
	 * @param in InputStream
	 * @return Encoding or null if not found
	 * @throws IOException
	 */
	public static String getEncodingFromInputStream(InputStream in) throws IOException {
		if (in.markSupported()) {
			// Mark current position of the InputStream
			in.mark(Integer.MAX_VALUE);
		}

		String encoding = null;
		UniversalDetector detector = new UniversalDetector(null);
		byte[] buffer = new byte[4096];
		int read;
		while ((read = in.read(buffer)) != -1 && !detector.isDone()) {
			if (read > 0) {
				detector.handleData(buffer, 0, read);
			}
		}
		detector.dataEnd();
		encoding = detector.getDetectedCharset();
		detector.reset();

		if (in.markSupported()) {
			// Reset the current position of the InputStream
			in.reset();
		}

		return encoding;
	}

	/**
	 * Read patterns defined by the user from a textfile
	 * 
	 * @param file Text File
	 * @param encoding Encoding
	 * @param caseInsensitive True if patterns should be compiled with CASE_INSENSITIVE flag, false otherwise
	 * @return Patterns
	 */
	public static List<Pattern> readPatternsFromTextFile(Path file, Charset encoding, boolean caseInsensitive) {
		List<Pattern> patterns = new ArrayList<>();
		if (!Files.exists(file)) {
			return patterns;
		}

		try (BufferedReader reader = Files.newBufferedReader(file, encoding)) {
			String row = null;
			while ((row = reader.readLine()) != null) {
				if (row.isEmpty()) {
					continue;
				}

				try {
					Pattern compiledPattern;
					if (caseInsensitive) {
						compiledPattern = Pattern.compile(row, Pattern.CASE_INSENSITIVE);
					} else {
						compiledPattern = Pattern.compile(row);
					}
					patterns.add(compiledPattern);
				} catch (PatternSyntaxException pse) {
					logger.error("Could not compile pattern: {}", row, pse);
				}
			}
		} catch (IOException e) {
			logger.error("Could not load patterns from: {}", file, e);
		}
		return patterns;
	}

	/**
	 * Changes the root logger level
	 * 
	 * @param level Level
	 */
	public static void changeLog4JRootLoggerLevel(Level level) {
		LoggerContext loggerContext = (LoggerContext)LogManager.getContext(false);
		Configuration config = loggerContext.getConfiguration();
		LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		loggerConfig.setLevel(level);
		loggerContext.updateLoggers(config);
	}
}

package ch.supertomcat.bh.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which provides Java methods to Javascript
 */
public final class JavascriptMethodProvider {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(JavascriptMethodProvider.class);

	/**
	 * Constructor
	 */
	private JavascriptMethodProvider() {
	}

	/**
	 * Method invoked from javascript to log debug messages
	 * 
	 * @param message Message
	 * @param source Cause
	 */
	public static void logDebug(String message, String source) {
		logger.debug("{}: {}", source, message);
	}

	/**
	 * Method invoked from javascript to log warn messages
	 * 
	 * @param message Message
	 * @param source Cause
	 */
	public static void logWarn(String message, String source) {
		logger.warn("{}: {}", source, message);
	}

	/**
	 * Method invoked from javascript to log error messages
	 * 
	 * @param message Message
	 * @param source Cause
	 */
	public static void logError(String message, String source) {
		logger.error("{}: {}", source, message);
	}
}

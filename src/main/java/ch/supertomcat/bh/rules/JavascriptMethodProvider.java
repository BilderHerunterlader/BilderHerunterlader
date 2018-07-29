package ch.supertomcat.bh.rules;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.Hoster;

/**
 * Class which provides Java methods to Javascript
 */
public class JavascriptMethodProvider {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(JavascriptMethodProvider.class);

	/**
	 * Map with java methods
	 */
	private static Map<String, Method> javaMethods = new HashMap<>();

	static {
		try {
			javaMethods.put("downloadContainerPage", JavascriptMethodProvider.class.getMethod("downloadContainerPage", String.class, String.class));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Could not initialize JavascriptMethodProvider", e);
		}
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

	/**
	 * Method invoked from javascript to download container page
	 * 
	 * @param url URL
	 * @param referrer Referrer
	 * @return Container Page
	 * @throws HostException
	 */
	public static String downloadContainerPage(String url, String referrer) throws HostException {
		Hoster hoster = new Hoster() {

			@Override
			public String toString() {
				return "JavascriptMethodProviderDummyHoster";
			}

			@Override
			public void setEnabled(boolean enabled) {
				// Nothing to do
			}

			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public boolean canBeDisabled() {
				return false;
			}
		};
		return hoster.downloadContainerPage("JavascriptMethodProviderDummyHoster", url, referrer);
	}

	/**
	 * @param functionName Function Name
	 * @param scope Scope
	 * @return Javascript Function
	 */
	public static FunctionObject getJavascriptFunction(String functionName, ScriptableObject scope) {
		Method method = javaMethods.get(functionName);
		if (method != null) {
			return new FunctionObject(functionName, method, scope);
		} else {
			return null;
		}
	}
}

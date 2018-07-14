package ch.supertomcat.bh.settings;

import java.util.HashMap;
import java.util.Map;

import ch.supertomcat.supertomcattools.cookietools.BrowserCookies;
import ch.supertomcat.supertomcattools.cookietools.firefox.FirefoxCookieStrategy;
import ch.supertomcat.supertomcattools.cookietools.firefox.FirefoxCookies;
import ch.supertomcat.supertomcattools.cookietools.opera.newformat.OperaNewCookieStrategy;
import ch.supertomcat.supertomcattools.cookietools.opera.newformat.OperaNewCookies;
import ch.supertomcat.supertomcattools.cookietools.opera.oldformat.OperaCookieStrategy;
import ch.supertomcat.supertomcattools.cookietools.opera.oldformat.OperaCookies;
import ch.supertomcat.supertomcattools.cookietools.palemoon.PaleMoonCookieStrategy;
import ch.supertomcat.supertomcattools.cookietools.palemoon.PaleMoonCookies;

/**
 * Class which provides methods to get the cookies from a browser.
 * The user can decide from which browser the cookies are read.
 */
public class CookieManager {
	/**
	 * Returns the Cookies for a domain
	 * 
	 * @param url URL
	 * @return Cookies
	 */
	public static String getCookies(String url) {
		Map<String, String> cookieStrategyOptions = new HashMap<>();

		int browser = SettingsManager.instance().getCookiesFromBrowser();
		switch (browser) {
			case BrowserCookies.BROWSER_NO_COOKIES:
				break;
			case BrowserCookies.BROWSER_IE:
				break;
			case BrowserCookies.BROWSER_FIREFOX:
				if (SettingsManager.instance().isCookieFileFirefoxFixed()) {
					String cookieFile = SettingsManager.instance().getCookieFileFirefox();
					cookieStrategyOptions.put(FirefoxCookieStrategy.COOKIE_FILE_FF_KEY, cookieFile);
					cookieStrategyOptions.put(FirefoxCookieStrategy.COOKIE_FILE_FF_V3_KEY, cookieFile);
				}
				break;
			case BrowserCookies.BROWSER_OPERA:
				if (SettingsManager.instance().isCookieFileOperaFixed()) {
					String cookieFile = SettingsManager.instance().getCookieFileOpera();
					cookieStrategyOptions.put(OperaCookieStrategy.COOKIE_FILE_OPERA_KEY, cookieFile);
				}
				break;
			case BrowserCookies.BROWSER_PALE_MOON:
				if (SettingsManager.instance().isCookieFilePaleMoonFixed()) {
					String cookieFile = SettingsManager.instance().getCookieFilePaleMoon();
					cookieStrategyOptions.put(PaleMoonCookieStrategy.COOKIE_FILE_PALE_MOON_KEY, cookieFile);
				}
				break;
			case BrowserCookies.BROWSER_OPERA_NEW:
				if (SettingsManager.instance().isCookieFileOperaNewFixed()) {
					String cookieFile = SettingsManager.instance().getCookieFileOperaNew();
					cookieStrategyOptions.put(OperaNewCookieStrategy.COOKIE_FILE_OPERA_NEW_KEY, cookieFile);
				}
				break;
			default:
				break;
		}

		return BrowserCookies.getCookies(url, browser, cookieStrategyOptions);
	}

	/**
	 * Returns the path to the cookie-file of Firefox 1 and Firefox 2
	 * 
	 * @return Cookie-File-Path
	 */
	public static String getCookieFileForFirefox() {
		if (SettingsManager.instance().isCookieFileFirefoxFixed()) {
			return SettingsManager.instance().getCookieFileFirefox();
		}

		return FirefoxCookies.getPathForFirefox() + "cookies.txt";
	}

	/**
	 * Returns the path to the cookie-file of Firefox 3
	 * 
	 * @return Cookie-File-Path
	 */
	public static String getCookieFileForFirefox3() {
		if (SettingsManager.instance().isCookieFileFirefoxFixed()) {
			return SettingsManager.instance().getCookieFileFirefox();
		}

		return FirefoxCookies.getPathForFirefox() + "cookies.sqlite";
	}

	/**
	 * Returns the path to the cookie-file of Pale Moon
	 * 
	 * @return Cookie-File-Path
	 */
	public static String getCookieFileForPaleMoon() {
		if (SettingsManager.instance().isCookieFilePaleMoonFixed()) {
			return SettingsManager.instance().getCookieFilePaleMoon();
		}

		return PaleMoonCookies.getPathForPaleMoon() + "cookies.sqlite";
	}

	/**
	 * Returns the path to the cookie-file of Opera
	 * 
	 * @param checkExist Check Existance
	 * @return Cookie Pfad
	 */
	public static String getCookieFileForOpera(boolean checkExist) {
		if (SettingsManager.instance().isCookieFileOperaFixed()) {
			return SettingsManager.instance().getCookieFileOpera();
		}

		return OperaCookies.getCookieFileForOpera(checkExist);
	}

	/**
	 * Returns the path to the cookie-file of Opera New
	 * 
	 * @return Cookie Pfad
	 */
	public static String getCookieFileForOperaNew() {
		if (SettingsManager.instance().isCookieFileOperaNewFixed()) {
			return SettingsManager.instance().getCookieFileOperaNew();
		}

		return OperaNewCookies.getCookieFileForOpera();
	}
}

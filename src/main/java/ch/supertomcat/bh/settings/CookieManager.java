package ch.supertomcat.bh.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieIdentityComparator;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;

import ch.supertomcat.supertomcatutils.http.cookies.BrowserCookie;
import ch.supertomcat.supertomcatutils.http.cookies.BrowserCookies;
import ch.supertomcat.supertomcatutils.http.cookies.firefox.FirefoxCookieStrategy;
import ch.supertomcat.supertomcatutils.http.cookies.firefox.FirefoxCookies;
import ch.supertomcat.supertomcatutils.http.cookies.opera.newformat.OperaNewCookieStrategy;
import ch.supertomcat.supertomcatutils.http.cookies.opera.newformat.OperaNewCookies;
import ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.OperaCookieStrategy;
import ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.OperaCookies;
import ch.supertomcat.supertomcatutils.http.cookies.palemoon.PaleMoonCookieStrategy;
import ch.supertomcat.supertomcatutils.http.cookies.palemoon.PaleMoonCookies;

/**
 * Class which provides methods to get the cookies from a browser.
 * The user can decide from which browser the cookies are read.
 */
public class CookieManager {
	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 */
	public CookieManager(SettingsManager settingsManager) {
		this.settingsManager = settingsManager;
	}

	/**
	 * Fill cookies to cookie store. Already existing cookies in the store will not be overwritten.
	 * 
	 * @param url URL
	 * @param cookieStore Cookie Store
	 */
	public void fillCookies(String url, CookieStore cookieStore) {
		List<Cookie> cookiesInStore = cookieStore.getCookies();
		List<BasicClientCookie> cookiesToFill = getBrowserCookies(url);
		Predicate<Cookie> cookieAlreadyExistsPredicate = cookieToFill -> cookiesInStore.stream().anyMatch(cookieInStore -> CookieIdentityComparator.INSTANCE.compare(cookieToFill, cookieInStore) == 0);
		cookiesToFill.removeIf(cookieAlreadyExistsPredicate);
		for (BasicClientCookie cookie : cookiesToFill) {
			cookieStore.addCookie(cookie);
		}
	}

	/**
	 * Returns the Cookies for a domain
	 * 
	 * @param url URL
	 * @return Cookies
	 */
	public List<BasicClientCookie> getBrowserCookies(String url) {
		List<BrowserCookie> browserCookies = getCookies(url, BrowserCookies::getBrowserCookies);

		List<BasicClientCookie> cookies = new ArrayList<>();
		for (BrowserCookie cookie : browserCookies) {
			BasicClientCookie basicClientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
			basicClientCookie.setDomain(cookie.getDomain());
			basicClientCookie.setPath(cookie.getPath());
			basicClientCookie.setCreationDate(cookie.getCreationDate());
			basicClientCookie.setExpiryDate(cookie.getExpiryDate());
			basicClientCookie.setSecure(cookie.isSecure());
			basicClientCookie.setHttpOnly(cookie.isHttpOnly());
			cookies.add(basicClientCookie);
		}
		return cookies;
	}

	/**
	 * Returns the Cookies for a domain
	 * 
	 * @param url URL
	 * @return Cookies
	 */
	public String getCookies(String url) {
		return getCookies(url, BrowserCookies::getCookies);
	}

	/**
	 * Returns the Cookies for a domain
	 * 
	 * @param <T> Supplier
	 * @param url URL
	 * @param cookiesFunction Function to get Cookies
	 * @return Cookies
	 */
	private <T> T getCookies(String url, CookiesFunction<T> cookiesFunction) {
		Map<String, String> cookieStrategyOptions = new HashMap<>();

		int browser = settingsManager.getCookiesFromBrowser();
		switch (browser) {
			case BrowserCookies.BROWSER_NO_COOKIES:
				break;
			case BrowserCookies.BROWSER_IE:
				break;
			case BrowserCookies.BROWSER_FIREFOX:
				if (settingsManager.isCookieFileFirefoxFixed()) {
					String cookieFile = settingsManager.getCookieFileFirefox();
					cookieStrategyOptions.put(FirefoxCookieStrategy.COOKIE_FILE_FF_KEY, cookieFile);
					cookieStrategyOptions.put(FirefoxCookieStrategy.COOKIE_FILE_FF_V3_KEY, cookieFile);
				}
				break;
			case BrowserCookies.BROWSER_OPERA:
				if (settingsManager.isCookieFileOperaFixed()) {
					String cookieFile = settingsManager.getCookieFileOpera();
					cookieStrategyOptions.put(OperaCookieStrategy.COOKIE_FILE_OPERA_KEY, cookieFile);
				}
				break;
			case BrowserCookies.BROWSER_PALE_MOON:
				if (settingsManager.isCookieFilePaleMoonFixed()) {
					String cookieFile = settingsManager.getCookieFilePaleMoon();
					cookieStrategyOptions.put(PaleMoonCookieStrategy.COOKIE_FILE_PALE_MOON_KEY, cookieFile);
				}
				break;
			case BrowserCookies.BROWSER_OPERA_NEW:
				if (settingsManager.isCookieFileOperaNewFixed()) {
					String cookieFile = settingsManager.getCookieFileOperaNew();
					cookieStrategyOptions.put(OperaNewCookieStrategy.COOKIE_FILE_OPERA_NEW_KEY, cookieFile);
				}
				break;
			default:
				break;
		}

		return cookiesFunction.getCookies(url, browser, cookieStrategyOptions);
	}

	/**
	 * Returns the path to the cookie-file of Firefox 1 and Firefox 2
	 * 
	 * @return Cookie-File-Path
	 */
	public String getCookieFileForFirefox() {
		if (settingsManager.isCookieFileFirefoxFixed()) {
			return settingsManager.getCookieFileFirefox();
		}

		return FirefoxCookies.getPathForFirefox() + "cookies.txt";
	}

	/**
	 * Returns the path to the cookie-file of Firefox 3
	 * 
	 * @return Cookie-File-Path
	 */
	public String getCookieFileForFirefox3() {
		if (settingsManager.isCookieFileFirefoxFixed()) {
			return settingsManager.getCookieFileFirefox();
		}

		return FirefoxCookies.getPathForFirefox() + "cookies.sqlite";
	}

	/**
	 * Returns the path to the cookie-file of Pale Moon
	 * 
	 * @return Cookie-File-Path
	 */
	public String getCookieFileForPaleMoon() {
		if (settingsManager.isCookieFilePaleMoonFixed()) {
			return settingsManager.getCookieFilePaleMoon();
		}

		return PaleMoonCookies.getPathForPaleMoon() + "cookies.sqlite";
	}

	/**
	 * Returns the path to the cookie-file of Opera
	 * 
	 * @param checkExist Check Existance
	 * @return Cookie Pfad
	 */
	public String getCookieFileForOpera(boolean checkExist) {
		if (settingsManager.isCookieFileOperaFixed()) {
			return settingsManager.getCookieFileOpera();
		}

		return OperaCookies.getCookieFileForOpera(checkExist);
	}

	/**
	 * Returns the path to the cookie-file of Opera New
	 * 
	 * @return Cookie Pfad
	 */
	public String getCookieFileForOperaNew() {
		if (settingsManager.isCookieFileOperaNewFixed()) {
			return settingsManager.getCookieFileOperaNew();
		}

		return OperaNewCookies.getCookieFileForOpera();
	}

	/**
	 * @param <T> Result Type
	 */
	@FunctionalInterface
	public interface CookiesFunction<T> {
		/**
		 * @param url URL
		 * @param browser Browser
		 * @param cookieStrategyOptions Cookie Strategy Options
		 * @return Cookies
		 */
		public T getCookies(String url, int browser, Map<String, String> cookieStrategyOptions);
	}
}

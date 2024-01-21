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

import ch.supertomcat.bh.settings.xml.BrowserCookiesMode;
import ch.supertomcat.bh.settings.xml.BrowserCookiesSetting;
import ch.supertomcat.bh.settings.xml.ConnectionSettings;
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

		ConnectionSettings connectionSettings = settingsManager.getConnectionSettings();
		BrowserCookiesMode browserCookiesMode = connectionSettings.getBrowserCookiesMode();
		int browser;
		switch (browserCookiesMode) {
			case BROWSER_IE:
				browser = BrowserCookies.BROWSER_IE;
				break;
			case BROWSER_FIREFOX:
				browser = BrowserCookies.BROWSER_FIREFOX;
				BrowserCookiesSetting firefoxBrowserCookiesSetting = connectionSettings.getBrowserCookiesFirefox();
				if (firefoxBrowserCookiesSetting.isCookieFileFixed()) {
					String cookieFile = firefoxBrowserCookiesSetting.getCookieFile();
					cookieStrategyOptions.put(FirefoxCookieStrategy.COOKIE_FILE_FF_KEY, cookieFile);
					cookieStrategyOptions.put(FirefoxCookieStrategy.COOKIE_FILE_FF_V3_KEY, cookieFile);
				}
				break;
			case BROWSER_OPERA:
				browser = BrowserCookies.BROWSER_OPERA;
				BrowserCookiesSetting operaBrowserCookiesSetting = connectionSettings.getBrowserCookiesOpera();
				if (operaBrowserCookiesSetting.isCookieFileFixed()) {
					String cookieFile = operaBrowserCookiesSetting.getCookieFile();
					cookieStrategyOptions.put(OperaCookieStrategy.COOKIE_FILE_OPERA_KEY, cookieFile);
				}
				break;
			case BROWSER_PALE_MOON:
				browser = BrowserCookies.BROWSER_PALE_MOON;
				BrowserCookiesSetting palemoonBrowserCookiesSetting = connectionSettings.getBrowserCookiesPaleMoon();
				if (palemoonBrowserCookiesSetting.isCookieFileFixed()) {
					String cookieFile = palemoonBrowserCookiesSetting.getCookieFile();
					cookieStrategyOptions.put(PaleMoonCookieStrategy.COOKIE_FILE_PALE_MOON_KEY, cookieFile);
				}
				break;
			case BROWSER_OPERA_NEW:
				browser = BrowserCookies.BROWSER_OPERA_NEW;
				BrowserCookiesSetting operaNewBrowserCookiesSetting = connectionSettings.getBrowserCookiesOperaNew();
				if (operaNewBrowserCookiesSetting.isCookieFileFixed()) {
					String cookieFile = operaNewBrowserCookiesSetting.getCookieFile();
					cookieStrategyOptions.put(OperaNewCookieStrategy.COOKIE_FILE_OPERA_NEW_KEY, cookieFile);
				}
				break;
			case NO_COOKIES:
			default:
				browser = BrowserCookies.BROWSER_NO_COOKIES;
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
		BrowserCookiesSetting browserCookiesSetting = settingsManager.getConnectionSettings().getBrowserCookiesFirefox();
		if (browserCookiesSetting.isCookieFileFixed()) {
			return browserCookiesSetting.getCookieFile();
		}

		return FirefoxCookies.getPathForFirefox() + "cookies.txt";
	}

	/**
	 * Returns the path to the cookie-file of Firefox 3
	 * 
	 * @return Cookie-File-Path
	 */
	public String getCookieFileForFirefox3() {
		BrowserCookiesSetting browserCookiesSetting = settingsManager.getConnectionSettings().getBrowserCookiesFirefox();
		if (browserCookiesSetting.isCookieFileFixed()) {
			return browserCookiesSetting.getCookieFile();
		}

		return FirefoxCookies.getPathForFirefox() + "cookies.sqlite";
	}

	/**
	 * Returns the path to the cookie-file of Pale Moon
	 * 
	 * @return Cookie-File-Path
	 */
	public String getCookieFileForPaleMoon() {
		BrowserCookiesSetting browserCookiesSetting = settingsManager.getConnectionSettings().getBrowserCookiesPaleMoon();
		if (browserCookiesSetting.isCookieFileFixed()) {
			return browserCookiesSetting.getCookieFile();
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
		BrowserCookiesSetting browserCookiesSetting = settingsManager.getConnectionSettings().getBrowserCookiesOpera();
		if (browserCookiesSetting.isCookieFileFixed()) {
			return browserCookiesSetting.getCookieFile();
		}

		return OperaCookies.getCookieFileForOpera(checkExist);
	}

	/**
	 * Returns the path to the cookie-file of Opera New
	 * 
	 * @return Cookie Pfad
	 */
	public String getCookieFileForOperaNew() {
		BrowserCookiesSetting browserCookiesSetting = settingsManager.getConnectionSettings().getBrowserCookiesOperaNew();
		if (browserCookiesSetting.isCookieFileFixed()) {
			return browserCookiesSetting.getCookieFile();
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

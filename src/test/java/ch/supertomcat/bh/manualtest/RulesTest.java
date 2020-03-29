package ch.supertomcat.bh.manualtest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.supertomcat.bh.BH;
import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.queue.DownloadRestriction;
import ch.supertomcat.bh.queue.RestrictionAccess;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.application.ApplicationUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;

class RulesTest {
	private HostManager hostManager;

	@BeforeAll
	public static void beforeAll() throws IOException {
		ApplicationProperties.initProperties(BH.class.getResourceAsStream("/Application_Config.properties"));

		String jarFilename = ApplicationUtil.getThisApplicationsJarFilename(BH.class);
		ApplicationProperties.setProperty("JarFilename", jarFilename);

		// Geth the program directory
		String appPath = ApplicationUtil.getThisApplicationsPath(!jarFilename.isEmpty() ? jarFilename : ApplicationProperties.getProperty("ApplicationShortName") + ".jar");
		ApplicationProperties.setProperty("ApplicationPath", appPath);

		String programUserDir = System.getProperty("user.home") + FileUtil.FILE_SEPERATOR + "." + ApplicationProperties.getProperty("ApplicationShortName") + FileUtil.FILE_SEPERATOR;
		ApplicationProperties.setProperty("ProfilePath", programUserDir);
		ApplicationProperties.setProperty("DatabasePath", programUserDir);
		ApplicationProperties.setProperty("SettingsPath", programUserDir);
		ApplicationProperties.setProperty("DownloadLogPath", programUserDir);
		ApplicationProperties.setProperty("LogsPath", programUserDir);
	}

	@BeforeEach
	public void beforeTest() {
		SettingsManager settingsManager = new SettingsManager(ApplicationProperties.getProperty("SettingsPath"), "settings.xml");
		ProxyManager proxyManager = new ProxyManager(settingsManager);
		CookieManager cookieManager = new CookieManager(settingsManager);
		hostManager = new HostManager(null, new RestrictionAccess() {

			@Override
			public void removeRestriction(DownloadRestriction restriction) {
				// Nothing to do
			}

			@Override
			public void addRestriction(DownloadRestriction restriction) {
				// Nothing to do
			}
		}, proxyManager, settingsManager, cookieManager);
	}

	@Test
	public void testPixhostTo() throws HostException {
		Rule rule = hostManager.getHostRules().getRuleByName("pixhost.org");
		assertNotNull(rule);

		String containerURL = "http://pixhost.to/show/93/81041631_090d7d124138416.jpg";
		String thumbnailURL = "";

		Pic p = new Pic(containerURL, "", "");
		p.setThumb(thumbnailURL);
		URLParseObject upo = new URLParseObject(containerURL, thumbnailURL, p);

		String result[] = rule.getURLAndFilename(upo, false);
		assertNotNull(result);
		assertTrue(result.length == 2);

		String resultURL = result[0];
		String resultFilename = result[1];

		assertEquals("https://img23.pixhost.to/images/93/81041631_090d7d124138416.jpg", resultURL);
		assertEquals("81041631_090d7d124138416.jpg", resultFilename);
	}
}

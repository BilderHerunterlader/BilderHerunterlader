package ch.supertomcat.bh.manualtest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import ch.supertomcat.bh.BH;
import ch.supertomcat.bh.cookies.CookieManager;
import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.hoster.hostimpl.HostRules;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.queue.DownloadRestriction;
import ch.supertomcat.bh.queue.RestrictionAccess;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import ch.supertomcat.bh.rules.xml.RuleRegex;
import ch.supertomcat.bh.rules.xml.URLJavascriptPipeline;
import ch.supertomcat.bh.rules.xml.URLRegexPipeline;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.application.ApplicationUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;
import jakarta.xml.bind.JAXBException;

class RulesTest {
	private static HostManager hostManager;

	@BeforeAll
	public static void beforeAll() throws IOException, SAXException, JAXBException {
		try (InputStream in = BH.class.getResourceAsStream("/Application_Config.properties")) {
			ApplicationProperties.initProperties(in);

			String jarFilename = ApplicationUtil.getThisApplicationsJarFilename(BH.class);
			ApplicationProperties.setProperty(ApplicationMain.JAR_FILENAME, jarFilename);

			// Geth the program directory
			String appPath = ApplicationUtil.getThisApplicationsPath(!jarFilename.isEmpty() ? jarFilename : ApplicationProperties.getProperty(ApplicationMain.APPLICATION_SHORT_NAME) + ".jar");
			ApplicationProperties.setProperty(ApplicationMain.APPLICATION_PATH, appPath);

			String programUserDir = System.getProperty("user.home") + FileUtil.FILE_SEPERATOR + "." + ApplicationProperties.getProperty(ApplicationMain.APPLICATION_SHORT_NAME)
					+ FileUtil.FILE_SEPERATOR;
			ApplicationProperties.setProperty(ApplicationMain.PROFILE_PATH, programUserDir);
			ApplicationProperties.setProperty(ApplicationMain.DATABASE_PATH, programUserDir);
			ApplicationProperties.setProperty(ApplicationMain.SETTINGS_PATH, programUserDir);
			ApplicationProperties.setProperty("DownloadLogPath", programUserDir);
			ApplicationProperties.setProperty(ApplicationMain.LOGS_PATH, programUserDir);
		}

		SettingsManager settingsManager = new SettingsManager(ApplicationProperties.getProperty(ApplicationMain.SETTINGS_PATH), "BH-settings.xml", "settings.xml");
		settingsManager.readSettings();
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
		assertEquals(2, result.length);

		String resultURL = result[0];
		String resultFilename = result[1];

		assertEquals("https://img23.pixhost.to/images/93/81041631_090d7d124138416.jpg", resultURL);
		assertEquals("81041631_090d7d124138416.jpg", resultFilename);
	}

	@Test
	public void testRulesWithoutHttps() {
		boolean changeRules = false;

		HostRules hostRules = hostManager.getHostRules();
		for (Rule rule : hostRules.getRules()) {
			RuleDefinition definition = rule.getDefinition();
			String urlPattern = definition.getUrlPattern();
			if (definition.getName().equals("testJavascript") || urlPattern.startsWith("javascript:")) {
				continue;
			}

			boolean urlPatternHttp = !urlPattern.startsWith("https?") && !urlPattern.startsWith("^https?") && !urlPattern.startsWith("(?i)https?") && !urlPattern.startsWith("(?i)^https?")
					&& !urlPattern.startsWith("(https?") && !urlPattern.startsWith(".+?/");

			boolean pipesRegexHttp = definition.getPipes().stream().filter(x -> x instanceof URLRegexPipeline).flatMap(x -> x.getRegexp().stream()).anyMatch(x -> checkHttpWithoutS(x.getPattern()));
			boolean pipesJavascriptHttp = definition.getPipes().stream().filter(x -> x instanceof URLJavascriptPipeline).map(x -> (URLJavascriptPipeline)x)
					.anyMatch(x -> checkHttpWithoutS(x.getJavascriptCode()));
			boolean filenamePipesHttp = definition.getFilenamePipeline().getRegexp().stream().anyMatch(x -> checkHttpWithoutS(x.getPattern()));
			boolean filenameDLSelPipesHttp = definition.getFilenameDownloadSelectionPipeline().getRegexp().stream().anyMatch(x -> checkHttpWithoutS(x.getPattern()));
			boolean failurePipesHttp = definition.getFailuresPipes().stream().flatMap(x -> x.getRegexp().stream()).anyMatch(x -> checkHttpWithoutS(x.getPattern()));

			if (urlPatternHttp || pipesRegexHttp || filenameDLSelPipesHttp || filenamePipesHttp || failurePipesHttp) {
				LoggerFactory.getLogger(getClass()).info("Rule without https {}: {}", rule.getName(), urlPattern);

				if (!changeRules) {
					continue;
				}

				boolean saveRule = true;
				if (urlPattern.startsWith("http://")) {
					definition.setUrlPattern(urlPattern.replace("http://", "https?://"));
				} else if (urlPattern.startsWith("^http://")) {
					definition.setUrlPattern(urlPattern.replace("^http://", "^https?://"));
				} else if (urlPattern.startsWith("http:\\/\\/")) {
					definition.setUrlPattern(urlPattern.replace("http:\\/\\/", "https?://"));
				} else if (urlPattern.startsWith("^http:\\/\\/")) {
					definition.setUrlPattern(urlPattern.replace("^http:\\/\\/", "^https?://"));
				} else if (!pipesRegexHttp && !filenameDLSelPipesHttp && !filenamePipesHttp && !failurePipesHttp && !pipesJavascriptHttp) {
					saveRule = false;
				}

				if (saveRule) {
					if (pipesRegexHttp) {
						definition.getPipes().stream().filter(x -> x instanceof URLRegexPipeline).flatMap(x -> x.getRegexp().stream()).filter(x -> checkHttpWithoutS(x.getPattern()))
								.forEach(x -> fixHttpsInRuleRegex(x));
					}
					if (filenameDLSelPipesHttp) {
						definition.getFilenameDownloadSelectionPipeline().getRegexp().stream().filter(x -> checkHttpWithoutS(x.getPattern())).forEach(x -> fixHttpsInRuleRegex(x));
					}
					if (filenamePipesHttp) {
						definition.getFilenamePipeline().getRegexp().stream().filter(x -> checkHttpWithoutS(x.getPattern())).forEach(x -> fixHttpsInRuleRegex(x));
					}
					if (failurePipesHttp) {
						definition.getFailuresPipes().stream().flatMap(x -> x.getRegexp().stream()).filter(x -> checkHttpWithoutS(x.getPattern())).forEach(x -> fixHttpsInRuleRegex(x));
					}
					if (pipesJavascriptHttp) {
						definition.getPipes().stream().filter(x -> x instanceof URLJavascriptPipeline).map(x -> (URLJavascriptPipeline)x).filter(x -> checkHttpWithoutS(x.getJavascriptCode()))
								.forEach(x -> fixHttpsInJavascriptPipeline(x));
					}

					definition.setVersion(increaseVersion(definition.getVersion()));
					hostRules.saveRule(rule);
				}
			}
		}
	}

	private void fixHttpsInRuleRegex(RuleRegex ruleRegex) {
		ruleRegex.setPattern(fixHttps(ruleRegex.getPattern()));
		ruleRegex.setReplacement(fixHttps(ruleRegex.getReplacement()));
	}

	private void fixHttpsInJavascriptPipeline(URLJavascriptPipeline javascriptPipeline) {
		javascriptPipeline.setJavascriptCode(fixHttps(javascriptPipeline.getJavascriptCode()));
	}

	private String fixHttps(String original) {
		String fixed = original;
		fixed = fixed.replace("^http:\\/\\/", "^https?://");
		fixed = fixed.replace("^http://", "^https?://");
		fixed = fixed.replace("http:\\/\\/", "https?://");
		fixed = fixed.replace("http://", "https?://");
		return fixed;
	}

	private boolean checkHttpWithoutS(String input) {
		return input.contains("^http:\\/\\/") || input.contains("^http://") || input.contains("http:\\/\\/") || input.contains("http://");
	}

	private String increaseVersion(String version) {
		String[] versionArr = version.split("\\.");
		int[] versionIntArr = new int[versionArr.length];
		for (int i = 0; i < versionArr.length; i++) {
			versionIntArr[i] = Integer.parseInt(versionArr[i]);
		}
		for (int i = versionIntArr.length - 1; i >= 0; i--) {
			versionIntArr[i]++;
			if (versionIntArr[i] > 9) {
				versionIntArr[i] = 0;
			} else {
				break;
			}
		}
		return Arrays.stream(versionIntArr).mapToObj(x -> Integer.toString(x)).collect(Collectors.joining("."));
	}
}

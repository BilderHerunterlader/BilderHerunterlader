package ch.supertomcat.bh.manualtest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import ch.supertomcat.bh.BH;
import ch.supertomcat.bh.cookies.CookieManager;
import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.hoster.IRedirect;
import ch.supertomcat.bh.hoster.hostimpl.HostRules;
import ch.supertomcat.bh.hoster.hostimpl.HostSortImages;
import ch.supertomcat.bh.hoster.hostimpl.HostzDefaultFiles;
import ch.supertomcat.bh.queue.DownloadRestriction;
import ch.supertomcat.bh.queue.RestrictionAccess;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.update.UpdatesXmlIO;
import ch.supertomcat.bh.updates.xml.UpdateData;
import ch.supertomcat.bh.updates.xml.Updates;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.application.ApplicationUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;
import jakarta.xml.bind.JAXBException;

@SuppressWarnings("javadoc")
public class UpdateUpdatesXMLTest {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private HostManager hostManager;

	private UpdatesXmlIO updatesXmlIO;

	@BeforeAll
	public static void beforeAll() throws IOException {
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
	}

	@BeforeEach
	public void beforeTest() throws IOException, SAXException, JAXBException {
		SettingsManager settingsManager = new SettingsManager(ApplicationProperties.getProperty(ApplicationMain.SETTINGS_PATH), "BH-settings.xml", "settings.xml");
		settingsManager.readSettings();
		CookieManager cookieManager = new CookieManager(settingsManager);
		ProxyManager proxyManager = new ProxyManager(settingsManager, cookieManager);
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
		updatesXmlIO = new UpdatesXmlIO();
	}

	@Test
	public void testUpdateUpdatesXML() throws IOException, JAXBException {
		File updatesXmlFile = new File("updatev6.xml");
		logger.info("Loading Updates XML File: {}", updatesXmlFile.getAbsolutePath());
		Updates updates = updatesXmlIO.readUpdates(updatesXmlFile.getAbsolutePath(), true);

		List<Class<?>> internalHostClasses = Arrays.asList(HostRules.class, HostSortImages.class, HostzDefaultFiles.class);

		Map<String, UpdateData> redirectUpdates = new HashMap<>();
		Map<String, UpdateData> hostUpdates = new HashMap<>();
		Map<String, UpdateData> ruleUpdates = new HashMap<>();
		for (UpdateData redicrect : updates.getRedirectUpdates().getRedirect()) {
			redirectUpdates.put(redicrect.getName(), redicrect);
		}
		for (UpdateData host : updates.getHosterUpdates().getHost()) {
			hostUpdates.put(host.getName(), host);
		}
		for (UpdateData rule : updates.getRuleUpdates().getRule()) {
			ruleUpdates.put(rule.getName(), rule);
		}

		for (IRedirect redirect : hostManager.getRedirectManager().getRedirects()) {
			if (internalHostClasses.contains(redirect.getClass())) {
				continue;
			}

			UpdateData redirectUpdate = redirectUpdates.get(redirect.getName());
			if (redirectUpdate != null) {
				if (!redirectUpdate.getVersion().equals(redirect.getVersion())) {
					logger.info("Updated Version of Redirect {}: {} -> {}", redirectUpdate.getName(), redirectUpdate.getVersion(), redirect.getVersion());
					redirectUpdate.setVersion(redirect.getVersion());
				}
			} else {
				UpdateData newRedirectUpdate = new UpdateData();
				newRedirectUpdate.setName(redirect.getName());
				newRedirectUpdate.setVersion(redirect.getVersion());
				newRedirectUpdate.setSrc("TODO");
				newRedirectUpdate.setFilename(redirect.getClass().getSimpleName() + ".class");
				updates.getRedirectUpdates().getRedirect().add(newRedirectUpdate);
				logger.info("Added Redirect {}: {}", newRedirectUpdate.getName(), newRedirectUpdate.getVersion());
			}
		}

		for (Host host : hostManager.getHosters()) {
			if (host.isDeveloper() || internalHostClasses.contains(host.getClass())) {
				continue;
			}

			UpdateData hostUpdate = hostUpdates.get(host.getName());
			if (hostUpdate != null) {
				if (!hostUpdate.getVersion().equals(host.getVersion())) {
					logger.info("Updated Version of Host {}: {} -> {}", hostUpdate.getName(), hostUpdate.getVersion(), host.getVersion());
					hostUpdate.setVersion(host.getVersion());
				}
			} else {
				UpdateData newHostUpdate = new UpdateData();
				newHostUpdate.setName(host.getName());
				newHostUpdate.setVersion(host.getVersion());
				newHostUpdate.setSrc("TODO");
				newHostUpdate.setFilename(host.getClass().getSimpleName() + ".class");
				updates.getHosterUpdates().getHost().add(newHostUpdate);
				logger.info("Added Host {}: {}", newHostUpdate.getName(), newHostUpdate.getVersion());
			}
		}

		for (Rule rule : hostManager.getHostRules().getRules()) {
			if (rule.isDeveloper()) {
				continue;
			}

			UpdateData ruleUpdate = ruleUpdates.get(rule.getFile().getFileName().toString());
			if (ruleUpdate != null) {
				if (!ruleUpdate.getVersion().equals(rule.getVersion())) {
					logger.info("Updated Version of Rule {}: {} -> {}", ruleUpdate.getName(), ruleUpdate.getVersion(), rule.getVersion());
					ruleUpdate.setVersion(rule.getVersion());
				}
			} else {
				UpdateData newRuleUpdate = new UpdateData();
				newRuleUpdate.setName(rule.getFile().getFileName().toString());
				newRuleUpdate.setVersion(rule.getVersion());
				newRuleUpdate.setSrc("TODO");
				newRuleUpdate.setFilename(rule.getFile().getFileName().toString());
				updates.getRuleUpdates().getRule().add(newRuleUpdate);
				logger.info("Added Rule {}: {}", newRuleUpdate.getName(), newRuleUpdate.getVersion());
			}
		}

		logger.info("Writing Updates XML File: {}", updatesXmlFile.getAbsolutePath());
		updatesXmlIO.writeUpdates(updatesXmlFile.getAbsolutePath(), updates);
		fixFormat(updatesXmlFile);
	}

	private void fixFormat(File updatesXmlFile) throws IOException {
		List<String> lines = Files.readAllLines(updatesXmlFile.toPath(), StandardCharsets.UTF_8);

		try (FileOutputStream out = new FileOutputStream(updatesXmlFile); OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
			for (String line : lines) {
				String formattedLine = line.replace("    ", "\t").replace("/>", " />");
				writer.write(formattedLine);
				writer.write("\n");
				writer.flush();

				if (line.contains("<changelog>")) {
					writer.write("\t\t<!--\n");
					writer.write("\t\t<changes version=\"4.9.0\"></changes>\n");
					writer.write("\t\t-->\n");
					writer.flush();
				}
			}
		}
	}
}

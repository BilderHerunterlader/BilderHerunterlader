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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

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
import ch.supertomcat.bh.hoster.hostimpl.HostRules;
import ch.supertomcat.bh.hoster.hostimpl.HostSortImages;
import ch.supertomcat.bh.hoster.hostimpl.HostzDefaultFiles;
import ch.supertomcat.bh.queue.DownloadRestriction;
import ch.supertomcat.bh.queue.RestrictionAccess;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.update.sources.httpxml.UpdatesXmlIO;
import ch.supertomcat.bh.update.sources.httpxml.xml.UpdateData;
import ch.supertomcat.bh.update.sources.httpxml.xml.Updates;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.application.ApplicationUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;

@SuppressWarnings("javadoc")
public class UpdateUpdatesXMLTest {
	private static final Pattern RULE_PATTERN = Pattern.compile("filename=\"Rule.+?\\.xml\"");

	private Logger logger = LoggerFactory.getLogger(getClass());

	private HostManager hostManager;

	private UpdatesXmlIO updatesXmlIO;

	@BeforeAll
	public static void beforeAll() throws IOException {
		try (InputStream in = BH.class.getResourceAsStream("/Application_Config.properties")) {
			ApplicationProperties.initProperties(in);

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
	}

	@BeforeEach
	public void beforeTest() throws IOException, SAXException, JAXBException {
		SettingsManager settingsManager = new SettingsManager(ApplicationProperties.getProperty("SettingsPath"), "BH-settings.xml", "settings.xml");
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
		updatesXmlIO = new UpdatesXmlIO();
	}

	@Test
	public void testUpdateUpdatesXML() throws IOException, JAXBException {
		File updatesXmlFile = new File("updatev6.xml");
		logger.info("Loading Updates XML File: {}", updatesXmlFile.getAbsolutePath());
		Updates updates = updatesXmlIO.readUpdates(updatesXmlFile.getAbsolutePath());

		List<Class<?>> internalHostClasses = Arrays.asList(HostRules.class, HostSortImages.class, HostzDefaultFiles.class);

		Map<String, UpdateData> hostUpdates = new HashMap<>();
		Map<String, UpdateData> ruleUpdates = new HashMap<>();
		for (UpdateData host : updates.getHoster().getHost()) {
			if (host.getFilename().endsWith(".xml")) {
				ruleUpdates.put(host.getName(), host);
			} else {
				hostUpdates.put(host.getName(), host);
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
				updates.getHoster().getHost().add(newHostUpdate);
				logger.info("Added Host {}: {}", newHostUpdate.getName(), newHostUpdate.getVersion());
			}
		}

		for (Rule rule : hostManager.getHostRules().getRules()) {
			if (rule.isDeveloper()) {
				continue;
			}

			UpdateData ruleUpdate = ruleUpdates.get(rule.getFile().getName());
			if (ruleUpdate != null) {
				if (!ruleUpdate.getVersion().equals(rule.getVersion())) {
					logger.info("Updated Version of Rule {}: {} -> {}", ruleUpdate.getName(), ruleUpdate.getVersion(), rule.getVersion());
					ruleUpdate.setVersion(rule.getVersion());
				}
			} else {
				UpdateData newRuleUpdate = new UpdateData();
				newRuleUpdate.setName(rule.getFile().getName());
				newRuleUpdate.setVersion(rule.getVersion());
				newRuleUpdate.setSrc("TODO");
				newRuleUpdate.setFilename(rule.getFile().getName());
				updates.getHoster().getHost().add(newRuleUpdate);
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
			boolean firstRuleFound = false;
			for (String line : lines) {
				if (!firstRuleFound) {
					Matcher ruleMatcher = RULE_PATTERN.matcher(line);
					if (ruleMatcher.find()) {
						writer.write("\t\t<!-- End of Host-Classes -->\n");
						writer.write("\t\t<!-- Start of Rules -->\n");
						writer.flush();
						firstRuleFound = true;
					}
				}

				if (line.contains("</hoster>")) {
					writer.write("\t\t<!-- End of Rules -->\n");
					writer.flush();
				}

				String formattedLine = line.replace("    ", "\t").replace("/>", " />");
				writer.write(formattedLine);
				writer.write("\n");
				writer.flush();

				if (line.contains("<hoster>")) {
					writer.write("\t\t<!-- Start of Host-Classes -->\n");
					writer.flush();
				} else if (line.contains("<changelog>")) {
					writer.write("\t\t<!--\n");
					writer.write("\t\t<changes version=\"4.9.0\" lng=\"de\"></changes>\n");
					writer.write("\t\t<changes version=\"4.9.0\" lng=\"en\"></changes>\n");
					writer.write("\t\t-->\n");
					writer.flush();
				}
			}
		}
	}
}

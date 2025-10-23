package ch.supertomcat.bh.manualtest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import ch.supertomcat.bh.BH;
import ch.supertomcat.bh.update.UpdatesXmlIO;
import ch.supertomcat.bh.updates.xml.Updates;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.application.ApplicationUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;
import jakarta.xml.bind.JAXBException;

@SuppressWarnings("javadoc")
public class UpdateXMLTest {
	private Logger logger = LoggerFactory.getLogger(getClass());

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
		updatesXmlIO = new UpdatesXmlIO();
	}

	@Test
	public void testUpdateUpdatesXML() throws IOException, JAXBException {
		File updatesXmlFile = new File("updatev7.xml");
		logger.info("Loading Updates XML File: {}", updatesXmlFile.getAbsolutePath());
		Updates updates = updatesXmlIO.readUpdates(updatesXmlFile.getAbsolutePath(), true);
		logger.info("{}", updates);
	}
}

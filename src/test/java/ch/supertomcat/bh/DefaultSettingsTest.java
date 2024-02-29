package ch.supertomcat.bh;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import ch.supertomcat.bh.settings.xml.ObjectFactory;
import ch.supertomcat.bh.settings.xml.Settings;

/**
 * Default Settings Test
 */
@SuppressWarnings("javadoc")
public class DefaultSettingsTest {
	@Test
	public void testTemplate() throws IOException, SAXException, JAXBException {
		Settings settings = loadSettingsFile("/ch/supertomcat/bh/settings/default-settings.xml");
		assertNotNull(settings.getDirectorySettings());
		assertNotNull(settings.getConnectionSettings());
		assertNotNull(settings.getGuiSettings());
		assertNotNull(settings.getHostsSettings());
		assertNotNull(settings.getDownloadSettings());
		assertNotNull(settings.getKeywordsSettings());
		assertNotNull(settings.getLogLevel());
		assertNotNull(settings.getHosterSettings());

		assertNotNull(settings.getDownloadSettings().isReduceFilenameLength());
		assertNotNull(settings.getDownloadSettings().isReducePathLength());
		assertTrue(settings.getDownloadSettings().isReduceFilenameLength());
		assertTrue(settings.getDownloadSettings().isReducePathLength());
	}

	private Settings loadSettingsFile(String resourceFile) throws IOException, SAXException, JAXBException {
		try (InputStream in = getClass().getResourceAsStream(resourceFile)) {
			if (in == null) {
				throw new IllegalArgumentException("Resource not found: " + resourceFile);
			}

			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(getClass().getResource("/ch/supertomcat/bh/settings/settings.xsd"));

			JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			unmarshaller.setSchema(schema);
			return (Settings)unmarshaller.unmarshal(in);
		}
	}
}

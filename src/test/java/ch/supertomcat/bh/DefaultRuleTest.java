package ch.supertomcat.bh;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import ch.supertomcat.bh.rules.xml.ObjectFactory;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

/**
 * Default Settings Test
 */
@SuppressWarnings("javadoc")
public class DefaultRuleTest {
	@Test
	public void testTemplate() throws IOException, SAXException, JAXBException {
		RuleDefinition rule = loadRuleFile("/ch/supertomcat/bh/rules/NewRule.xml");
		assertNotNull(rule.getUserAgent());
		assertEquals("", rule.getUserAgent());
	}

	private RuleDefinition loadRuleFile(String resourceFile) throws IOException, SAXException, JAXBException {
		try (InputStream in = getClass().getResourceAsStream(resourceFile)) {
			if (in == null) {
				throw new IllegalArgumentException("Resource not found: " + resourceFile);
			}

			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(getClass().getResource("/ch/supertomcat/bh/rules/rule.xsd"));

			JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			unmarshaller.setSchema(schema);
			return (RuleDefinition)unmarshaller.unmarshal(in);
		}
	}
}

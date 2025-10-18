package ch.supertomcat.bh.update.sources.httpxml;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import ch.supertomcat.bh.update.sources.httpxml.xml.ObjectFactory;
import ch.supertomcat.bh.update.sources.httpxml.xml.Updates;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * Class for reading and writing the Updates XML File
 */
public class UpdatesXmlIO {
	/**
	 * Unmarshaller
	 */
	private final Unmarshaller unmarshaller;

	/**
	 * Unmarshaller
	 */
	private final Unmarshaller unmarshallerValidated;

	/**
	 * Marshaller
	 */
	private final Marshaller marshaller;

	/**
	 * Constructor
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws JAXBException
	 */
	public UpdatesXmlIO() throws IOException, SAXException, JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);

		Schema schema;
		try (InputStream schemaIn = getClass().getResourceAsStream("/ch/supertomcat/bh/update/sources/httpxml/updates.xsd")) {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Source schemaSource = new StreamSource(schemaIn);
			schema = sf.newSchema(schemaSource);
		}

		unmarshallerValidated = jaxbContext.createUnmarshaller();
		unmarshallerValidated.setSchema(schema);

		unmarshaller = jaxbContext.createUnmarshaller();

		marshaller = jaxbContext.createMarshaller();
		marshaller.setSchema(schema);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	}

	/**
	 * Read updates from XML file
	 * 
	 * @param file XML File
	 * @param validate True if XSD validation should be done, false otherwise
	 * @return Updates
	 * @throws IOException
	 * @throws JAXBException
	 */
	public Updates readUpdates(String file, boolean validate) throws IOException, JAXBException {
		try (FileInputStream in = new FileInputStream(file)) {
			return readUpdates(in, validate);
		}
	}

	/**
	 * Read updates from XML file
	 * 
	 * @param in Input Stream
	 * @param validate True if XSD validation should be done, false otherwise
	 * @return Updates
	 * @throws JAXBException
	 */
	public Updates readUpdates(InputStream in, boolean validate) throws JAXBException {
		if (validate) {
			synchronized (unmarshallerValidated) {
				return unmarshallerValidated.unmarshal(new StreamSource(in), Updates.class).getValue();
			}
		} else {
			synchronized (unmarshaller) {
				return unmarshaller.unmarshal(new StreamSource(in), Updates.class).getValue();
			}
		}
	}

	/**
	 * Saves the updates to the XML-File
	 * 
	 * @param file XML File
	 * @param updates Updates
	 * 
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void writeUpdates(String file, Updates updates) throws IOException, JAXBException {
		Path folder = Paths.get(file).getParent();
		if (folder != null) {
			Files.createDirectories(folder);
		}

		try (FileOutputStream out = new FileOutputStream(file)) {
			synchronized (marshaller) {
				marshaller.marshal(updates, out);
			}
		}
	}
}

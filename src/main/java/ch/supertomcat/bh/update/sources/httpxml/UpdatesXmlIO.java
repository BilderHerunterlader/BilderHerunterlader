package ch.supertomcat.bh.update.sources.httpxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import ch.supertomcat.bh.update.sources.httpxml.xml.ObjectFactory;
import ch.supertomcat.bh.update.sources.httpxml.xml.Updates;

/**
 * Class for reading and writing the Updates XML File
 */
public class UpdatesXmlIO {
	/**
	 * Unmarshaller
	 */
	private final Unmarshaller unmarshaller;

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

		unmarshaller = jaxbContext.createUnmarshaller();
		unmarshaller.setSchema(schema);

		marshaller = jaxbContext.createMarshaller();
		marshaller.setSchema(schema);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	}

	/**
	 * Read updates from XML file
	 * 
	 * @param file XML File
	 * @return Updates
	 * @throws IOException
	 * @throws JAXBException
	 */
	public Updates readUpdates(String file) throws IOException, JAXBException {
		try (FileInputStream in = new FileInputStream(file)) {
			return readUpdates(in);
		}
	}

	/**
	 * Read updates from XML file
	 * 
	 * @param in Input Stream
	 * @return Updates
	 * @throws JAXBException
	 */
	public Updates readUpdates(InputStream in) throws JAXBException {
		synchronized (unmarshaller) {
			return unmarshaller.unmarshal(new StreamSource(in), Updates.class).getValue();
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
		File folder = new File(file).getParentFile();
		if (folder != null) {
			Files.createDirectories(folder.toPath());
		}

		try (FileOutputStream out = new FileOutputStream(file)) {
			synchronized (marshaller) {
				marshaller.marshal(updates, out);
			}
		}
	}
}

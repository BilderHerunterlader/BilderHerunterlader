package ch.supertomcat.bh.rules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import ch.supertomcat.bh.rules.xml.ObjectFactory;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * Class for reading and writing rules from xml files
 */
public class RuleIO {
	/**
	 * Unmarshaller
	 */
	private final Unmarshaller unmarshaller;

	/**
	 * Validated Unmarshaller
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
	public RuleIO() throws IOException, SAXException, JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);

		Schema schema;
		try (InputStream schemaIn = getClass().getResourceAsStream("/ch/supertomcat/bh/rules/rule.xsd")) {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Source schemaSource = new StreamSource(schemaIn);
			schema = sf.newSchema(schemaSource);
		}

		/*
		 * No Schema is set for unmarshaller so that backward compatibility is no problem.
		 */
		unmarshaller = jaxbContext.createUnmarshaller();
		/*
		 * Unmarshaller with validation is only used to read default rule
		 */
		unmarshallerValidated = jaxbContext.createUnmarshaller();
		unmarshallerValidated.setSchema(schema);

		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	}

	/**
	 * Read rule from XML file
	 * 
	 * @param file XML File
	 * @return Rule Definition
	 * @throws IOException
	 * @throws JAXBException
	 */
	public RuleDefinition readRule(String file) throws IOException, JAXBException {
		RuleDefinition ruleDefinition = readRuleNewFormat(file);
		applyDefinitionUpdate(ruleDefinition);
		return ruleDefinition;
	}

	/**
	 * Read Default Rule
	 * 
	 * @return Default Rule
	 * @throws IOException
	 * @throws JAXBException
	 */
	public RuleDefinition readDefaultRule() throws IOException, JAXBException {
		try (InputStream in = getClass().getResourceAsStream("NewRule.xml")) {
			return readRuleNewFormat(in, true);
		}
	}

	/**
	 * Read rule in new format from XML file
	 * 
	 * @param file XML File
	 * @return Rule Definition
	 * @throws IOException
	 * @throws JAXBException
	 */
	private RuleDefinition readRuleNewFormat(String file) throws IOException, JAXBException {
		try (FileInputStream in = new FileInputStream(file)) {
			return readRuleNewFormat(in, false);
		}
	}

	/**
	 * Read rule in new format from XML file
	 * 
	 * @param in Input Stream
	 * @param validate True if validate xml, false otherwise
	 * @return Rule Definition
	 * @throws JAXBException
	 */
	private RuleDefinition readRuleNewFormat(InputStream in, boolean validate) throws JAXBException {
		if (validate) {
			synchronized (unmarshallerValidated) {
				return unmarshallerValidated.unmarshal(new StreamSource(in), RuleDefinition.class).getValue();
			}
		} else {
			synchronized (unmarshaller) {
				return unmarshaller.unmarshal(new StreamSource(in), RuleDefinition.class).getValue();
			}
		}
	}

	/**
	 * If the rule format changed, then there might be some values to set after the rule was read from the file. This method should set everything, which is
	 * necessary.
	 * 
	 * @param ruleDefinition Rule Definition
	 */
	private void applyDefinitionUpdate(RuleDefinition ruleDefinition) {
		if (ruleDefinition.getUserAgent() == null) {
			ruleDefinition.setUserAgent("");
		}
	}

	/**
	 * Saves the rule to the XML-File
	 * 
	 * @param rule Rule
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void writeRule(Rule rule) throws IOException, JAXBException {
		writeRule(rule.getFile().getAbsolutePath(), rule.getDefinition());
	}

	/**
	 * Saves the rule to the XML-File
	 * 
	 * @param file XML File
	 * @param ruleDefinition RuleDefinition
	 * 
	 * @throws IOException
	 * @throws JAXBException
	 */
	private void writeRule(String file, RuleDefinition ruleDefinition) throws IOException, JAXBException {
		File folder = new File(file).getParentFile();
		if (folder != null) {
			Files.createDirectories(folder.toPath());
		}

		try (FileOutputStream out = new FileOutputStream(file)) {
			synchronized (marshaller) {
				marshaller.marshal(ruleDefinition, out);
			}
		}
	}
}

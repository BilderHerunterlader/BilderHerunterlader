package ch.supertomcat.bh.rules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import ch.supertomcat.bh.rules.xml.FailureType;
import ch.supertomcat.bh.rules.xml.FailuresPipeline;
import ch.supertomcat.bh.rules.xml.FilenameDownloadSelectionMode;
import ch.supertomcat.bh.rules.xml.FilenameDownloadSelectionPipeline;
import ch.supertomcat.bh.rules.xml.FilenameMode;
import ch.supertomcat.bh.rules.xml.FilenamePipeline;
import ch.supertomcat.bh.rules.xml.ObjectFactory;
import ch.supertomcat.bh.rules.xml.Pipeline;
import ch.supertomcat.bh.rules.xml.Restriction;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import ch.supertomcat.bh.rules.xml.RuleRegex;
import ch.supertomcat.bh.rules.xml.URLJavascriptPipeline;
import ch.supertomcat.bh.rules.xml.URLMode;
import ch.supertomcat.bh.rules.xml.URLPipeline;
import ch.supertomcat.bh.rules.xml.URLRegexPipeline;

/**
 * Class for reading and writing rules from xml files
 */
public class RuleIO {
	/**
	 * Replace in Container-URL or Thumbnail-URL
	 */
	private static final int OLD_RULE_MODE_CONTAINER_OR_THUMBNAIL_URL = 0;

	/**
	 * Replace in Container-Page-Sourcecode
	 */
	private static final int OLD_RULE_MODE_CONTAINER_PAGE_SOURCECODE = 1;

	/**
	 * Replace Filename
	 */
	private static final int OLD_RULE_MODE_FILENAME = 2;

	/**
	 * Replace Filename
	 */
	private static final int OLD_RULE_MODE_FILENAME_ON_DOWNLOAD_SELECTION = 3;

	/**
	 * RULE_MODE_JAVASCRIPT
	 */
	private static final int OLD_RULE_MODE_JAVASCRIPT = 5;

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

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
		marshaller.setSchema(schema);
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
		if (checkNewFormat(file)) {
			return readRuleNewFormat(file);
		} else {
			return readRuleOldFormat(file);
		}
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
	 * Check if the XML file is in new format
	 * 
	 * @param file XML File
	 * @return True if XML file is in new format, false otherwise
	 * @throws IOException
	 */
	public boolean checkNewFormat(String file) throws IOException {
		try (FileInputStream in = new FileInputStream(file); BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("<ruleDefinition")) {
					return true;
				}
			}
		}
		return false;
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
	 * @throws IOException
	 * @throws JAXBException
	 */
	private RuleDefinition readRuleNewFormat(InputStream in, boolean validate) throws IOException, JAXBException {
		if (validate) {
			return unmarshallerValidated.unmarshal(new StreamSource(in), RuleDefinition.class).getValue();
		} else {
			return unmarshaller.unmarshal(new StreamSource(in), RuleDefinition.class).getValue();
		}
	}

	/**
	 * Read the rule from the XML-File
	 * 
	 * @param strFile XML File
	 * @return True if successful
	 * @throws IOException
	 */
	private RuleDefinition readRuleOldFormat(String strFile) throws IOException {
		File file = new File(strFile);
		try {
			// Create new XML-Parser
			SAXBuilder b = new SAXBuilder();
			// Parse the file
			Document doc = b.build(file);

			// Check if there are all required elements
			Element root = doc.getRootElement();
			if (!(root.getName().equals("rule"))) {
				doc = null;
				root = null;
				b = null;
				throw new IOException("Could not load rule, because root tag is wrong: " + file);
			}

			RuleDefinition ruleDefinition = new RuleDefinition();

			ruleDefinition.setName(root.getAttributeValue("name"));
			ruleDefinition.setVersion(root.getAttributeValue("version"));

			// Set Default Values
			// TODO Maybe this can be done in XSD as much as possible?
			ruleDefinition.setRedirect(false);
			ruleDefinition.setResend(false);
			ruleDefinition.setUseContentDisposition(false);
			ruleDefinition.setReducePathLength(true);
			ruleDefinition.setReduceFilenameLength(true);
			ruleDefinition.setReferrerMode(ch.supertomcat.bh.rules.xml.ReferrerMode.NO_REFERRER);
			ruleDefinition.setDownloadReferrerMode(ch.supertomcat.bh.rules.xml.ReferrerMode.LAST_CONTAINER_URL);
			ruleDefinition.setCustomReferrer("");
			ruleDefinition.setDownloadCustomReferrer("");
			ruleDefinition.setSendCookies(true);
			ruleDefinition.setDuplicateRemoveMode(ch.supertomcat.bh.rules.xml.DuplicateRemoveMode.DEFAULT);
			Restriction restriction = new Restriction();
			restriction.setMaxConnections(0);
			ruleDefinition.setRestriction(restriction);
			FilenamePipeline filenamePipeline = new FilenamePipeline();
			filenamePipeline.setMode(FilenameMode.CONTAINER_URL_FILENAME_PART);
			ruleDefinition.setFilenamePipeline(filenamePipeline);
			FilenameDownloadSelectionPipeline filenameDownloadSelectionPipeline = new FilenameDownloadSelectionPipeline();
			filenameDownloadSelectionPipeline.setMode(FilenameDownloadSelectionMode.CONTAINER_URL_FILENAME_PART);
			ruleDefinition.setFilenameDownloadSelectionPipeline(filenameDownloadSelectionPipeline);

			try {
				ruleDefinition.setRedirect(Boolean.parseBoolean(root.getAttributeValue("redirect")));
			} catch (Exception exc) {
			}
			try {
				ruleDefinition.setResend(Boolean.parseBoolean(root.getAttributeValue("resend")));
			} catch (Exception exc) {
			}
			try {
				ruleDefinition.setUseContentDisposition(Boolean.parseBoolean(root.getAttributeValue("usecontentdisposition")));
			} catch (Exception exc) {
			}
			try {
				String strReducePathLength = root.getAttributeValue("reducePathLength");
				if (strReducePathLength != null) {
					ruleDefinition.setReducePathLength(Boolean.parseBoolean(strReducePathLength));
				}
			} catch (Exception exc) {
			}
			try {
				String strReduceFilenameLength = root.getAttributeValue("reduceFilenameLength");
				if (strReduceFilenameLength != null) {
					ruleDefinition.setReduceFilenameLength(Boolean.parseBoolean(strReduceFilenameLength));
				}
			} catch (Exception exc) {
			}
			try {
				int iRef = Integer.parseInt(root.getAttributeValue("referrermode"));
				ruleDefinition.setReferrerMode(mapReferrerMode(iRef));
			} catch (Exception exc) {
			}
			try {
				int iRef = Integer.parseInt(root.getAttributeValue("referrermodedownload"));
				ruleDefinition.setDownloadReferrerMode(mapReferrerMode(iRef));
			} catch (Exception exc) {
			}
			try {
				if (root.getAttributeValue("customreferrer") != null) {
					ruleDefinition.setCustomReferrer(root.getAttributeValue("customreferrer"));
				}
			} catch (Exception exc) {
			}
			try {
				if (root.getAttributeValue("customreferrerdownload") != null) {
					ruleDefinition.setDownloadCustomReferrer(root.getAttributeValue("customreferrerdownload"));
				}
			} catch (Exception exc) {
			}
			try {
				if (root.getAttributeValue("duplicateRemoveMode") != null) {
					int iDuplicateRemoveMode = Integer.parseInt(root.getAttributeValue("duplicateRemoveMode"));
					ruleDefinition.setDuplicateRemoveMode(mapDuplicateRemoveMode(iDuplicateRemoveMode));
				}
			} catch (Exception exc) {
			}
			if (root.getAttributeValue("sendCookies") != null) {
				ruleDefinition.setSendCookies(Boolean.parseBoolean(root.getAttributeValue("sendCookies")));
			}

			Element ePipes = root.getChild("pipes");
			if (ePipes != null) {
				List<Element> liPipelines = ePipes.getChildren("pipeline");
				Iterator<Element> it = liPipelines.iterator();
				Element currentElement = null;
				String currentMode = null;
				while (it.hasNext()) {
					currentElement = it.next();
					currentMode = currentElement.getAttributeValue("mode");
					if (currentMode != null) {
						if (currentMode.equals(String.valueOf(OLD_RULE_MODE_CONTAINER_OR_THUMBNAIL_URL)) || currentMode.equals(String.valueOf(OLD_RULE_MODE_CONTAINER_PAGE_SOURCECODE))) {
							URLRegexPipeline urlRegexPipeline = new URLRegexPipeline();
							parsePipelineRegexps(currentElement, urlRegexPipeline);

							// Set default Values
							urlRegexPipeline.setWaitBeforeExecute(0);
							urlRegexPipeline.setUrlDecodeResult(false);
							urlRegexPipeline.setSendCookies(true);

							parseURLPipelineValues(currentElement, urlRegexPipeline);

							if (currentMode.equals(String.valueOf(OLD_RULE_MODE_CONTAINER_OR_THUMBNAIL_URL))) {
								// Set default Value
								urlRegexPipeline.setUrlMode(URLMode.CONTAINER_URL);

								String strURLMode = currentElement.getAttributeValue("urlmode");
								try {
									urlRegexPipeline.setUrlMode(mapURLMode(Integer.parseInt(strURLMode)));
								} catch (NumberFormatException ex) {
									logger.error("Could not parse urlmode: {}", strURLMode, ex);
								}
							}

							ruleDefinition.getPipes().add(urlRegexPipeline);
						} else if (currentMode.equals(String.valueOf(OLD_RULE_MODE_JAVASCRIPT))) {
							URLJavascriptPipeline urlJavascriptPipeline = new URLJavascriptPipeline();

							// Set default values
							urlJavascriptPipeline.setJavascriptCode("");
							urlJavascriptPipeline.setWaitBeforeExecute(0);
							urlJavascriptPipeline.setUrlDecodeResult(false);
							urlJavascriptPipeline.setSendCookies(true);

							String javascriptCode = currentElement.getChildText("javascript");
							if (javascriptCode != null) {
								urlJavascriptPipeline.setJavascriptCode(javascriptCode);
							}

							parseURLPipelineValues(currentElement, urlJavascriptPipeline);

							ruleDefinition.getPipes().add(urlJavascriptPipeline);
						}
					}
				}
			}

			Element ePipesFailures = root.getChild("pipesFailures");
			if (ePipesFailures != null) {
				List<Element> liPipelines = ePipesFailures.getChildren("pipeline");
				Iterator<Element> it = liPipelines.iterator();
				while (it.hasNext()) {
					Element eFailurePipeline = it.next();
					FailuresPipeline failuresPipeline = new FailuresPipeline();

					// Set default values
					failuresPipeline.setFailureType(FailureType.FAILED);
					failuresPipeline.setCheckURL(false);
					failuresPipeline.setCheckThumbURL(false);
					failuresPipeline.setCheckPageSourceCode(false);

					failuresPipeline.setFailureType(mapFailureType(Integer.parseInt(eFailurePipeline.getAttributeValue("failureType"))));
					failuresPipeline.setCheckURL(Boolean.parseBoolean(eFailurePipeline.getAttributeValue("checkURL")));
					failuresPipeline.setCheckThumbURL(Boolean.parseBoolean(eFailurePipeline.getAttributeValue("checkThumbURL")));
					failuresPipeline.setCheckPageSourceCode(Boolean.parseBoolean(eFailurePipeline.getAttributeValue("checkPageSourceCode")));
					parsePipelineRegexps(eFailurePipeline, failuresPipeline);
					ruleDefinition.getFailuresPipes().add(failuresPipeline);
				}
			}

			/*
			 * Now we look for Filename-Pipelines
			 */
			List<Element> li = root.getChildren("pipeline");
			Iterator<Element> it = li.iterator();
			while (it.hasNext()) {
				Element ePipeline = it.next();
				int pipelineMode = Integer.parseInt(ePipeline.getAttributeValue("mode"));
				if (pipelineMode == OLD_RULE_MODE_FILENAME) {
					filenamePipeline.setMode(mapFilenameMode(Integer.parseInt(ePipeline.getAttributeValue("filenamemode"))));
					parsePipelineRegexps(ePipeline, filenamePipeline);
				} else if (pipelineMode == OLD_RULE_MODE_FILENAME_ON_DOWNLOAD_SELECTION) {
					filenameDownloadSelectionPipeline.setMode(mapFilenameDownloadSelectionMode(Integer.parseInt(ePipeline.getAttributeValue("filenameDownloadSelectionMode"))));
					parsePipelineRegexps(ePipeline, filenameDownloadSelectionPipeline);
				}
			}

			ruleDefinition.setUrlPattern(root.getChild("urlpattern").getValue());

			try {
				Element eMaxConnections = root.getChild("maxConnections");
				int iMaxCon = Integer.parseInt(eMaxConnections.getAttributeValue("value"));
				if (iMaxCon > -1) {
					restriction.setMaxConnections(iMaxCon);
				}
				List<Element> liMaxConDomains = eMaxConnections.getChildren("domain");
				Iterator<Element> itMaxConDomains = liMaxConDomains.iterator();
				while (itMaxConDomains.hasNext()) {
					Element eMaxConDomain = itMaxConDomains.next();
					String dom = eMaxConDomain.getAttributeValue("name");
					if (!dom.isEmpty()) {
						restriction.getDomain().add(dom);
					}
				}
			} catch (Exception ex) {
			}

			return ruleDefinition;
		} catch (Exception e) {
			throw new IOException("Could not load rule: " + file, e);
		}
	}

	/**
	 * Saves the rule to the XML-File
	 * 
	 * @param rule Rule
	 * 
	 * @return True if successful
	 */
	public boolean writeRule(Rule rule) {
		/*
		 * TODO Write rule in new format instead of old
		 */
		try {
			return writeRuleOldFormat(rule);
		} catch (IOException e) {
			logger.error("Could not save rule {} {}: {}", rule.getName(), rule.getVersion(), rule.getFile(), e);
			return false;
		}
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
	public void writeRule(String file, RuleDefinition ruleDefinition) throws IOException, JAXBException {
		File folder = new File(file).getParentFile();
		if (folder != null) {
			Files.createDirectories(folder.toPath());
		}

		try (FileOutputStream out = new FileOutputStream(file)) {
			marshaller.marshal(ruleDefinition, out);
		}
	}

	/**
	 * Saves the rule to the XML-File
	 * 
	 * @param rule Rule
	 * 
	 * @return True if successful
	 * @throws IOException
	 */
	private boolean writeRuleOldFormat(Rule rule) throws IOException {
		File folder = rule.getFile().getParentFile();
		if (folder != null) {
			Files.createDirectories(folder.toPath());
		}

		// Get the the Element
		Element root = rule.getXmlElement();
		// Create new document
		Document doc = new Document(root);
		try (FileOutputStream fos = new FileOutputStream(rule.getFile())) {
			// Create new outputter
			XMLOutputter serializer = new XMLOutputter();
			// This will create nice formated xml-file
			serializer.setFormat(Format.getPrettyFormat());
			// Write the data to the file
			serializer.output(doc, fos);
			// Close the file
			fos.flush();
			return true;
		} catch (IOException e) {
			logger.error("Could not save rule {} {}: {}", rule.getName(), rule.getVersion(), rule.getFile(), e);
			return false;
		}
	}

	private ch.supertomcat.bh.rules.xml.ReferrerMode mapReferrerMode(int value) {
		switch (value) {
			case 0:
				return ch.supertomcat.bh.rules.xml.ReferrerMode.NO_REFERRER;
			case 1:
				return ch.supertomcat.bh.rules.xml.ReferrerMode.LAST_CONTAINER_URL;
			case 2:
				return ch.supertomcat.bh.rules.xml.ReferrerMode.FIRST_CONTAINER_URL;
			case 3:
				return ch.supertomcat.bh.rules.xml.ReferrerMode.ORIGIN_PAGE;
			case 4:
				return ch.supertomcat.bh.rules.xml.ReferrerMode.CUSTOM;
			default:
				return ch.supertomcat.bh.rules.xml.ReferrerMode.NO_REFERRER;
		}
	}

	private ch.supertomcat.bh.rules.xml.DuplicateRemoveMode mapDuplicateRemoveMode(int value) {
		switch (value) {
			case 0:
				return ch.supertomcat.bh.rules.xml.DuplicateRemoveMode.DEFAULT;
			case 1:
				return ch.supertomcat.bh.rules.xml.DuplicateRemoveMode.CONTAINER_URL_ONLY;
			case 2:
				return ch.supertomcat.bh.rules.xml.DuplicateRemoveMode.CONTAINER_URL_AND_THUMBNAIL_URL;
			case 3:
				return ch.supertomcat.bh.rules.xml.DuplicateRemoveMode.CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_FIRST;
			case 4:
				return ch.supertomcat.bh.rules.xml.DuplicateRemoveMode.CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_LAST;
			case 5:
				return ch.supertomcat.bh.rules.xml.DuplicateRemoveMode.CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_FIRST;
			case 6:
				return ch.supertomcat.bh.rules.xml.DuplicateRemoveMode.CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_LAST;
			default:
				return ch.supertomcat.bh.rules.xml.DuplicateRemoveMode.DEFAULT;
		}
	}

	private ch.supertomcat.bh.rules.xml.FilenameMode mapFilenameMode(int value) {
		switch (value) {
			case 0:
				return ch.supertomcat.bh.rules.xml.FilenameMode.CONTAINER_URL_FILENAME_PART;
			case 1:
				return ch.supertomcat.bh.rules.xml.FilenameMode.CONTAINER_URL;
			case 2:
				return ch.supertomcat.bh.rules.xml.FilenameMode.THUMBNAIL_URL_FILENAME_PART;
			case 3:
				return ch.supertomcat.bh.rules.xml.FilenameMode.THUMBNAIL_URL;
			case 4:
				return ch.supertomcat.bh.rules.xml.FilenameMode.CONTAINER_PAGE_SOURCECODE;
			case 5:
				return ch.supertomcat.bh.rules.xml.FilenameMode.DOWNLOAD_URL;
			case 6:
				return ch.supertomcat.bh.rules.xml.FilenameMode.DOWNLOAD_URL_FILENAME_PART;
			case 7:
				return ch.supertomcat.bh.rules.xml.FilenameMode.LAST_CONTAINER_URL_FILENAME_PART;
			case 8:
				return ch.supertomcat.bh.rules.xml.FilenameMode.LAST_CONTAINER_URL;
			case 9:
				return ch.supertomcat.bh.rules.xml.FilenameMode.FIRST_CONTAINER_PAGE_SOURCECODE;
			case 10:
				return ch.supertomcat.bh.rules.xml.FilenameMode.LAST_CONTAINER_PAGE_SOURCECODE;
			default:
				return ch.supertomcat.bh.rules.xml.FilenameMode.CONTAINER_URL_FILENAME_PART;
		}
	}

	private ch.supertomcat.bh.rules.xml.FilenameDownloadSelectionMode mapFilenameDownloadSelectionMode(int value) {
		switch (value) {
			case 0:
				return ch.supertomcat.bh.rules.xml.FilenameDownloadSelectionMode.CONTAINER_URL_FILENAME_PART;
			case 1:
				return ch.supertomcat.bh.rules.xml.FilenameDownloadSelectionMode.CONTAINER_URL;
			default:
				return ch.supertomcat.bh.rules.xml.FilenameDownloadSelectionMode.CONTAINER_URL_FILENAME_PART;
		}
	}

	private ch.supertomcat.bh.rules.xml.FailureType mapFailureType(int value) {
		switch (value) {
			case 0:
				return ch.supertomcat.bh.rules.xml.FailureType.SLEEPING;
			case 3:
				return ch.supertomcat.bh.rules.xml.FailureType.COMPLETE;
			case 4:
				return ch.supertomcat.bh.rules.xml.FailureType.FAILED;
			case 6:
				return ch.supertomcat.bh.rules.xml.FailureType.FAILED_FILE_NOT_EXIST;
			case 7:
				return ch.supertomcat.bh.rules.xml.FailureType.FAILED_FILE_TEMPORARY_OFFLINE;
			case 1:
			case 2:
			case 5:
			default:
				return ch.supertomcat.bh.rules.xml.FailureType.FAILED;
		}
	}

	private ch.supertomcat.bh.rules.xml.URLMode mapURLMode(int value) {
		switch (value) {
			case 0:
				return ch.supertomcat.bh.rules.xml.URLMode.CONTAINER_URL;
			case 1:
				return ch.supertomcat.bh.rules.xml.URLMode.THUMBNAIL_URL;
			default:
				return ch.supertomcat.bh.rules.xml.URLMode.CONTAINER_URL;
		}
	}

	private void parsePipelineRegexps(Element e, Pipeline pipeline) {
		for (Element child : e.getChildren("regexp")) {
			try {
				String search = child.getAttributeValue("search");
				String replace = child.getAttributeValue("replace");
				RuleRegex ruleRegex = new RuleRegex();
				ruleRegex.setPattern(search);
				ruleRegex.setReplacement(replace);
				pipeline.getRegexp().add(ruleRegex);
			} catch (Exception ex) {
				logger.error("Could not parse regexp: {}", child, ex);
			}
		}
	}

	private void parseURLPipelineValues(Element currentElement, URLPipeline urlPipeline) {
		String strWaitBeforeExecute = currentElement.getAttributeValue("waitBeforeExecute");
		if (strWaitBeforeExecute != null) {
			try {
				urlPipeline.setWaitBeforeExecute(Integer.parseInt(currentElement.getAttributeValue("waitBeforeExecute")));
			} catch (NumberFormatException ex) {
				logger.error("Could not parse waitBeforeExecute: {}", strWaitBeforeExecute, ex);
			}
		}

		String strUrlDecodeResult = currentElement.getAttributeValue("urlDecodeResult");
		if (strUrlDecodeResult != null) {
			urlPipeline.setUrlDecodeResult(Boolean.parseBoolean(strUrlDecodeResult));
		}

		String strSendCookies = currentElement.getAttributeValue("sendCookies");
		if (strSendCookies != null) {
			urlPipeline.setSendCookies(Boolean.parseBoolean(strSendCookies));
		}
	}
}

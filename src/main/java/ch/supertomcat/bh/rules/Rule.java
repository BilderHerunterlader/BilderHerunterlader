package ch.supertomcat.bh.rules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.Hoster;
import ch.supertomcat.bh.hoster.containerpage.DownloadContainerPageOptions;
import ch.supertomcat.bh.hoster.hosteroptions.DeactivateOption;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.queue.Restriction;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.applicationtool.ApplicationProperties;
import ch.supertomcat.supertomcattools.fileiotools.FileTool;
import ch.supertomcat.supertomcattools.htmltools.HTMLTool;
import ch.supertomcat.supertomcattools.httptools.HTTPTool;
import ch.supertomcat.supertomcattools.settingstools.options.OptionString;

/**
 * A Rule can be defined by the user.
 * The input is then the Container-URL or the Sourcecode of the Container-URL.
 * The user can define if the rule should do a replacement in the Container-URL or
 * download the Container-Page and do a replacement there.
 * For the replacement Regular Expressions are used.
 * There also the possibillity to do a replacement for a nicer filename.
 * 
 * A rule has for all those modes pipelines. So in a pipeline multiple replacements can
 * be defined.
 * 
 * A rule is stored as an XML-File.
 */
public class Rule extends Hoster {
	/**
	 * Logger for this class
	 */
	public Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Name
	 */
	private String name = "";

	/**
	 * Version
	 */
	private String version = "";

	/**
	 * redirect
	 */
	private boolean redirect = false;

	/**
	 * Flag if the URL should be parsed again by the hostermanager after the replacement
	 */
	private boolean resend = false;

	/**
	 * Flag if the filename is used from the content-disposition in http-header
	 */
	private boolean useContentDisposition = false;

	private boolean reducePathLength = true;

	private boolean reduceFilenameLength = true;

	/**
	 * Referrer-Mode
	 */
	private ReferrerMode referrerMode = ReferrerMode.REFERRER_NO_REFERRER;

	/**
	 * referrerModeDownload
	 */
	private ReferrerMode referrerModeDownload = ReferrerMode.REFERRER_LAST_CONTAINER_URL;

	/**
	 * Custom referrer
	 */
	private String customReferrer = "";

	/**
	 * Custom referrer
	 */
	private String customReferrerDownload = "";

	/**
	 * Pattern for the Container-URL
	 */
	private String pattern = "";

	/**
	 * Compiled Pattern
	 */
	private Pattern urlPattern;

	/**
	 * Pipelines
	 */
	private List<RulePipeline> pipelines = new ArrayList<>();

	/**
	 * Pipeline
	 */
	private RulePipeline pipelineFilename = null;

	/**
	 * Pipeline
	 */
	private RulePipeline pipelineFilenameDownloadSelection = null;

	/**
	 * pipelinesFailures
	 */
	private List<RulePipeline> pipelinesFailures = new ArrayList<>();

	/**
	 * sendCookies
	 */
	private boolean sendCookies = true;

	/**
	 * duplicateRemoveMode
	 */
	private DuplicateRemoveMode duplicateRemoveMode = DuplicateRemoveMode.DUPLICATES_BH_DEFAULT;

	/**
	 * maxConnection
	 */
	private int maxConnections = 0;

	/**
	 * maxConnectionDomains
	 */
	private List<String> maxConnectionDomains = new ArrayList<>();

	/**
	 * restriction
	 */
	private Restriction restriction = null;

	private DeactivateOption deactivateOption = null;

	/**
	 * Flag if the object is ok (Pattern could be compiled, and all needed definitions are defined)
	 */
	private boolean statusOK = true;

	/**
	 * Path to the XML-File
	 */
	private String strFile;

	/**
	 * Constructor
	 * 
	 * @param file Path to the XML-File
	 * @param developer True if is developer rule, false otherwise
	 * @throws PatternSyntaxException
	 */
	public Rule(String file, boolean developer) throws PatternSyntaxException {
		this.strFile = file;
		setDeveloper(developer);
		deactivateOption = new DeactivateOption(FileTool.getFilename(this.strFile));
		boolean b = readRule();
		this.urlPattern = Pattern.compile(this.pattern);
		if (!b) {
			statusOK = false;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param name Name
	 * @param version Version
	 * @param pattern Container-URL-Pattern
	 * @param file Path to the XML-File
	 * @throws PatternSyntaxException
	 */
	public Rule(String name, String version, String pattern, String file) throws PatternSyntaxException {
		this.name = name;
		this.version = version;
		this.pattern = pattern;
		this.urlPattern = Pattern.compile(this.pattern);
		// Create Pipelines
		pipelineFilename = new RulePipelineFilename(RuleMode.RULE_MODE_FILENAME);
		pipelineFilenameDownloadSelection = new RulePipelineFilename(RuleMode.RULE_MODE_FILENAME_ON_DOWNLOAD_SELECTION);
		this.strFile = file;
		deactivateOption = new DeactivateOption(FileTool.getFilename(this.strFile));
	}

	/**
	 * Returns if this rule can parse the URL
	 * 
	 * @param url Container-URL
	 * @param sourceRule Last rule which parsed the URL
	 * @return True if this rule can parse the URL
	 */
	public boolean isFromThisHoster(String url, Rule sourceRule) {
		if (sourceRule == this) {
			return false;
		}
		if (deactivateOption.isDeactivated()) {
			return false;
		}
		Matcher urlMatcher = urlPattern.matcher(url);
		if (urlMatcher.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * Returns if the status of the object is ok
	 * 
	 * @return True if ok
	 */
	public boolean getStatusOK() {
		return this.statusOK;
	}

	/**
	 * Returns parsed URL and filename
	 * 
	 * @param upo URLParseObject
	 * @return URL and filename
	 * @throws HostException
	 */
	public String[] getURLAndFilename(URLParseObject upo) throws HostException {
		return getURLAndFilename(upo, null);
	}

	/**
	 * Returns parsed URL and filename
	 * 
	 * @param upo URLParseObject
	 * @param pagesourcecode Container-Page-Source-Code
	 * @return URL and filename
	 * @throws HostException
	 */
	public String[] getURLAndFilename(URLParseObject upo, OptionString pagesourcecode) throws HostException {
		Pic pic = upo.getPic();
		String url = upo.getContainerURL();
		String thumbURL = upo.getThumbURL();
		String retval[] = new String[2];

		if (pipelines.size() == 0) {
			for (RulePipeline failurePipe : pipelinesFailures) {
				failurePipe.checkForFailure(url, thumbURL, "");
			}
		}

		/*
		 * Now we go through the pipelines-Array and the result of one pipeline is used as input for the next pipeline
		 * 
		 * Example:
		 * First pipeline is a RULE_MODE_CONTAINER_OR_THUMBNAIL_URL pipeline.
		 * Second pipeline is a RULE_MODE_CONTAINER_PAGE_SOURCECODE pipeline.
		 * So now the search and replace is done for the first pipeline, which results in a new URL.
		 * That URL is now used for the second pipeline by downloading it's source-code.
		 */
		boolean bHtmlCodeFromFirstURL = false;
		String htmlCodeFromFirstURL = "";

		boolean bHtmlCodeFirst = false;
		String htmlCodeFirst = "";
		String htmlCodeFirstURL = "";
		String htmlCodeFirstReferrer = "";

		boolean bHtmlCodeLast = false;
		String htmlCodeLast = "";
		String htmlCodeLastURL = "";
		String htmlCodeLastReferrer = "";

		String htmlcode = "";

		String pipelineURL = url;
		String pipelineThumbURL = thumbURL;
		String pipelineReferrer = getReferrer(upo);
		String pipelineResult = "";
		for (int i = 0; i < pipelines.size(); i++) {
			int waitBeforeExecute = pipelines.get(i).getWaitBeforeExecute();
			if (waitBeforeExecute > 0) {
				try {
					Thread.sleep(waitBeforeExecute);
				} catch (InterruptedException e) {
				}
			}
			boolean sendCookies = pipelines.get(i).isSendCookies();
			if (pipelines.get(i).getMode() == RuleMode.RULE_MODE_CONTAINER_PAGE_SOURCECODE) {
				htmlcode = downloadContainerPage(this.name, pipelineURL, pipelineReferrer, new DownloadContainerPageOptions(sendCookies, true));
				if (pagesourcecode != null) {
					pagesourcecode.setValue(htmlcode);
				}
				logger.debug(this.name + " -> " + url + " -> Download Container-Page done -> Result: " + htmlcode);
				if (i == 0) {
					htmlCodeFromFirstURL = htmlcode;
					bHtmlCodeFromFirstURL = true;
				}
				if (bHtmlCodeFirst == false) {
					htmlCodeFirst = htmlcode;
					htmlCodeFirstURL = pipelineURL;
					htmlCodeFirstReferrer = pipelineReferrer;
					bHtmlCodeFirst = true;
				}
				htmlCodeLast = htmlcode;
				htmlCodeLastURL = pipelineURL;
				htmlCodeLastReferrer = pipelineReferrer;
				bHtmlCodeLast = true;
			}

			/*
			 * Check for failures before replace
			 */
			for (RulePipeline failurePipe : pipelinesFailures) {
				failurePipe.checkForFailure(pipelineURL, pipelineThumbURL, htmlcode);
			}

			RulePipeline rulePipeline = pipelines.get(i);
			if (rulePipeline instanceof RulePipelineURLRegex) {
				pipelineResult = rulePipeline.getURL(pipelineURL, pipelineThumbURL, htmlcode, pic);
			} else if (rulePipeline instanceof RulePipelineURLJavascript) {
				pipelineResult = rulePipeline.getURLByJavascript(pipelineURL, pipelineThumbURL, htmlcode, upo);
			}

			if (rulePipeline.isUrlDecodeResult()) {
				URLCodec urlCodec = new URLCodec("UTF-8");
				try {
					pipelineResult = urlCodec.decode(pipelineResult);
				} catch (DecoderException e) {
					logger.error("Could not URL decode (application/x-www-form-urlencoded) pipeline result: {}", pipelineResult);
				}
			}

			pipelineResult = HTTPTool.decodeURL(pipelineResult);
			logger.debug(this.name + " -> " + url + " -> pipe[" + i + "] -> Result: " + pipelineResult);

			/*
			 * Check for failures after replace
			 */
			for (RulePipeline failurePipe : pipelinesFailures) {
				failurePipe.checkForFailure(pipelineResult);
			}

			if (i > 0 && i < (pipelines.size() - 1)) {
				upo.setContainerURL(pipelineURL);
			}
			if (i < (pipelines.size() - 1)) {
				pipelineReferrer = pipelineURL;
				pipelineURL = pipelineResult;
				pipelineThumbURL = pipelineResult;
			}
		}

		if (pipelines.size() == 0) {
			pipelineResult = url;
		}

		logger.debug(this.name + " -> " + url + " -> Final URL Result: " + pipelineResult);
		retval[0] = pipelineResult;
		retval[1] = upo.getCorrectedFilename(); // Corrected Filename
		if (pic != null && pic.getTargetFilename().length() == 0) {
			pic.setTargetFilename(getFilenamePart(retval[0]));
		}

		if (pipelineFilename != null && retval[0].length() > 0 && pipelineFilename.getRegexps().size() > 0) {
			// First Container-URL
			if (pipelineFilename.getFilenameMode() == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_CONTAINER_URL_FILENAME_PART) {
				String filename = getFilenamePart(url);
				if (filename.length() > 0) {
					retval[1] = pipelineFilename.getCorrectedFilename(filename, thumbURL, "", pic);
				}
			} else if (pipelineFilename.getFilenameMode() == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_CONTAINER_URL) {
				retval[1] = pipelineFilename.getCorrectedFilename(url, thumbURL, "", pic);

				// Thumbnail-URL
			} else if (pipelineFilename.getFilenameMode() == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_THUMBNAIL_URL_FILENAME_PART) {
				String filename = getFilenamePart(thumbURL);
				if (filename.length() > 0) {
					retval[1] = pipelineFilename.getCorrectedFilename(url, filename, "", pic);
				}
			} else if (pipelineFilename.getFilenameMode() == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_THUMBNAIL_URL) {
				retval[1] = pipelineFilename.getCorrectedFilename(url, thumbURL, "", pic);

				// Container-Page-Sourcecode
			} else if ((pipelineFilename.getFilenameMode() == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_CONTAINER_PAGE_SOURCECODE)
					|| (pipelineFilename.getFilenameMode() == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_FIRST_CONTAINER_PAGE_SOURCECODE)
					|| (pipelineFilename.getFilenameMode() == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_LAST_CONTAINER_PAGE_SOURCECODE)) {
				String filenamePageSourceURL = url;
				String filenamePageSourceReferrer = getReferrer(upo);

				if (bHtmlCodeFromFirstURL && (pipelineFilename.getFilenameMode() == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_FIRST_CONTAINER_PAGE_SOURCECODE)) {
					htmlcode = htmlCodeFromFirstURL;
				} else if (bHtmlCodeFirst && (pipelineFilename.getFilenameMode() == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_CONTAINER_PAGE_SOURCECODE)) {
					htmlcode = htmlCodeFirst;
					filenamePageSourceURL = htmlCodeFirstURL;
					filenamePageSourceReferrer = htmlCodeFirstReferrer;
				} else if (bHtmlCodeLast && (pipelineFilename.getFilenameMode() == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_LAST_CONTAINER_PAGE_SOURCECODE)) {
					htmlcode = htmlCodeLast;
					filenamePageSourceURL = htmlCodeLastURL;
					filenamePageSourceReferrer = htmlCodeLastReferrer;
				} else {
					htmlcode = downloadContainerPage(this.name, filenamePageSourceURL, filenamePageSourceReferrer);
					if (pagesourcecode != null) {
						pagesourcecode.setValue(htmlcode);
					}
				}

				retval[1] = pipelineFilename.getCorrectedFilename(filenamePageSourceURL, thumbURL, htmlcode, pic);
				String encodedFilename = retval[1];
				String encoding = HTMLTool.getEncodingFromSourceCode(htmlcode);
				if (encoding.length() > 0 && isEncodingAvailable(encoding)) {
					try {
						encodedFilename = new String(encodedFilename.getBytes(), encoding);
						retval[1] = encodedFilename;
					} catch (UnsupportedEncodingException e) {
						logger.error(e.getMessage(), e);
					}
				}

				// Download-URL
			} else if (pipelineFilename.getFilenameMode() == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_DOWNLOAD_URL) {
				retval[1] = pipelineFilename.getCorrectedFilename(retval[0], thumbURL, "", pic);
			} else if (pipelineFilename.getFilenameMode() == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_DOWNLOAD_URL_FILENAME_PART) {
				int pos = retval[0].lastIndexOf("/");
				if (pos > -1) {
					String filename = retval[0].substring(pos + 1);
					retval[1] = pipelineFilename.getCorrectedFilename(filename, thumbURL, "", pic);
				}

				// Last Container-URL
			} else if (pipelineFilename.getFilenameMode() == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_LAST_CONTAINER_URL_FILENAME_PART) {
				String filename = getFilenamePart(pipelineURL);
				if (filename.length() > 0) {
					retval[1] = pipelineFilename.getCorrectedFilename(filename, thumbURL, "", pic);
				}
			} else if (pipelineFilename.getFilenameMode() == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_LAST_CONTAINER_URL) {
				retval[1] = pipelineFilename.getCorrectedFilename(pipelineURL, thumbURL, "", pic);
			}
		}
		if (retval[1].length() == 0) {
			retval[1] = getFilenamePart(retval[0]);
		}
		if (pic != null) {
			pic.setRenameWithContentDisposition(useContentDisposition);
		}

		upo.addInfo("sendCookies", sendCookies);
		upo.addInfo("ReducePathLength", reducePathLength);
		upo.addInfo("ReduceFilenameLength", reduceFilenameLength);

		return retval;
	}

	/**
	 * @param encoding Encoding
	 * @return Available
	 */
	private boolean isEncodingAvailable(String encoding) {
		return Charset.availableCharsets().containsKey(encoding);
	}

	private String getFilenamePart(String url) {
		int pos = url.lastIndexOf("/");
		if (pos > -1) {
			try {
				return url.substring(pos + 1);
			} catch (Exception e) {
			}
		}
		return "";
	}

	/**
	 * Returns the referrer to use in downloadContainerPage-Method
	 * 
	 * @param upo URLParseObject
	 * @return Referrer
	 */
	private String getReferrer(final URLParseObject upo) {
		String referrer = "";
		switch (this.referrerMode) {
			case REFERRER_NO_REFERRER:
				referrer = "";
				break;
			case REFERRER_LAST_CONTAINER_URL:
				referrer = upo.getContainerURL();
				break;
			case REFERRER_FIRST_CONTAINER_URL:
				referrer = upo.getFirstContainerURL();
				break;
			case REFERRER_ORIGIN_PAGE:
				referrer = upo.getPic().getThreadURL();
				break;
			case REFERRER_CUSTOM:
				referrer = this.customReferrer;
				break;
			default:
				referrer = "";
				break;
		}
		return referrer;
	}

	/**
	 * Returns the filename after replacement
	 * 
	 * @param url URL
	 * @return Filename
	 */
	public String getFilename(String url) {
		String retval = url;
		if (pipelineFilenameDownloadSelection.getFilenameDownloadSelectionMode() == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_CONTAINER_URL_FILENAME_PART) {
			retval = getFilenamePart(url);
		}
		return pipelineFilenameDownloadSelection.getCorrectedFilenameOnDownloadSelection(retval);
	}

	/**
	 * Sets the restriction in the queue and removes the old restriction
	 */
	private void applyRestriction() {
		if (restriction != null) {
			DownloadQueueManager.instance().removeRestriction(restriction);
		}
		if (maxConnections > 0 && maxConnectionDomains.size() > 0) {
			restriction = new Restriction(maxConnectionDomains, maxConnections);
			DownloadQueueManager.instance().addRestriction(restriction);
		} else {
			restriction = null;
		}
	}

	/**
	 * Read the rule from the XML-File
	 * 
	 * @return True if successful
	 */
	private boolean readRule() {
		Document doc = null;
		File file = new File(strFile);
		try {
			// Create new XML-Parser
			SAXBuilder b = new SAXBuilder();
			// Parse the file
			doc = b.build(file);

			// Check if there are all required elements
			Element root = doc.getRootElement();
			if (!(root.getName().equals("rule"))) {
				doc = null;
				root = null;
				b = null;
				logger.error("Could not load rule, because root tag is wrong: {}", file);
				return false;
			}

			try {
				this.name = root.getAttributeValue("name");
				this.version = root.getAttributeValue("version");
				try {
					this.redirect = Boolean.parseBoolean(root.getAttributeValue("redirect"));
				} catch (Exception exc) {
				}
				try {
					this.resend = Boolean.parseBoolean(root.getAttributeValue("resend"));
				} catch (Exception exc) {
				}
				try {
					this.useContentDisposition = Boolean.parseBoolean(root.getAttributeValue("usecontentdisposition"));
				} catch (Exception exc) {
				}
				try {
					String strReducePathLength = root.getAttributeValue("reducePathLength");
					if (strReducePathLength != null) {
						this.reducePathLength = Boolean.parseBoolean(strReducePathLength);
					}
				} catch (Exception exc) {
				}
				try {
					String strReduceFilenameLength = root.getAttributeValue("reduceFilenameLength");
					if (strReduceFilenameLength != null) {
						this.reduceFilenameLength = Boolean.parseBoolean(strReduceFilenameLength);
					}
				} catch (Exception exc) {
				}
				try {
					int iRef = Integer.parseInt(root.getAttributeValue("referrermode"));
					if (iRef >= 0 && iRef <= 4) {
						this.referrerMode = ReferrerMode.getByValue(iRef);
					}
				} catch (Exception exc) {
				}
				try {
					int iRef = Integer.parseInt(root.getAttributeValue("referrermodedownload"));
					if (iRef >= 0 && iRef <= 4) {
						this.referrerModeDownload = ReferrerMode.getByValue(iRef);
					}
				} catch (Exception exc) {
				}
				try {
					if (root.getAttributeValue("customreferrer") != null) {
						this.customReferrer = root.getAttributeValue("customreferrer");
					}
				} catch (Exception exc) {
				}
				try {
					if (root.getAttributeValue("customreferrerdownload") != null) {
						this.customReferrerDownload = root.getAttributeValue("customreferrerdownload");
					}
				} catch (Exception exc) {
				}
				try {
					if (root.getAttributeValue("duplicateRemoveMode") != null) {
						int iDuplicateRemoveMode = Integer.parseInt(root.getAttributeValue("duplicateRemoveMode"));
						this.duplicateRemoveMode = DuplicateRemoveMode.getByValue(iDuplicateRemoveMode);
					}
				} catch (Exception exc) {
				}
				if (root.getAttributeValue("sendCookies") != null) {
					this.sendCookies = Boolean.parseBoolean(root.getAttributeValue("sendCookies"));
				}
			} catch (Exception ex) {
				logger.error("Could not load rule: {}", file, ex);
				return false;
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
						if (currentMode.equals(String.valueOf(RuleMode.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL.getValue()))
								|| currentMode.equals(String.valueOf(RuleMode.RULE_MODE_CONTAINER_PAGE_SOURCECODE.getValue()))) {
							RulePipeline rp = new RulePipelineURLRegex(currentElement);
							pipelines.add(rp);
						} else if (currentMode.equals(String.valueOf(RuleMode.RULE_MODE_JAVASCRIPT.getValue()))) {
							RulePipeline rp = new RulePipelineURLJavascript(currentElement);
							pipelines.add(rp);
						}
					}
				}
			}

			Element ePipesFailures = root.getChild("pipesFailures");
			if (ePipesFailures != null) {
				List<Element> liPipelines = ePipesFailures.getChildren("pipeline");
				Iterator<Element> it = liPipelines.iterator();
				while (it.hasNext()) {
					RulePipeline rp = new RulePipelineFailures(it.next());
					pipelinesFailures.add(rp);
				}
			}

			/*
			 * Now we look for Filename-Pipelines
			 */
			List<Element> li = root.getChildren("pipeline");
			Iterator<Element> it = li.iterator();
			while (it.hasNext()) {
				RulePipeline rp = new RulePipelineFilename(it.next());
				if (rp.getMode() == RuleMode.RULE_MODE_FILENAME) {
					pipelineFilename = rp;
				} else if (rp.getMode() == RuleMode.RULE_MODE_FILENAME_ON_DOWNLOAD_SELECTION) {
					pipelineFilenameDownloadSelection = rp;
				}
			}

			if (pipelineFilename == null) {
				pipelineFilename = new RulePipelineFilename(RuleMode.RULE_MODE_FILENAME);
			}
			if (pipelineFilenameDownloadSelection == null) {
				pipelineFilenameDownloadSelection = new RulePipelineFilename(RuleMode.RULE_MODE_FILENAME_ON_DOWNLOAD_SELECTION);
			}

			Element ep = root.getChild("urlpattern");
			this.pattern = ep.getValue();

			try {
				Element eMaxConnections = root.getChild("maxConnections");
				int iMaxCon = Integer.parseInt(eMaxConnections.getAttributeValue("value"));
				if (iMaxCon > -1) {
					this.maxConnections = iMaxCon;
				}
				List<Element> liMaxConDomains = eMaxConnections.getChildren("domain");
				Iterator<Element> itMaxConDomains = liMaxConDomains.iterator();
				while (itMaxConDomains.hasNext()) {
					Element eMaxConDomain = itMaxConDomains.next();
					String dom = eMaxConDomain.getAttributeValue("name");
					if (dom.length() > 0) {
						maxConnectionDomains.add(dom);
					}
				}
				applyRestriction();
			} catch (Exception ex) {
			}

			doc = null;
			root = null;
			b = null;
			file = null;
			return true;
		} catch (Exception e) {
			logger.error("Could not load rule: {}", file, e);
			return false;
		}
	}

	/**
	 * Saves the rule to the XML-File
	 * 
	 * @return True if successful
	 */
	public boolean writeRule() {
		File settingsPath = new File(ApplicationProperties.getProperty("ApplicationPath") + "rules");

		// If the directory does not exist, create it
		if (!settingsPath.exists()) {
			settingsPath.mkdirs();
		}

		// Get the the Element
		Element root = getXmlElement();
		// Create new document
		Document doc = new Document(root);
		File file = new File(strFile);
		try (FileOutputStream fos = new FileOutputStream(file)) {
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
			logger.error("Could not save rule {} {}: {}", name, version, file.getAbsolutePath(), e);
			return false;
		}
	}

	/**
	 * Returns the Element for creating the XML-File
	 * 
	 * @return Element
	 */
	private Element getXmlElement() {
		Element e = new Element("rule");
		e.setAttribute("name", this.name);
		e.setAttribute("version", this.version);
		e.setAttribute("redirect", String.valueOf(this.redirect));
		e.setAttribute("resend", String.valueOf(this.resend));
		e.setAttribute("usecontentdisposition", String.valueOf(this.useContentDisposition));
		e.setAttribute("reducePathLength", String.valueOf(this.reducePathLength));
		e.setAttribute("reduceFilenameLength", String.valueOf(this.reduceFilenameLength));
		e.setAttribute("referrermode", String.valueOf(this.referrerMode.getValue()));
		e.setAttribute("referrermodedownload", String.valueOf(this.referrerModeDownload.getValue()));
		e.setAttribute("customreferrer", this.customReferrer);
		e.setAttribute("customreferrerdownload", this.customReferrerDownload);
		e.setAttribute("duplicateRemoveMode", String.valueOf(this.duplicateRemoveMode.getValue()));
		e.setAttribute("sendCookies", String.valueOf(this.sendCookies));
		Element eup = new Element("urlpattern");
		eup.setText(this.pattern);
		e.addContent(eup);

		Element ePipes = new Element("pipes");
		for (int i = 0; i < pipelines.size(); i++) {
			ePipes.addContent(pipelines.get(i).getXmlElement());
		}
		e.addContent(ePipes);
		Element ePipesFailures = new Element("pipesFailures");
		for (int i = 0; i < pipelinesFailures.size(); i++) {
			ePipesFailures.addContent(pipelinesFailures.get(i).getXmlElement());
		}
		e.addContent(ePipesFailures);
		if (this.pipelineFilename != null) {
			e.addContent(this.pipelineFilename.getXmlElement());
		}
		if (this.pipelineFilenameDownloadSelection != null) {
			e.addContent(this.pipelineFilenameDownloadSelection.getXmlElement());
		}
		Element eMaxConnections = new Element("maxConnections");
		eMaxConnections.setAttribute("value", String.valueOf(this.maxConnections));
		for (int i = 0; i < maxConnectionDomains.size(); i++) {
			Element eMaxConnectionDomain = new Element("domain");
			eMaxConnectionDomain.setAttribute("name", maxConnectionDomains.get(i));
			eMaxConnections.addContent(eMaxConnectionDomain);
		}
		e.addContent(eMaxConnections);
		return e;
	}

	/**
	 * Returns the name
	 * 
	 * @return Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name
	 * 
	 * @param name Name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the version
	 * 
	 * @return Version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version
	 * 
	 * @param version Version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Returns the Container-URL-Pattern
	 * 
	 * @return Container-URL-Pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Sets the Container-URL-Pattern
	 * 
	 * @param pattern Container-URL-Pattern
	 * @throws PatternSyntaxException
	 */
	public void setPattern(String pattern) throws PatternSyntaxException {
		this.urlPattern = Pattern.compile(pattern);
		this.pattern = pattern;
	}

	/**
	 * Returns the pipelines
	 * Filename- and Filename-on-Download-Selection pipelines are not included
	 * 
	 * @return Pipelines
	 */
	public List<RulePipeline> getPipelines() {
		return pipelines;
	}

	/**
	 * Returns the pipelinesFailures
	 * 
	 * @return pipelinesFailures
	 */
	public List<RulePipeline> getPipelinesFailures() {
		return pipelinesFailures;
	}

	/**
	 * Returns the pipelineFilename
	 * 
	 * @return pipelineFilename
	 */
	public RulePipeline getPipelineFilename() {
		return pipelineFilename;
	}

	/**
	 * Returns the pipelineFilenameDownloadSelection
	 * 
	 * @return pipelineFilenameDownloadSelection
	 */
	public RulePipeline getPipelineFilenameDownloadSelection() {
		return pipelineFilenameDownloadSelection;
	}

	/**
	 * Returns the path of the XML-File
	 * 
	 * @return Path of the XML-File
	 */
	public File getFile() {
		return new File(strFile);
	}

	/**
	 * Sets the path of the XML-File
	 * 
	 * @param file Path of the XML-File
	 */
	public void setFile(File file) {
		this.strFile = file.getAbsolutePath();
	}

	/**
	 * Returns the redirect
	 * 
	 * @return redirect
	 */
	public boolean isRedirect() {
		return redirect;
	}

	/**
	 * Sets the redirect
	 * 
	 * @param redirect redirect
	 */
	public void setRedirect(boolean redirect) {
		this.redirect = redirect;
	}

	/**
	 * Returns if the URL must again be parsed by the HostManager
	 * 
	 * @return True if the URL must again be parsed by the HostManager
	 */
	public boolean isResend() {
		return resend;
	}

	/**
	 * Sets the flag
	 * 
	 * @param resend Flag if the URL must again be parsed by the HostManager
	 */
	public void setResend(boolean resend) {
		this.resend = resend;
	}

	/**
	 * Returns the useContentDisposition
	 * 
	 * @return useContentDisposition
	 */
	public boolean isUseContentDisposition() {
		return useContentDisposition;
	}

	/**
	 * Sets the useContentDisposition
	 * 
	 * @param useContentDisposition useContentDisposition
	 */
	public void setUseContentDisposition(boolean useContentDisposition) {
		this.useContentDisposition = useContentDisposition;
	}

	/**
	 * Returns the referrerMode
	 * 
	 * @return referrerMode
	 */
	public ReferrerMode getReferrerMode() {
		return referrerMode;
	}

	/**
	 * Sets the referrerMode
	 * 
	 * @param referrerMode referrerMode
	 */
	public void setReferrerMode(ReferrerMode referrerMode) {
		this.referrerMode = referrerMode;
	}

	/**
	 * Returns the referrerModeDownload
	 * 
	 * @return referrerModeDownload
	 */
	public ReferrerMode getReferrerModeDownload() {
		return referrerModeDownload;
	}

	/**
	 * Sets the referrerModeDownload
	 * 
	 * @param referrerModeDownload referrerModeDownload
	 */
	public void setReferrerModeDownload(ReferrerMode referrerModeDownload) {
		this.referrerModeDownload = referrerModeDownload;
	}

	/**
	 * Returns the customReferrer
	 * 
	 * @return customReferrer
	 */
	public String getCustomReferrer() {
		return customReferrer;
	}

	/**
	 * Sets the customReferrer
	 * 
	 * @param customReferrer customReferrer
	 */
	public void setCustomReferrer(String customReferrer) {
		this.customReferrer = customReferrer;
	}

	/**
	 * Returns the customReferrerDownload
	 * 
	 * @return customReferrerDownload
	 */
	public String getCustomReferrerDownload() {
		return customReferrerDownload;
	}

	/**
	 * Sets the customReferrerDownload
	 * 
	 * @param customReferrerDownload customReferrerDownload
	 */
	public void setCustomReferrerDownload(String customReferrerDownload) {
		this.customReferrerDownload = customReferrerDownload;
	}

	/**
	 * Returns the maxConnections
	 * 
	 * @return maxConnections
	 */
	public int getMaxConnections() {
		return maxConnections;
	}

	/**
	 * Sets the maxConnections
	 * 
	 * @param maxConnections maxConnections
	 */
	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
		applyRestriction();
	}

	/**
	 * Returns the maxConnectionDomains
	 * 
	 * @return maxConnectionDomains
	 */
	public List<String> getMaxConnectionDomains() {
		return maxConnectionDomains;
	}

	/**
	 * Sets the maxConnectionDomains
	 * 
	 * @param maxConnectionDomains maxConnectionDomains
	 */
	public void setMaxConnectionDomains(List<String> maxConnectionDomains) {
		this.maxConnectionDomains = maxConnectionDomains;
		applyRestriction();
	}

	/**
	 * Returns the duplicateRemoveMode
	 * 
	 * @return duplicateRemoveMode
	 */
	public DuplicateRemoveMode getDuplicateRemoveMode() {
		return duplicateRemoveMode;
	}

	/**
	 * Sets the duplicateRemoveMode
	 * 
	 * @param duplicateRemoveMode duplicateRemoveMode
	 */
	public void setDuplicateRemoveMode(DuplicateRemoveMode duplicateRemoveMode) {
		this.duplicateRemoveMode = duplicateRemoveMode;
	}

	/**
	 * Returns the sendCookies
	 * 
	 * @return sendCookies
	 */
	public boolean isSendCookies() {
		return sendCookies;
	}

	/**
	 * Sets the sendCookies
	 * 
	 * @param sendCookies sendCookies
	 */
	public void setSendCookies(boolean sendCookies) {
		this.sendCookies = sendCookies;
	}

	@Override
	public boolean isEnabled() {
		return !deactivateOption.isDeactivated();
	}

	@Override
	public void setEnabled(boolean enabled) {
		deactivateOption.setDeactivated(!enabled);
		deactivateOption.saveOption();
		SettingsManager.instance().writeSettings(true);
	}

	@Override
	public boolean canBeDisabled() {
		return true;
	}

	/**
	 * Returns the reducePathLength
	 * 
	 * @return reducePathLength
	 */
	public boolean isReducePathLength() {
		return reducePathLength;
	}

	/**
	 * Sets the reducePathLength
	 * 
	 * @param reducePathLength reducePathLength
	 */
	public void setReducePathLength(boolean reducePathLength) {
		this.reducePathLength = reducePathLength;
	}

	/**
	 * Returns the reduceFilenameLength
	 * 
	 * @return reduceFilenameLength
	 */
	public boolean isReduceFilenameLength() {
		return reduceFilenameLength;
	}

	/**
	 * Sets the reduceFilenameLength
	 * 
	 * @param reduceFilenameLength reduceFilenameLength
	 */
	public void setReduceFilenameLength(boolean reduceFilenameLength) {
		this.reduceFilenameLength = reduceFilenameLength;
	}

	@Override
	public boolean removeDuplicateEqualsMethod(URL url1, URL url2) {
		boolean sameURL = super.removeDuplicateEqualsMethod(url1, url2);
		switch (duplicateRemoveMode) {
			case DUPLICATES_CONTAINER_URL_AND_THUMBNAIL_URL:
				return sameURL && url1.getThumb().equals(url2.getThumb());
			case DUPLICATES_CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_FIRST:
				return sameURL && url2.getThumb().isEmpty();
			case DUPLICATES_CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_LAST:
				return sameURL && url2.getThumb().isEmpty();
			case DUPLICATES_CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_FIRST:
				return sameURL && url1.getThumb().isEmpty();
			case DUPLICATES_CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_LAST:
				return sameURL && url1.getThumb().isEmpty();
			case DUPLICATES_BH_DEFAULT:
			case DUPLICATES_CONTAINER_URL_ONLY:
			default:
				return sameURL;
		}
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Returns if this rule can parse the URL
	 * 
	 * @param url Container-URL
	 * @param sourceRule Last rule which parsed the URL
	 * @return True if this rule can parse the URL
	 */
	public boolean isFromThisRedirect(String url, Rule sourceRule) {
		if (redirect == false || sourceRule == this) {
			return false;
		}
		if (deactivateOption.isDeactivated()) {
			return false;
		}
		Matcher urlMatcher = urlPattern.matcher(url);
		if (urlMatcher.matches()) {
			return true;
		}
		return false;
	}
}

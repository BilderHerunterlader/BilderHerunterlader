package ch.supertomcat.bh.rules;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.Hoster;
import ch.supertomcat.bh.hoster.containerpage.DownloadContainerPageOptions;
import ch.supertomcat.bh.hoster.hosteroptions.DeactivateOption;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.queue.DownloadRestriction;
import ch.supertomcat.bh.rules.trace.RuleTraceInfo;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoURL;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoURLDownloadContainerPage;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoURLReplace;
import ch.supertomcat.bh.rules.xml.FailuresPipeline;
import ch.supertomcat.bh.rules.xml.FilenameDownloadSelectionMode;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import ch.supertomcat.bh.rules.xml.URLJavascriptPipeline;
import ch.supertomcat.bh.rules.xml.URLPipeline;
import ch.supertomcat.bh.rules.xml.URLRegexPipeline;
import ch.supertomcat.bh.rules.xml.URLRegexPipelineMode;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;

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
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Rule Definition
	 */
	private final RuleDefinition definition;

	/**
	 * Compiled Pattern
	 */
	private Pattern compiledUrlPattern;

	/**
	 * Pipelines
	 */
	private final List<RuleURLPipeline<?>> pipelines = new ArrayList<>();

	/**
	 * Pipeline
	 */
	private RulePipelineFilename pipelineFilename = null;

	/**
	 * Pipeline
	 */
	private RulePipelineFilenameDownloadSelection pipelineFilenameDownloadSelection = null;

	/**
	 * pipelinesFailures
	 */
	private final List<RulePipelineFailures> pipelinesFailures = new ArrayList<>();

	/**
	 * restriction
	 */
	private DownloadRestriction restriction = null;

	private DeactivateOption deactivateOption = null;

	/**
	 * Path to the XML-File
	 */
	private String strFile;

	/**
	 * Constructor
	 * 
	 * @param file Path to the XML-File
	 * @param definition Rule Definition
	 * @param developer True if is developer rule, false otherwise
	 * @throws PatternSyntaxException
	 */
	public Rule(String file, RuleDefinition definition, boolean developer) throws PatternSyntaxException {
		this.strFile = file;
		this.definition = definition;
		setDeveloper(developer);
		updateFromDefinition();
	}

	/**
	 * Updates internal members from definition. This method should always be called, when the definition was changed
	 */
	public void updateFromDefinition() {
		deactivateOption = new DeactivateOption(FileUtil.getFilename(this.strFile), getSettingsManager());

		this.compiledUrlPattern = Pattern.compile(definition.getUrlPattern());

		pipelines.clear();
		for (URLPipeline pipeDefinition : definition.getPipes()) {
			if (pipeDefinition instanceof URLRegexPipeline) {
				pipelines.add(new RulePipelineURLRegex((URLRegexPipeline)pipeDefinition));
			} else if (pipeDefinition instanceof URLJavascriptPipeline) {
				pipelines.add(new RulePipelineURLJavascript((URLJavascriptPipeline)pipeDefinition));
			}
		}

		pipelineFilename = new RulePipelineFilename(definition.getFilenamePipeline());

		pipelineFilenameDownloadSelection = new RulePipelineFilenameDownloadSelection(definition.getFilenameDownloadSelectionPipeline());

		pipelinesFailures.clear();
		for (FailuresPipeline failurePipeDefinition : definition.getFailuresPipes()) {
			pipelinesFailures.add(new RulePipelineFailures(failurePipeDefinition));
		}

		applyRestriction();
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
		Matcher urlMatcher = compiledUrlPattern.matcher(url);
		return urlMatcher.matches();
	}

	/**
	 * Returns parsed URL and filename
	 * 
	 * @param upo URLParseObject
	 * @return URL and filename
	 * @throws HostException
	 */
	public String[] getURLAndFilename(URLParseObject upo) throws HostException {
		return getURLAndFilename(upo, false);
	}

	/**
	 * Returns parsed URL and filename
	 * 
	 * @param upo URLParseObject
	 * @param trace Flag if trace is active and information like html codes and so on are added to the URLParseObject
	 * @return URL and filename
	 * @throws HostException
	 */
	public String[] getURLAndFilename(URLParseObject upo, boolean trace) throws HostException {
		Pic pic = upo.getPic();
		String url = upo.getContainerURL();
		String thumbURL = upo.getThumbURL();
		String retval[] = new String[2];

		RuleTraceInfo ruleTraceInfo = null;
		if (trace) {
			ruleTraceInfo = new RuleTraceInfo();
			upo.addInfo("RuleTraceInfo", ruleTraceInfo);
		}

		if (pipelines.isEmpty()) {
			for (RulePipelineFailures failurePipe : pipelinesFailures) {
				failurePipe.checkForFailure(url, thumbURL, "", ruleTraceInfo);
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
		RuleHtmlCode htmlCodeFromFirstURL = new RuleHtmlCode();
		RuleHtmlCode htmlCodeFirst = new RuleHtmlCode();
		RuleHtmlCode htmlCodeLast = new RuleHtmlCode();

		String pipelineURL = url;
		String pipelineThumbURL = thumbURL;
		String pipelineReferrer = getReferrer(upo);
		String pipelineResult = "";
		for (int i = 0; i < pipelines.size(); i++) {
			RuleURLPipeline<?> rulePipeline = pipelines.get(i);
			URLPipeline pipelineDefinition = rulePipeline.getDefinition();

			RuleTraceInfoURL ruleTraceInfoURL = null;
			if (ruleTraceInfo != null) {
				ruleTraceInfoURL = new RuleTraceInfoURL(pipelineURL);
				ruleTraceInfo.addURLTraceInfo(ruleTraceInfoURL);
			}

			sleepIfRequired(pipelineDefinition);

			boolean sendCookies = pipelineDefinition.isSendCookies();
			String htmlcode = "";
			if (rulePipeline instanceof RulePipelineURLRegex && ((RulePipelineURLRegex)rulePipeline).getDefinition().getMode() == URLRegexPipelineMode.CONTAINER_PAGE_SOURCECODE) {
				htmlcode = downloadContainerPage(definition.getName(), pipelineURL, pipelineReferrer, new DownloadContainerPageOptions(sendCookies, true));
				logger.debug("{} -> {} -> Download Container-Page done -> Result: {}", definition.getName(), url, htmlcode);
				if (ruleTraceInfoURL != null) {
					ruleTraceInfoURL.addStep(new RuleTraceInfoURLDownloadContainerPage(i, pipelineURL, htmlcode));
				}
				if (i == 0) {
					htmlCodeFromFirstURL.setData(htmlcode, pipelineURL, pipelineReferrer);
					if (trace) {
						upo.addInfo("PageSourceCodeFromFirstURL", htmlCodeFromFirstURL);
					}
				}
				if (!htmlCodeFirst.isAvailable()) {
					htmlCodeFirst.setData(htmlcode, pipelineURL, pipelineReferrer);
					if (trace) {
						upo.addInfo("PageSourceCodeFirst", htmlCodeFirst);
					}
				}
				htmlCodeLast.setData(htmlcode, pipelineURL, pipelineReferrer);
				if (trace) {
					upo.addInfo("PageSourceCodeLast", htmlCodeLast);
				}
			}

			/*
			 * Check for failures before replace
			 */
			for (RulePipelineFailures failurePipe : pipelinesFailures) {
				failurePipe.checkForFailure(pipelineURL, pipelineThumbURL, htmlcode, ruleTraceInfo);
			}

			if (rulePipeline instanceof RulePipelineURLRegex) {
				pipelineResult = ((RulePipelineURLRegex)rulePipeline).getURL(pipelineURL, pipelineThumbURL, htmlcode, pic, ruleTraceInfoURL);
			} else if (rulePipeline instanceof RulePipelineURLJavascript) {
				pipelineResult = ((RulePipelineURLJavascript)rulePipeline).getURLByJavascript(pipelineURL, pipelineThumbURL, htmlcode, upo);
			}

			if (pipelineDefinition.isUrlDecodeResult()) {
				URLCodec urlCodec = new URLCodec("UTF-8");
				try {
					pipelineResult = urlCodec.decode(pipelineResult);
				} catch (DecoderException e) {
					logger.error("Could not URL decode (application/x-www-form-urlencoded) pipeline result: {}", pipelineResult);
				}
			}

			pipelineResult = HTTPUtil.decodeURL(pipelineResult);
			logger.debug("{} -> {} -> pipe[{}] -> Result: {}", definition.getName(), url, i, pipelineResult);
			if (ruleTraceInfoURL != null) {
				ruleTraceInfoURL.addStep(new RuleTraceInfoURLReplace(i, pipelineResult));
			}

			/*
			 * Check for failures after replace
			 */
			for (RulePipelineFailures failurePipe : pipelinesFailures) {
				failurePipe.checkForFailure(pipelineResult, ruleTraceInfo);
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

		/*
		 * If there are no pipelines, then just use the container url as result
		 */
		if (pipelines.isEmpty()) {
			pipelineResult = url;
		}

		logger.debug("{} -> {} -> Final URL Result: {}", definition.getName(), url, pipelineResult);
		retval[0] = pipelineResult;
		retval[1] = upo.getCorrectedFilename(); // Corrected Filename
		if (pic != null && pic.getTargetFilename().isEmpty()) {
			pic.setTargetFilename(RuleUtil.getFilenamePart(retval[0]));
		}

		if (pipelineFilename != null && !retval[0].isEmpty() && !pipelineFilename.getRegexps().isEmpty()) {
			RuleDownloadContainerPageSupplier<RuleHtmlCode> htmlCodeDownloadSupplier = () -> {
				String referrer = getReferrer(upo);
				String downloadedHtmlCode = downloadContainerPage(definition.getName(), url, referrer);
				return new RuleHtmlCode(downloadedHtmlCode, url, referrer);
			};
			String filenamePipelineResult = pipelineFilename
					.getCorrectedFilename(url, pipelineURL, thumbURL, retval[0], htmlCodeFromFirstURL, htmlCodeFirst, htmlCodeLast, htmlCodeDownloadSupplier, pic, upo, ruleTraceInfo);
			if (filenamePipelineResult != null) {
				retval[1] = filenamePipelineResult;
			}
		}

		/*
		 * If filename is still empty, then use filename part of download URL
		 */
		if (retval[1].isEmpty()) {
			retval[1] = RuleUtil.getFilenamePart(retval[0]);
		}

		if (pic != null) {
			pic.setRenameWithContentDisposition(definition.isUseContentDisposition());
		}

		upo.addInfo("sendCookies", definition.isSendCookies());
		upo.addInfo("ReducePathLength", definition.isReducePathLength());
		upo.addInfo("ReduceFilenameLength", definition.isReduceFilenameLength());

		return retval;
	}

	private void sleepIfRequired(URLPipeline pipelineDefinition) {
		int waitBeforeExecute = pipelineDefinition.getWaitBeforeExecute();
		if (waitBeforeExecute > 0) {
			try {
				Thread.sleep(waitBeforeExecute);
			} catch (InterruptedException e) {
				logger.error("Sleep was interrupted", e);
			}
		}
	}

	/**
	 * Returns the referrer to use in downloadContainerPage-Method
	 * 
	 * @param upo URLParseObject
	 * @return Referrer
	 */
	private String getReferrer(final URLParseObject upo) {
		String referrer = "";
		switch (definition.getReferrerMode()) {
			case NO_REFERRER:
				referrer = "";
				break;
			case LAST_CONTAINER_URL:
				referrer = upo.getContainerURL();
				break;
			case FIRST_CONTAINER_URL:
				referrer = upo.getFirstContainerURL();
				break;
			case ORIGIN_PAGE:
				referrer = upo.getPic().getThreadURL();
				break;
			case CUSTOM:
				referrer = definition.getCustomReferrer();
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
		return getFilename(url, null);
	}

	/**
	 * Returns the filename after replacement
	 * 
	 * @param url URL
	 * @param ruleTraceInfo Rule Trace Info or null
	 * @return Filename
	 */
	public String getFilename(String url, RuleTraceInfo ruleTraceInfo) {
		String retval = url;
		if (pipelineFilenameDownloadSelection.getDefinition().getMode() == FilenameDownloadSelectionMode.CONTAINER_URL_FILENAME_PART) {
			retval = RuleUtil.getFilenamePart(url);
		}
		return pipelineFilenameDownloadSelection.getCorrectedFilenameOnDownloadSelection(retval, ruleTraceInfo);
	}

	/**
	 * Sets the restriction in the queue and removes the old restriction
	 */
	private void applyRestriction() {
		if (restriction != null) {
			removeRestriction(restriction);
		}
		if (definition.getRestriction().getMaxConnections() > 0 && !definition.getRestriction().getDomain().isEmpty()) {
			restriction = new DownloadRestriction(definition.getRestriction().getDomain(), definition.getRestriction().getMaxConnections());
			addRestriction(restriction);
		} else {
			restriction = null;
		}
	}

	/**
	 * Returns the Element for creating the XML-File
	 * 
	 * @return Element
	 */
	public Element getXmlElement() {
		Element e = new Element("rule");
		e.setAttribute("name", definition.getName());
		e.setAttribute("version", definition.getVersion());
		e.setAttribute("redirect", String.valueOf(definition.isRedirect()));
		e.setAttribute("resend", String.valueOf(definition.isResend()));
		e.setAttribute("usecontentdisposition", String.valueOf(definition.isUseContentDisposition()));
		e.setAttribute("reducePathLength", String.valueOf(definition.isReducePathLength()));
		e.setAttribute("reduceFilenameLength", String.valueOf(definition.isReduceFilenameLength()));
		e.setAttribute("referrermode", String.valueOf(definition.getReferrerMode().ordinal()));
		e.setAttribute("referrermodedownload", String.valueOf(definition.getDownloadReferrerMode().ordinal()));
		e.setAttribute("customreferrer", definition.getCustomReferrer());
		e.setAttribute("customreferrerdownload", definition.getDownloadCustomReferrer());
		e.setAttribute("duplicateRemoveMode", String.valueOf(definition.getDuplicateRemoveMode().ordinal()));
		e.setAttribute("sendCookies", String.valueOf(definition.isSendCookies()));
		Element eup = new Element("urlpattern");
		eup.setText(definition.getUrlPattern());
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
		eMaxConnections.setAttribute("value", String.valueOf(definition.getRestriction().getMaxConnections()));
		for (int i = 0; i < definition.getRestriction().getDomain().size(); i++) {
			Element eMaxConnectionDomain = new Element("domain");
			eMaxConnectionDomain.setAttribute("name", definition.getRestriction().getDomain().get(i));
			eMaxConnections.addContent(eMaxConnectionDomain);
		}
		e.addContent(eMaxConnections);
		return e;
	}

	/**
	 * Returns the definition
	 * 
	 * @return definition
	 */
	public RuleDefinition getDefinition() {
		return definition;
	}

	/**
	 * Returns the name
	 * 
	 * @return Name
	 */
	public String getName() {
		return definition.getName();
	}

	/**
	 * Returns the version
	 * 
	 * @return Version
	 */
	public String getVersion() {
		return definition.getVersion();
	}

	/**
	 * Returns the pipelines
	 * Filename- and Filename-on-Download-Selection pipelines are not included
	 * 
	 * @return Pipelines
	 */
	public List<RuleURLPipeline<? extends URLPipeline>> getPipelines() {
		return pipelines;
	}

	/**
	 * Returns the pipelinesFailures
	 * 
	 * @return pipelinesFailures
	 */
	public List<RulePipelineFailures> getPipelinesFailures() {
		return pipelinesFailures;
	}

	/**
	 * Returns the pipelineFilename
	 * 
	 * @return pipelineFilename
	 */
	public RulePipelineFilename getPipelineFilename() {
		return pipelineFilename;
	}

	/**
	 * Returns the pipelineFilenameDownloadSelection
	 * 
	 * @return pipelineFilenameDownloadSelection
	 */
	public RulePipelineFilenameDownloadSelection getPipelineFilenameDownloadSelection() {
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
	 * Checks if the file for this rule exists
	 * 
	 * @return True if file exists, false otherwise
	 */
	public boolean isFileExists() {
		if (strFile.isEmpty()) {
			return false;
		}
		File file = getFile();
		return file.exists() && file.isFile() && file.length() > 0;
	}

	@Override
	public boolean isEnabled() {
		return !deactivateOption.isDeactivated();
	}

	@Override
	public void setEnabled(boolean enabled) {
		deactivateOption.setDeactivated(!enabled);
		deactivateOption.saveOption();
		getSettingsManager().writeSettings(true);
	}

	@Override
	public boolean canBeDisabled() {
		return true;
	}

	@Override
	public boolean removeDuplicateEqualsMethod(URL url1, URL url2) {
		boolean sameURL = super.removeDuplicateEqualsMethod(url1, url2);
		switch (definition.getDuplicateRemoveMode()) {
			case CONTAINER_URL_AND_THUMBNAIL_URL:
				return sameURL && url1.getThumb().equals(url2.getThumb());
			case CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_FIRST:
				return sameURL && url2.getThumb().isEmpty();
			case CONTAINER_URL_ONLY_REMOVE_WITH_THUMB_THUMBS_ALWAYS_LAST:
				return sameURL && url2.getThumb().isEmpty();
			case CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_FIRST:
				return sameURL && url1.getThumb().isEmpty();
			case CONTAINER_URL_ONLY_REMOVE_WITHOUT_THUMB_THUMBS_ALWAYS_LAST:
				return sameURL && url1.getThumb().isEmpty();
			case DEFAULT:
			case CONTAINER_URL_ONLY:
			default:
				return sameURL;
		}
	}

	@Override
	public String toString() {
		return definition.getName();
	}

	/**
	 * Returns if this rule can parse the URL
	 * 
	 * @param url Container-URL
	 * @param sourceRule Last rule which parsed the URL
	 * @return True if this rule can parse the URL
	 */
	public boolean isFromThisRedirect(String url, Rule sourceRule) {
		if (!definition.isRedirect() || sourceRule == this) {
			return false;
		}
		if (deactivateOption.isDeactivated()) {
			return false;
		}
		Matcher urlMatcher = compiledUrlPattern.matcher(url);
		return urlMatcher.matches();
	}
}

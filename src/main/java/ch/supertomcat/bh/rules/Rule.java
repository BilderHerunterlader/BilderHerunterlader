package ch.supertomcat.bh.rules;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.hc.core5.http.protocol.HttpContext;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.Hoster;
import ch.supertomcat.bh.hoster.hosteroptions.DeactivateOption;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.queue.DownloadRestriction;
import ch.supertomcat.bh.rules.trace.RuleTraceInfo;
import ch.supertomcat.bh.rules.xml.FailuresPipeline;
import ch.supertomcat.bh.rules.xml.FilenameDownloadSelectionMode;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import ch.supertomcat.bh.rules.xml.URLJavascriptPipeline;
import ch.supertomcat.bh.rules.xml.URLPipeline;
import ch.supertomcat.bh.rules.xml.URLRegexPipeline;
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
	private final List<RuleURLPipeline<? extends URLPipeline>> pipelines = new ArrayList<>();

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
			if (pipeDefinition instanceof URLRegexPipeline urlRegexPipeline) {
				pipelines.add(new RulePipelineURLRegex(urlRegexPipeline));
			} else if (pipeDefinition instanceof URLJavascriptPipeline urlJavascriptPipeline) {
				pipelines.add(new RulePipelineURLJavascript(urlJavascriptPipeline));
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

		HttpContext httpContext = upo.getInfo(URLParseObject.DOWNLOADER_HTTP_CONTEXT, HttpContext.class);

		RuleDownloadContainerPageFunction<String> downloadContainerPageFunction = (containerURL, referrer,
				options) -> downloadContainerPage(definition.getName(), containerURL, referrer, options, httpContext);

		RuleContext ruleContext = new RuleContext(definition, upo, trace, downloadContainerPageFunction);

		if (pipelines.isEmpty()) {
			for (RulePipelineFailures failurePipe : pipelinesFailures) {
				failurePipe.checkForFailure(ruleContext);
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
		for (int i = 0; i < pipelines.size(); i++) {
			RuleURLPipeline<?> rulePipeline = pipelines.get(i);
			URLPipeline pipelineDefinition = rulePipeline.getDefinition();

			ruleContext.addRuleTraceInfoURL();

			rulePipeline.sleepIfRequired();

			/*
			 * Download container page if necessary
			 */
			rulePipeline.downloadContainerPage(ruleContext, i);

			/*
			 * Check for failures before replace
			 */
			for (RulePipelineFailures failurePipe : pipelinesFailures) {
				failurePipe.checkForFailure(ruleContext);
			}

			/*
			 * Get parsed URL from Pipeline
			 */
			rulePipeline.getURL(ruleContext);

			if (pipelineDefinition.isUrlDecodeResult()) {
				ruleContext.urlDecodePipelineResult();
			}

			if (pipelineDefinition.isJavascriptDecodeResult() != null && pipelineDefinition.isJavascriptDecodeResult()) {
				ruleContext.javascriptDecodePipelineResult();
			}

			ruleContext.decodePipelineResult();
			logger.debug("{} -> {} -> pipe[{}] -> Result: {}", definition.getName(), url, i, ruleContext.getPipelineResult());
			ruleContext.addRuleTraceInfoURLReplaceStep(i);

			/*
			 * Check for failures after replace
			 */
			for (RulePipelineFailures failurePipe : pipelinesFailures) {
				failurePipe.checkForFailureURLOnly(ruleContext);
			}

			if (i > 0 && i < (pipelines.size() - 1)) {
				ruleContext.applyPipelineURLToUPO();
			}
			if (i < (pipelines.size() - 1)) {
				ruleContext.applyURLsForNextPipeline();
			}
		}

		/*
		 * If there are no pipelines, then just use the container url as result
		 */
		if (pipelines.isEmpty()) {
			ruleContext.setPipelineResult(url);
		}

		String pipelineResult = ruleContext.getPipelineResult();
		logger.debug("{} -> {} -> Final URL Result: {}", definition.getName(), url, pipelineResult);
		String correctedFilename = upo.getCorrectedFilename();

		if (pic != null && pic.getTargetFilename().isEmpty()) {
			pic.setTargetFilename(HTTPUtil.getFilenameFromURL(pipelineResult, pipelineResult));
		}

		if (pipelineFilename != null && !pipelineResult.isEmpty() && !pipelineFilename.getRegexps().isEmpty()) {
			String filenamePipelineResult = pipelineFilename.getCorrectedFilename(ruleContext);
			if (filenamePipelineResult != null) {
				correctedFilename = filenamePipelineResult;
			}
		}

		/*
		 * If filename is still empty, then use filename part of download URL
		 */
		if (correctedFilename.isEmpty()) {
			correctedFilename = HTTPUtil.getFilenameFromURL(pipelineResult, pipelineResult);
		}

		if (pic != null) {
			pic.setRenameWithContentDisposition(definition.isUseContentDisposition());
		}

		upo.addInfo(URLParseObject.SEND_COOKIES, definition.isSendCookies());
		String userAgent = definition.getUserAgent();
		if (userAgent != null && !userAgent.isEmpty()) {
			upo.addInfo(URLParseObject.USE_USER_AGENT, userAgent);
		}
		upo.addInfo(URLParseObject.REDUCE_PATH_LENGTH, definition.isReducePathLength());
		upo.addInfo(URLParseObject.REDUCE_FILENAME_LENGTH, definition.isReduceFilenameLength());

		return new String[] { pipelineResult, correctedFilename };
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
			retval = HTTPUtil.getFilenameFromURL(url, url);
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
			case DEFAULT, CONTAINER_URL_ONLY:
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

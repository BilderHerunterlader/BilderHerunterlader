package ch.supertomcat.bh.rules;

import java.util.ArrayList;
import java.util.List;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostImageUrlNotFoundException;
import ch.supertomcat.bh.hoster.containerpage.DownloadContainerPageOptions;
import ch.supertomcat.bh.hoster.hostimpl.HostRules;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoURL;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoURLRegexReplace;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoURLRegexSearch;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoURLRegexVariable;
import ch.supertomcat.bh.rules.xml.URLMode;
import ch.supertomcat.bh.rules.xml.URLRegexPipeline;
import ch.supertomcat.bh.rules.xml.URLRegexPipelineMode;
import ch.supertomcat.bh.rules.xml.VarRuleRegex;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * RulePipeline
 */
public class RulePipelineURLRegex extends RuleURLPipeline<URLRegexPipeline> {
	/**
	 * Regex Search / Replaces for storing into variable
	 */
	protected List<RuleVarRegExp> varRegexps = new ArrayList<>();

	/**
	 * Constructor
	 */
	public RulePipelineURLRegex() {
		this(URLRegexPipelineMode.CONTAINER_OR_THUMBNAIL_URL);
	}

	/**
	 * Constructor
	 * 
	 * @param mode Mode
	 */
	public RulePipelineURLRegex(URLRegexPipelineMode mode) {
		super(new URLRegexPipeline());
		definition.setWaitBeforeExecute(0);
		definition.setUrlDecodeResult(false);
		definition.setSendCookies(true);
		definition.setMode(mode);
		definition.setUrlMode(URLMode.CONTAINER_URL);
	}

	/**
	 * Constructor
	 * 
	 * @param definition Definition
	 */
	public RulePipelineURLRegex(URLRegexPipeline definition) {
		super(definition);
		updateFromDefinition();
	}

	/**
	 * Update internal variables from definition
	 */
	private void updateFromDefinition() {
		varRegexps.clear();
		for (VarRuleRegex varRegexDefinition : definition.getVarRegexp()) {
			RuleVarRegExp ruleVarRegExp = new RuleVarRegExp(varRegexDefinition);
			varRegexps.add(ruleVarRegExp);
		}
	}

	@Override
	public String downloadContainerPage(RuleContext ruleContext, int step) throws HostException {
		if (definition.getMode() == URLRegexPipelineMode.CONTAINER_PAGE_SOURCECODE) {
			boolean sendCookies = definition.isSendCookies();
			String pipelineURL = ruleContext.getPipelineURL();
			String htmlCode = ruleContext.downloadContainerPage(pipelineURL, ruleContext.getPipelineReferrer(), new DownloadContainerPageOptions(sendCookies, true));
			logger.info("{} -> {} -> Download Container-Page done -> Result: {}", ruleContext.getRuleName(), pipelineURL, htmlCode);

			if (step == 0) {
				ruleContext.setHtmlCodeFromFirstURLData(htmlCode);
			}
			ruleContext.setHtmlCodeFirstData(htmlCode);
			ruleContext.setHtmlCodeLastData(htmlCode);

			ruleContext.addRuleTraceInfoURLContainerPageStep(step);
			return htmlCode;
		}
		return null;
	}

	@Override
	public String getURL(RuleContext ruleContext) throws HostException {
		String url = ruleContext.getPipelineURL();
		String thumbURL = ruleContext.getPipelineThumbURL();
		Pic pic = ruleContext.getPic();
		String htmlCode = ruleContext.getHtmlCodeLast().getHtmlCode();
		RuleTraceInfoURL ruleTraceInfoURL = ruleContext.getCurrentRuleTraceInfoURL();

		for (int i = 0; i < varRegexps.size(); i++) {
			RuleVarRegExp ruleVarRegex = varRegexps.get(i);
			List<RuleRegExp> regexList = ruleVarRegex.getRegexps();
			String value;
			if (definition.getMode() == URLRegexPipelineMode.CONTAINER_OR_THUMBNAIL_URL) {
				value = getURLFromContainerOrThumbURL(regexList, url, thumbURL, pic, ruleContext, ruleTraceInfoURL);
			} else {
				value = getURLFromContainerPage(regexList, url, htmlCode, pic, ruleContext, ruleTraceInfoURL);
			}
			String variableName = ruleVarRegex.getVariableName();
			ruleContext.putVar(variableName, value);
			logger.info("{} -> Step {} -> Variable assigned: {}={}", url, i, variableName, value);
			if (ruleTraceInfoURL != null) {
				ruleTraceInfoURL.addStep(new RuleTraceInfoURLRegexVariable(i, variableName, value));
			}
		}

		String result = getURL(url, thumbURL, htmlCode, pic, ruleContext, ruleTraceInfoURL);
		ruleContext.setPipelineResult(result);
		return result;
	}

	/**
	 * Returns parsed URL
	 * 
	 * @param url Container-URL
	 * @param thumbURL Thumbnail-URL
	 * @param htmlCode Sourcecode
	 * @param pic Pic
	 * @return URL
	 * @throws HostException
	 */
	public String getURL(String url, String thumbURL, String htmlCode, Pic pic) throws HostException {
		return getURL(url, thumbURL, htmlCode, pic, null, null);
	}

	/**
	 * Returns parsed URL
	 * 
	 * @param url Container-URL
	 * @param thumbURL Thumbnail-URL
	 * @param htmlCode Sourcecode
	 * @param pic Pic
	 * @param ruleContext Rule Context or null
	 * @param ruleTraceInfoURL Rule Trace Info URL or null
	 * @return URL
	 * @throws HostException
	 */
	public String getURL(String url, String thumbURL, String htmlCode, Pic pic, RuleContext ruleContext, RuleTraceInfoURL ruleTraceInfoURL) throws HostException {
		String retval = "";
		if (definition.getMode() == URLRegexPipelineMode.CONTAINER_OR_THUMBNAIL_URL) {
			retval = getURLFromContainerOrThumbURL(regexps, url, thumbURL, pic, ruleContext, ruleTraceInfoURL);
		} else {
			retval = getURLFromContainerPage(regexps, url, htmlCode, pic, ruleContext, ruleTraceInfoURL);
		}
		if (retval.isEmpty()) {
			if (definition.getUrlMode() == URLMode.THUMBNAIL_URL && thumbURL.isEmpty()) {
				throw new HostImageUrlNotFoundException(HostRules.NAME + ": " + Localization.getString("ErrorImageURLThumbMissing"));
			} else {
				throw new HostImageUrlNotFoundException(HostRules.NAME + ": " + Localization.getString("ErrorImageURL"));
			}
		}
		return retval;
	}

	/**
	 * Returns parsed URL
	 * 
	 * @param regexList Regex List
	 * @param url Container-URL
	 * @param thumbURL Thumbnail-URL
	 * @param pic Pic
	 * @param ruleContext Rule Context or null
	 * @param ruleTraceInfoURL Rule Trace Info URL or null
	 * @return URL
	 */
	private String getURLFromContainerOrThumbURL(List<RuleRegExp> regexList, String url, String thumbURL, Pic pic, RuleContext ruleContext, RuleTraceInfoURL ruleTraceInfoURL) {
		String result;
		if (definition.getUrlMode() == URLMode.THUMBNAIL_URL) {
			result = thumbURL;
		} else {
			result = url;
		}

		for (int i = 0; i < regexList.size(); i++) {
			result = regexList.get(i).doURLReplace(result, pic, ruleContext);
			logger.info("{} -> Replace done -> Step {} -> Result: {}", url, i, result);
			if (ruleTraceInfoURL != null) {
				ruleTraceInfoURL.addStep(new RuleTraceInfoURLRegexReplace(i, 0, result));
			}
		}
		return result;
	}

	/**
	 * Returns parsed URL
	 * 
	 * @param regexList Regex List
	 * @param url Container-URL
	 * @param htmlCode Sourcecode
	 * @param pic Pic
	 * @param ruleContext Rule Context or null
	 * @param ruleTraceInfoURL Rule Trace Info URL or null
	 * @return URL
	 */
	private String getURLFromContainerPage(List<RuleRegExp> regexList, String url, String htmlCode, Pic pic, RuleContext ruleContext, RuleTraceInfoURL ruleTraceInfoURL) {
		String result = "";
		int start = 0;
		for (int i = 0; i < regexList.size(); i++) {
			if (i < (regexList.size() - 1)) {
				int pos = regexList.get(i).doPageSourcecodeSearch(htmlCode, start);
				if (pos >= 0) {
					logger.info("{} -> Search done -> Step {} -> Pattern found at: {}", url, i, pos);
				} else {
					logger.info("{} -> Search done -> Step {} -> Pattern not found!", url, i);
				}
				if (ruleTraceInfoURL != null) {
					ruleTraceInfoURL.addStep(new RuleTraceInfoURLRegexSearch(i, start, pos));
				}
				start = pos;
			} else {
				result = regexList.get(i).doPageSourcecodeReplace(htmlCode, start, url, pic, ruleContext);
				logger.info("{} -> Replace done -> Step {} -> Result: {}", url, i, result);
				if (ruleTraceInfoURL != null) {
					ruleTraceInfoURL.addStep(new RuleTraceInfoURLRegexReplace(i, start, result));
				}
			}
		}
		return result;
	}
}

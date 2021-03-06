package ch.supertomcat.bh.rules;

import org.jdom2.Element;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostImageUrlNotFoundException;
import ch.supertomcat.bh.hoster.hostimpl.HostRules;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoURL;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoURLRegexReplace;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoURLRegexSearch;
import ch.supertomcat.bh.rules.xml.URLMode;
import ch.supertomcat.bh.rules.xml.URLRegexPipeline;
import ch.supertomcat.bh.rules.xml.URLRegexPipelineMode;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * RulePipeline
 */
public class RulePipelineURLRegex extends RuleURLPipeline<URLRegexPipeline> {
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
	}

	@Override
	public void fillXmlElement(Element e) {
		if (definition.getMode() == URLRegexPipelineMode.CONTAINER_OR_THUMBNAIL_URL) {
			e.setAttribute("mode", "0");
			e.setAttribute("urlmode", String.valueOf(definition.getUrlMode().ordinal()));
		} else if (definition.getMode() == URLRegexPipelineMode.CONTAINER_PAGE_SOURCECODE) {
			e.setAttribute("mode", "1");
		}
		super.fillXmlElement(e);
		/*
		 * sendCookies was not saved previously for URLPipelines, which was probably a bug. So we remove the attribute here.
		 */
		e.removeAttribute("sendCookies");
	}

	/**
	 * Returns parsed URL
	 * 
	 * @param url Container-URL
	 * @param thumbURL Thumbnail-URL
	 * @param htmlcode Sourcecode
	 * @param pic Pic
	 * @return URL
	 * @throws HostException
	 */
	public String getURL(String url, String thumbURL, String htmlcode, Pic pic) throws HostException {
		return getURL(url, thumbURL, htmlcode, pic, null);
	}

	/**
	 * Returns parsed URL
	 * 
	 * @param url Container-URL
	 * @param thumbURL Thumbnail-URL
	 * @param htmlcode Sourcecode
	 * @param pic Pic
	 * @param ruleTraceInfoURL Rule Trace Info URL or null
	 * @return URL
	 * @throws HostException
	 */
	public String getURL(String url, String thumbURL, String htmlcode, Pic pic, RuleTraceInfoURL ruleTraceInfoURL) throws HostException {
		String retval = "";
		if (definition.getMode() == URLRegexPipelineMode.CONTAINER_OR_THUMBNAIL_URL) {
			String result = url;
			if (definition.getUrlMode() == URLMode.THUMBNAIL_URL) {
				result = thumbURL;
			}
			for (int i = 0; i < regexps.size(); i++) {
				result = regexps.get(i).doURLReplace(result, pic);
				logger.debug(url + " -> Replace done -> Step " + i + " -> Result: " + result);
				if (ruleTraceInfoURL != null) {
					ruleTraceInfoURL.addStep(new RuleTraceInfoURLRegexReplace(i, 0, result));
				}
			}
			retval = result;
		} else {
			String result = "";
			int start = 0;
			for (int i = 0; i < regexps.size(); i++) {
				if (i < (regexps.size() - 1)) {
					int pos = regexps.get(i).doPageSourcecodeSearch(htmlcode, start);
					if (pos >= 0) {
						logger.debug(url + " -> Search done -> Step " + i + " -> Pattern found at: " + pos);
					} else {
						logger.debug(url + " -> Search done -> Step " + i + " -> Pattern not found!");
					}
					if (ruleTraceInfoURL != null) {
						ruleTraceInfoURL.addStep(new RuleTraceInfoURLRegexSearch(i, start, pos));
					}
					start = pos;
				} else {
					result = regexps.get(i).doPageSourcecodeReplace(htmlcode, start, url, pic);
					logger.debug(url + " -> Replace done -> Step " + i + " -> Result: " + result);
					if (ruleTraceInfoURL != null) {
						ruleTraceInfoURL.addStep(new RuleTraceInfoURLRegexReplace(i, start, result));
					}
				}
			}
			retval = result;
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
}

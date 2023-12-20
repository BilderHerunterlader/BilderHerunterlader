package ch.supertomcat.bh.rules;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.containerpage.DownloadContainerPageOptions;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.rules.trace.RuleTraceInfo;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoURL;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoURLDownloadContainerPage;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoURLReplace;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;

/**
 * Rule Context
 */
public class RuleContext {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Rule Definition
	 */
	private final RuleDefinition definition;

	/**
	 * URL Parse Object
	 */
	private final URLParseObject upo;

	/**
	 * Pic
	 */
	private final Pic pic;

	/**
	 * Container URL
	 */
	private final String containerURL;

	/**
	 * Thumb URL
	 */
	private final String thumbURL;

	/**
	 * HTTP Context or null
	 */
	private final HttpContext httpContext;

	/**
	 * Trace Flag
	 */
	private final boolean trace;

	/**
	 * Rule Trace Info or null
	 */
	private final RuleTraceInfo ruleTraceInfo;

	/**
	 * Download Container Page Function
	 */
	private final RuleDownloadContainerPageFunction<String> downloadContainerPageFunction;

	/**
	 * Variables to store information from pipelines
	 */
	private final Map<String, String> vars = new HashMap<>();

	/**
	 * HTML Code of First URL
	 */
	private final RuleHtmlCode htmlCodeFromFirstURL = new RuleHtmlCode();

	/**
	 * First HTML Code
	 */
	private final RuleHtmlCode htmlCodeFirst = new RuleHtmlCode();

	/**
	 * Last HTML Code
	 */
	private final RuleHtmlCode htmlCodeLast = new RuleHtmlCode();

	/**
	 * Pipeline URL
	 */
	private String pipelineURL;

	/**
	 * Pipeline Thumb URL
	 */
	private String pipelineThumbURL;

	/**
	 * Pipeline Referrer
	 */
	private String pipelineReferrer;

	/**
	 * Pipeline Result
	 */
	private String pipelineResult = "";

	/**
	 * Current Rule Trace Info URL
	 */
	private RuleTraceInfoURL currentRuleTraceInfoURL;

	/**
	 * Constructor
	 * 
	 * @param definition Rule Definition
	 * @param upo URL Parse Object
	 * @param trace True if trace should be enabled, false otherwise
	 * @param downloadContainerPageFunction Download Container Page Function
	 */
	public RuleContext(RuleDefinition definition, URLParseObject upo, boolean trace, RuleDownloadContainerPageFunction<String> downloadContainerPageFunction) {
		this.definition = definition;
		this.upo = upo;
		this.trace = trace;
		this.pic = upo.getPic();
		this.containerURL = upo.getContainerURL();
		this.thumbURL = upo.getThumbURL();
		this.httpContext = upo.getInfo(URLParseObject.DOWNLOADER_HTTP_CONTEXT, HttpContext.class);
		if (trace) {
			this.ruleTraceInfo = new RuleTraceInfo();
			upo.addInfo(URLParseObject.RULE_TRACE_INFO, ruleTraceInfo);
		} else {
			this.ruleTraceInfo = null;
		}
		this.downloadContainerPageFunction = downloadContainerPageFunction;
		this.pipelineURL = containerURL;
		this.pipelineThumbURL = thumbURL;
		this.pipelineReferrer = getReferrer();
	}

	/**
	 * @return Rule Name
	 */
	public String getRuleName() {
		return definition.getName();
	}

	/**
	 * Returns the pipelineURL
	 * 
	 * @return pipelineURL
	 */
	public String getPipelineURL() {
		return pipelineURL;
	}

	/**
	 * Sets the pipelineURL
	 * 
	 * @param pipelineURL pipelineURL
	 */
	public void setPipelineURL(String pipelineURL) {
		this.pipelineURL = pipelineURL;
	}

	/**
	 * Returns the pipelineThumbURL
	 * 
	 * @return pipelineThumbURL
	 */
	public String getPipelineThumbURL() {
		return pipelineThumbURL;
	}

	/**
	 * Sets the pipelineThumbURL
	 * 
	 * @param pipelineThumbURL pipelineThumbURL
	 */
	public void setPipelineThumbURL(String pipelineThumbURL) {
		this.pipelineThumbURL = pipelineThumbURL;
	}

	/**
	 * Returns the pipelineReferrer
	 * 
	 * @return pipelineReferrer
	 */
	public String getPipelineReferrer() {
		return pipelineReferrer;
	}

	/**
	 * Sets the pipelineReferrer
	 * 
	 * @param pipelineReferrer pipelineReferrer
	 */
	public void setPipelineReferrer(String pipelineReferrer) {
		this.pipelineReferrer = pipelineReferrer;
	}

	/**
	 * Returns the pipelineResult
	 * 
	 * @return pipelineResult
	 */
	public String getPipelineResult() {
		return pipelineResult;
	}

	/**
	 * Sets the pipelineResult
	 * 
	 * @param pipelineResult pipelineResult
	 */
	public void setPipelineResult(String pipelineResult) {
		this.pipelineResult = pipelineResult;
	}

	/**
	 * Returns the upo
	 * 
	 * @return upo
	 */
	public URLParseObject getUpo() {
		return upo;
	}

	/**
	 * Returns the pic
	 * 
	 * @return pic
	 */
	public Pic getPic() {
		return pic;
	}

	/**
	 * Returns the containerURL
	 * 
	 * @return containerURL
	 */
	public String getContainerURL() {
		return containerURL;
	}

	/**
	 * Returns the thumbURL
	 * 
	 * @return thumbURL
	 */
	public String getThumbURL() {
		return thumbURL;
	}

	/**
	 * Returns the httpContext
	 * 
	 * @return httpContext
	 */
	public HttpContext getHttpContext() {
		return httpContext;
	}

	/**
	 * Returns the ruleTraceInfo
	 * 
	 * @return ruleTraceInfo
	 */
	public RuleTraceInfo getRuleTraceInfo() {
		return ruleTraceInfo;
	}

	/**
	 * Returns the currentRuleTraceInfoURL
	 * 
	 * @return currentRuleTraceInfoURL or null
	 */
	public RuleTraceInfoURL getCurrentRuleTraceInfoURL() {
		return currentRuleTraceInfoURL;
	}

	/**
	 * Returns the vars
	 * 
	 * @return vars
	 */
	public Map<String, String> getVars() {
		return vars;
	}

	/**
	 * Put Variable
	 * 
	 * @param name Name
	 * @param value Value
	 */
	public void putVar(String name, String value) {
		vars.put(name, value);
	}

	/**
	 * Returns the htmlCodeFromFirstURL
	 * 
	 * @return htmlCodeFromFirstURL
	 */
	public RuleHtmlCode getHtmlCodeFromFirstURL() {
		return htmlCodeFromFirstURL;
	}

	/**
	 * Set HTML Code from first URL
	 * 
	 * @param htmlCode HTML Code
	 */
	public void setHtmlCodeFromFirstURLData(String htmlCode) {
		htmlCodeFromFirstURL.setData(htmlCode, pipelineURL, pipelineReferrer);
		if (trace) {
			upo.addInfo("PageSourceCodeFromFirstURL", htmlCodeFromFirstURL);
		}
	}

	/**
	 * Returns the htmlCodeFirst
	 * 
	 * @return htmlCodeFirst
	 */
	public RuleHtmlCode getHtmlCodeFirst() {
		return htmlCodeFirst;
	}

	/**
	 * Set first HTML Code
	 * 
	 * @param htmlCode HTML Code
	 */
	public void setHtmlCodeFirstData(String htmlCode) {
		if (htmlCodeFirst.isAvailable()) {
			return;
		}
		htmlCodeFirst.setData(htmlCode, pipelineURL, pipelineReferrer);
		if (trace) {
			upo.addInfo("PageSourceCodeFirst", htmlCodeFirst);
		}
	}

	/**
	 * Returns the htmlCodeLast
	 * 
	 * @return htmlCodeLast
	 */
	public RuleHtmlCode getHtmlCodeLast() {
		return htmlCodeLast;
	}

	/**
	 * Set last HTML Code
	 * 
	 * @param htmlCode HTML Code
	 */
	public void setHtmlCodeLastData(String htmlCode) {
		htmlCodeLast.setData(htmlCode, pipelineURL, pipelineReferrer);
		if (trace) {
			upo.addInfo("PageSourceCodeLast", htmlCodeLast);
		}
	}

	/**
	 * Returns the referrer to use in downloadContainerPage-Method
	 * 
	 * @return Referrer
	 */
	public String getReferrer() {
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
	 * URL Decode Pipeline Result
	 */
	public void urlDecodePipelineResult() {
		URLCodec urlCodec = new URLCodec("UTF-8");
		try {
			pipelineResult = urlCodec.decode(pipelineResult);
		} catch (DecoderException e) {
			logger.error("Could not URL decode (application/x-www-form-urlencoded) pipeline result: {}", pipelineResult);
		}
	}

	/**
	 * Javascript Decode Pipeline Result
	 */
	public void javascriptDecodePipelineResult() {
		pipelineResult = StringEscapeUtils.unescapeEcmaScript(pipelineResult);
	}

	/**
	 * Decode Pipeline Result
	 */
	public void decodePipelineResult() {
		pipelineResult = HTTPUtil.decodeURL(pipelineResult);
	}

	/**
	 * Apply Pipeline URL to UPO Container URL
	 */
	public void applyPipelineURLToUPO() {
		upo.setContainerURL(pipelineURL);
	}

	/**
	 * Apply URLs for next pipeline
	 */
	public void applyURLsForNextPipeline() {
		pipelineReferrer = pipelineURL;
		pipelineURL = pipelineResult;
		pipelineThumbURL = pipelineResult;
	}

	/**
	 * Add Rule Trace Info URL
	 */
	public void addRuleTraceInfoURL() {
		currentRuleTraceInfoURL = null;
		if (ruleTraceInfo != null) {
			currentRuleTraceInfoURL = new RuleTraceInfoURL(pipelineURL);
			ruleTraceInfo.addURLTraceInfo(currentRuleTraceInfoURL);
		}
	}

	/**
	 * Add Rule Trace Info URL Container Page Step
	 * 
	 * @param step Step
	 */
	public void addRuleTraceInfoURLContainerPageStep(int step) {
		if (currentRuleTraceInfoURL != null) {
			currentRuleTraceInfoURL.addStep(new RuleTraceInfoURLDownloadContainerPage(step, pipelineURL, htmlCodeLast.getHtmlCode()));
		}
	}

	/**
	 * Add Rule Trace Info URL Replace Step
	 * 
	 * @param step Step
	 */
	public void addRuleTraceInfoURLReplaceStep(int step) {
		if (currentRuleTraceInfoURL != null) {
			currentRuleTraceInfoURL.addStep(new RuleTraceInfoURLReplace(step, pipelineResult));
		}
	}

	/**
	 * Download Container Page
	 * 
	 * @param url URL
	 * @param referrer Referrer
	 * @param options Options
	 * @return Result
	 * @throws HostException
	 */
	public String downloadContainerPage(String url, String referrer, DownloadContainerPageOptions options) throws HostException {
		return downloadContainerPageFunction.downloadContainerPage(url, referrer, options);
	}

}

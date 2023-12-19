package ch.supertomcat.bh.rules;

import ch.supertomcat.bh.exceptions.HostAbortedException;
import ch.supertomcat.bh.exceptions.HostCompletedException;
import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostFileNotExistException;
import ch.supertomcat.bh.exceptions.HostFileTemporaryOfflineException;
import ch.supertomcat.bh.rules.trace.RuleTraceInfo;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoFailures;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoFailuresFinal;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoFailuresSearch;
import ch.supertomcat.bh.rules.xml.FailureType;
import ch.supertomcat.bh.rules.xml.FailuresPipeline;

/**
 * RulePipeline
 */
public class RulePipelineFailures extends RulePipeline<FailuresPipeline> {
	/**
	 * Constructor
	 */
	public RulePipelineFailures() {
		super(new FailuresPipeline());
		definition.setFailureType(FailureType.FAILED);
		definition.setCheckURL(false);
		definition.setCheckThumbURL(false);
		definition.setCheckPageSourceCode(false);
	}

	/**
	 * Constructor
	 * 
	 * @param definition Definition
	 */
	public RulePipelineFailures(FailuresPipeline definition) {
		super(definition);
	}

	private String check(String input, RuleTraceInfoFailures traceInfo) {
		String result = "";
		int start = 0;
		for (int i = 0; i < regexps.size(); i++) {
			if (i < (regexps.size() - 1)) {
				int pos = regexps.get(i).doFailureSearch(input, start);
				if (pos >= 0) {
					logger.debug("FailureCheck -> Search done -> Step {} -> Pattern found at: {}", i, pos);
				} else {
					logger.debug("FailureCheck -> Search done -> Step {} -> Pattern not found!", i);
				}
				if (traceInfo != null) {
					traceInfo.addStep(new RuleTraceInfoFailuresSearch(i, start, pos));
				}
				start = pos;
			} else {
				result = regexps.get(i).doFailureLastSearch(input, start);
				logger.debug("FailureCheck -> Failure found -> Step {} -> Result: {}", i, result);
				if (traceInfo != null) {
					traceInfo.addStep(new RuleTraceInfoFailuresFinal(i, start, result));
				}
			}
		}
		return result;
	}

	/**
	 * Check for failure
	 * 
	 * @param ruleContext Rule Context
	 * @throws HostException
	 */
	public void checkForFailureURLOnly(RuleContext ruleContext) throws HostException {
		checkForFailure(ruleContext.getPipelineResult(), ruleContext.getRuleTraceInfo());
	}

	/**
	 * Check for failure
	 * 
	 * @param url URL
	 * @param ruleTraceInfo Rule Trace Info or null
	 * @throws HostException
	 */
	public void checkForFailure(String url, RuleTraceInfo ruleTraceInfo) throws HostException {
		String message = "";

		RuleTraceInfoFailures traceInfo = null;
		if (ruleTraceInfo != null) {
			traceInfo = new RuleTraceInfoFailures(url, "", "", false);
			ruleTraceInfo.addFailuresBeforeReplaceTraceInfo(traceInfo);
		}

		if (definition.isCheckURL()) {
			message = check(url, traceInfo);
		}

		if (!message.isEmpty()) {
			switch (definition.getFailureType()) {
				case COMPLETE:
					throw new HostCompletedException(message);
				case SLEEPING:
					throw new HostAbortedException(message);
				case FAILED_FILE_TEMPORARY_OFFLINE:
					throw new HostFileTemporaryOfflineException(message);
				case FAILED_FILE_NOT_EXIST:
					throw new HostFileNotExistException(message);
				case FAILED:
					throw new HostException(message);
				default:
					// Do nothing, other failure types are not supported
					break;
			}
		}
	}

	/**
	 * Check for failure
	 * 
	 * @param ruleContext Rule Context
	 * @throws HostException
	 */
	public void checkForFailure(RuleContext ruleContext) throws HostException {
		checkForFailure(ruleContext.getPipelineURL(), ruleContext.getPipelineThumbURL(), ruleContext.getHtmlCodeLast().getHtmlCode(), ruleContext.getRuleTraceInfo());
	}

	/**
	 * Check for failure
	 * 
	 * @param url URL
	 * @param thumbURL Thumbnail-URL
	 * @param htmlcode HTML-Code
	 * @param ruleTraceInfo Rule Trace Info or null
	 * @throws HostException
	 */
	public void checkForFailure(String url, String thumbURL, String htmlcode, RuleTraceInfo ruleTraceInfo) throws HostException {
		String message = "";

		RuleTraceInfoFailures traceInfo = null;
		if (ruleTraceInfo != null) {
			traceInfo = new RuleTraceInfoFailures(url, thumbURL, htmlcode, true);
			ruleTraceInfo.addFailuresBeforeReplaceTraceInfo(traceInfo);
		}

		if (definition.isCheckURL()) {
			message = check(url, traceInfo);
		}

		if (definition.isCheckThumbURL() && message.isEmpty()) {
			message = check(thumbURL, traceInfo);
		}

		if (definition.isCheckPageSourceCode() && message.isEmpty()) {
			message = check(htmlcode, traceInfo);
		}

		if (!message.isEmpty()) {
			switch (definition.getFailureType()) {
				case COMPLETE:
					throw new HostCompletedException(message);
				case SLEEPING:
					throw new HostAbortedException(message);
				case FAILED_FILE_TEMPORARY_OFFLINE:
					throw new HostFileTemporaryOfflineException(message);
				case FAILED_FILE_NOT_EXIST:
					throw new HostFileNotExistException(message);
				case FAILED:
					throw new HostException(message);
				default:
					// Do nothing, other failure types are not supported
					break;
			}
		}
	}
}

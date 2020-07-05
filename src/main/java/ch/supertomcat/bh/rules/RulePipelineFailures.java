package ch.supertomcat.bh.rules;

import org.jdom2.Element;

import ch.supertomcat.bh.exceptions.HostAbortedException;
import ch.supertomcat.bh.exceptions.HostCompletedException;
import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostFileNotExistException;
import ch.supertomcat.bh.exceptions.HostFileTemporaryOfflineException;
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

	@Override
	public Element getXmlElement() {
		Element e = super.getXmlElement();
		e.setAttribute("mode", "4");
		e.setAttribute("checkURL", String.valueOf(definition.isCheckURL()));
		e.setAttribute("checkThumbURL", String.valueOf(definition.isCheckThumbURL()));
		e.setAttribute("checkPageSourceCode", String.valueOf(definition.isCheckPageSourceCode()));
		e.setAttribute("failureType", String.valueOf(definition.getFailureType().ordinal()));
		return e;
	}

	private String check(String input) {
		String result = "";
		int start = 0;
		for (int i = 0; i < regexps.size(); i++) {
			if (i < (regexps.size() - 1)) {
				start = regexps.get(i).doFailureSearch(input, start);
				if (start > 0) {
					logger.debug("FailureCheck -> Search done -> Step " + i + " -> Pattern found at: " + start);
				} else {
					logger.debug("FailureCheck -> Search done -> Step " + i + " -> Pattern not found!");
				}
			} else {
				result = regexps.get(i).doFailureLastSearch(input, start);
				logger.debug("FailureCheck -> Failure found -> Step " + i + " -> Result: " + result);
			}
		}
		return result;
	}

	/**
	 * Check for failure
	 * 
	 * @param url URL
	 * @throws HostException
	 */
	public void checkForFailure(String url) throws HostException {
		String message = "";

		if (definition.isCheckURL()) {
			message = check(url);
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
	 * @param url URL
	 * @param thumbURL Thumbnail-URL
	 * @param htmlcode HTML-Code
	 * @throws HostException
	 */
	public void checkForFailure(String url, String thumbURL, String htmlcode) throws HostException {
		String message = "";

		if (definition.isCheckURL()) {
			message = check(url);
		}

		if (definition.isCheckThumbURL() && message.isEmpty()) {
			message = check(thumbURL);
		}

		if (definition.isCheckPageSourceCode() && message.isEmpty()) {
			message = check(htmlcode);
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

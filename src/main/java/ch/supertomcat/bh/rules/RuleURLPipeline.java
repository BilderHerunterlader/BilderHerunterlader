package ch.supertomcat.bh.rules;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.rules.xml.URLPipeline;

/**
 * Rule URL Pipeline
 * 
 * @param <T> URLPipeline Type
 */
public abstract class RuleURLPipeline<T extends URLPipeline> extends RulePipeline<T> {
	/**
	 * Constructor
	 * 
	 * @param definition Definition
	 */
	public RuleURLPipeline(T definition) {
		super(definition);
	}

	/**
	 * Sleep if required
	 */
	public void sleepIfRequired() {
		int waitBeforeExecute = definition.getWaitBeforeExecute();
		if (waitBeforeExecute > 0) {
			try {
				Thread.sleep(waitBeforeExecute);
			} catch (InterruptedException e) {
				logger.error("Sleep was interrupted", e);
			}
		}
	}

	/**
	 * Download Container Page
	 * 
	 * @param ruleContext Rule Context
	 * @param step Step
	 * @return Container Page or null if subclass does not need to download container page
	 * @throws HostException
	 */
	public abstract String downloadContainerPage(RuleContext ruleContext, int step) throws HostException;

	/**
	 * Get parsed URL
	 * 
	 * @param ruleContext Rule Context
	 * @return Result
	 * @throws HostException
	 */
	public abstract String getURL(RuleContext ruleContext) throws HostException;
}

package ch.supertomcat.bh.rules;

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
}

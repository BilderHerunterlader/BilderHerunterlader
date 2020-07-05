package ch.supertomcat.bh.rules;

import org.jdom2.Element;

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

	@Override
	public void fillXmlElement(Element e) {
		super.fillXmlElement(e);
		e.setAttribute("waitBeforeExecute", String.valueOf(definition.getWaitBeforeExecute()));
		e.setAttribute("urlDecodeResult", String.valueOf(definition.isUrlDecodeResult()));
		e.setAttribute("sendCookies", String.valueOf(definition.isSendCookies()));
	}
}

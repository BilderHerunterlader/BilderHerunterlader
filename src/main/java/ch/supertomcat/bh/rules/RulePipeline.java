package ch.supertomcat.bh.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.rules.xml.Pipeline;
import ch.supertomcat.bh.rules.xml.RuleRegex;

/**
 * RulePipeline
 * 
 * @param <T> Pipeline Type
 */
public abstract class RulePipeline<T extends Pipeline> {
	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Definition
	 */
	protected final T definition;

	/**
	 * RuleRegExps
	 */
	protected List<RuleRegExp> regexps = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param definition Definition
	 */
	public RulePipeline(T definition) {
		this.definition = definition;
		updateFromDefinition();
	}

	/**
	 * Update internal variables from definition
	 */
	public void updateFromDefinition() {
		regexps.clear();
		for (RuleRegex regexDefinition : definition.getRegexp()) {
			RuleRegExp ruleRegExp = new RuleRegExp(regexDefinition);
			regexps.add(ruleRegExp);
		}
	}

	/**
	 * Returns the definition
	 * 
	 * @return definition
	 */
	public T getDefinition() {
		return definition;
	}

	/**
	 * Returns the Element for creating the XML-File
	 * 
	 * @return Element
	 */
	public Element getXmlElement() {
		Element e = new Element("pipeline");
		fillXmlElement(e);
		return e;
	}

	/**
	 * Returns the Element for creating the XML-File
	 * 
	 * @param e Element
	 */
	public void fillXmlElement(Element e) {
		for (RuleRegExp regexp : regexps) {
			Element elRegex = new Element("regexp");
			elRegex.setAttribute("search", regexp.getSearch());
			elRegex.setAttribute("replace", regexp.getReplace());
			e.addContent(elRegex);
		}
	}

	/**
	 * Returns all RuleRegExps. The returned list can't be modified.
	 * 
	 * @return RuleRegExps
	 */
	public List<RuleRegExp> getRegexps() {
		return Collections.unmodifiableList(regexps);
	}

	/**
	 * Returns the RuleRegExp
	 * 
	 * @param index Index in the array
	 * @return RuleRegExp
	 */
	public RuleRegExp getRegexp(int index) {
		return regexps.get(index);
	}

	/**
	 * Adds a RuleRegExp to the pipeline
	 * 
	 * @param rre RuleRegExp
	 */
	public void addRegExp(RuleRegExp rre) {
		if (rre == null) {
			return;
		}
		regexps.add(rre);
		definition.getRegexp().add(rre.getDefinition());
	}

	/**
	 * Removes a RuleRegExp from the pipeline
	 * 
	 * @param index1 Index 1
	 * @param index2 Index 2
	 */
	public void swapRegExp(int index1, int index2) {
		if (index1 == index2) {
			return;
		}
		if (index1 >= regexps.size() || index1 < 0) {
			return;
		}
		if (index2 >= regexps.size() || index2 < 0) {
			return;
		}

		RuleRegExp regex1 = regexps.get(index1);
		RuleRegex definition1 = definition.getRegexp().get(index1);
		RuleRegExp regex2 = regexps.get(index2);
		RuleRegex definition2 = definition.getRegexp().get(index2);
		regexps.set(index2, regex1);
		definition.getRegexp().set(index2, definition1);
		regexps.set(index1, regex2);
		definition.getRegexp().set(index1, definition2);
	}

	/**
	 * Removes a RuleRegExp from the pipeline
	 * 
	 * @param index Index in the array
	 */
	public void removeRegExp(int index) {
		if (index >= regexps.size() || index < 0) {
			return;
		}
		regexps.remove(index);
		definition.getRegexp().remove(index);
	}
}

package ch.supertomcat.bh.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import ch.supertomcat.bh.rules.xml.RuleRegex;
import ch.supertomcat.bh.rules.xml.VarRuleRegex;

/**
 * Search and Replace by Regexp and store to variable
 */
public class RuleVarRegExp {
	/**
	 * Definition
	 */
	private final VarRuleRegex definition;

	/**
	 * RuleRegExps
	 */
	protected List<RuleRegExp> regexps = new ArrayList<>();

	/**
	 * Constructor
	 */
	public RuleVarRegExp() {
		this.definition = new VarRuleRegex();
	}

	/**
	 * Constructor
	 * 
	 * @param definition Definition
	 * @throws PatternSyntaxException
	 */
	public RuleVarRegExp(VarRuleRegex definition) throws PatternSyntaxException {
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
	public VarRuleRegex getDefinition() {
		return definition;
	}

	/**
	 * @return Variable Name
	 */
	public String getVariableName() {
		return definition.getVariableName();
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

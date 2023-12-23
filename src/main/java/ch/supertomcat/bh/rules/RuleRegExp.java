package ch.supertomcat.bh.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.rules.xml.RuleRegex;
import ch.supertomcat.supertomcatutils.regex.RegexReplace;

/**
 * Search and Replace by Regexp
 */
public class RuleRegExp extends RegexReplace {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Definition
	 */
	private final RuleRegex definition;

	/**
	 * Flag if variables are defined in Search
	 */
	private boolean variablesInSearch;

	/**
	 * Constructor
	 */
	public RuleRegExp() {
		this.definition = new RuleRegex();
		this.definition.setPattern("");
		this.definition.setReplacement("");
		variablesInSearch = false;
	}

	/**
	 * Constructor
	 * 
	 * @param definition Definition
	 * @throws PatternSyntaxException
	 */
	public RuleRegExp(RuleRegex definition) throws PatternSyntaxException {
		super(definition.getPattern(), definition.getReplacement(), !checkVariablesInString(definition.getPattern()));
		this.definition = definition;
		variablesInSearch = checkVariablesInString(search);
	}

	/**
	 * Constructor
	 * 
	 * @param search Search
	 * @param replace Replace
	 * @throws PatternSyntaxException
	 */
	public RuleRegExp(String search, String replace) throws PatternSyntaxException {
		super(search, replace, !checkVariablesInString(search));
		this.definition = new RuleRegex();
		this.definition.setPattern(search);
		this.definition.setReplacement(replace);
		variablesInSearch = checkVariablesInString(search);
	}

	/**
	 * Returns the definition
	 * 
	 * @return definition
	 */
	public RuleRegex getDefinition() {
		return definition;
	}

	@Override
	public void setSearch(String search) throws PatternSyntaxException {
		variablesInSearch = checkVariablesInString(search);
		super.setSearch(search, !variablesInSearch);
		/*
		 * defintion is null when the super constructor with parameters is called, but the constructors in this class will set the pattern anyway, so we can
		 * just do nothing here
		 */
		if (definition != null) {
			definition.setPattern(search);
		}
	}

	@Override
	public void setReplace(String replace) {
		super.setReplace(replace);
		/*
		 * defintion is null when the super constructor with parameters is called, but the constructors in this class will set the replacement anyway, so we can
		 * just do nothing here
		 */
		if (definition != null) {
			definition.setReplacement(replace);
		}
	}

	/**
	 * Search-Method for Pagesourcecodes
	 * Returns the start-position when found the pattern
	 * Returns -1 if not found the pattern or an error occured
	 * 
	 * @param htmlcode Sourcecode
	 * @param start Startposition
	 * @return Found position
	 */
	public int doFailureSearch(String htmlcode, int start) {
		// If pattern is not compiled or url is empty we return an empty String
		if (pattern == null || search.isEmpty() || htmlcode.isEmpty() || start < 0 || start > htmlcode.length()) {
			return -1;
		}

		// Now we search for the position
		Matcher matcher = pattern.matcher(htmlcode);
		if (matcher.find(start)) {
			return matcher.start();
		}
		return -1;
	}

	/**
	 * @param htmlcode Sourcecode
	 * @param start Startposition
	 * @return Found position
	 */
	public String doFailureLastSearch(String htmlcode, int start) {
		// If pattern is not compiled or url is empty we return an empty String
		if (pattern == null || search.isEmpty() || htmlcode.isEmpty() || start < 0 || start > htmlcode.length()) {
			return "";
		}

		// Now we search for the position
		Matcher matcher = pattern.matcher(htmlcode);
		if (matcher.find(start)) {
			return matcher.group();
		}
		return "";
	}

	/**
	 * Replace-Method for URLs
	 * 
	 * @param url URL
	 * @param pic Pic
	 * @return URL
	 */
	public String doURLReplace(String url, Pic pic) {
		return doURLReplace(url, pic, null);
	}

	/**
	 * Replace-Method for URLs
	 * 
	 * @param url URL
	 * @param pic Pic
	 * @param ruleContext Rule Context or null
	 * @return URL
	 */
	public String doURLReplace(String url, Pic pic, RuleContext ruleContext) {
		// If pattern is not compiled or url is empty we return an empty String
		if ((pattern == null && !variablesInSearch) || search.isEmpty() || url.isEmpty()) {
			return "";
		}

		Pattern patternToUse = pattern;
		if (variablesInSearch) {
			String resolvedSearch = replaceVariablesInString(search, url, pic, ruleContext, true);
			patternToUse = Pattern.compile(resolvedSearch);
		}

		// Now we replace the variables if the user defined some in the Replace-String
		String dReplace = replaceVariablesInString(replace, url, pic, ruleContext, false);

		// Now we replace
		return patternToUse.matcher(url).replaceAll(dReplace);
	}

	/**
	 * Search-Method for Pagesourcecodes
	 * Returns the start-position when found the pattern
	 * Returns -1 if not found the pattern or an error occured
	 * 
	 * @param htmlcode Sourcecode
	 * @param start Startposition
	 * @return Found position
	 */
	public int doPageSourcecodeSearch(String htmlcode, int start) {
		return doSearch(htmlcode, start);
	}

	/**
	 * Replace-Method for Pagesourcecodes
	 * Returns an empty String if the pattern was not found
	 * 
	 * @param htmlcode Sourcecode
	 * @param start Startposition
	 * @param url Container-URL
	 * @param pic Pic
	 * @return URL
	 */
	public String doPageSourcecodeReplace(String htmlcode, int start, String url, Pic pic) {
		return doPageSourcecodeReplace(htmlcode, start, url, pic, null);
	}

	/**
	 * Replace-Method for Pagesourcecodes
	 * Returns an empty String if the pattern was not found
	 * 
	 * @param htmlcode Sourcecode
	 * @param start Startposition
	 * @param url Container-URL
	 * @param pic Pic
	 * @param ruleContext Rule Context or null
	 * @return URL
	 */
	public String doPageSourcecodeReplace(String htmlcode, int start, String url, Pic pic, RuleContext ruleContext) {
		// If pattern is not compiled or url is empty we return an empty String
		if ((pattern == null && !variablesInSearch) || search.isEmpty() || url.isEmpty() || start < 0 || start > htmlcode.length()) {
			return "";
		}

		Pattern patternToUse = pattern;
		if (variablesInSearch) {
			logger.debug("Unresolved search pattern: {}", search);
			String resolvedSearch = replaceVariablesInString(search, url, pic, ruleContext, true);
			logger.debug("Resolved search pattern: {}", resolvedSearch);
			patternToUse = Pattern.compile(resolvedSearch);
		}

		// Now we replace the variables if the user defined some in the Replace-String
		String dReplace = replaceVariablesInString(replace, url, pic, ruleContext, false);

		// Now we replace
		Matcher matcher = patternToUse.matcher(htmlcode);
		if (matcher.find(start)) {
			/*
			 * Here we replace only the region that was matched and also return only the matched region. Instead of returning the whole input with parts
			 * replaced.
			 */
			Matcher matchRegionMatcher = patternToUse.matcher(matcher.group());
			return matchRegionMatcher.replaceAll(dReplace);
		}
		return "";
	}

	/**
	 * The user can use variables in the replace-String of a rule,
	 * so here we replace the variables. If searchStringReplacement is true, then old style variables are not replaced. Old style variables are only used for
	 * replacement of replace String, because of compatibility.
	 * 
	 * @param replace Replace-String
	 * @param url Container-URL
	 * @param pic Pic
	 * @param ruleContext Rule Context or null
	 * @param searchStringReplacement True if a search string is replaced, false otherwise
	 * @return Replace-String
	 */
	private String replaceVariablesInString(final String replace, String url, Pic pic, RuleContext ruleContext, boolean searchStringReplacement) {
		/*
		 * We initialise the variables, which the user can use in a rule.
		 * $SRV -> Domain only with / at the end
		 * $SRVT -> Domain only without / at the end
		 * $URL -> URL without the filename with / at the end
		 * $URLT -> URL without the filename without / at the end
		 * $FURL -> The complete URL
		 * $DIR -> Directory in which the file will be downloaded
		 * $FILE -> Filename
		 * 
		 * Example:
		 * URL is: http://bla.irgendwas.net/folder/image.jpg
		 * SRV would be http://bla.irgendwas.net/
		 * URL would be http://bla.irgendwas.net/ordner/
		 * DIR and FILE are read out from the Pic-Object
		 */
		String retval = replace;
		String dSRV = "";
		String dSRVT = "";
		String dURL = "";
		String dURLT = "";
		String dFURL = url;
		String dDIR = "";
		String dFILE = "";
		if (pic != null) {
			dDIR = pic.getTargetPath();
			dFILE = pic.getTargetFilename();
		}

		if (url.matches("^https?://.+\\..+/.*")) {
			int startIndex = url.startsWith("https") ? 8 : 7;
			dSRV = url.substring(0, url.indexOf("/", startIndex) + 1);
			dSRVT = dSRV.substring(0, dSRV.length() - 1);
		}

		int last = url.lastIndexOf("/");
		if (last > -1) {
			dURL = url.substring(0, last + 1);
			dURLT = dURL.substring(0, dURL.length() - 1);
		}

		Map<String, String> vars;
		if (ruleContext != null) {
			vars = new HashMap<>(ruleContext.getVars());
		} else {
			vars = new HashMap<>();
		}

		// Now we replace the variables if the user defined some in the Replace-String
		retval = replaceIfNecessary(retval, "$SRV", dSRV, "SRV", vars, searchStringReplacement);
		retval = replaceIfNecessary(retval, "$SRVT", dSRVT, "SRVT", vars, searchStringReplacement);
		retval = replaceIfNecessary(retval, "$URL", dURL, "URL", vars, searchStringReplacement);
		retval = replaceIfNecessary(retval, "$URLT", dURLT, "URLT", vars, searchStringReplacement);
		retval = replaceIfNecessary(retval, "$FURL", dFURL, "FURL", vars, searchStringReplacement);
		retval = replaceIfNecessary(retval, "$DIR", dDIR, "DIR", vars, searchStringReplacement);
		retval = replaceIfNecessary(retval, "$FILE", dFILE, "FILE", vars, searchStringReplacement);

		if (!vars.isEmpty()) {
			StringSubstitutor substitutor = new StringSubstitutor(vars);
			retval = substitutor.replace(retval);
		}

		return retval;
	}

	private String replaceIfNecessary(String input, String variableKey, String variableValue, String mapVariableKey, Map<String, String> vars, boolean searchStringReplacement) {
		String result = input;
		if (!variableValue.isEmpty()) {
			if (!searchStringReplacement) {
				result = result.replace(variableKey, variableValue);
			}
			vars.put(mapVariableKey, variableValue);
		}
		return result;
	}

	/**
	 * Check if variables are in String
	 * 
	 * @param input Input
	 * @return True if variables are in input, false otherwise
	 */
	public static boolean checkVariablesInString(String input) {
		StringSubstitutor substitutor = new StringSubstitutor(key -> "RULE_REG_EXP_UNDEFINED_VARIABLE");
		String result = substitutor.replace(input);
		return !result.equals(input);
	}
}

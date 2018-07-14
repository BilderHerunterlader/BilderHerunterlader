package ch.supertomcat.bh.hoster.linkextract;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ch.supertomcat.bh.pic.URL;

/**
 * 
 *
 */
public class ExtractConfigWhitelist {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(ExtractConfigWhitelist.class);

	private Pattern patternContainerURL = null;
	private List<Pattern> patternsLinksToExtract = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param containerURLPattern Container-URL Pattern
	 * @param linksToExtractPatterns Links To Extract Patterns
	 */
	public ExtractConfigWhitelist(String containerURLPattern, List<String> linksToExtractPatterns) {
		try {
			patternContainerURL = Pattern.compile(containerURLPattern);
		} catch (PatternSyntaxException pse) {
			logger.error(pse.getMessage(), pse);
		}

		for (String str : linksToExtractPatterns) {
			try {
				patternsLinksToExtract.add(Pattern.compile(str));
			} catch (PatternSyntaxException pse) {
				logger.error(pse.getMessage(), pse);
			}
		}
	}

	/**
	 * Returns the patternContainerURL
	 * 
	 * @return patternContainerURL
	 */
	public Pattern getPatternContainerURL() {
		return patternContainerURL;
	}

	/**
	 * Returns the patternsLinksToExtract
	 * 
	 * @return patternsLinksToExtract
	 */
	public List<Pattern> getPatternsLinksToExtract() {
		return patternsLinksToExtract;
	}

	/**
	 * @param input Input
	 * @return TRUE/FALSE
	 */
	public boolean isContainerURL(String input) {
		Matcher matcher = getContainerURLMatcher(input);
		if (matcher == null) {
			return false;
		}
		return matcher.matches();
	}

	/**
	 * @param input Input
	 * @return TRUE/FALSE
	 */
	public boolean isAllowedURL(String input) {
		List<Matcher> matchers = getLinksToExtractMatchers(input);
		for (Matcher matcher : matchers) {
			if (matcher.matches()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return ILinkExtractFilter
	 */
	public ILinkExtractFilter getExtractFilter() {
		ILinkExtractFilter filterCategory = new ILinkExtractFilter() {
			@Override
			public boolean isLinkAccepted(Node nodeURL, Document nodeRoot, URL url, String containerURL) {
				return isAllowedURL(url.getURL());
			}
		};
		return filterCategory;
	}

	/**
	 * @param input Input
	 * @return Matcher
	 */
	public Matcher getContainerURLMatcher(String input) {
		if (patternContainerURL == null) {
			return null;
		}
		return patternContainerURL.matcher(input);
	}

	/**
	 * @param input Input
	 * @return Matchers
	 */
	public List<Matcher> getLinksToExtractMatchers(String input) {
		List<Matcher> matchers = new ArrayList<>();
		for (Pattern pattern : patternsLinksToExtract) {
			matchers.add(pattern.matcher(input));
		}
		return matchers;
	}
}

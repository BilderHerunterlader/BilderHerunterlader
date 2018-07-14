package ch.supertomcat.bh.rules;

import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.supertomcattools.regextools.RegexReplace;


/**
 * Search and Replace by Regexp
 */
public class RuleRegExp extends RegexReplace {
	
	/**
	 * Constructor
	 */
	public RuleRegExp() {
		super();
	}

	/**
	 * Constructor
	 * @param search Search
	 * @param replace Replace
	 * @throws PatternSyntaxException
	 */
	public RuleRegExp(String search, String replace) throws PatternSyntaxException {
		super(search, replace);
	}
	
	/**
	 * Search-Method for Pagesourcecodes
	 * Returns the start-position when found the pattern
	 * Returns -1 if not found the pattern or an error occured
	 * @param htmlcode Sourcecode
	 * @param start Startposition
	 * @return Found position
	 */
	public int doFailureSearch(String htmlcode, int start) {
		//If pattern is not compiled or url is empty we return an empty String
		if (pattern == null) return -1;
		if (search.length() == 0) return -1;
		if (htmlcode.length() == 0) return -1;
		if (start > htmlcode.length()) return -1;
		
		try {
			//Now we search for the position
			Matcher matcher = pattern.matcher(htmlcode);
			if (matcher.find(start)) {
				int position = matcher.start();
				return position;
			}
		} catch (Exception e) {
			return -1;
		}
		return -1;
	}
	
	/**
	 * @param htmlcode Sourcecode
	 * @param start Startposition
	 * @return Found position
	 */
	public String doFailureLastSearch(String htmlcode, int start) {
		//If pattern is not compiled or url is empty we return an empty String
		if (pattern == null) return "";
		if (search.length() == 0) return "";
		if (htmlcode.length() == 0) return "";
		if (start > htmlcode.length()) return "";
		
		try {
			//Now we search for the position
			Matcher matcher = pattern.matcher(htmlcode);
			if (matcher.find(start)) {
				return htmlcode.substring(matcher.start(), matcher.end());
			}
		} catch (Exception e) {
			return "";
		}
		return "";
	}
	
	/**
	 * Replace-Method for URLs
	 * @param url URL
	 * @param pic Pic
	 * @return URL
	 */
	public String doURLReplace(String url, Pic pic) {
		//If pattern is not compiled or url is empty we return an empty String
		if (pattern == null) return "";
		if (search.length() == 0) return "";
		if (url.length() == 0) return "";
		
		//Now we replace the variables if the user defined some in the Replace-String
		String dReplace = replaceVariablesInReplaceString(replace, url, pic);
		
		try {
			//Now we replace
			Matcher matcher = pattern.matcher(url);
			String result = matcher.replaceAll(dReplace);
			return result;
		} catch (Exception e) {
			return "";
		}
	}
	
	/**
	 * Search-Method for Pagesourcecodes
	 * Returns the start-position when found the pattern
	 * Returns -1 if not found the pattern or an error occured
	 * @param htmlcode Sourcecode
	 * @param start Startposition
	 * @return Found position
	 */
	public int doPageSourcecodeSearch(String htmlcode, int start) {
		return super.doSearch(htmlcode, start);
	}
	
	/**
	 * Replace-Method for Pagesourcecodes
	 * Returns an empty String if the pattern was not found
	 * @param htmlcode Sourcecode
	 * @param start Startposition
	 * @param url Container-URL
	 * @param pic Pic
	 * @return URL
	 */
	public String doPageSourcecodeReplace(String htmlcode, int start, String url, Pic pic) {
		//If pattern is not compiled or url is empty we return an empty String
		if (pattern == null) return "";
		if (search.length() == 0) return "";
		if (url.length() == 0) return "";
		if (start > htmlcode.length()) return "";
		if (start < 0) return "";
		
		//Now we replace the variables if the user defined some in the Replace-String
		String dReplace = replaceVariablesInReplaceString(replace, url, pic);
		
		try {
			//Now we replace
			Matcher matcher = pattern.matcher(htmlcode);
			Matcher smatcher;
			String result = "";
			if (matcher.find(start)) {
				smatcher = pattern.matcher(htmlcode.substring(matcher.start(), matcher.end()));
				result = smatcher.replaceAll(dReplace);
			}
			return result;
		} catch (Exception e) {
			return "";
		}
	}
	
	/**
	 * The user can use variables in the replace-String of a rule, 
	 * so here we replace the variables
	 * @param replace Replace-String
	 * @param url Container-URL
	 * @param pic Pic
	 * @return Replace-String
	 */
	private String replaceVariablesInReplaceString(final String replace, String url, Pic pic) {
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
		
		//Now we replace the variables if the user defined some in the Replace-String
		if (dSRV.length() > 0) {
			retval = retval.replace("$SRV", dSRV);
		}
		if (dSRVT.length() > 0) {
			retval = retval.replace("$SRVT", dSRVT);
		}
		if (dURL.length() > 0) {
			retval = retval.replace("$URL", dURL);
		}
		if (dURLT.length() > 0) {
			retval = retval.replace("$URLT", dURLT);
		}
		if (dFURL.length() > 0) {
			retval = retval.replace("$FURL", dFURL);
		}
		if (dFURL.length() > 0) {
			retval = retval.replace("$DIR", dDIR);
		}
		if (dFURL.length() > 0) {
			retval = retval.replace("$FILE", dFILE);
		}
		return retval;
	}
}

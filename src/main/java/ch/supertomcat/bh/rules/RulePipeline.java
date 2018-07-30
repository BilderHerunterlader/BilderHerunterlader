package ch.supertomcat.bh.rules;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.PicState;

/**
 * RulePipeline
 */
public abstract class RulePipeline {
	/**
	 * Replace in Container-URL
	 */
	public static final int RULEPIPELINE_MODE_CONTAINER_URL = 0;

	/**
	 * Replace in Thumbnail-URL
	 */
	public static final int RULEPIPELINE_MODE_THUMBNAIL_URL = 1;

	/**
	 * Use Container-URL-Filename-Part for Filename Search and Replace
	 */
	public static final int RULEPIPELINE_MODE_FILENAME_CONTAINER_URL_FILENAME_PART = 0;

	/**
	 * Use Container-URL for Filename Search and Replace
	 */
	public static final int RULEPIPELINE_MODE_FILENAME_CONTAINER_URL = 1;

	/**
	 * Use Thumbnail-URL-Filename-Part for Filename Search and Replace
	 */
	public static final int RULEPIPELINE_MODE_FILENAME_THUMBNAIL_URL_FILENAME_PART = 2;

	/**
	 * Use Thumbnail-URL for Filename Search and Replace
	 */
	public static final int RULEPIPELINE_MODE_FILENAME_THUMBNAIL_URL = 3;

	/**
	 * Use Container-Page-Sourcecode from first URL for Filename Search and Replace
	 */
	public static final int RULEPIPELINE_MODE_FILENAME_CONTAINER_PAGE_SOURCECODE = 4;

	/**
	 * Use Download-URL for Filename Search and Replace
	 */
	public static final int RULEPIPELINE_MODE_FILENAME_DOWNLOAD_URL = 5;

	/**
	 * Use Download-URL-Filename-Part for Filename Search and Replace
	 */
	public static final int RULEPIPELINE_MODE_FILENAME_DOWNLOAD_URL_FILENAME_PART = 6;

	/**
	 * Use Last Container-URL-Filename-Part for Filename Search and Replace
	 */
	public static final int RULEPIPELINE_MODE_FILENAME_LAST_CONTAINER_URL_FILENAME_PART = 7;

	/**
	 * Use Last Container-URL for Filename Search and Replace
	 */
	public static final int RULEPIPELINE_MODE_FILENAME_LAST_CONTAINER_URL = 8;

	/**
	 * Use First Container-Page-Sourcecode for Filename Search and Replace
	 */
	public static final int RULEPIPELINE_MODE_FILENAME_FIRST_CONTAINER_PAGE_SOURCECODE = 9;

	/**
	 * Use Last Container-Page-Sourcecode for Filename Search and Replace
	 */
	public static final int RULEPIPELINE_MODE_FILENAME_LAST_CONTAINER_PAGE_SOURCECODE = 10;

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Mode
	 */
	protected int mode = Rule.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL;

	/**
	 * RuleRegExps
	 */
	protected List<RuleRegExp> regexps = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param mode Rule-Mode
	 */
	public RulePipeline(int mode) {
		this.mode = mode;
	}

	/**
	 * Constructor
	 * 
	 * @param e Element
	 */
	public RulePipeline(Element e) {
		try {
			this.mode = Integer.parseInt(e.getAttributeValue("mode"));
			if (!((this.mode == Rule.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL) || (this.mode == Rule.RULE_MODE_CONTAINER_PAGE_SOURCECODE) || (this.mode == Rule.RULE_MODE_FILENAME)
					|| (this.mode == Rule.RULE_MODE_FILENAME_ON_DOWNLOAD_SELECTION) || (this.mode == Rule.RULE_MODE_FAILURES) || (this.mode == Rule.RULE_MODE_JAVASCRIPT))) {
				mode = Rule.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL;
			}
		} catch (Exception ex) {
		}

		for (Element child : e.getChildren("regexp")) {
			try {
				String search = child.getAttributeValue("search");
				String replace = child.getAttributeValue("replace");
				RuleRegExp regexp = new RuleRegExp(search, replace);
				regexps.add(regexp);
			} catch (Exception ex) {
				logger.error("Could not parse regexp: {}", child, ex);
			}
		}
	}

	/**
	 * Returns the Element for creating the XML-File
	 * 
	 * @return Element
	 */
	public Element getXmlElement() {
		Element e = new Element("pipeline");
		e.setAttribute("mode", String.valueOf(this.mode));
		for (RuleRegExp regexp : regexps) {
			Element elRegex = new Element("regexp");
			elRegex.setAttribute("search", regexp.getSearch());
			elRegex.setAttribute("replace", regexp.getReplace());
			e.addContent(elRegex);
		}
		return e;
	}

	/**
	 * Returns the mode
	 * 
	 * @return Mode
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * Sets the Mode
	 * 
	 * @param mode Mode
	 */
	public void setMode(int mode) {
		if ((mode == Rule.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL) || (mode == Rule.RULE_MODE_CONTAINER_PAGE_SOURCECODE) || (mode == Rule.RULE_MODE_FILENAME)
				|| (mode == Rule.RULE_MODE_FILENAME_ON_DOWNLOAD_SELECTION) || (mode == Rule.RULE_MODE_FAILURES) || (mode == Rule.RULE_MODE_JAVASCRIPT)) {
			this.mode = mode;
		}
	}

	/*
	 * All Pipelines except Javascript
	 */

	/**
	 * Returns all RuleRegExps
	 * 
	 * @return RuleRegExps
	 */
	public List<RuleRegExp> getRegexps() {
		return regexps;
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
		RuleRegExp regex2 = regexps.get(index2);
		regexps.set(index2, regex1);
		regexps.set(index1, regex2);
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
	}

	/*
	 * ONLY Javascript
	 */
	/**
	 * @return Javascript Code
	 */
	public String getJavascript() {
		return "";
	}

	/**
	 * Sets Javascript Code
	 * 
	 * @param javascriptCode Javascript Code
	 */
	public void setJavascript(String javascriptCode) {
	}

	/*
	 * ONLY URL Regex Pipelines
	 */

	/**
	 * Returns the UrlMode
	 * 
	 * @return UrlMode
	 */
	public int getURLMode() {
		return -1;
	}

	/**
	 * Sets the UrlMode
	 * 
	 * @param urlMode UrlMode
	 */
	public void setURLMode(int urlMode) {
	}

	/**
	 * Returns parsed URL
	 * 
	 * @param url Container-URL
	 * @param thumbURL Thumbnail-URL
	 * @param htmlcode Sourcecode
	 * @param pic Pic
	 * @return URL
	 * @throws HostException
	 */
	public String getURL(String url, String thumbURL, String htmlcode, Pic pic) throws HostException {
		return "";
	}

	/*
	 * ONLY URL Javascript Pipelines
	 */

	/**
	 * @param url Container-URL
	 * @param thumbURL Thumbnail-URL
	 * @param htmlcode Sourcecode
	 * @param upo URLParseObject
	 * @return URL
	 * @throws HostException
	 */
	public String getURLByJavascript(String url, String thumbURL, String htmlcode, URLParseObject upo) throws HostException {
		return "";
	}

	/*
	 * ONLY URL Pipelines
	 */

	/**
	 * Returns the waitBeforeExecute
	 * 
	 * @return waitBeforeExecute
	 */
	public int getWaitBeforeExecute() {
		return 0;
	}

	/**
	 * Sets the waitBeforeExecute
	 * 
	 * @param waitBeforeExecute waitBeforeExecute
	 */
	public void setWaitBeforeExecute(int waitBeforeExecute) {
	}

	/**
	 * Returns the urlDecodeResult
	 * 
	 * @return urlDecodeResult
	 */
	public boolean isUrlDecodeResult() {
		return false;
	}

	/**
	 * Sets the urlDecodeResult
	 * 
	 * @param urlDecodeResult urlDecodeResult
	 */
	public void setUrlDecodeResult(boolean urlDecodeResult) {
	}

	/**
	 * Returns if cookies should be sent
	 * 
	 * @return True if cookies should be sent, false otherwise
	 */
	public boolean isSendCookies() {
		return true;
	}

	/**
	 * Sets if cookies should be sent
	 * 
	 * @param sendCookies True if cookies should be sent, false otherwise
	 */
	public void setSendCookies(boolean sendCookies) {
	}

	/*
	 * ONLY Filename Pipelines
	 */

	/**
	 * Returns the filenameMode
	 * 
	 * @return filenameMode
	 */
	public int getFilenameMode() {
		return -1;
	}

	/**
	 * Sets the filenameMode
	 * 
	 * @param filenameMode filenameMode
	 */
	public void setFilenameMode(int filenameMode) {
	}

	/**
	 * Returns the filenameDownloadSelectionMode
	 * 
	 * @return filenameDownloadSelectionMode
	 */
	public int getFilenameDownloadSelectionMode() {
		return -1;
	}

	/**
	 * Sets the filenameDownloadSelectionMode
	 * 
	 * @param filenameDownloadSelectionMode filenameDownloadSelectionMode
	 */
	public void setFilenameDownloadSelectionMode(int filenameDownloadSelectionMode) {
	}

	/**
	 * Returns the filename after replacement
	 * 
	 * @param url URL
	 * @param thumbURL Thumbnail-URL
	 * @param htmlcode Container-Page-Sourcecode
	 * @param pic Pic
	 * @return Filename
	 */
	public String getCorrectedFilename(String url, String thumbURL, String htmlcode, Pic pic) {
		return "";
	}

	/**
	 * Returns the filename after replacement
	 * 
	 * @param url URL
	 * @return Filename
	 */
	public String getCorrectedFilenameOnDownloadSelection(String url) {
		return "";
	}

	/*
	 * ONLY Failure Pipelines
	 */

	/**
	 * Returns the failureType
	 * 
	 * @return failureType
	 */
	public PicState getFailureType() {
		return null;
	}

	/**
	 * Sets the failureType
	 * 
	 * @param failureType failureType
	 */
	public void setFailureType(PicState failureType) {
	}

	/**
	 * Returns the checkURL
	 * 
	 * @return checkURL
	 */
	public boolean isCheckURL() {
		return false;
	}

	/**
	 * Sets the checkURL
	 * 
	 * @param checkURL checkURL
	 */
	public void setCheckURL(boolean checkURL) {
	}

	/**
	 * Returns the checkThumbURL
	 * 
	 * @return checkThumbURL
	 */
	public boolean isCheckThumbURL() {
		return false;
	}

	/**
	 * Sets the checkThumbURL
	 * 
	 * @param checkThumbURL checkThumbURL
	 */
	public void setCheckThumbURL(boolean checkThumbURL) {
	}

	/**
	 * Returns the checkPageSourceCode
	 * 
	 * @return checkPageSourceCode
	 */
	public boolean isCheckPageSourceCode() {
		return false;
	}

	/**
	 * Sets the checkPageSourceCode
	 * 
	 * @param checkPageSourceCode checkPageSourceCode
	 */
	public void setCheckPageSourceCode(boolean checkPageSourceCode) {
	}

	/**
	 * @param url URL
	 * @throws HostException
	 */
	public void checkForFailure(String url) throws HostException {
	}

	/**
	 * @param url URL
	 * @param thumbURL Thumbnail-URL
	 * @param htmlcode HTML-Code
	 * @throws HostException
	 */
	public void checkForFailure(String url, String thumbURL, String htmlcode) throws HostException {
	}
}

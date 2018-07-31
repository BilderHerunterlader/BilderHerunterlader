package ch.supertomcat.bh.rules;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.pic.Pic;

/**
 * RulePipeline
 */
public class RulePipelineFilename extends RulePipeline {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Defines which source for filename search and replace should be used (Only used when mode is 2)
	 */
	private RuleFilenameMode filenameMode = RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_CONTAINER_URL_FILENAME_PART;

	/**
	 * Defines which source for filename for download selection search and replace should be used (Only used when mode is 3)
	 */
	private RuleFilenameMode filenameDownloadSelectionMode = RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_CONTAINER_URL_FILENAME_PART;

	/**
	 * Constructor
	 * 
	 * @param mode Rule-Mode
	 */
	public RulePipelineFilename(RuleMode mode) {
		super(mode);
	}

	/**
	 * Constructor
	 * 
	 * @param e Element
	 */
	public RulePipelineFilename(Element e) {
		super(e);
		if (this.mode == RuleMode.RULE_MODE_FILENAME) {
			try {
				this.setFilenameMode(RuleFilenameMode.getByValue(Integer.parseInt(e.getAttributeValue("filenamemode"))));
			} catch (Exception exx) {
			}
		} else if (this.mode == RuleMode.RULE_MODE_FILENAME_ON_DOWNLOAD_SELECTION) {
			try {
				this.setFilenameDownloadSelectionMode(RuleFilenameMode.getByValue(Integer.parseInt(e.getAttributeValue("filenameDownloadSelectionMode"))));
			} catch (Exception exx) {
			}
		}
	}

	@Override
	public Element getXmlElement() {
		Element e = super.getXmlElement();
		if (this.mode == RuleMode.RULE_MODE_FILENAME) {
			e.setAttribute("filenamemode", String.valueOf(this.filenameMode.getValue()));
		} else if (this.mode == RuleMode.RULE_MODE_FILENAME_ON_DOWNLOAD_SELECTION) {
			e.setAttribute("filenameDownloadSelectionMode", String.valueOf(this.filenameDownloadSelectionMode.getValue()));
		}
		return e;
	}

	@Override
	public RuleFilenameMode getFilenameMode() {
		return filenameMode;
	}

	@Override
	public void setFilenameMode(RuleFilenameMode filenameMode) {
		this.filenameMode = filenameMode;
	}

	@Override
	public RuleFilenameMode getFilenameDownloadSelectionMode() {
		return filenameDownloadSelectionMode;
	}

	@Override
	public void setFilenameDownloadSelectionMode(RuleFilenameMode filenameDownloadSelectionMode) {
		if (filenameDownloadSelectionMode == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_CONTAINER_URL_FILENAME_PART
				|| filenameDownloadSelectionMode == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_CONTAINER_URL) {
			this.filenameDownloadSelectionMode = filenameDownloadSelectionMode;
		}
	}

	@Override
	public String getCorrectedFilename(String url, String thumbURL, String htmlcode, Pic pic) {
		if (this.mode == RuleMode.RULE_MODE_FILENAME) {
			String result = url;
			boolean bSourcecode = false;
			if ((this.filenameMode == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_THUMBNAIL_URL_FILENAME_PART) || (this.filenameMode == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_THUMBNAIL_URL)) {
				result = thumbURL;
			} else if (this.filenameMode == RuleFilenameMode.RULEPIPELINE_MODE_FILENAME_CONTAINER_PAGE_SOURCECODE) {
				result = htmlcode;
				bSourcecode = true;
			}
			if (bSourcecode == false) {
				for (int i = 0; i < regexps.size(); i++) {
					result = regexps.get(i).doURLReplace(result, pic);
					logger.debug(url + " -> Filename Replace done -> Step " + i + " -> Result: " + result);
				}
			} else {
				int start = 0;
				for (int i = 0; i < regexps.size(); i++) {
					if (i < (regexps.size() - 1)) {
						start = regexps.get(i).doPageSourcecodeSearch(htmlcode, start);
						if (start > 0) {
							logger.debug(url + " -> Filename Search done -> Step " + i + " -> Pattern found at: " + start);
						} else {
							logger.debug(url + " -> Filename Search done -> Step " + i + " -> Pattern not found!");
						}
					} else {
						result = regexps.get(i).doPageSourcecodeReplace(htmlcode, start, url, pic);
						logger.debug(url + " -> Filename Replace done -> Step " + i + " -> Result: " + result);
					}
				}
			}
			return result;
		}
		return "";
	}

	@Override
	public String getCorrectedFilenameOnDownloadSelection(String url) {
		if (this.mode == RuleMode.RULE_MODE_FILENAME_ON_DOWNLOAD_SELECTION) {
			String result = url;
			for (int i = 0; i < regexps.size(); i++) {
				result = regexps.get(i).doURLReplace(result, null);
				logger.debug(url + " -> Filename on Download Selection Replace done -> Step " + i + " -> Result: " + result);
			}
			return result;
		}
		return "";
	}
}

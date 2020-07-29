package ch.supertomcat.bh.rules;

import org.jdom2.Element;

import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.rules.xml.FilenameMode;
import ch.supertomcat.bh.rules.xml.FilenamePipeline;

/**
 * RulePipeline
 */
public class RulePipelineFilename extends RulePipeline<FilenamePipeline> {
	/**
	 * Constructor
	 */
	public RulePipelineFilename() {
		super(new FilenamePipeline());
		definition.setMode(FilenameMode.CONTAINER_URL_FILENAME_PART);
	}

	/**
	 * Constructor
	 * 
	 * @param definition Definition
	 */
	public RulePipelineFilename(FilenamePipeline definition) {
		super(definition);
	}

	@Override
	public Element getXmlElement() {
		Element e = super.getXmlElement();
		e.setAttribute("mode", "2");
		e.setAttribute("filenamemode", String.valueOf(definition.getMode().ordinal()));
		return e;
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
		String result;
		boolean bSourcecode = false;
		switch (definition.getMode()) {
			case THUMBNAIL_URL_FILENAME_PART:
			case THUMBNAIL_URL:
				result = thumbURL;
				break;
			case CONTAINER_PAGE_SOURCECODE:
			case FIRST_CONTAINER_PAGE_SOURCECODE:
			case LAST_CONTAINER_PAGE_SOURCECODE:
				result = htmlcode;
				bSourcecode = true;
				break;
			case CONTAINER_URL_FILENAME_PART:
			case CONTAINER_URL:
			case LAST_CONTAINER_URL_FILENAME_PART:
			case LAST_CONTAINER_URL:
			case DOWNLOAD_URL:
			case DOWNLOAD_URL_FILENAME_PART:
			default:
				result = url;
				break;
		}

		if (!bSourcecode) {
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
}

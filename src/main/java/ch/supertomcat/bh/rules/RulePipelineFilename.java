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

		String result = url;
		boolean bSourcecode = false;
		if ((definition.getMode() == FilenameMode.THUMBNAIL_URL_FILENAME_PART) || (definition.getMode() == FilenameMode.THUMBNAIL_URL)) {
			result = thumbURL;
		} else if (definition.getMode() == FilenameMode.CONTAINER_PAGE_SOURCECODE) {
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
}

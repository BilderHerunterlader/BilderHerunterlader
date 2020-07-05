package ch.supertomcat.bh.rules;

import org.jdom2.Element;

import ch.supertomcat.bh.rules.xml.FilenameDownloadSelectionMode;
import ch.supertomcat.bh.rules.xml.FilenameDownloadSelectionPipeline;

/**
 * RulePipeline
 */
public class RulePipelineFilenameDownloadSelection extends RulePipeline<FilenameDownloadSelectionPipeline> {
	/**
	 * Constructor
	 */
	public RulePipelineFilenameDownloadSelection() {
		super(new FilenameDownloadSelectionPipeline());
		definition.setMode(FilenameDownloadSelectionMode.CONTAINER_URL_FILENAME_PART);
	}

	/**
	 * Constructor
	 * 
	 * @param definition Definition
	 */
	public RulePipelineFilenameDownloadSelection(FilenameDownloadSelectionPipeline definition) {
		super(definition);
	}

	@Override
	public Element getXmlElement() {
		Element e = super.getXmlElement();
		e.setAttribute("mode", "3");
		e.setAttribute("filenameDownloadSelectionMode", String.valueOf(definition.getMode().ordinal()));
		return e;
	}

	/**
	 * Returns the filename after replacement
	 * 
	 * @param url URL
	 * @return Filename
	 */
	public String getCorrectedFilenameOnDownloadSelection(String url) {
		String result = url;
		for (int i = 0; i < regexps.size(); i++) {
			result = regexps.get(i).doURLReplace(result, null);
			logger.debug(url + " -> Filename on Download Selection Replace done -> Step " + i + " -> Result: " + result);
		}
		return result;
	}
}
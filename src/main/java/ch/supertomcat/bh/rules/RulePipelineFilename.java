package ch.supertomcat.bh.rules;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.jdom2.Element;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.rules.trace.RuleTraceInfo;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoFilename;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoFilenameReplace;
import ch.supertomcat.bh.rules.trace.RuleTraceInfoFilenameSearch;
import ch.supertomcat.bh.rules.xml.FilenameMode;
import ch.supertomcat.bh.rules.xml.FilenamePipeline;
import ch.supertomcat.supertomcatutils.html.HTMLUtil;

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
	 * @param containerURL Container URL
	 * @param lastContainerURL Last Container URL
	 * @param downloadURL Download URL
	 * @param thumbURL Thumbnail-URL
	 * @param htmlCodeFromFirstURL Container-Page-Sourcecode from First URL
	 * @param htmlCodeFirst First Container-Page-Sourcecode
	 * @param htmlCodeLast Last Container-Page-Sourcecode
	 * @param htmlCodeDownloadSupplier Supplier for downloading the HTML Code if necessary
	 * @param pic Pic
	 * @param upo URLParseObject
	 * @param ruleTraceInfo Rule Trace Info or null
	 * @return Filename or null if correction could not be done
	 * @throws HostException
	 */
	public String getCorrectedFilename(String containerURL, String lastContainerURL, String thumbURL, String downloadURL, RuleHtmlCode htmlCodeFromFirstURL, RuleHtmlCode htmlCodeFirst,
			RuleHtmlCode htmlCodeLast, RuleDownloadContainerPageSupplier<RuleHtmlCode> htmlCodeDownloadSupplier, Pic pic, URLParseObject upo, RuleTraceInfo ruleTraceInfo) throws HostException {
		boolean trace = ruleTraceInfo != null;
		String result;
		RuleHtmlCode ruleHtmlCode = null;
		boolean filenamePartOnly = false;
		switch (definition.getMode()) {
			case THUMBNAIL_URL_FILENAME_PART:
				result = thumbURL;
				filenamePartOnly = true;
				break;
			case THUMBNAIL_URL:
				result = thumbURL;
				break;
			case FIRST_CONTAINER_PAGE_SOURCECODE:
				ruleHtmlCode = htmlCodeFromFirstURL;
				result = ruleHtmlCode.getHtmlCode();
				break;
			case CONTAINER_PAGE_SOURCECODE:
				ruleHtmlCode = htmlCodeFirst;
				result = ruleHtmlCode.getHtmlCode();
				break;
			case LAST_CONTAINER_PAGE_SOURCECODE:
				ruleHtmlCode = htmlCodeLast;
				result = ruleHtmlCode.getHtmlCode();
				break;
			case LAST_CONTAINER_URL_FILENAME_PART:
				result = lastContainerURL;
				filenamePartOnly = true;
				break;
			case LAST_CONTAINER_URL:
				result = lastContainerURL;
				break;
			case DOWNLOAD_URL_FILENAME_PART:
				result = downloadURL;
				filenamePartOnly = true;
				break;
			case DOWNLOAD_URL:
				result = downloadURL;
				break;
			case CONTAINER_URL_FILENAME_PART:
				result = containerURL;
				filenamePartOnly = true;
				break;
			case CONTAINER_URL:
			default:
				result = containerURL;
				break;
		}

		RuleTraceInfoFilename traceInfo = null;

		if (ruleHtmlCode == null) {
			if (filenamePartOnly) {
				String filenamePart = RuleUtil.getFilenamePart(result);
				if (filenamePart.isEmpty()) {
					return null;
				}
				result = filenamePart;
			}

			if (trace) {
				traceInfo = new RuleTraceInfoFilename(containerURL, result);
				ruleTraceInfo.setFilenameTraceInfo(traceInfo);
			}

			for (int i = 0; i < regexps.size(); i++) {
				result = regexps.get(i).doURLReplace(result, pic);
				logger.debug("{} -> Filename Replace done -> Step {} -> Result: {}", containerURL, i, result);
				if (traceInfo != null) {
					traceInfo.addStep(new RuleTraceInfoFilenameReplace(i, 0, result));
				}
			}
		} else {
			if (!ruleHtmlCode.isAvailable()) {
				ruleHtmlCode = htmlCodeDownloadSupplier.downloadContainerPage();
			}

			String htmlCodeSourceURL = ruleHtmlCode.getUrl();
			String htmlCode = ruleHtmlCode.getHtmlCode();
			result = htmlCode;
			if (trace) {
				upo.addInfo("PageSourceCodeFilename", ruleHtmlCode);
				traceInfo = new RuleTraceInfoFilename(htmlCodeSourceURL, htmlCode);
				ruleTraceInfo.setFilenameTraceInfo(traceInfo);
			}

			int start = 0;
			for (int i = 0; i < regexps.size(); i++) {
				if (i < (regexps.size() - 1)) {
					int pos = regexps.get(i).doPageSourcecodeSearch(htmlCode, start);
					if (pos >= 0) {
						logger.debug("{} -> Filename Search done -> Step {} -> Pattern found at: {}", htmlCodeSourceURL, i, pos);
					} else {
						logger.debug("{} -> Filename Search done -> Step {} -> Pattern not found!", htmlCodeSourceURL, i);
					}
					if (traceInfo != null) {
						traceInfo.addStep(new RuleTraceInfoFilenameSearch(i, start, pos));
					}
					start = pos;
				} else {
					result = regexps.get(i).doPageSourcecodeReplace(htmlCode, start, htmlCodeSourceURL, pic);
					logger.debug("{} -> Filename Replace done -> Step {} -> Result: {}", htmlCodeSourceURL, i, result);
					if (traceInfo != null) {
						traceInfo.addStep(new RuleTraceInfoFilenameReplace(i, start, result));
					}
				}
			}

			/*
			 * TODO When the content is provided as String by the httpclient library it already determines the encoding. So the conversion here is then not
			 * always working correctly, because the bytes are used with default platform encoding and with the encoding that they were read. So we would have
			 * to know here which encoding was used to read the content.
			 * TODO This conversion should probably an option of the pipeline
			 */
			String encoding = HTMLUtil.getEncodingFromSourceCode(htmlCode);
			if (!encoding.isEmpty() && isEncodingAvailable(encoding)) {
				try {
					result = new String(result.getBytes(), encoding);
				} catch (UnsupportedEncodingException e) {
					logger.error("Encoding is not supported: {}", encoding, e);
				}
			}
		}
		return result;
	}

	/**
	 * @param encoding Encoding
	 * @return Available
	 */
	private boolean isEncodingAvailable(String encoding) {
		return Charset.availableCharsets().containsKey(encoding);
	}
}

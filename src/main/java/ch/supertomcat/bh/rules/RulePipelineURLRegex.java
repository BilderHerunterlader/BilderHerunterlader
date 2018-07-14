package ch.supertomcat.bh.rules;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostImageUrlNotFoundException;
import ch.supertomcat.bh.hoster.HostRules;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.supertomcattools.guitools.Localization;

/**
 * RulePipeline
 */
public class RulePipelineURLRegex extends RulePipeline {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(RulePipelineURLRegex.class);

	/**
	 * URL-Mode (Only used when mode is 0)
	 */
	private int urlMode = RULEPIPELINE_MODE_CONTAINER_URL;

	/**
	 * Amount of milliseconds to wait before getURL is executed
	 */
	private int waitBeforeExecute = 0;

	/**
	 * Flag if the result should be decoded (application/x-www-form-urlencoded)
	 */
	private boolean urlDecodeResult = false;

	/**
	 * Flag if cookies should be sent
	 */
	private boolean sendCookies = true;

	/**
	 * Constructor
	 * 
	 * @param mode Rule-Mode
	 */
	public RulePipelineURLRegex(int mode) {
		super(mode);
	}

	/**
	 * Constructor
	 * 
	 * @param e Element
	 */
	public RulePipelineURLRegex(Element e) {
		super(e);
		if (this.mode == Rule.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL) {
			String strURLMode = e.getAttributeValue("urlmode");
			try {
				this.setURLMode(Integer.parseInt(strURLMode));
			} catch (NumberFormatException ex) {
				logger.error("Could not parse urlmode: {}", strURLMode, ex);
			}
		}

		if (this.mode == Rule.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL || this.mode == Rule.RULE_MODE_CONTAINER_PAGE_SOURCECODE) {
			String strWaitBeforeExecute = e.getAttributeValue("waitBeforeExecute");
			if (strWaitBeforeExecute != null) {
				try {
					this.waitBeforeExecute = Integer.parseInt(e.getAttributeValue("waitBeforeExecute"));
				} catch (NumberFormatException ex) {
					logger.error("Could not parse waitBeforeExecute: {}", strWaitBeforeExecute, ex);
				}
			}
		}

		String strUrlDecodeResult = e.getAttributeValue("urlDecodeResult");
		if (strUrlDecodeResult != null) {
			urlDecodeResult = Boolean.parseBoolean(strUrlDecodeResult);
		}

		String strSendCookies = e.getAttributeValue("sendCookies");
		if (strSendCookies != null) {
			sendCookies = Boolean.parseBoolean(strSendCookies);
		}
	}

	/**
	 * Returns the Element for creating the XML-File
	 * 
	 * @return Element
	 */
	@Override
	public Element getXmlElement() {
		Element e = super.getXmlElement();
		if (this.mode == Rule.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL) {
			e.setAttribute("urlmode", String.valueOf(this.urlMode));
			e.setAttribute("waitBeforeExecute", String.valueOf(this.waitBeforeExecute));
		} else if (this.mode == Rule.RULE_MODE_CONTAINER_PAGE_SOURCECODE) {
			e.setAttribute("waitBeforeExecute", String.valueOf(this.waitBeforeExecute));
		}
		e.setAttribute("urlDecodeResult", String.valueOf(urlDecodeResult));
		return e;
	}

	/**
	 * Returns the UrlMode
	 * 
	 * @return UrlMode
	 */
	@Override
	public int getURLMode() {
		return urlMode;
	}

	/**
	 * Sets the UrlMode
	 * 
	 * @param urlMode UrlMode
	 */
	@Override
	public void setURLMode(int urlMode) {
		if ((urlMode == RULEPIPELINE_MODE_CONTAINER_URL) || (urlMode == RULEPIPELINE_MODE_THUMBNAIL_URL)) {
			this.urlMode = urlMode;
		}
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
	@Override
	public String getURL(String url, String thumbURL, String htmlcode, Pic pic) throws HostException {
		String retval = "";
		if (this.mode == Rule.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL) {
			String result = url;
			if (this.urlMode == RULEPIPELINE_MODE_THUMBNAIL_URL) {
				result = thumbURL;
			}
			for (int i = 0; i < regexps.size(); i++) {
				result = regexps.get(i).doURLReplace(result, pic);
				logger.debug(url + " -> Replace done -> Step " + i + " -> Result: " + result);
			}
			retval = result;
		} else {
			String result = "";
			int start = 0;
			for (int i = 0; i < regexps.size(); i++) {
				if (i < (regexps.size() - 1)) {
					start = regexps.get(i).doPageSourcecodeSearch(htmlcode, start);
					if (start > 0) {
						logger.debug(url + " -> Search done -> Step " + i + " -> Pattern found at: " + start);
					} else {
						logger.debug(url + " -> Search done -> Step " + i + " -> Pattern not found!");
					}
				} else {
					result = regexps.get(i).doPageSourcecodeReplace(htmlcode, start, url, pic);
					logger.debug(url + " -> Replace done -> Step " + i + " -> Result: " + result);
				}
			}
			retval = result;
		}
		if (retval.equals("")) {
			if (this.urlMode == RULEPIPELINE_MODE_THUMBNAIL_URL && thumbURL.isEmpty()) {
				throw new HostImageUrlNotFoundException(HostRules.NAME + ": " + Localization.getString("ErrorImageURLThumbMissing"));
			} else {
				throw new HostImageUrlNotFoundException(HostRules.NAME + ": " + Localization.getString("ErrorImageURL"));
			}
		}
		return retval;
	}

	/**
	 * Returns the waitBeforeExecute
	 * 
	 * @return waitBeforeExecute
	 */
	@Override
	public int getWaitBeforeExecute() {
		return waitBeforeExecute;
	}

	/**
	 * Sets the waitBeforeExecute
	 * 
	 * @param waitBeforeExecute waitBeforeExecute
	 */
	@Override
	public void setWaitBeforeExecute(int waitBeforeExecute) {
		this.waitBeforeExecute = waitBeforeExecute;
	}

	@Override
	public boolean isUrlDecodeResult() {
		return urlDecodeResult;
	}

	@Override
	public void setUrlDecodeResult(boolean urlDecodeResult) {
		this.urlDecodeResult = urlDecodeResult;
	}

	@Override
	public boolean isSendCookies() {
		return sendCookies;
	}

	@Override
	public void setSendCookies(boolean sendCookies) {
		this.sendCookies = sendCookies;
	}
}

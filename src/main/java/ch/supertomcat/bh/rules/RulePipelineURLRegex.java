package ch.supertomcat.bh.rules;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostImageUrlNotFoundException;
import ch.supertomcat.bh.hoster.hostimpl.HostRules;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.supertomcattools.guitools.Localization;

/**
 * RulePipeline
 */
public class RulePipelineURLRegex extends RulePipeline {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * URL-Mode (Only used when mode is 0)
	 */
	private RuleURLMode urlMode = RuleURLMode.RULEPIPELINE_MODE_CONTAINER_URL;

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
	public RulePipelineURLRegex(RuleMode mode) {
		super(mode);
	}

	/**
	 * Constructor
	 * 
	 * @param e Element
	 */
	public RulePipelineURLRegex(Element e) {
		super(e);
		if (this.mode == RuleMode.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL) {
			String strURLMode = e.getAttributeValue("urlmode");
			try {
				this.setURLMode(RuleURLMode.getByValue(Integer.parseInt(strURLMode)));
			} catch (NumberFormatException ex) {
				logger.error("Could not parse urlmode: {}", strURLMode, ex);
			}
		}

		if (this.mode == RuleMode.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL || this.mode == RuleMode.RULE_MODE_CONTAINER_PAGE_SOURCECODE) {
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

	@Override
	public Element getXmlElement() {
		Element e = super.getXmlElement();
		if (this.mode == RuleMode.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL) {
			e.setAttribute("urlmode", String.valueOf(this.urlMode.getValue()));
			e.setAttribute("waitBeforeExecute", String.valueOf(this.waitBeforeExecute));
		} else if (this.mode == RuleMode.RULE_MODE_CONTAINER_PAGE_SOURCECODE) {
			e.setAttribute("waitBeforeExecute", String.valueOf(this.waitBeforeExecute));
		}
		e.setAttribute("urlDecodeResult", String.valueOf(urlDecodeResult));
		return e;
	}

	@Override
	public RuleURLMode getURLMode() {
		return urlMode;
	}

	@Override
	public void setURLMode(RuleURLMode urlMode) {
		this.urlMode = urlMode;
	}

	@Override
	public String getURL(String url, String thumbURL, String htmlcode, Pic pic) throws HostException {
		String retval = "";
		if (this.mode == RuleMode.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL) {
			String result = url;
			if (this.urlMode == RuleURLMode.RULEPIPELINE_MODE_THUMBNAIL_URL) {
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
		if (retval.isEmpty()) {
			if (this.urlMode == RuleURLMode.RULEPIPELINE_MODE_THUMBNAIL_URL && thumbURL.isEmpty()) {
				throw new HostImageUrlNotFoundException(HostRules.NAME + ": " + Localization.getString("ErrorImageURLThumbMissing"));
			} else {
				throw new HostImageUrlNotFoundException(HostRules.NAME + ": " + Localization.getString("ErrorImageURL"));
			}
		}
		return retval;
	}

	@Override
	public int getWaitBeforeExecute() {
		return waitBeforeExecute;
	}

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

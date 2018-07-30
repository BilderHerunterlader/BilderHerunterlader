package ch.supertomcat.bh.hoster.linkextract;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.supertomcat.bh.pic.URL;

/**
 * Utility class for link extraction
 */
public final class ExtractTools {
	private static final Pattern PATTERN_DOMAIN = Pattern.compile("^(https?://(.*?))/.*$");

	private static final Pattern PATTERN_DOMAIN_AND_PATH = Pattern.compile("^(https?://(.*?/){1,}).*$");

	/**
	 * Constructor
	 */
	private ExtractTools() {
	}

	/**
	 * Returns the value of an attribute from a node or null if attribute not found
	 * 
	 * @param node Node
	 * @param attribute Attribute
	 * @return Attribute value
	 */
	public static String getAttributeValueFromNode(Node node, String attribute) {
		String val = "";
		NamedNodeMap nnm = node.getAttributes();

		boolean found = false;

		for (int o = 0; o < nnm.getLength(); o++) {
			Node no = nnm.item(o);
			if (no.getNodeName().equalsIgnoreCase(attribute)) {
				val = no.getNodeValue();
				found = true;
				break;
			}
		}

		if (!found) {
			return null;
		}
		return val;
	}

	/**
	 * Returns the text value of the node or null if not available
	 * 
	 * @param node Node
	 * @return Text Value
	 */
	public static String getTextValueFromNode(Node node) {
		NodeList childNodes = node.getChildNodes();
		if (childNodes.getLength() == 0) {
			return null;
		}

		return childNodes.item(0).getNodeValue();
	}

	/**
	 * Converts a relative URL to an absolute URL
	 * 
	 * @param absoluteURL Absolute-URL
	 * @param urlToConvert URL to convert
	 * @return Converted URL
	 */
	public static URL convertURLFromRelativeToAbsolute(String absoluteURL, URL urlToConvert) {
		String strCurrentURL = urlToConvert.getURL();
		Matcher matcher;
		if (strCurrentURL.startsWith("/")) {
			matcher = PATTERN_DOMAIN.matcher(absoluteURL);
		} else {
			matcher = PATTERN_DOMAIN_AND_PATH.matcher(absoluteURL);
		}

		strCurrentURL = matcher.replaceAll("$1") + strCurrentURL;

		URL changedURL = new URL(strCurrentURL, urlToConvert.getThumb());
		changedURL.setThreadURL(urlToConvert.getThreadURL());
		changedURL.setFilenameCorrected(urlToConvert.getFilenameCorrected());
		changedURL.setTargetPath(urlToConvert.getTargetPath());
		return changedURL;
	}

	/**
	 * @param containerURL Container-URL
	 * @param whitelists Whitelists
	 * @return ILinkExtractFilter
	 */
	public static ILinkExtractFilter getFilterForContainerURL(String containerURL, List<ExtractConfigWhitelist> whitelists) {
		for (ExtractConfigWhitelist whitelist : whitelists) {
			if (whitelist.isContainerURL(containerURL)) {
				return whitelist.getExtractFilter();
			}
		}
		return null;
	}
}

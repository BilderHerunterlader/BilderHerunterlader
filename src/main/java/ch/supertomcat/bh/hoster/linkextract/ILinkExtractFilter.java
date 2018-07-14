package ch.supertomcat.bh.hoster.linkextract;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ch.supertomcat.bh.pic.URL;


/**
 * Class which can be used to filter out links on LinkExtract
 */
public interface ILinkExtractFilter {
	/**
	 * Returns true if the link should be added to the extracted Links
	 * @param nodeURL Node of URL
	 * @param nodeRoot Node of the root object
	 * @param url URL
	 * @param containerURL Container-URL
	 * @return true if the link should be added to the extracted Links
	 */
	public boolean isLinkAccepted(Node nodeURL, Document nodeRoot, URL url, String containerURL);
}

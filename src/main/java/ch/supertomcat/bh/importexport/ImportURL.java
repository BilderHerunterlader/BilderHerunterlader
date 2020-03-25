package ch.supertomcat.bh.importexport;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.clipboard.ClipboardObserver;
import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;

/**
 * Utility Class for import
 */
public class ImportURL {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Link List Importer
	 */
	private final ImportLinkList linkListImporter;

	/**
	 * HTML Importer
	 */
	private final ImportHTML htmlImporter;

	/**
	 * Parent Component
	 */
	private final Component parentComponent;

	/**
	 * Constructor
	 * 
	 * @param parentComponent Parent Component
	 * @param mainWindowAccess Main Window Access
	 * @param logManager Log Manager
	 * @param queueManager Queue Manager
	 * @param keywordManager Keyword Manager
	 * @param clipboardObserver Clipboard Observer
	 */
	public ImportURL(Component parentComponent, MainWindowAccess mainWindowAccess, LogManager logManager, QueueManager queueManager, KeywordManager keywordManager,
			ClipboardObserver clipboardObserver) {
		this.parentComponent = parentComponent;
		linkListImporter = new ImportLinkList(parentComponent, mainWindowAccess, logManager, queueManager, keywordManager, clipboardObserver);
		htmlImporter = new ImportHTML(parentComponent, mainWindowAccess, logManager, queueManager, keywordManager, clipboardObserver);
	}

	/**
	 * Uses ImportLinkList or ImportHTML depending on the content-type
	 * 
	 * @param url URL
	 * @param referrer Referrer
	 * @param embeddedImages Embedded Images
	 */
	public void importURL(String url, String referrer, boolean embeddedImages) {
		String cookies = CookieManager.getCookies(url);
		url = HTTPUtil.encodeURL(url);

		/*
		 * A user reported to me, that when BH is running for a while and then
		 * by this method a new URL is imported, then it doesen't returned any
		 * URLs. The URL was from a Bulletin-Board. And this only appeared when
		 * cookies from IE were used. But the cookies were read corretly by BH, and
		 * also sent (I didn't checked this really). However, the Bulletin-Board did
		 * not accept the cookies or didn't get them or something else ;-)
		 * I was able to fix this problem by not using the MultiThreadedHttpConnectionManager.
		 * So, maybe there is a bug in Jakarta-HttpClient...
		 */
		HttpGet method = null;
		try (CloseableHttpClient client = ProxyManager.instance().getNonMultithreadedHTTPClient()) {
			// Open connection
			method = new HttpGet(url);
			method.setHeader("User-Agent", SettingsManager.instance().getUserAgent());
			if (cookies.length() > 0) {
				method.setHeader("Cookie", cookies);
			}
			try (CloseableHttpResponse response = client.execute(method)) {
				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode < 200 && statusCode >= 400) {
					method.abort();
					JOptionPane.showMessageDialog(parentComponent, "HTTP-Error:" + statusCode, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// Get the InputStream
				try (InputStream in = response.getEntity().getContent()) {
					if ("text/plain".equals(response.getFirstHeader("Content-Type").getValue())) {
						logger.debug("PlainText detected: Using ImportLinkList");
						linkListImporter.read(new BufferedReader(new InputStreamReader(in)));
					} else {
						logger.debug("HTML detected: Using ImportHTML");
						htmlImporter.importHTML(url, referrer, embeddedImages, in, response, method);
					}
				}
			}
		} catch (IOException e) {
			logger.error("Could not import links from URL: {}", url, e);
		} finally {
			if (method != null) {
				method.abort();
			}
		}
	}
}

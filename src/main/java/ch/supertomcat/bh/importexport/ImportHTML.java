package ch.supertomcat.bh.importexport;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.StatusLine;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import ch.supertomcat.bh.clipboard.ClipboardObserver;
import ch.supertomcat.bh.cookies.CookieManager;
import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.gui.adder.AdderWindow;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.hoster.linkextract.ImageExtract;
import ch.supertomcat.bh.hoster.linkextract.LinkExtract;
import ch.supertomcat.bh.importexport.base.AdderImportBase;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.pic.URLList;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;

/**
 * Class for importing URLs from HTML
 */
public class ImportHTML extends AdderImportBase {
	/**
	 * Cookie Manager
	 */
	private final CookieManager cookieManager;

	/**
	 * Constructor
	 * 
	 * @param parentComponent Parent Component
	 * @param mainWindowAccess Main Window Access
	 * @param logManager Log Manager
	 * @param queueManager Queue Manager
	 * @param keywordManager Keyword Manager
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param hostManager Host Manager
	 * @param cookieManager Cookie Manager
	 * @param clipboardObserver Clipboard Observer
	 */
	public ImportHTML(Component parentComponent, MainWindowAccess mainWindowAccess, LogManager logManager, QueueManager queueManager, KeywordManager keywordManager, ProxyManager proxyManager,
			SettingsManager settingsManager, HostManager hostManager, CookieManager cookieManager, ClipboardObserver clipboardObserver) {
		super(parentComponent, mainWindowAccess, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver);
		this.cookieManager = cookieManager;
	}

	/**
	 * 
	 */
	public void importHTML() {
		File file = getTextFileFromFileChooserDialog(".+\\.((s|x|j|)htm(l|)|php([0-5]|)|(a|d|j)sp|cfm|ssi|(f|)cg(i|)|pl|htx)", "Supported Files", false);
		if (file != null) {
			ProgressObserver pg = new ProgressObserver();
			mainWindowAccess.addProgressObserver(pg);
			pg.progressChanged(-1, -1, -1);
			pg.progressChanged(Localization.getString("ImportingHTMLFile") + "...");
			setLastUsedImportPath(file);

			try (FileInputStream in = new FileInputStream(file)) {
				// Parse the inputstream by tidy
				Tidy tidy = new Tidy();
				tidy.setShowWarnings(false);
				tidy.setShowErrors(0);
				tidy.setQuiet(true);
				tidy.setInputEncoding("UTF-8");
				Document node = tidy.parseDOM(in, null);

				// Get the page-title
				String title = getPageTitle(node);

				// Add the urls
				ArrayList<URL> urls = new ArrayList<>();
				LinkExtract.addLinks(file.getAbsolutePath(), null, urls, node);

				file = null;

				if (urls.isEmpty()) {
					return;
				}

				// Open the Dialog
				AdderWindow adderpnl = new AdderWindow(parentComponent, new URLList(title, "", urls), logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver);
				adderpnl.init(); // We need to do this!
				adderpnl = null;

				mainWindowAccess.setMessage(Localization.getString("HTMLFileImported"));
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				mainWindowAccess.setMessage(Localization.getString("HTMLFileImportFailed"));
			} finally {
				mainWindowAccess.removeProgressObserver(pg);
			}
		}
	}

	/**
	 * @param url URL
	 * @param referrer Referrer
	 * @param embeddedImages Embedded Images
	 */
	public void importHTML(String url, String referrer, boolean embeddedImages) {
		String encodedURL = HTTPUtil.encodeURL(url);

		try (CloseableHttpClient client = proxyManager.getHTTPClient()) {
			// Open connection
			HttpGet method = new HttpGet(encodedURL);
			method.setHeader(HttpHeaders.USER_AGENT, settingsManager.getUserAgent());

			CookieStore cookieStore = cookieManager.getCookieStore();
			HttpClientContext context = ContextBuilder.create().useCookieStore(cookieStore).build();
			cookieManager.fillCookies(url, cookieStore);

			client.execute(method, context, response -> {
				StatusLine statusLine = new StatusLine(response);
				int statusCode = statusLine.getStatusCode();

				if (statusCode != 200) {
					JOptionPane.showMessageDialog(parentComponent, "HTTP-Error:" + statusCode, "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}

				// Get the InputStream
				try (@SuppressWarnings("resource")
				InputStream in = response.getEntity().getContent()) {
					importHTML(encodedURL, referrer, embeddedImages, in, response, method);
				}
				return null;
			});
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @param url URL
	 * @param referrer Referrer
	 * @param embeddedImages Embedded Images
	 * @param in InputStream
	 * @param response Response
	 * @param method Method
	 */
	public void importHTML(String url, String referrer, boolean embeddedImages, InputStream in, HttpResponse response, HttpGet method) {
		// Parse the stream by tidy
		Tidy tidy = new Tidy();
		tidy.setShowWarnings(false);
		tidy.setShowErrors(0);
		tidy.setQuiet(true);
		tidy.setInputEncoding("UTF-8");
		Document node = tidy.parseDOM(in, null);

		// Get the page-title
		String title = getPageTitle(node);

		List<URL> urls = new ArrayList<>();

		// Add the urls
		if (embeddedImages) {
			ImageExtract.addLinks(url, null, urls, node);
		} else {
			LinkExtract.addLinks(url, null, urls, node);
		}

		if (urls.isEmpty()) {
			return;
		}

		// Open the Dialog
		AdderWindow adderpnl = new AdderWindow(parentComponent, new URLList(title, referrer, urls), logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver);
		adderpnl.init(); // We need to do this!
	}

	/**
	 * @param in InputStream
	 * @param referrer Referrer
	 * @param title Title
	 */
	public void importHTML(InputStream in, String referrer, String title) {
		List<URL> urls = new ArrayList<>();

		// Add the urls
		LinkExtract.addLinks("", null, urls, in);

		if (urls.isEmpty()) {
			return;
		}

		// Open the Dialog
		AdderWindow adderpnl = new AdderWindow(parentComponent, new URLList(title, referrer, urls), logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver);
		adderpnl.init(); // We need to do this!
	}

	/**
	 * Get the page-title from the document
	 * 
	 * @param documentNode Document-Node
	 * @return Page-title
	 */
	private String getPageTitle(Document documentNode) {
		// Get the page-title
		String title = "";
		// Get the title-Element
		NodeList nlt = documentNode.getElementsByTagName("title");
		for (int i = 0; i < nlt.getLength(); i++) {
			Node n = nlt.item(i);
			// Get the childnodes of the title
			NodeList lll = n.getChildNodes();
			for (int x = 0; x < lll.getLength(); x++) {
				Node nx = lll.item(x);
				// Get the title
				title = nx.getNodeValue();
			}
		}
		return title;
	}
}

package ch.supertomcat.bh.importexport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import ch.supertomcat.bh.gui.Main;
import ch.supertomcat.bh.gui.adder.AdderPanel;
import ch.supertomcat.bh.hoster.linkextract.ImageExtract;
import ch.supertomcat.bh.hoster.linkextract.LinkExtract;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.pic.URLList;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.fileiotools.FileTool;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.progressmonitor.ProgressObserver;
import ch.supertomcat.supertomcattools.httptools.HTTPTool;

/**
 * Class for importing URLs from HTML
 */
public abstract class ImportHTML {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(ImportHTML.class);

	/**
	 * 
	 */
	public static void importHTML() {
		File file = Import.getTextFileFromFileChooserDialog(".+\\.((s|x|j|)htm(l|)|php([0-5]|)|(a|d|j)sp|cfm|ssi|(f|)cg(i|)|pl|htx)", "Supported Files", false);
		if (file != null) {
			ProgressObserver pg = new ProgressObserver();
			Main.instance().addProgressObserver(pg);
			pg.progressChanged(-1, -1, -1);
			pg.progressChanged(Localization.getString("ImportingHTMLFile") + "...");
			SettingsManager.instance().setLastUsedImportDialogPath(FileTool.getPathFromFile(file));

			try (FileInputStream in = new FileInputStream(file)) {
				// Parse the inputstream by tidy
				Tidy tidy = new Tidy();
				tidy.setShowWarnings(false);
				tidy.setShowErrors(0);
				tidy.setQuiet(true);
				tidy.setInputEncoding("UTF-8");
				Document node = tidy.parseDOM(in, null);

				in.close();

				// Get the page-title
				String title = getPageTitle(node);

				// Add the urls
				ArrayList<URL> urls = new ArrayList<>();
				LinkExtract.addLinks(file.getAbsolutePath(), null, urls, node);

				file = null;

				if (urls.size() < 1) {
					return;
				}

				// Open the Dialog
				AdderPanel adderpnl = new AdderPanel(new URLList(title, "", urls));
				adderpnl.init(); // We need to do this!
				adderpnl = null;

				Main.instance().setMessage(Localization.getString("HTMLFileImported"));
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage(), e);
				Main.instance().setMessage(Localization.getString("HTMLFileImportFailed"));
			} catch (MalformedURLException e) {
				logger.error(e.getMessage(), e);
				Main.instance().setMessage(Localization.getString("HTMLFileImportFailed"));
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				Main.instance().setMessage(Localization.getString("HTMLFileImportFailed"));
			} finally {
				Main.instance().removeProgressObserver(pg);
			}
		}
	}

	/**
	 * @param url URL
	 * @param referrer Referrer
	 * @param embeddedImages Embedded Images
	 */
	public static void importHTML(String url, String referrer, boolean embeddedImages) {
		String cookies = CookieManager.getCookies(url);
		url = HTTPTool.encodeURL(url);

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
			if (!cookies.isEmpty()) {
				method.setHeader("Cookie", cookies);
			}
			try (CloseableHttpResponse response = client.execute(method)) {
				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode != 200) {
					method.abort();
					JOptionPane.showMessageDialog(Main.instance(), "HTTP-Error:" + statusCode, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// Get the InputStream
				try (InputStream in = response.getEntity().getContent()) {
					importHTML(url, referrer, embeddedImages, in, response, method);
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (method != null) {
				method.abort();
			}
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
	public static void importHTML(String url, String referrer, boolean embeddedImages, InputStream in, HttpResponse response, HttpGet method) {
		ArrayList<URL> urls = new ArrayList<>();

		try {
			// Parse the stream by tidy
			Tidy tidy = new Tidy();
			tidy.setShowWarnings(false);
			tidy.setShowErrors(0);
			tidy.setQuiet(true);
			tidy.setInputEncoding("UTF-8");
			Document node = tidy.parseDOM(in, null);

			EntityUtils.consume(response.getEntity());

			// Get the page-title
			String title = getPageTitle(node);

			// Add the urls
			if (embeddedImages) {
				ImageExtract.addLinks(url, null, urls, node);
			} else {
				LinkExtract.addLinks(url, null, urls, node);
			}

			if (urls.size() < 1) {
				return;
			}

			// Open the Dialog
			AdderPanel adderpnl = new AdderPanel(new URLList(title, referrer, urls));
			adderpnl.init(); // We need to do this!
			adderpnl = null;
		} catch (MalformedURLException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			method.abort();
		}
	}

	/**
	 * @param in InputStream
	 * @param referrer Referrer
	 * @param title Title
	 */
	public static void importHTML(InputStream in, String referrer, String title) {
		ArrayList<URL> urls = new ArrayList<>();

		// Add the urls
		LinkExtract.addLinks("", null, urls, in);

		if (urls.size() < 1) {
			return;
		}

		// Open the Dialog
		AdderPanel adderpnl = new AdderPanel(new URLList(title, referrer, urls));
		adderpnl.init(); // We need to do this!
		adderpnl = null;
	}

	/**
	 * Get the page-title from the document
	 * 
	 * @param documentNode Document-Node
	 * @return Page-title
	 */
	private static String getPageTitle(Document documentNode) {
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

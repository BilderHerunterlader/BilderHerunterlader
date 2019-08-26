package ch.supertomcat.bh.importexport;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.Main;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.dialog.FileDialogUtil;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;

/**
 * 
 *
 */
public abstract class Import {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(Import.class);

	/**
	 * @param filterPattern Filter Pattern
	 * @param description Description
	 * @param save Save or Open
	 * @return Text-File
	 */
	public static File getTextFileFromFileChooserDialog(final String filterPattern, final String description, boolean save) {
		// Choose a file
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.getName().matches(filterPattern) || f.isDirectory();
			}

			@Override
			public String getDescription() {
				return description;
			}
		};
		if (save) {
			return FileDialogUtil.showFileSaveDialog(Main.instance(), SettingsManager.instance().getLastUsedImportDialogPath(), filter);
		} else {
			return FileDialogUtil.showFileOpenDialog(Main.instance(), SettingsManager.instance().getLastUsedImportDialogPath(), filter);
		}
	}

	/**
	 * Uses ImportLinkList or ImportHTML depending on the content-type
	 * 
	 * @param url URL
	 * @param referrer Referrer
	 * @param embeddedImages Embedded Images
	 */
	public static void importURL(String url, String referrer, boolean embeddedImages) {
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
					JOptionPane.showMessageDialog(Main.instance(), "HTTP-Error:" + statusCode, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// Get the InputStream
				try (InputStream in = response.getEntity().getContent()) {
					if ("text/plain".equals(response.getFirstHeader("Content-Type").getValue())) {
						logger.debug("PlainText detected: Using ImportLinkList");
						ImportLinkList.read(new BufferedReader(new InputStreamReader(in)));
					} else {
						logger.debug("HTML detected: Using ImportHTML");
						ImportHTML.importHTML(url, referrer, embeddedImages, in, response, method);
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

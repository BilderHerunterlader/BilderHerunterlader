package ch.supertomcat.bh.transmitter;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.clipboard.ClipboardObserver;
import ch.supertomcat.bh.cookies.CookieManager;
import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.gui.adder.AdderWindow;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.importexport.ImportLinkList;
import ch.supertomcat.bh.importexport.ImportURL;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.pic.URLList;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.SettingsManager;

/**
 * Helper class for Transmitter
 */
public class TransmitterHelper {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Parent Component
	 */
	private final Component parentComponent;

	/**
	 * Main Window Access
	 */
	private final MainWindowAccess mainWindowAccess;

	/**
	 * Queue Manager
	 */
	private final QueueManager queueManager;

	/**
	 * Log Manager
	 */
	private final LogManager logManager;

	/**
	 * Keyword Manager
	 */
	private final KeywordManager keywordManager;

	/**
	 * Proxy Manager
	 */
	private final ProxyManager proxyManager;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Host Manager
	 */
	private final HostManager hostManager;

	/**
	 * Clipboard Observer
	 */
	private final ClipboardObserver clipboardObserver;

	/**
	 * URL Importer
	 */
	private final ImportURL urlImporter;

	/**
	 * Constructor
	 * 
	 * @param parentComponent Parent Component
	 * @param mainWindowAccess Main Window Access
	 * @param queueManager Queue Manager
	 * @param logManager Log Manager
	 * @param keywordManager Keyword Manager
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param cookieManager Cookie Manager
	 * @param hostManager Host Manager
	 * @param clipboardObserver Clipboard Observer
	 */
	public TransmitterHelper(Component parentComponent, MainWindowAccess mainWindowAccess, QueueManager queueManager, LogManager logManager, KeywordManager keywordManager, ProxyManager proxyManager,
			SettingsManager settingsManager, CookieManager cookieManager, HostManager hostManager, ClipboardObserver clipboardObserver) {
		this.parentComponent = parentComponent;
		this.mainWindowAccess = mainWindowAccess;
		this.queueManager = queueManager;
		this.logManager = logManager;
		this.keywordManager = keywordManager;
		this.proxyManager = proxyManager;
		this.settingsManager = settingsManager;
		this.hostManager = hostManager;
		this.clipboardObserver = clipboardObserver;
		this.urlImporter = new ImportURL(parentComponent, mainWindowAccess, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, cookieManager, clipboardObserver);
	}

	/**
	 * Parse Transmitter Input and open Download-Selection-Dialog
	 * 
	 * @param in InputStream
	 * @param encoding Encoding
	 * @return True if successful, false otherwise
	 */
	public boolean parseTransmitterInput(InputStream in, Charset encoding) {
		/*
		 * There are 3 ways, how URLs could be transferred to BH.
		 * The first one is to send all the URLs by the stream (The Firefox-Extension does this)
		 * The second one is that the plugin/extension writes the URLs to a file and send
		 * the path to file by the stream (The IE-Plugin does this)
		 * The third one is that the plugin/extension sends only the URL which contains the URLs
		 * by the stream (The Opera-Plugin does this)
		 */
		try {
			logger.debug("Opening InputStream");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));

			String file = "";
			String url = "";
			String title = "";
			String referrer = "";
			List<URL> urls = new ArrayList<>();
			boolean fullList = false;
			boolean withThumbs = false;
			boolean eof = false;
			boolean imgs = false;

			String line = null;
			logger.debug("Reading InputStream");

			/*
			 * Read first line
			 * Must describe which information is provided:
			 * FULLLISTTHUMBS (Full Link List with Thumbs, Link and Thumb seperated by tab)
			 * FULLLIST (Full Link list without Thumbs)
			 * SOF (Only one Link to a page containing the Links which BH will read out)
			 */
			line = reader.readLine();
			logger.debug("Line read: {}", line);
			if (line.equals("FULLLISTTHUMBS")) {
				fullList = true;
				withThumbs = true;
				logger.debug("Recieving Full List with Thumbs");
			} else if (line.equals("FULLLIST")) {
				fullList = true;
				logger.debug("Recieving Full List");
			} else if (line.equals("SOF")) {
				logger.debug("SOF");
			} else {
				logger.error("First line did not match expected values: {}", line);
				return false;
			}

			/*
			 * Read next line in fullList Mode
			 * Line must be SOF
			 */
			if (fullList) {
				line = reader.readLine();
				logger.debug("Line read: {}", line);
				if (line.equals("SOF")) {
					logger.debug("SOF");
				} else {
					logger.error("Line should have been SOF, but was: {}", line);
					return false;
				}
			}

			/*
			 * Read rest of the lines
			 */
			if (fullList) {
				/*
				 * Read next line
				 * Must be BH{af2f0750-c598-4826-8e5f-bb98aab519a5}
				 */
				line = reader.readLine();
				logger.debug("Line read: {}", line);
				if (!line.startsWith("BH{af2f0750-c598-4826-8e5f-bb98aab519a5}")) {
					logger.error("Line did not match BH serial number: {}", line);
					return false;
				}

				/*
				 * Read title
				 */
				line = reader.readLine();
				logger.debug("Line read: {}", line);
				title = line;

				/*
				 * Read referrer url
				 */
				line = reader.readLine();
				logger.debug("Line read: {}", line);
				referrer = line;

				while ((line = reader.readLine()) != null) {
					logger.debug("Line read: {}", line);

					// Break the loop when recieving the EOF
					if (line.equals("EOF")) {
						eof = true;
						logger.debug("EOF");
						break;
					}

					URL urlToAdd = null;
					// If we recieving a full list
					if (withThumbs) {
						// If we recieving also thumbs
						int last = line.length();
						int seperator = line.indexOf("\t");
						if ((seperator > 0) && (seperator < last)) {
							urlToAdd = new URL(line.substring(0, seperator), line.substring(seperator + 1, last));
						} else {
							urlToAdd = new URL(line, "");
						}
					} else {
						urlToAdd = new URL(line, "");
					}
					urlToAdd.setThreadURL(referrer);
					urls.add(urlToAdd);
				}
			} else {
				if ((line = reader.readLine()) != null) {
					logger.debug("Line read: {}", line);
					if (line.startsWith("URL:")) {
						url = line.substring(line.indexOf(":") + 1);
						file = "";
					} else if (line.startsWith("IMG:")) {
						url = line.substring(line.indexOf(":") + 1);
						file = "";
						imgs = true;
					} else {
						file = line;
						url = "";
					}
					if ((line = reader.readLine()) != null) {
						logger.debug("Line read: {}", line);
						if (line.equals("EOF")) {
							logger.debug("EOF");
							eof = true;
						} else {
							logger.error("Line should have been EOF, but was: {}", line);
							return false;
						}
					}
				}
			}

			if (!fullList && !file.isEmpty() && eof) {
				// If we recieved only a path to a file, we must read it
				new ImportLinkList(parentComponent, mainWindowAccess, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver)
						.importLinkList(file, true);
				logger.info("Handled Connection successfully");
			} else if (!fullList && !url.isEmpty() && eof) {
				// If we recieved only a URL which contains all the URLs, we download the URL
				if (url.matches("^https?://.*/?.*")) {
					logger.debug("Recieved URL to Download: {}", url);
					urlImporter.importURL(url, url, imgs);
				} else {
					logger.error("URL did not match URL-Pattern: {}", url);
				}
				logger.info("Handled Connection successfully");
			} else if (fullList && !urls.isEmpty() && eof) {
				// If we recieved all the URLs
				logger.debug("Recieved {} Links", urls.size());
				// Open the Download-Selection-Dialog
				AdderWindow adderpnl = new AdderWindow(parentComponent, new URLList(title, referrer, urls), logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver);
				adderpnl.init();
				logger.info("Handled Connection successfully");
			}
			return true;
		} catch (IOException e) {
			logger.error("Could not handle connection", e);
			return false;
		}
	}
}

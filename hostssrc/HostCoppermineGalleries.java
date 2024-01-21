import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.message.StatusLine;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostHttpIOException;
import ch.supertomcat.bh.exceptions.HostIOException;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.IHosterURLAdder;
import ch.supertomcat.bh.hoster.hosteroptions.IHosterOptions;
import ch.supertomcat.bh.hoster.linkextract.ExtractTools;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Host class for Coppermine Galleries (Recursive)
 * 
 * @version 4.6
 */
public class HostCoppermineGalleries extends Host implements IHoster, IHosterOptions, IHosterURLAdder {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "4.6";

	/**
	 * Name dieser Klasse
	 */
	public static final String NAME = "HostCoppermineGalleries";

	/**
	 * Kompiliertes Muster
	 */
	private Pattern patternDomain;
	private Pattern patternIndexWithoutPageParam;
	private Pattern patternIndexWithoutPageParamMetaAlbums;
	private Pattern patternIndex;
	private Pattern patternIndexMetaAlbums;
	private Pattern patternThumbnailsWithoutPageParam;
	private Pattern patternThumbnailsWithoutPageParamMetaAlbums;
	private Pattern patternThumbnails;
	private Pattern patternThumbnailsMetaAlbums;
	private Pattern patternDisplay;
	private Pattern patternDisplayMetaAlbums;
	private Pattern patternDisplayThumbSearch;
	private Pattern patternDisplayDirectLinkedArchiveSearch;

	private boolean recursive = false;

	private boolean metaAlbumsEnabled = false;

	/**
	 * Konstruktor
	 */
	public HostCoppermineGalleries() {
		super(NAME, VERSION);
		patternDomain = Pattern.compile("^(https?://.*/|)(index|thumbnails|displayimage)\\.php.*$");

		patternIndexWithoutPageParam = Pattern.compile("^(https?://.*/|)index\\.php\\?cat=[0-9]+$");
		patternIndexWithoutPageParamMetaAlbums = Pattern.compile("^(https?://.*/|)index\\.php\\?cat=-?[0-9]+$");
		patternIndex = Pattern.compile("^(https?://.*/|)index\\.php\\?cat=[0-9]+(&page=[0-9]+|)$");
		patternIndexMetaAlbums = Pattern.compile("^(https?://.*/|)index\\.php\\?cat=-?[0-9]+(&page=[0-9]+|)$");

		patternThumbnailsWithoutPageParam = Pattern.compile("^(https?://.*/|)thumbnails\\.php\\?album=[0-9]+$");
		patternThumbnailsWithoutPageParamMetaAlbums = Pattern.compile("^(https?://.*/|)thumbnails\\.php\\?album=[0-9a-zA-Z]+(?:&cat=-?[0-9]+)?$");
		patternThumbnails = Pattern.compile("^(https?://.*/|)thumbnails\\.php\\?album=[0-9]+(&page=[0-9]+|)$");
		patternThumbnailsMetaAlbums = Pattern.compile("^(https?://.*/|)thumbnails\\.php\\?album=[0-9a-zA-Z]+(?:&cat=-?[0-9]+)?(&page=[0-9]+|)$");

		patternDisplay = Pattern.compile("^(https?://.*/|)displayimage\\.php\\?album=[0-9]+&(pos|pid)=[0-9]+(#top_display_media)?$");
		patternDisplayMetaAlbums = Pattern.compile("^(https?://.*/|)displayimage\\.php\\?album=[0-9a-zA-Z]+(?:&cat=-?[0-9]+)?&(pos|pid)=[0-9]+(#top_display_media)?$");

		patternDisplayThumbSearch = Pattern.compile("^(https?://.*/)(thumb_|tn)(.*)$");
		String strArchive = "(7z|arj|bz2|bzip2|cab|cpio|deb|dmg|gz|gzip|hfs|iso|lha|lzh|lzma|rar|rpm|split|swm|tar|taz|tbz|tbz2|tgz|tpz|wim|xar|z|zip)";
		patternDisplayDirectLinkedArchiveSearch = Pattern.compile("^(https?://.*/|).*\\." + strArchive + "$");

		updateBooleanOptionValue("recursive", false);
		updateBooleanOptionValue("metaAlbumsEnabled", false);
	}

	@Override
	public boolean isFromThisHoster(String url) {
		if (deactivateOption.isDeactivated()) {
			return false;
		}

		if (isDisplayPage(url)) {
			return true;
		}

		if (isThumbnailsPage(url)) {
			return true;
		}

		if (isIndexPage(url)) {
			return true;
		}

		return false;
	}

	@Override
	public String getFilenameFromURL(String url) {
		if (!isFromThisHoster(url)) {
			return "";
		}
		return "";
	}

	/**
	 * Method to get all direct linked archives
	 * 
	 * @param url URL
	 * @param context Context
	 * @param cookieStore Cookie Store
	 * @return Array with links
	 */
	private List<URL> getDirectLinkedArchives(String url, HttpClientContext context, CookieStore cookieStore) {
		List<URL> urls = new ArrayList<>();

		String encodedURL = HTTPUtil.encodeURL(url);
		try (CloseableHttpClient client = getProxyManager().getHTTPClient()) {
			// Verbindung oeffnen
			HttpGet method = new HttpGet(encodedURL);
			method.setHeader("User-Agent", getSettingsManager().getUserAgent());

			getCookieManager().fillCookies(url, cookieStore);

			Document node = client.execute(method, context, response -> {
				StatusLine statusLine = new StatusLine(response);
				int statusCode = statusLine.getStatusCode();

				if (statusCode != 200) {
					JOptionPane.showMessageDialog(getMainWindow(), "HTTP-Error:" + statusCode, "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}

				// Inputstream oeffnen
				try (InputStream in = response.getEntity().getContent()) {
					Tidy tidy = new Tidy();
					tidy.setShowWarnings(false);
					tidy.setShowErrors(0);
					tidy.setQuiet(true);
					tidy.setInputEncoding("UTF-8");
					return tidy.parseDOM(in, null);
				}
			});
			if (node == null) {
				return urls;
			}

			/*
			 * Get the links
			 */
			NodeList nl = node.getElementsByTagName("a");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				NodeList thl = n.getChildNodes();
				Node th = null;
				int thi = 0;
				for (int x = 0; x < thl.getLength(); x++) {
					if (thl.item(x).getNodeName().equals("img")) {
						th = thl.item(x);
						thi++;
					}
				}
				String s = ExtractTools.getAttributeValueFromNode(n, "href");
				if (s != null && s.length() > 0) {
					URL newURL = new URL(s);
					if ((th != null) && (thi == 1)) {
						String st = ExtractTools.getAttributeValueFromNode(th, "src");
						if (st != null) {
							newURL.setThumb(st);
						}
					}
					urls.add(newURL);
				}
			}

			return urls;
		} catch (Exception e) {
			logger.error("Could not get direct linked archive", e);
		}
		return urls;
	}

	/**
	 * Method to get all links from a page
	 * 
	 * @param url URL
	 * @param context Context
	 * @param cookieStore Cookie Store
	 * @return Array with links
	 * @throws HostException
	 */
	private List<URL> getLinks(String url, HttpClientContext context, CookieStore cookieStore) throws HostException {
		List<URL> urls = new ArrayList<>();
		String encodedURL = HTTPUtil.encodeURL(url);
		try (CloseableHttpClient client = getProxyManager().getHTTPClient()) {
			// Verbindung oeffnen
			HttpGet method = new HttpGet(encodedURL);
			method.setHeader("User-Agent", getSettingsManager().getUserAgent());

			getCookieManager().fillCookies(url, cookieStore);

			Document node = client.execute(method, context, response -> {
				StatusLine statusLine = new StatusLine(response);
				int statusCode = statusLine.getStatusCode();

				if (statusCode != 200) {
					JOptionPane.showMessageDialog(getMainWindow(), "HTTP-Error:" + statusCode, "Error", JOptionPane.ERROR_MESSAGE);
					throw new HostHttpIOException(NAME + ": Container-Page: " + url + " :HTTP-Error: " + statusCode);
				}

				// Inputstream oeffnen
				try (InputStream in = response.getEntity().getContent()) {
					Tidy tidy = new Tidy();
					tidy.setShowWarnings(false);
					tidy.setShowErrors(0);
					tidy.setQuiet(true);
					tidy.setInputEncoding("UTF-8");
					return tidy.parseDOM(in, null);
				}
			});

			/*
			 * Get the target path
			 */
			NodeList nlSpan = node.getElementsByTagName("span");

			/*
			 * Example:
			 * <span class="statlink"><b><a href="index.php">Galerie</a> > <a href="index.php?cat=19">Projects</a></b></span>
			 */

			/*
			 * Example newer version:
			 * <span class="statlink"><a href="index.php">Galerie</a> > <a href="index.php?cat=19">Projects</a></span>
			 */

			StringBuilder sb = new StringBuilder("");

			for (int i = 0; i < nlSpan.getLength(); i++) {
				Node n = nlSpan.item(i);

				String strClass = ExtractTools.getAttributeValueFromNode(n, "class");
				if (strClass != null && strClass.equals("statlink")) {
					NodeList nlB = n.getChildNodes();
					Node nodeB = null;
					for (int x = 0; x < nlB.getLength(); x++) {
						if (nlB.item(x).getNodeName().equals("b")) {
							nodeB = nlB.item(x);
							break;
						}
					}

					Node nodeAContainer = (nodeB != null) ? nodeB : n;
					NodeList nlSeperatorLinks = nodeAContainer.getChildNodes();
					for (int x = 0; x < nlSeperatorLinks.getLength(); x++) {
						if (nlSeperatorLinks.item(x).getNodeName().equals("a")) {
							String nodeVal = "";
							NodeList nlAChilds = nlSeperatorLinks.item(x).getChildNodes();
							for (int u = 0; u < nlAChilds.getLength(); u++) {
								if (nlAChilds.item(u).getNodeType() == Node.TEXT_NODE) {
									nodeVal = nlAChilds.item(u).getNodeValue();
								}
							}

							String seperatorLinkTitle = nodeVal != null ? nodeVal : "";
							if (seperatorLinkTitle.length() > 0) {
								seperatorLinkTitle = filterFilename(seperatorLinkTitle);
								seperatorLinkTitle += FileUtil.FILE_SEPERATOR;
							}
							sb.append(seperatorLinkTitle);
						}
					}
					break;
				}
			}

			java.net.URL completeURL = null;
			String rootFolder = "";
			try {
				completeURL = new java.net.URL(url);
				rootFolder = completeURL.getHost() + "_" + completeURL.getPath();
				int lastSlash = rootFolder.lastIndexOf("/");
				if (lastSlash > 0) {
					rootFolder = rootFolder.substring(0, lastSlash);
				}
				rootFolder = rootFolder.replaceAll("[/]", "_");
				rootFolder = filterFilename(rootFolder);
			} catch (MalformedURLException mue) {
			}
			String targetPath = getSettingsManager().getSavePath() + rootFolder + FileUtil.FILE_SEPERATOR + sb.toString();

			/*
			 * Get the links
			 */
			NodeList nl = node.getElementsByTagName("a");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);

				NodeList thl = n.getChildNodes();
				Node th = null;
				int thi = 0;
				for (int x = 0; x < thl.getLength(); x++) {
					if (thl.item(x).getNodeName().equals("img")) {
						th = thl.item(x);
						thi++;
					}
				}
				String s = ExtractTools.getAttributeValueFromNode(n, "href");
				if (s != null && s.length() > 0) {

					if (isIndexPageWithoutPageParam(s)) {
						boolean bSubCat = false;
						Node parentNode = n.getParentNode();
						if (parentNode != null) {
							if (parentNode.getNodeName().equals("span")) {
								String strClass = ExtractTools.getAttributeValueFromNode(parentNode, "class");
								if (strClass != null && strClass.equals("catlink")) {
									bSubCat = true;
								}
							}
							if (!bSubCat) {
								Node parentParentNode = parentNode.getParentNode();
								if (parentParentNode != null && parentParentNode.getNodeName().equals("span")) {
									String strClass = ExtractTools.getAttributeValueFromNode(parentParentNode, "class");
									if (strClass != null && strClass.equals("catlink")) {
										bSubCat = true;
									}
								}
							}
						}
						if (!bSubCat) {
							continue;
						}
					}

					URL newURL = new URL(s);
					newURL.setThreadURL(url);
					if ((th != null) && (thi == 1)) {
						String st = ExtractTools.getAttributeValueFromNode(th, "src");
						if (st != null) {
							newURL.setThumb(st);
						}
					}
					newURL.setTargetPath(targetPath);
					urls.add(newURL);
				}
			}

			return urls;
		} catch (Exception e) {
			throw new HostIOException(NAME + ": Container-Page: " + url + " :" + e.getMessage(), e);
		}
	}

	@Override
	public void openOptionsDialog() {
		final JButton btnOK = new JButton("OK");
		final JButton btnCancel = new JButton("Cancel");
		JPanel pnlButtons = new JPanel();
		JPanel pnlCenter = new JPanel();
		final JCheckBox cbRecursive = new JCheckBox(Localization.getString("CoppermineRecursive"), recursive);
		final JCheckBox cbMetaAlbumsEnabled = new JCheckBox(Localization.getString("CoppermineMetaAlbumsEnabled"), metaAlbumsEnabled);

		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);

		pnlCenter.setLayout(new GridLayout(4, 1));
		pnlCenter.add(cbRecursive);
		pnlCenter.add(cbMetaAlbumsEnabled);
		pnlCenter.add(new JLabel(Localization.getString("CoppermineAcceptedURLs1")));
		pnlCenter.add(new JLabel(Localization.getString("CoppermineAcceptedURLs2")));

		final JDialog dialog = new JDialog(getMainWindow(), "Coppermine-Galleries", true);
		dialog.setLayout(new BorderLayout());
		dialog.add(pnlButtons, BorderLayout.SOUTH);
		dialog.add(pnlCenter, BorderLayout.CENTER);

		ActionListener action = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == btnOK) {
					recursive = cbRecursive.isSelected();
					setBooleanOptionValue("recursive", recursive);
					metaAlbumsEnabled = cbMetaAlbumsEnabled.isSelected();
					setBooleanOptionValue("metaAlbumsEnabled", metaAlbumsEnabled);
					deactivateOption.saveOption();
					getSettingsManager().writeSettings(true);
					dialog.dispose();
				} else if (e.getSource() == btnCancel) {
					dialog.dispose();
				}
			}
		};

		btnOK.addActionListener(action);
		btnCancel.addActionListener(action);

		dialog.pack();
		dialog.setLocationRelativeTo(getMainWindow());
		dialog.setVisible(true);
	}

	private void updateBooleanOptionValue(String option, boolean defaultvalue) {
		boolean bVal = false;
		try {
			bVal = getSettingsManager().getBooleanValue(NAME, option);
			if (option.equals("recursive")) {
				recursive = bVal;
			} else if (option.equals("deactivated")) {
				deactivateOption.setDeactivated(bVal);
			} else if (option.equals("metaAlbumsEnabled")) {
				metaAlbumsEnabled = bVal;
			}
		} catch (Exception e2) {
			setBooleanOptionValue(option, defaultvalue);
		}
	}

	private void setBooleanOptionValue(String option, boolean value) {
		try {
			getSettingsManager().setHosterSettingValue(NAME, option, value);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}
	}

	/**
	 * Dateiname korrigieren
	 * 
	 * @param filename URL
	 * @return Korrigierter Dateiname
	 */
	private String correctFilename(String filename) {
		String retval = "";
		try {
			retval = filename.substring(filename.lastIndexOf("/") + 1);
			retval = retval.replace("%20", " ");
		} catch (StringIndexOutOfBoundsException e) {
			logger.error(e.getMessage(), e);
		}
		return retval;
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		String url = upo.getContainerURL();
		String thumbURL = upo.getThumbURL();

		if (isDisplayPage(url)) {
			String s = "";
			Matcher matcherDisplayThumbSearch = patternDisplayThumbSearch.matcher(thumbURL);
			s = matcherDisplayThumbSearch.replaceAll("$1$3");
			if (s.length() > 0) {
				CookieStore cookieStore;
				HttpClientContext context;
				if (upo.checkExistInfo(URLParseObject.DOWNLOADER_HTTP_CONTEXT, HttpClientContext.class) && upo.checkExistInfo(URLParseObject.DOWNLOADER_HTTP_COOKIE_STORE, CookieStore.class)) {
					cookieStore = upo.getInfo(URLParseObject.DOWNLOADER_HTTP_COOKIE_STORE, BasicCookieStore.class);
					context = upo.getInfo(URLParseObject.DOWNLOADER_HTTP_CONTEXT, HttpClientContext.class);
				} else {
					cookieStore = new BasicCookieStore();
					context = ContextBuilder.create().useCookieStore(cookieStore).build();
				}

				// Get direct linked archives from page
				List<URL> newURLs = getDirectLinkedArchives(url, context, cookieStore);

				String directLinkedArchive = "";

				URL currentURL;
				String strCurrentURL;
				for (int i = newURLs.size() - 1; i > -1; i--) {
					currentURL = newURLs.get(i);
					strCurrentURL = currentURL.getURL();

					Matcher matcherDirectLinkedArchive = patternDisplayDirectLinkedArchiveSearch.matcher(strCurrentURL);
					if (matcherDirectLinkedArchive.matches()) {
						if (strCurrentURL.startsWith("http://") == false && strCurrentURL.startsWith("https://") == false) {
							// If the link was relative we have to correct that
							currentURL = convertURLFromRelativeToAbsolute(url, currentURL);
							strCurrentURL = currentURL.getURL();
						}
						directLinkedArchive = strCurrentURL;
						break;
					}
				}

				if (directLinkedArchive.length() > 0) {
					s = directLinkedArchive;
				}
				upo.setDirectLink(s);
				upo.setCorrectedFilename(correctFilename(s));

				upo.addInfo(URLParseObject.REDUCE_PATH_LENGTH, false);
				upo.addInfo(URLParseObject.REDUCE_FILENAME_LENGTH, false);
			}
			return;
		}
	}

	private boolean isDisplayPage(String url) {
		if (metaAlbumsEnabled) {
			return patternDisplayMetaAlbums.matcher(url).matches();
		}
		return patternDisplay.matcher(url).matches();
	}

	private boolean isThumbnailsPage(String url) {
		if (metaAlbumsEnabled) {
			return patternThumbnailsMetaAlbums.matcher(url).matches();
		}
		return patternThumbnails.matcher(url).matches();
	}

	private boolean isThumbnailsPageWithoutPageParam(String url) {
		if (metaAlbumsEnabled) {
			return patternThumbnailsWithoutPageParamMetaAlbums.matcher(url).matches();
		}
		return patternThumbnailsWithoutPageParam.matcher(url).matches();
	}

	private boolean isIndexPage(String url) {
		if (metaAlbumsEnabled) {
			return patternIndexMetaAlbums.matcher(url).matches();
		}
		return patternIndex.matcher(url).matches();
	}

	private boolean isIndexPageWithoutPageParam(String url) {
		if (metaAlbumsEnabled) {
			return patternIndexWithoutPageParamMetaAlbums.matcher(url).matches();
		}
		return patternIndexWithoutPageParam.matcher(url).matches();
	}

	private URL convertURLFromRelativeToAbsolute(String absoluteURL, URL urlToConvert) {
		String strCurrentURL = urlToConvert.getURL();
		Matcher m = patternDomain.matcher(absoluteURL);
		strCurrentURL = m.replaceAll("$1") + strCurrentURL;

		String changedThumb = urlToConvert.getThumb();
		changedThumb = m.replaceAll("$1") + changedThumb;

		URL changedURL = new URL(strCurrentURL, changedThumb);
		changedURL.setThreadURL(urlToConvert.getThreadURL());
		changedURL.setFilenameCorrected(urlToConvert.getFilenameCorrected());
		changedURL.setTargetPath(urlToConvert.getTargetPath());
		return changedURL;
	}

	@Override
	public List<URL> isFromThisHoster(URL url, AtomicBoolean isFromThisHoster, ProgressObserver progress) throws Exception {
		// Is Only the image
		if (isDisplayPage(url.getURL())) {
			isFromThisHoster.set(true);
			return null;
		}

		// Is Thumbnails- or Index-Page
		boolean bThumbPage = isThumbnailsPage(url.getURL());
		boolean bIndexPage = isIndexPage(url.getURL());

		if (!bThumbPage && !bIndexPage) {
			isFromThisHoster.set(false);
			return null;
		}

		if (!recursive) {
			isFromThisHoster.set(false);
			return null;
		}

		String threadURL = url.getThreadURL();
		if (threadURL != null && isThumbnailsPage(threadURL) && (bIndexPage || (bThumbPage && isThumbnailsPageWithoutPageParam(url.getURL())))) {
			isFromThisHoster.set(false);
			return null;
		}

		CookieStore cookieStore = new BasicCookieStore();
		HttpClientContext context = ContextBuilder.create().useCookieStore(cookieStore).build();

		// Get links from page
		List<URL> newURLs = getLinks(url.getURL(), context, cookieStore);

		for (int i = newURLs.size() - 1; i > -1; i--) {
			URL currentURL = newURLs.get(i);
			String strCurrentURL = currentURL.getURL();

			if (isDisplayPage(strCurrentURL)) {
				if (!strCurrentURL.startsWith("http://") && !strCurrentURL.startsWith("https://")) {
					// If the link was relative we have to correct that
					currentURL = convertURLFromRelativeToAbsolute(url.getURL(), currentURL);
					strCurrentURL = currentURL.getURL();
					newURLs.set(i, currentURL);
				}
				continue;
			}

			if (isThumbnailsPage(strCurrentURL) && recursive) {
				if (!strCurrentURL.startsWith("http://") && !strCurrentURL.startsWith("https://")) {
					// If the link was relative we have to correct that
					currentURL = convertURLFromRelativeToAbsolute(url.getURL(), currentURL);
					strCurrentURL = currentURL.getURL();
					newURLs.set(i, currentURL);
				}
				if (!strCurrentURL.equals(url.getURL())) {
					continue;
				}
			}

			if (isIndexPage(strCurrentURL) && bIndexPage && recursive) {
				if (!strCurrentURL.startsWith("http://") && !strCurrentURL.startsWith("https://")) {
					// If the link was relative we have to correct that
					currentURL = convertURLFromRelativeToAbsolute(url.getURL(), currentURL);
					strCurrentURL = currentURL.getURL();
					newURLs.set(i, currentURL);
				}
				if (!strCurrentURL.equals(url.getURL())) {
					continue;
				}
			}

			// If the url is not accepted remove it
			newURLs.remove(i);
		}

		/*
		 * We got all urls from this page, so this page is not needed anymore.
		 * So we set the false-flag
		 */
		isFromThisHoster.set(false);
		return newURLs;
	}
}

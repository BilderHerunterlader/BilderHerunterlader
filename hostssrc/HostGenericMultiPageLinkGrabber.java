import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
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
import javax.swing.JTextField;

import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.StatusLine;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
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
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;

/**
 * Host class for grabbing links from all pages of a thread from Boards
 * This class should support different boards in case that different board-softwares
 * could have same url-pattern. So within in this class it could be determent which
 * board is the right one for a url.
 * 
 * @version 3.4
 */
public class HostGenericMultiPageLinkGrabber extends Host implements IHoster, IHosterOptions, IHosterURLAdder {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "3.4";

	/**
	 * Name dieser Klasse
	 */
	public static final String NAME = "HostGenericMultiPageLinkGrabber";

	/**
	 * VBULLETIN
	 */
	private static final int SOFTWARE_VBULLETIN = 0;

	/**
	 * PHPBB
	 */
	private static final int SOFTWARE_PHPBB = 1;

	/**
	 * Kompiliertes Muster
	 */
	private Pattern patternDomain;

	private Pattern patternVBulletinThread;

	private Pattern patternPhpBBThread;

	private boolean bVBulletin = true;

	private boolean bPhpBB = true;

	private int maxPages = 500;

	/**
	 * Konstruktor
	 */
	public HostGenericMultiPageLinkGrabber() {
		super(NAME, VERSION);
		patternDomain = Pattern.compile("^(https?://(.*?/){1,}|).*$");

		// vBulletin
		patternVBulletinThread = Pattern.compile("^((https?://([^/]{1,}/){1,}|)showthread\\.php\\?t=([0-9]+).*?)&page=([0-9]+)");

		// phpBB
		patternPhpBBThread = Pattern.compile("^((https?://([^/]{1,}/){1,}|)viewtopic\\.php\\?t=([0-9]+).*?)(&start=([0-9]+))");

		try {
			maxPages = getSettingsManager().getIntValue(NAME, "maxPages");
		} catch (Exception e) {
			try {
				getSettingsManager().setHosterSettingValue(NAME, "maxPages", maxPages);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		updateBooleanOptionValue("vBulletin", false);
		updateBooleanOptionValue("phpBB", false);
	}

	@Override
	public boolean isFromThisHoster(String url) {
		if (deactivateOption.isDeactivated()) {
			return false;
		}

		boolean bOK = false;

		if (bVBulletin && isVBulletin(url)) {
			bOK = true;
		}

		if (bPhpBB && isPhpBB(url)) {
			bOK = true;
		}

		return bOK;
	}

	@Override
	public String getFilenameFromURL(String url) {
		if (!isFromThisHoster(url)) {
			return "";
		}
		return "";
	}

	/**
	 * Method to get all links from a page
	 * 
	 * @param url URL
	 * @param threadID Thread-ID
	 * @param software Software
	 * @param context Context
	 * @param cookieStore Cookie Store
	 * @return Array with links
	 * @throws HostException
	 */
	@SuppressWarnings("unchecked")
	private List<URL>[] getLinks(String url, String threadID, int software, HttpClientContext context, CookieStore cookieStore) throws HostException {
		List<URL> normalURLs = new ArrayList<>();
		List<URL> multiPageURLs = new ArrayList<>();

		try (CloseableHttpClient client = getProxyManager().getHTTPClient()) {
			// Verbindung oeffnen
			String encodedURL = HTTPUtil.encodeURL(url);
			HttpGet method = new HttpGet(encodedURL);

			method.setHeader(HttpHeaders.USER_AGENT, getSettingsManager().getUserAgent());

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
			 * Get the links
			 */
			NodeList nl = node.getElementsByTagName("a");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				String s = parseNode(n);
				if (s.length() > 0) {
					switch (software) {
						case SOFTWARE_VBULLETIN:
							if (isVBulletin(s)) {
								// don't add links to other threads
								if (isVBulletin(s, threadID)) {
									URL newURL = new URL(s);
									if (!s.startsWith("http://") && !s.startsWith("https://")) {
										// If the link was relative we have to correct that
										newURL = convertURLFromRelativeToAbsolute(url, newURL);
									}
									if (!newURL.getURL().equals(url)) {
										newURL.setThreadURL(url);
										multiPageURLs.add(newURL);
									}
								}
							} else {
								URL newURL = new URL(s);
								if (!s.startsWith("http://") && !s.startsWith("https://")) {
									// If the link was relative we have to correct that
									newURL = convertURLFromRelativeToAbsolute(url, newURL);
								}
								if (!newURL.getURL().equals(url)) {
									newURL.setThreadURL(url);
									normalURLs.add(newURL);
								}
							}
							break;
						case SOFTWARE_PHPBB:
							if (isPhpBB(s)) {
								// don't add links to other threads
								if (isPhpBB(s, threadID)) {
									URL newURL = new URL(s);
									if (!s.startsWith("http://") && !s.startsWith("https://")) {
										// If the link was relative we have to correct that
										newURL = convertURLFromRelativeToAbsolute(url, newURL);
									}
									if (!newURL.getURL().equals(url)) {
										newURL.setThreadURL(url);
										multiPageURLs.add(newURL);
									}
								}
							} else {
								URL newURL = new URL(s);
								if (!s.startsWith("http://") && !s.startsWith("https://")) {
									// If the link was relative we have to correct that
									newURL = convertURLFromRelativeToAbsolute(url, newURL);
								}
								if (!newURL.getURL().equals(url)) {
									newURL.setThreadURL(url);
									normalURLs.add(newURL);
								}
							}
							break;
						default:
							// Software not supported
							break;
					}
				}
			}

			return new List[] { normalURLs, multiPageURLs };
		} catch (Exception e) {
			throw new HostIOException(NAME + ": Container-Page: " + url + " :" + e.getMessage(), e);
		}
	}

	/**
	 * a-Node parsen
	 * 
	 * @param n Node
	 * @return URL
	 */
	private String parseNode(Node n) {
		String url = "";
		NamedNodeMap nnm = n.getAttributes();
		url = getURLFromNode(nnm);
		return url;
	}

	/**
	 * Attribut parsen
	 * 
	 * @param nnm NamedNodeMap
	 * @return URL
	 */
	private String getURLFromNode(NamedNodeMap nnm) {
		String url = "";
		for (int o = 0; o < nnm.getLength(); o++) {
			Node no = nnm.item(o);
			if (no.getNodeName().equals("href")) {
				url = no.getNodeValue();
				return url;
			}
		}
		return "";
	}

	@Override
	public void openOptionsDialog() {
		final JButton btnOK = new JButton("OK");
		final JButton btnCancel = new JButton("Cancel");
		JPanel pnlButtons = new JPanel();
		JPanel pnlCenter = new JPanel();
		JLabel lblMaxPages = new JLabel(Localization.getString("GenericMultiPageLinkGrabberMaxPages"));
		final JTextField txtMaxPages = new JTextField("0", 4);
		final JCheckBox cbVBulletin = new JCheckBox(Localization.getString("GenericMultiPageLinkGrabberVBulletin"), bVBulletin);
		final JCheckBox cbPhpBB = new JCheckBox(Localization.getString("GenericMultiPageLinkGrabberPhpBB"), bPhpBB);

		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);

		txtMaxPages.setToolTipText(Localization.getString("GenericMultiPageLinkGrabberMaxPagesToolTip"));

		pnlCenter.setLayout(new GridLayout(3, 2));
		pnlCenter.add(lblMaxPages);
		pnlCenter.add(txtMaxPages);
		pnlCenter.add(cbVBulletin);
		pnlCenter.add(cbPhpBB);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtMaxPages);

		final JDialog dialog = new JDialog(getMainWindow(), "GenericMultiPageLinkGrabber", true);
		dialog.setLayout(new BorderLayout());
		dialog.add(pnlButtons, BorderLayout.SOUTH);
		txtMaxPages.setText(Integer.toString(maxPages));
		cbVBulletin.setSelected(bVBulletin);
		cbPhpBB.setSelected(bPhpBB);
		dialog.add(pnlCenter, BorderLayout.CENTER);

		ActionListener action = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == btnOK) {
					try {
						maxPages = Integer.parseInt(txtMaxPages.getText());
					} catch (NumberFormatException nfe) {
						return;
					}
					bVBulletin = cbVBulletin.isSelected();
					bPhpBB = cbPhpBB.isSelected();
					try {
						getSettingsManager().setHosterSettingValue(NAME, "maxPages", maxPages);
					} catch (Exception e1) {
						logger.error(e1.getMessage(), e1);
					}
					setBooleanOptionValue("vBulletin", bVBulletin);
					setBooleanOptionValue("phpBB", bPhpBB);
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
			if (option.equals("vBulletin")) {
				bVBulletin = bVal;
			} else if (option.equals("phpBB")) {
				bPhpBB = bVal;
			} else if (option.equals("deactivated")) {
				deactivateOption.setDeactivated(bVal);
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

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		throw new HostException("This Host-Plugin cannot parse URLs trough parseURLAndFilename-Method");
	}

	private URL convertURLFromRelativeToAbsolute(String absoluteURL, URL urlToConvert) {
		String strCurrentURL = urlToConvert.getURL();
		Matcher m = patternDomain.matcher(absoluteURL);
		strCurrentURL = m.replaceAll("$1") + strCurrentURL;

		URL changedURL = new URL(strCurrentURL, urlToConvert.getThumb());
		changedURL.setThreadURL(urlToConvert.getThreadURL());
		changedURL.setFilenameCorrected(urlToConvert.getFilenameCorrected());
		changedURL.setTargetPath(urlToConvert.getTargetPath());
		return changedURL;
	}

	/*
	 * vBulletin
	 */

	private boolean isVBulletin(String url) {
		Matcher matcher = patternVBulletinThread.matcher(url);
		return matcher.matches();
	}

	private boolean isVBulletin(String url, String threadID) {
		Pattern patternVBulletinThread = Pattern.compile("^(https?://([^/]{1,}/){1,}|)showthread\\.php\\?t=" + threadID + ".*?(&page=([0-9]+))");
		Matcher matcher = patternVBulletinThread.matcher(url);
		return matcher.matches();
	}

	private String getVBulletinThreadID(String url) {
		String retval = "";
		Matcher matcher = patternVBulletinThread.matcher(url);
		retval = matcher.replaceAll("$4");
		return retval;
	}

	private String getVBulletinThreadURL(String url) {
		String retval = "";
		Matcher matcher = patternVBulletinThread.matcher(url);
		retval = matcher.replaceAll("$1");
		return retval;
	}

	private int getVBulletinThreadPage(String url) {
		String retval = "";
		Matcher matcher = patternVBulletinThread.matcher(url);
		retval = matcher.replaceAll("$5");
		int iRetval = 1;
		try {
			iRetval = Integer.parseInt(retval);
		} catch (NumberFormatException nfe) {
		}
		return iRetval;
	}

	/*
	 * phpBB
	 */

	private boolean isPhpBB(String url) {
		Matcher matcher = patternPhpBBThread.matcher(url);
		return matcher.matches();
	}

	private boolean isPhpBB(String url, String threadID) {
		Pattern patternPhpBBThread = Pattern.compile("^(https?://([^/]{1,}/){1,}|)viewtopic\\.php\\?t=" + threadID + ".*?(&start=([0-9]+))");
		Matcher matcher = patternPhpBBThread.matcher(url);
		return matcher.matches();
	}

	private String getPhpBBThreadID(String url) {
		String retval = "";
		Matcher matcher = patternPhpBBThread.matcher(url);
		retval = matcher.replaceAll("$4");
		return retval;
	}

	private String getPhpBBThreadURL(String url) {
		String retval = "";
		Matcher matcher = patternPhpBBThread.matcher(url);
		retval = matcher.replaceAll("$1");
		return retval;
	}

	private int getPhpBBThreadStart(String url) {
		String retval = "";
		Matcher matcher = patternPhpBBThread.matcher(url);
		retval = matcher.replaceAll("$6");
		int iRetval = 0;
		try {
			iRetval = Integer.parseInt(retval);
		} catch (NumberFormatException nfe) {
		}
		return iRetval;
	}

	@Override
	public List<URL> isFromThisHoster(URL url, AtomicBoolean isFromThisHoster, ProgressObserver progress) throws Exception {
		isFromThisHoster.set(false);

		List<URL> newURLs = new ArrayList<>();

		CookieStore cookieStore = new BasicCookieStore();
		HttpClientContext context = ContextBuilder.create().useCookieStore(cookieStore).build();

		if (isVBulletin(url.getURL())) {
			String threadID = getVBulletinThreadID(url.getURL());
			String threadURL = getVBulletinThreadURL(url.getURL());

			List<URL> urlsToScan = new ArrayList<>();
			urlsToScan.add(url);

			progress.progressChanged(0, 1, 0);
			progress.progressChanged(Localization.getString("GenericMultiPageLinkGrabberGrabbingLinks") + ": " + url.getURL() + "..." + 0 + "/" + 1);

			// Get links from page and find out start- and endpage
			int firstPage = 1;
			int lastPage = 1;
			List<URL>[] urlVectorsFirstTime = getLinks(url.getURL(), threadID, SOFTWARE_VBULLETIN, context, cookieStore);
			for (int x = 0; x < urlVectorsFirstTime[1].size(); x++) {
				int page = getVBulletinThreadPage(urlVectorsFirstTime[1].get(x).getURL());
				if (page > lastPage) {
					lastPage = page;
				}
			}

			for (int i = firstPage; i <= lastPage; i++) {
				// Get links from page
				String urlToScan = threadURL + "&page=" + i;
				List<URL>[] urlVectors = getLinks(urlToScan, threadID, SOFTWARE_VBULLETIN, context, cookieStore);
				newURLs.addAll(urlVectors[0]);

				progress.progressChanged(firstPage, lastPage, i);
				progress.progressChanged(Localization.getString("GenericMultiPageLinkGrabberGrabbingLinks") + ": " + url.getURL() + "... " + i + "/" + lastPage);
				if (maxPages > 0 && i >= maxPages) {
					break;
				}
			}

		} else if (isPhpBB(url.getURL())) {
			String threadID = getPhpBBThreadID(url.getURL());
			String threadURL = getPhpBBThreadURL(url.getURL());
			int currentPageStart = getPhpBBThreadStart(url.getURL());

			List<URL> urlsToScan = new ArrayList<>();
			urlsToScan.add(url);

			progress.progressChanged(0, 1, 0);
			progress.progressChanged(Localization.getString("GenericMultiPageLinkGrabberGrabbingLinks") + ": " + url.getURL() + "..." + 0 + "/" + 1);

			// Get links from page and find out start- and endpage, and posts per page
			int firstPageStart = 0;
			int lastPageStart = 1;
			int step = 0;
			List<URL>[] urlVectorsFirstTime = getLinks(url.getURL(), threadID, SOFTWARE_PHPBB, context, cookieStore);
			int secondStart = Integer.MAX_VALUE;
			for (int x = 0; x < urlVectorsFirstTime[1].size(); x++) {
				int start = getPhpBBThreadStart(urlVectorsFirstTime[1].get(x).getURL());
				if (start > lastPageStart) {
					lastPageStart = start;
				}
				if (start > 0 && start < secondStart) {
					secondStart = start;
				}
			}
			if (currentPageStart < secondStart && currentPageStart > 0) {
				secondStart = currentPageStart;
			}
			step = secondStart;

			if (step > 0) {
				int endIndex = (lastPageStart / step) + 1;
				for (int i = firstPageStart; i <= lastPageStart; i += step) {
					// Get links from page
					String urlToScan = threadURL + "&start=" + i;
					logger.debug("urltoscan: {}", urlToScan);
					List<URL>[] urlVectors = getLinks(urlToScan, threadID, SOFTWARE_PHPBB, context, cookieStore);
					newURLs.addAll(urlVectors[0]);

					progress.progressChanged(1, endIndex, ((i / step) + 1));
					progress.progressChanged(Localization.getString("GenericMultiPageLinkGrabberGrabbingLinks") + ": " + url.getURL() + "... " + ((i / step) + 1) + "/" + endIndex);
					if (maxPages > 0 && i >= maxPages) {
						break;
					}
				}
			}
		}

		return newURLs;
	}

	@Override
	public boolean removeDuplicateEqualsMethod(URL url1, URL url2) {
		if (isVBulletin(url2.getURL())) {
			String threadID = getVBulletinThreadID(url1.getURL());
			if (isVBulletin(url2.getURL(), threadID)) {
				return true;
			}
			return false;
		} else if (isPhpBB(url2.getURL())) {
			String threadID = getPhpBBThreadID(url1.getURL());
			if (isPhpBB(url2.getURL(), threadID)) {
				return true;
			}
			return false;
		} else {
			return super.removeDuplicateEqualsMethod(url1, url2);
		}
	}
}

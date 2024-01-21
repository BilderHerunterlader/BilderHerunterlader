import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.IHosterURLAdder;
import ch.supertomcat.bh.hoster.hosteroptions.IHosterOptions;
import ch.supertomcat.bh.hoster.linkextract.ExtractTools;
import ch.supertomcat.bh.hoster.linkextract.ILinkExtractFilter;
import ch.supertomcat.bh.hoster.linkextract.LinkExtract;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.rules.RuleRegExp;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Host class for Photo Sharing Galleries (Recursive)
 * 
 * @version 3.3
 */
public class HostPhotoSharingGallery extends Host implements IHoster, IHosterURLAdder, IHosterOptions {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "3.3";

	/**
	 * Name dieser Klasse
	 */
	public static final String NAME = "HostPhotoSharingGallery";

	/**
	 * Kompiliertes Muster
	 */
	private Pattern patternCategory;
	private Pattern patternCategoryNoPages;
	private Pattern patternCategoryPages;
	private Pattern patternPhoto;

	private RuleRegExp regexImg = new RuleRegExp();

	private boolean recursive = false;

	/**
	 * Konstruktor
	 */
	public HostPhotoSharingGallery() {
		super(NAME, VERSION);
		patternCategory = Pattern.compile("https?://[^/]+/([^/]+/)*showgallery\\.php(/cat/[0-9]+/?(page/[0-9]+)?|\\?cat=[0-9]+(&page=[0-9]+)?)");
		patternCategoryNoPages = Pattern.compile("https?://[^/]+/([^/]+/)*showgallery\\.php(/cat/[0-9]+/?|\\?cat=[0-9]+)");
		patternCategoryPages = Pattern.compile("https?://[^/]+/([^/]+/)*showgallery\\.php(/cat/[0-9]+/page/[0-9]+|\\?cat=[0-9]+&page=[0-9]+)");

		patternPhoto = Pattern.compile("https?://[^/]+/([^/]+/)*showphoto\\.php(/photo/[0-9]+|\\?photo=[0-9]+)");

		regexImg.setSearch("<img.*?src=\"(https?://[^/]+/.*?data/[0-9]+/)(?:.*?/)?(.*?)\"");
		regexImg.setReplace("$1$2");

		try {
			recursive = getBooleanOptionValue("recursive");
		} catch (Exception e) {
			try {
				setBooleanOptionValue("recursive", false);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
	}

	@Override
	public boolean isFromThisHoster(String url) {
		if (deactivateOption.isDeactivated()) {
			return false;
		}
		Matcher matcherCategory = patternCategory.matcher(url);
		if (matcherCategory.matches()) {
			return true;
		}

		Matcher matcherPhoto = patternPhoto.matcher(url);
		if (matcherPhoto.matches()) {
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
	 * Get the page-title from the document
	 * 
	 * @param documentNode Document-Node
	 * @param url URL
	 * @return Page-title
	 */
	private String getTargetPath(Document documentNode, URL url) {
		// Get the page-title
		String title = "";
		// Get the title-Element
		NodeList nlt = documentNode.getElementsByTagName("title");

		nlt = documentNode.getElementsByTagName("span");
		for (int i = 0; i < nlt.getLength(); i++) {
			Node node = nlt.item(i);

			String strClass = ExtractTools.getAttributeValueFromNode(node, "class");
			if (strClass != null && strClass.equals("medium")) {
				// Get the childnodes
				NodeList lChilds = node.getChildNodes();
				if (lChilds.getLength() > 1) {
					Node firstChild = lChilds.item(0);
					if (firstChild.getNodeName().equals("a")) {
						String val = firstChild.getFirstChild().getNodeValue();
						if (val != null && val.equals("Home")) {
							Node secondChild = lChilds.item(1);
							if (secondChild.getNodeValue().equals(" � ")) {
								StringBuilder sb = new StringBuilder();
								for (int x = 1; x < lChilds.getLength(); x++) {
									Node nx = lChilds.item(x);
									if (nx.getNodeName().equals("a")) {
										String titlePart = nx.getFirstChild().getNodeValue();
										sb.append(titlePart + FileUtil.FILE_SEPERATOR);
									}
								}
								title = sb.toString();
								break;
							}
						}
					}
				}
			}
		}

		title = filterPath(title);

		String dlDir = getSettingsManager().getSavePath();
		if (dlDir.length() > 0 && !dlDir.endsWith("/") && !dlDir.endsWith("\\")) {
			dlDir += FileUtil.FILE_SEPERATOR;
		}

		// Get host and use it as the root folder
		java.net.URL completeURL = null;
		String rootFolder = "";
		try {
			completeURL = new java.net.URL(url.getURL());
			rootFolder = completeURL.getHost() + "_" + completeURL.getPath();
			int lastSlash = rootFolder.lastIndexOf("/");
			if (lastSlash > 0) {
				rootFolder = rootFolder.substring(0, lastSlash);
			}
			rootFolder = rootFolder.replace("/", "_");
			rootFolder = filterFilename(rootFolder);
		} catch (MalformedURLException mue) {
		}

		return dlDir + rootFolder + FileUtil.FILE_SEPERATOR + title;
	}

	@Override
	public void openOptionsDialog() {
		final JButton btnOK = new JButton("OK");
		final JButton btnCancel = new JButton("Cancel");
		JPanel pnlButtons = new JPanel();
		JPanel pnlCenter = new JPanel();
		final JCheckBox cbRecursive = new JCheckBox(Localization.getString("CoppermineRecursive"), recursive);

		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);

		pnlCenter.setLayout(new GridLayout(1, 1));
		pnlCenter.add(cbRecursive);

		final JDialog dialog = new JDialog(getMainWindow(), "HostPhotoSharingGallery", true);
		dialog.setLayout(new BorderLayout());
		dialog.add(pnlButtons, BorderLayout.SOUTH);
		dialog.add(pnlCenter, BorderLayout.CENTER);

		ActionListener action = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == btnOK) {
					recursive = cbRecursive.isSelected();
					setBooleanOptionValue("recursive", recursive);
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

	private boolean getBooleanOptionValue(String name) throws Exception {
		return getSettingsManager().getBooleanValue(NAME, name);
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
		try {
			return filename.substring(filename.lastIndexOf("/") + 1);
		} catch (StringIndexOutOfBoundsException e) {
			logger.error(e.getMessage(), e);
		}
		return "";
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		if (deactivateOption.isDeactivated()) {
			return;
		}
		Matcher matcher = patternPhoto.matcher(upo.getContainerURL());
		if (matcher.matches()) {
			String htmlcode = downloadContainerPage(upo.getContainerURL(), "");

			String imgsrc = regexImg.doPageSourcecodeReplace(htmlcode, 0, upo.getContainerURL(), upo.getPic());
			upo.setDirectLink(imgsrc);

			String correctedFilename = correctFilename(upo.getDirectLink());
			upo.setCorrectedFilename(correctedFilename);
		}
	}

	@Override
	public List<URL> isFromThisHoster(URL url, AtomicBoolean isFromThisHoster, ProgressObserver progress) throws Exception {
		Matcher matcher = patternPhoto.matcher(url.getURL());
		if (matcher.matches()) {
			isFromThisHoster.set(true);
			return null;
		}

		if (!recursive) {
			isFromThisHoster.set(false);
			return null;
		}

		// Get links from page

		ILinkExtractFilter filter = new ILinkExtractFilter() {
			private HashMap<String, String> targetPaths = new HashMap<>();

			@Override
			public boolean isLinkAccepted(Node nodeURL, Document nodeRoot, URL url, String containerURL) {

				Matcher matcherCat = patternCategoryNoPages.matcher(url.getURL());
				if (matcherCat.matches()) {
					Node parent = nodeURL.getParentNode();
					if (parent != null) {
						String strClass = ExtractTools.getAttributeValueFromNode(parent, "class");
						if (strClass != null && strClass.equals("catcolumn")) {
							return true;
						}
					}
				}

				Matcher matcherCatPages = patternCategoryPages.matcher(url.getURL());
				if (matcherCatPages.matches()) {
					return true;
				}

				Matcher matcherPhoto = patternPhoto.matcher(url.getURL());
				if (matcherPhoto.matches()) {
					Node imgNode = nodeURL.getFirstChild();
					if (imgNode != null && imgNode.getNodeName().equals("img")) {
						String strT = ExtractTools.getAttributeValueFromNode(imgNode, "title");
						if (strT != null && strT.length() > 0) {
							String targetPath = targetPaths.get(containerURL);
							if (targetPath == null) {
								targetPath = getTargetPath(nodeRoot, url);
								targetPaths.put(containerURL, targetPath);
							}
							url.setTargetPath(targetPath);
							return true;
						}
					}
				}

				return false;
			}
		};

		List<URL> downloadedCategoryLinks = new ArrayList<>();
		List<URL> links = LinkExtract.getLinks(url.getURL(), "", filter, getProxyManager(), getSettingsManager(), getCookieManager());
		for (int i = 0; i < links.size(); i++) {
			Matcher matcherCat = patternCategory.matcher(links.get(i).getURL());
			if (matcherCat.matches()) {
				if (links.get(i).getURL().endsWith("page/1") || links.get(i).getURL().endsWith("page=1")) {
					downloadedCategoryLinks.add(links.get(i));
				} else if (!downloadedCategoryLinks.contains(links.get(i))) {
					progress.progressChanged("Extracting Links from " + links.get(i).getURL() + " (" + i + "/" + links.size() + ")");
					List<URL> foundLinks = LinkExtract.getLinks(links.get(i).getURL(), "", filter, getProxyManager(), getSettingsManager(), getCookieManager());
					for (int x = 0; x < foundLinks.size(); x++) {
						if (!links.contains(foundLinks.get(x))) {
							links.add(foundLinks.get(x));
						}
					}
					downloadedCategoryLinks.add(links.get(i));
				}
				links.remove(i);
				i--;
				progress.progressChanged(0, links.size(), i);
				progress.progressChanged("Extracting Links from " + url.getURL() + " (" + i + "/" + links.size() + ")");
			} else {
				progress.progressChanged(i);
				progress.progressChanged("Extracting Links from " + url.getURL() + " (" + i + "/" + links.size() + ")");
			}
		}

		/*
		 * We got all urls from this page, so this page is not needed anymore.
		 * So we set the false-flag
		 */
		isFromThisHoster.set(false);
		return links;
	}
}

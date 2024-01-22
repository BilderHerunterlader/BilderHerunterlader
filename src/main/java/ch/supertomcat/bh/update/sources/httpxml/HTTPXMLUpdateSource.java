package ch.supertomcat.bh.update.sources.httpxml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.message.StatusLine;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.exceptions.HostHttpIOException;
import ch.supertomcat.bh.update.UpdateException;
import ch.supertomcat.bh.update.UpdateIOException;
import ch.supertomcat.bh.update.UpdateSource;
import ch.supertomcat.bh.update.containers.UpdateList;
import ch.supertomcat.bh.update.containers.UpdateObject;
import ch.supertomcat.bh.update.containers.UpdateSourceFile;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;

/**
 * UpdateSource implementation which retrieves Updates by an xml file over HTTP
 */
public class HTTPXMLUpdateSource implements UpdateSource {
	/**
	 * Proxy Manager
	 */
	protected final ProxyManager proxyManager;

	/**
	 * Constructor
	 * 
	 * @param proxyManager Proxy Manager
	 */
	public HTTPXMLUpdateSource(ProxyManager proxyManager) {
		this.proxyManager = proxyManager;
	}

	/**
	 * Downloads and parses Update-xml-File
	 * 
	 * @param url URL
	 * @return UpdateList if updates available, null otherwise
	 * @throws UpdateException
	 */
	private UpdateList getUpdateXmlFile(String url) throws UpdateException {
		UpdateObject updateBH = null;
		List<UpdateObject> updateRules = new ArrayList<>();
		List<UpdateObject> updateHostPlugins = new ArrayList<>();
		List<UpdateObject> updateRedirectPlugins = new ArrayList<>();

		url = HTTPUtil.encodeURL(url);
		try (CloseableHttpClient client = proxyManager.getHTTPClient()) {
			HttpGet method = new HttpGet(url);
			Document doc = client.execute(method, response -> {
				StatusLine statusLine = new StatusLine(response);
				int statusCode = statusLine.getStatusCode();

				if (statusCode != 200) {
					throw new UpdateIOException("HTTP-Error: " + statusCode + " " + statusLine.getReasonPhrase());
				}

				try (InputStream in = response.getEntity().getContent()) {
					SAXBuilder b = new SAXBuilder();
					try {
						return b.build(in);
					} catch (JDOMException e) {
						throw new HostHttpIOException("Could not parse XML", e);
					}
				}
			});

			Element root = doc.getRootElement();

			// Main
			Element main = root.getChild("main");
			updateBH = getUpdateObject(main, UpdateObject.UpdateType.TYPE_BH);

			Element changelog = root.getChild("changelog");
			List<Element> changes = changelog.getChildren();
			StringBuilder bufferDE = new StringBuilder();
			StringBuilder bufferEN = new StringBuilder();
			Element change = null;
			String lang = "";
			String version = "";
			String description = "";
			for (int l = 0; l < changes.size(); l++) {
				change = changes.get(l);
				lang = change.getAttribute("lng").getValue();
				version = change.getAttribute("version").getValue();
				description = change.getValue();
				if (lang.equals("de")) {
					if (bufferDE.length() > 0) {
						bufferDE.append("\n");
					}
					bufferDE.append("Version: " + version + "\n");
					bufferDE.append(description + "\n");
				} else {
					if (bufferEN.length() > 0) {
						bufferEN.append("\n");
					}
					bufferEN.append("Version: " + version + "\n");
					bufferEN.append(description + "\n");
				}
			}

			updateBH.setChangeLog("DE", bufferDE.toString());
			updateBH.setChangeLog("EN", bufferEN.toString());

			// Hosts und Rules
			updateHostPlugins.clear();
			updateRules.clear();

			Element hoster = root.getChild("hoster");
			Iterator<?> ite = hoster.getChildren().iterator();

			while (ite.hasNext()) {
				Element el = (Element)ite.next();
				String filename = el.getAttributeValue("filename");
				UpdateObject.UpdateType type = UpdateObject.UpdateType.TYPE_HOST_PLUGIN;
				if (filename.endsWith(".xml")) {
					type = UpdateObject.UpdateType.TYPE_RULE;
				}
				UpdateObject update = getUpdateObject(el, type);
				if (type == UpdateObject.UpdateType.TYPE_HOST_PLUGIN) {
					updateHostPlugins.add(update);
				} else if (type == UpdateObject.UpdateType.TYPE_RULE) {
					updateRules.add(update);
				}
			}

			// Redirects
			updateRedirectPlugins.clear();

			Element redirects = root.getChild("redirects");
			Iterator<?> itr = redirects.getChildren().iterator();

			while (itr.hasNext()) {
				Element el = (Element)itr.next();
				UpdateObject update = getUpdateObject(el, UpdateObject.UpdateType.TYPE_REDIRECT_PLUGIN);
				updateRedirectPlugins.add(update);
			}

			return new UpdateList(updateBH, updateRules, updateHostPlugins, updateRedirectPlugins);
		} catch (Exception e) {
			throw new UpdateException("Could not retrieve update list", e);
		}
	}

	/**
	 * Returns the UpdateObject or null if not all required information is present
	 * 
	 * @param elHost Element
	 * @param updateType Update-Type
	 * @return UpdateObject or null if not all required information is present
	 */
	private UpdateObject getUpdateObject(Element elHost, UpdateObject.UpdateType updateType) {
		UpdateObject updateObject = null;

		// Read out name
		String updateName = elHost.getAttributeValue("name");
		if (updateName == null) {
			return null;
		}

		// Read out version
		String version = elHost.getAttributeValue("version");
		if (version == null) {
			return null;
		}

		// Read out min and max version
		String minVersion = elHost.getAttributeValue("bhminversion");
		String maxVersion = elHost.getAttributeValue("bhmaxversion");
		if (minVersion == null) {
			minVersion = "";
		}
		if (maxVersion == null) {
			maxVersion = "";
		}

		// Read out delete attribute
		String delete = elHost.getAttributeValue("delete");

		// Read out source
		List<UpdateSourceFile> sources = new ArrayList<>();

		String source = elHost.getAttributeValue("src");
		if (source == null) {
			return null;
		}
		String targetFilename = elHost.getAttributeValue("filename");
		if (targetFilename == null) {
			return null;
		}
		sources.add(new HTTPUpdateSourceFile(source, targetFilename, delete != null, proxyManager));

		/*
		 * In the new version of the update-xml-file host-plugins and redirect-plugins
		 * can now define additional source-files, so we read them out too
		 */
		if (updateType == UpdateObject.UpdateType.TYPE_HOST_PLUGIN || updateType == UpdateObject.UpdateType.TYPE_REDIRECT_PLUGIN) {
			Iterator<?> itSource = elHost.getChildren().iterator();
			while (itSource.hasNext()) {
				Element el = (Element)itSource.next();
				if (el.getName().equals("source")) {
					String src = el.getAttributeValue("src");
					if (src == null) {
						return null;
					}
					String tgtFilename = el.getAttributeValue("filename");
					if (tgtFilename == null) {
						return null;
					}
					String deleteAdditional = el.getAttributeValue("delete");
					sources.add(new HTTPUpdateSourceFile(src, tgtFilename, deleteAdditional != null, proxyManager));
				}
			}
		}

		// Create update object
		updateObject = new UpdateObject(updateType, updateName, version, sources, minVersion, maxVersion);

		// Add type specific information to the created update object
		if (updateType == UpdateObject.UpdateType.TYPE_HOST_PLUGIN || updateType == UpdateObject.UpdateType.TYPE_REDIRECT_PLUGIN || updateType == UpdateObject.UpdateType.TYPE_RULE) {
			if (delete != null) {
				updateObject.setAction(UpdateObject.UpdateActionType.ACTION_REMOVE);
				updateObject.setComment(delete);
			}
		}

		return updateObject;
	}

	@Override
	public UpdateList checkForUpdates() throws UpdateException {
		String updateXMLURL = ApplicationProperties.getProperty("UpdateURL");
		return getUpdateXmlFile(updateXMLURL);
	}
}

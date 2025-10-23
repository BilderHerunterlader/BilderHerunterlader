package ch.supertomcat.bh.update.sources.httpxml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.message.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.update.UpdateException;
import ch.supertomcat.bh.update.UpdateIOException;
import ch.supertomcat.bh.update.UpdatesXmlIO;
import ch.supertomcat.bh.update.sources.UpdateSource;
import ch.supertomcat.bh.updates.xml.MainVersion;
import ch.supertomcat.bh.updates.xml.UpdateData;
import ch.supertomcat.bh.updates.xml.UpdateDataAdditionalSource;
import ch.supertomcat.bh.updates.xml.Updates;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;
import jakarta.xml.bind.JAXBException;

/**
 * UpdateSource implementation which retrieves Updates by an xml file over HTTP
 */
public class HTTPXMLUpdateSource implements UpdateSource {
	/**
	 * Logger
	 */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Proxy Manager
	 */
	protected final ProxyManager proxyManager;

	/**
	 * Update Xml IO
	 */
	protected final UpdatesXmlIO updateXmlIO;

	/**
	 * Constructor
	 * 
	 * @param proxyManager Proxy Manager
	 * @throws JAXBException
	 * @throws SAXException
	 * @throws IOException
	 */
	public HTTPXMLUpdateSource(ProxyManager proxyManager) throws IOException, SAXException, JAXBException {
		this.proxyManager = proxyManager;
		this.updateXmlIO = new UpdatesXmlIO();
	}

	@Override
	public Updates checkForUpdates() throws UpdateException {
		String updateXMLURL = ApplicationProperties.getProperty("UpdateURL");
		logger.info("Downloading Updates XML: {}", updateXMLURL);
		try (CloseableHttpClient client = proxyManager.getHTTPClient()) {
			String encodedURL = HTTPUtil.encodeURL(updateXMLURL);
			HttpGet method = new HttpGet(encodedURL);
			return client.execute(method, response -> {
				StatusLine statusLine = new StatusLine(response);
				int statusCode = statusLine.getStatusCode();

				logger.info("StatusLine: {}", statusLine);

				if (statusCode != 200) {
					throw new UpdateIOException("HTTP-Error: " + statusCode + " " + statusLine.getReasonPhrase());
				}

				try (@SuppressWarnings("resource")
				InputStream in = response.getEntity().getContent()) {
					/*
					 * Don't validate, so that XML can still be read, even if there are changes in it which don't match the current XSD
					 */
					try {
						Updates updatesXML = updateXmlIO.readUpdates(in, false);
						logger.info("Download Successful: Source: {}", updateXMLURL);
						return updatesXML;
					} catch (JAXBException e) {
						throw new UpdateIOException("Failed to parse XML", e);
					}
				}
			});
		} catch (Exception e) {
			throw new UpdateException("Could not retrieve update list", e);
		}
	}

	/**
	 * Download File
	 * 
	 * @param url URL
	 * @param targetFile Target File
	 * @throws UpdateException
	 */
	protected void downloadFile(String url, Path targetFile) throws UpdateException {
		logger.info("Download Update: Source: {}, Target: {}", url, targetFile);
		String encodedURL = HTTPUtil.encodeURL(url);
		try (CloseableHttpClient client = proxyManager.getHTTPClient()) {
			HttpGet method = new HttpGet(encodedURL);

			Path targetFolder = targetFile.toAbsolutePath().getParent();
			Files.createDirectories(targetFolder);

			client.execute(method, response -> {
				StatusLine statusLine = new StatusLine(response);
				int statusCode = statusLine.getStatusCode();

				logger.info("StatusLine: {}", statusLine);

				if (statusCode != 200) {
					throw new UpdateIOException("HTTP-Error: " + statusCode + " " + statusLine.getReasonPhrase());
				}

				try (@SuppressWarnings("resource")
				InputStream in = response.getEntity().getContent();
						ReadableByteChannel inChannel = Channels.newChannel(in);
						FileChannel out = FileChannel.open(targetFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
					long totalBytesRead = 0;
					long bytesRead;
					while ((bytesRead = out.transferFrom(inChannel, totalBytesRead, ProxyManager.BUFFER_SIZE)) > 0) {
						totalBytesRead += bytesRead;
					}
				}
				logger.info("Download Successful: Source: {}, Target: {}", url, targetFile);
				return null;
			});
		} catch (IOException e) {
			throw new UpdateException("Download Update failed: " + url, e);
		}
	}

	@Override
	public Path downloadUpdate(MainVersion update, Path targetDirectory) throws UpdateException {
		Path targetFile = targetDirectory.resolve(update.getFilename());
		downloadFile(update.getSrc(), targetFile);
		return targetFile;
	}

	@Override
	public List<Path> downloadUpdate(UpdateData update, Path targetDirectory) throws UpdateException {
		if (update.getDelete() != null) {
			return Collections.emptyList();
		}

		List<Path> downloadedFiles = new ArrayList<>();

		Path targetFile = targetDirectory.resolve(update.getFilename());
		downloadFile(update.getSrc(), targetFile);
		downloadedFiles.add(targetFile);

		for (UpdateDataAdditionalSource additionalFile : update.getSource()) {
			if (additionalFile.getDelete() != null) {
				continue;
			}

			Path subTargetFile = targetDirectory.resolve(additionalFile.getFilename());
			downloadFile(additionalFile.getSrc(), subTargetFile);
			downloadedFiles.add(subTargetFile);
		}

		return downloadedFiles;
	}
}

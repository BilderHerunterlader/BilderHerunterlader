package ch.supertomcat.bh.update.sources.httpxml;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.message.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.update.UpdateException;
import ch.supertomcat.bh.update.UpdateIOException;
import ch.supertomcat.bh.update.containers.UpdateObject.UpdateActionType;
import ch.supertomcat.bh.update.containers.UpdateObject.UpdateType;
import ch.supertomcat.bh.update.containers.UpdateSourceFile;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;

/**
 * HTTP Update Source File
 */
public class HTTPUpdateSourceFile extends UpdateSourceFile {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	private final String sourceURL;

	private final String targetFilename;

	/**
	 * Proxy Manager
	 */
	private final ProxyManager proxyManager;

	/**
	 * Constructor
	 * 
	 * @param sourceURL Source URL
	 * @param targetFilename Target Filename
	 * @param delete True if is delete update, false otherwise
	 * @param proxyManager Proxy Manager
	 */
	public HTTPUpdateSourceFile(String sourceURL, String targetFilename, boolean delete, ProxyManager proxyManager) {
		super(delete);
		this.sourceURL = sourceURL;
		this.targetFilename = targetFilename;
		this.proxyManager = proxyManager;
	}

	@Override
	public String getSource() {
		return sourceURL;
	}

	@Override
	public String getTargetFilename() {
		return targetFilename;
	}

	@Override
	public boolean downloadUpdate(UpdateType updateType, UpdateActionType action, String target) throws UpdateException {
		if (action == UpdateActionType.ACTION_NONE || action == UpdateActionType.ACTION_REMOVE) {
			return false;
		}

		logger.info("Download Update: Source: '{}', Filename: '{}'", sourceURL, target);

		String encodedURL = HTTPUtil.encodeURL(sourceURL);
		try (CloseableHttpClient client = proxyManager.getHTTPClient()) {
			HttpGet method = new HttpGet(encodedURL);
			Path tempTargetFile = Paths.get(target);

			Path tempTargetFolder = tempTargetFile.toAbsolutePath().getParent();
			if (tempTargetFolder != null) {
				Files.createDirectories(tempTargetFolder);
			}

			return client.execute(method, response -> {
				StatusLine statusLine = new StatusLine(response);
				int statusCode = statusLine.getStatusCode();

				if (statusCode != 200) {
					throw new UpdateIOException("HTTP-Error: " + statusCode + " " + statusLine.getReasonPhrase());
				}

				try (@SuppressWarnings("resource")
				InputStream in = response.getEntity().getContent()) {
					try (FileOutputStream out = new FileOutputStream(target)) {
						int read;
						byte[] buf = new byte[8192];
						while ((read = in.read(buf)) != -1) {
							out.write(buf, 0, read);
							out.flush();
						}
					}
				}
				return true;
			});
		} catch (IOException e) {
			throw new UpdateException("Download Update failed: " + sourceURL, e);
		}
	}
}

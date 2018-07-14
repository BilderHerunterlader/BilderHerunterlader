package ch.supertomcat.bh.update.sources.httpxml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.update.UpdateException;
import ch.supertomcat.bh.update.containers.UpdateObject.UpdateActionType;
import ch.supertomcat.bh.update.containers.UpdateObject.UpdateType;
import ch.supertomcat.bh.update.containers.UpdateSourceFile;
import ch.supertomcat.supertomcattools.httptools.HTTPTool;

/**
 * 
 */
public class HTTPUpdateSourceFile extends UpdateSourceFile {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	private final String sourceURL;

	private final String targetFilename;

	/**
	 * Constructor
	 * 
	 * @param sourceURL Source URL
	 * @param targetFilename Target Filename
	 * @param delete True if is delete update, false otherwise
	 */
	public HTTPUpdateSourceFile(String sourceURL, String targetFilename, boolean delete) {
		super(delete);
		this.sourceURL = sourceURL;
		this.targetFilename = targetFilename;
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

		logger.info("Download Update: Source: '" + sourceURL + "', Filename: '" + target + "'");

		String encodedURL = HTTPTool.encodeURL(sourceURL);
		HttpGet method = null;
		try (CloseableHttpClient client = ProxyManager.instance().getHTTPClient()) {
			method = new HttpGet(encodedURL);
			File tempTargetFile = new File(target);

			File tempTargetFolder = tempTargetFile.getAbsoluteFile().getParentFile();
			if (tempTargetFolder != null) {
				Files.createDirectories(tempTargetFolder.toPath());
			}

			try (CloseableHttpResponse response = client.execute(method)) {
				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode != 200) {
					method.abort();
					throw new IOException("HTTP-Error: " + statusCode + " " + response.getStatusLine().getReasonPhrase());
				}

				try (InputStream in = response.getEntity().getContent()) {
					try (FileOutputStream out = new FileOutputStream(target)) {
						int read;
						byte[] buf = new byte[8192];
						while ((read = in.read(buf)) != -1) {
							out.write(buf, 0, read);
							out.flush();
						}
					}
					EntityUtils.consume(response.getEntity());
				}
				return true;
			}
		} catch (IOException e) {
			throw new UpdateException("Download Update failed: " + sourceURL, e);
		} finally {
			if (method != null) {
				method.abort();
			}
		}
	}
}

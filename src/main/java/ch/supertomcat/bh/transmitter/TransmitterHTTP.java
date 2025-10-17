package ch.supertomcat.bh.transmitter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.http.util.ContentType;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a HTTP server, which accepts connections from browsers
 */
public class TransmitterHTTP extends HttpServer {
	/**
	 * Pattern for content type
	 */
	private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile("; *charset=([^;]+)$");

	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Flag if connections are accepted or not
	 */
	private boolean acceptConnections = true;

	/**
	 * Transmitter Helper
	 */
	private final TransmitterHelper transmitterHelper;

	/**
	 * Network Listener
	 */
	private final NetworkListener networkListener;

	/**
	 * Constructor
	 * 
	 * @param port Port
	 * @param transmitterHelper Transmitter Helper
	 */
	public TransmitterHTTP(int port, TransmitterHelper transmitterHelper) {
		this.transmitterHelper = transmitterHelper;

		ServerConfiguration config = getServerConfiguration();
		config.addHttpHandler(new TransmitterHttpHandler(), "/");

		networkListener = new NetworkListener("BH-Transmitter-HTTP", "localhost", port);
		addListener(networkListener);
	}

	/**
	 * @return Port
	 */
	public int getPort() {
		return networkListener.getPort();
	}

	/**
	 * Returns the acceptConnections
	 * 
	 * @return acceptConnections
	 */
	public boolean isAcceptConnections() {
		return acceptConnections;
	}

	/**
	 * Sets the acceptConnections
	 * 
	 * @param acceptConnections acceptConnections
	 */
	public void setAcceptConnections(boolean acceptConnections) {
		this.acceptConnections = acceptConnections;
	}

	/**
	 * Http Handler
	 */
	private class TransmitterHttpHandler extends HttpHandler {
		@SuppressWarnings("resource")
		@Override
		public void service(Request request, Response response) throws Exception {
			logger.info("Received request: URI: {}, Method: {}", request.getRequestURI(), request.getMethod());
			if (!"/BH/DownloadFiles".equals(request.getRequestURI())) {
				logger.info("Send 404 Not Found");
				response.sendError(HttpStatus.NOT_FOUND_404.getStatusCode(), "Resource not found");
				return;
			} else if (!request.getMethod().matchesMethod("POST")) {
				logger.info("Send 405 Method not allowed");
				response.sendError(HttpStatus.METHOD_NOT_ALLOWED_405.getStatusCode(), "Method not allowed");
				return;
			} else if (!acceptConnections) {
				logger.info("Send 503 Service not available");
				response.sendError(HttpStatus.SERVICE_UNAVAILABLE_503.getStatusCode(), "BH currently does not accept connections, try later");
				return;
			}

			Charset encoding = StandardCharsets.ISO_8859_1;
			String contentType = request.getHeader(Header.ContentType);
			logger.info("Content-Type Header: {}", contentType);
			if (contentType != null) {
				Matcher matcher = CONTENT_TYPE_PATTERN.matcher(contentType);
				if (matcher.find()) {
					String strEncoding = matcher.group(1);
					try {
						encoding = Charset.forName(strEncoding);
					} catch (IllegalArgumentException e) {
						logger.error("Could not find charset: '{}'", strEncoding, e);
					}
				}
			}
			logger.info("Response Encoding: {}", encoding);

			boolean result = transmitterHelper.parseTransmitterInput(request.getInputStream(), encoding);
			if (!result) {
				logger.info("Send 400 Bad Request");
				response.sendError(HttpStatus.BAD_REQUEST_400.getStatusCode(), "URLs could not be parsed");
				return;
			}

			logger.info("Send success reponse");
			response.setStatus(HttpStatus.OK_200);
			response.setContentType(ContentType.newContentType("text/plain", encoding.name()));
			response.getWriter().write("URLs received");
		}
	}
}

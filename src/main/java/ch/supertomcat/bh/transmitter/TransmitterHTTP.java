package ch.supertomcat.bh.transmitter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.iki.elonen.NanoHTTPD;

/**
 * This class is a HTTP server, which accepts connections from browsers
 */
public class TransmitterHTTP extends NanoHTTPD {
	/**
	 * Pattern for content type
	 */
	private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile(";[ ]*charset=([^;]+)$");

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
	 * Constructor
	 * 
	 * @param port Port
	 * @param transmitterHelper Transmitter Helper
	 */
	public TransmitterHTTP(int port, TransmitterHelper transmitterHelper) {
		super("localhost", port);
		this.transmitterHelper = transmitterHelper;
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

	@Override
	public Response serve(IHTTPSession session) {
		if (!"/BH/DownloadFiles".equals(session.getUri())) {
			return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Resource not found");
		} else if (!Method.POST.equals(session.getMethod())) {
			return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Method not allowed");
		} else if (!acceptConnections) {
			return newFixedLengthResponse(NanoHTTPD.Response.Status.SERVICE_UNAVAILABLE, "text/plain", "BH currently does not accept connections, try later");
		}

		Charset encoding = StandardCharsets.ISO_8859_1;
		String contentType = session.getHeaders().get("Content-Type");
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

		boolean result = transmitterHelper.parseTransmitterInput(session.getInputStream(), encoding);
		if (result) {
			return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", "URLs received");
		} else {
			return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", "URLs could not be parsed");
		}
	}
}

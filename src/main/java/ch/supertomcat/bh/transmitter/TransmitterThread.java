package ch.supertomcat.bh.transmitter;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread for reading the data from a browser-extension/plugin
 * The threads are started by the TransmitterSocket
 * 
 * @see ch.supertomcat.bh.transmitter.TransmitterSocket
 */
public class TransmitterThread extends Thread {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Socket
	 */
	private final Socket socket;

	/**
	 * Transmitter Helper
	 */
	private final TransmitterHelper transmitterHelper;

	/**
	 * Constructor
	 * 
	 * @param socket Socket
	 * @param transmitterHelper Transmitter Helper
	 */
	public TransmitterThread(Socket socket, TransmitterHelper transmitterHelper) {
		this.socket = socket;
		this.transmitterHelper = transmitterHelper;
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {
		super.run();

		logger.info("Handling Socket Connection at " + socket.getLocalAddress() + ":" + socket.getLocalPort() + " from " + socket.getInetAddress() + ":" + socket.getPort());

		/*
		 * There are 3 ways, how URLs could be transferred to BH.
		 * The first one is to send all the URLs by the stream (The Firefox-Extension does this)
		 * The second one is that the plugin/extension writes the URLs to a file and send
		 * the path to file by the stream (The IE-Plugin does this)
		 * The third one is that the plugin/extension sends only the URL which contains the URLs
		 * by the stream (The Opera-Plugin does this)
		 */
		try {
			transmitterHelper.parseTransmitterInput(new DataInputStream(socket.getInputStream()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("Could not handle connection", e);
		} finally {
			// Close the socket
			closeSocket();
			logger.info("Socket closed");
		}
	}

	/**
	 * Closes the socket
	 * 
	 * @return True if successful
	 */
	private boolean closeSocket() {
		try {
			// Close the socket
			socket.close();
			return true;
		} catch (IOException e) {
			logger.error("Socket could not be closed", e);
			return false;
		}
	}
}

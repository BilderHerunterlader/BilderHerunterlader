package ch.supertomcat.bh.transmitter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * This class is a server socket, which accepts connections from browsers
 */
public class TransmitterSocket implements Runnable {
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
	 * Stop Flag
	 */
	private boolean stop = false;

	/**
	 * Constructor
	 * 
	 * @param transmitterHelper Transmitter Helper
	 */
	public TransmitterSocket(TransmitterHelper transmitterHelper) {
		this.transmitterHelper = transmitterHelper;
	}

	@Override
	public void run() {
		// Open a socket
		try (ServerSocket ss = new ServerSocket(0, 20, InetAddress.getByName("127.0.0.1"))) {
			int port = ss.getLocalPort(); // get the port of the socket
			logger.info("Listening on Port {}", port);
			writePortFile(port); // write the port number to a file (for browserextensions)

			while (!stop) {
				try {
					// wait for incomming connection
					@SuppressWarnings("resource")
					Socket socket = ss.accept();
					logger.info("Accepted connection at {}:{} from {}:{}", socket.getLocalAddress(), socket.getLocalPort(), socket.getInetAddress(), socket.getPort());
					if (!stop && acceptConnections) {
						// Start new thread, which will read data from the stream
						TransmitterThread t = new TransmitterThread(socket, transmitterHelper);
						t.setName("TransmitterReader-" + t.getId());
						logger.info("Handle connection by thread: {}", t.getName());
						t.start();
					} else {
						logger.warn("Close connection, because acceptConnections Flag is false");
						try {
							socket.close();
						} catch (IOException e) {
							logger.error("Could not close socket", e);
						}
					}
				} catch (IOException e) {
					logger.error("Could not handle incoming connection", e);
				}
			}
		} catch (IOException e) {
			logger.error("Could not create Server Socket", e);
		}
	}

	/**
	 * Write the Port-Number to a file for Browser-Extensions
	 * 
	 * @param port Port-Number
	 */
	private void writePortFile(int port) {
		String filename = System.getProperty("user.home") + FileUtil.FILE_SEPERATOR + ".BH" + FileUtil.FILE_SEPERATOR + "port.txt";
		File file = new File(filename);
		try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
			// Write the port to the file
			out.write(String.valueOf(port));
			// Close the file
			out.flush();
		} catch (IOException e) {
			logger.error("Could not write port file: {}", file.getAbsolutePath(), e);
		}
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
	 * Stop
	 */
	public void stop() {
		stop = true;
	}
}

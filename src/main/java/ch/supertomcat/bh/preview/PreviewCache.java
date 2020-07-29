package ch.supertomcat.bh.preview;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preview Cache
 */
public class PreviewCache {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	private List<PreviewCacheListener> listeners = new CopyOnWriteArrayList<>();

	private static PreviewDiskCache diskCache = new PreviewDiskCache();

	/**
	 * Image Format
	 */
	private final String imageFormat;

	/**
	 * Constructor
	 */
	public PreviewCache() {
		// Find a format, which can be read and written, because not all JVMs have codecs for all formats
		List<String> readerFormats = Arrays.asList(ImageIO.getReaderFormatNames());
		List<String> writerFormats = Arrays.asList(ImageIO.getWriterFormatNames());

		if (readerFormats.contains("jpg") && writerFormats.contains("jpg")) {
			imageFormat = "jpg";
		} else if (readerFormats.contains("png") && writerFormats.contains("png")) {
			imageFormat = "png";
		} else {
			imageFormat = null;
		}
	}

	/**
	 * @param url URL
	 * @param preview Preview
	 */
	public void addPreview(String url, BufferedImage preview) {
		if (imageFormat == null) {
			return;
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			if (!ImageIO.write(preview, imageFormat, baos)) {
				logger.error("Could not store preview: ImageIO.write returned false");
				return;
			}
			byte[] data = baos.toByteArray();
			diskCache.addPreview(url, data);
		} catch (IOException e) {
			logger.error("Could not store preview", e);
		}
	}

	/**
	 * All Previews Added
	 */
	public void notifyAllPreviewsAdded() {
		for (PreviewCacheListener listener : listeners) {
			listener.allPreviewsAdded();
		}
	}

	/**
	 * @param url URL
	 * @return Preview or null if not in cache
	 */
	public BufferedImage getPreview(String url) {
		byte[] data = diskCache.getPreview(url);
		if (data == null) {
			return null;
		}

		try {
			return ImageIO.read(new ByteArrayInputStream(data));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * @param listener Listener
	 */
	public void addListener(PreviewCacheListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * @param listener Listener
	 */
	public void removeListener(PreviewCacheListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Listener for preview cache
	 */
	public static interface PreviewCacheListener {
		/**
		 * All Previews Added
		 */
		public void allPreviewsAdded();
	}
}

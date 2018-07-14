package ch.supertomcat.bh.preview;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class PreviewCache {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	private List<PreviewCacheListener> listeners = new CopyOnWriteArrayList<>();

	private PreviewDiskCache diskCache = new PreviewDiskCache();

	/**
	 * Constructor
	 */
	public PreviewCache() {
	}

	/**
	 * @param url URL
	 * @param preview Preview
	 */
	public void addPreview(String url, BufferedImage preview) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(preview, "jpg", baos);
			baos.flush();
			byte[] data = baos.toByteArray();
			baos.close();
			diskCache.addPreview(url, data);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
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

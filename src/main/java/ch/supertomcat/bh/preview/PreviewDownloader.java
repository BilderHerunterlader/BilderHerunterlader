package ch.supertomcat.bh.preview;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

import ch.supertomcat.supertomcatutils.image.ImageUtil;

/**
 * Class for downloading previews
 */
public class PreviewDownloader implements Callable<PreviewDownloader.DownloadedPreview> {
	private int index = 0;
	private String url = null;
	int previewHeight = 100;
	private int connectTimeout = 1000;
	private int readTimeout = 1000;

	/**
	 * Constructor
	 * 
	 * @param index Index
	 * @param url URL
	 * @param previewHeight Preview Height
	 */
	public PreviewDownloader(int index, String url, int previewHeight) {
		this.index = index;
		this.url = url;
		this.previewHeight = previewHeight;
	}

	/**
	 * Constructor
	 * 
	 * @param index Index
	 * @param url URL
	 * @param previewHeight Preview Height
	 * @param connectTimeout Connect Timeout
	 * @param readTimeout Read Timeout
	 */
	public PreviewDownloader(int index, String url, int previewHeight, int connectTimeout, int readTimeout) {
		this.index = index;
		this.url = url;
		this.previewHeight = previewHeight;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public DownloadedPreview call() throws Exception {
		if (url == null) {
			return new DownloadedPreview(index, null, null);
		}
		BufferedImage imgPreview = ImageUtil.downloadImage(url, null, connectTimeout, readTimeout, false);
		if (imgPreview != null) {
			imgPreview = ImageUtil.generatePreviewImage(imgPreview, -1, previewHeight);
		}
		return new DownloadedPreview(index, url, imgPreview);
	}

	/**
	 * 
	 */
	public static class DownloadedPreview {
		private int index = 0;
		private String url = null;
		private BufferedImage preview = null;

		/**
		 * Constructor
		 * 
		 * @param index Index
		 * @param url URL
		 * @param preview Preview
		 */
		public DownloadedPreview(int index, String url, BufferedImage preview) {
			this.index = index;
			this.url = url;
			this.preview = preview;
		}

		/**
		 * Returns the index
		 * 
		 * @return index
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * Returns the url
		 * 
		 * @return url
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * Returns the preview
		 * 
		 * @return preview
		 */
		public BufferedImage getPreview() {
			return preview;
		}
	}
}

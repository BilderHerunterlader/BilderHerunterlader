package ch.supertomcat.bh.pic;

/**
 * Pic Progress
 */
public class PicProgress {
	/**
	 * Bytes Total
	 */
	private long bytesTotal = 0;

	/**
	 * Bytes Downloaded
	 */
	private long bytesDownloaded = 0;

	/**
	 * Percent
	 */
	private float percent = 0;

	/**
	 * Percent as int
	 */
	private int percentInt = 0;

	/**
	 * Upload Rate
	 */
	private double rate = 0;

	/**
	 * Current URL Index
	 */
	private int currentURLIndex = 1;

	/**
	 * URL Count
	 */
	private int urlCount = 1;

	/**
	 * Constructor
	 */
	public PicProgress() {
	}

	/**
	 * Returns the bytesTotal
	 * 
	 * @return bytesTotal
	 */
	public long getBytesTotal() {
		return bytesTotal;
	}

	/**
	 * Sets the bytesTotal
	 * 
	 * @param bytesTotal bytesTotal
	 */
	public void setBytesTotal(long bytesTotal) {
		this.bytesTotal = bytesTotal;
		calculatePercent();
	}

	/**
	 * Returns the bytesUploaded
	 * 
	 * @return bytesUploaded
	 */
	public long getBytesDownloaded() {
		return bytesDownloaded;
	}

	/**
	 * Sets the bytesDownloaded
	 * 
	 * @param bytesDownloaded Bytes Downloaded
	 */
	public void setBytesDownloaded(long bytesDownloaded) {
		this.bytesDownloaded = bytesDownloaded;
		calculatePercent();
	}

	/**
	 * Returns the percent
	 * 
	 * @return percent
	 */
	public float getPercent() {
		return percent;
	}

	/**
	 * Returns the percentInt
	 * 
	 * @return percentInt
	 */
	public int getPercentInt() {
		return percentInt;
	}

	/**
	 * Calculate percent
	 */
	private void calculatePercent() {
		if (bytesDownloaded <= 0 || bytesTotal <= 0) {
			percent = 0;
			percentInt = 0;
		} else {
			percent = ((float)bytesDownloaded / bytesTotal) * 100;
			percentInt = (int)percent;
		}
	}

	/**
	 * Returns the rate
	 * 
	 * @return rate
	 */
	public double getRate() {
		return rate;
	}

	/**
	 * Sets the rate
	 * 
	 * @param rate rate
	 */
	public void setRate(double rate) {
		this.rate = rate;
	}

	/**
	 * Returns the currentURLIndex
	 * 
	 * @return currentURLIndex
	 */
	public int getCurrentURLIndex() {
		return currentURLIndex;
	}

	/**
	 * Sets the currentURLIndex
	 * 
	 * @param currentURLIndex currentURLIndex
	 */
	public void setCurrentURLIndex(int currentURLIndex) {
		this.currentURLIndex = currentURLIndex;
	}

	/**
	 * Returns the urlCount
	 * 
	 * @return urlCount
	 */
	public int getUrlCount() {
		return urlCount;
	}

	/**
	 * Sets the urlCount
	 * 
	 * @param urlCount urlCount
	 */
	public void setUrlCount(int urlCount) {
		this.urlCount = urlCount;
	}
}

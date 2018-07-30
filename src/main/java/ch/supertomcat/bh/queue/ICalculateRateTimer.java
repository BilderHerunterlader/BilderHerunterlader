package ch.supertomcat.bh.queue;

/**
 * Interface for download rate calculation
 */
public interface ICalculateRateTimer {
	/**
	 * @param downloadRate Download Rate
	 */
	public void totalDownloadRateCalculated(double downloadRate);
}

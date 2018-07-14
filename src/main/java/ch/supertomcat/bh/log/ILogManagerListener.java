package ch.supertomcat.bh.log;

/**
 * 
 *
 */
public interface ILogManagerListener {
	/**
	 * Log changed
	 */
	public void logChanged();
	
	/**
	 * Current logfile changed
	 */
	public void currentLogFileChanged();
}

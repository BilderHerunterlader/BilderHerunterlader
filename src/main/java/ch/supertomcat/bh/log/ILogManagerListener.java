package ch.supertomcat.bh.log;

/**
 * Log Manager Listener
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

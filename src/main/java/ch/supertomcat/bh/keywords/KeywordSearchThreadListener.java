package ch.supertomcat.bh.keywords;

import java.util.EventListener;

/**
 * Listener for KeywordSearchThreads
 */
public interface KeywordSearchThreadListener extends EventListener {
	/**
	 * Search done
	 * 
	 * @param retval Keywords
	 */
	public void searchDone(Keyword[] retval);

	/**
	 * Progressbar changed
	 * 
	 * @param min Minimum
	 * @param max Maximum
	 * @param val Value
	 */
	public void progressBarChanged(int min, int max, int val);

	/**
	 * Progressbar changed
	 * 
	 * @param val Value
	 */
	public void progressBarChanged(int val);

	/**
	 * Progressbar status changed
	 * 
	 * @param enabled Enabled
	 */
	public void progressBarStatusChanged(boolean enabled);
}

package ch.supertomcat.bh.hoster;

import java.util.List;

import ch.supertomcat.bh.pic.URL;

/**
 * Listener for the Adder-Panel to detect when the URLs
 * are parsed or to update the Progressbar.
 */
public interface IHostURLCheckerListener {
	/**
	 * URLs are parsed
	 * 
	 * @param urls URLs The parsed URLs
	 */
	public void linksChecked(List<URL> urls);

	/**
	 * Update the progressbar with a new value
	 * 
	 * @param val Value
	 */
	public void progressChanged(int val);

	/**
	 * Update the progressbar with new min and max values
	 * 
	 * @param min Minimum
	 * @param max Maximum
	 * @param val Value
	 */
	public void progressChanged(int min, int max, int val);

	/**
	 * Update the text of the progressbar
	 * 
	 * @param text Progressbar-Text
	 */
	public void progressChanged(String text);
}

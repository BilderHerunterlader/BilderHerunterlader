package ch.supertomcat.bh.clipboard;

import java.util.List;

/**
 * Listener for ClipboardObserver
 */
public interface ClipboardObserverListener {
	/**
	 * Called when links are detected in Clipboard
	 * 
	 * @param links Links
	 */
	public void linksDetected(List<String> links);
}

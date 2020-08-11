package ch.supertomcat.bh.pic;

import java.util.EventListener;

/**
 * Listener to provide information about size, target path and filename and so on
 */
public interface IPicListener extends EventListener {
	/**
	 * ProgressBar has changed
	 * 
	 * @param pic Pic
	 */
	public void progressChanged(Pic pic);

	/**
	 * Filesize has changed
	 * 
	 * @param pic Pic
	 */
	public void sizeChanged(Pic pic);

	/**
	 * Target has changed
	 * 
	 * @param pic Pic
	 */
	public void targetChanged(Pic pic);

	/**
	 * Status has changed
	 * 
	 * @param pic Pic
	 */
	public void statusChanged(Pic pic);

	/**
	 * Download is deactivated
	 * 
	 * @param pic Pic
	 */
	public void deactivatedChanged(Pic pic);

	/**
	 * Hoster has changed
	 * 
	 * @param pic Pic
	 */
	public void hosterChanged(Pic pic);

	/**
	 * Download URL has changed
	 * 
	 * @param pic Pic
	 */
	public void downloadURLChanged(Pic pic);
}

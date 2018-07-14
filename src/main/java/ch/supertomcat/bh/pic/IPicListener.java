package ch.supertomcat.bh.pic;

import java.util.EventListener;

/**
 * Listener to provide information about size, target path and filename and so on
 */
public interface IPicListener extends EventListener{
	/**
	 * ProgressBar has changed
	 * @param pic Pic
	 * @param min Minimum
	 * @param max Maximum
	 * @param val Value
	 * @param s Text
	 * @param errMsg Error-Message
	 */
	public void progressBarChanged(Pic pic, int min, int max, int val, String s, String errMsg);
	
	/**
	 * Filesize has changed
	 * @param pic Pic
	 */
	public void sizeChanged(Pic pic);
	
	/**
	 * Target has changed
	 * @param pic Pic
	 */
	public void targetChanged(Pic pic);
	
	/**
	 * Status has changed
	 * @param pic Pic
	 */
	public void statusChanged(Pic pic);
	
	/**
	 * Download is deactivated
	 * @param pic Pic
	 */
	public void deactivatedChanged(Pic pic);
}

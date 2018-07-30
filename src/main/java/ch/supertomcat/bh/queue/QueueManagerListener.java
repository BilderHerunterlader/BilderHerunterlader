package ch.supertomcat.bh.queue;

import java.util.List;

import ch.supertomcat.bh.pic.Pic;

/**
 * Listener for QueueManager
 */
public interface QueueManagerListener {
	/**
	 * @param pic Pic
	 */
	public void picAdded(Pic pic);

	/**
	 * @param pics Pics
	 */
	public void picsAdded(List<Pic> pics);

	/**
	 * @param pic Pic
	 * @param index Index
	 */
	public void picRemoved(Pic pic, int index);

	/**
	 * @param removedIndeces Removed Indeces
	 */
	public void picsRemoved(int removedIndeces[]);

	/**
	 * ProgressBar has changed
	 * 
	 * @param pic Pic
	 * @param min Minimum
	 * @param max Maximum
	 * @param val Value
	 * @param s Text
	 * @param errMsg Error-Message
	 * @param index Index
	 */
	public void picProgressBarChanged(Pic pic, int min, int max, int val, String s, String errMsg, int index);

	/**
	 * Filesize has changed
	 * 
	 * @param pic Pic
	 * @param index Index
	 */
	public void picSizeChanged(Pic pic, int index);

	/**
	 * Target has changed
	 * 
	 * @param pic Pic
	 * @param index Index
	 */
	public void picTargetChanged(Pic pic, int index);

	/**
	 * Status has changed
	 * 
	 * @param pic Pic
	 * @param index Index
	 */
	public void picStatusChanged(Pic pic, int index);

	/**
	 * Download is deactivated
	 * 
	 * @param pic Pic
	 * @param index Index
	 */
	public void picDeactivatedChanged(Pic pic, int index);
}

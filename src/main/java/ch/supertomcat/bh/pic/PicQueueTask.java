package ch.supertomcat.bh.pic;

import ch.supertomcat.supertomcatutils.queue.QueueTaskBase;

/**
 * Pic Queue Task
 */
public class PicQueueTask extends QueueTaskBase<PicDownloadListener, PicDownloadResult> {
	/**
	 * Constructor
	 * 
	 * @param picDownloadListener Pic Download Listener
	 */
	public PicQueueTask(PicDownloadListener picDownloadListener) {
		super(picDownloadListener);
	}

	@Override
	public PicDownloadResult call() throws Exception {
		task.download();
		return new PicDownloadResult();
	}
}

package ch.supertomcat.bh.queue;

import java.io.Serializable;
import java.util.Comparator;

import ch.supertomcat.bh.pic.Pic;

/**
 * This class is for sorting of the download-queue
 */
public class DownloadComparator implements Comparator<Pic>, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Do not Sort
	 */
	public static final int NO_SORT = 4;

	/**
	 * Sort first by container url, then by directory
	 */
	public static final int BY_BOTH_CONTAINER_URL_FIRST = 0;

	/**
	 * Sort first by directory, then by container url
	 */
	public static final int BY_BOTH_DIRECTORY_FIRST = 1;

	/**
	 * Sort only by container url
	 */
	public static final int BY_CONTAINER_URL_ONLY = 2;

	/**
	 * Sort only by directory
	 */
	public static final int BY_TARGET_DIRECTORY_ONLY = 3;

	/**
	 * Sort only by directory
	 */
	public static final int BY_DATE_TIME_ADDED_ONLY = 5;

	/**
	 * How to sort
	 */
	private int compareBy = BY_DATE_TIME_ADDED_ONLY;

	/**
	 * Constructor
	 * 
	 * @param compareBy How to sort
	 */
	public DownloadComparator(int compareBy) {
		this.compareBy = compareBy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Pic p1, Pic p2) {
		int comp = 0;
		// compare
		switch (compareBy) {

			case BY_CONTAINER_URL_ONLY:
				comp = p1.getContainerURL().compareTo(p2.getContainerURL());
				break;
			case BY_TARGET_DIRECTORY_ONLY:
				comp = p1.getTargetPath().compareTo(p2.getTargetPath());
				break;
			case BY_BOTH_CONTAINER_URL_FIRST:
				comp = p1.getContainerURL().compareTo(p2.getContainerURL());
				if (comp == 0) {
					comp = p1.getTargetPath().compareTo(p2.getTargetPath());
				}
				break;
			case BY_BOTH_DIRECTORY_FIRST:
				comp = p1.getTargetPath().compareTo(p2.getTargetPath());
				if (comp == 0) {
					comp = p1.getContainerURL().compareTo(p2.getContainerURL());
				}
				break;
			case BY_DATE_TIME_ADDED_ONLY:
				comp = ((Long)p1.getDateTimeSimple()).compareTo(p2.getDateTimeSimple());
				break;
			case NO_SORT:
				comp = 0;
				break;
			default:
				comp = ((Long)p1.getDateTimeSimple()).compareTo(p2.getDateTimeSimple());
				break;
		}
		return comp;
	}
}

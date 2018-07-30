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
	 * How to sort
	 */
	private final DownloadCompareType compareBy;

	/**
	 * Constructor
	 * 
	 * @param compareBy How to sort
	 */
	public DownloadComparator(DownloadCompareType compareBy) {
		this.compareBy = compareBy;
	}

	@Override
	public int compare(Pic p1, Pic p2) {
		switch (compareBy) {
			case BY_CONTAINER_URL_ONLY:
				return p1.getContainerURL().compareTo(p2.getContainerURL());
			case BY_TARGET_DIRECTORY_ONLY:
				return p1.getTargetPath().compareTo(p2.getTargetPath());
			case BY_BOTH_CONTAINER_URL_FIRST:
				int compContainerURLFirst = p1.getContainerURL().compareTo(p2.getContainerURL());
				if (compContainerURLFirst == 0) {
					return p1.getTargetPath().compareTo(p2.getTargetPath());
				}
				return compContainerURLFirst;
			case BY_BOTH_DIRECTORY_FIRST:
				int compDirectoryFirst = p1.getTargetPath().compareTo(p2.getTargetPath());
				if (compDirectoryFirst == 0) {
					return p1.getContainerURL().compareTo(p2.getContainerURL());
				}
				return compDirectoryFirst;
			case BY_DATE_TIME_ADDED_ONLY:
				return Long.compare(p1.getDateTimeSimple(), p2.getDateTimeSimple());
			case NO_SORT:
				return 0;
			default:
				return Long.compare(p1.getDateTimeSimple(), p2.getDateTimeSimple());
		}
	}
}

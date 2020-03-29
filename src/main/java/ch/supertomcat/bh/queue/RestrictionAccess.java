package ch.supertomcat.bh.queue;

/**
 * Access to restrictions
 */
public interface RestrictionAccess {
	/**
	 * Adds a restriction
	 * If there is already a restriction for a domain the restriction
	 * is not added. But the value of maximum simultanious downloads from
	 * the restriction to add is taken and set to the existing restriction.
	 * So this method can be used to update a restriction
	 * 
	 * @param restriction Restriction
	 */
	public void addRestriction(DownloadRestriction restriction);

	/**
	 * Removes a restriction
	 * 
	 * @param restriction Restriction
	 */
	public void removeRestriction(DownloadRestriction restriction);
}

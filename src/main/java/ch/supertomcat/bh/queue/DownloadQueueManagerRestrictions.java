package ch.supertomcat.bh.queue;

import java.util.ArrayList;
import java.util.List;

/**
 * Access to restrictions
 */
public class DownloadQueueManagerRestrictions implements RestrictionAccess {
	/**
	 * Array containing the restrictions
	 */
	private final List<Restriction> restrictions = new ArrayList<>();

	/**
	 * Constructor
	 */
	public DownloadQueueManagerRestrictions() {
	}

	/**
	 * Returns the restrictions
	 * 
	 * @return restrictions
	 */
	public List<Restriction> getRestrictions() {
		return restrictions;
	}

	/**
	 * Returns the restriction for a domain
	 * If there is no restriction for a domain null is returned
	 * 
	 * @param domain Domain
	 * @return Restriction
	 */
	public Restriction getRestrictionForDomain(String domain) {
		if (domain.isEmpty()) {
			return null;
		}
		for (Restriction restriction : restrictions) {
			if (restriction.isDomainRestricted(domain)) {
				return restriction;
			}
		}
		return null;
	}

	@Override
	public synchronized void addRestriction(Restriction restriction) {
		for (int i = 0; i < restrictions.size(); i++) {
			if (restrictions.get(i).equals(restriction)) {
				restrictions.get(i).setMaxSimultaneousDownloads(restriction.getMaxSimultaneousDownloads());
				return;
			}
		}
		restrictions.add(restriction);
	}

	@Override
	public synchronized void removeRestriction(Restriction restriction) {
		restrictions.remove(restriction);
	}
}

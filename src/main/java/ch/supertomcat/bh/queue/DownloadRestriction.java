package ch.supertomcat.bh.queue;

import java.util.ArrayList;
import java.util.List;

import ch.supertomcat.supertomcatutils.queue.RestrictionBase;

/**
 * This class is to set a limit of simultanious downloads for a
 * domain.
 * An example:
 * The hostclass for rapidshare sets a restriction of one
 * simultanious downloads when the hostclass is set to free-user.
 * Because rapidshare does not allow more than one simultanious
 * downloads for free-users.
 * So when a rapidshare download is started, the counter is increased.
 * Now if a other rapidshare downloads requested a slot, QueueData checks
 * what the restriction for rapidshare is. Then it checks if there are already
 * too many connections for rapidshare, and so it lets the next rapidshare
 * download only start, when the one downloading at the moment has finished.
 * 
 * One domain must be set, but it is possible to set multiple domains.
 * So the restriction takes care of all domains set.
 * 
 * The equals- and compareTo-methods take only care about the single domain
 * or first domain in the vector!
 * But the equals-method check the whole vector if a string should be compared.
 * 
 * @see ch.supertomcat.bh.queue.DownloadQueueManager
 */
public class DownloadRestriction extends RestrictionBase implements Comparable<DownloadRestriction> {
	/**
	 * Domain
	 */
	private final String domain;

	/**
	 * Domains
	 */
	private final List<String> domains = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param domain Domain
	 * @param maxSimultaneousDownloads Maximum
	 */
	public DownloadRestriction(String domain, int maxSimultaneousDownloads) {
		super(domain, maxSimultaneousDownloads);
		if (domain == null || domain.isEmpty()) {
			throw new IllegalArgumentException("domain is null or empty: " + domain);
		}

		this.domain = domain;
		this.domains.add(domain);
	}

	/**
	 * Constructor
	 * 
	 * @param domains Domains
	 * @param maxSimultaneousDownloads Maximum
	 */
	public DownloadRestriction(List<String> domains, int maxSimultaneousDownloads) {
		super(domains.get(0), maxSimultaneousDownloads);

		this.domain = domains.get(0);
		for (String currentDomain : domains) {
			if (currentDomain == null || currentDomain.isEmpty()) {
				throw new IllegalArgumentException("currentDomain is null or empty: " + currentDomain);
			}
			this.domains.add(currentDomain);
		}
	}

	/**
	 * Returns the domain
	 * 
	 * @return the name
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * Returns the domains
	 * 
	 * @return domains
	 */
	public List<String> getDomains() {
		return new ArrayList<>(domains);
	}

	/**
	 * Returns the maximum of simultanious downloads for this domain
	 * 
	 * @return Maximum
	 */
	public int getMaxSimultaneousDownloads() {
		return getMaxConnectionCount();
	}

	/**
	 * Sets the maximum of simultanious downloads for this domain
	 * 
	 * @param maxSimultaneousDownloads Maximum
	 */
	public void setMaxSimultaneousDownloads(int maxSimultaneousDownloads) {
		if (maxSimultaneousDownloads >= 1) {
			setMaxConnectionCount(maxSimultaneousDownloads);
		}
	}

	/**
	 * Returns true if the given domain is restricted, false otherwise
	 * 
	 * @param otherDomain Domain
	 * @return True if the given domain is restricted, false otherwise
	 */
	public boolean isDomainRestricted(String otherDomain) {
		return domains.contains(otherDomain);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DownloadRestriction other = (DownloadRestriction)obj;
		if (domain == null) {
			if (other.domain != null) {
				return false;
			}
		} else if (!domain.equals(other.domain)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(DownloadRestriction o) {
		return domain.compareTo(o.getDomain());
	}
}

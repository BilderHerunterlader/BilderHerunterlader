package ch.supertomcat.bh.hoster.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.Hoster;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.rules.Rule;

/**
 * This class is used as arguments for parsing the urls
 */
public class URLParseObject {
	/**
	 * This array is for the future, if there would be additional information
	 * required on parsing urls. So they can be stored in this array.
	 * So in the future there are no changes of the class needed.
	 */
	private Map<String, Object> info = new HashMap<>();

	/**
	 * The parsed URL
	 */
	private URLParseObjectFile directLink = new URLParseObjectFile();

	/**
	 * Additional parsed URLs
	 */
	private List<URLParseObjectFile> additionalDirectLinks = new ArrayList<>();

	/**
	 * The container URLs
	 * Usually there is only one, but since rules can send its
	 * result again trough the rules/hostclasses and it has to be
	 * always the same UPO, the rules are just changing the
	 * container-URL of this object.
	 * For new possibilities like using the container-URLs as
	 * Referrer in rules, we need to store all of them.
	 * So setContainerURL will just add the url to the vector.
	 */
	private List<String> containerURLs = new ArrayList<>();

	/**
	 * The thumbnail URL
	 */
	private String thumbURL = "";

	/**
	 * Contains the Hosts/Rules which parsed this Object
	 */
	private List<Hoster> hosterStackTrace = new ArrayList<>();

	/**
	 * The pic which has started the parsing request
	 * I needed this for the rapidshare-host-class, in which i
	 * change the progressbar of the pic.
	 */
	private Pic pic = null;

	/**
	 * Constructor
	 * 
	 * @param containerURL Container-URL
	 * @param thumbURL Thumbnail-URL
	 * @param pic The Pic
	 */
	public URLParseObject(String containerURL, String thumbURL, Pic pic) {
		containerURLs.add(containerURL);
		this.thumbURL = thumbURL;
		this.pic = pic;
	}

	/**
	 * Add an Object to the info-array
	 * 
	 * @param name Key
	 * @param value Object
	 */
	public void addInfo(String name, Object value) {
		info.put(name, value);
	}

	/**
	 * Check if an object exists for a key
	 * 
	 * @param name Key
	 * @return TRUE/FALSE
	 */
	public boolean checkExistInfo(String name) {
		return info.containsKey(name);
	}

	/**
	 * Get the object for a key
	 * 
	 * @param name Key
	 * @return Object
	 */
	public Object getInfo(String name) {
		return info.get(name);
	}

	/**
	 * Returns the info
	 * 
	 * @return info
	 */
	public Map<String, Object> getInfos() {
		return new HashMap<>(info);
	}

	/**
	 * Returns the pic
	 * 
	 * @return the pic
	 */
	public Pic getPic() {
		return pic;
	}

	/**
	 * @return All Parsed URLs
	 */
	public List<URLParseObjectFile> getAllDirectLinks() {
		List<URLParseObjectFile> allDirectLinks = new ArrayList<>();
		allDirectLinks.add(directLink);
		allDirectLinks.addAll(additionalDirectLinks);
		return allDirectLinks;
	}

	/**
	 * Returns the parsed URL
	 * 
	 * @return the directLink
	 */
	public String getDirectLink() {
		return directLink.getDirectLink();
	}

	/**
	 * Set the parsed URL
	 * 
	 * @param directLink the directLink to set
	 */
	public void setDirectLink(String directLink) {
		this.directLink.setDirectLink(directLink);
	}

	/**
	 * Returns the corrected filename
	 * 
	 * @return the correctedFilename
	 */
	public String getCorrectedFilename() {
		return directLink.getCorrectedFilename();
	}

	/**
	 * Set the corrected filename
	 * 
	 * @param correctedFilename the correctedFilename to set
	 */
	public void setCorrectedFilename(String correctedFilename) {
		this.directLink.setCorrectedFilename(correctedFilename);
	}

	/**
	 * Returns the additionalDirectLinks
	 * 
	 * @return additionalDirectLinks
	 */
	public List<URLParseObjectFile> getAdditionalDirectLinks() {
		return additionalDirectLinks;
	}

	/**
	 * Sets the additionalDirectLinks
	 * 
	 * @param additionalDirectLinks additionalDirectLinks
	 */
	public void setAdditionalDirectLinks(List<URLParseObjectFile> additionalDirectLinks) {
		this.additionalDirectLinks = additionalDirectLinks;
	}

	/**
	 * Add additional direct link
	 * 
	 * @param additionalDirectLink Additional direct link
	 */
	public void addAdditionalDirectLink(URLParseObjectFile additionalDirectLink) {
		this.additionalDirectLinks.add(additionalDirectLink);
	}

	/**
	 * Returns the last Host which parsed the URL
	 * 
	 * @return the lastHost
	 */
	public synchronized Hoster getLastHoster() {
		try {
			return hosterStackTrace.get(hosterStackTrace.size() - 1);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the last Host which parsed the URL or null if there isn't any
	 * 
	 * @return the lastHost
	 */
	public synchronized Host getLastHost() {
		for (int i = hosterStackTrace.size() - 1; i > -1; i--) {
			if (hosterStackTrace.get(i) instanceof Host) {
				return (Host)hosterStackTrace.get(i);
			}
		}
		return null;
	}

	/**
	 * Returns the last Host which parsed the URL or null if there isn't any
	 * 
	 * @return the lastHost
	 */
	public synchronized Rule getLastRule() {
		for (int i = hosterStackTrace.size() - 1; i > -1; i--) {
			if (hosterStackTrace.get(i) instanceof Rule) {
				return (Rule)hosterStackTrace.get(i);
			}
		}
		return null;
	}

	/**
	 * Add the last Host which parsed the URL
	 * 
	 * @param hoster the lastHost to set
	 */
	public synchronized void addHoster(Hoster hoster) {
		hosterStackTrace.add(hoster);
	}

	/**
	 * Returns the HosterStackTrace
	 * 
	 * @return HosterStackTrace
	 */
	public synchronized String getHosterStackTrace() {
		StringBuilder sb = new StringBuilder();
		sb.append("Host-/Rule-Parser-Trace:\n  ContainerURLs:\n");
		for (int i = containerURLs.size() - 1; i > -1; i--) {
			sb.append("    " + this.containerURLs.get(i) + "\n");
		}
		sb.append("  Hoster/Rules:\n");
		for (int i = hosterStackTrace.size() - 1; i > -1; i--) {
			String strHoster = "";
			if (hosterStackTrace.get(i) instanceof Host) {
				Host host = (Host)hosterStackTrace.get(i);
				strHoster = host.getName() + " (" + host.getVersion() + ")";
			} else if (hosterStackTrace.get(i) instanceof Rule) {
				Rule rule = (Rule)hosterStackTrace.get(i);
				strHoster = rule.getName() + " (" + rule.getVersion() + ")";
			}
			sb.append("    " + strHoster + "\n");
		}
		return sb.toString();
	}

	/**
	 * Returns the array with the container-URLs.
	 * It returns just a copy, not the real internal array,
	 * because it should not be possible for outside objects
	 * changing the array.
	 * 
	 * @return Container-URLs
	 */
	public synchronized List<String> getContainerURLs() {
		return new ArrayList<>(containerURLs);
	}

	/**
	 * Returns the first container URL
	 * 
	 * @return the first containerURL
	 */
	public String getFirstContainerURL() {
		return containerURLs.get(0);
	}

	/**
	 * Returns the last container URL
	 * 
	 * @return the last containerURL
	 */
	public String getContainerURL() {
		return containerURLs.get(containerURLs.size() - 1);
	}

	/**
	 * Set the container URL
	 * 
	 * @param containerURL the containerURL to set
	 */
	public void setContainerURL(String containerURL) {
		containerURLs.add(containerURL);
	}

	/**
	 * Return the thumbnail URL
	 * 
	 * @return the thumbURL
	 */
	public String getThumbURL() {
		return thumbURL;
	}

	/**
	 * Set the thumbnail URL
	 * 
	 * @param thumbURL the thumbURL to set
	 */
	public void setThumbURL(String thumbURL) {
		this.thumbURL = thumbURL;
	}

	/**
	 * Method to detect if this object is in a loop
	 * when parsing. Loop means, that this object
	 * is parsed, by the same hostclass or rule
	 * over and over.
	 * This method is not yet implemented, i don't know
	 * how i will make this.
	 * There could also be a loop between to hostclasses.
	 * Example:
	 * HostClass1 parses the Object and let it parsed again by
	 * the hostmanager, now the url is parsed by HostClass2 and
	 * this class let it parse again by the hostmanager. Now the
	 * url matches again the HostClass1. And so on...
	 * But this could also be a required by a hostclass, to
	 * parse an object a few times by hisself.
	 * 
	 * @return True if object is parsed in a loop
	 */
	public boolean isLoop() {
		if (hosterStackTrace.size() > 50) {
			return true;
		}
		return false;
	}
}

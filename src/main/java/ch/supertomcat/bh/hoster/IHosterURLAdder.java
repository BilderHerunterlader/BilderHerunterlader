package ch.supertomcat.bh.hoster;

import java.util.List;

import ch.supertomcat.supertomcattools.guitools.progressmonitor.ProgressObserver;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.supertomcattools.settingstools.options.OptionBoolean;


/**
 * Interface which a host-classes can implement, but it is not needed.
 * But when a host-class has implemented this interface, then the
 * isFromThisHoster-Method of this IHoster is called first, and if it is
 * true returned then the isFromThisHoster-Method of this interface will also
 * called.
 * 
 * This interface can be used to add additional URLs to the
 * array. An example for this could be, to download all pictures from a thread
 * in forums which has more than one pages.
 * 
 * The other difference to IHoster is, that the URL-Object is the argument, not only
 * the url. And so the host-class could now also change the target-path at this point.
 * 
 * When additional URLs are added, then it could be required to not accept the 
 * Container-URL, so when the isFromThisHoster-Argument is set to false, then the
 * additional URLs are added, but the Container-URL is not accepted. However the
 * isFromThisHoster-Method from IHoster-Interface needs to return true, otherwise
 * this would not work.
 */
public interface IHosterURLAdder {
	/**
	 * The host-class can modifie some data of the url-Object
	 * and can return array with URLs to be added to the URL-array
	 * which contains the actual url-Object.
	 * When a host-class only wants to modifie the data of the url-object
	 * then null should be returned.
	 * 
	 * @param url URL-Object
	 * @param isFromThisHoster Flag to set if url given is also accepted or not
	 * @param progress ProgressObserver
	 * @return Array with additional urls
	 * @throws Exception 
	 */
	public List<URL> isFromThisHoster(URL url, OptionBoolean isFromThisHoster, ProgressObserver progress) throws Exception;
}

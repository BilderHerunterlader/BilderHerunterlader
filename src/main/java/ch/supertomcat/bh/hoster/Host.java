package ch.supertomcat.bh.hoster;

import org.apache.http.impl.client.CloseableHttpClient;

import ch.supertomcat.bh.exceptions.HostException;

/**
 * Class which all hostclasses have to extend
 */
public abstract class Host extends Hoster implements IHoster {
	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @return Sourcecode
	 * @throws HostException
	 */
	protected final String downloadContainerPage(String url, String referrer) throws HostException {
		return super.downloadContainerPage(getName(), url, referrer);
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @param options Options
	 * @return Sourcecode
	 * @throws HostException
	 */
	protected final String downloadContainerPage(String url, String referrer, DownloadContainerPageOptions options) throws HostException {
		return super.downloadContainerPage(getName(), url, referrer, options);
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @param options Options
	 * @param client HttpClient
	 * @return Sourcecode
	 * @throws HostException
	 */
	protected final String downloadContainerPage(String url, String referrer, DownloadContainerPageOptions options, CloseableHttpClient client) throws HostException {
		return super.downloadContainerPage(getName(), url, referrer, options, client);
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @return Sourcecode
	 * @throws HostException
	 */
	protected final ContainerPage downloadContainerPageEx(String url, String referrer) throws HostException {
		return super.downloadContainerPageEx(getName(), url, referrer);
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @param options Options
	 * @return Sourcecode
	 * @throws HostException
	 */
	protected final ContainerPage downloadContainerPageEx(String url, String referrer, DownloadContainerPageOptions options) throws HostException {
		return super.downloadContainerPageEx(getName(), url, referrer, options);
	}

	/**
	 * Downloads and Returns the sourcecode of a Container-Page
	 * 
	 * @param url Container-URL
	 * @param referrer Referrer
	 * @param options Options
	 * @param client HttpClient
	 * @return Sourcecode
	 * @throws HostException
	 */
	protected final ContainerPage downloadContainerPageEx(String url, String referrer, DownloadContainerPageOptions options, CloseableHttpClient client) throws HostException {
		return super.downloadContainerPageEx(getName(), url, referrer, options, client);
	}
}

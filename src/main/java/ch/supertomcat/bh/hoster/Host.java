package ch.supertomcat.bh.hoster;

import org.apache.http.impl.client.CloseableHttpClient;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.hosteroptions.DeactivateOption;
import ch.supertomcat.bh.settings.SettingsManager;

/**
 * Class which all hostclasses have to extend
 */
public abstract class Host extends Hoster implements IHoster {
	/**
	 * Name
	 */
	protected final String name;

	/**
	 * Version
	 */
	protected final String version;

	/**
	 * Deactivate Option or null
	 */
	protected final DeactivateOption deactivateOption;

	/**
	 * Constructor
	 * 
	 * @param name Name
	 * @param version Version
	 */
	public Host(String name, String version) {
		this(name, version, true);
	}

	/**
	 * Constructor
	 * 
	 * @param name Name
	 * @param version Version
	 * @param canBeDisabled True if host can be disabled, false otherwise
	 */
	public Host(String name, String version, boolean canBeDisabled) {
		this.name = name;
		this.version = version;
		if (canBeDisabled) {
			deactivateOption = new DeactivateOption(name);
		} else {
			deactivateOption = null;
		}
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final String getVersion() {
		return version;
	}

	@Override
	public final boolean isEnabled() {
		if (deactivateOption != null) {
			return !deactivateOption.isDeactivated();
		} else {
			return true;
		}
	}

	@Override
	public final void setEnabled(boolean enabled) {
		if (deactivateOption != null) {
			deactivateOption.setDeactivated(!enabled);
			deactivateOption.saveOption();
			SettingsManager.instance().writeSettings(true);
		}
	}

	@Override
	public boolean canBeDisabled() {
		return deactivateOption != null;
	}

	@Override
	public final String toString() {
		return name;
	}

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

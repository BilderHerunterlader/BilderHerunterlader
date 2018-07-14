package ch.supertomcat.bh.hoster.hosteroptions;

/**
 * This interface can be implemented by host-classes, which provide an option
 * to override the download-directory.
 * Examples are HostRapidshare, HostMegaupload, HostFilesTo, HostUploadedTo
 */
public interface IHosterOverrideDirectoryOption extends IHosterOptions {
	
	/**
	 * @return OverrideDirectoryOption
	 */
	public OverrideDirectoryOption getOverrideDirectoryOption();
}

package ch.supertomcat.bh.rules;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.containerpage.DownloadContainerPageOptions;

/**
 * Download Container Page Function
 * 
 * @param <T> Result Type
 */
@FunctionalInterface
public interface RuleDownloadContainerPageFunction<T> {
	/**
	 * Download Container Page
	 * 
	 * @param url URL
	 * @param referrer Referrer
	 * @param options Options
	 * @return Result
	 * @throws HostException
	 */
	public T downloadContainerPage(String url, String referrer, DownloadContainerPageOptions options) throws HostException;
}

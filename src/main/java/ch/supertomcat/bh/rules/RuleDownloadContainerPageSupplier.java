package ch.supertomcat.bh.rules;

import ch.supertomcat.bh.exceptions.HostException;

/**
 * Download Container Page Supplier
 * 
 * @param <T> Result Type
 */
@FunctionalInterface
public interface RuleDownloadContainerPageSupplier<T> {
	/**
	 * Download Container Page
	 * 
	 * @return Result
	 * @throws HostException
	 */
	public T downloadContainerPage() throws HostException;
}

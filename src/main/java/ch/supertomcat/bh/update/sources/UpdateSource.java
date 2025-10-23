package ch.supertomcat.bh.update.sources;

import java.nio.file.Path;
import java.util.List;

import ch.supertomcat.bh.update.UpdateException;
import ch.supertomcat.bh.updates.xml.MainVersion;
import ch.supertomcat.bh.updates.xml.UpdateData;
import ch.supertomcat.bh.updates.xml.Updates;

/**
 * Update Source
 */
public interface UpdateSource {
	/**
	 * Checks for Updates and returns a list of available updates
	 * 
	 * @return UpdateList
	 * @throws UpdateException
	 */
	public Updates checkForUpdates() throws UpdateException;

	/**
	 * Download Update
	 * 
	 * @param update Update
	 * @param targetDirectory Target Directory
	 * @return Downloaded File
	 * @throws UpdateException
	 */
	public Path downloadUpdate(MainVersion update, Path targetDirectory) throws UpdateException;

	/**
	 * Download Update
	 * 
	 * @param update Update
	 * @param targetDirectory Target Directory
	 * @return Downloaded Files
	 * @throws UpdateException
	 */
	public List<Path> downloadUpdate(UpdateData update, Path targetDirectory) throws UpdateException;
}

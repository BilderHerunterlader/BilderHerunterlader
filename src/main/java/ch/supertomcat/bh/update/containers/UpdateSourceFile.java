package ch.supertomcat.bh.update.containers;

import ch.supertomcat.bh.update.UpdateException;

/**
 * 
 */
public abstract class UpdateSourceFile {
	private boolean delete = false;

	/**
	 * Constructor
	 * 
	 * @param delete True if is delete update, false otherwise
	 */
	public UpdateSourceFile(boolean delete) {
		this.delete = delete;
	}

	/**
	 * Returns the source
	 * 
	 * @return source
	 */
	public abstract String getSource();

	/**
	 * Returns the target filename
	 * 
	 * @return Target filename
	 */
	public abstract String getTargetFilename();

	/**
	 * Returns the delete
	 * 
	 * @return delete
	 */
	public boolean isDelete() {
		return delete;
	}

	/**
	 * Download update
	 * If the actionType is NONE or REMOVE this method just returns false and does nothing
	 * If the type is BH or LIB then .new is added to the target filename. But the target-variable of this object is not changed.
	 * 
	 * @param updateType Update Type
	 * @param action Action Type
	 * @param target Target
	 * @return True if successfull
	 * @throws UpdateException
	 */
	public abstract boolean downloadUpdate(UpdateObject.UpdateType updateType, UpdateObject.UpdateActionType action, String target) throws UpdateException;
}

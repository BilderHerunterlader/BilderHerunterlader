package ch.supertomcat.bh.update.containers;

import ch.supertomcat.bh.updates.xml.UpdateData;

/**
 * Wrapped Update Data
 */
public class WrappedUpdateData {
	/**
	 * Update Data
	 */
	private final UpdateData updateData;

	/**
	 * Type
	 */
	private final UpdateType type;

	/**
	 * Action
	 */
	private UpdateActionType action = UpdateActionType.ACTION_NONE;

	/**
	 * Constructor
	 * 
	 * @param updateData Update Data
	 * @param type Type
	 */
	public WrappedUpdateData(UpdateData updateData, UpdateType type) {
		this.updateData = updateData;
		this.type = type;
		if (updateData.getDelete() != null) {
			this.action = UpdateActionType.ACTION_REMOVE;
		}
	}

	/**
	 * Returns the updateData
	 * 
	 * @return updateData
	 */
	public UpdateData getUpdateData() {
		return updateData;
	}

	/**
	 * Returns the type
	 * 
	 * @return type
	 */
	public UpdateType getType() {
		return type;
	}

	/**
	 * Returns the action
	 * 
	 * @return action
	 */
	public UpdateActionType getAction() {
		return action;
	}

	/**
	 * Sets the action
	 * 
	 * @param action action
	 */
	public void setAction(UpdateActionType action) {
		this.action = action;
	}

	/**
	 * Returns the name
	 * 
	 * @return name
	 */
	public String getName() {
		return updateData.getName();
	}

	/**
	 * Returns the version
	 * 
	 * @return version
	 */
	public String getVersion() {
		return updateData.getVersion();
	}

	/**
	 * Returns the bhMinVersion
	 * 
	 * @return bhMinVersion
	 */
	public String getBhMinVersion() {
		return updateData.getBhminversion();
	}

	/**
	 * Returns the bhMaxVersion
	 * 
	 * @return bhMaxVersion
	 */
	public String getBhMaxVersion() {
		return updateData.getBhmaxversion();
	}

	/**
	 * Returns the comment
	 * 
	 * @return comment
	 */
	public String getComment() {
		return updateData.getDelete() != null ? updateData.getDelete() : updateData.getSrc();
	}

	/**
	 * This method returns true if an update is required.
	 * You can use this method as a shortcut if you don't want to check the action.
	 * If the action is NONE false is returned.
	 * If the action is ADD, UPDATE or REMOVE true is returned
	 * 
	 * @return True if an update is required
	 */
	public boolean isUpdateRequired() {
		return action != UpdateActionType.ACTION_NONE;
	}

	/**
	 * This method returns true if an update is required.
	 * You can use this method as a shortcut if you don't want to check the action.
	 * If the action is NONE false is returned.
	 * If the action is ADD, UPDATE or REMOVE true is returned
	 * 
	 * @return True if an update is required
	 */
	public boolean isDownloadRequired() {
		return action == UpdateActionType.ACTION_NEW || action == UpdateActionType.ACTION_UPDATE;
	}

	/**
	 * @return True if update is delete type, false otherwise
	 */
	public boolean isDelete() {
		return action == UpdateActionType.ACTION_REMOVE;
	}

	/**
	 * @return True if update is delete type, false otherwise
	 */
	public boolean isSubDelete() {
		return updateData.getSource().stream().anyMatch(x -> x.getDelete() != null);
	}
}

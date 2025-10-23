package ch.supertomcat.bh.update.containers;

import java.util.ArrayList;
import java.util.List;

import ch.supertomcat.bh.updates.xml.Change;
import ch.supertomcat.bh.updates.xml.Updates;

/**
 * 
 */
public class WrappedUpdates {
	/**
	 * Updates
	 */
	private final Updates updates;

	/**
	 * Redirect Updates
	 */
	private final List<WrappedUpdateData> redirectUpdates = new ArrayList<>();

	/**
	 * Hoster Updates
	 */
	private final List<WrappedUpdateData> hosterUpdates = new ArrayList<>();

	/**
	 * Rule Updates
	 */
	private final List<WrappedUpdateData> ruleUpdates = new ArrayList<>();

	/**
	 * Type
	 */
	private final UpdateType type = UpdateType.TYPE_BH;

	/**
	 * Action
	 */
	private UpdateActionType action = UpdateActionType.ACTION_NONE;

	/**
	 * Constructor
	 * 
	 * @param updates
	 */
	public WrappedUpdates(Updates updates) {
		this.updates = updates;

		updates.getRedirectUpdates().getRedirect().stream().map(x -> new WrappedUpdateData(x, UpdateType.TYPE_REDIRECT_PLUGIN)).forEach(redirectUpdates::add);
		updates.getHosterUpdates().getHost().stream().map(x -> new WrappedUpdateData(x, UpdateType.TYPE_HOST_PLUGIN)).forEach(hosterUpdates::add);
		updates.getRuleUpdates().getRule().stream().map(x -> new WrappedUpdateData(x, UpdateType.TYPE_RULE)).forEach(ruleUpdates::add);
	}

	/**
	 * Returns the updates
	 * 
	 * @return updates
	 */
	public Updates getUpdates() {
		return updates;
	}

	/**
	 * Returns the redirectUpdates
	 * 
	 * @return redirectUpdates
	 */
	public List<WrappedUpdateData> getRedirectUpdates() {
		return redirectUpdates;
	}

	/**
	 * Returns the hosterUpdates
	 * 
	 * @return hosterUpdates
	 */
	public List<WrappedUpdateData> getHosterUpdates() {
		return hosterUpdates;
	}

	/**
	 * Returns the ruleUpdates
	 * 
	 * @return ruleUpdates
	 */
	public List<WrappedUpdateData> getRuleUpdates() {
		return ruleUpdates;
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
	 * Returns the type
	 * 
	 * @return type
	 */
	public UpdateType getType() {
		return type;
	}

	/**
	 * Returns the type as String
	 * 
	 * @return Type as String
	 */
	public String getTypeAsString() {
		return type.getName();
	}

	/**
	 * Returns the name
	 * 
	 * @return name
	 */
	public String getName() {
		return updates.getMain().getName();
	}

	/**
	 * Returns the version
	 * 
	 * @return version
	 */
	public String getVersion() {
		return updates.getMain().getVersion();
	}

	/**
	 * Returns the changelog
	 * 
	 * @return changelog for the given language or an empty String if not available
	 */
	public String getChangeLog() {
		String mainVersion = getVersion();
		return updates.getChangelog().getChanges().stream().filter(x -> x.getVersion().equals(mainVersion)).map(Change::getValue).findFirst().orElse("");
	}

	/**
	 * Returns the comment
	 * 
	 * @return comment
	 */
	public String getComment() {
		return updates.getMain().getSrc();
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
		return this.action != UpdateActionType.ACTION_NONE;
	}

	/**
	 * This method returns true if an update is required.
	 * You can use this method as a shortcut if you don't want to check the action.
	 * If the action is NONE false is returned.
	 * If the action is ADD, UPDATE or REMOVE true is returned
	 * 
	 * @return True if an update is required
	 */
	public boolean isSubUpdateRequired() {
		boolean redirectUpdatesRequired = redirectUpdates.stream().anyMatch(WrappedUpdateData::isUpdateRequired);
		boolean hosterUpdatesRequired = hosterUpdates.stream().anyMatch(WrappedUpdateData::isUpdateRequired);
		boolean ruleUpdatesRequired = ruleUpdates.stream().anyMatch(WrappedUpdateData::isUpdateRequired);
		return redirectUpdatesRequired || hosterUpdatesRequired || ruleUpdatesRequired;
	}
}

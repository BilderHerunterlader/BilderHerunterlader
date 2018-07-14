package ch.supertomcat.bh.update.containers;

import java.util.List;

/**
 * Contains a list of different types of updates
 */
public class UpdateList {
	/**
	 * updateBH
	 */
	private final UpdateObject updateApplication;

	/**
	 * updateRules
	 */
	private final List<UpdateObject> updateRules;

	/**
	 * updateHostPlugins
	 */
	private final List<UpdateObject> updateHostPlugins;

	/**
	 * updateRedirectPlugins
	 */
	private final List<UpdateObject> updateRedirectPlugins;

	/**
	 * Constructor
	 * 
	 * @param updateApplication Application Update
	 * @param updateRules Rules Updates
	 * @param updateHostPlugins HostPlugin Updates
	 * @param updateRedirectPlugins RedirectPlugin Updates
	 */
	public UpdateList(UpdateObject updateApplication, List<UpdateObject> updateRules, List<UpdateObject> updateHostPlugins, List<UpdateObject> updateRedirectPlugins) {
		this.updateApplication = updateApplication;
		this.updateRules = updateRules;
		this.updateHostPlugins = updateHostPlugins;
		this.updateRedirectPlugins = updateRedirectPlugins;
	}

	/**
	 * @return Application Update
	 */
	public UpdateObject getApplicationUpdate() {
		return updateApplication;
	}

	/**
	 * @return Rules Updates
	 */
	public List<UpdateObject> getRulesUpdates() {
		return updateRules;
	}

	/**
	 * @return HostPlugin Updates
	 */
	public List<UpdateObject> getHostPluginUpdates() {
		return updateHostPlugins;
	}

	/**
	 * @return RedirectPlugin Updates
	 */
	public List<UpdateObject> getRedirectPluginUpdates() {
		return updateRedirectPlugins;
	}
}

package ch.supertomcat.bh.update;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.GuiEvent;
import ch.supertomcat.bh.update.containers.UpdateList;
import ch.supertomcat.bh.update.containers.UpdateObject;
import ch.supertomcat.bh.update.containers.UpdateObject.UpdateType;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.application.ApplicationUtil;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Update Manager
 */
public class UpdateManager {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(UpdateManager.class);

	private List<UpdateManagerListener> listeners = new CopyOnWriteArrayList<>();

	private UpdateSource updateSource = null;

	/**
	 * GUI Event
	 */
	private final GuiEvent guiEvent;

	/**
	 * Constructor
	 * 
	 * @param updateSource UpdateSource
	 * @param guiEvent GUI Event
	 */
	public UpdateManager(UpdateSource updateSource, GuiEvent guiEvent) {
		this.updateSource = updateSource;
		this.guiEvent = guiEvent;
	}

	/**
	 * Checks for updates
	 * 
	 * @return UpdateList if successful, null otherwise
	 * @throws UpdateException
	 */
	public UpdateList checkForUpdates() throws UpdateException {
		if (updateSource == null) {
			throw new UpdateException("No source for updates available!");
		}
		return updateSource.checkForUpdates();
	}

	/**
	 * Start Update
	 * 
	 * @param updateList UpdateList
	 * @param owner Owner
	 */
	public void startUpdate(UpdateList updateList, Component owner) {
		fireUpdatesStarted();

		File fDeleteUpdate = new File(ApplicationProperties.getProperty(ApplicationMain.APPLICATION_PATH) + "delete_update.txt");
		try {
			Files.deleteIfExists(fDeleteUpdate.toPath());
		} catch (IOException e) {
			logger.error("Could not delete delete_update.txt file", e);
		}

		UpdateObject updateBH = updateList.getApplicationUpdate();
		List<UpdateObject> updateRules = updateList.getRulesUpdates();
		List<UpdateObject> updateHostPlugins = updateList.getHostPluginUpdates();
		List<UpdateObject> updateRedirectPlugins = updateList.getRedirectPluginUpdates();

		boolean mainUpdate = updateBH != null && updateBH.updateRequired();
		boolean mainUpdateOK = false;
		boolean deleteUpdatesAvailable = false;

		int updatesInstalled = 0;

		for (int i = 0; i < updateHostPlugins.size(); i++) {
			UpdateObject update = updateHostPlugins.get(i);
			if (update.updateRequired()) {
				if (update.getAction() == UpdateObject.UpdateActionType.ACTION_REMOVE) {
					deleteUpdatesAvailable = true;
					for (int x = 0; x < update.getTargets().size(); x++) {
						fireUpdateInstallStarted(UpdateType.TYPE_HOST_PLUGIN, update.getAction(), "", update.getTargets().get(x));
					}
				} else {
					if (update.isAdditionalRemove()) {
						deleteUpdatesAvailable = true;
					}
					for (int x = 0; x < update.getSources().size(); x++) {
						fireUpdateInstallStarted(UpdateType.TYPE_HOST_PLUGIN, update.getAction(), update.getSources().get(x).getSource(), "");
					}
				}
				boolean b = false;
				try {
					if (update.getAction() == UpdateObject.UpdateActionType.ACTION_REMOVE) {
						b = update.doFinalUpdateProcess();
					} else {
						b = update.downloadUpdate();
					}
				} catch (Exception e) {
					fireErrorOccured(e.getMessage());
					logger.error(e.getMessage(), e);
				}
				if (b) {
					updatesInstalled++;
					fireUpdateInstallComplete(UpdateType.TYPE_HOST_PLUGIN, update.getAction());
				} else {
					fireUpdateInstallFailed(UpdateType.TYPE_HOST_PLUGIN, update.getAction());
				}
			}
		}

		for (int i = 0; i < updateRules.size(); i++) {
			UpdateObject update = updateRules.get(i);
			if (update.updateRequired()) {
				if (update.getAction() == UpdateObject.UpdateActionType.ACTION_REMOVE) {
					deleteUpdatesAvailable = true;
					for (int x = 0; x < update.getTargets().size(); x++) {
						fireUpdateInstallStarted(UpdateType.TYPE_RULE, update.getAction(), "", update.getTargets().get(x));
					}
				} else {
					if (update.isAdditionalRemove()) {
						deleteUpdatesAvailable = true;
					}
					for (int x = 0; x < update.getSources().size(); x++) {
						fireUpdateInstallStarted(UpdateType.TYPE_RULE, update.getAction(), update.getSources().get(x).getSource(), "");
					}
				}
				boolean b = false;
				try {
					if (update.getAction() == UpdateObject.UpdateActionType.ACTION_REMOVE) {
						b = update.doFinalUpdateProcess();
					} else {
						b = update.downloadUpdate();
					}
				} catch (Exception e) {
					fireErrorOccured(e.getMessage());
					logger.error(e.getMessage(), e);
				}
				if (b) {
					updatesInstalled++;
					fireUpdateInstallComplete(UpdateType.TYPE_RULE, update.getAction());
				} else {
					fireUpdateInstallFailed(UpdateType.TYPE_RULE, update.getAction());
				}
			}
		}

		for (int i = 0; i < updateRedirectPlugins.size(); i++) {
			UpdateObject update = updateRedirectPlugins.get(i);
			if (update.updateRequired()) {
				if (update.getAction() == UpdateObject.UpdateActionType.ACTION_REMOVE) {
					deleteUpdatesAvailable = true;
					for (int x = 0; x < update.getTargets().size(); x++) {
						fireUpdateInstallStarted(UpdateType.TYPE_REDIRECT_PLUGIN, update.getAction(), "", update.getTargets().get(x));
					}
				} else {
					if (update.isAdditionalRemove()) {
						deleteUpdatesAvailable = true;
					}
					for (int x = 0; x < update.getSources().size(); x++) {
						fireUpdateInstallStarted(UpdateType.TYPE_REDIRECT_PLUGIN, update.getAction(), update.getSources().get(x).getSource(), "");
					}
				}
				boolean b = false;
				try {
					if (update.getAction() == UpdateObject.UpdateActionType.ACTION_REMOVE) {
						b = update.doFinalUpdateProcess();
					} else {
						b = update.downloadUpdate();
					}
				} catch (Exception e) {
					fireErrorOccured(e.getMessage());
					logger.error(e.getMessage(), e);
				}
				if (b) {
					updatesInstalled++;
					fireUpdateInstallComplete(UpdateType.TYPE_REDIRECT_PLUGIN, update.getAction());
				} else {
					fireUpdateInstallFailed(UpdateType.TYPE_REDIRECT_PLUGIN, update.getAction());
				}
			}
		}

		// Main Update
		if (mainUpdate) {
			fireUpdateInstallStarted(UpdateType.TYPE_BH, updateBH.getAction(), updateBH.getSources().get(0).getSource(), "");
			boolean bMainDownload = false;
			try {
				bMainDownload = updateBH.downloadUpdate();
			} catch (Exception e) {
				fireErrorOccured(e.getMessage());
				logger.error(e.getMessage(), e);
			}

			if (bMainDownload) {
				// Main Update
				try {
					mainUpdateOK = updateBH.doFinalUpdateProcess();
				} catch (Exception e) {
					fireErrorOccured(e.getMessage());
					logger.error(e.getMessage(), e);
					mainUpdateOK = false;
				}
			} else {
				fireUpdateInstallFailed(UpdateType.TYPE_BH, updateBH.getAction());
			}
		}

		fireUpdatesInstalled(updatesInstalled);

		if (mainUpdate && mainUpdateOK) {
			fireNewProgramVersionInstalled();
			fireUpdatesComplete();
		} else if (mainUpdate && !mainUpdateOK) {
			fireNewProgramVersionInstallFailed();
			fireUpdatesFailed();
		} else if (updatesInstalled > 0) {
			fireUpdatesComplete();
		}

		if (deleteUpdatesAvailable) {
			guiEvent.exitAppForced(Localization.getString("ProgrammExitBecauseUpdateNoAutoRestart"), Localization.getString("Update"), owner, false, true);
		} else {
			guiEvent.exitAppForced(Localization.getString("ProgrammExitBecauseUpdate"), Localization.getString("Update"), owner, true, true);
		}
	}

	private void fireErrorOccured(String message) {
		for (UpdateManagerListener listener : listeners) {
			listener.errorOccured(message);
		}
	}

	private void fireUpdatesStarted() {
		for (UpdateManagerListener listener : listeners) {
			listener.updatesStarted();
		}
	}

	private void fireUpdatesInstalled(int updateCount) {
		for (UpdateManagerListener listener : listeners) {
			listener.updatesInstalled(updateCount);
		}
	}

	private void fireNewProgramVersionInstalled() {
		for (UpdateManagerListener listener : listeners) {
			listener.newProgramVersionInstalled();
		}
	}

	private void fireNewProgramVersionInstallFailed() {
		for (UpdateManagerListener listener : listeners) {
			listener.newProgramVersionInstallFailed();
		}
	}

	private void fireUpdatesComplete() {
		for (UpdateManagerListener listener : listeners) {
			listener.updatesComplete();
		}
	}

	private void fireUpdatesFailed() {
		for (UpdateManagerListener listener : listeners) {
			listener.updatesFailed();
		}
	}

	private void fireUpdateInstallStarted(UpdateObject.UpdateType updateType, UpdateObject.UpdateActionType updateActionType, String source, String target) {
		for (UpdateManagerListener listener : listeners) {
			listener.updateInstallStarted(updateType, updateActionType, source, target);
		}
	}

	private void fireUpdateInstallComplete(UpdateObject.UpdateType updateType, UpdateObject.UpdateActionType updateActionType) {
		for (UpdateManagerListener listener : listeners) {
			listener.updateInstallComplete(updateType, updateActionType);
		}
	}

	private void fireUpdateInstallFailed(UpdateObject.UpdateType updateType, UpdateObject.UpdateActionType updateActionType) {
		for (UpdateManagerListener listener : listeners) {
			listener.updateInstallFailed(updateType, updateActionType);
		}
	}

	/**
	 * Checks if the current version of BH fits in min- and max-version
	 * 
	 * @param minVersion Min-Version
	 * @param maxVersion Max-Version
	 * @return True if current BH-version fits in min- and max-version
	 */
	public static boolean checkMinMaxVersions(String minVersion, String maxVersion) {
		int iMinVersion;
		int iMaxVersion;
		if (minVersion == null || minVersion.isEmpty()) {
			iMinVersion = 0;
		} else {
			iMinVersion = ApplicationUtil.compareVersions(minVersion, ApplicationProperties.getProperty(ApplicationMain.APPLICATION_VERSION));
		}
		if (maxVersion == null || maxVersion.isEmpty()) {
			iMaxVersion = 0;
		} else {
			iMaxVersion = ApplicationUtil.compareVersions(maxVersion, ApplicationProperties.getProperty(ApplicationMain.APPLICATION_VERSION));
		}
		return (iMinVersion <= 0) && (iMaxVersion >= 0);
	}

	/**
	 * Add Listener
	 * 
	 * @param listener Listener
	 */
	public void addListener(UpdateManagerListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * Remove Listener
	 * 
	 * @param listener Listener
	 */
	public void removeListener(UpdateManagerListener listener) {
		listeners.remove(listener);
	}
}

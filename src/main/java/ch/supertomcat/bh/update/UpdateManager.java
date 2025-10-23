package ch.supertomcat.bh.update;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.GuiEvent;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.update.containers.UpdateActionType;
import ch.supertomcat.bh.update.containers.UpdateType;
import ch.supertomcat.bh.update.containers.WrappedUpdateData;
import ch.supertomcat.bh.update.containers.WrappedUpdates;
import ch.supertomcat.bh.update.sources.UpdateSource;
import ch.supertomcat.bh.updates.xml.UpdateData;
import ch.supertomcat.bh.updates.xml.UpdateDataAdditionalSource;
import ch.supertomcat.bh.updates.xml.Updates;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.application.ApplicationUtil;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.io.DirectoryUtil;
import ch.supertomcat.supertomcatutils.io.ZipUtil;

/**
 * Update Manager
 */
public class UpdateManager {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(UpdateManager.class);

	/**
	 * Listeners
	 */
	private List<UpdateManagerListener> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Update Source
	 */
	private final UpdateSource updateSource;

	/**
	 * GUI Event
	 */
	private final GuiEvent guiEvent;

	/**
	 * Host Manager
	 */
	private final HostManager hostManager;

	/**
	 * Constructor
	 * 
	 * @param updateSource UpdateSource
	 * @param hostManager Host Manager
	 * @param guiEvent GUI Event
	 */
	public UpdateManager(UpdateSource updateSource, HostManager hostManager, GuiEvent guiEvent) {
		if (updateSource == null) {
			throw new IllegalArgumentException("No source for updates available!");
		}
		this.updateSource = updateSource;
		this.guiEvent = guiEvent;
		this.hostManager = hostManager;
	}

	/**
	 * Checks for updates
	 * 
	 * @return UpdateList if successful, null otherwise
	 * @throws UpdateException
	 */
	public WrappedUpdates checkForUpdates() throws UpdateException {
		logger.info("Checking for updates");
		Updates updates = updateSource.checkForUpdates();
		WrappedUpdates wrappedUpdates = new WrappedUpdates(updates);

		/*
		 * Main Version
		 */
		String currentVersion = ApplicationProperties.getProperty(ApplicationMain.APPLICATION_VERSION);
		logger.info("Check main update needed: CurrentVersion: {}, UpdateVersion: {}", currentVersion, wrappedUpdates.getVersion());
		if (ApplicationUtil.compareVersions(wrappedUpdates.getVersion(), ApplicationProperties.getProperty(ApplicationMain.APPLICATION_VERSION)) > 0) {
			wrappedUpdates.setAction(UpdateActionType.ACTION_UPDATE);
		}

		/*
		 * Redirects
		 */
		wrappedUpdates.getRedirectUpdates().stream().forEach(x -> checkUpdateNeeded(x, hostManager.getRedirectManager()::getRedirectVersion));

		/*
		 * Hosters
		 */
		wrappedUpdates.getHosterUpdates().stream().forEach(x -> checkUpdateNeeded(x, hostManager::getHostVersion));

		/*
		 * Rules
		 */
		wrappedUpdates.getRuleUpdates().stream().forEach(x -> checkUpdateNeeded(x, hostManager.getHostRules()::getRuleVersionByFileName));

		return wrappedUpdates;
	}

	/**
	 * Check if update is needed
	 * 
	 * @param wrappedUpdateData WrappedUpdateData
	 * @param currentVersionFunction Current Version Function
	 */
	private void checkUpdateNeeded(WrappedUpdateData wrappedUpdateData, UnaryOperator<String> currentVersionFunction) {
		String name = wrappedUpdateData.getName();
		String currentVersion = currentVersionFunction.apply(name);
		logger.info("Check update needed: Name: {}, CurrentVersion: {}, UpdateVersion: {}", name, currentVersion, wrappedUpdateData.getVersion());
		if (currentVersion == null || currentVersion.isEmpty()) {
			if (wrappedUpdateData.isDelete()) {
				// Current version not found, so already deleted
				wrappedUpdateData.setAction(UpdateActionType.ACTION_NONE);
				return;
			}

			// Current version not found, new file
			if (checkMinMaxVersions(wrappedUpdateData.getBhMinVersion(), wrappedUpdateData.getBhMaxVersion())) {
				wrappedUpdateData.setAction(UpdateActionType.ACTION_NEW);
			}
			return;
		}

		if (wrappedUpdateData.isDelete()) {
			// Current version found, needs to be deleted, action already set
			return;
		}

		if (ApplicationUtil.compareVersions(wrappedUpdateData.getVersion(), currentVersion) > 0 && checkMinMaxVersions(wrappedUpdateData.getBhMinVersion(), wrappedUpdateData.getBhMaxVersion())) {
			wrappedUpdateData.setAction(UpdateActionType.ACTION_UPDATE);
		}
	}

	/**
	 * Start Update
	 * 
	 * @param wrappedUpdates Updates
	 * @param owner Owner
	 */
	public void startUpdate(WrappedUpdates wrappedUpdates, Component owner) {
		logger.info("Update started");
		fireUpdatesStarted();

		Path applicationPath = Paths.get(ApplicationProperties.getProperty(ApplicationMain.APPLICATION_PATH)).toAbsolutePath();
		Path hostsDirectory = applicationPath.resolve("hosts");
		Path rulesDirectory = applicationPath.resolve("rules");
		logger.info("Directories: Application: {}, Hosts: {}, Rules: {}", applicationPath, hostsDirectory, rulesDirectory);

		Path deleteUpdatesFile = applicationPath.resolve("delete_update.txt");
		try {
			logger.info("Delete delete_update.txt if it exists");
			Files.deleteIfExists(deleteUpdatesFile);
		} catch (IOException e) {
			logger.error("Could not delete delete_update.txt file", e);
		}

		boolean mainUpdateAvailable = wrappedUpdates.isUpdateRequired();
		boolean redirectDeleteUpdatesAvailable = checkDeleteUpdatesAvaialble(wrappedUpdates.getRedirectUpdates());
		boolean hosterDeleteUpdatesAvailable = checkDeleteUpdatesAvaialble(wrappedUpdates.getHosterUpdates());
		boolean ruleDeleteUpdatesAvailable = checkDeleteUpdatesAvaialble(wrappedUpdates.getRuleUpdates());
		boolean deleteUpdatesAvailable = redirectDeleteUpdatesAvailable || hosterDeleteUpdatesAvailable || ruleDeleteUpdatesAvailable;

		logger.info("Available Updates: Main: {}, Redirects: {}, Hoster: {}, Rules: {}, Deletes: {}", mainUpdateAvailable, redirectDeleteUpdatesAvailable, hosterDeleteUpdatesAvailable, ruleDeleteUpdatesAvailable, deleteUpdatesAvailable);

		Path tempDirectory = null;
		try {
			tempDirectory = Files.createTempDirectory("BHUpdate");
			logger.info("Temp directory created: {}", tempDirectory);

			Path tempMainDirectory = tempDirectory.resolve("main");
			if (mainUpdateAvailable) {
				fireUpdateDownloadStarted(UpdateType.TYPE_BH, UpdateActionType.ACTION_UPDATE, wrappedUpdates.getUpdates().getMain().getSrc(), tempDirectory.toString());
				Path mainUpdateZipFile = updateSource.downloadUpdate(wrappedUpdates.getUpdates().getMain(), tempDirectory);
				fireUpdateDownloadComplete(UpdateType.TYPE_BH, UpdateActionType.ACTION_UPDATE);
				fireUpdateUnpackStarted(UpdateType.TYPE_BH, UpdateActionType.ACTION_UPDATE, mainUpdateZipFile.toString(), tempMainDirectory.toString());
				logger.info("Unpack {} to {}", mainUpdateZipFile, tempMainDirectory);
				ZipUtil.extractZipFile(mainUpdateZipFile, tempMainDirectory);
				logger.info("Unpack successful");
				fireUpdateUnpackComplete(UpdateType.TYPE_BH, UpdateActionType.ACTION_UPDATE);
			}

			Path tempHostsDirectory = tempDirectory.resolve("hosts");
			List<Path> downloadedRedirectFiles = downloadUpdates(wrappedUpdates.getRedirectUpdates(), tempHostsDirectory, UpdateType.TYPE_REDIRECT_PLUGIN);
			List<Path> downloadedHosterFiles = downloadUpdates(wrappedUpdates.getHosterUpdates(), tempHostsDirectory, UpdateType.TYPE_HOST_PLUGIN);

			Path tempRulesDirectory = tempDirectory.resolve("rules");
			List<Path> downloadedRuleFiles = downloadUpdates(wrappedUpdates.getRuleUpdates(), tempRulesDirectory, UpdateType.TYPE_RULE);

			addDeleteUpdates(wrappedUpdates.getRedirectUpdates(), hostsDirectory, deleteUpdatesFile);
			addDeleteUpdates(wrappedUpdates.getHosterUpdates(), hostsDirectory, deleteUpdatesFile);
			addDeleteUpdates(wrappedUpdates.getRuleUpdates(), rulesDirectory, deleteUpdatesFile);

			/*
			 * Now that everything is downloaded copy files to application directory
			 * 
			 * TODO In the future this should not be done by this application, but by a separate updater
			 */
			copyFilesToApplicationDirectory(downloadedRedirectFiles, applicationPath, tempDirectory);
			copyFilesToApplicationDirectory(downloadedHosterFiles, applicationPath, tempDirectory);
			copyFilesToApplicationDirectory(downloadedRuleFiles, applicationPath, tempDirectory);
			if (mainUpdateAvailable) {
				copyFolderToApplicationDirectory(applicationPath, tempMainDirectory);
				fireNewProgramVersionInstalled();
			}

			logger.info("Updates successful");
			fireUpdatesComplete();

			if (deleteUpdatesAvailable) {
				logger.info("Exit BH without restart");
				guiEvent.exitAppForced(Localization.getString("ProgrammExitBecauseUpdateNoAutoRestart"), Localization.getString("Update"), owner, false, true);
			} else {
				logger.info("Exit BH with restart");
				guiEvent.exitAppForced(Localization.getString("ProgrammExitBecauseUpdate"), Localization.getString("Update"), owner, true, true);
			}
		} catch (UpdateException | IOException e) {
			logger.error("Update installation failed", e);
			fireErrorOccured("Update installation failed", e);
			if (mainUpdateAvailable) {
				fireNewProgramVersionInstallFailed();
			}
			fireUpdatesFailed();
		} finally {
			if (tempDirectory != null) {
				try {
					logger.info("Delete temp directory: {}", tempDirectory);
					DirectoryUtil.deleteDirectoryRecursive(tempDirectory, true);
				} catch (IOException e) {
					logger.error("Could not delete temp directory", e);
				}
			}
		}
	}

	/**
	 * Download Update
	 * 
	 * @param wrappedUpdateDataList List
	 * @param directory Directory
	 * @param updateType Update Type
	 * @return Downloaded Files
	 * @throws UpdateException
	 */
	private List<Path> downloadUpdates(List<WrappedUpdateData> wrappedUpdateDataList, Path directory, UpdateType updateType) throws UpdateException {
		List<Path> overallDownloadedFiles = new ArrayList<>();

		List<WrappedUpdateData> downloads = wrappedUpdateDataList.stream().filter(WrappedUpdateData::isDownloadRequired).toList();
		for (WrappedUpdateData wrappedUpdateData : downloads) {
			UpdateData updateData = wrappedUpdateData.getUpdateData();
			fireUpdateDownloadStarted(updateType, wrappedUpdateData.getAction(), updateData.getSrc(), directory.toString());
			List<Path> downloadedFiles = updateSource.downloadUpdate(updateData, directory);
			fireUpdateDownloadComplete(updateType, wrappedUpdateData.getAction());
			overallDownloadedFiles.addAll(downloadedFiles);
		}

		return overallDownloadedFiles;
	}

	/**
	 * Copy Files to application directory
	 * 
	 * @param tempFiles Temp Files
	 * @param applicationDirectory Application Directory
	 * @param tempDirectory Temp Directory
	 * @throws IOException
	 */
	private void copyFilesToApplicationDirectory(List<Path> tempFiles, Path applicationDirectory, Path tempDirectory) throws IOException {
		logger.info("Copy files from {} to {}", tempDirectory, applicationDirectory);
		for (Path tempFile : tempFiles) {
			Path targetFile = applicationDirectory.resolve(tempDirectory.relativize(tempFile).toString());
			logger.info("Copy file from {} to {}", tempFile, targetFile);
			fireUpdateCopyStarted(tempFile.toString(), targetFile.toString());
			Files.copy(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
			fireUpdateCopyComplete();
		}
	}

	/**
	 * Copy Files to application directory
	 * 
	 * @param applicationDirectory Application Directory
	 * @param tempDirectory Temp Directory
	 * @throws IOException
	 */
	private void copyFolderToApplicationDirectory(Path applicationDirectory, Path tempDirectory) throws IOException {
		logger.info("Copy files from {} to {}", tempDirectory, applicationDirectory);
		fireUpdateCopyStarted(tempDirectory.toString(), applicationDirectory.toString());
		Files.walkFileTree(tempDirectory, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path targetDirectory = applicationDirectory.resolve(tempDirectory.relativize(dir).toString());
				logger.info("Create directory if not exists: {}", targetDirectory);
				Files.createDirectories(targetDirectory);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Path targetFile = applicationDirectory.resolve(tempDirectory.relativize(file).toString());
				logger.info("Copy file from {} to {}", file, targetFile);
				Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}
		});
		fireUpdateCopyComplete();
	}

	/**
	 * Check if delete updates available
	 * 
	 * @param wrappedUpdateDataList List
	 * @return True if delete updates available, false otherwise
	 */
	private boolean checkDeleteUpdatesAvaialble(List<WrappedUpdateData> wrappedUpdateDataList) {
		return wrappedUpdateDataList.stream().anyMatch(x -> x.isDelete() || x.isSubDelete());
	}

	/**
	 * Add delete updates
	 * 
	 * @param wrappedUpdateDataList List
	 * @param directory Directory
	 * @param deleteUpdatesFile Delete Updates File
	 * @throws IOException
	 */
	private void addDeleteUpdates(List<WrappedUpdateData> wrappedUpdateDataList, Path directory, Path deleteUpdatesFile) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(deleteUpdatesFile, Charset.forName(System.getProperty("native.encoding")), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
			List<WrappedUpdateData> deletes = wrappedUpdateDataList.stream().filter(WrappedUpdateData::isDelete).toList();
			for (WrappedUpdateData wrappedUpdateData : deletes) {
				UpdateData updateData = wrappedUpdateData.getUpdateData();
				writer.write(directory.resolve(updateData.getFilename()).toString());
				writer.write("\n");
				writer.flush();
			}

			List<UpdateDataAdditionalSource> subDeletes = wrappedUpdateDataList.stream().filter(WrappedUpdateData::isSubDelete).map(WrappedUpdateData::getUpdateData)
					.flatMap(x -> x.getSource().stream()).filter(x -> x.getDelete() != null).toList();
			for (UpdateDataAdditionalSource additionalSource : subDeletes) {
				writer.write(directory.resolve(additionalSource.getFilename()).toString());
				writer.write("\n");
				writer.flush();
			}
		}
	}

	private void fireErrorOccured(String message, Exception ex) {
		for (UpdateManagerListener listener : listeners) {
			listener.errorOccured(message, ex);
		}
	}

	private void fireUpdatesStarted() {
		for (UpdateManagerListener listener : listeners) {
			listener.updatesStarted();
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

	private void fireUpdateDownloadStarted(UpdateType updateType, UpdateActionType updateActionType, String source, String target) {
		for (UpdateManagerListener listener : listeners) {
			listener.updateDownloadStarted(updateType, updateActionType, source, target);
		}
	}

	private void fireUpdateDownloadComplete(UpdateType updateType, UpdateActionType updateActionType) {
		for (UpdateManagerListener listener : listeners) {
			listener.updateDownloadComplete(updateType, updateActionType);
		}
	}

	private void fireUpdateUnpackStarted(UpdateType updateType, UpdateActionType updateActionType, String source, String target) {
		for (UpdateManagerListener listener : listeners) {
			listener.updateUnpackStarted(updateType, updateActionType, source, target);
		}
	}

	private void fireUpdateUnpackComplete(UpdateType updateType, UpdateActionType updateActionType) {
		for (UpdateManagerListener listener : listeners) {
			listener.updateUnpackComplete(updateType, updateActionType);
		}
	}

	private void fireUpdateCopyStarted(String source, String target) {
		for (UpdateManagerListener listener : listeners) {
			listener.updateCopyStarted(source, target);
		}
	}

	private void fireUpdateCopyComplete() {
		for (UpdateManagerListener listener : listeners) {
			listener.updateCopyComplete();
		}
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
}

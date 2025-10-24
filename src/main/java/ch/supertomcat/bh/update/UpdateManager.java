package ch.supertomcat.bh.update;

import java.awt.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.sun.jna.Platform;

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
import ch.supertomcat.supertomcatutils.io.FileUtil;
import ch.supertomcat.supertomcatutils.io.ZipUtil;
import ch.supertomcat.supertomcatutils.process.EvelatedProcessExecutor;
import ch.supertomcat.supertomcatutils.process.linux.LinuxEvelatedProcessExecutor;
import ch.supertomcat.supertomcatutils.process.macos.MacOSEvelatedProcessExecutor;
import ch.supertomcat.supertomcatutils.process.windows.WindowsEvelatedProcessExecutor;
import ch.supertomcat.updaterxml.UpdateXmlIO;
import ch.supertomcat.updaterxml.update.xml.CopyDirectoryActionDefinition;
import ch.supertomcat.updaterxml.update.xml.DeleteFileActionDefinition;
import ch.supertomcat.updaterxml.update.xml.SelfUpdateActionDefinition;
import ch.supertomcat.updaterxml.update.xml.StartProcessActionDefinition;
import ch.supertomcat.updaterxml.update.xml.Update;
import jakarta.xml.bind.JAXBException;

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
	 * Update XML IO
	 */
	private final UpdateXmlIO updateXMLIO;

	/**
	 * Constructor
	 * 
	 * @param updateSource UpdateSource
	 * @param hostManager Host Manager
	 * @param guiEvent GUI Event
	 * @throws JAXBException
	 * @throws SAXException
	 * @throws IOException
	 */
	public UpdateManager(UpdateSource updateSource, HostManager hostManager, GuiEvent guiEvent) throws IOException, SAXException, JAXBException {
		if (updateSource == null) {
			throw new IllegalArgumentException("No source for updates available!");
		}
		this.updateSource = updateSource;
		this.guiEvent = guiEvent;
		this.hostManager = hostManager;
		this.updateXMLIO = new UpdateXmlIO();
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
			downloadUpdates(wrappedUpdates.getRedirectUpdates(), tempHostsDirectory, UpdateType.TYPE_REDIRECT_PLUGIN);
			downloadUpdates(wrappedUpdates.getHosterUpdates(), tempHostsDirectory, UpdateType.TYPE_HOST_PLUGIN);

			Path tempRulesDirectory = tempDirectory.resolve("rules");
			downloadUpdates(wrappedUpdates.getRuleUpdates(), tempRulesDirectory, UpdateType.TYPE_RULE);

			logger.info("Downloads successful");

			boolean selfUpdate = false;
			Path updaterJarPath = getUpdaterJarPath(tempMainDirectory);
			if (updaterJarPath != null) {
				selfUpdate = true;
			} else {
				updaterJarPath = getUpdaterJarPath(applicationPath);
			}

			if (updaterJarPath == null) {
				logger.error("Update installation failed. Could not find updater");
				fireErrorOccured("Update installation failed. Could not find updater", null);
				if (mainUpdateAvailable) {
					fireNewProgramVersionInstallFailed();
				}
				fireUpdatesFailed();
				return;
			}

			Path updateXMLFilePath = tempDirectory.resolve("update.xml");

			Update updateXMLDefinition = new Update();
			addDeleteFiles(updateXMLDefinition, wrappedUpdates.getRedirectUpdates(), hostsDirectory);
			addDeleteFiles(updateXMLDefinition, wrappedUpdates.getHosterUpdates(), hostsDirectory);
			addDeleteFiles(updateXMLDefinition, wrappedUpdates.getRuleUpdates(), rulesDirectory);
			addCopyDirectory(updateXMLDefinition, tempHostsDirectory, hostsDirectory, false);
			addCopyDirectory(updateXMLDefinition, tempRulesDirectory, rulesDirectory, false);
			if (mainUpdateAvailable) {
				addCopyDirectory(updateXMLDefinition, tempMainDirectory, applicationPath, true);
			}
			if (selfUpdate) {
				addUpdaterSelfUpdate(updateXMLDefinition, applicationPath);
			}
			addStartProcess(updateXMLDefinition, applicationPath);
			writeUpdateXMLFile(updateXMLDefinition, updateXMLFilePath);

			JOptionPane.showMessageDialog(owner, Localization.getString("ProgrammExitBecauseUpdate"), Localization.getString("Update"), JOptionPane.WARNING_MESSAGE);

			logger.info("Start updater and exit");
			if (mainUpdateAvailable) {
				fireNewProgramVersionInstalled();
			}
			fireUpdatesComplete();

			if (!startUpdater(updateXMLFilePath, updaterJarPath)) {
				logger.error("Update installation failed. Could not launche updater");
				fireErrorOccured("Update installation failed. Could not launche updater", null);
				if (mainUpdateAvailable) {
					fireNewProgramVersionInstallFailed();
				}
				fireUpdatesFailed();
				return;
			}

			logger.info("Exit BH without restart");
			guiEvent.exitAppForced(false, true);
		} catch (UpdateException | IOException | JAXBException e) {
			logger.error("Update installation failed", e);
			fireErrorOccured("Update installation failed", e);
			if (mainUpdateAvailable) {
				fireNewProgramVersionInstallFailed();
			}
			fireUpdatesFailed();
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
	 * Check if delete updates available
	 * 
	 * @param wrappedUpdateDataList List
	 * @return True if delete updates available, false otherwise
	 */
	private boolean checkDeleteUpdatesAvaialble(List<WrappedUpdateData> wrappedUpdateDataList) {
		return wrappedUpdateDataList.stream().anyMatch(x -> x.isDelete() || x.isSubDelete());
	}

	private void addCopyDirectory(Update updateXMLDefinition, Path sourceDirectory, Path targetDirectory, boolean recursive) {
		if (!Files.exists(sourceDirectory)) {
			logger.info("Source directory does not exist, does not need to be copied: {}", sourceDirectory);
			return;
		}
		CopyDirectoryActionDefinition copyDirectoryDefinition = new CopyDirectoryActionDefinition();
		copyDirectoryDefinition.setSourceDirectory(sourceDirectory.toAbsolutePath().toString());
		copyDirectoryDefinition.setTargetDirectory(targetDirectory.toAbsolutePath().toString());
		copyDirectoryDefinition.setRecursive(recursive);
		updateXMLDefinition.getActions().add(copyDirectoryDefinition);
	}

	private void addDeleteFiles(Update updateXMLDefinition, List<WrappedUpdateData> wrappedUpdateDataList, Path directory) {
		List<WrappedUpdateData> deletes = wrappedUpdateDataList.stream().filter(WrappedUpdateData::isDelete).toList();
		for (WrappedUpdateData wrappedUpdateData : deletes) {
			UpdateData updateData = wrappedUpdateData.getUpdateData();
			Path targetFile = directory.resolve(updateData.getFilename());
			addDeleteFile(updateXMLDefinition, targetFile);
		}

		List<UpdateDataAdditionalSource> subDeletes = wrappedUpdateDataList.stream().filter(WrappedUpdateData::isSubDelete).map(WrappedUpdateData::getUpdateData).flatMap(x -> x.getSource().stream())
				.filter(x -> x.getDelete() != null).toList();
		for (UpdateDataAdditionalSource additionalSource : subDeletes) {
			Path targetFile = directory.resolve(additionalSource.getFilename());
			addDeleteFile(updateXMLDefinition, targetFile);
		}
	}

	private void addDeleteFile(Update updateXMLDefinition, Path targetFile) {
		DeleteFileActionDefinition deleteFileDefinition = new DeleteFileActionDefinition();
		deleteFileDefinition.setFile(targetFile.toAbsolutePath().toString());
		updateXMLDefinition.getActions().add(deleteFileDefinition);
	}

	private void addUpdaterSelfUpdate(Update updateXMLDefinition, Path applicationPath) {
		SelfUpdateActionDefinition selfUpdateDefinition = new SelfUpdateActionDefinition();
		selfUpdateDefinition.setTargetDirectory(applicationPath.resolve("updater").toAbsolutePath().toString());
		updateXMLDefinition.getActions().add(selfUpdateDefinition);
	}

	private void addStartProcess(Update updateXMLDefinition, Path applicationPath) {
		String jarFilename = ApplicationProperties.getProperty(ApplicationMain.JAR_FILENAME);
		if (jarFilename == null || jarFilename.isEmpty()) {
			logger.error("JarFilename Application Property is null or empty: {}. Updater will not start BH after updates.", jarFilename);
			return;
		}

		String applicationAbsolutePath = applicationPath.toAbsolutePath().toString();
		if (!applicationAbsolutePath.endsWith(FileUtil.FILE_SEPERATOR)) {
			applicationAbsolutePath += FileUtil.FILE_SEPERATOR;
		}

		String javaExePath = getJavaExePath();
		if (javaExePath == null) {
			logger.error("Could not find java executable. Updater will not start BH after updates.");
			return;
		}

		List<String> arguments = new ArrayList<>();
		arguments.add(javaExePath);
		arguments.add("-jar");
		arguments.add(applicationAbsolutePath + jarFilename);

		StartProcessActionDefinition startProcessActionDefinition = new StartProcessActionDefinition();
		startProcessActionDefinition.setWorkingDirectory(applicationPath.toAbsolutePath().toString());
		startProcessActionDefinition.getCommand().addAll(arguments);
		updateXMLDefinition.getActions().add(startProcessActionDefinition);
	}

	/**
	 * TODO Already available in ApplicationMain, but as instance method. Should be made available static in utils. Copied to code for now.
	 * 
	 * @return Java Executable Path or null
	 */
	protected String getJavaExePath() {
		String jreBinPath = System.getProperty("java.home") + FileUtil.FILE_SEPERATOR + "bin" + FileUtil.FILE_SEPERATOR;

		String jreJavaw = jreBinPath + "javaw";
		String jreJava = jreBinPath + "java";

		Path fJreJavaw;
		Path fJreJava;
		if (Platform.isWindows()) {
			fJreJavaw = Paths.get(jreJavaw + ".exe");
			fJreJava = Paths.get(jreJava + ".exe");
		} else {
			fJreJavaw = Paths.get(jreJavaw);
			fJreJava = Paths.get(jreJava);
		}

		if (Files.exists(fJreJavaw)) {
			logger.info("Updater not found: {}", fJreJavaw);
			return jreJavaw;
		}

		if (Files.exists(fJreJava)) {
			logger.info("Updater not found: {}", fJreJava);
			return jreJava;
		}

		logger.info("Java exe not found");
		return null;
	}

	private void writeUpdateXMLFile(Update updateXMLDefinition, Path xmlFile) throws IOException, JAXBException {
		updateXMLIO.writeUpdate(xmlFile, updateXMLDefinition);
		String xmlString = new String(Files.readAllBytes(xmlFile), StandardCharsets.UTF_8);
		logger.info("Update XML File written to {}:\n{}", xmlFile, xmlString);
	}

	private Path getUpdaterJarPath(Path directory) {
		String updateJarFilename = "Updater.jar";
		Path updaterJarPath = directory.resolve("updater").resolve(updateJarFilename);
		/*
		 * Choose updater from temp directory
		 */
		if (Files.exists(updaterJarPath)) {
			logger.info("Updater found: {}", updaterJarPath);
			return updaterJarPath;
		}

		logger.info("Updater not found in: {}", directory);
		return null;
	}

	private boolean startUpdater(Path updateXMLFilePath, Path updaterJarPath) {
		try {
			logger.info("Start upater: XMLFile: {}, UpdaterJarPath: {}", updateXMLFilePath, updaterJarPath);
			EvelatedProcessExecutor executor;
			if (Platform.isWindows()) {
				executor = new WindowsEvelatedProcessExecutor();
			} else if (Platform.isMac()) {
				executor = new MacOSEvelatedProcessExecutor();
			} else {
				executor = new LinuxEvelatedProcessExecutor();
			}

			String javaExePath = getJavaExePath();
			if (javaExePath == null) {
				logger.error("Could not find java executable");
				return false;
			}

			List<String> arguments = new ArrayList<>();
			arguments.add(javaExePath);
			arguments.add("-jar");
			arguments.add(updaterJarPath.toAbsolutePath().toString());
			arguments.add("-update");
			arguments.add(updateXMLFilePath.toAbsolutePath().toString());

			executor.startProcess(null, arguments);
			return true;
		} catch (Exception e) {
			logger.error("Could not start updater", e);
			return false;
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

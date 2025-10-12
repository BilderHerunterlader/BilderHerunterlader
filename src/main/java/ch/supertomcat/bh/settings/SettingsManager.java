package ch.supertomcat.bh.settings;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.logging.log4j.Level;
import org.xml.sax.SAXException;

import ch.supertomcat.bh.settings.oldsettingsconverter.OldSettingsConverter;
import ch.supertomcat.bh.settings.options.Subdir;
import ch.supertomcat.bh.settings.xml.ConnectionSettings;
import ch.supertomcat.bh.settings.xml.CustomSetting;
import ch.supertomcat.bh.settings.xml.DirectorySettings;
import ch.supertomcat.bh.settings.xml.DownloadSettings;
import ch.supertomcat.bh.settings.xml.GUISettings;
import ch.supertomcat.bh.settings.xml.HostDeactivationSetting;
import ch.supertomcat.bh.settings.xml.HosterSettings;
import ch.supertomcat.bh.settings.xml.HostsSettings;
import ch.supertomcat.bh.settings.xml.KeywordsSettings;
import ch.supertomcat.bh.settings.xml.LogLevelSetting;
import ch.supertomcat.bh.settings.xml.LookAndFeelSetting;
import ch.supertomcat.bh.settings.xml.ObjectFactory;
import ch.supertomcat.bh.settings.xml.RegexReplaceSetting;
import ch.supertomcat.bh.settings.xml.Settings;
import ch.supertomcat.bh.settings.xml.SubDirSetting;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.formatter.UnitFormatUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;
import ch.supertomcat.supertomcatutils.regex.RegexReplace;
import ch.supertomcat.supertomcatutils.regex.RegexReplacePipeline;
import ch.supertomcat.supertomcatutils.settings.SettingsManagerBase;
import ch.supertomcat.supertomcatutils.settings.SettingsUtil;
import jakarta.xml.bind.JAXBException;

/**
 * Class which handels the settings
 * There are also methods to get hoster-classes the possibility
 * to save their own settings.
 */
public class SettingsManager extends SettingsManagerBase<Settings, BHSettingsListener> {
	/**
	 * Resource Path to the default settings file
	 */
	private static final String DEFAULT_SETTINGS_FILE_RESOURCE_PATH = "/ch/supertomcat/bh/settings/default-settings.xml";

	/**
	 * Resource Path to settings schema file
	 */
	private static final String SETTINGS_SCHEMA_FILE_RESOURCE_PATH = "/ch/supertomcat/bh/settings/settings.xsd";

	/**
	 * Default User Agent
	 */
	private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:6.5) Goanna/20231221 PaleMoon/32.5.2";

	/**
	 * Log Level Mapping
	 */
	private static final Map<LogLevelSetting, Level> LOG_LEVEL_MAPPING = new LinkedHashMap<>();

	static {
		LOG_LEVEL_MAPPING.put(LogLevelSetting.TRACE, Level.TRACE);
		LOG_LEVEL_MAPPING.put(LogLevelSetting.DEBUG, Level.DEBUG);
		LOG_LEVEL_MAPPING.put(LogLevelSetting.INFO, Level.INFO);
		LOG_LEVEL_MAPPING.put(LogLevelSetting.WARN, Level.WARN);
		LOG_LEVEL_MAPPING.put(LogLevelSetting.ERROR, Level.ERROR);
		LOG_LEVEL_MAPPING.put(LogLevelSetting.FATAL, Level.FATAL);
	}

	/**
	 * Flag if the Application is started at first time
	 */
	private boolean languageFirstRun = true;

	/**
	 * Hoster Settings Map
	 */
	private final Map<String, HosterSettings> hosterSettingsMap = new HashMap<>();

	/**
	 * regexReplacePipelinePageTitle
	 */
	private RegexReplacePipeline regexReplacePipelinePageTitle = new RegexReplacePipeline("regexReplacePageTitle");

	/**
	 * regexReplacePipelineFilename
	 */
	private RegexReplacePipeline regexReplacePipelineFilename = new RegexReplacePipeline("regexReplaceFilename");

	/**
	 * Subdirs
	 */
	private List<Subdir> subDirs = new ArrayList<>();

	/**
	 * Deactivated Hosts
	 */
	private Map<String, Boolean> deactivatedHosts = new HashMap<>();

	/**
	 * Portable Save Path
	 */
	private String portableSavePath = null;

	/**
	 * Old Settings Converter
	 */
	private final OldSettingsConverter oldSettingsConverter;

	/**
	 * Dummy Constructor. ONLY USE FOR UNIT TESTS.
	 * 
	 * @throws JAXBException
	 */
	protected SettingsManager() throws JAXBException {
		super(ObjectFactory.class, DEFAULT_SETTINGS_FILE_RESOURCE_PATH, SETTINGS_SCHEMA_FILE_RESOURCE_PATH);
		this.oldSettingsConverter = null;
	}

	/**
	 * Constructor
	 * 
	 * @param strSettingsFolder Settings Folder
	 * @param strSettingsFilename Settings Filename
	 * @param oldSettingsFilename Old Settings Filename (For backward compatibility, to be loaded, when new settings filename is not available)
	 * @throws JAXBException
	 */
	public SettingsManager(String strSettingsFolder, String strSettingsFilename, String oldSettingsFilename) throws JAXBException {
		super(strSettingsFolder, strSettingsFilename, ObjectFactory.class, DEFAULT_SETTINGS_FILE_RESOURCE_PATH, SETTINGS_SCHEMA_FILE_RESOURCE_PATH);
		this.oldSettingsConverter = new OldSettingsConverter(strSettingsFolder, oldSettingsFilename);

		// If path for downloads are overridden by directories.properties file, then we need to set the path here
		String portableDownloadPath = ApplicationProperties.getProperty("DownloadPath");
		if (portableDownloadPath != null) {
			if (!portableDownloadPath.endsWith("/") && !portableDownloadPath.endsWith("\\")) {
				portableDownloadPath += FileUtil.FILE_SEPERATOR;
			}
			portableSavePath = portableDownloadPath;
		}
	}

	/**
	 * Read the Settings
	 * 
	 * @return True if successful, false otherwise
	 */
	public synchronized boolean readSettings() {
		if (Files.exists(settingsFile)) {
			logger.info("Loading Settings File: {}", settingsFile);
			try {
				this.settings = loadUserSettingsFile();
				checkSettings();
				updateHosterSettingsMap();
				applyLogLevel();
				applyRegexPipelines();
				applySubDirs();
				applyHostDeactivations();
				languageFirstRun = false;
				settingsChanged();
				return true;
			} catch (Exception e) {
				logger.error("Could not read settings file: {}", settingsFile, e);

				String[] options = { "Replace with default settings", "Exit" };
				var selectedOption = JOptionPane.showOptionDialog(null, "BH: Could not read settings file: " + settingsFile.toAbsolutePath().toString()
						+ "\nChoose how to proceed", "Error", JOptionPane.YES_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
				if (selectedOption == 0) {
					return readDefaultSettings(false, true);
				} else {
					return false;
				}
			}
		} else {
			return readDefaultSettings(true, false);
		}
	}

	/**
	 * Read default settings
	 * 
	 * @param checkConvertOldSettings True if it should be checked for old settings file and convert it, false otherwise
	 * @param forceWrite True if it should be force to write settings to file, false otherwise
	 * @return True if successful, false otherwise
	 */
	private synchronized boolean readDefaultSettings(boolean checkConvertOldSettings, boolean forceWrite) {
		logger.info("Loading Default Settings File");
		try {
			this.settings = loadDefaultSettingsFile();
			applyDynamicDefaultSettings();
		} catch (Exception e) {
			logger.error("Could not read default settings file", e);
			return false;
		}

		boolean converted = false;
		if (checkConvertOldSettings && oldSettingsConverter != null && oldSettingsConverter.checkOldSettingsFileExists()) {
			oldSettingsConverter.convertSettings(settings);
			checkSettings();
			converted = true;
			languageFirstRun = false;
		}

		try {
			updateHosterSettingsMap();
			applyLogLevel();
			applyRegexPipelines();
			applySubDirs();
			applyHostDeactivations();
			settingsChanged();
			if (converted || forceWrite) {
				writeSettings(true);
			}
			return true;
		} catch (Exception e) {
			logger.error("Could not read default settings file", e);
			return false;
		}
	}

	/**
	 * Check settings
	 */
	private void checkSettings() {
		String savePath = settings.getDirectorySettings().getSavePath();
		if (!savePath.endsWith("/") && !savePath.endsWith("\\")) {
			settings.getDirectorySettings().setSavePath(savePath + FileUtil.FILE_SEPERATOR);
		}

		int availableProcessors = Runtime.getRuntime().availableProcessors();
		if (!checkIntValue(1, availableProcessors, settings.getThreadCount())) {
			settings.setThreadCount(availableProcessors);
		}

		if (!checkIntValue(1, 50, settings.getConnectionSettings().getMaxConnections())) {
			settings.getConnectionSettings().setMaxConnections(32);
		}

		if (!checkIntValue(0, 50, settings.getConnectionSettings().getMaxConnectionsPerHost())) {
			settings.getConnectionSettings().setMaxConnectionsPerHost(8);
		}

		if (!checkIntValue(100, 1000, settings.getGuiSettings().getPreviewSize())) {
			settings.getGuiSettings().setPreviewSize(100);
		}

		if (!checkIntValueNonNegative(settings.getDownloadSettings().getMaxFailedCount())) {
			settings.getDownloadSettings().setMaxFailedCount(2);
		}

		if (!checkIntValueNonNegative(settings.getDownloadSettings().getMinFileSize())) {
			settings.getDownloadSettings().setMinFileSize(0);
		}

		if (!checkIntValue(0, 65535, settings.getWebExtensionPort())) {
			settings.setWebExtensionPort(35990);
		}

		MappedLookAndFeelSetting mappedLookAndFeelSetting = MappedLookAndFeelSetting.getByXMLValue(settings.getGuiSettings().getLookAndFeel());
		if (!mappedLookAndFeelSetting.isAvailable()) {
			settings.getGuiSettings().setLookAndFeel(LookAndFeelSetting.LAF_DEFAULT);
		}

		if (settings.getDownloadSettings().isReduceFilenameLength() == null) {
			settings.getDownloadSettings().setReduceFilenameLength(true);
		}

		if (settings.getDownloadSettings().isReducePathLength() == null) {
			settings.getDownloadSettings().setReducePathLength(true);
		}
	}

	private boolean checkIntValue(int min, int max, int value) {
		return value >= min && value <= max;
	}

	private boolean checkIntValueNonNegative(int value) {
		return value >= 0;
	}

	private void applyDynamicDefaultSettings() {
		if (portableSavePath != null) {
			settings.getDirectorySettings().setSavePath(portableSavePath);
		} else {
			settings.getDirectorySettings().setSavePath(ApplicationProperties.getProperty("SettingsPath"));
		}

		int availableProcessors = Runtime.getRuntime().availableProcessors();
		settings.setThreadCount(availableProcessors);
	}

	/**
	 * Update Hoster Settings Map
	 */
	private synchronized void updateHosterSettingsMap() {
		for (HosterSettings hosterSettings : this.settings.getHosterSettings()) {
			this.hosterSettingsMap.put(hosterSettings.getName(), hosterSettings);
		}
	}

	private void applyRegexPipelines() {
		regexReplacePipelinePageTitle = new RegexReplacePipeline("regexReplacePageTitle", getRegexReplaces(settings.getGuiSettings().getRegexReplacesPageTitle(), "regexReplacePageTitle"));
		regexReplacePipelineFilename = new RegexReplacePipeline("regexReplaceFilename", getRegexReplaces(settings.getDownloadSettings().getRegexReplacesFilename(), "regexReplaceFilename"));
	}

	private void applySubDirs() {
		subDirs.clear();
		for (SubDirSetting subDirSetting : settings.getDirectorySettings().getSubDirSettings()) {
			subDirs.add(new Subdir(subDirSetting));
		}
	}

	private void applyHostDeactivations() {
		for (HostDeactivationSetting hostDeactivation : settings.getHostsSettings().getDeactivations()) {
			this.deactivatedHosts.put(hostDeactivation.getValue(), hostDeactivation.isDeactivated());
		}
	}

	private List<RegexReplace> getRegexReplaces(List<RegexReplaceSetting> regexReplaceSettings, String pipelineName) {
		List<RegexReplace> regexps = new ArrayList<>();

		for (RegexReplaceSetting regexReplaceSetting : regexReplaceSettings) {
			try {
				regexps.add(new RegexReplace(regexReplaceSetting.getPattern(), regexReplaceSetting.getReplacement()));
			} catch (Exception ex) {
				logger.error("Could not load regexp from RegexReplaceSetting for {}: Pattern: '{}', Replacement: '{}'", pipelineName, regexReplaceSetting.getPattern(), regexReplaceSetting
						.getReplacement(), ex);
				throw ex;
			}
		}

		return regexps;
	}

	/**
	 * Save the Settings
	 * 
	 * @param noShutdown When the application is not shutdowned
	 * @return True if successful, false otherwise
	 */
	public synchronized boolean writeSettings(boolean noShutdown) {
		try (OutputStream out = Files.newOutputStream(settingsFile)) {
			writeSettingsFile(this.settings, out, false);
			if (noShutdown) {
				backupSettingsFile();
			}
			return true;
		} catch (IOException | SAXException | JAXBException e) {
			logger.error("Could not write settings file: {}", settingsFile, e);
			return false;
		}
	}

	private void convertToXMLRegexReplaceSettings(RegexReplacePipeline regexReplacePipeline, List<RegexReplaceSetting> xmlRegexList) {
		xmlRegexList.clear();
		for (RegexReplace regexp : regexReplacePipeline.getRegexps()) {
			RegexReplaceSetting regexReplaceSetting = new RegexReplaceSetting();
			regexReplaceSetting.setPattern(regexp.getSearch());
			regexReplaceSetting.setReplacement(regexp.getReplace());
			xmlRegexList.add(regexReplaceSetting);
		}
	}

	/**
	 * Returns the regexReplacePipelinePageTitle
	 * 
	 * @return regexReplacePipelinePageTitle
	 */
	public RegexReplacePipeline getRegexReplacePipelinePageTitle() {
		return regexReplacePipelinePageTitle;
	}

	/**
	 * Apply Regex Replace Pipeline for Page Titles to XML Settings
	 */
	public void applyRegexReplacePipelinePageTitleToXMLSettings() {
		convertToXMLRegexReplaceSettings(regexReplacePipelinePageTitle, settings.getGuiSettings().getRegexReplacesPageTitle());
	}

	/**
	 * Returns the regexReplacePipelineFilename
	 * 
	 * @return regexReplacePipelineFilename
	 */
	public RegexReplacePipeline getRegexReplacePipelineFilename() {
		return regexReplacePipelineFilename;
	}

	/**
	 * Apply Regex Replace Pipeline for Filenames to XML Settings
	 */
	public void applyRegexReplacePipelineFilenameToXMLSettings() {
		convertToXMLRegexReplaceSettings(regexReplacePipelineFilename, settings.getDownloadSettings().getRegexReplacesFilename());
	}

	/**
	 * @return the languageFirstRun
	 */
	public boolean isLanguageFirstRun() {
		return languageFirstRun;
	}

	/**
	 * @param language Language
	 */
	public void setLanguage(String language) {
		settings.getGuiSettings().setLanguage(language);
		languageFirstRun = false;
	}

	/**
	 * Apply the log level
	 */
	private void applyLogLevel() {
		Level log4jLevel = LOG_LEVEL_MAPPING.get(settings.getLogLevel());
		if (log4jLevel == null) {
			logger.error("Unsupported log level: {}", settings.getLogLevel());
			return;
		}
		BHUtil.changeLog4JRootLoggerLevel(log4jLevel);
	}

	/**
	 * Sets the log level
	 * 
	 * @param logLevel Log Level
	 */
	public synchronized void setLogLevel(LogLevelSetting logLevel) {
		if (logLevel == null) {
			throw new IllegalArgumentException("logLevel is null");
		}

		Level log4jLevel = LOG_LEVEL_MAPPING.get(logLevel);
		if (log4jLevel == null) {
			logger.error("Unsupported log level: {}", logLevel);
			return;
		}
		settings.setLogLevel(logLevel);
		BHUtil.changeLog4JRootLoggerLevel(log4jLevel);
	}

	/**
	 * @param lookAndFeel Look and Feel
	 * @return True if look and feel changed, false otherwise
	 */
	public boolean setLookAndFeel(LookAndFeelSetting lookAndFeel) {
		LookAndFeelSetting previousLookAndFeel = settings.getGuiSettings().getLookAndFeel();
		settings.getGuiSettings().setLookAndFeel(lookAndFeel);
		boolean lookAndFeelChanged = previousLookAndFeel != lookAndFeel;
		if (lookAndFeelChanged) {
			try {
				MappedLookAndFeelSetting mappedLookAndFeel = MappedLookAndFeelSetting.getByXMLValue(lookAndFeel);
				UIManager.setLookAndFeel(mappedLookAndFeel.getClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
				logger.error("Could not set LookAndFeel", e1);
			}

			for (BHSettingsListener listener : listeners) {
				listener.lookAndFeelChanged(lookAndFeel);
			}
		}
		return lookAndFeelChanged;
	}

	/**
	 * @return Save Path
	 */
	public String getSavePath() {
		return settings.getDirectorySettings().getSavePath();
	}

	/**
	 * @return Default User Agent
	 */
	public String getDefaultUserAgent() {
		return DEFAULT_USER_AGENT;
	}

	/**
	 * @return User Agent
	 */
	public String getUserAgent() {
		String userAgentFromSettings = settings.getConnectionSettings().getUserAgent();
		if (userAgentFromSettings != null && !userAgentFromSettings.isEmpty()) {
			return userAgentFromSettings;
		}
		return DEFAULT_USER_AGENT;
	}

	/**
	 * Returns the subDirs
	 * 
	 * @return subDirs
	 */
	public List<Subdir> getSubDirs() {
		return subDirs;
	}

	/**
	 * @param subDirs Sub Directories
	 */
	public void setSubdirs(List<Subdir> subDirs) {
		this.subDirs = subDirs;

		List<SubDirSetting> xmlSubDirSettings = settings.getDirectorySettings().getSubDirSettings();
		xmlSubDirSettings.clear();
		for (Subdir subDir : subDirs) {
			SubDirSetting subDirSetting = new SubDirSetting();
			subDirSetting.setName(subDir.getSubdirName());
			subDirSetting.setMin(subDir.getMinSize());
			subDirSetting.setMax(subDir.getMaxSize());
			subDirSetting.setResMinWidth(subDir.getMinWidth());
			subDirSetting.setResMinHeight(subDir.getMinHeight());
			subDirSetting.setResMaxWidth(subDir.getMaxWidth());
			subDirSetting.setResMaxHeight(subDir.getMaxHeight());
			xmlSubDirSettings.add(subDirSetting);
		}
	}

	/**
	 * @return the allowedFilenameChars
	 */
	public int getAllowedFilenameChars() {
		switch (settings.getDownloadSettings().getAllowedFilenameCharacters()) {
			case ALL:
				return FileUtil.FILENAME_ALL;
			case ASCII_ONLY:
				return FileUtil.FILENAME_ASCII_ONLY;
			case ASCII_UMLAUT:
				return FileUtil.FILENAME_ASCII_UMLAUT;
			default:
				logger.error("Unknown allowed filename characters value: {}", settings.getDownloadSettings().getAllowedFilenameCharacters());
				return FileUtil.FILENAME_ALL;
		}
	}

	/**
	 * Returns the settings
	 * 
	 * @return settings
	 */
	public DirectorySettings getDirectorySettings() {
		return settings.getDirectorySettings();
	}

	/**
	 * Returns the settings
	 * 
	 * @return settings
	 */
	public ConnectionSettings getConnectionSettings() {
		return settings.getConnectionSettings();
	}

	/**
	 * Returns the settings
	 * 
	 * @return settings
	 */
	public DownloadSettings getDownloadsSettings() {
		return settings.getDownloadSettings();
	}

	/**
	 * Returns the settings
	 * 
	 * @return settings
	 */
	public HostsSettings getHostsSettings() {
		return settings.getHostsSettings();
	}

	/**
	 * Returns the settings
	 * 
	 * @return settings
	 */
	public KeywordsSettings getKeywordsSettings() {
		return settings.getKeywordsSettings();
	}

	/**
	 * Returns the settings
	 * 
	 * @return settings
	 */
	public GUISettings getGUISettings() {
		return settings.getGuiSettings();
	}

	/**
	 * Returns the saveTableColumnSizes
	 * 
	 * @return saveTableColumnSizes
	 */
	public boolean isSaveTableColumnSizes() {
		return settings.getGuiSettings().isSaveTableColumnSizes();
	}

	/**
	 * Returns the colWidthsQueue
	 * 
	 * @return colWidthsQueue
	 */
	public String getColWidthsQueue() {
		return settings.getGuiSettings().getColWidthsQueue();
	}

	/**
	 * Sets the colWidthsQueue
	 * 
	 * @param colWidthsQueue colWidthsQueue
	 */
	public void setColWidthsQueue(String colWidthsQueue) {
		settings.getGuiSettings().setColWidthsQueue(colWidthsQueue);
	}

	/**
	 * Returns the colWidthsLog
	 * 
	 * @return colWidthsLog
	 */
	public String getColWidthsLog() {
		return settings.getGuiSettings().getColWidthsLog();
	}

	/**
	 * Sets the colWidthsLog
	 * 
	 * @param colWidthsLog colWidthsLog
	 */
	public void setColWidthsLog(String colWidthsLog) {
		settings.getGuiSettings().setColWidthsLog(colWidthsLog);
	}

	/**
	 * Returns the colWidthsKeywords
	 * 
	 * @return colWidthsKeywords
	 */
	public String getColWidthsKeywords() {
		return settings.getGuiSettings().getColWidthsKeywords();
	}

	/**
	 * Sets the colWidthsKeywords
	 * 
	 * @param colWidthsKeywords colWidthsKeywords
	 */
	public void setColWidthsKeywords(String colWidthsKeywords) {
		settings.getGuiSettings().setColWidthsKeywords(colWidthsKeywords);
	}

	/**
	 * Returns the colWidthsRules
	 * 
	 * @return colWidthsRules
	 */
	public String getColWidthsRules() {
		return settings.getGuiSettings().getColWidthsRules();
	}

	/**
	 * Sets the colWidthsRules
	 * 
	 * @param colWidthsRules colWidthsRules
	 */
	public void setColWidthsRules(String colWidthsRules) {
		settings.getGuiSettings().setColWidthsRules(colWidthsRules);
	}

	/**
	 * Returns the colWidthsRulesEditor
	 * 
	 * @return colWidthsRulesEditor
	 */
	public String getColWidthsRulesEditor() {
		return settings.getGuiSettings().getColWidthsRulesEditor();
	}

	/**
	 * Sets the colWidthsRulesEditor
	 * 
	 * @param colWidthsRulesEditor colWidthsRulesEditor
	 */
	public void setColWidthsRulesEditor(String colWidthsRulesEditor) {
		settings.getGuiSettings().setColWidthsRulesEditor(colWidthsRulesEditor);
	}

	/**
	 * Returns the colWidthsHosts
	 * 
	 * @return colWidthsHosts
	 */
	public String getColWidthsHosts() {
		return settings.getGuiSettings().getColWidthsHosts();
	}

	/**
	 * Sets the colWidthsHosts
	 * 
	 * @param colWidthsHosts colWidthsHosts
	 */
	public void setColWidthsHosts(String colWidthsHosts) {
		settings.getGuiSettings().setColWidthsHosts(colWidthsHosts);
	}

	/**
	 * Returns the colWidthsSubdirs
	 * 
	 * @return colWidthsSubdirs
	 */
	public String getColWidthsSubdirs() {
		return settings.getGuiSettings().getColWidthsSubdirs();
	}

	/**
	 * Sets the colWidthsSubdirs
	 * 
	 * @param colWidthsSubdirs colWidthsSubdirs
	 */
	public void setColWidthsSubdirs(String colWidthsSubdirs) {
		settings.getGuiSettings().setColWidthsSubdirs(colWidthsSubdirs);
	}

	/**
	 * Returns the colWidthsUpdate
	 * 
	 * @return colWidthsUpdate
	 */
	public String getColWidthsUpdate() {
		return settings.getGuiSettings().getColWidthsUpdate();
	}

	/**
	 * Sets the colWidthsUpdate
	 * 
	 * @param colWidthsUpdate colWidthsUpdate
	 */
	public void setColWidthsUpdate(String colWidthsUpdate) {
		settings.getGuiSettings().setColWidthsUpdate(colWidthsUpdate);
	}

	/**
	 * Returns the colWidthsAdder
	 * 
	 * @return colWidthsAdder
	 */
	public String getColWidthsAdder() {
		return settings.getGuiSettings().getColWidthsAdder();
	}

	/**
	 * Sets the colWidthsAdder
	 * 
	 * @param colWidthsAdder colWidthsAdder
	 */
	public void setColWidthsAdder(String colWidthsAdder) {
		settings.getGuiSettings().setColWidthsAdder(colWidthsAdder);
	}

	/**
	 * Returns the colWidthsAdderKeywordSelectorFilename
	 * 
	 * @return colWidthsAdderKeywordSelectorFilename
	 */
	public String getColWidthsAdderKeywordSelectorFilename() {
		return settings.getGuiSettings().getColWidthsAdderKeywordSelectorFilename();
	}

	/**
	 * Sets the colWidthsAdderKeywordSelectorFilename
	 * 
	 * @param colWidthsAdderKeywordSelectorFilename colWidthsAdderKeywordSelectorFilename
	 */
	public void setColWidthsAdderKeywordSelectorFilename(String colWidthsAdderKeywordSelectorFilename) {
		settings.getGuiSettings().setColWidthsAdderKeywordSelectorFilename(colWidthsAdderKeywordSelectorFilename);
	}

	/**
	 * Returns the saveTableSortOrders
	 * 
	 * @return saveTableSortOrders
	 */
	public boolean isSaveTableSortOrders() {
		return settings.getGuiSettings().isSaveTableSortOrders();
	}

	/**
	 * Sets the saveTableSortOrders
	 * 
	 * @param saveTableSortOrders saveTableSortOrders
	 */
	public void setSaveTableSortOrders(boolean saveTableSortOrders) {
		settings.getGuiSettings().setSaveTableSortOrders(saveTableSortOrders);
	}

	/**
	 * Returns the tableSortOrdersKeywords
	 * 
	 * @return tableSortOrdersKeywords
	 */
	public String getTableSortOrdersKeywords() {
		return settings.getGuiSettings().getTableSortOrdersKeywords();
	}

	/**
	 * Sets the tableSortOrdersKeywords
	 * 
	 * @param tableSortOrdersKeywords tableSortOrdersKeywords
	 */
	public void setTableSortOrdersKeywords(String tableSortOrdersKeywords) {
		settings.getGuiSettings().setTableSortOrdersKeywords(tableSortOrdersKeywords);
	}

	/**
	 * @return the sizeView
	 */
	public int getSizeView() {
		switch (settings.getGuiSettings().getSizeDisplayMode()) {
			case AUTO_CHANGE_SIZE:
				return UnitFormatUtil.AUTO_CHANGE_SIZE;
			case ONLY_B:
				return UnitFormatUtil.ONLY_B;
			case ONLY_KIB:
				return UnitFormatUtil.ONLY_KIB;
			case ONLY_MIB:
				return UnitFormatUtil.ONLY_MIB;
			case ONLY_GIB:
				return UnitFormatUtil.ONLY_GIB;
			case ONLY_TIB:
				return UnitFormatUtil.ONLY_TIB;
			default:
				logger.error("Unknown size display mode: {}", settings.getGuiSettings().getSizeDisplayMode());
				return UnitFormatUtil.AUTO_CHANGE_SIZE;
		}
	}

	/**
	 * @param name Name
	 * @param deactivated Deactivated
	 */
	public void setHostDeactivated(String name, boolean deactivated) {
		this.deactivatedHosts.put(name, deactivated);
		HostDeactivationSetting hostDeactivation = settings.getHostsSettings().getDeactivations().stream().filter(x -> x.getValue().equals(name)).findFirst().orElse(null);
		if (hostDeactivation == null) {
			hostDeactivation = new HostDeactivationSetting();
			hostDeactivation.setValue(name);
			settings.getHostsSettings().getDeactivations().add(hostDeactivation);
		}
		hostDeactivation.setDeactivated(deactivated);
	}

	/**
	 * @param name Name
	 * @return Host Deactivated
	 */
	public boolean isHostDeactivated(String name) {
		Boolean deactivated = this.deactivatedHosts.get(name);
		if (deactivated == null) {
			return false;
		} else {
			return deactivated;
		}
	}

	/**
	 * Get Hoster Settings for given Hoster Name
	 * 
	 * @param hosterName Hoster Name
	 * @return Hoster Settings or null if not found
	 */
	private HosterSettings getHosterSettings(String hosterName) {
		return hosterSettingsMap.get(hosterName);
	}

	/**
	 * Get data type for value
	 * 
	 * @param <E> Value Type
	 * @param value Value
	 * @return Data Type
	 */
	private <E> String getHostSettingValueDataType(E value) {
		return SettingsUtil.getValueDataType(value);
	}

	/**
	 * Get value as String
	 * 
	 * @param <E> Value Type
	 * @param value Value
	 * @return Value as String
	 */
	private <E> String getHostSettingValueAsString(E value) {
		return SettingsUtil.getValueAsString(value);
	}

	/**
	 * Set Hoster Setting Value
	 * 
	 * @param <E> Value Type
	 * @param hosterName Hoster Name
	 * @param key Key
	 * @param value Value
	 */
	public <E> void setHosterSettingValue(String hosterName, String key, E value) {
		String dataType = getHostSettingValueDataType(value);
		String strValue = getHostSettingValueAsString(value);
		if (dataType == null || strValue == null) {
			throw new IllegalArgumentException("Type is not allowed. You can only use Boolean, Byte, Double, Float, Integer, Long, Short or String");
		}

		setHosterSettingValue(hosterName, key, dataType, strValue);
	}

	/**
	 * Set Hoster Setting Value
	 * 
	 * @param hosterName Hoster Name
	 * @param key Key
	 * @param dataType Data Type
	 * @param value Value
	 */
	public void setHosterSettingValue(String hosterName, String key, String dataType, String value) {
		HosterSettings hosterSettings = getHosterSettings(hosterName);
		if (hosterSettings == null) {
			hosterSettings = new HosterSettings();
			hosterSettings.setName(hosterName);
			hosterSettingsMap.put(hosterName, hosterSettings);
			settings.getHosterSettings().add(hosterSettings);
		}
		List<CustomSetting> customSettings = hosterSettings.getSettings();
		CustomSetting customSetting = customSettings.stream().filter(x -> x.getName().equals(key)).findFirst().orElse(null);
		if (customSetting == null) {
			customSetting = new CustomSetting();
			customSettings.add(customSetting);
		}
		customSetting.setDataType(dataType);
		customSetting.setName(key);
		customSetting.setValue(value);
	}

	/**
	 * Get Hoster Setting
	 * 
	 * @param hosterName Hoster Name
	 * @param key Key
	 * @return Custom Setting or null
	 */
	private CustomSetting getHosterSetting(String hosterName, String key) {
		HosterSettings hosterSettings = getHosterSettings(hosterName);
		if (hosterSettings == null) {
			return null;
		}
		return hosterSettings.getSettings().stream().filter(x -> x.getName().equals(key)).findFirst().orElse(null);
	}

	/**
	 * Get Hoster Setting Value
	 * 
	 * @param <E> Value Type
	 * @param hosterName Hoster Name
	 * @param key Key
	 * @param valueType Value Type
	 * @param defaultValue Default Value
	 * @return Value
	 */
	public <E> E getHosterSettingValue(String hosterName, String key, Class<E> valueType, E defaultValue) {
		E value = getHosterSettingValue(hosterName, key, valueType);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * Get Hoster Setting Value
	 * 
	 * @param <E> Value Type
	 * @param hosterName Hoster Name
	 * @param key Key
	 * @param valueType Value Type
	 * @return Value
	 */
	public <E> E getHosterSettingValue(String hosterName, String key, Class<E> valueType) {
		CustomSetting customSetting = getHosterSetting(hosterName, key);
		if (customSetting == null) {
			return null;
		}
		return parseCustomSettingValue(customSetting, valueType);
	}

	/**
	 * Get Hoster Settings Values for given Hoster Name
	 * 
	 * @param hosterName Hoster Name
	 * @return Hoster Settings or an empty Map if not found
	 */
	public Map<String, Object> getHosterSettingsValues(String hosterName) {
		Map<String, Object> settingsValues = new HashMap<>();
		HosterSettings hosterSettings = getHosterSettings(hosterName);
		if (hosterSettings == null || hosterSettings.getSettings().isEmpty()) {
			return settingsValues;
		}
		for (CustomSetting customSetting : hosterSettings.getSettings()) {
			Object value = parseCustomSettingValue(customSetting);
			settingsValues.put(customSetting.getName(), value);
		}
		return settingsValues;
	}

	/**
	 * Parse Custom Setting Value
	 * 
	 * @param customSetting Custom Setting
	 * @return Parsed Value
	 */
	private Object parseCustomSettingValue(CustomSetting customSetting) {
		String dataType = customSetting.getDataType();
		String strValue = customSetting.getValue();
		return SettingsUtil.parseValue(dataType, strValue);
	}

	/**
	 * Parse Custom Setting Value
	 * 
	 * @param <E> Value Type
	 * @param customSetting Custom Setting
	 * @param valueType
	 * @return Parsed Value
	 */
	private <E> E parseCustomSettingValue(CustomSetting customSetting, Class<E> valueType) {
		String dataType;
		if (valueType == Boolean.class) {
			dataType = "boolean";
		} else if (valueType == Byte.class) {
			dataType = "byte";
		} else if (valueType == Double.class) {
			dataType = "double";
		} else if (valueType == Float.class) {
			dataType = "float";
		} else if (valueType == Integer.class) {
			dataType = "int";
		} else if (valueType == Long.class) {
			dataType = "long";
		} else if (valueType == Short.class) {
			dataType = "short";
		} else if (valueType == String.class) {
			dataType = "string";
		} else {
			throw new IllegalArgumentException("Type is not allowed. You can only use Boolean, Byte, Double, Float, Integer, Long, Short or String");
		}
		String strValue = customSetting.getValue();
		Object value = SettingsUtil.parseValue(dataType, strValue);
		return valueType.cast(value);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @return Wert
	 */
	public synchronized boolean getBooleanValue(String host, String key) {
		return getHosterSettingValue(host, key, Boolean.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @param defautValue Default Value
	 * @return Wert
	 */
	public synchronized boolean getBooleanValue(String host, String key, boolean defautValue) {
		return getHosterSettingValue(host, key, Boolean.class, defautValue);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @return Wert
	 */
	public synchronized int getIntValue(String host, String key) {
		return getHosterSettingValue(host, key, Integer.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @param defautValue Default Value
	 * @return Wert
	 */
	public synchronized int getIntValue(String host, String key, int defautValue) {
		return getHosterSettingValue(host, key, Integer.class, defautValue);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @return Wert
	 */
	public synchronized String getStringValue(String host, String key) {
		return getHosterSettingValue(host, key, String.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @param defautValue Default Value
	 * @return Wert
	 */
	public synchronized String getStringValue(String host, String key, String defautValue) {
		return getHosterSettingValue(host, key, String.class, defautValue);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @return Wert
	 */
	public synchronized long getLongValue(String host, String key) {
		return getHosterSettingValue(host, key, Long.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @param defautValue Default Value
	 * @return Wert
	 */
	public synchronized long getLongValue(String host, String key, long defautValue) {
		return getHosterSettingValue(host, key, Long.class, defautValue);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @return Wert
	 */
	public synchronized byte getByteValue(String host, String key) {
		return getHosterSettingValue(host, key, Byte.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @param defautValue Default Value
	 * @return Wert
	 */
	public synchronized byte getByteValue(String host, String key, byte defautValue) {
		return getHosterSettingValue(host, key, Byte.class, defautValue);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @return Wert
	 */
	public synchronized short getShortValue(String host, String key) {
		return getHosterSettingValue(host, key, Short.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @param defautValue Default Value
	 * @return Wert
	 */
	public synchronized short getShortValue(String host, String key, short defautValue) {
		return getHosterSettingValue(host, key, Short.class, defautValue);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @return Wert
	 */
	public synchronized float getFloatValue(String host, String key) {
		return getHosterSettingValue(host, key, Float.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @param defautValue Default Value
	 * @return Wert
	 */
	public synchronized float getFloatValue(String host, String key, float defautValue) {
		return getHosterSettingValue(host, key, Float.class, defautValue);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @return Wert
	 */
	public synchronized double getDoubleValue(String host, String key) {
		return getHosterSettingValue(host, key, Double.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param host Host
	 * @param key Key
	 * @param defautValue Default Value
	 * @return Wert
	 */
	public synchronized double getDoubleValue(String host, String key, double defautValue) {
		return getHosterSettingValue(host, key, Double.class, defautValue);
	}

	/**
	 * @param count Count
	 */
	public synchronized void increaseOverallDownloadedFiles(int count) {
		settings.getDownloadSettings().setOverallDownloadedFiles(settings.getDownloadSettings().getOverallDownloadedFiles() + count);
	}

	/**
	 * @param size Size
	 */
	public synchronized void increaseOverallDownloadedBytes(long size) {
		settings.getDownloadSettings().setOverallDownloadedBytes(settings.getDownloadSettings().getOverallDownloadedBytes() + size);
	}

	/**
	 * Fire Settings Changed
	 */
	public void fireSettingsChanged() {
		settingsChanged();
	}
}

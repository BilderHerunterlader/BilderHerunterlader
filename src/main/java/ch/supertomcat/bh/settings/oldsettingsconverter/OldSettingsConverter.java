package ch.supertomcat.bh.settings.oldsettingsconverter;

import static ch.supertomcat.bh.settings.oldsettingsconverter.SettingsToolkit.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.settings.xml.AllowedFilenameCharacters;
import ch.supertomcat.bh.settings.xml.AutoTargetDirMode;
import ch.supertomcat.bh.settings.xml.BrowserCookiesMode;
import ch.supertomcat.bh.settings.xml.BrowserCookiesSetting;
import ch.supertomcat.bh.settings.xml.ConnectionSettings;
import ch.supertomcat.bh.settings.xml.CustomSetting;
import ch.supertomcat.bh.settings.xml.DirectorySettings;
import ch.supertomcat.bh.settings.xml.DownloadSettings;
import ch.supertomcat.bh.settings.xml.GUISettings;
import ch.supertomcat.bh.settings.xml.HostDeactivationSetting;
import ch.supertomcat.bh.settings.xml.HosterSettings;
import ch.supertomcat.bh.settings.xml.HostsSettings;
import ch.supertomcat.bh.settings.xml.KeywordMatchMode;
import ch.supertomcat.bh.settings.xml.KeywordsSettings;
import ch.supertomcat.bh.settings.xml.LogLevelSetting;
import ch.supertomcat.bh.settings.xml.LookAndFeelSetting;
import ch.supertomcat.bh.settings.xml.ProgressDisplayMode;
import ch.supertomcat.bh.settings.xml.ProxyMode;
import ch.supertomcat.bh.settings.xml.ProxySettings;
import ch.supertomcat.bh.settings.xml.RegexReplaceSetting;
import ch.supertomcat.bh.settings.xml.Settings;
import ch.supertomcat.bh.settings.xml.SizeDisplayMode;
import ch.supertomcat.bh.settings.xml.SubDirSetting;
import ch.supertomcat.bh.settings.xml.SubdirsResolutionMode;
import ch.supertomcat.bh.settings.xml.WindowSettings;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.io.CopyUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Class which handels the settings
 * There are also methods to get hoster-classes the possibility
 * to save their own settings.
 */
public class OldSettingsConverter {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(OldSettingsConverter.class);

	/**
	 * Automatic Download Directory Recognition by Title
	 */
	private static final int BY_TITLE = 0;

	/**
	 * Automatic Download Directory Recognition by Filename
	 */
	private static final int BY_FILENAME = 1;

	/**
	 * Only the lower component of the resolution is checked
	 */
	private static final int RESOLUTION_ONLY_LOWER = 0;

	/**
	 * Only the higher component of the resolution is checked
	 */
	private static final int RESOLUTION_ONLY_HIGHER = 1;

	/**
	 * Only width is checked
	 */
	private static final int RESOLUTION_ONLY_WIDTH = 2;

	/**
	 * Only height is checked
	 */
	private static final int RESOLUTION_ONLY_HEIGHT = 3;

	/**
	 * Both
	 */
	private static final int RESOLUTION_BOTH = 4;

	/**
	 * Direct connection
	 */
	private static final int DIRECT_CONNECTION = 0;

	/**
	 * Connection over a proxy
	 */
	private static final int HTTP_PROXY = 1;

	/**
	 * No Cookies
	 */
	private static final int BROWSER_NO_COOKIES = 0;

	/**
	 * Cookies from Internet Explorer
	 */
	private static final int BROWSER_IE = 1;

	/**
	 * Cookies from Firefox
	 */
	private static final int BROWSER_FIREFOX = 2;

	/**
	 * Cookies from Opera up to Version 12
	 */
	private static final int BROWSER_OPERA = 3;

	/**
	 * Cookies from Firefox
	 */
	private static final int BROWSER_PALE_MOON = 4;

	/**
	 * Cookies from Opera from Version 13 or higher
	 */
	private static final int BROWSER_OPERA_NEW = 5;

	/**
	 * Takes automatically a suitable unit for displaying filesize
	 */
	private static final int AUTO_CHANGE_SIZE = 0;

	/**
	 * Display filesize only as bytes
	 */
	private static final int ONLY_B = 1;

	/**
	 * Display filesize only as kibibytes
	 */
	private static final int ONLY_KIB = 2;

	/**
	 * Display filesize only as mebibytes
	 */
	private static final int ONLY_MIB = 3;

	/**
	 * Display filesize only as gibibytes
	 */
	private static final int ONLY_GIB = 4;

	/**
	 * Display filesize only as tebibytes
	 */
	private static final int ONLY_TIB = 5;

	/**
	 * Display progressbar and only percent
	 */
	private static final int PROGRESSBAR_PERCENT = 0;

	/**
	 * Display progressbar and only filesize
	 */
	private static final int PROGRESSBAR_SIZE = 1;

	/**
	 * Display no progressbar and only percent
	 */
	private static final int NOPROGRESSBAR_PERCENT = 2;

	/**
	 * Display no progressbar and only filesize
	 */
	private static final int NOPROGRESSBAR_SIZE = 3;

	/**
	 * Use java-default-theme
	 */
	private static final int LAF_DEFAULT = 0;

	/**
	 * Use operating-system-theme
	 */
	private static final int LAF_OS = 1;

	/**
	 * Metal
	 */
	private static final int LAF_METAL = 2;

	/**
	 * Windows
	 */
	private static final int LAF_WINDOWS = 3;

	/**
	 * Windows Classic
	 */
	private static final int LAF_WINDOWS_CLASSIC = 4;

	/**
	 * Motif
	 */
	private static final int LAF_MOTIF = 5;

	/**
	 * GTK
	 */
	private static final int LAF_GTK = 6;

	/**
	 * MacOS
	 */
	private static final int LAF_MACOS = 7;

	/**
	 * Nimbus (since jre6 update 10)
	 */
	private static final int LAF_NIMBUS = 8;

	/**
	 * Filter all non-ascii chars from paths and filenames
	 */
	private static final int FILENAME_ASCII_ONLY = 0;

	/**
	 * Filter all non-ascii and non-umlaut chars from paths and filenames
	 */
	private static final int FILENAME_ASCII_UMLAUT = 1;

	/**
	 * Filter only not allowed chars from paths and filenames
	 */
	private static final int FILENAME_ALL = 2;

	/**
	 * Only exact matches
	 */
	private static final int MATCH_ONLY_EXACT = 0;

	/**
	 * All keywords found, but strict search
	 */
	private static final int MATCH_ALL_STRICT = 1;

	/**
	 * All keywords found
	 */
	private static final int MATCH_ALL = 2;

	/**
	 * Restricted Settingspaths
	 * This Settings can not be overwritten by the set*Value-Methods!
	 */
	private static final String[] RESTRICTED_PATHS = { "Directories.SavePath", "Directories.RememberLastUsedPath", "Directories.AutoTargetDir", "Directories.AutoTargetDirMode", "Directories.subdirsEnabled", "Directories.subdirsResolutionMode", "Connection.Connections", "Connection.connectionsPerHost", "Connection.Proxy.Mode", "Connection.Proxy.Name", "Connection.Proxy.Port", "Connection.Proxy.User", "Connection.Proxy.Passwort", "Connection.Proxy.Auth", "Connection.cookiesFromBrowser", "Connection.cookieFileOpera", "Connection.cookieFileOperaFixed", "Connection.cookieFileOperaNew", "Connection.cookieFileOperaNewFixed", "Connection.cookieFileFirefox", "Connection.cookieFileFirefoxFixed", "Connection.cookieFilePaleMoon", "Connection.cookieFilePaleMoonFixed", "Connection.userAgent", "GUI.Language", "GUI.Window.Width", "GUI.Window.Height", "GUI.Window.X", "GUI.Window.Y", "GUI.Window.State", "GUI.Window.Save", "GUI.DownloadSelectionWindow.Width", "GUI.DownloadSelectionWindow.Height", "GUI.DownloadSelectionWindow.X", "GUI.DownloadSelectionWindow.Y", "GUI.DownloadSelectionWindow.Save", "GUI.colWidthsQueue", "GUI.colWidthsLog", "GUI.colWidthsKeywords", "GUI.colWidthsRules", "GUI.colWidthsRulesEditor", "GUI.colWidthsHosts", "GUI.colWidthsSubdirs", "GUI.colWidthsUpdate", "GUI.colWidthsAdder", "GUI.colWidthsAdderKeywordSelectorFilename", "GUI.saveTableColumnSizes", "GUI.tableSortOrdersKeywords", "GUI.saveTableSortOrders", "GUI.Size", "GUI.Progress", "GUI.Bitrate", "GUI.LAF", "GUI.LogDays", "GUI.currentDownloadLogFile", "GUI.AlwaysAddTitle", "GUI.adderAdd", "GUI.DeselectNoKeyword", "GUI.DeleteNoKeyword", "GUI.targetDirChangeHistory", "GUI.FilenameChangeHistory", "GUI.FilenameChangePrefix", "GUI.AppendPrefixFilenameChange", "GUI.FilenameChangeAppendix", "GUI.AppendAppendixFilenameChange", "GUI.filenameChangeKeepOriginal", "GUI.DownloadRate", "GUI.downloadsCompleteNotification", "GUI.directoryLog.FilterEnabled", "GUI.directoryLog.DirCount", "GUI.directoryLog.OnlyExisting", "GUI.lastUsedImportDialogPath", "GUI.lastUsedExportDialogPath", "GUI.downloadPreviews", "GUI.previewSize", "Keywords.FilterEnabled", "Keywords.MatchMode", "Keywords.displayKeywordsWhenNoMatches", "Hosts.RulesBeforeClasses", "Downloads.AutoStartDownloads", "Downloads.DownloadedBytes", "Downloads.DownloadedFiles", "Downloads.SaveLogs", "Downloads.useOldDownloadLogMode", "Downloads.MaxFailedCount", "Downloads.MinFilesize", "Downloads.Timeout", "Downloads.sortDownloadsOnStart", "Downloads.autoRetryAfterDownloadsComplete", "Other.Updates", "Other.CheckClipboard", "Other.WebExtensionPort", "Other.allowedFilenameChars", "Other.backupDbOnStart", "Other.defragDBOnStart", "Other.defragMinFilesize", "Other.debugLevel", "Other.threadCount" };

	/**
	 * Restricted Settingspaths and subpaths
	 * This Settings and all subpaths of it can not be overwritten by the set*Value-Methods!
	 */
	private static final String[] RESTRICTED_START_WITH_PATHS = { "Directories.Subdir", "GUI.regexReplacePageTitle", "Downloads.regexReplaceFilename", "GUI.FilenameChangeHistory", "GUI.targetDirChangeHistory", "GUI.adderAdd", "Hosts.deactivatedHosts" };

	/**
	 * Restricted Settingspaths and subpaths
	 * This Settings and all subpaths of it can not be overwritten by the set*Value-Methods!
	 */
	private static final String[] RESTRICTED_ROOT_PATHS = { "Directories", "Connection", "Keywords", "Downloads", "GUI", "Hosts", "Other" };

	/**
	 * Pfad to the settings-folder
	 */
	private final String strSettingsPath;

	/**
	 * Path to the settings-file
	 */
	private final String strSettingsFile;

	/**
	 * Path to the backup of the settings-file
	 */
	private final String strSettingsFileBackup;

	/**
	 * Constructor
	 * 
	 * @param strSettingsFolder Settings Folder
	 * @param strSettingsFilename Settings Filename
	 */
	public OldSettingsConverter(String strSettingsFolder, final String strSettingsFilename) {
		strSettingsPath = strSettingsFolder;
		strSettingsFile = strSettingsPath + strSettingsFilename;
		strSettingsFileBackup = strSettingsFile + ".backup";

		File settingsPath = new File(strSettingsPath);
		File settingsFile = new File(strSettingsFile);
		File settingsFileBackup = new File(strSettingsFileBackup);

		if (!settingsFile.exists()) {
			logger.info("Old Settingsfile not found in folder '{}': {}", settingsPath.getAbsolutePath(), settingsFile.getAbsolutePath());
			if (settingsFileBackup.exists() && settingsFileBackup.length() > 0) {
				logger.info("Restoring Old Settingsfile with backup: {}", settingsFileBackup.getAbsolutePath());
				CopyUtil.copy(strSettingsFileBackup, strSettingsFile);
			}
		} else {
			if (settingsFile.length() == 0) {
				logger.error("Old Settingsfile is empty: {}", settingsFile.getAbsolutePath());
				if (settingsFileBackup.exists() && settingsFileBackup.length() > 0) {
					logger.info("Restoring Old Settingsfile with backup: {}", settingsFileBackup.getAbsolutePath());
					CopyUtil.copy(strSettingsFileBackup, strSettingsFile);
				}
			}
		}
	}

	/**
	 * Check if old settings file exists
	 * 
	 * @return True if old settings file exists, false otherwise
	 */
	public boolean checkOldSettingsFileExists() {
		File settingsFile = new File(strSettingsFile);
		if (settingsFile.exists() && settingsFile.isFile()) {
			logger.info("Old settings file does exist: Folder: {}, Filename: {}", strSettingsPath, strSettingsFile);
			return true;
		}
		return false;
	}

	/**
	 * Convert the Settings
	 * 
	 * @param settings Default Settings
	 * @return True if settings could be read, false otherwise
	 */
	public boolean convertSettings(Settings settings) {
		try {
			File settingsFile = new File(strSettingsFile);
			logger.info("Convert old settings file: Folder: {}, Filename: {}", strSettingsPath, strSettingsFile);

			SAXBuilder b = new SAXBuilder();
			Document doc = b.build(settingsFile);
			Element root = doc.getRootElement();

			/* Directories */
			DirectorySettings directorySettings = settings.getDirectorySettings();

			// SavePath
			String savePath = readStringValue("Directories.SavePath", root, directorySettings.getSavePath());
			if (!savePath.endsWith("/") && !savePath.endsWith("\\")) {
				savePath += FileUtil.FILE_SEPERATOR;
			}
			directorySettings.setSavePath(savePath);

			boolean saveLastPath = readBooleanValue("Directories.RememberLastUsedPath", root, directorySettings.isRememberLastUsedPath());
			directorySettings.setRememberLastUsedPath(saveLastPath);

			// AutoTargetDir
			boolean autoTargetdir = readBooleanValue("Directories.AutoTargetDir", root, directorySettings.isAutoTargetDir());
			directorySettings.setAutoTargetDir(autoTargetdir);

			// AutoTargetDirMode
			int atdm = readIntValue("Directories.AutoTargetDirMode", root, -1);
			switch (atdm) {
				case BY_TITLE:
					directorySettings.setAutoTargetDirMode(AutoTargetDirMode.BY_TITLE);
					break;
				case BY_FILENAME:
					directorySettings.setAutoTargetDirMode(AutoTargetDirMode.BY_FILENAME);
					break;
			}

			// Subdirs
			boolean subdirsEnabled = readBooleanValue("Directories.subdirsEnabled", root, directorySettings.isSubDirsEnabled());
			directorySettings.setSubDirsEnabled(subdirsEnabled);

			int srm = readIntValue("Directories.subdirsResolutionMode", root, -1);
			switch (srm) {
				case RESOLUTION_ONLY_LOWER:
					directorySettings.setSubDirsResolutionMode(SubdirsResolutionMode.RESOLUTION_ONLY_LOWER);
					break;
				case RESOLUTION_ONLY_HIGHER:
					directorySettings.setSubDirsResolutionMode(SubdirsResolutionMode.RESOLUTION_ONLY_HIGHER);
					break;
				case RESOLUTION_ONLY_WIDTH:
					directorySettings.setSubDirsResolutionMode(SubdirsResolutionMode.RESOLUTION_ONLY_WIDTH);
					break;
				case RESOLUTION_ONLY_HEIGHT:
					directorySettings.setSubDirsResolutionMode(SubdirsResolutionMode.RESOLUTION_ONLY_HEIGHT);
					break;
				case RESOLUTION_BOTH:
					directorySettings.setSubDirsResolutionMode(SubdirsResolutionMode.RESOLUTION_BOTH);
					break;
			}

			/* Connection */
			ConnectionSettings connectionSettings = settings.getConnectionSettings();

			// Connections
			int con = readIntValue("Connection.Connections", root, -1);
			if ((con <= 50) && (con >= 1)) {
				connectionSettings.setMaxConnections(con);
			}

			int conph = readIntValue("Connection.connectionsPerHost", root, -1);
			if ((conph <= 50) && (conph >= 0)) {
				connectionSettings.setMaxConnectionsPerHost(conph);
			}

			// Proxy
			ProxySettings proxySettings = connectionSettings.getProxy();
			int pm = readIntValue("Connection.Proxy.Mode", root, -1);
			switch (pm) {
				case DIRECT_CONNECTION:
					proxySettings.setMode(ProxyMode.DIRECT_CONNECTION);
					break;
				case HTTP_PROXY:
					proxySettings.setMode(ProxyMode.PROXY);
					break;
			}

			String proxyname = readStringValue("Connection.Proxy.Name", root, proxySettings.getHost());
			int proxyport = readIntValue("Connection.Proxy.Port", root, proxySettings.getPort());
			String proxyuser = readStringValue("Connection.Proxy.User", root, proxySettings.getUser());
			String proxypassword = readStringValue("Connection.Proxy.Passwort", root, proxySettings.getPassword());
			boolean proxyauth = readBooleanValue("Connection.Proxy.Auth", root, proxySettings.isAuth());

			proxySettings.setHost(proxyname);
			proxySettings.setPort(proxyport);
			proxySettings.setUser(proxyuser);
			proxySettings.setPassword(proxypassword);
			proxySettings.setAuth(proxyauth);

			// Cookies
			int cfb = readIntValue("Connection.cookiesFromBrowser", root, -1);
			switch (cfb) {
				case BROWSER_NO_COOKIES:
					connectionSettings.setBrowserCookiesMode(BrowserCookiesMode.NO_COOKIES);
					break;
				case BROWSER_IE:
					connectionSettings.setBrowserCookiesMode(BrowserCookiesMode.BROWSER_IE);
					break;
				case BROWSER_FIREFOX:
					connectionSettings.setBrowserCookiesMode(BrowserCookiesMode.BROWSER_FIREFOX);
					break;
				case BROWSER_OPERA:
					connectionSettings.setBrowserCookiesMode(BrowserCookiesMode.BROWSER_OPERA);
					break;
				case BROWSER_PALE_MOON:
					connectionSettings.setBrowserCookiesMode(BrowserCookiesMode.BROWSER_PALE_MOON);
					break;
				case BROWSER_OPERA_NEW:
					connectionSettings.setBrowserCookiesMode(BrowserCookiesMode.BROWSER_OPERA_NEW);
					break;
			}

			BrowserCookiesSetting operaBrowserCookiesSetting = connectionSettings.getBrowserCookiesOpera();
			String cookieFileOpera = readStringValue("Connection.cookieFileOpera", root, "");
			boolean cookieFileOperaFixed = readBooleanValue("Connection.cookieFileOperaFixed", root, false);
			operaBrowserCookiesSetting.setCookieFile(cookieFileOpera);
			operaBrowserCookiesSetting.setCookieFileFixed(cookieFileOperaFixed);

			BrowserCookiesSetting operaNewBrowserCookiesSetting = connectionSettings.getBrowserCookiesOpera();
			String cookieFileOperaNew = readStringValue("Connection.cookieFileOperaNew", root, "");
			boolean cookieFileOperaNewFixed = readBooleanValue("Connection.cookieFileOperaNewFixed", root, false);
			operaNewBrowserCookiesSetting.setCookieFile(cookieFileOperaNew);
			operaNewBrowserCookiesSetting.setCookieFileFixed(cookieFileOperaNewFixed);

			BrowserCookiesSetting firefoxBrowserCookiesSetting = connectionSettings.getBrowserCookiesOpera();
			String cookieFileFirefox = readStringValue("Connection.cookieFileFirefox", root, "");
			boolean cookieFileFirefoxFixed = readBooleanValue("Connection.cookieFileFirefoxFixed", root, false);
			firefoxBrowserCookiesSetting.setCookieFile(cookieFileFirefox);
			firefoxBrowserCookiesSetting.setCookieFileFixed(cookieFileFirefoxFixed);

			BrowserCookiesSetting palemoonBrowserCookiesSetting = connectionSettings.getBrowserCookiesOpera();
			String cookieFilePaleMoon = readStringValue("Connection.cookieFilePaleMoon", root, "");
			boolean cookieFilePaleMoonFixed = readBooleanValue("Connection.cookieFilePaleMoonFixed", root, false);
			palemoonBrowserCookiesSetting.setCookieFile(cookieFilePaleMoon);
			palemoonBrowserCookiesSetting.setCookieFileFixed(cookieFilePaleMoonFixed);

			// User-Agent
			String userAgent = readStringValue("Connection.userAgent", root, connectionSettings.getUserAgent());
			connectionSettings.setUserAgent(userAgent);

			/* GUI */
			GUISettings guiSettings = settings.getGuiSettings();

			// Language
			String lang = readStringValue("GUI.Language", root, null);
			guiSettings.setLanguage(lang);

			// Window
			WindowSettings mainWindowSetting = guiSettings.getMainWindow();
			int windowWidth = readIntValue("GUI.Window.Width", root, mainWindowSetting.getWidth());
			int windowHeight = readIntValue("GUI.Window.Height", root, mainWindowSetting.getHeight());
			int windowXPos = readIntValue("GUI.Window.X", root, mainWindowSetting.getX());
			int windowYPos = readIntValue("GUI.Window.Y", root, mainWindowSetting.getY());
			int windowState = readIntValue("GUI.Window.State", root, mainWindowSetting.getState());
			boolean saveWindowSizePosition = readBooleanValue("GUI.Window.Save", root, mainWindowSetting.isSave());
			mainWindowSetting.setWidth(windowWidth);
			mainWindowSetting.setHeight(windowHeight);
			mainWindowSetting.setX(windowXPos);
			mainWindowSetting.setY(windowYPos);
			mainWindowSetting.setState(windowState);
			mainWindowSetting.setSave(saveWindowSizePosition);

			// Download-Selection-Window
			WindowSettings downloadSelectionWindowSetting = guiSettings.getDownloadSelectionWindow();
			int downloadSelectionWindowWidth = readIntValue("GUI.DownloadSelectionWindow.Width", root, mainWindowSetting.getWidth());
			int downloadSelectionWindowHeight = readIntValue("GUI.DownloadSelectionWindow.Height", root, mainWindowSetting.getHeight());
			int downloadSelectionWindowXPos = readIntValue("GUI.DownloadSelectionWindow.X", root, mainWindowSetting.getX());
			int downloadSelectionWindowYPos = readIntValue("GUI.DownloadSelectionWindow.Y", root, mainWindowSetting.getState());
			boolean saveDownloadSelectionWindowSizePosition = readBooleanValue("GUI.DownloadSelectionWindow.Save", root, mainWindowSetting.isSave());
			downloadSelectionWindowSetting.setWidth(downloadSelectionWindowWidth);
			downloadSelectionWindowSetting.setHeight(downloadSelectionWindowHeight);
			downloadSelectionWindowSetting.setX(downloadSelectionWindowXPos);
			downloadSelectionWindowSetting.setY(downloadSelectionWindowYPos);
			downloadSelectionWindowSetting.setSave(saveDownloadSelectionWindowSizePosition);

			String colWidthsQueue = readStringValue("GUI.colWidthsQueue", root, guiSettings.getColWidthsQueue());
			String colWidthsLog = readStringValue("GUI.colWidthsLog", root, guiSettings.getColWidthsLog());
			String colWidthsKeywords = readStringValue("GUI.colWidthsKeywords", root, guiSettings.getColWidthsKeywords());
			String colWidthsRules = readStringValue("GUI.colWidthsRules", root, guiSettings.getColWidthsRules());
			String colWidthsRulesEditor = readStringValue("GUI.colWidthsRulesEditor", root, guiSettings.getColWidthsRulesEditor());
			String colWidthsHosts = readStringValue("GUI.colWidthsHosts", root, guiSettings.getColWidthsHosts());
			String colWidthsSubdirs = readStringValue("GUI.colWidthsSubdirs", root, guiSettings.getColWidthsSubdirs());
			String colWidthsUpdate = readStringValue("GUI.colWidthsUpdate", root, guiSettings.getColWidthsUpdate());
			String colWidthsAdder = readStringValue("GUI.colWidthsAdder", root, guiSettings.getColWidthsAdder());
			String colWidthsAdderKeywordSelectorFilename = readStringValue("GUI.colWidthsAdderKeywordSelectorFilename", root, guiSettings.getColWidthsAdderKeywordSelectorFilename());

			guiSettings.setColWidthsQueue(colWidthsQueue);
			guiSettings.setColWidthsLog(colWidthsLog);
			guiSettings.setColWidthsKeywords(colWidthsKeywords);
			guiSettings.setColWidthsRules(colWidthsRules);
			guiSettings.setColWidthsRulesEditor(colWidthsRulesEditor);
			guiSettings.setColWidthsHosts(colWidthsHosts);
			guiSettings.setColWidthsSubdirs(colWidthsSubdirs);
			guiSettings.setColWidthsUpdate(colWidthsUpdate);
			guiSettings.setColWidthsAdder(colWidthsAdder);
			guiSettings.setColWidthsAdderKeywordSelectorFilename(colWidthsAdderKeywordSelectorFilename);

			boolean saveTableColumnSizes = readBooleanValue("GUI.saveTableColumnSizes", root, guiSettings.isSaveTableColumnSizes());
			guiSettings.setSaveTableColumnSizes(saveTableColumnSizes);

			String tableSortOrdersKeywords = readStringValue("GUI.tableSortOrdersKeywords", root, guiSettings.getTableSortOrdersKeywords());
			guiSettings.setTableSortOrdersKeywords(tableSortOrdersKeywords);

			boolean saveTableSortOrders = readBooleanValue("GUI.saveTableSortOrders", root, guiSettings.isSaveTableSortOrders());
			guiSettings.setSaveTableSortOrders(saveTableSortOrders);

			// Display
			int sv = readIntValue("GUI.Size", root, -1);
			switch (sv) {
				case AUTO_CHANGE_SIZE:
					guiSettings.setSizeDisplayMode(SizeDisplayMode.AUTO_CHANGE_SIZE);
					break;
				case ONLY_B:
					guiSettings.setSizeDisplayMode(SizeDisplayMode.ONLY_B);
					break;
				case ONLY_KIB:
					guiSettings.setSizeDisplayMode(SizeDisplayMode.ONLY_KIB);
					break;
				case ONLY_MIB:
					guiSettings.setSizeDisplayMode(SizeDisplayMode.ONLY_MIB);
					break;
				case ONLY_GIB:
					guiSettings.setSizeDisplayMode(SizeDisplayMode.ONLY_GIB);
					break;
				case ONLY_TIB:
					guiSettings.setSizeDisplayMode(SizeDisplayMode.ONLY_TIB);
					break;
			}

			int pv = readIntValue("GUI.Progress", root, -1);
			switch (pv) {
				case PROGRESSBAR_PERCENT:
				case NOPROGRESSBAR_PERCENT:
					guiSettings.setProgressDisplayMode(ProgressDisplayMode.PROGRESSBAR_PERCENT);
					break;
				case PROGRESSBAR_SIZE:
				case NOPROGRESSBAR_SIZE:
					guiSettings.setProgressDisplayMode(ProgressDisplayMode.PROGRESSBAR_SIZE);
					break;
			}

			DownloadSettings downloadSettings = settings.getDownloadSettings();

			// Log
			String cdlf = readStringValue("GUI.currentDownloadLogFile", root, downloadSettings.getCurrentDownloadLogFile());
			File fileCDLF = new File(ApplicationProperties.getProperty("DownloadLogPath") + cdlf);
			if (fileCDLF.exists() && fileCDLF.isFile()) {
				downloadSettings.setCurrentDownloadLogFile(cdlf);
			}

			// DownloadRate
			boolean downloadRate = readBooleanValue("GUI.DownloadRate", root, guiSettings.isDownloadRate());
			guiSettings.setDownloadRate(downloadRate);

			// LookAndFeel
			int laf = readIntValue("GUI.LAF", root, LAF_DEFAULT);
			switch (laf) {
				case LAF_DEFAULT:
					guiSettings.setLookAndFeel(LookAndFeelSetting.LAF_DEFAULT);
					break;
				case LAF_OS:
					guiSettings.setLookAndFeel(LookAndFeelSetting.LAF_OS);
					break;
				case LAF_METAL:
					guiSettings.setLookAndFeel(LookAndFeelSetting.LAF_METAL);
					break;
				case LAF_WINDOWS:
					guiSettings.setLookAndFeel(LookAndFeelSetting.LAF_WINDOWS);
					break;
				case LAF_WINDOWS_CLASSIC:
					guiSettings.setLookAndFeel(LookAndFeelSetting.LAF_WINDOWS_CLASSIC);
					break;
				case LAF_MOTIF:
					guiSettings.setLookAndFeel(LookAndFeelSetting.LAF_MOTIF);
					break;
				case LAF_GTK:
					guiSettings.setLookAndFeel(LookAndFeelSetting.LAF_GTK);
					break;
				case LAF_MACOS:
					guiSettings.setLookAndFeel(LookAndFeelSetting.LAF_MACOS);
					break;
				case LAF_NIMBUS:
					guiSettings.setLookAndFeel(LookAndFeelSetting.LAF_NIMBUS);
					break;
			}

			// Targetdirectory and title
			boolean alwaysAddTitle = readBooleanValue("GUI.AlwaysAddTitle", root, guiSettings.isAlwaysAddTitle());
			guiSettings.setAlwaysAddTitle(alwaysAddTitle);

			// AdderAdd
			Element eAdderAdd = getElementByPath("GUI.adderAdd", root);
			if (eAdderAdd != null && !eAdderAdd.getChildren("add").isEmpty()) {
				List<String> adderAdd = getListForXmlElement(eAdderAdd, "add");
				guiSettings.getDownloadSelectionAddStrings().addAll(adderAdd);
			} else {
				String strAdderAdd = readStringValue("GUI.adderAdd", root, "");
				if (strAdderAdd != null) {
					String[] add = strAdderAdd.split("\\|");

					if (!(add.length == 0 || (add.length == 1 && add[0].isEmpty()))) {
						for (int i = add.length - 1; i >= 0; i--) {
							guiSettings.getDownloadSelectionAddStrings().add(add[i]);
						}
					}
				}
			}

			KeywordsSettings keywordsSettings = settings.getKeywordsSettings();

			// Downloaddirectory recognition
			boolean deselectNoKeyword = readBooleanValue("GUI.DeselectNoKeyword", root, keywordsSettings.isDeselectNoKeyword());
			boolean deleteNoKeyword = readBooleanValue("GUI.DeleteNoKeyword", root, keywordsSettings.isDeleteNoKeyword());
			keywordsSettings.setDeselectNoKeyword(deselectNoKeyword);
			keywordsSettings.setDeleteNoKeyword(deleteNoKeyword);

			// TargetDirChangeHistory
			Element eTargetDirChangeHistory = getElementByPath("GUI.targetDirChangeHistory", root);
			if (eTargetDirChangeHistory != null && !eTargetDirChangeHistory.getChildren("targetDir").isEmpty()) {
				List<String> targetDirChangeHistory = getListForXmlElement(eTargetDirChangeHistory, "targetDir");
				guiSettings.getTargetDirChangeHistory().addAll(targetDirChangeHistory);
			} else {
				String strTargetDirChangeHistory = readStringValue("GUI.targetDirChangeHistory", root, "");
				if (strTargetDirChangeHistory != null) {
					String[] add = strTargetDirChangeHistory.split("\\|");
					if (((add.length == 0) || ((add.length == 1) && (add[0].equals("")))) == false) {
						for (int i = add.length - 1; i >= 0; i--) {
							guiSettings.getTargetDirChangeHistory().add(add[i]);
						}
					}
				}
			}

			// FilenameChangeHistory
			Element eFilenameChangeHistory = getElementByPath("GUI.FilenameChangeHistory", root);
			if (eFilenameChangeHistory != null && !eFilenameChangeHistory.getChildren("filename").isEmpty()) {
				List<String> filenameChangeHistory = getListForXmlElement(eFilenameChangeHistory, "filename");
				guiSettings.getFilenameChangeHistory().addAll(filenameChangeHistory);
			} else {
				String strFilenameChangeHistory = readStringValue("GUI.FilenameChangeHistory", root, "");
				if (strFilenameChangeHistory != null) {
					String[] add = strFilenameChangeHistory.split("\\|");
					if (((add.length == 0) || ((add.length == 1) && (add[0].equals("")))) == false) {
						for (int i = add.length - 1; i >= 0; i--) {
							guiSettings.getFilenameChangeHistory().add(add[i]);
						}
					}
				}
			}

			// FilenameChangePrefix
			String filenameChangePrefix = readStringValue("GUI.FilenameChangePrefix", root, guiSettings.getFilenameChangePrefix());
			boolean appendPrefixFilenameChange = readBooleanValue("GUI.AppendPrefixFilenameChange", root, guiSettings.isAppendPrefixFilenameChange());
			guiSettings.setFilenameChangePrefix(filenameChangePrefix);
			guiSettings.setAppendPrefixFilenameChange(appendPrefixFilenameChange);

			// FilenameChangePrefix
			String filenameChangeAppendix = readStringValue("GUI.FilenameChangeAppendix", root, guiSettings.getFilenameChangeAppendix());
			boolean appendAppendixFilenameChange = readBooleanValue("GUI.AppendAppendixFilenameChange", root, guiSettings.isAppendAppendixFilenameChange());
			guiSettings.setFilenameChangeAppendix(filenameChangeAppendix);
			guiSettings.setAppendAppendixFilenameChange(appendAppendixFilenameChange);

			// FilenameChangeKeepOriginal
			boolean filenameChangeKeepOriginal = readBooleanValue("GUI.filenameChangeKeepOriginal", root, guiSettings.isFilenameChangeKeepOriginal());
			guiSettings.setFilenameChangeKeepOriginal(filenameChangeKeepOriginal);

			// DownloadsCompleteNotification
			boolean downloadsCompleteNotification = readBooleanValue("GUI.downloadsCompleteNotification", root, guiSettings.isDownloadsCompleteNotification());
			guiSettings.setDownloadsCompleteNotification(downloadsCompleteNotification);

			// Directory-Log-Filter
			boolean directoryLogFilterEnabled = readBooleanValue("GUI.directoryLog.FilterEnabled", root, guiSettings.isDirectoryLogFilterEnabled());
			guiSettings.setDirectoryLogFilterEnabled(directoryLogFilterEnabled);

			int gdd = readIntValue("GUI.directoryLog.DirCount", root, guiSettings.getDirectoryLogDirCount());
			if (gdd > -1) {
				guiSettings.setDirectoryLogDirCount(gdd);
			}

			boolean directoryLogOnlyExisting = readBooleanValue("GUI.directoryLog.OnlyExisting", root, guiSettings.isDirectoryLogOnlyExisting());
			guiSettings.setDirectoryLogOnlyExisting(directoryLogOnlyExisting);

			// DialogPaths
			String lastUsedImportDialogPath = readStringValue("GUI.lastUsedImportDialogPath", root, directorySettings.getLastUsedImportPath());
			String lastUsedExportDialogPath = readStringValue("GUI.lastUsedExportDialogPath", root, directorySettings.getLastUsedExportPath());
			directorySettings.setLastUsedImportPath(lastUsedImportDialogPath);
			directorySettings.setLastUsedExportPath(lastUsedExportDialogPath);

			// Image Previews
			boolean downloadPreviews = readBooleanValue("GUI.downloadPreviews", root, guiSettings.isDownloadPreviews());
			guiSettings.setDownloadPreviews(downloadPreviews);

			int ps = readIntValue("GUI.previewSize", root, -1);
			if (ps >= 100 && ps <= 1000) {
				guiSettings.setPreviewSize(ps);
			}

			/* Keywords */
			// Keyword-Filter
			boolean keywordFilterEnabled = readBooleanValue("Keywords.FilterEnabled", root, keywordsSettings.isFilterEnabled());
			keywordsSettings.setFilterEnabled(keywordFilterEnabled);

			// KeywordSearch
			int kmm = readIntValue("Keywords.MatchMode", root, -1);
			switch (kmm) {
				case MATCH_ALL:
					keywordsSettings.setMatchMode(KeywordMatchMode.MATCH_ALL);
					break;
				case MATCH_ALL_STRICT:
					keywordsSettings.setMatchMode(KeywordMatchMode.MATCH_ALL_STRICT);
					break;
				case MATCH_ONLY_EXACT:
					keywordsSettings.setMatchMode(KeywordMatchMode.MATCH_ONLY_EXACT);
					break;
			}

			boolean displayKeywordsWhenNoMatches = readBooleanValue("Keywords.displayKeywordsWhenNoMatches", root, keywordsSettings.isDisplayKeywordsWhenNoMatches());
			keywordsSettings.setDisplayKeywordsWhenNoMatches(displayKeywordsWhenNoMatches);

			/* Hosts */
			HostsSettings hostsSettings = settings.getHostsSettings();

			// Rules
			boolean rulesBeforeClasses = readBooleanValue("Hosts.RulesBeforeClasses", root, hostsSettings.isRulesBeforeClasses());
			hostsSettings.setRulesBeforeClasses(rulesBeforeClasses);

			boolean checkContentTypeDefaultImages = readBooleanValue("Hosts.CheckContentTypeDefaultImages", root, false);

			// DeactivatedHosts
			Element eDeactivatedHosts = getElementByPath("Hosts.deactivatedHosts", root);
			if (eDeactivatedHosts != null && !eDeactivatedHosts.getChildren("host").isEmpty()) {
				Map<String, Boolean> deactivatedHosts = getMapForXmlElement(eDeactivatedHosts, "host", "deactivated");
				for (Map.Entry<String, Boolean> entry : deactivatedHosts.entrySet()) {
					HostDeactivationSetting hostDeactivation = new HostDeactivationSetting();
					hostDeactivation.setValue(entry.getKey());
					hostDeactivation.setDeactivated(entry.getValue());
					hostsSettings.getDeactivations().add(hostDeactivation);
				}
			}

			/* Downloads */
			// AutoStartDownloads
			boolean autoStartDownloads = readBooleanValue("Downloads.AutoStartDownloads", root, downloadSettings.isAutoStartDownloads());
			downloadSettings.setAutoStartDownloads(autoStartDownloads);

			// Downloads
			long overallDownloadedBytes = readLongValue("Downloads.DownloadedBytes", root, downloadSettings.getOverallDownloadedBytes());
			long overallDownloadedFiles = readLongValue("Downloads.DownloadedFiles", root, downloadSettings.getOverallDownloadedFiles());
			downloadSettings.setOverallDownloadedBytes(overallDownloadedBytes);
			downloadSettings.setOverallDownloadedFiles(overallDownloadedFiles);

			// SaveLogs
			boolean saveLogs = readBooleanValue("Downloads.SaveLogs", root, downloadSettings.isSaveLogs());
			downloadSettings.setSaveLogs(saveLogs);

			// Max Failed Count
			int mfc = readIntValue("Downloads.MaxFailedCount", root, downloadSettings.getMaxFailedCount());
			if (mfc > -1) {
				downloadSettings.setMaxFailedCount(mfc);
			}

			// Min Filesize
			int mfs = readIntValue("Downloads.MinFilesize", root, downloadSettings.getMinFileSize());
			if (mfs > -1) {
				downloadSettings.setMinFileSize(mfs);
			}

			// Timeout
			int to = readIntValue("Downloads.Timeout", root, -1);
			if (to > 1000) {
				connectionSettings.setConnectionRequestTimeout(to);
			}

			// autoRetryAfterDownloadsComplete
			boolean autoRetryAfterDownloadsComplete = readBooleanValue("Downloads.autoRetryAfterDownloadsComplete", root, downloadSettings.isAutoRetryAfterDownloadsComplete());
			downloadSettings.setAutoRetryAfterDownloadsComplete(autoRetryAfterDownloadsComplete);

			/* Other */
			// Updates
			boolean updates = readBooleanValue("Other.Updates", root, settings.isCheckForUpdatesOnStart());
			settings.setCheckForUpdatesOnStart(updates);

			// CheckClipboard
			boolean checkClipboard = readBooleanValue("Other.CheckClipboard", root, settings.isCheckClipboard());
			settings.setCheckClipboard(checkClipboard);

			// HTTP Port for connections from WebExtension
			int webExtPort = readIntValue("Other.WebExtensionPort", root, settings.getWebExtensionPort());
			if (webExtPort >= 0 && webExtPort <= 65535) {
				settings.setWebExtensionPort(webExtPort);
			}

			// Allowed chars
			int afc = readIntValue("Other.allowedFilenameChars", root, -1);
			switch (afc) {
				case FILENAME_ALL:
					downloadSettings.setAllowedFilenameCharacters(AllowedFilenameCharacters.ALL);
					break;
				case FILENAME_ASCII_ONLY:
					downloadSettings.setAllowedFilenameCharacters(AllowedFilenameCharacters.ASCII_ONLY);
					break;
				case FILENAME_ASCII_UMLAUT:
					downloadSettings.setAllowedFilenameCharacters(AllowedFilenameCharacters.ASCII_UMLAUT);
					break;
			}

			boolean backupDbOnStart = readBooleanValue("Other.backupDbOnStart", root, settings.isBackupDbOnStart());
			settings.setBackupDbOnStart(backupDbOnStart);

			boolean defragDBOnStart = readBooleanValue("Other.defragDBOnStart", root, settings.isDefragDBOnStart());
			settings.setDefragDBOnStart(defragDBOnStart);

			long mds = readLongValue("Other.defragMinFilesize", root, -1);
			if (mds > -1) {
				settings.setDefragMinFilesize(mds);
			}

			// Debug
			String dl = readStringValue("Other.debugLevel", root, "Info");
			LogLevelSetting logLevelSetting = getLogLevelSettingForString(dl);
			settings.setLogLevel(logLevelSetting);

			// Threads
			int tc = readIntValue("Other.threadCount", root, -1);
			if ((tc > 0) && (tc <= Runtime.getRuntime().availableProcessors())) {
				settings.setThreadCount(tc);
			}

			List<HosterSettings> hosterSettings = settings.getHosterSettings();

			// OPTIONS
			List<HosterSettings> hosterSettingsList = readOptions(root);
			hosterSettings.addAll(hosterSettingsList);

			HosterSettings hostDefaultFilesHosterSettings = hosterSettingsList.stream().filter(x -> "HostDefaultFiles".equals(x.getName())).findFirst().orElse(null);
			if (hostDefaultFilesHosterSettings == null) {
				hostDefaultFilesHosterSettings = new HosterSettings();
				hosterSettingsList.add(hostDefaultFilesHosterSettings);
			}

			CustomSetting checkContentTypeDefaultImagesCustomSetting = new CustomSetting();
			checkContentTypeDefaultImagesCustomSetting.setDataType("boolean");
			checkContentTypeDefaultImagesCustomSetting.setName("checkContentTypeDefaultImages");
			checkContentTypeDefaultImagesCustomSetting.setValue(Boolean.toString(checkContentTypeDefaultImages));

			hostDefaultFilesHosterSettings.getSettings().add(checkContentTypeDefaultImagesCustomSetting);

			// Subdirs
			Element eDirs = root.getChild("Directories");
			if (eDirs != null) {
				List<Element> lSubdirs = eDirs.getChildren();
				for (int l = 0; l < lSubdirs.size(); l++) {
					Element eSub = lSubdirs.get(l);
					if (!eSub.getName().matches("^Subdir[0-9]+$")) {
						continue;
					}
					String subVal = eSub.getValue();
					if ((subVal.length() > 0) && (subVal.contains("|"))) {
						String[] parts = subVal.split("\\|");
						if (parts.length == 7) {
							String name = parts[0];
							try {
								long min = Long.parseLong(parts[1]);
								long max = Long.parseLong(parts[2]);
								int resMinW = Integer.parseInt(parts[3]);
								int resMinH = Integer.parseInt(parts[4]);
								int resMaxW = Integer.parseInt(parts[5]);
								int resMaxH = Integer.parseInt(parts[6]);

								SubDirSetting subDirSetting = new SubDirSetting();
								subDirSetting.setName(name);
								subDirSetting.setMin(min);
								subDirSetting.setMax(max);
								subDirSetting.setResMinWidth(resMinW);
								subDirSetting.setResMinHeight(resMinH);
								subDirSetting.setResMaxWidth(resMaxW);
								subDirSetting.setResMaxHeight(resMaxH);
								directorySettings.getSubDirSettings().add(subDirSetting);
							} catch (NumberFormatException nfe) {
								logger.error(nfe.getMessage(), nfe);
							}
						}
					}
				}
			}

			// RegexReplaces Page-Title
			List<RegexReplaceSetting> regexReplacesPageTitle = getRegexReplaces(root, "GUI", "regexReplacePageTitle");
			guiSettings.getRegexReplacesPageTitle().addAll(regexReplacesPageTitle);

			// RegexReplaces Filename
			List<RegexReplaceSetting> regexReplacesFilename = getRegexReplaces(root, "Downloads", "regexReplaceFilename");
			downloadSettings.getRegexReplacesFilename().addAll(regexReplacesFilename);

			logger.info("Converted old settings file: Folder: {}, Filename: {}", strSettingsPath, strSettingsFile);
			return true;
		} catch (Exception e) {
			logger.error("Could not convert old settings file: Folder: {}, Filename: {}", strSettingsPath, strSettingsFile, e);
			return false;
		}
	}

	private List<RegexReplaceSetting> getRegexReplaces(Element root, String rootChild, String subChild) {
		List<RegexReplaceSetting> regexps = new ArrayList<>();

		Element eRootChild = root.getChild(rootChild);
		Element eSubChild = null;
		if (eRootChild != null) {
			eSubChild = eRootChild.getChild(subChild);
		}

		if (eSubChild == null) {
			return regexps;
		}

		for (Element child : eSubChild.getChildren("regexp")) {
			try {
				String search = child.getAttributeValue("search");
				String replace = child.getAttributeValue("replace");

				RegexReplaceSetting regexReplaceSetting = new RegexReplaceSetting();
				regexReplaceSetting.setPattern(search);
				regexReplaceSetting.setReplacement(replace);

				regexps.add(regexReplaceSetting);
			} catch (Exception ex) {
				logger.error("Could not load regexp child from element '{}' in RegexReplacePipeline '{}': {}", eSubChild, subChild, child, ex);
			}
		}

		return regexps;
	}

	private List<String> getListForXmlElement(Element e, String valuesName) {
		List<String> values = new ArrayList<>();
		List<Element> childs = e.getChildren(valuesName);
		for (int i = 0; i < childs.size(); i++) {
			String val = childs.get(i).getValue();
			if (val != null) {
				values.add(val);
			}
		}
		return values;
	}

	private Map<String, Boolean> getMapForXmlElement(Element e, String valuesName, String attributeName) {
		Map<String, Boolean> map = new LinkedHashMap<>();
		List<Element> childs = e.getChildren(valuesName);
		for (int i = 0; i < childs.size(); i++) {
			String val = childs.get(i).getValue();
			if (val != null) {
				map.put(val, Boolean.parseBoolean(childs.get(i).getAttributeValue(attributeName)));
			}
		}
		return map;
	}

	private List<HosterSettings> readOptions(Element root) {
		List<HosterSettings> hosterSettingsList = new ArrayList<>();

		List<Element> l = root.getChildren();
		if (!l.isEmpty()) {
			Iterator<Element> it = l.iterator();
			while (it.hasNext()) {
				HosterSettings hosterSettings = readRootOption(it.next());
				if (hosterSettings != null) {
					hosterSettingsList.add(hosterSettings);
				}
			}
		}

		return hosterSettingsList;
	}

	private HosterSettings readRootOption(Element e) {
		String path = e.getName();
		if (!checkOptionRootPath(path)) {
			return null;
		}

		List<Element> l = e.getChildren();
		if (l.isEmpty()) {
			return null;
		}

		HosterSettings hosterSettings = new HosterSettings();
		hosterSettings.setName(path);

		Iterator<Element> it = l.iterator();
		while (it.hasNext()) {
			readOption(path, "", it.next(), hosterSettings);
		}

		return hosterSettings;
	}

	private void readOption(String fullPath, String path, Element e, HosterSettings hosterSettings) {
		fullPath += "." + e.getName();
		if (path.isEmpty()) {
			path += e.getName();
		} else {
			path += "." + e.getName();
		}

		List<Element> l = e.getChildren();
		if (!l.isEmpty()) {
			Iterator<Element> it = l.iterator();
			while (it.hasNext()) {
				readOption(fullPath, path, it.next(), hosterSettings);
			}
		}

		if (checkOptionPath(fullPath)) {
			String dataType = e.getAttributeValue("datatype");
			if (!checkOptionDataType(dataType)) {
				return;
			}

			String value = e.getText();

			CustomSetting customSetting = new CustomSetting();
			customSetting.setName(path);
			customSetting.setDataType(dataType);
			customSetting.setValue(value);

			hosterSettings.getSettings().add(customSetting);
		}
	}

	private boolean checkOptionDataType(String dataType) {
		if (dataType == null) {
			return false;
		}
		return dataType.equals("boolean") || dataType.equals("int") || dataType.equals("long") || dataType.equals("string") || dataType.equals("byte") || dataType.equals("short")
				|| dataType.equals("float") || dataType.equals("double");
	}

	/**
	 * Ueberprueft ob dieser Pfad erlaubt ist
	 * 
	 * @param path Pfad
	 * @return TRUE falls erlaubt
	 */
	private boolean checkOptionRootPath(String path) {
		for (int i = 0; i < RESTRICTED_ROOT_PATHS.length; i++) {
			if (path.equals(RESTRICTED_ROOT_PATHS[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Ueberprueft ob dieser Pfad erlaubt ist
	 * 
	 * @param path Pfad
	 * @return TRUE falls erlaubt
	 */
	private boolean checkOptionPath(String path) {
		for (int i = 0; i < RESTRICTED_PATHS.length; i++) {
			if (path.equals(RESTRICTED_PATHS[i])) {
				return false;
			}
		}
		for (int i = 0; i < RESTRICTED_START_WITH_PATHS.length; i++) {
			if (path.startsWith(RESTRICTED_START_WITH_PATHS[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the level
	 * If the string is not ok, Level.WARN is returned
	 * 
	 * @param debugLevel Debug-Level
	 * @return Level
	 */
	private LogLevelSetting getLogLevelSettingForString(String debugLevel) {
		if (debugLevel.equalsIgnoreCase("Off")) {
			return LogLevelSetting.INFO;
		} else if (debugLevel.equalsIgnoreCase("All")) {
			return LogLevelSetting.INFO;
		} else if (debugLevel.equalsIgnoreCase("Info")) {
			return LogLevelSetting.INFO;
		} else if (debugLevel.equalsIgnoreCase("Trace")) {
			return LogLevelSetting.TRACE;
		} else if (debugLevel.equalsIgnoreCase("Debug")) {
			return LogLevelSetting.DEBUG;
		} else if (debugLevel.equalsIgnoreCase("Warn")) {
			return LogLevelSetting.WARN;
		} else if (debugLevel.equalsIgnoreCase("Error")) {
			return LogLevelSetting.ERROR;
		} else if (debugLevel.equalsIgnoreCase("Fatal")) {
			return LogLevelSetting.FATAL;
		}
		return LogLevelSetting.INFO;
	}
}

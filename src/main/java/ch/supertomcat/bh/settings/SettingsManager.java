package ch.supertomcat.bh.settings;

import static ch.supertomcat.bh.settings.SettingsToolkit.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.UIManager;

import org.apache.logging.log4j.Level;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.OptionException;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.settings.options.Option;
import ch.supertomcat.bh.settings.options.OptionBoolean;
import ch.supertomcat.bh.settings.options.OptionByte;
import ch.supertomcat.bh.settings.options.OptionDouble;
import ch.supertomcat.bh.settings.options.OptionFloat;
import ch.supertomcat.bh.settings.options.OptionInt;
import ch.supertomcat.bh.settings.options.OptionLong;
import ch.supertomcat.bh.settings.options.OptionShort;
import ch.supertomcat.bh.settings.options.OptionString;
import ch.supertomcat.bh.settings.options.Subdir;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.http.cookies.BrowserCookies;
import ch.supertomcat.supertomcatutils.io.CopyUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;
import ch.supertomcat.supertomcatutils.regex.RegexReplace;
import ch.supertomcat.supertomcatutils.regex.RegexReplacePipeline;

/**
 * Class which handels the settings
 * There are also methods to get hoster-classes the possibility
 * to save their own settings.
 */
public class SettingsManager {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(SettingsManager.class);

	/**
	 * Automatic Download Directory Recognition by Title
	 */
	public static final int BY_TITLE = 0;

	/**
	 * Automatic Download Directory Recognition by Filename
	 */
	public static final int BY_FILENAME = 1;

	/**
	 * Takes automatically a suitable unit for displaying filesize
	 */
	public static final int AUTO_CHANGE_SIZE = 0;

	/**
	 * Display filesize only as bytes
	 */
	public static final int ONLY_B = 1;

	/**
	 * Display filesize only as kibibytes
	 */
	public static final int ONLY_KIB = 2;

	/**
	 * Display filesize only as mebibytes
	 */
	public static final int ONLY_MIB = 3;

	/**
	 * Display filesize only as gibibytes
	 */
	public static final int ONLY_GIB = 4;

	/**
	 * Display filesize only as tebibytes
	 */
	public static final int ONLY_TIB = 5;

	/**
	 * Display progressbar and only percent
	 */
	public static final int PROGRESSBAR_PERCENT = 0;

	/**
	 * Display progressbar and only filesize
	 */
	public static final int PROGRESSBAR_SIZE = 1;

	/**
	 * Display no progressbar and only percent
	 */
	public static final int NOPROGRESSBAR_PERCENT = 2;

	/**
	 * Display no progressbar and only filesize
	 */
	public static final int NOPROGRESSBAR_SIZE = 3;

	/**
	 * Filter all non-ascii chars from paths and filenames
	 */
	public static final int FILENAME_ASCII_ONLY = 0;

	/**
	 * Filter all non-ascii and non-umlaut chars from paths and filenames
	 */
	public static final int FILENAME_ASCII_UMLAUT = 1;

	/**
	 * Filter only not allowed chars from paths and filenames
	 */
	public static final int FILENAME_ALL = 2;

	/**
	 * Use java-default-theme
	 */
	public static final int LAF_DEFAULT = 0;

	/**
	 * Use operating-system-theme
	 */
	public static final int LAF_OS = 1;

	/**
	 * NOT IMPLEMENTED YET
	 */
	public static final int LAF_METAL = 2;

	/**
	 * NOT IMPLEMENTED YET
	 */
	public static final int LAF_WINDOWS = 3;

	/**
	 * NOT IMPLEMENTED YET
	 */
	public static final int LAF_WINDOWS_CLASSIC = 4;

	/**
	 * NOT IMPLEMENTED YET
	 */
	public static final int LAF_MOTIF = 5;

	/**
	 * NOT IMPLEMENTED YET
	 */
	public static final int LAF_GTK = 6;

	/**
	 * NOT IMPLEMENTED YET
	 */
	public static final int LAF_MACOS = 7;

	/**
	 * Nimbus (since jre6 update 10)
	 */
	public static final int LAF_NIMBUS = 8;

	/**
	 * Classpaths of the LAFs
	 */
	public static final String LAF_CLASSPATHES[] = { UIManager.getCrossPlatformLookAndFeelClassName(), UIManager
			.getSystemLookAndFeelClassName(), "javax.swing.plaf.metal.MetalLookAndFeel", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel", "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel", "com.sun.java.swing.plaf.motif.MotifLookAndFeel", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel", "javax.swing.plaf.mac.MacLookAndFeel", "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel" };

	/**
	 * Names of the LAFs
	 */
	public static final String LAF_NAMES[] = { "Default", "OperatingSystem", "Metal", "Windows", "Windows Classic", "Motif", "GTK", "Mac OS", "Nimbus" };

	/**
	 * Restricted Settingspaths
	 * This Settings can not be overwritten by the set*Value-Methods!
	 */
	private static final String RESTRICTED_PATHS[] = { "Directories.SavePath", "Directories.RememberLastUsedPath", "Directories.AutoTargetDir", "Directories.AutoTargetDirMode", "Directories.subdirsEnabled", "Directories.subdirsResolutionMode", "Connection.Connections", "Connection.connectionsPerHost", "Connection.Proxy.Mode", "Connection.Proxy.Name", "Connection.Proxy.Port", "Connection.Proxy.User", "Connection.Proxy.Passwort", "Connection.Proxy.Auth", "Connection.cookiesFromBrowser", "Connection.cookieFileOpera", "Connection.cookieFileOperaFixed", "Connection.cookieFileOperaNew", "Connection.cookieFileOperaNewFixed", "Connection.cookieFileFirefox", "Connection.cookieFileFirefoxFixed", "Connection.cookieFilePaleMoon", "Connection.cookieFilePaleMoonFixed", "Connection.userAgent", "GUI.Language", "GUI.Window.Width", "GUI.Window.Height", "GUI.Window.X", "GUI.Window.Y", "GUI.Window.State", "GUI.Window.Save", "GUI.DownloadSelectionWindow.Width", "GUI.DownloadSelectionWindow.Height", "GUI.DownloadSelectionWindow.X", "GUI.DownloadSelectionWindow.Y", "GUI.DownloadSelectionWindow.Save", "GUI.colWidthsQueue", "GUI.colWidthsLog", "GUI.colWidthsKeywords", "GUI.colWidthsRules", "GUI.colWidthsRulesEditor", "GUI.colWidthsHosts", "GUI.colWidthsSubdirs", "GUI.colWidthsUpdate", "GUI.colWidthsAdder", "GUI.colWidthsAdderKeywordSelectorFilename", "GUI.saveTableColumnSizes", "GUI.tableSortOrdersKeywords", "GUI.saveTableSortOrders", "GUI.Size", "GUI.Progress", "GUI.Bitrate", "GUI.LAF", "GUI.LogDays", "GUI.currentDownloadLogFile", "GUI.AlwaysAddTitle", "GUI.adderAdd", "GUI.DeselectNoKeyword", "GUI.DeleteNoKeyword", "GUI.targetDirChangeHistory", "GUI.FilenameChangeHistory", "GUI.FilenameChangePrefix", "GUI.AppendPrefixFilenameChange", "GUI.FilenameChangeAppendix", "GUI.AppendAppendixFilenameChange", "GUI.filenameChangeKeepOriginal", "GUI.DownloadRate", "GUI.downloadsCompleteNotification", "GUI.directoryLog.FilterEnabled", "GUI.directoryLog.DirCount", "GUI.directoryLog.OnlyExisting", "GUI.lastUsedImportDialogPath", "GUI.lastUsedExportDialogPath", "GUI.downloadPreviews", "GUI.previewSize", "Keywords.FilterEnabled", "Keywords.MatchMode", "Keywords.displayKeywordsWhenNoMatches", "Hosts.RulesBeforeClasses", "Downloads.AutoStartDownloads", "Downloads.DownloadedBytes", "Downloads.DownloadedFiles", "Downloads.SaveLogs", "Downloads.useOldDownloadLogMode", "Downloads.MaxFailedCount", "Downloads.MinFilesize", "Downloads.Timeout", "Downloads.sortDownloadsOnStart", "Downloads.autoRetryAfterDownloadsComplete", "Other.Updates", "Other.CheckClipboard", "Other.WebExtensionPort", "Other.allowedFilenameChars", "Other.backupDbOnStart", "Other.defragDBOnStart", "Other.defragMinFilesize", "Other.debugLevel", "Other.threadCount" };

	/**
	 * Restricted Settingspaths and subpaths
	 * This Settings and all subpaths of it can not be overwritten by the set*Value-Methods!
	 */
	private static final String RESTRICTED_START_WITH_PATHS[] = { "Directories.Subdir", "GUI.regexReplacePageTitle", "Downloads.regexReplaceFilename", "GUI.FilenameChangeHistory", "GUI.targetDirChangeHistory", "GUI.adderAdd", "Hosts.deactivatedHosts" };

	/**
	 * Listener
	 */
	private List<BHSettingsListener> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Default Download Directory
	 */
	private String savePath = ApplicationProperties.getProperty("SettingsPath");

	/**
	 * Remember Last Used Save-Path
	 */
	private boolean saveLastPath = false;

	/**
	 * Max Connections
	 */
	private int connections = 4;

	/**
	 * Max Connections per host
	 */
	private int connectionsPerHost = 0;

	/**
	 * Size of all downloaded files
	 */
	private long overallDownloadedBytes = 0;

	/**
	 * Amount of all downloaded files
	 */
	private long overallDownloadedFiles = 0;

	/**
	 * Automatic Download Directory Recognition
	 */
	private boolean autoTargetdir = false;

	/**
	 * Mode for Automatic Download Directory Recognition
	 */
	private int autoTargetDirMode = BY_TITLE;

	/**
	 * Check automatically for updates on application start
	 */
	private boolean updates = false;

	/**
	 * Autostart downloads when they are added to the queue
	 */
	private boolean autoStartDownloads = false;

	/**
	 * Save Download logs
	 */
	private boolean saveLogs = true;

	/**
	 * Check Clipboard for URL
	 */
	private boolean checkClipboard = false;

	/**
	 * HTTP Port for connections from WebExtension
	 */
	private int webExtensionPort = 35990;

	/**
	 * Language
	 */
	private String language = "en_EN";

	/**
	 * Connection-Mode
	 */
	private int proxymode = ProxyManager.DIRECT_CONNECTION;

	/**
	 * Proxy-Name
	 */
	private String proxyname = "127.0.0.1";

	/**
	 * Proxy-Port
	 */
	private int proxyport = 0;

	/**
	 * Proxy-User
	 */
	private String proxyuser = "";

	/**
	 * Proxy-Password
	 */
	private String proxypassword = "";

	/**
	 * Proxy-Authenticate
	 */
	private boolean proxyauth = false;

	/**
	 * Filesize Display Mode
	 */
	private int sizeView = SettingsManager.AUTO_CHANGE_SIZE;

	/**
	 * Progressbar Display Mode
	 */
	private int progessView = SettingsManager.PROGRESSBAR_PERCENT;

	/**
	 * Mode of Keyword-Search
	 */
	private int keywordMatchMode = KeywordManager.MATCH_ALL_STRICT;

	/**
	 * Display Keywords When No Matches were found
	 */
	private boolean displayKeywordsWhenNoMatches = true;

	/**
	 * LAF
	 */
	private int lookAndFeel = LAF_OS;

	/**
	 * Flag if the Application is started at first time
	 */
	private boolean languageFirstRun = true;

	/**
	 * Flag if a new SettingsFile was created
	 */
	private boolean newSettingsFileCreated = false;

	/**
	 * Width
	 */
	private int windowWidth = 0;

	/**
	 * Height
	 */
	private int windowHeight = 0;

	/**
	 * X-Position
	 */
	private int windowXPos = 0;

	/**
	 * Y-Position
	 */
	private int windowYPos = 0;

	/**
	 * Windowstate
	 */
	private int windowState = 0;

	/**
	 * Save Size and Position of Main-Window
	 */
	private boolean saveWindowSizePosition = false;

	/**
	 * Width
	 */
	private int downloadSelectionWindowWidth = 0;

	/**
	 * Height
	 */
	private int downloadSelectionWindowHeight = 0;

	/**
	 * X-Position
	 */
	private int downloadSelectionWindowXPos = 0;

	/**
	 * Y-Position
	 */
	private int downloadSelectionWindowYPos = 0;

	/**
	 * Save Size and Position of Main-Window
	 */
	private boolean saveDownloadSelectionWindowSizePosition = false;

	/**
	 * Always add the title automatically to the downloadpath
	 */
	private boolean alwaysAddTitle = false;

	/**
	 * Deselect files in Download-Selection-Dialog when no keyword has matched
	 */
	private boolean deselectNoKeyword = false;

	/**
	 * Keyword-Filter enabled (means the filter-button on the gui on the keywords-tab)
	 */
	private boolean keywordFilterEnabled = false;

	/**
	 * Directory-Log-Filter enabled (means the filter-button on the gui in the directory log window)
	 */
	private boolean directoryLogFilterEnabled = false;

	/**
	 * directoryLogDirCount
	 */
	private int directoryLogDirCount = 200;

	/**
	 * directoryLogOnlyExisting
	 */
	private boolean directoryLogOnlyExisting = true;

	/**
	 * Strings in cbAdd in AdderPanel
	 */
	private List<String> adderAdd = new ArrayList<>();

	/**
	 * String in AdderPanel
	 */
	private List<String> targetDirChangeHistory = new ArrayList<>();

	/**
	 * String in Filerenamedialog
	 */
	private List<String> filenameChangeHistory = new ArrayList<>();

	/**
	 * Add Prefix in Filerenamedialog
	 */
	private boolean appendPrefixFilenameChange = false;

	/**
	 * Add Appendix in Filerenamedialog
	 */
	private boolean appendAppendixFilenameChange = false;

	/**
	 * String in Filerenamedialog
	 */
	private String filenameChangePrefix = "";

	/**
	 * String in Filerenamedialog
	 */
	private String filenameChangeAppendix = ".jpg";

	/**
	 * Keep original filenames on filename change dialog
	 */
	private boolean filenameChangeKeepOriginal = false;

	/**
	 * URLs are parsed by rules first
	 */
	private boolean rulesBeforeClasses = false;

	/**
	 * From which browser we have to read out the cookies
	 */
	private int cookiesFromBrowser = BrowserCookies.BROWSER_NO_COOKIES;

	/**
	 * Delete local files when no keyword has matched
	 */
	private boolean deleteNoKeyword = false;

	/**
	 * Allowd chars in path and filename
	 */
	private int allowedFilenameChars = SettingsManager.FILENAME_ALL;

	/**
	 * Amount of failures before the download will automatically be deactivated
	 * 0 = Infinit (So Downloads are not automatically deactivated)
	 */
	private int maxFailedCount = 2;

	/**
	 * Minimum filesize (When the file is smaller the download is treated as failure)
	 * 0 = Deactivates this check
	 * Unit: Byte
	 */
	private int minFilesize = 0;

	/**
	 * Timeout
	 * Unit: Milliseconds
	 */
	private int timeout = 60000;

	/**
	 * Manually set path to the cookie file of opera
	 */
	private boolean cookieFileOperaFixed = false;

	/**
	 * Manually set path to the cookie file of opera
	 */
	private String cookieFileOpera = "";

	/**
	 * Manually set path to the cookie file of opera
	 */
	private boolean cookieFileOperaNewFixed = false;

	/**
	 * Manually set path to the cookie file of opera
	 */
	private String cookieFileOperaNew = "";

	/**
	 * Manually set path to the cookie file of firefox
	 */
	private boolean cookieFileFirefoxFixed = false;

	/**
	 * Manually set path to the cookie file of firefox
	 */
	private String cookieFileFirefox = "";

	/**
	 * Manually set path to the cookie file of firefox
	 */
	private boolean cookieFilePaleMoonFixed = false;

	/**
	 * Manually set path to the cookie file of firefox
	 */
	private String cookieFilePaleMoon = "";

	/**
	 * Backup databases on application start
	 */
	private boolean backupDbOnStart = true;

	/**
	 * Defrag databases on application start
	 */
	private boolean defragDBOnStart = true;

	/**
	 * Minimum filesize from which databases are defraged on application start
	 */
	private long defragMinFilesize = 5000000;

	/**
	 * Subdirs activated
	 */
	private boolean subdirsEnabled = false;

	/**
	 * Mode for checking if a image is in the resolution range of a subdir
	 */
	private int subdirsResolutionMode = 0;

	/**
	 * Subdirs
	 */
	private List<Subdir> subdirs = new ArrayList<>();

	/**
	 * regexReplacePipelinePageTitle
	 */
	private RegexReplacePipeline regexReplacePipelinePageTitle = new RegexReplacePipeline("regexReplacePageTitle");

	/**
	 * regexReplacePipelineFilename
	 */
	private RegexReplacePipeline regexReplacePipelineFilename = new RegexReplacePipeline("regexReplaceFilename");

	/**
	 * Display downloadrate
	 */
	private boolean downloadRate = false;

	/**
	 * Debug-Level
	 * Available Levels:
	 * Off
	 * All
	 * Info
	 * Trace
	 * Debug
	 * Warn
	 * Error
	 * Fatal
	 */
	private String debugLevel = "Info";

	/**
	 * ColWidths of Queue
	 */
	private String colWidthsQueue = "";

	/**
	 * ColWidths of Log
	 */
	private String colWidthsLog = "";

	/**
	 * ColWidths of Keywords
	 */
	private String colWidthsKeywords = "";

	/**
	 * ColWidths of Rules
	 */
	private String colWidthsRules = "";

	/**
	 * ColWidths of RulesEditor
	 */
	private String colWidthsRulesEditor = "";

	/**
	 * ColWidths of Hosts
	 */
	private String colWidthsHosts = "";

	/**
	 * ColWidths of Subdirs
	 */
	private String colWidthsSubdirs = "";

	/**
	 * ColWidths of UpdateWindow
	 */
	private String colWidthsUpdate = "";

	/**
	 * ColWidths of AdderPanel
	 */
	private String colWidthsAdder = "";

	/**
	 * ColWidths of AdderKeywordSelectorFilename
	 */
	private String colWidthsAdderKeywordSelectorFilename = "";

	/**
	 * Save ColWidths of Tables
	 */
	private boolean saveTableColumnSizes = false;

	/**
	 * SortOrders of Keywords-Table
	 */
	private String tableSortOrdersKeywords = "";

	/**
	 * Save ColWidths of Tables
	 */
	private boolean saveTableSortOrders = false;

	/**
	 * Sort Downloads on Start
	 * 0 = By ContainerURL and Directory
	 * 1 = By Directory and ContainerURL
	 * 2 = By ContainerURL only
	 * 3 = By Directory only
	 * 4 = No Sort
	 * 5 = By Date and Time added only
	 */
	private int sortDownloadsOnStart = 5;

	/**
	 * Display a notification when all downloads are complete
	 */
	private boolean downloadsCompleteNotification = false;

	/**
	 * Use old downloadlog mode, which saves the download log to a textfile instead of the sqlite-db
	 */
	private boolean useOldDownloadLogMode = true;

	/**
	 * After all downloads are completed, means the download-queue (not the table in the GUI) is empty,
	 * BH starts downloads of all files in the table in the GUI again.
	 * If in the GUI the queue is empty or all files are deactivated nothing is done.
	 * If the Stop-Button was clicked also nothing is done
	 */
	private boolean autoRetryAfterDownloadsComplete = false;

	/**
	 * lastUsedImportDialogPath
	 */
	private String lastUsedImportDialogPath = "";

	/**
	 * lastUsedExportDialogPath
	 */
	private String lastUsedExportDialogPath = "";

	/**
	 * userAgent
	 */
	private String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.2.6) Gecko/20100625 Firefox/3.6.6";

	/**
	 * currentDownloadLogFile
	 */
	private String currentDownloadLogFile = "BH-logs.txt";

	/**
	 * Count of threads that should be used to process tasks which
	 * need a lot of cpu-usage and can be speed up by using multiple
	 * threads.
	 */
	private int threadCount = Runtime.getRuntime().availableProcessors();

	private boolean downloadPreviews = false;

	private int previewSize = 100;

	private Map<String, Boolean> deactivatedHosts = new HashMap<>();

	/**
	 * Array containig the external options
	 */
	private List<Option<?>> options = new ArrayList<>();

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
	public SettingsManager(String strSettingsFolder, final String strSettingsFilename) {
		// If path for downloads are overridden by directories.properties file, then we need to set the path here
		String portableDownloadPath = ApplicationProperties.getProperty("DownloadPath");
		if (portableDownloadPath != null) {
			if (!portableDownloadPath.endsWith("/") && !portableDownloadPath.endsWith("\\")) {
				portableDownloadPath += FileUtil.FILE_SEPERATOR;
			}
			savePath = portableDownloadPath;
		}

		strSettingsPath = strSettingsFolder;
		strSettingsFile = strSettingsPath + strSettingsFilename;
		strSettingsFileBackup = strSettingsFile + ".backup";

		File settingsPath = new File(strSettingsPath);
		File settingsFile = new File(strSettingsFile);
		File settingsFileBackup = new File(strSettingsFileBackup);

		if (!settingsPath.exists() && !settingsPath.mkdirs()) {
			logger.error("Settings-Folder could not be created: {}", settingsPath.getAbsolutePath());
		}

		if (!settingsFile.exists()) {
			logger.info("Settingsfile not found in folder '{}': {}", settingsPath.getAbsolutePath(), settingsFile.getAbsolutePath());
			if (settingsFileBackup.exists() && settingsFileBackup.length() > 0) {
				logger.info("Restoring Settingsfile with backup: {}", settingsFileBackup.getAbsolutePath());
				CopyUtil.copy(strSettingsFileBackup, strSettingsFile);
			} else {
				try {
					// If settingsfile does not exists
					settingsFile.createNewFile();
					languageFirstRun = true;
					newSettingsFileCreated = true;
					writeSettings(true);
				} catch (IOException e) {
					logger.error("Could not create Settingsfile: {}", settingsFile.getAbsolutePath(), e);
				}
			}
		} else {
			if (settingsFile.length() == 0) {
				logger.error("Settingsfile is empty: {}", settingsFile.getAbsolutePath());
				if (settingsFileBackup.exists() && settingsFileBackup.length() > 0) {
					logger.info("Restoring Settingsfile with backup: {}", settingsFileBackup.getAbsolutePath());
					CopyUtil.copy(strSettingsFileBackup, strSettingsFile);
				}
			} else {
				long now = System.currentTimeMillis();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss-SSS");
				String target = strSettingsFile + "-" + dateFormat.format(now);

				CopyUtil.copy(strSettingsFile, target);

				// Delete old backup-Files
				BHUtil.deleteOldBackupFiles(settingsPath, strSettingsFilename, 3);

				if (!settingsFileBackup.exists()) {
					CopyUtil.copy(strSettingsFile, strSettingsFileBackup);
				}
			}
		}
	}

	/**
	 * Read the Settings
	 * 
	 * @return True if successful
	 */
	public boolean readSettings() {
		Document doc = null;
		File settingsFile;
		try {
			settingsFile = new File(strSettingsFile);
			SAXBuilder b = new SAXBuilder();
			doc = b.build(settingsFile);
			Element root = doc.getRootElement();

			/* Directories */
			// SavePath
			this.savePath = readStringValue("Directories.SavePath", root, this.savePath);
			if ((this.savePath.endsWith("/") == false) && (this.savePath.endsWith("\\") == false)) {
				this.savePath += FileUtil.FILE_SEPERATOR;
			}
			this.saveLastPath = readBooleanValue("Directories.RememberLastUsedPath", root, this.saveLastPath);

			// AutoTargetDir
			this.autoTargetdir = readBooleanValue("Directories.AutoTargetDir", root, this.autoTargetdir);

			// AutoTargetDirMode
			int atdm = readIntValue("Directories.AutoTargetDirMode", root, this.autoTargetDirMode);
			if ((atdm < 2) && (atdm > -1)) {
				this.autoTargetDirMode = atdm;
			}

			// Subdirs
			this.subdirsEnabled = readBooleanValue("Directories.subdirsEnabled", root, this.subdirsEnabled);
			int srm = readIntValue("Directories.subdirsResolutionMode", root, this.subdirsResolutionMode);
			if ((srm < 5) && (srm > -1)) {
				this.subdirsResolutionMode = srm;
			}

			/* Connection */
			// Connections
			int con = readIntValue("Connection.Connections", root, this.connections);
			if ((con <= 50) && (con >= 1)) {
				this.connections = con;
			}

			int conph = readIntValue("Connection.connectionsPerHost", root, this.connectionsPerHost);
			if ((conph <= 50) && (conph >= 0)) {
				this.connectionsPerHost = conph;
			}

			// Proxy
			int pm = readIntValue("Connection.Proxy.Mode", root, this.proxymode);
			if ((pm < 2) && (pm > -1)) {
				this.proxymode = pm;
			}
			this.proxyname = readStringValue("Connection.Proxy.Name", root, this.proxyname);
			this.proxyport = readIntValue("Connection.Proxy.Port", root, this.proxyport);
			this.proxyuser = readStringValue("Connection.Proxy.User", root, this.proxyuser);
			this.proxypassword = readStringValue("Connection.Proxy.Passwort", root, this.proxypassword);
			this.proxyauth = readBooleanValue("Connection.Proxy.Auth", root, this.proxyauth);

			// Cookies
			int cfb = readIntValue("Connection.cookiesFromBrowser", root, this.cookiesFromBrowser);
			if ((cfb < 6) && (cfb > -1)) {
				this.cookiesFromBrowser = cfb;
			}
			this.cookieFileOpera = readStringValue("Connection.cookieFileOpera", root, this.cookieFileOpera);
			this.cookieFileOperaFixed = readBooleanValue("Connection.cookieFileOperaFixed", root, this.cookieFileOperaFixed);
			this.cookieFileOperaNew = readStringValue("Connection.cookieFileOperaNew", root, this.cookieFileOperaNew);
			this.cookieFileOperaNewFixed = readBooleanValue("Connection.cookieFileOperaNewFixed", root, this.cookieFileOperaNewFixed);
			this.cookieFileFirefox = readStringValue("Connection.cookieFileFirefox", root, this.cookieFileFirefox);
			this.cookieFileFirefoxFixed = readBooleanValue("Connection.cookieFileFirefoxFixed", root, this.cookieFileFirefoxFixed);
			this.cookieFilePaleMoon = readStringValue("Connection.cookieFilePaleMoon", root, this.cookieFilePaleMoon);
			this.cookieFilePaleMoonFixed = readBooleanValue("Connection.cookieFilePaleMoonFixed", root, this.cookieFilePaleMoonFixed);

			// User-Agent
			this.userAgent = readStringValue("Connection.userAgent", root, this.userAgent);

			/* GUI */
			// Language
			String lang = readStringValue("GUI.Language", root, this.language);
			if (lang.matches("(de_DE|en_EN)")) {
				if (newSettingsFileCreated == false) {
					languageFirstRun = false;
				}
				this.language = lang;
			}

			// Window
			this.windowWidth = readIntValue("GUI.Window.Width", root, this.windowWidth);
			this.windowHeight = readIntValue("GUI.Window.Height", root, this.windowHeight);
			this.windowXPos = readIntValue("GUI.Window.X", root, this.windowXPos);
			this.windowYPos = readIntValue("GUI.Window.Y", root, this.windowYPos);
			this.windowState = readIntValue("GUI.Window.State", root, this.windowState);
			this.saveWindowSizePosition = readBooleanValue("GUI.Window.Save", root, this.saveWindowSizePosition);

			// Download-Selection-Window
			this.downloadSelectionWindowWidth = readIntValue("GUI.DownloadSelectionWindow.Width", root, this.downloadSelectionWindowWidth);
			this.downloadSelectionWindowHeight = readIntValue("GUI.DownloadSelectionWindow.Height", root, this.downloadSelectionWindowHeight);
			this.downloadSelectionWindowXPos = readIntValue("GUI.DownloadSelectionWindow.X", root, this.downloadSelectionWindowXPos);
			this.downloadSelectionWindowYPos = readIntValue("GUI.DownloadSelectionWindow.Y", root, this.downloadSelectionWindowYPos);
			this.saveDownloadSelectionWindowSizePosition = readBooleanValue("GUI.DownloadSelectionWindow.Save", root, this.saveDownloadSelectionWindowSizePosition);

			this.colWidthsQueue = readStringValue("GUI.colWidthsQueue", root, this.colWidthsQueue);
			this.colWidthsLog = readStringValue("GUI.colWidthsLog", root, this.colWidthsLog);
			this.colWidthsKeywords = readStringValue("GUI.colWidthsKeywords", root, this.colWidthsKeywords);
			this.colWidthsRules = readStringValue("GUI.colWidthsRules", root, this.colWidthsRules);
			this.colWidthsRulesEditor = readStringValue("GUI.colWidthsRulesEditor", root, this.colWidthsRulesEditor);
			this.colWidthsHosts = readStringValue("GUI.colWidthsHosts", root, this.colWidthsHosts);
			this.colWidthsSubdirs = readStringValue("GUI.colWidthsSubdirs", root, this.colWidthsSubdirs);
			this.colWidthsUpdate = readStringValue("GUI.colWidthsUpdate", root, this.colWidthsUpdate);
			this.colWidthsAdder = readStringValue("GUI.colWidthsAdder", root, this.colWidthsAdder);
			this.colWidthsAdderKeywordSelectorFilename = readStringValue("GUI.colWidthsAdderKeywordSelectorFilename", root, this.colWidthsAdderKeywordSelectorFilename);
			this.saveTableColumnSizes = readBooleanValue("GUI.saveTableColumnSizes", root, this.saveTableColumnSizes);

			this.tableSortOrdersKeywords = readStringValue("GUI.tableSortOrdersKeywords", root, this.tableSortOrdersKeywords);
			this.saveTableSortOrders = readBooleanValue("GUI.saveTableSortOrders", root, this.saveTableSortOrders);

			// Display
			int sv = readIntValue("GUI.Size", root, this.sizeView);
			if ((sv < 6) && (sv > -1)) {
				this.sizeView = sv;
			}
			int pv = readIntValue("GUI.Progress", root, this.progessView);
			if ((pv < 4) && (pv > -1)) {
				this.progessView = pv;
			}

			// Log
			String cdlf = readStringValue("GUI.currentDownloadLogFile", root, this.currentDownloadLogFile);
			File fileCDLF = new File(ApplicationProperties.getProperty("DownloadLogPath") + cdlf);
			if (fileCDLF.exists() && fileCDLF.isFile()) {
				this.currentDownloadLogFile = cdlf;
			}

			// DownloadRate
			this.downloadRate = readBooleanValue("GUI.DownloadRate", root, this.downloadRate);

			// LookAndFeel
			int laf = readIntValue("GUI.LAF", root, this.lookAndFeel);
			if ((laf < 9) && (laf > -1)) {
				this.lookAndFeel = laf;
			}

			// Targetdirectory and title
			this.alwaysAddTitle = readBooleanValue("GUI.AlwaysAddTitle", root, this.alwaysAddTitle);

			// AdderAdd
			Element eAdderAdd = getElementByPath("GUI.adderAdd", root);
			if (eAdderAdd != null && eAdderAdd.getChildren("add").size() > 0) {
				this.adderAdd = getListForXmlElement(eAdderAdd, "add");
			} else {
				String strAdderAdd = readStringValue("GUI.adderAdd", root, "");
				if (strAdderAdd != null) {
					String add[] = strAdderAdd.split("\\|");
					if (((add.length == 0) || ((add.length == 1) && (add[0].equals("")))) == false) {
						for (int i = add.length - 1; i >= 0; i--) {
							this.adderAdd.add(add[i]);
						}
					}
				}
			}

			// Downloaddirectory recognition
			this.deselectNoKeyword = readBooleanValue("GUI.DeselectNoKeyword", root, this.deselectNoKeyword);
			this.deleteNoKeyword = readBooleanValue("GUI.DeleteNoKeyword", root, this.deleteNoKeyword);

			// TargetDirChangeHistory
			Element eTargetDirChangeHistory = getElementByPath("GUI.targetDirChangeHistory", root);
			if (eTargetDirChangeHistory != null && eTargetDirChangeHistory.getChildren("targetDir").size() > 0) {
				this.targetDirChangeHistory = getListForXmlElement(eTargetDirChangeHistory, "targetDir");
			} else {
				String strTargetDirChangeHistory = readStringValue("GUI.targetDirChangeHistory", root, "");
				if (strTargetDirChangeHistory != null) {
					String add[] = strTargetDirChangeHistory.split("\\|");
					if (((add.length == 0) || ((add.length == 1) && (add[0].equals("")))) == false) {
						for (int i = add.length - 1; i >= 0; i--) {
							this.targetDirChangeHistory.add(add[i]);
						}
					}
				}
			}

			// FilenameChangeHistory
			Element eFilenameChangeHistory = getElementByPath("GUI.FilenameChangeHistory", root);
			if (eFilenameChangeHistory != null && eFilenameChangeHistory.getChildren("filename").size() > 0) {
				this.filenameChangeHistory = getListForXmlElement(eFilenameChangeHistory, "filename");
			} else {
				String strFilenameChangeHistory = readStringValue("GUI.FilenameChangeHistory", root, "");
				if (strFilenameChangeHistory != null) {
					String add[] = strFilenameChangeHistory.split("\\|");
					if (((add.length == 0) || ((add.length == 1) && (add[0].equals("")))) == false) {
						for (int i = add.length - 1; i >= 0; i--) {
							this.filenameChangeHistory.add(add[i]);
						}
					}
				}
			}

			// FilenameChangePrefix
			this.filenameChangePrefix = readStringValue("GUI.FilenameChangePrefix", root, this.filenameChangePrefix);
			this.appendPrefixFilenameChange = readBooleanValue("GUI.AppendPrefixFilenameChange", root, this.appendPrefixFilenameChange);

			// FilenameChangePrefix
			this.filenameChangeAppendix = readStringValue("GUI.FilenameChangeAppendix", root, this.filenameChangeAppendix);
			this.appendAppendixFilenameChange = readBooleanValue("GUI.AppendAppendixFilenameChange", root, this.appendAppendixFilenameChange);

			// FilenameChangeKeepOriginal
			this.filenameChangeKeepOriginal = readBooleanValue("GUI.filenameChangeKeepOriginal", root, this.filenameChangeKeepOriginal);

			// DownloadsCompleteNotification
			this.downloadsCompleteNotification = readBooleanValue("GUI.downloadsCompleteNotification", root, this.downloadsCompleteNotification);

			// Directory-Log-Filter
			this.directoryLogFilterEnabled = readBooleanValue("GUI.directoryLog.FilterEnabled", root, this.directoryLogFilterEnabled);

			int gdd = readIntValue("GUI.directoryLog.DirCount", root, this.directoryLogDirCount);
			if (gdd > -1) {
				this.directoryLogDirCount = gdd;
			}

			this.directoryLogOnlyExisting = readBooleanValue("GUI.directoryLog.OnlyExisting", root, this.directoryLogOnlyExisting);

			// DialogPaths
			this.lastUsedImportDialogPath = readStringValue("GUI.lastUsedImportDialogPath", root, this.lastUsedImportDialogPath);
			this.lastUsedExportDialogPath = readStringValue("GUI.lastUsedExportDialogPath", root, this.lastUsedExportDialogPath);

			// Image Previews
			this.downloadPreviews = readBooleanValue("GUI.downloadPreviews", root, this.downloadPreviews);
			int ps = readIntValue("GUI.previewSize", root, this.previewSize);
			if (ps >= 100 && ps <= 1000) {
				this.previewSize = ps;
			}

			/* Keywords */
			// Keyword-Filter
			this.keywordFilterEnabled = readBooleanValue("Keywords.FilterEnabled", root, this.keywordFilterEnabled);

			// KeywordSearch
			int kmm = readIntValue("Keywords.MatchMode", root, this.keywordMatchMode);
			if ((kmm < 3) && (kmm > -1)) {
				this.keywordMatchMode = kmm;
			}

			this.displayKeywordsWhenNoMatches = readBooleanValue("Keywords.displayKeywordsWhenNoMatches", root, this.displayKeywordsWhenNoMatches);

			/* Hosts */
			// Rules
			this.rulesBeforeClasses = readBooleanValue("Hosts.RulesBeforeClasses", root, this.rulesBeforeClasses);

			// DeactivatedHosts
			Element eDeactivatedHosts = getElementByPath("Hosts.deactivatedHosts", root);
			if (eDeactivatedHosts != null && eDeactivatedHosts.getChildren("host").size() > 0) {
				this.deactivatedHosts = getMapForXmlElement(eDeactivatedHosts, "host", "deactivated");
			}

			/* Downloads */
			// AutoStartDownloads
			this.autoStartDownloads = readBooleanValue("Downloads.AutoStartDownloads", root, this.autoStartDownloads);

			// Downloads
			this.overallDownloadedBytes = readLongValue("Downloads.DownloadedBytes", root, this.overallDownloadedBytes);
			this.overallDownloadedFiles = readLongValue("Downloads.DownloadedFiles", root, this.overallDownloadedFiles);

			// SaveLogs
			this.saveLogs = readBooleanValue("Downloads.SaveLogs", root, this.saveLogs);
			this.useOldDownloadLogMode = readBooleanValue("Downloads.useOldDownloadLogMode", root, this.useOldDownloadLogMode);

			// Max Failed Count
			int mfc = readIntValue("Downloads.MaxFailedCount", root, this.maxFailedCount);
			if (mfc > -1) {
				this.maxFailedCount = mfc;
			}

			// Min Filesize
			int mfs = readIntValue("Downloads.MinFilesize", root, this.minFilesize);
			if (mfs > -1) {
				this.minFilesize = mfs;
			}

			// Timeout
			int to = readIntValue("Downloads.Timeout", root, this.timeout);
			if (to > 1000) {
				this.timeout = to;
			}

			// Sort
			int sdos = readIntValue("Downloads.sortDownloadsOnStart", root, this.sortDownloadsOnStart);
			if (sdos >= 0 && sdos <= 5) {
				this.sortDownloadsOnStart = sdos;
			}

			// autoRetryAfterDownloadsComplete
			this.autoRetryAfterDownloadsComplete = readBooleanValue("Downloads.autoRetryAfterDownloadsComplete", root, this.autoRetryAfterDownloadsComplete);

			/* Other */
			// Updates
			this.updates = readBooleanValue("Other.Updates", root, this.updates);

			// CheckClipboard
			this.checkClipboard = readBooleanValue("Other.CheckClipboard", root, this.checkClipboard);

			// HTTP Port for connections from WebExtension
			int webExtPort = readIntValue("Other.WebExtensionPort", root, this.webExtensionPort);
			if (webExtPort >= 0 && webExtPort <= 65535) {
				this.webExtensionPort = webExtPort;
			}

			// Allowed chars
			int afc = readIntValue("Other.allowedFilenameChars", root, this.allowedFilenameChars);
			if ((afc > -1) && (afc < 3)) {
				this.allowedFilenameChars = afc;
			}

			this.backupDbOnStart = readBooleanValue("Other.backupDbOnStart", root, this.backupDbOnStart);
			this.defragDBOnStart = readBooleanValue("Other.defragDBOnStart", root, this.defragDBOnStart);
			long mds = readLongValue("Other.defragMinFilesize", root, this.defragMinFilesize);
			if (mds > -1) {
				this.defragMinFilesize = mds;
			}

			// Debug
			String dl = readStringValue("Other.debugLevel", root, this.debugLevel);
			if (checkDebugLevel(dl)) {
				this.debugLevel = dl;
				BHUtil.changeLog4JRootLoggerLevel(getDebugLevelForString(this.debugLevel));
			}

			// Threads
			int tc = readIntValue("Other.threadCount", root, this.threadCount);
			if ((tc > 0) && (tc <= Runtime.getRuntime().availableProcessors())) {
				this.threadCount = tc;
			}

			// OPTIONS
			readOptions(root);

			// Subdirs
			Element eDirs = root.getChild("Directories");
			if (eDirs != null) {
				List<Element> lSubdirs = eDirs.getChildren();
				for (int l = 0; l < lSubdirs.size(); l++) {
					Element eSub = lSubdirs.get(l);
					if (eSub.getName().matches("^Subdir[0-9]+$") == false) {
						continue;
					}
					String subVal = eSub.getValue();
					if ((subVal.length() > 0) && (subVal.contains("|"))) {
						String parts[] = subVal.split("\\|");
						if (parts.length == 7) {
							String name = parts[0];
							try {
								long min = Long.parseLong(parts[1]);
								long max = Long.parseLong(parts[2]);
								int resMinW = Integer.parseInt(parts[3]);
								int resMinH = Integer.parseInt(parts[4]);
								int resMaxW = Integer.parseInt(parts[5]);
								int resMaxH = Integer.parseInt(parts[6]);
								subdirs.add(new Subdir(name, min, max, resMinW, resMinH, resMaxW, resMaxH));
							} catch (NumberFormatException nfe) {
								logger.error(nfe.getMessage(), nfe);
							}
						}
					}
				}
			}

			// RegexReplaces Page-Title
			regexReplacePipelinePageTitle = new RegexReplacePipeline("regexReplacePageTitle", getRegexReplaces(root, "GUI", "regexReplacePageTitle"));

			// RegexReplaces Filename
			regexReplacePipelineFilename = new RegexReplacePipeline("regexReplaceFilename", getRegexReplaces(root, "Downloads", "regexReplaceFilename"));

			settingsChanged();
			doc = null;
			root = null;
			b = null;
			settingsFile = null;
			return true;
		} catch (JDOMException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		settingsFile = null;
		return false;
	}

	/**
	 * Save the Settings
	 * 
	 * @param noShutdown When the application is not shutdowned
	 * @return True if successful
	 */
	public boolean writeSettings(boolean noShutdown) {
		Element root = new Element("BilderHerunterlader");
		Document doc = new Document(root);
		File settingsFile;

		/* Directories */
		// SavePath
		addStringValue("Directories.SavePath", this.savePath, root);
		addBooleanValue("Directories.RememberLastUsedPath", this.saveLastPath, root);

		// AutoTargetDir
		addBooleanValue("Directories.AutoTargetDir", this.autoTargetdir, root);

		// AutoTargetDirMode
		addIntValue("Directories.AutoTargetDirMode", this.autoTargetDirMode, root);

		// Subdirs
		addBooleanValue("Directories.subdirsEnabled", this.subdirsEnabled, root);
		addIntValue("Directories.subdirsResolutionMode", this.subdirsResolutionMode, root);

		/* Connection */
		// Connections
		addIntValue("Connection.Connections", this.connections, root);

		addIntValue("Connection.connectionsPerHost", this.connectionsPerHost, root);

		// Proxy
		addIntValue("Connection.Proxy.Mode", this.proxymode, root);
		addStringValue("Connection.Proxy.Name", this.proxyname, root);
		addIntValue("Connection.Proxy.Port", this.proxyport, root);
		addStringValue("Connection.Proxy.User", this.proxyuser, root);
		addStringValue("Connection.Proxy.Passwort", this.proxypassword, root);
		addBooleanValue("Connection.Proxy.Auth", this.proxyauth, root);

		// Cookies
		addIntValue("Connection.cookiesFromBrowser", this.cookiesFromBrowser, root);
		addStringValue("Connection.cookieFileOpera", this.cookieFileOpera, root);
		addBooleanValue("Connection.cookieFileOperaFixed", this.cookieFileOperaFixed, root);
		addStringValue("Connection.cookieFileOperaNew", this.cookieFileOperaNew, root);
		addBooleanValue("Connection.cookieFileOperaNewFixed", this.cookieFileOperaNewFixed, root);
		addStringValue("Connection.cookieFileFirefox", this.cookieFileFirefox, root);
		addBooleanValue("Connection.cookieFileFirefoxFixed", this.cookieFileFirefoxFixed, root);
		addStringValue("Connection.cookieFilePaleMoon", this.cookieFilePaleMoon, root);
		addBooleanValue("Connection.cookieFilePaleMoonFixed", this.cookieFilePaleMoonFixed, root);

		// User-Agent
		addStringValue("Connection.userAgent", this.userAgent, root);

		/* GUI */
		// Language
		addStringValue("GUI.Language", this.language, root);

		// Window
		addIntValue("GUI.Window.Width", this.windowWidth, root);
		addIntValue("GUI.Window.Height", this.windowHeight, root);
		addIntValue("GUI.Window.X", this.windowXPos, root);
		addIntValue("GUI.Window.Y", this.windowYPos, root);
		addIntValue("GUI.Window.State", this.windowState, root);
		addBooleanValue("GUI.Window.Save", this.saveWindowSizePosition, root);

		// Download-Selection-Window
		addIntValue("GUI.DownloadSelectionWindow.Width", this.downloadSelectionWindowWidth, root);
		addIntValue("GUI.DownloadSelectionWindow.Height", this.downloadSelectionWindowHeight, root);
		addIntValue("GUI.DownloadSelectionWindow.X", this.downloadSelectionWindowXPos, root);
		addIntValue("GUI.DownloadSelectionWindow.Y", this.downloadSelectionWindowYPos, root);
		addBooleanValue("GUI.DownloadSelectionWindow.Save", this.saveDownloadSelectionWindowSizePosition, root);

		addStringValue("GUI.colWidthsQueue", this.colWidthsQueue, root);
		addStringValue("GUI.colWidthsLog", this.colWidthsLog, root);
		addStringValue("GUI.colWidthsKeywords", this.colWidthsKeywords, root);
		addStringValue("GUI.colWidthsRules", this.colWidthsRules, root);
		addStringValue("GUI.colWidthsRulesEditor", this.colWidthsRulesEditor, root);
		addStringValue("GUI.colWidthsHosts", this.colWidthsHosts, root);
		addStringValue("GUI.colWidthsSubdirs", this.colWidthsSubdirs, root);
		addStringValue("GUI.colWidthsUpdate", this.colWidthsUpdate, root);
		addStringValue("GUI.colWidthsAdder", this.colWidthsAdder, root);
		addStringValue("GUI.colWidthsAdderKeywordSelectorFilename", this.colWidthsAdderKeywordSelectorFilename, root);
		addBooleanValue("GUI.saveTableColumnSizes", this.saveTableColumnSizes, root);

		addStringValue("GUI.tableSortOrdersKeywords", this.tableSortOrdersKeywords, root);
		addBooleanValue("GUI.saveTableSortOrders", this.saveTableSortOrders, root);

		// Display
		addIntValue("GUI.Size", this.sizeView, root);
		addIntValue("GUI.Progress", this.progessView, root);

		// Log
		addStringValue("GUI.currentDownloadLogFile", this.currentDownloadLogFile, root);

		// DownloadRate
		addBooleanValue("GUI.DownloadRate", this.downloadRate, root);

		// LookAndFeel
		addIntValue("GUI.LAF", this.lookAndFeel, root);

		// Targetdirectory and title
		addBooleanValue("GUI.AlwaysAddTitle", this.alwaysAddTitle, root);

		// AdderAdd
		addSubElement("GUI", getXmlElementForList(this.adderAdd, "adderAdd", "add"), root);

		// Downloaddirectory recognition
		addBooleanValue("GUI.DeselectNoKeyword", this.deselectNoKeyword, root);
		addBooleanValue("GUI.DeleteNoKeyword", this.deleteNoKeyword, root);

		// TargetDirChangeHistory
		addSubElement("GUI", getXmlElementForList(this.targetDirChangeHistory, "targetDirChangeHistory", "targetDir"), root);

		// FilenameChangeHistory
		addSubElement("GUI", getXmlElementForList(this.filenameChangeHistory, "FilenameChangeHistory", "filename"), root);

		// FilenameChangePrefix
		addStringValue("GUI.FilenameChangePrefix", this.filenameChangePrefix, root);
		addBooleanValue("GUI.AppendPrefixFilenameChange", this.appendPrefixFilenameChange, root);

		// FilenameChangePrefix
		addStringValue("GUI.FilenameChangeAppendix", this.filenameChangeAppendix, root);
		addBooleanValue("GUI.AppendAppendixFilenameChange", this.appendAppendixFilenameChange, root);

		// FilenameChangeKeepOriginal
		addBooleanValue("GUI.filenameChangeKeepOriginal", this.filenameChangeKeepOriginal, root);

		// DownloadsCompleteNotification
		addBooleanValue("GUI.downloadsCompleteNotification", this.downloadsCompleteNotification, root);

		// Directory-Log-Filter
		addBooleanValue("GUI.directoryLog.FilterEnabled", this.directoryLogFilterEnabled, root);
		addIntValue("GUI.directoryLog.DirCount", this.directoryLogDirCount, root);
		addBooleanValue("GUI.directoryLog.OnlyExisting", this.directoryLogOnlyExisting, root);

		// DialogPaths
		addStringValue("GUI.lastUsedImportDialogPath", this.lastUsedImportDialogPath, root);
		addStringValue("GUI.lastUsedExportDialogPath", this.lastUsedExportDialogPath, root);

		// Image Previews
		addBooleanValue("GUI.downloadPreviews", this.downloadPreviews, root);
		addIntValue("GUI.previewSize", this.previewSize, root);

		/* Keywords */
		// Keyword-Filter
		addBooleanValue("Keywords.FilterEnabled", this.keywordFilterEnabled, root);

		// KeywordSearch
		addIntValue("Keywords.MatchMode", this.keywordMatchMode, root);
		addBooleanValue("Keywords.displayKeywordsWhenNoMatches", this.displayKeywordsWhenNoMatches, root);

		/* Hosts */
		// Rules
		addBooleanValue("Hosts.RulesBeforeClasses", this.rulesBeforeClasses, root);

		// DeactivatedHosts
		addSubElement("Hosts", getXmlElementForMap(this.deactivatedHosts, "deactivatedHosts", "host", "deactivated"), root);

		/* Downloads */
		// AutoStartDownloads
		addBooleanValue("Downloads.AutoStartDownloads", this.autoStartDownloads, root);

		// Downloads
		addLongValue("Downloads.DownloadedBytes", this.overallDownloadedBytes, root);
		addLongValue("Downloads.DownloadedFiles", this.overallDownloadedFiles, root);

		// SaveLogs
		addBooleanValue("Downloads.SaveLogs", this.saveLogs, root);
		addBooleanValue("Downloads.useOldDownloadLogMode", this.useOldDownloadLogMode, root);

		// Max Failed Count
		addIntValue("Downloads.MaxFailedCount", this.maxFailedCount, root);

		// Min Filesize
		addIntValue("Downloads.MinFilesize", this.minFilesize, root);

		// Timeout
		addIntValue("Downloads.Timeout", this.timeout, root);

		// Sort
		addIntValue("Downloads.sortDownloadsOnStart", this.sortDownloadsOnStart, root);

		// autoRetryAfterDownloadsComplete
		addBooleanValue("Downloads.autoRetryAfterDownloadsComplete", this.autoRetryAfterDownloadsComplete, root);

		/* Other */
		// Updates
		addBooleanValue("Other.Updates", this.updates, root);

		// CheckClipboard
		addBooleanValue("Other.CheckClipboard", this.checkClipboard, root);

		// HTTP Port for connections from WebExtension
		addIntValue("Other.WebExtensionPort", this.webExtensionPort, root);

		// Allowed chars
		addIntValue("Other.allowedFilenameChars", this.allowedFilenameChars, root);

		// Defrag
		addBooleanValue("Other.backupDbOnStart", this.backupDbOnStart, root);
		addBooleanValue("Other.defragDBOnStart", this.defragDBOnStart, root);
		addLongValue("Other.defragMinFilesize", this.defragMinFilesize, root);

		// Debug
		addStringValue("Other.debugLevel", this.debugLevel, root);

		// Threads
		addIntValue("Other.threadCount", this.threadCount, root);

		for (int x = 0; x < options.size(); x++) {
			Option<?> option = options.get(x);
			addElement(option.getPath(), option.getValueAsString(), root, option.getValueTypeName());
		}

		// Subdirs
		for (int xx = 0; xx < subdirs.size(); xx++) {
			Subdir sub = subdirs.get(xx);
			addStringValue("Directories.Subdir" + xx, sub.getSubdirName() + "|" + sub.getMinSize() + "|" + sub.getMaxSize() + "|" + sub.getMinWidth() + "|" + sub.getMinHeight() + "|"
					+ sub.getMaxWidth() + "|" + sub.getMaxHeight(), root);
		}

		// RegexReplaces Page-Title
		addSubElement("GUI", getRegexReplaceXmlElement(regexReplacePipelinePageTitle, "regexReplacePageTitle"), root);

		// RegexReplaces Filename
		addSubElement("Downloads", getRegexReplaceXmlElement(regexReplacePipelineFilename, "regexReplaceFilename"), root);

		try {
			settingsFile = new File(strSettingsFile);
			FileOutputStream fos;
			fos = new FileOutputStream(settingsFile);
			XMLOutputter serializer = new XMLOutputter();
			serializer.setFormat(Format.getPrettyFormat());
			serializer.output(doc, fos);
			fos.flush();
			fos.close();
			serializer = null;
			fos = null;
			doc = null;
			root = null;
			settingsFile = null;
			if (noShutdown) {
				CopyUtil.copy(strSettingsFile, strSettingsFileBackup);
			}
			return true;
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		settingsFile = null;
		return false;
	}

	/**
	 * Pruefen ob int in definiertem Bereich liegt
	 * 
	 * @param min Minimum
	 * @param max Maxmimum
	 * @param value Wert
	 * @return TRUE/FALSE
	 */
	public static boolean checkIntValue(int min, int max, int value) {
		if ((value >= min) && (value <= max)) {
			return true;
		}
		return false;
	}

	private List<RegexReplace> getRegexReplaces(Element root, String rootChild, String subChild) {
		List<RegexReplace> regexps = new ArrayList<>();

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
				regexps.add(new RegexReplace(search, replace));
			} catch (Exception ex) {
				logger.error("Could not load regexp child from element '{}' in RegexReplacePipeline '{}': {}", eSubChild, subChild, child.toString(), ex);
				throw ex;
			}
		}

		return regexps;
	}

	private Element getRegexReplaceXmlElement(RegexReplacePipeline regexReplacePipeline, String elementTitle) {
		Element e = new Element(elementTitle);
		for (RegexReplace child : regexReplacePipeline.getRegexps()) {
			Element elRegex = new Element("regexp");
			elRegex.setAttribute("search", child.getSearch());
			elRegex.setAttribute("replace", child.getReplace());
			e.addContent(elRegex);
		}
		return e;
	}

	private Element getXmlElementForList(List<String> values, String title, String valuesName) {
		Element e = new Element(title);
		for (String s : values) {
			Element el = new Element(valuesName);
			el.setText(s);
			e.addContent(el);
		}
		return e;
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

	private Element getXmlElementForMap(Map<String, Boolean> map, String title, String valuesName, String attributeName) {
		Element e = new Element(title);
		for (Map.Entry<String, Boolean> entry : map.entrySet()) {
			Element el = new Element(valuesName);
			el.setText(entry.getKey());
			el.setAttribute(attributeName, String.valueOf(entry.getValue()));
			e.addContent(el);
		}
		return e;
	}

	private Map<String, Boolean> getMapForXmlElement(Element e, String valuesName, String attributeName) {
		Map<String, Boolean> map = new HashMap<>();
		List<Element> childs = e.getChildren(valuesName);
		for (int i = 0; i < childs.size(); i++) {
			String val = childs.get(i).getValue();
			if (val != null) {
				map.put(val, Boolean.parseBoolean(childs.get(i).getAttributeValue(attributeName)));
			}
		}
		return map;
	}

	/*
	 * START OF LISTENER METHODS
	 */

	/**
	 * Benachrichtigung ueber Einstellungsaenderungen
	 */
	private void settingsChanged() {
		for (BHSettingsListener listener : listeners) {
			listener.settingsChanged();
		}
	}

	/**
	 * Listener hinzufuegen
	 * 
	 * @param l Listener
	 */
	public void addSettingsListener(BHSettingsListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	/**
	 * Listener loeschen
	 * 
	 * @param l Listener
	 */
	public void removeSettingsListener(BHSettingsListener l) {
		listeners.remove(l);
	}

	/*
	 * END OF LISTENER METHODS
	 */

	/*
	 * START OF EXTERNAL OPTION METHODS
	 */
	private void readOptions(Element root) {
		List<Element> l = root.getChildren();
		if (l.size() > 0) {
			Iterator<Element> it = l.iterator();
			while (it.hasNext()) {
				readOption("", it.next());
			}
		}
	}

	private void readOption(String path, Element e) {
		if (path.equals("")) {
			path += e.getName();
		} else {
			path += "." + e.getName();
		}
		List<Element> l = e.getChildren();
		if (l.size() > 0) {
			Iterator<Element> it = l.iterator();
			while (it.hasNext()) {
				readOption(path, it.next());
			}
		}
		if (checkOptionPath(path)) {
			String datatype = e.getAttributeValue("datatype");
			if (datatype == null) {
				return;
			}
			String value;
			if (datatype.equals("boolean")) {
				value = e.getText();
				boolean setval = parseBooleanValue(value, false);
				try {
					setOptionValue(path, setval);
				} catch (OptionException e1) {
					logger.error(e1.getMessage(), e1);
				}
			} else if (datatype.equals("int")) {
				value = e.getText();
				int setval = parseIntValue(value, 0);
				try {
					setOptionValue(path, setval);
				} catch (OptionException e1) {
					logger.error(e1.getMessage(), e1);
				}
			} else if (datatype.equals("long")) {
				value = e.getText();
				long setval = parseLongValue(value, 0);
				try {
					setOptionValue(path, setval);
				} catch (OptionException e1) {
					logger.error(e1.getMessage(), e1);
				}
			} else if (datatype.equals("string")) {
				value = e.getText();
				try {
					setOptionValue(path, value);
				} catch (OptionException e1) {
					logger.error(e1.getMessage(), e1);
				}
			} else if (datatype.equals("byte")) {
				value = e.getText();
				byte b = 0;
				byte setval = parseByteValue(value, b);
				try {
					setOptionValue(path, setval);
				} catch (OptionException e1) {
					logger.error(e1.getMessage(), e1);
				}
			} else if (datatype.equals("short")) {
				value = e.getText();
				short s = 0;
				short setval = parseShortValue(value, s);
				try {
					setOptionValue(path, setval);
				} catch (OptionException e1) {
					logger.error(e1.getMessage(), e1);
				}
			} else if (datatype.equals("float")) {
				value = e.getText();
				float setval = parseFloatValue(value, 0);
				try {
					setOptionValue(path, setval);
				} catch (OptionException e1) {
					logger.error(e1.getMessage(), e1);
				}
			} else if (datatype.equals("double")) {
				value = e.getText();
				double setval = parseDoubleValue(value, 0);
				try {
					setOptionValue(path, setval);
				} catch (OptionException e1) {
					logger.error(e1.getMessage(), e1);
				}
			}
		}
	}

	/**
	 * Aendert den Wert einer Einstellung
	 * Falls die Aenderung nicht erfolgreich war wird eine OptionException geworfen, welche
	 * den Grund fuer den Fehlschlag angibt.
	 * 
	 * @param path Pfad der Einstellung
	 * @param value Wert
	 * @throws OptionException
	 */
	@SuppressWarnings("unchecked")
	public synchronized <E> void setOptionValue(String path, E value) throws OptionException {
		Class<E> valueType = (Class<E>)value.getClass();
		Option<?> option = getOptionForPath(path);
		if (option == null) {
			option = Option.getNewOptionByClass(valueType, path);
			if (option == null) {
				throw new OptionException("No Options for this type! You can only use Boolean, Byte, Double, Float, Integer, Long, Short or String");
			}
			options.add(option);
		} else if (!Option.checkType(option, valueType)) {
			throw new OptionException("Could not set Value because there is already an Option with the same path, but with another datatype: " + path);
		}

		((Option<E>)option).setValue(value);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @param valueType Value Type
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized <E> E getOptionValue(String path, Class<? extends Option<E>> valueType) throws OptionException {
		Option<?> option = getOptionForPath(path);
		if (option == null) {
			throw new OptionException("Option does not exists: " + path);
		} else if (valueType.isInstance(option)) {
			return valueType.cast(option).getValue();
		} else {
			throw new OptionException("Could not get Value because the Option with this path has another datatype: " + path);
		}
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @param valueType Value Type
	 * @param defaultValue Default Value
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized <E> E getOptionValue(String path, Class<? extends Option<E>> valueType, E defaultValue) throws OptionException {
		Option<?> option = getOptionForPath(path);
		if (option == null) {
			return defaultValue;
		} else if (valueType.isInstance(option)) {
			return valueType.cast(option).getValue();
		} else {
			throw new OptionException("Could not get Value because the Option with this path has another datatype: " + path);
		}
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized boolean getBooleanValue(String path) throws OptionException {
		return getOptionValue(path, OptionBoolean.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @param defautValue Default Value
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized boolean getBooleanValue(String path, boolean defautValue) throws OptionException {
		return getOptionValue(path, OptionBoolean.class, defautValue);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized int getIntValue(String path) throws OptionException {
		return getOptionValue(path, OptionInt.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @param defautValue Default Value
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized int getIntValue(String path, int defautValue) throws OptionException {
		return getOptionValue(path, OptionInt.class, defautValue);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized String getStringValue(String path) throws OptionException {
		return getOptionValue(path, OptionString.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @param defautValue Default Value
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized String getStringValue(String path, String defautValue) throws OptionException {
		return getOptionValue(path, OptionString.class, defautValue);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized long getLongValue(String path) throws OptionException {
		return getOptionValue(path, OptionLong.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @param defautValue Default Value
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized long getLongValue(String path, long defautValue) throws OptionException {
		return getOptionValue(path, OptionLong.class, defautValue);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized byte getByteValue(String path) throws OptionException {
		return getOptionValue(path, OptionByte.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @param defautValue Default Value
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized byte getByteValue(String path, byte defautValue) throws OptionException {
		return getOptionValue(path, OptionByte.class, defautValue);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized short getShortValue(String path) throws OptionException {
		return getOptionValue(path, OptionShort.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @param defautValue Default Value
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized short getShortValue(String path, short defautValue) throws OptionException {
		return getOptionValue(path, OptionShort.class, defautValue);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized float getFloatValue(String path) throws OptionException {
		return getOptionValue(path, OptionFloat.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @param defautValue Default Value
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized float getFloatValue(String path, float defautValue) throws OptionException {
		return getOptionValue(path, OptionFloat.class, defautValue);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized double getDoubleValue(String path) throws OptionException {
		return getOptionValue(path, OptionDouble.class);
	}

	/**
	 * Gibt den Wert zurueck
	 * 
	 * @param path Pfad der Einstellung
	 * @param defautValue Default Value
	 * @return Wert
	 * @throws OptionException
	 */
	public synchronized double getDoubleValue(String path, double defautValue) throws OptionException {
		return getOptionValue(path, OptionDouble.class, defautValue);
	}

	/**
	 * Gibt die Option zurueck oder null
	 * 
	 * @param path Pfad der Option
	 * @return Option oder null
	 * @throws OptionException
	 */
	private Option<?> getOptionForPath(String path) throws OptionException {
		boolean b = checkOptionPath(path);
		if (b == false) {
			throw new OptionException("This path is restricted by the SettingsManager and can not be get or set by this way.");
		}
		for (int i = 0; i < options.size(); i++) {
			if (options.get(i).getPath().equals(path)) {
				return options.get(i);
			}
		}
		return null;
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

	/*
	 * END OF EXTERNAL OPTION METHODS
	 */

	/*
	 * START OF DEBUG LEVEL
	 */

	/**
	 * Checks if the string is an available debug-level
	 * 
	 * @param debugLevel Debug-Level
	 * @return True if Ok
	 */
	private boolean checkDebugLevel(String debugLevel) {
		if (debugLevel.equalsIgnoreCase("Off")) {
			return true;
		} else if (debugLevel.equalsIgnoreCase("All")) {
			return true;
		} else if (debugLevel.equalsIgnoreCase("Info")) {
			return true;
		} else if (debugLevel.equalsIgnoreCase("Trace")) {
			return true;
		} else if (debugLevel.equalsIgnoreCase("Debug")) {
			return true;
		} else if (debugLevel.equalsIgnoreCase("Warn")) {
			return true;
		} else if (debugLevel.equalsIgnoreCase("Error")) {
			return true;
		} else if (debugLevel.equalsIgnoreCase("Fatal")) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the level
	 * If the string is not ok, Level.WARN is returned
	 * 
	 * @param debugLevel Debug-Level
	 * @return Level
	 */
	private Level getDebugLevelForString(String debugLevel) {
		if (debugLevel.equalsIgnoreCase("Off")) {
			return Level.OFF;
		} else if (debugLevel.equalsIgnoreCase("All")) {
			return Level.ALL;
		} else if (debugLevel.equalsIgnoreCase("Info")) {
			return Level.INFO;
		} else if (debugLevel.equalsIgnoreCase("Trace")) {
			return Level.TRACE;
		} else if (debugLevel.equalsIgnoreCase("Debug")) {
			return Level.DEBUG;
		} else if (debugLevel.equalsIgnoreCase("Warn")) {
			return Level.WARN;
		} else if (debugLevel.equalsIgnoreCase("Error")) {
			return Level.ERROR;
		} else if (debugLevel.equalsIgnoreCase("Fatal")) {
			return Level.FATAL;
		}
		return Level.WARN;
	}

	/*
	 * END OF DEBUG LEVEL
	 */

	/*
	 * START OF SUBDIRS
	 */

	/**
	 * @return the subdirs
	 */
	public List<Subdir> getSubdirs() {
		return subdirs;
	}

	/**
	 * @param subdirs Sub Directories
	 * @param overwrite Overwrite
	 */
	public void addSubdirs(List<Subdir> subdirs, boolean overwrite) {
		if (overwrite) {
			this.subdirs.clear();
			this.subdirs = null;
			this.subdirs = subdirs;
		} else {
			Iterator<Subdir> it = subdirs.iterator();
			while (it.hasNext()) {
				subdirs.add(it.next());
			}
		}
	}

	/**
	 * @param subdir Sub Directory
	 */
	public void addSubdir(Subdir subdir) {
		subdirs.add(subdir);
	}

	/**
	 * @param subdir Sub Directory
	 */
	public void removeSubdir(Subdir subdir) {
		subdirs.remove(subdir);
	}

	/*
	 * END OF SUBDIRS
	 */

	/*
	 * NOW HERE ARE THE HARDCODED GETTER- AND SETTER-METHODS
	 */

	/**
	 * Get-Method
	 * 
	 * @return Einstellungs-Datei
	 */
	public String getSettingsFile() {
		return strSettingsFile;
	}

	/**
	 * Get-Method
	 * 
	 * @return Pfad der Einstellungen
	 */
	public String getSettingsPath() {
		return strSettingsPath;
	}

	/**
	 * Get-Method
	 * 
	 * @return Anzahl Verbindungen
	 */
	public int getConnections() {
		return connections;
	}

	/**
	 * Set-Method
	 * 
	 * @param connections Anzahl Verbindungen
	 */
	public void setConnections(int connections) {
		if (connections < 1) {
			this.connections = 1;
		} else if (connections > 50) {
			this.connections = 50;
		} else {
			this.connections = connections;
		}
		settingsChanged();
	}

	/**
	 * Get-Method
	 * 
	 * @return Standard Zielpfad
	 */
	public String getSavePath() {
		return savePath;
	}

	/**
	 * Set-Method
	 * 
	 * @param savePath Standard Zielpfad
	 */
	public void setSavePath(String savePath) {
		this.savePath = savePath;
		settingsChanged();
	}

	/**
	 * Get-Method
	 * 
	 * @return Automatische Suche nach Updates
	 */
	public boolean isUpdates() {
		return updates;
	}

	/**
	 * Set-Method
	 * 
	 * @param updates Automatische Suche nach Updates
	 */
	public void setUpdates(boolean updates) {
		this.updates = updates;
	}

	/**
	 * Get-Method
	 * 
	 * @return Automatische Zielerkennung
	 */
	public boolean isAutoTargetdir() {
		return autoTargetdir;
	}

	/**
	 * Set-Method
	 * 
	 * @param autoTargetdir Automatische Zielerkennung
	 */
	public void setAutoTargetdir(boolean autoTargetdir) {
		this.autoTargetdir = autoTargetdir;
	}

	/**
	 * Get-Method
	 * 
	 * @return Automatischer Zielerkennungs Modus
	 */
	public int getAutoTargetDirMode() {
		return autoTargetDirMode;
	}

	/**
	 * Set-Method
	 * 
	 * @param autoTargetDirMode Automatischer Zielerkennungs Modus
	 */
	public void setAutoTargetDirMode(int autoTargetDirMode) {
		if (checkIntValue(0, 1, autoTargetDirMode)) {
			this.autoTargetDirMode = autoTargetDirMode;
		}
	}

	/**
	 * Get-Method
	 * 
	 * @return Downloads automatisch starten
	 */
	public boolean isAutoStartDownloads() {
		return autoStartDownloads;
	}

	/**
	 * Set-Method
	 * 
	 * @param autoStartDownloads Downloads automatisch starten
	 */
	public void setAutoStartDownloads(boolean autoStartDownloads) {
		this.autoStartDownloads = autoStartDownloads;
	}

	/**
	 * Get-Method
	 * 
	 * @return Logs speichern
	 */
	public boolean isSaveLogs() {
		return saveLogs;
	}

	/**
	 * Set-Method
	 * 
	 * @param saveLogs Logs speichern
	 */
	public void setSaveLogs(boolean saveLogs) {
		this.saveLogs = saveLogs;
	}

	/**
	 * Get-Method
	 * 
	 * @return Zwischenablage auf Links pruefen
	 */
	public boolean isCheckClipboard() {
		return checkClipboard;
	}

	/**
	 * Set-Method
	 * 
	 * @param checkClipboard Zwischenablage auf Links pruefen
	 */
	public void setCheckClipboard(boolean checkClipboard) {
		this.checkClipboard = checkClipboard;
		writeSettings(true);
		settingsChanged();
	}

	/**
	 * Returns the webExtensionPort
	 * 
	 * @return webExtensionPort
	 */
	public int getWebExtensionPort() {
		return webExtensionPort;
	}

	/**
	 * Sets the webExtensionPort
	 * 
	 * @param webExtensionPort webExtensionPort
	 */
	public void setWebExtensionPort(int webExtensionPort) {
		if (checkIntValue(0, 65535, webExtensionPort)) {
			this.webExtensionPort = webExtensionPort;
		}
	}

	/**
	 * Get-Method
	 * 
	 * @return Sprache
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Set-Method
	 * 
	 * @param language Sprache
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Get-Method
	 * 
	 * @return Geladene Bytes
	 */
	public long getOverallDownloadedBytes() {
		return overallDownloadedBytes;
	}

	/**
	 * Set-Method
	 * 
	 * @param downloadedBytes Geladene Bytes
	 */
	public synchronized void increaseOverallDownloadedBytes(long downloadedBytes) {
		this.overallDownloadedBytes += downloadedBytes;
	}

	/**
	 * Get-Method
	 * 
	 * @return Anzahl geladene Dateien
	 */
	public long getOverallDownloadedFiles() {
		return overallDownloadedFiles;
	}

	/**
	 * Set-Method
	 * 
	 * @param downloadedFiles Anzahl geladene Dateien
	 */
	public synchronized void increaseOverallDownloadedFiles(long downloadedFiles) {
		this.overallDownloadedFiles += downloadedFiles;
	}

	/**
	 * @return the proxyauth
	 */
	public boolean isProxyauth() {
		return proxyauth;
	}

	/**
	 * @param proxyauth the proxyauth to set
	 */
	public void setProxyauth(boolean proxyauth) {
		this.proxyauth = proxyauth;
	}

	/**
	 * @return the proxymode
	 */
	public int getProxymode() {
		return proxymode;
	}

	/**
	 * @param proxymode the proxymode to set
	 */
	public void setProxymode(int proxymode) {
		if (checkIntValue(0, 1, proxymode)) {
			this.proxymode = proxymode;
		}
	}

	/**
	 * @return the proxyname
	 */
	public String getProxyname() {
		return proxyname;
	}

	/**
	 * @param proxyname the proxyname to set
	 */
	public void setProxyname(String proxyname) {
		this.proxyname = proxyname;
	}

	/**
	 * @return the proxypassword
	 */
	public String getProxypassword() {
		return proxypassword;
	}

	/**
	 * @param proxypassword the proxypassword to set
	 */
	public void setProxypassword(String proxypassword) {
		this.proxypassword = proxypassword;
	}

	/**
	 * @return the proxyport
	 */
	public int getProxyport() {
		return proxyport;
	}

	/**
	 * @param proxyport the proxyport to set
	 */
	public void setProxyport(int proxyport) {
		if (checkIntValue(0, 65535, proxyport)) {
			this.proxyport = proxyport;
		}
	}

	/**
	 * @return the proxyuser
	 */
	public String getProxyuser() {
		return proxyuser;
	}

	/**
	 * @param proxyuser the proxyuser to set
	 */
	public void setProxyuser(String proxyuser) {
		this.proxyuser = proxyuser;
	}

	/**
	 * @return the progessView
	 */
	public int getProgessView() {
		return progessView;
	}

	/**
	 * @param progessView the progessView to set
	 */
	public void setProgessView(int progessView) {
		if (checkIntValue(0, 3, progessView)) {
			this.progessView = progessView;
		}
	}

	/**
	 * @return the sizeView
	 */
	public int getSizeView() {
		return sizeView;
	}

	/**
	 * @param sizeView the sizeView to set
	 */
	public void setSizeView(int sizeView) {
		if (checkIntValue(0, 5, sizeView)) {
			this.sizeView = sizeView;
		}
	}

	/**
	 * @return the keywordMatchMode
	 */
	public int getKeywordMatchMode() {
		return keywordMatchMode;
	}

	/**
	 * @param keywordMatchMode the keywordMatchMode to set
	 */
	public void setKeywordMatchMode(int keywordMatchMode) {
		if (checkIntValue(0, 2, keywordMatchMode)) {
			this.keywordMatchMode = keywordMatchMode;
		}
	}

	/**
	 * Returns the displayKeywordsWhenNoMatches
	 * 
	 * @return displayKeywordsWhenNoMatches
	 */
	public boolean isDisplayKeywordsWhenNoMatches() {
		return displayKeywordsWhenNoMatches;
	}

	/**
	 * Sets the displayKeywordsWhenNoMatches
	 * 
	 * @param displayKeywordsWhenNoMatches displayKeywordsWhenNoMatches
	 */
	public void setDisplayKeywordsWhenNoMatches(boolean displayKeywordsWhenNoMatches) {
		this.displayKeywordsWhenNoMatches = displayKeywordsWhenNoMatches;
	}

	/**
	 * @return the lookAndFeel
	 */
	public int getLookAndFeel() {
		return lookAndFeel;
	}

	/**
	 * @param lookAndFeel the lookAndFeel to set
	 */
	public void setLookAndFeel(int lookAndFeel) {
		if (checkIntValue(0, 9, lookAndFeel)) {
			this.lookAndFeel = lookAndFeel;
			for (BHSettingsListener listener : listeners) {
				listener.lookAndFeelChanged(lookAndFeel);
			}
		}
	}

	/**
	 * @return the languageFirstRun
	 */
	public boolean isLanguageFirstRun() {
		return languageFirstRun;
	}

	/**
	 * @param languageFirstRun the languageFirstRun to set
	 */
	public void setLanguageFirstRun(boolean languageFirstRun) {
		this.languageFirstRun = languageFirstRun;
	}

	/**
	 * @return the windowWidth
	 */
	public int getWindowWidth() {
		return windowWidth;
	}

	/**
	 * @param windowWidth the windowWidth to set
	 */
	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	/**
	 * @return the windowHeight
	 */
	public int getWindowHeight() {
		return windowHeight;
	}

	/**
	 * @param windowHeight the windowHeight to set
	 */
	public void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
	}

	/**
	 * @return the windowState
	 */
	public int getWindowState() {
		return windowState;
	}

	/**
	 * @param windowState the windowState to set
	 */
	public void setWindowState(int windowState) {
		this.windowState = windowState;
	}

	/**
	 * @return the saveWindowSizePosition
	 */
	public boolean isSaveWindowSizePosition() {
		return saveWindowSizePosition;
	}

	/**
	 * @param saveWindowSizePosition the saveWindowSizePosition to set
	 */
	public void setSaveWindowSizePosition(boolean saveWindowSizePosition) {
		this.saveWindowSizePosition = saveWindowSizePosition;
	}

	/**
	 * @return the windowXPos
	 */
	public int getWindowXPos() {
		return windowXPos;
	}

	/**
	 * @param windowXPos the windowXPos to set
	 */
	public void setWindowXPos(int windowXPos) {
		this.windowXPos = windowXPos;
	}

	/**
	 * @return the windowYPos
	 */
	public int getWindowYPos() {
		return windowYPos;
	}

	/**
	 * @param windowYPos the windowYPos to set
	 */
	public void setWindowYPos(int windowYPos) {
		this.windowYPos = windowYPos;
	}

	/**
	 * Returns the downloadSelectionWindowWidth
	 * 
	 * @return downloadSelectionWindowWidth
	 */
	public int getDownloadSelectionWindowWidth() {
		return downloadSelectionWindowWidth;
	}

	/**
	 * Sets the downloadSelectionWindowWidth
	 * 
	 * @param downloadSelectionWindowWidth downloadSelectionWindowWidth
	 */
	public void setDownloadSelectionWindowWidth(int downloadSelectionWindowWidth) {
		this.downloadSelectionWindowWidth = downloadSelectionWindowWidth;
	}

	/**
	 * Returns the downloadSelectionWindowHeight
	 * 
	 * @return downloadSelectionWindowHeight
	 */
	public int getDownloadSelectionWindowHeight() {
		return downloadSelectionWindowHeight;
	}

	/**
	 * Sets the downloadSelectionWindowHeight
	 * 
	 * @param downloadSelectionWindowHeight downloadSelectionWindowHeight
	 */
	public void setDownloadSelectionWindowHeight(int downloadSelectionWindowHeight) {
		this.downloadSelectionWindowHeight = downloadSelectionWindowHeight;
	}

	/**
	 * Returns the downloadSelectionWindowXPos
	 * 
	 * @return downloadSelectionWindowXPos
	 */
	public int getDownloadSelectionWindowXPos() {
		return downloadSelectionWindowXPos;
	}

	/**
	 * Sets the downloadSelectionWindowXPos
	 * 
	 * @param downloadSelectionWindowXPos downloadSelectionWindowXPos
	 */
	public void setDownloadSelectionWindowXPos(int downloadSelectionWindowXPos) {
		this.downloadSelectionWindowXPos = downloadSelectionWindowXPos;
	}

	/**
	 * Returns the downloadSelectionWindowYPos
	 * 
	 * @return downloadSelectionWindowYPos
	 */
	public int getDownloadSelectionWindowYPos() {
		return downloadSelectionWindowYPos;
	}

	/**
	 * Sets the downloadSelectionWindowYPos
	 * 
	 * @param downloadSelectionWindowYPos downloadSelectionWindowYPos
	 */
	public void setDownloadSelectionWindowYPos(int downloadSelectionWindowYPos) {
		this.downloadSelectionWindowYPos = downloadSelectionWindowYPos;
	}

	/**
	 * Returns the saveDownloadSelectionWindowSizePosition
	 * 
	 * @return saveDownloadSelectionWindowSizePosition
	 */
	public boolean isSaveDownloadSelectionWindowSizePosition() {
		return saveDownloadSelectionWindowSizePosition;
	}

	/**
	 * Sets the saveDownloadSelectionWindowSizePosition
	 * 
	 * @param saveDownloadSelectionWindowSizePosition saveDownloadSelectionWindowSizePosition
	 */
	public void setSaveDownloadSelectionWindowSizePosition(boolean saveDownloadSelectionWindowSizePosition) {
		this.saveDownloadSelectionWindowSizePosition = saveDownloadSelectionWindowSizePosition;
	}

	/**
	 * @return the saveLastPath
	 */
	public boolean isSaveLastPath() {
		return saveLastPath;
	}

	/**
	 * @param saveLastPath the saveLastPath to set
	 */
	public void setSaveLastPath(boolean saveLastPath) {
		this.saveLastPath = saveLastPath;
	}

	/**
	 * @return the alwaysAddTitle
	 */
	public boolean isAlwaysAddTitle() {
		return alwaysAddTitle;
	}

	/**
	 * @param alwaysAddTitle the alwaysAddTitle to set
	 */
	public void setAlwaysAddTitle(boolean alwaysAddTitle) {
		this.alwaysAddTitle = alwaysAddTitle;
	}

	/**
	 * @return DeselectNoKeyword
	 */
	public boolean isDeselectNoKeyword() {
		return deselectNoKeyword;
	}

	/**
	 * @param deselectNoKeyword DeselectNoKeyword
	 */
	public void setDeselectNoKeyword(boolean deselectNoKeyword) {
		this.deselectNoKeyword = deselectNoKeyword;
	}

	/**
	 * Get-Method
	 * 
	 * @return directoryLogFilterEnabled DirectoryLogFilterEnabled
	 */
	public boolean isDirectoryLogFilterEnabled() {
		return directoryLogFilterEnabled;
	}

	/**
	 * @param directoryLogFilterEnabled DirectoryLogFilterEnabled
	 */
	public void setDirectoryLogFilterEnabled(boolean directoryLogFilterEnabled) {
		this.directoryLogFilterEnabled = directoryLogFilterEnabled;
	}

	/**
	 * Get-Method
	 * 
	 * @return KeywordFilterEnabled
	 */
	public boolean isKeywordFilterEnabled() {
		return keywordFilterEnabled;
	}

	/**
	 * @param keywordFilterEnabled KeywordFilterEnabled
	 */
	public void setKeywordFilterEnabled(boolean keywordFilterEnabled) {
		this.keywordFilterEnabled = keywordFilterEnabled;
	}

	/**
	 * Get-Method
	 * 
	 * @return AdderAdd
	 */
	public List<String> getAdderAdd() {
		return adderAdd;
	}

	/**
	 * Set-Method
	 * 
	 * @param adderAdd AdderAdd
	 */
	public void setAdderAdd(List<String> adderAdd) {
		this.adderAdd = adderAdd;
	}

	/**
	 * Add-Method
	 * 
	 * @param adderAdd AdderAdd
	 */
	public synchronized void addAdderAdd(String adderAdd) {
		if (this.adderAdd.contains(adderAdd) == false) {
			this.adderAdd.add(adderAdd);
		}
	}

	/**
	 * Get-Method
	 * 
	 * @return TargetDirChangeHistory
	 */
	public List<String> getTargetDirChangeHistory() {
		return targetDirChangeHistory;
	}

	/**
	 * Set-Method
	 * 
	 * @param targetDirChangeHistory TargetDirChangeHistory
	 */
	public void setTargetDirChangeHistory(List<String> targetDirChangeHistory) {
		this.targetDirChangeHistory = targetDirChangeHistory;
	}

	/**
	 * Add-Method
	 * 
	 * @param targetDirChangeHistory TargetDirChangeHistory
	 */
	public synchronized void addTargetDirChangeHistory(String targetDirChangeHistory) {
		if (this.targetDirChangeHistory.contains(targetDirChangeHistory) == false) {
			this.targetDirChangeHistory.add(targetDirChangeHistory);
		}
	}

	/**
	 * Get-Method
	 * 
	 * @return FilenameChangeHistory
	 */
	public List<String> getFilenameChangeHistory() {
		return filenameChangeHistory;
	}

	/**
	 * Set-Method
	 * 
	 * @param filenameChangeHistory FilenameChangeHistory
	 */
	public void setFilenameChangeHistory(List<String> filenameChangeHistory) {
		this.filenameChangeHistory = filenameChangeHistory;
	}

	/**
	 * Add-Method
	 * 
	 * @param filenameChangeHistory FilenameChangeHistory
	 */
	public synchronized void addFilenameChangeHistory(String filenameChangeHistory) {
		if (this.filenameChangeHistory.contains(filenameChangeHistory) == false) {
			this.filenameChangeHistory.add(filenameChangeHistory);
		}
	}

	/**
	 * @return isRulesBeforeClasses
	 */
	public boolean isRulesBeforeClasses() {
		return rulesBeforeClasses;
	}

	/**
	 * @param rulesBeforeClasses isRulesBeforeClasses
	 */
	public void setRulesBeforeClasses(boolean rulesBeforeClasses) {
		this.rulesBeforeClasses = rulesBeforeClasses;
	}

	/**
	 * @return the cookiesFromBrowser
	 */
	public int getCookiesFromBrowser() {
		return cookiesFromBrowser;
	}

	/**
	 * @param cookiesFromBrowser the cookiesFromBrowser to set
	 */
	public void setCookiesFromBrowser(int cookiesFromBrowser) {
		if (checkIntValue(0, 5, cookiesFromBrowser)) {
			this.cookiesFromBrowser = cookiesFromBrowser;
		}
	}

	/**
	 * @return the deleteNoKeyword
	 */
	public boolean isDeleteNoKeyword() {
		return deleteNoKeyword;
	}

	/**
	 * @param deleteNoKeyword the deleteNoKeyword to set
	 */
	public void setDeleteNoKeyword(boolean deleteNoKeyword) {
		this.deleteNoKeyword = deleteNoKeyword;
	}

	/**
	 * @return the allowedFilenameChars
	 */
	public int getAllowedFilenameChars() {
		return allowedFilenameChars;
	}

	/**
	 * @param allowedFilenameChars the allowedFilenameChars to set
	 */
	public void setAllowedFilenameChars(int allowedFilenameChars) {
		if ((allowedFilenameChars == SettingsManager.FILENAME_ASCII_ONLY) || (allowedFilenameChars == SettingsManager.FILENAME_ASCII_UMLAUT)
				|| (allowedFilenameChars == SettingsManager.FILENAME_ALL)) {
			this.allowedFilenameChars = allowedFilenameChars;
		}
	}

	/**
	 * @return the maxFailedCount
	 */
	public int getMaxFailedCount() {
		return maxFailedCount;
	}

	/**
	 * @param maxFailedCount the maxFailedCount to set
	 */
	public void setMaxFailedCount(int maxFailedCount) {
		this.maxFailedCount = maxFailedCount;
	}

	/**
	 * @return the appendPrefixFilenameChange
	 */
	public boolean isAppendPrefixFilenameChange() {
		return appendPrefixFilenameChange;
	}

	/**
	 * @param appendPrefixFilenameChange the appendPrefixFilenameChange to set
	 */
	public void setAppendPrefixFilenameChange(boolean appendPrefixFilenameChange) {
		this.appendPrefixFilenameChange = appendPrefixFilenameChange;
	}

	/**
	 * @return the appendAppendixFilenameChange
	 */
	public boolean isAppendAppendixFilenameChange() {
		return appendAppendixFilenameChange;
	}

	/**
	 * @param appendAppendixFilenameChange the appendAppendixFilenameChange to set
	 */
	public void setAppendAppendixFilenameChange(boolean appendAppendixFilenameChange) {
		this.appendAppendixFilenameChange = appendAppendixFilenameChange;
	}

	/**
	 * @return the filenameChangePrefix
	 */
	public String getFilenameChangePrefix() {
		return filenameChangePrefix;
	}

	/**
	 * @param filenameChangePrefix the filenameChangePrefix to set
	 */
	public void setFilenameChangePrefix(String filenameChangePrefix) {
		this.filenameChangePrefix = filenameChangePrefix;
	}

	/**
	 * @return the filenameChangeAppendix
	 */
	public String getFilenameChangeAppendix() {
		return filenameChangeAppendix;
	}

	/**
	 * @param filenameChangeAppendix the filenameChangeAppendix to set
	 */
	public void setFilenameChangeAppendix(String filenameChangeAppendix) {
		this.filenameChangeAppendix = filenameChangeAppendix;
	}

	/**
	 * @return the minFilesize
	 */
	public int getMinFilesize() {
		return minFilesize;
	}

	/**
	 * @param minFilesize the minFilesize to set
	 */
	public void setMinFilesize(int minFilesize) {
		this.minFilesize = minFilesize;
	}

	/**
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return the backupDbOnStart
	 */
	public boolean isBackupDbOnStart() {
		return backupDbOnStart;
	}

	/**
	 * @param backupDbOnStart the backupDbOnStart to set
	 */
	public void setBackupDbOnStart(boolean backupDbOnStart) {
		this.backupDbOnStart = backupDbOnStart;
	}

	/**
	 * @return the defragDBOnStart
	 */
	public boolean isDefragDBOnStart() {
		return defragDBOnStart;
	}

	/**
	 * @param defragDBOnStart the defragDBOnStart to set
	 */
	public void setDefragDBOnStart(boolean defragDBOnStart) {
		this.defragDBOnStart = defragDBOnStart;
	}

	/**
	 * @return the defragMinFilesize
	 */
	public long getDefragMinFilesize() {
		return defragMinFilesize;
	}

	/**
	 * @param defragMinFilesize the defragMinFilesize to set
	 */
	public void setDefragMinFilesize(long defragMinFilesize) {
		this.defragMinFilesize = defragMinFilesize;
	}

	/**
	 * @return the cookieFileOperaFixed
	 */
	public boolean isCookieFileOperaFixed() {
		return cookieFileOperaFixed;
	}

	/**
	 * @param cookieFileOperaFixed the cookieFileOperaFixed to set
	 */
	public void setCookieFileOperaFixed(boolean cookieFileOperaFixed) {
		this.cookieFileOperaFixed = cookieFileOperaFixed;
	}

	/**
	 * @return the cookieFileOpera
	 */
	public String getCookieFileOpera() {
		return cookieFileOpera;
	}

	/**
	 * @param cookieFileOpera the cookieFileOpera to set
	 */
	public void setCookieFileOpera(String cookieFileOpera) {
		this.cookieFileOpera = cookieFileOpera;
	}

	/**
	 * @return the cookieFileOperaFixed
	 */
	public boolean isCookieFileOperaNewFixed() {
		return cookieFileOperaNewFixed;
	}

	/**
	 * @param cookieFileOperaNewFixed the cookieFileOperaFixed to set
	 */
	public void setCookieFileOperaNewFixed(boolean cookieFileOperaNewFixed) {
		this.cookieFileOperaNewFixed = cookieFileOperaNewFixed;
	}

	/**
	 * @return the cookieFileOpera
	 */
	public String getCookieFileOperaNew() {
		return cookieFileOperaNew;
	}

	/**
	 * @param cookieFileOperaNew the cookieFileOpera to set
	 */
	public void setCookieFileOperaNew(String cookieFileOperaNew) {
		this.cookieFileOperaNew = cookieFileOperaNew;
	}

	/**
	 * @return the cookieFileFirefoxFixed
	 */
	public boolean isCookieFileFirefoxFixed() {
		return cookieFileFirefoxFixed;
	}

	/**
	 * @param cookieFileFirefoxFixed the cookieFileFirefoxFixed to set
	 */
	public void setCookieFileFirefoxFixed(boolean cookieFileFirefoxFixed) {
		this.cookieFileFirefoxFixed = cookieFileFirefoxFixed;
	}

	/**
	 * @return the cookieFileFirefox
	 */
	public String getCookieFileFirefox() {
		return cookieFileFirefox;
	}

	/**
	 * @param cookieFileFirefox the cookieFileFirefox to set
	 */
	public void setCookieFileFirefox(String cookieFileFirefox) {
		this.cookieFileFirefox = cookieFileFirefox;
	}

	/**
	 * @return the cookieFilePaleMoonFixed
	 */
	public boolean isCookieFilePaleMoonFixed() {
		return cookieFilePaleMoonFixed;
	}

	/**
	 * @param cookieFilePaleMoonFixed the cookieFilePaleMoonFixed to set
	 */
	public void setCookieFilePaleMoonFixed(boolean cookieFilePaleMoonFixed) {
		this.cookieFilePaleMoonFixed = cookieFilePaleMoonFixed;
	}

	/**
	 * @return the cookieFilePaleMoon
	 */
	public String getCookieFilePaleMoon() {
		return cookieFilePaleMoon;
	}

	/**
	 * @param cookieFilePaleMoon the cookieFilePaleMoon to set
	 */
	public void setCookieFilePaleMoon(String cookieFilePaleMoon) {
		this.cookieFilePaleMoon = cookieFilePaleMoon;
	}

	/**
	 * @return the subdirsEnabled
	 */
	public boolean isSubdirsEnabled() {
		return subdirsEnabled;
	}

	/**
	 * @param subdirsEnabled the subdirsEnabled to set
	 */
	public void setSubdirsEnabled(boolean subdirsEnabled) {
		this.subdirsEnabled = subdirsEnabled;
	}

	/**
	 * @return the downloadRate
	 */
	public boolean isDownloadRate() {
		return downloadRate;
	}

	/**
	 * @param downloadRate the downloadRate to set
	 */
	public void setDownloadRate(boolean downloadRate) {
		this.downloadRate = downloadRate;
	}

	/**
	 * Returns the debugLevel
	 * Available Levels:
	 * Off
	 * All
	 * Info
	 * Trace
	 * Debug
	 * Warn
	 * Error
	 * Fatal
	 * 
	 * @return debugLevel
	 */
	public String getDebugLevel() {
		return debugLevel;
	}

	/**
	 * Sets the debugLevel
	 * Available Levels:
	 * Off
	 * All
	 * Info
	 * Trace
	 * Debug
	 * Warn
	 * Error
	 * Fatal
	 * 
	 * @param debugLevel debugLevel
	 */
	public void setDebugLevel(String debugLevel) {
		synchronized (this) {
			if (checkDebugLevel(debugLevel)) {
				this.debugLevel = debugLevel;
				BHUtil.changeLog4JRootLoggerLevel(getDebugLevelForString(this.debugLevel));
			}
		}
	}

	/**
	 * Returns the colWidthsQueue
	 * 
	 * @return colWidthsQueue
	 */
	public String getColWidthsQueue() {
		return colWidthsQueue;
	}

	/**
	 * Sets the colWidthsQueue
	 * 
	 * @param colWidthsQueue colWidthsQueue
	 */
	public void setColWidthsQueue(String colWidthsQueue) {
		this.colWidthsQueue = colWidthsQueue;
	}

	/**
	 * Returns the colWidthsLog
	 * 
	 * @return colWidthsLog
	 */
	public String getColWidthsLog() {
		return colWidthsLog;
	}

	/**
	 * Sets the colWidthsLog
	 * 
	 * @param colWidthsLog colWidthsLog
	 */
	public void setColWidthsLog(String colWidthsLog) {
		this.colWidthsLog = colWidthsLog;
	}

	/**
	 * Returns the colWidthsKeywords
	 * 
	 * @return colWidthsKeywords
	 */
	public String getColWidthsKeywords() {
		return colWidthsKeywords;
	}

	/**
	 * Sets the colWidthsKeywords
	 * 
	 * @param colWidthsKeywords colWidthsKeywords
	 */
	public void setColWidthsKeywords(String colWidthsKeywords) {
		this.colWidthsKeywords = colWidthsKeywords;
	}

	/**
	 * Returns the colWidthsRules
	 * 
	 * @return colWidthsRules
	 */
	public String getColWidthsRules() {
		return colWidthsRules;
	}

	/**
	 * Sets the colWidthsRules
	 * 
	 * @param colWidthsRules colWidthsRules
	 */
	public void setColWidthsRules(String colWidthsRules) {
		this.colWidthsRules = colWidthsRules;
	}

	/**
	 * Returns the colWidthsRulesEditor
	 * 
	 * @return colWidthsRulesEditor
	 */
	public String getColWidthsRulesEditor() {
		return colWidthsRulesEditor;
	}

	/**
	 * Sets the colWidthsRulesEditor
	 * 
	 * @param colWidthsRulesEditor colWidthsRulesEditor
	 */
	public void setColWidthsRulesEditor(String colWidthsRulesEditor) {
		this.colWidthsRulesEditor = colWidthsRulesEditor;
	}

	/**
	 * Returns the colWidthsHosts
	 * 
	 * @return colWidthsHosts
	 */
	public String getColWidthsHosts() {
		return colWidthsHosts;
	}

	/**
	 * Sets the colWidthsHosts
	 * 
	 * @param colWidthsHosts colWidthsHosts
	 */
	public void setColWidthsHosts(String colWidthsHosts) {
		this.colWidthsHosts = colWidthsHosts;
	}

	/**
	 * Returns the colWidthsSubdirs
	 * 
	 * @return colWidthsSubdirs
	 */
	public String getColWidthsSubdirs() {
		return colWidthsSubdirs;
	}

	/**
	 * Sets the colWidthsSubdirs
	 * 
	 * @param colWidthsSubdirs colWidthsSubdirs
	 */
	public void setColWidthsSubdirs(String colWidthsSubdirs) {
		this.colWidthsSubdirs = colWidthsSubdirs;
	}

	/**
	 * Returns the colWidthsUpdate
	 * 
	 * @return colWidthsUpdate
	 */
	public String getColWidthsUpdate() {
		return colWidthsUpdate;
	}

	/**
	 * Sets the colWidthsUpdate
	 * 
	 * @param colWidthsUpdate colWidthsUpdate
	 */
	public void setColWidthsUpdate(String colWidthsUpdate) {
		this.colWidthsUpdate = colWidthsUpdate;
	}

	/**
	 * Returns the colWidthsAdder
	 * 
	 * @return colWidthsAdder
	 */
	public String getColWidthsAdder() {
		return colWidthsAdder;
	}

	/**
	 * Sets the colWidthsAdder
	 * 
	 * @param colWidthsAdder colWidthsAdder
	 */
	public void setColWidthsAdder(String colWidthsAdder) {
		this.colWidthsAdder = colWidthsAdder;
	}

	/**
	 * Returns the colWidthsAdderKeywordSelectorFilename
	 * 
	 * @return colWidthsAdderKeywordSelectorFilename
	 */
	public String getColWidthsAdderKeywordSelectorFilename() {
		return colWidthsAdderKeywordSelectorFilename;
	}

	/**
	 * Sets the colWidthsAdderKeywordSelectorFilename
	 * 
	 * @param colWidthsAdderKeywordSelectorFilename colWidthsAdderKeywordSelectorFilename
	 */
	public void setColWidthsAdderKeywordSelectorFilename(String colWidthsAdderKeywordSelectorFilename) {
		this.colWidthsAdderKeywordSelectorFilename = colWidthsAdderKeywordSelectorFilename;
	}

	/**
	 * Returns the saveTableColumnSizes
	 * 
	 * @return saveTableColumnSizes
	 */
	public boolean isSaveTableColumnSizes() {
		return saveTableColumnSizes;
	}

	/**
	 * Sets the saveTableColumnSizes
	 * 
	 * @param saveTableColumnSizes saveTableColumnSizes
	 */
	public void setSaveTableColumnSizes(boolean saveTableColumnSizes) {
		this.saveTableColumnSizes = saveTableColumnSizes;
	}

	/**
	 * Returns the tableSortOrdersKeywords
	 * 
	 * @return tableSortOrdersKeywords
	 */
	public String getTableSortOrdersKeywords() {
		return tableSortOrdersKeywords;
	}

	/**
	 * Sets the tableSortOrdersKeywords
	 * 
	 * @param tableSortOrdersKeywords tableSortOrdersKeywords
	 */
	public void setTableSortOrdersKeywords(String tableSortOrdersKeywords) {
		this.tableSortOrdersKeywords = tableSortOrdersKeywords;
	}

	/**
	 * Returns the saveTableSortOrders
	 * 
	 * @return saveTableSortOrders
	 */
	public boolean isSaveTableSortOrders() {
		return saveTableSortOrders;
	}

	/**
	 * Sets the saveTableSortOrders
	 * 
	 * @param saveTableSortOrders saveTableSortOrders
	 */
	public void setSaveTableSortOrders(boolean saveTableSortOrders) {
		this.saveTableSortOrders = saveTableSortOrders;
	}

	/**
	 * Returns the sortDownloadsOnStart
	 * 
	 * @return sortDownloadsOnStart
	 */
	public int getSortDownloadsOnStart() {
		return sortDownloadsOnStart;
	}

	/**
	 * Sets the sortDownloadsOnStart
	 * 
	 * @param sortDownloadsOnStart sortDownloadsOnStart
	 */
	public void setSortDownloadsOnStart(int sortDownloadsOnStart) {
		this.sortDownloadsOnStart = sortDownloadsOnStart;
	}

	/**
	 * Returns the downloadsCompleteNotification
	 * 
	 * @return downloadsCompleteNotification
	 */
	public boolean isDownloadsCompleteNotification() {
		return downloadsCompleteNotification;
	}

	/**
	 * Sets the downloadsCompleteNotification
	 * 
	 * @param downloadsCompleteNotification downloadsCompleteNotification
	 */
	public void setDownloadsCompleteNotification(boolean downloadsCompleteNotification) {
		this.downloadsCompleteNotification = downloadsCompleteNotification;
	}

	/**
	 * Returns the filenameChangeKeepOriginal
	 * 
	 * @return filenameChangeKeepOriginal
	 */
	public boolean isFilenameChangeKeepOriginal() {
		return filenameChangeKeepOriginal;
	}

	/**
	 * Sets the filenameChangeKeepOriginal
	 * 
	 * @param filenameChangeKeepOriginal filenameChangeKeepOriginal
	 */
	public void setFilenameChangeKeepOriginal(boolean filenameChangeKeepOriginal) {
		this.filenameChangeKeepOriginal = filenameChangeKeepOriginal;
	}

	/**
	 * Returns the useOldDownloadLogMode
	 * 
	 * @return useOldDownloadLogMode
	 */
	public boolean isUseOldDownloadLogMode() {
		return useOldDownloadLogMode;
	}

	/**
	 * Sets the useOldDownloadLogMode
	 * 
	 * @param useOldDownloadLogMode useOldDownloadLogMode
	 */
	public void setUseOldDownloadLogMode(boolean useOldDownloadLogMode) {
		this.useOldDownloadLogMode = useOldDownloadLogMode;
	}

	/**
	 * Returns the subdirsResolutionMode
	 * 
	 * @return subdirsResolutionMode
	 */
	public int getSubdirsResolutionMode() {
		return subdirsResolutionMode;
	}

	/**
	 * Sets the subdirsResolutionMode
	 * 
	 * @param subdirsResolutionMode subdirsResolutionMode
	 */
	public void setSubdirsResolutionMode(int subdirsResolutionMode) {
		if (checkIntValue(0, 4, subdirsResolutionMode)) {
			this.subdirsResolutionMode = subdirsResolutionMode;
		}
	}

	/**
	 * Returns the autoRetryAfterDownloadsComplete
	 * 
	 * @return autoRetryAfterDownloadsComplete
	 */
	public boolean isAutoRetryAfterDownloadsComplete() {
		return autoRetryAfterDownloadsComplete;
	}

	/**
	 * Sets the autoRetryAfterDownloadsComplete
	 * 
	 * @param autoRetryAfterDownloadsComplete autoRetryAfterDownloadsComplete
	 */
	public void setAutoRetryAfterDownloadsComplete(boolean autoRetryAfterDownloadsComplete) {
		this.autoRetryAfterDownloadsComplete = autoRetryAfterDownloadsComplete;
	}

	/**
	 * Returns the currentDownloadLogFile
	 * 
	 * @return currentDownloadLogFile
	 */
	public String getCurrentDownloadLogFile() {
		return currentDownloadLogFile;
	}

	/**
	 * Sets the currentDownloadLogFile
	 * 
	 * @param currentDownloadLogFile currentDownloadLogFile
	 */
	public void setCurrentDownloadLogFile(String currentDownloadLogFile) {
		this.currentDownloadLogFile = currentDownloadLogFile;
		settingsChanged();
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
	 * Returns the regexReplacePipelineFilename
	 * 
	 * @return regexReplacePipelineFilename
	 */
	public RegexReplacePipeline getRegexReplacePipelineFilename() {
		return regexReplacePipelineFilename;
	}

	/**
	 * Returns the userAgent
	 * 
	 * @return userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * Sets the userAgent
	 * 
	 * @param userAgent userAgent
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * Returns the directoryLogDirCount
	 * 
	 * @return directoryLogDirCount
	 */
	public int getDirectoryLogDirCount() {
		return directoryLogDirCount;
	}

	/**
	 * Sets the directoryLogDirCount
	 * 
	 * @param directoryLogDirCount directoryLogDirCount
	 */
	public void setDirectoryLogDirCount(int directoryLogDirCount) {
		this.directoryLogDirCount = directoryLogDirCount;
	}

	/**
	 * Returns the directoryLogOnlyExisting
	 * 
	 * @return directoryLogOnlyExisting
	 */
	public boolean isDirectoryLogOnlyExisting() {
		return directoryLogOnlyExisting;
	}

	/**
	 * Sets the directoryLogOnlyExisting
	 * 
	 * @param directoryLogOnlyExisting directoryLogOnlyExisting
	 */
	public void setDirectoryLogOnlyExisting(boolean directoryLogOnlyExisting) {
		this.directoryLogOnlyExisting = directoryLogOnlyExisting;
	}

	/**
	 * Returns the connectionsPerHost
	 * 
	 * @return connectionsPerHost
	 */
	public int getConnectionsPerHost() {
		return connectionsPerHost;
	}

	/**
	 * Sets the connectionsPerHost
	 * 
	 * @param connectionsPerHost connectionsPerHost
	 */
	public void setConnectionsPerHost(int connectionsPerHost) {
		if (connectionsPerHost < 0) {
			this.connectionsPerHost = 0;
		} else if (connectionsPerHost > 50) {
			this.connectionsPerHost = 50;
		} else {
			this.connectionsPerHost = connectionsPerHost;
		}
		settingsChanged();
	}

	/**
	 * Returns the lastUsedImportDialogPath
	 * 
	 * @return lastUsedImportDialogPath
	 */
	public String getLastUsedImportDialogPath() {
		return lastUsedImportDialogPath;
	}

	/**
	 * Sets the lastUsedImportDialogPath
	 * 
	 * @param lastUsedImportDialogPath lastUsedImportDialogPath
	 */
	public void setLastUsedImportDialogPath(String lastUsedImportDialogPath) {
		this.lastUsedImportDialogPath = lastUsedImportDialogPath;
		this.writeSettings(true);
	}

	/**
	 * Returns the lastUsedExportDialogPath
	 * 
	 * @return lastUsedExportDialogPath
	 */
	public String getLastUsedExportDialogPath() {
		return lastUsedExportDialogPath;
	}

	/**
	 * Sets the lastUsedExportDialogPath
	 * 
	 * @param lastUsedExportDialogPath lastUsedExportDialogPath
	 */
	public void setLastUsedExportDialogPath(String lastUsedExportDialogPath) {
		this.lastUsedExportDialogPath = lastUsedExportDialogPath;
		this.writeSettings(true);
	}

	/**
	 * Returns the threadCount
	 * 
	 * @return threadCount
	 */
	public int getThreadCount() {
		return threadCount;
	}

	/**
	 * Sets the threadCount
	 * 
	 * @param threadCount threadCount
	 */
	public void setThreadCount(int threadCount) {
		if ((threadCount > 0) && (threadCount <= Runtime.getRuntime().availableProcessors())) {
			this.threadCount = threadCount;
		}
	}

	/**
	 * @param name Name
	 * @param deactivated Deactivated
	 */
	public void setHostDeactivated(String name, boolean deactivated) {
		this.deactivatedHosts.put(name, deactivated);
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
	 * Returns the downloadPreviews
	 * 
	 * @return downloadPreviews
	 */
	public boolean isDownloadPreviews() {
		return downloadPreviews;
	}

	/**
	 * Sets the downloadPreviews
	 * 
	 * @param downloadPreviews downloadPreviews
	 */
	public void setDownloadPreviews(boolean downloadPreviews) {
		this.downloadPreviews = downloadPreviews;
	}

	/**
	 * Returns the previewSize
	 * 
	 * @return previewSize
	 */
	public int getPreviewSize() {
		return previewSize;
	}

	/**
	 * Sets the previewSize
	 * 
	 * @param previewSize previewSize
	 */
	public void setPreviewSize(int previewSize) {
		if (previewSize >= 100 && previewSize <= 1000) {
			this.previewSize = previewSize;
		}
	}
}

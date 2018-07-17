package ch.supertomcat.bh.gui.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.gui.Main;
import ch.supertomcat.bh.gui.SpringUtilities;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ISettingsListener;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.options.Subdir;
import ch.supertomcat.supertomcattools.cookietools.BrowserCookies;
import ch.supertomcat.supertomcattools.fileiotools.FileTool;
import ch.supertomcat.supertomcattools.guitools.FileDialogTool;
import ch.supertomcat.supertomcattools.guitools.GridBagLayoutTool;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.TableTool;
import ch.supertomcat.supertomcattools.guitools.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcattools.guitools.tablerenderer.DefaultNumberColorRowRenderer;
import ch.supertomcat.supertomcattools.guitools.tablerenderer.DefaultStringColorRowRenderer;

/**
 * Settings-Panel
 */
public class Settings extends JDialog implements ActionListener, ItemListener, ChangeListener, ISettingsListener, MouseListener, TableColumnModelListener, WindowListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -2682713963632393292L;

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(Settings.class);

	/**
	 * TabbedPane
	 */
	private JTabbedPane tp = new JTabbedPane();

	/**
	 * Scrollpane
	 */
	private JScrollPane spPaths;

	/**
	 * Scrollpane
	 */
	private JScrollPane spConnection;

	/**
	 * Scrollpane
	 */
	private JScrollPane spGUI;

	/**
	 * Scrollpane
	 */
	private JScrollPane spKeywords;

	/**
	 * Scrollpane
	 */
	private JScrollPane spDownload;

	/**
	 * Scrollpane
	 */
	private JScrollPane spOther;

	/**
	 * Panel
	 */
	private JPanel pnlPaths = new JPanel();

	/**
	 * Panel
	 */
	private JPanel pnlConnection = new JPanel();

	/**
	 * Panel
	 */
	private JPanel pnlGUI = new JPanel();

	/**
	 * Panel
	 */
	private JPanel pnlKeywords = new JPanel();

	/**
	 * Panel
	 */
	private JPanel pnlDownload = new JPanel();

	/**
	 * Panel
	 */
	private JPanel pnlOther = new JPanel();

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * Label
	 */
	private JLabel lblStdSavePath = new JLabel(Localization.getString("StdSavePath"));

	/**
	 * TextField
	 */
	private JTextField txtStdSavePath = new JTextField(SettingsManager.instance().getSavePath());

	/**
	 * Button
	 */
	private JButton btnStdSavePath = new JButton("...");

	/**
	 * Label
	 */
	private JLabel lblRememberLastUsedPath = new JLabel(Localization.getString("StdSavePath"));

	/**
	 * Checkbox
	 */
	private JCheckBox chkRememberLastUsedPath = new JCheckBox(Localization.getString("RememberLastUsedPath"), false);

	/**
	 * Label
	 */
	private JLabel lblConnectionCount = new JLabel(Localization.getString("ConnectionCount"));

	/**
	 * TextField
	 */
	private JTextField txtConnectionCount = new JTextField("", 3);

	/**
	 * Slider
	 */
	private JSlider sldConnectionCount = new JSlider();

	/**
	 * Label
	 */
	private JLabel lblConnectionCountPerHost = new JLabel(Localization.getString("ConnectionCountPerHost"));

	/**
	 * TextField
	 */
	private JTextField txtConnectionCountPerHost = new JTextField("", 3);

	/**
	 * Slider
	 */
	private JSlider sldConnectionCountPerHost = new JSlider();

	/**
	 * Panel
	 */
	private JPanel pnlLogDays = new JPanel();

	/**
	 * Label
	 */
	private JLabel lblUpdates = new JLabel(Localization.getString("Updates"));

	/**
	 * Checkbox
	 */
	private JCheckBox chkUpdates = new JCheckBox(Localization.getString("CheckForUpdates"), false);

	/**
	 * Label
	 */
	private JLabel lblSaveLogs = new JLabel(Localization.getString("Logs"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkSaveLogs = new JCheckBox(Localization.getString("SaveLogs"), false);

	/**
	 * Label
	 */
	private JLabel lblAutoStartDownloads = new JLabel(Localization.getString("Downloads"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkAutoStartDownloads = new JCheckBox(Localization.getString("AutoStartDownloads"), false);

	/**
	 * Label
	 */
	private JLabel lblCheckClipboard = new JLabel(Localization.getString("Clipboard"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkCheckClipboard = new JCheckBox(Localization.getString("CheckClipboard"), false);

	/**
	 * Label
	 */
	private JLabel lblWebExtensionPort = new JLabel(Localization.getString("WebExtensionPort"));

	/**
	 * TextFields
	 */
	private JTextField txtWebExtensionPort = new JTextField("35990", 20);

	/**
	 * Label
	 */
	private JLabel lblLanguage = new JLabel(Localization.getString("Language"));

	/**
	 * ComboBox
	 */
	private JComboBox<String> cmbLanguage = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblSizeView = new JLabel(Localization.getString("SizeView"));

	/**
	 * ComboBox
	 */
	private JComboBox<String> cmbSizeView = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblProgressView = new JLabel(Localization.getString("ProgressView"));

	/**
	 * ComboBox
	 */
	private JComboBox<String> cmbProgressView = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblDownloadRate = new JLabel(Localization.getString("ProgressView"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkDownloadRate = new JCheckBox(Localization.getString("DownloadRate"));

	/**
	 * CheckBox
	 */
	private JCheckBox cbAuth = new JCheckBox(Localization.getString("ProxyAuthentication"), false);

	/**
	 * Label
	 */
	private JLabel lblProxyName = new JLabel(Localization.getString("ProxyName"));

	/**
	 * TextField
	 */
	private JTextField txtProxyName = new JTextField("127.0.0.1", 20);

	/**
	 * Label
	 */
	private JLabel lblProxyPort = new JLabel(Localization.getString("ProxyPort"));

	/**
	 * TextField
	 */
	private JTextField txtProxyPort = new JTextField("80", 20);

	/**
	 * Label
	 */
	private JLabel lblProxyUser = new JLabel(Localization.getString("ProxyUser"));

	/**
	 * TextField
	 */
	private JTextField txtProxyUser = new JTextField("", 20);

	/**
	 * Label
	 */
	private JLabel lblProxyPassword = new JLabel(Localization.getString("ProxyPassword"));

	/**
	 * TextField
	 */
	private JPasswordField txtProxyPassword = new JPasswordField("", 20);

	/**
	 * Label
	 */
	private JLabel lblCookies = new JLabel(Localization.getString("Cookies"));

	/**
	 * ComboBox
	 */
	private JComboBox<String> cmbCookies = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblCookiesOpera = new JLabel(Localization.getString("CookiesOpera"));

	/**
	 * Panel
	 */
	private JPanel pnlCookiesOpera = new JPanel(new BorderLayout());

	/**
	 * Checkbox
	 */
	private JCheckBox cbCookiesOperaFixed = new JCheckBox();

	/**
	 * TextField
	 */
	private JTextField txtCookiesOpera = new JTextField(20);

	/**
	 * Button
	 */
	private JButton btnCookiesOpera = new JButton("...");

	/**
	 * Label
	 */
	private JLabel lblCookiesOperaNew = new JLabel(Localization.getString("CookiesOperaNew"));

	/**
	 * Panel
	 */
	private JPanel pnlCookiesOperaNew = new JPanel(new BorderLayout());

	/**
	 * Checkbox
	 */
	private JCheckBox cbCookiesOperaNewFixed = new JCheckBox();

	/**
	 * TextField
	 */
	private JTextField txtCookiesOperaNew = new JTextField(20);

	/**
	 * Button
	 */
	private JButton btnCookiesOperaNew = new JButton("...");

	/**
	 * Label
	 */
	private JLabel lblCookiesFirefox = new JLabel(Localization.getString("CookiesFirefox"));

	/**
	 * Panel
	 */
	private JPanel pnlCookiesFirefox = new JPanel(new BorderLayout());

	/**
	 * Checkbox
	 */
	private JCheckBox cbCookiesFirefoxFixed = new JCheckBox();

	/**
	 * TextField
	 */
	private JTextField txtCookiesFirefox = new JTextField(20);

	/**
	 * Button
	 */
	private JButton btnCookiesFirefox = new JButton("...");

	/**
	 * Label
	 */
	private JLabel lblCookiesPaleMoon = new JLabel(Localization.getString("CookiesPaleMoon"));

	/**
	 * Panel
	 */
	private JPanel pnlCookiesPaleMoon = new JPanel(new BorderLayout());

	/**
	 * Checkbox
	 */
	private JCheckBox cbCookiesPaleMoonFixed = new JCheckBox();

	/**
	 * TextField
	 */
	private JTextField txtCookiesPaleMoon = new JTextField(20);

	/**
	 * Button
	 */
	private JButton btnCookiesPaleMoon = new JButton("...");

	/**
	 * ButtonGroup
	 */
	private ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * RadioButton
	 */
	private JRadioButton rbNoProxy = new JRadioButton(Localization.getString("ProxyDirectConnection"), true);

	/**
	 * RadioButton
	 */
	private JRadioButton rbHTTP = new JRadioButton(Localization.getString("ProxyHTTP"), false);

	/**
	 * Label
	 */
	private JLabel lblKeywordMatchMode = new JLabel(Localization.getString("KeywordSearch"));

	/**
	 * ComboBox
	 */
	private JComboBox<String> cmbKeywordMatchMode = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblLAF = new JLabel(Localization.getString("LookAndFeel"));

	/**
	 * ComboBox
	 */
	private JComboBox<String> cmbLAF = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblWindowSizePos = new JLabel(Localization.getString("MainWindow"));

	/**
	 * Checkbox
	 */
	private JCheckBox chkWindowSizePos = new JCheckBox(Localization.getString("WindowSizePos"), false);

	/**
	 * Label
	 */
	private JLabel lblDownloadSelectionWindowSizePos = new JLabel(Localization.getString("DownloadSelectionWindow"));

	/**
	 * Checkbox
	 */
	private JCheckBox chkDownloadSelectionWindowSizePos = new JCheckBox(Localization.getString("WindowSizePos"), false);

	/**
	 * Label
	 */
	private JLabel lblAlwaysAddTitle = new JLabel(Localization.getString("TargetFolder"));

	/**
	 * Checkbox
	 */
	private JCheckBox chkAlwaysAddTitle = new JCheckBox(Localization.getString("AlwaysAddTitle"), false);

	/**
	 * Label
	 */
	private JLabel lblDeselectNoKeyword = new JLabel(Localization.getString("KeywordSearch"));

	/**
	 * Checkbox
	 */
	private JCheckBox chkDeselectNoKeyword = new JCheckBox(Localization.getString("DeselectNoKeyword"), false);

	/**
	 * Label
	 */
	private JLabel lblRulesBefore = new JLabel(Localization.getString("Rules"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkRulesBefore = new JCheckBox(Localization.getString("RulesBeforeClasses"), false);

	/**
	 * Label
	 */
	private JLabel lblAllowedFilenameChars = new JLabel(Localization.getString("Filename"));

	/**
	 * ComboBox
	 */
	private JComboBox<String> cmbAllowedFilenameChars = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblDebugLevel = new JLabel("Debug-Level");

	/**
	 * ComboBox
	 */
	private JComboBox<String> cmbDebugLevel = new JComboBox<>();

	/**
	 * Panel
	 */
	private JPanel pnlMaxFailedCount = new JPanel();

	/**
	 * Label
	 */
	private JLabel lblMaxFailedCount = new JLabel(Localization.getString("Downloads"));

	/**
	 * TextField
	 */
	private JTextField txtMaxFailedCount = new JTextField("");

	/**
	 * Button
	 */
	private JButton btnMaxFailedCountPlus = new JButton("", Icons.getTangoIcon("actions/list-add.png", 16));

	/**
	 * Button
	 */
	private JButton btnMaxFailedCountMinus = new JButton("", Icons.getTangoIcon("actions/list-remove.png", 16));

	/**
	 * Label
	 */
	private JLabel lblMinFilesize = new JLabel(Localization.getString("MinFilesize"));

	/**
	 * TextField
	 */
	private JTextField txtMinFilesize = new JTextField("0");

	/**
	 * Label
	 */
	private JLabel lblTimeout = new JLabel(Localization.getString("Timeout"));

	/**
	 * TextField
	 */
	private JTextField txtTimeout = new JTextField("30000");

	/**
	 * Label
	 */
	private JLabel lblDefragDB = new JLabel(Localization.getString("Defrag"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkDefragDB = new JCheckBox(Localization.getString("DefragDB"), false);

	/**
	 * Label
	 */
	private JLabel lblBackupDB = new JLabel(Localization.getString("Backup"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkBackupDB = new JCheckBox(Localization.getString("BackupDB"), false);

	/**
	 * Label
	 */
	private JLabel lblDefragMinFilesize = new JLabel(Localization.getString("DefragMinFilesize"));

	/**
	 * TextField
	 */
	private JTextField txtDefragMinFilesize = new JTextField("5000000");

	/**
	 * Label
	 */
	private JLabel lblSubdirsEnabled = new JLabel(Localization.getString("Subdirs"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkSubdirsEnabled = new JCheckBox(Localization.getString("SubdirsEnabled"), false);

	/**
	 * Label
	 */
	private JLabel lblSubdirsResolutionMode = new JLabel(Localization.getString("SubdirResolutionMode"));

	/**
	 * ComboBox
	 */
	private JComboBox<String> cmbSubdirsResolutionMode = new JComboBox<>();

	/**
	 * TableModel
	 */
	private SubdirsTableModel subdirModel = new SubdirsTableModel();

	/**
	 * Table
	 */
	private JTable jtSubdirs = new JTable(subdirModel);

	/**
	 * Scrollpane
	 */
	private JScrollPane spSubdirs = new JScrollPane(jtSubdirs);

	/**
	 * Panel
	 */
	private JPanel pnlSubdirButtons = new JPanel();

	/**
	 * Button
	 */
	private JButton btnSubdirNew = new JButton(Localization.getString("New"), Icons.getTangoIcon("actions/document-new.png", 16));

	/**
	 * Button
	 */
	private JButton btnSubdirUp = new JButton(Localization.getString("Up"), Icons.getTangoIcon("actions/go-up.png", 16));

	/**
	 * Button
	 */
	private JButton btnSubdirDown = new JButton(Localization.getString("Down"), Icons.getTangoIcon("actions/go-down.png", 16));

	/**
	 * Button
	 */
	private JButton btnSubdirDelete = new JButton(Localization.getString("Delete"), Icons.getTangoIcon("actions/edit-delete.png", 16));

	/**
	 * Button
	 */
	private JButton btnSubdirHelp = new JButton(Localization.getString("Help"), Icons.getTangoIcon("apps/help-browser.png", 16));

	/**
	 * PopupMenu
	 */
	private JPopupMenu popupSubdirs = new JPopupMenu();

	/**
	 * Label
	 */
	private JLabel lblSaveTableColumnSizes = new JLabel(Localization.getString("Tables"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkSaveTableColumnSizes = new JCheckBox(Localization.getString("SaveTableColumnSizes"), false);

	/**
	 * Label
	 */
	private JLabel lblSaveTableSortOrders = new JLabel(Localization.getString("Tables"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkSaveTableSortOrders = new JCheckBox(Localization.getString("SaveTableSortOrders"), false);

	/**
	 * Label
	 */
	private JLabel lblSortDownloadsOnStart = new JLabel(Localization.getString("SortDownloadsOnStart"));

	/**
	 * ComboBox
	 */
	private JComboBox<String> cmbSortDownloadsOnStart = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblDownloadsCompleteNotification = new JLabel(Localization.getString("Notification"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkDownloadsCompleteNotification = new JCheckBox(Localization.getString("DownloadsCompleteNotification"), false);

	/**
	 * Label
	 */
	private JLabel lblDownloadPreviews = new JLabel(Localization.getString("Preview"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkDownloadPreviews = new JCheckBox(Localization.getString("DownloadPreviews"), false);

	/**
	 * Label
	 */
	private JLabel lblAutoRetryAfterDownloadsComplete = new JLabel(Localization.getString("Downloads"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkAutoRetryAfterDownloadsComplete = new JCheckBox(Localization.getString("AutoRetryAfterDownloadsComplete"), false);

	/**
	 * CheckBox
	 */
	private RegexReplacePanel pnlRegexReplacePageTitle = new RegexReplacePanel(SettingsManager.instance().getRegexReplacePipelinePageTitle());

	/**
	 * CheckBox
	 */
	private RegexReplacePanel pnlRegexReplaceFilename = new RegexReplacePanel(SettingsManager.instance().getRegexReplacePipelineFilename());

	/**
	 * Label
	 */
	private JLabel lblUserAgent = new JLabel(Localization.getString("UserAgent"));

	/**
	 * TextField
	 */
	private JTextField txtUserAgent = new JTextField("");

	/**
	 * Label
	 */
	private JLabel lblThreadCount = new JLabel(Localization.getString("ThreadCount"));

	/**
	 * TextField
	 */
	private JTextField txtThreadCount = new JTextField("", 3);

	/**
	 * Slider
	 */
	private JSlider sldThreadCount = new JSlider();

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemNew = new JMenuItem(Localization.getString("New"), Icons.getTangoIcon("actions/document-new.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemDelete = new JMenuItem(Localization.getString("Delete"), Icons.getTangoIcon("actions/edit-delete.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemUp = new JMenuItem(Localization.getString("Up"), Icons.getTangoIcon("actions/go-up.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemDown = new JMenuItem(Localization.getString("Down"), Icons.getTangoIcon("actions/go-down.png", 16));

	/**
	 * Button
	 */
	private JButton btnSave = new JButton(Localization.getString("SaveAndApply"), Icons.getTangoIcon("actions/document-save.png", 16));

	/**
	 * Button
	 */
	private JButton btnReset = new JButton(Localization.getString("Reset"), Icons.getTangoIcon("actions/edit-undo.png", 16));

	/**
	 * Button
	 */
	private JButton btnCancel = new JButton(Localization.getString("Cancel"), Icons.getTangoIcon("emblems/emblem-unreadable.png", 16));

	/**
	 * GridBagLayout
	 */
	private GridBagLayout gbl = new GridBagLayout();

	/**
	 * GridBagLayout
	 */
	private GridBagLayout gblPaths = new GridBagLayout();

	/**
	 * GridBagLayout
	 */
	private GridBagLayout gblConnection = new GridBagLayout();

	/**
	 * GridBagLayout
	 */
	private GridBagLayout gblGUI = new GridBagLayout();

	/**
	 * GridBagLayout
	 */
	private GridBagLayout gblKeywords = new GridBagLayout();

	/**
	 * GridBagLayout
	 */
	private GridBagLayout gblDownload = new GridBagLayout();

	/**
	 * GridBagLayout
	 */
	private GridBagLayout gblOther = new GridBagLayout();

	/**
	 * GridBagLayoutTool
	 */
	private GridBagLayoutTool gblt = new GridBagLayoutTool(5, 10, 5, 5);

	/**
	 * Settingsmanager
	 */
	private SettingsManager sm = SettingsManager.instance();

	/**
	 * ProxyManager
	 */
	private ProxyManager pm = ProxyManager.instance();

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 */
	public Settings(Window owner) {
		setTitle(Localization.getString("Settings"));
		setModal(true);
		setIconImage(Icons.getBHImage("BH.png"));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setLayout(gbl);
		pnlPaths.setLayout(gblPaths);
		pnlConnection.setLayout(gblConnection);
		pnlGUI.setLayout(gblGUI);
		pnlKeywords.setLayout(gblKeywords);
		pnlDownload.setLayout(gblDownload);
		pnlOther.setLayout(gblOther);
		spPaths = new JScrollPane(pnlPaths);
		spConnection = new JScrollPane(pnlConnection);
		spGUI = new JScrollPane(pnlGUI);
		spKeywords = new JScrollPane(pnlKeywords);
		spDownload = new JScrollPane(pnlDownload);
		spOther = new JScrollPane(pnlOther);

		txtStdSavePath.setEditable(false);
		txtConnectionCount.setEditable(false);
		txtConnectionCountPerHost.setEditable(false);
		txtThreadCount.setEditable(false);

		btnCancel.setMnemonic(KeyEvent.VK_C);

		pnlButtons.add(btnSave);
		pnlButtons.add(btnReset);
		pnlButtons.add(btnCancel);
		btnSave.addActionListener(this);
		btnReset.addActionListener(this);
		btnCancel.addActionListener(this);
		btnStdSavePath.addActionListener(this);

		sldConnectionCount.setSnapToTicks(true);
		sldConnectionCount.setMajorTickSpacing(10);
		sldConnectionCount.setMinorTickSpacing(1);
		sldConnectionCount.setPaintTicks(true);
		sldConnectionCount.setPaintLabels(true);
		sldConnectionCount.setMinimum(0);
		sldConnectionCount.setMaximum(50);
		sldConnectionCount.addChangeListener(this);

		sldConnectionCountPerHost.setSnapToTicks(true);
		sldConnectionCountPerHost.setMajorTickSpacing(10);
		sldConnectionCountPerHost.setMinorTickSpacing(1);
		sldConnectionCountPerHost.setPaintTicks(true);
		sldConnectionCountPerHost.setPaintLabels(true);
		sldConnectionCountPerHost.setMinimum(0);
		sldConnectionCountPerHost.setMaximum(50);
		sldConnectionCountPerHost.addChangeListener(this);

		txtConnectionCountPerHost.setToolTipText(Localization.getString("MaxConnectionCountToolTip"));
		sldConnectionCountPerHost.setToolTipText(Localization.getString("MaxConnectionCountToolTip"));

		sldThreadCount.setSnapToTicks(true);
		sldThreadCount.setMajorTickSpacing(1);
		sldThreadCount.setMinorTickSpacing(1);
		sldThreadCount.setPaintTicks(true);
		sldThreadCount.setPaintLabels(true);
		sldThreadCount.setMinimum(1);
		sldThreadCount.setMaximum(Runtime.getRuntime().availableProcessors());
		sldThreadCount.addChangeListener(this);

		pnlLogDays.add(new JLabel(Localization.getString("LogDaysT")));

		btnMaxFailedCountPlus.addActionListener(this);
		btnMaxFailedCountMinus.addActionListener(this);
		txtMaxFailedCount.setEditable(false);
		txtMaxFailedCount.setColumns(5);
		pnlMaxFailedCount.add(new JLabel(Localization.getString("MaxFailedCountT1")));
		pnlMaxFailedCount.add(txtMaxFailedCount);
		pnlMaxFailedCount.add(btnMaxFailedCountPlus);
		pnlMaxFailedCount.add(btnMaxFailedCountMinus);
		pnlMaxFailedCount.add(new JLabel(Localization.getString("MaxFailedCountT2")));
		pnlMaxFailedCount.setToolTipText(Localization.getString("MaxFailedCountToolTip"));

		txtMinFilesize.setToolTipText(Localization.getString("MinFilesizeToolTip"));

		tp.setFocusable(false);
		tp.setTabPlacement(JTabbedPane.TOP);
		tp.addTab(Localization.getString("SettingsGUI"), Icons.getTangoIcon("apps/preferences-system-windows.png", 22), spGUI);
		tp.addTab(Localization.getString("SettingsConnection"), Icons.getTangoIcon("status/network-idle.png", 22), spConnection);
		tp.addTab(Localization.getString("SettingsPaths"), Icons.getTangoIcon("places/folder.png", 22), spPaths);
		tp.addTab(Localization.getString("SettingsKeywords"), Icons.getTangoIcon("emblems/emblem-favorite.png", 22), spKeywords);
		tp.addTab(Localization.getString("SettingsDownload"), Icons.getTangoIcon("actions/go-down.png", 22), spDownload);
		tp.addTab(Localization.getString("SettingsOther"), Icons.getTangoIcon("categories/preferences-system.png", 22), spOther);

		buttonGroup.add(rbNoProxy);
		buttonGroup.add(rbHTTP);
		txtProxyPassword.setEchoChar('*');

		cmbSizeView.addItem(Localization.getString("AutoChangeSize"));
		cmbSizeView.addItem(Localization.getString("OnlyB"));
		cmbSizeView.addItem(Localization.getString("OnlyKiB"));
		cmbSizeView.addItem(Localization.getString("OnlyMiB"));
		cmbSizeView.addItem(Localization.getString("OnlyGiB"));
		cmbSizeView.addItem(Localization.getString("OnlyTiB"));
		cmbProgressView.addItem(Localization.getString("ProgressbarPercent"));
		cmbProgressView.addItem(Localization.getString("ProgressbarSize"));
		cmbProgressView.addItem(Localization.getString("NoProgressbarPercent"));
		cmbProgressView.addItem(Localization.getString("NoProgressbarSize"));
		cmbKeywordMatchMode.addItem(Localization.getString("MatchOnlyExact"));
		cmbKeywordMatchMode.addItem(Localization.getString("MatchAllStrict"));
		cmbKeywordMatchMode.addItem(Localization.getString("MatchAll"));
		cmbKeywordMatchMode.setToolTipText(Localization.getString("KeywordSearchTooltip"));
		cmbLanguage.addItem(Localization.getString("German"));
		cmbLanguage.addItem(Localization.getString("English"));
		cmbLanguage.setToolTipText(Localization.getString("LanguageTooltip"));
		cmbLAF.addItem(Localization.getString("LAFDefault"));
		cmbLAF.addItem(Localization.getString("LAFSystem"));
		for (int i = 2; i < SettingsManager.LAF_NAMES.length; i++) {
			cmbLAF.addItem(SettingsManager.LAF_NAMES[i]);
		}
		cmbCookies.addItem(Localization.getString("CookiesNo"));
		cmbCookies.addItem(Localization.getString("CookiesIE"));
		cmbCookies.addItem(Localization.getString("CookiesFF"));
		cmbCookies.addItem(Localization.getString("CookiesOP"));
		cmbCookies.addItem(Localization.getString("CookiesPM"));
		cmbCookies.addItem(Localization.getString("CookiesOPNEW"));
		cmbCookies.addItemListener(this);
		cmbSortDownloadsOnStart.addItem(Localization.getString("ByContainerURLAndDirectory"));
		cmbSortDownloadsOnStart.addItem(Localization.getString("ByDirectoryAndContainerURL"));
		cmbSortDownloadsOnStart.addItem(Localization.getString("ByContainerURLOnly"));
		cmbSortDownloadsOnStart.addItem(Localization.getString("ByDirectoryOnly"));
		cmbSortDownloadsOnStart.addItem(Localization.getString("NoSort"));
		cmbSortDownloadsOnStart.addItem(Localization.getString("ByDateTimeOnly"));

		cmbSubdirsResolutionMode.addItem(Localization.getString("SubdirResolutionModeOnlyLower"));
		cmbSubdirsResolutionMode.addItem(Localization.getString("SubdirResolutionModeOnlyHigher"));
		cmbSubdirsResolutionMode.addItem(Localization.getString("SubdirResolutionModeOnlyWidth"));
		cmbSubdirsResolutionMode.addItem(Localization.getString("SubdirResolutionModeOnlyHeight"));
		cmbSubdirsResolutionMode.addItem(Localization.getString("SubdirResolutionModeBoth"));

		pnlCookiesOpera.add(cbCookiesOperaFixed, BorderLayout.WEST);
		pnlCookiesOpera.add(txtCookiesOpera, BorderLayout.CENTER);
		pnlCookiesOpera.add(btnCookiesOpera, BorderLayout.EAST);
		pnlCookiesOpera.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		cbCookiesOperaFixed.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		txtCookiesOpera.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		btnCookiesOpera.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		txtCookiesOpera.setEditable(false);
		btnCookiesOpera.addActionListener(this);
		cbCookiesOperaFixed.addItemListener(this);

		pnlCookiesOperaNew.add(cbCookiesOperaNewFixed, BorderLayout.WEST);
		pnlCookiesOperaNew.add(txtCookiesOperaNew, BorderLayout.CENTER);
		pnlCookiesOperaNew.add(btnCookiesOperaNew, BorderLayout.EAST);
		pnlCookiesOperaNew.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		cbCookiesOperaNewFixed.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		txtCookiesOperaNew.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		btnCookiesOperaNew.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		txtCookiesOperaNew.setEditable(false);
		btnCookiesOperaNew.addActionListener(this);
		cbCookiesOperaNewFixed.addItemListener(this);

		pnlCookiesFirefox.add(cbCookiesFirefoxFixed, BorderLayout.WEST);
		pnlCookiesFirefox.add(txtCookiesFirefox, BorderLayout.CENTER);
		pnlCookiesFirefox.add(btnCookiesFirefox, BorderLayout.EAST);
		pnlCookiesFirefox.setToolTipText(Localization.getString("CookiesFirefoxToolTip"));
		cbCookiesFirefoxFixed.setToolTipText(Localization.getString("CookiesFirefoxToolTip"));
		txtCookiesFirefox.setToolTipText(Localization.getString("CookiesFirefoxToolTip"));
		btnCookiesFirefox.setToolTipText(Localization.getString("CookiesFirefoxToolTip"));
		txtCookiesFirefox.setEditable(false);
		btnCookiesFirefox.addActionListener(this);
		cbCookiesFirefoxFixed.addItemListener(this);

		pnlCookiesPaleMoon.add(cbCookiesPaleMoonFixed, BorderLayout.WEST);
		pnlCookiesPaleMoon.add(txtCookiesPaleMoon, BorderLayout.CENTER);
		pnlCookiesPaleMoon.add(btnCookiesPaleMoon, BorderLayout.EAST);
		pnlCookiesPaleMoon.setToolTipText(Localization.getString("CookiesPaleMoonToolTip"));
		cbCookiesPaleMoonFixed.setToolTipText(Localization.getString("CookiesPaleMoonToolTip"));
		txtCookiesPaleMoon.setToolTipText(Localization.getString("CookiesPaleMoonToolTip"));
		btnCookiesPaleMoon.setToolTipText(Localization.getString("CookiesPaleMoonToolTip"));
		txtCookiesPaleMoon.setEditable(false);
		btnCookiesPaleMoon.addActionListener(this);
		cbCookiesPaleMoonFixed.addItemListener(this);

		cmbAllowedFilenameChars.addItem(Localization.getString("FilenameAsciiOnly"));
		cmbAllowedFilenameChars.addItem(Localization.getString("FilenameAsciiUmlaut"));
		cmbAllowedFilenameChars.addItem(Localization.getString("FilenameAlle"));

		cmbDebugLevel.addItem("Off");
		cmbDebugLevel.addItem("All");
		cmbDebugLevel.addItem("Info");
		cmbDebugLevel.addItem("Trace");
		cmbDebugLevel.addItem("Debug");
		cmbDebugLevel.addItem("Warn");
		cmbDebugLevel.addItem("Error");
		cmbDebugLevel.addItem("Fatal");
		cmbDebugLevel.setSelectedIndex(5);

		chkDeselectNoKeyword.setToolTipText(Localization.getString("DeselectNoKeywordTooltip"));
		chkSaveLogs.setToolTipText(Localization.getString("SaveLogsTooltip"));

		init();

		rbNoProxy.addChangeListener(this);
		rbHTTP.addChangeListener(this);
		cbAuth.addChangeListener(this);

		TableTool.internationalizeColumns(jtSubdirs);

		jtSubdirs.getColumn("SubdirMinimum").setCellEditor(new SubdirCellEditor());
		jtSubdirs.getColumn("SubdirMaximum").setCellEditor(new SubdirCellEditor());
		jtSubdirs.getColumn("SubdirResolutionMinimum").setCellEditor(new SubdirResolutionCellEditor());
		jtSubdirs.getColumn("SubdirResolutionMaximum").setCellEditor(new SubdirResolutionCellEditor());
		jtSubdirs.setToolTipText(Localization.getString("SubdirsToolTip"));
		jtSubdirs.addMouseListener(this);
		jtSubdirs.setDefaultRenderer(Object.class, new DefaultStringColorRowRenderer());
		jtSubdirs.getColumn("SubdirMinimum").setCellRenderer(new DefaultNumberColorRowRenderer());
		jtSubdirs.getColumn("SubdirMaximum").setCellRenderer(new DefaultNumberColorRowRenderer());
		jtSubdirs.getColumn("SubdirResolutionMinimum").setCellRenderer(new DefaultStringColorRowRenderer());
		jtSubdirs.getColumn("SubdirResolutionMaximum").setCellRenderer(new DefaultStringColorRowRenderer());
		jtSubdirs.setRowHeight(TableTool.calculateRowHeight(jtSubdirs, true, true));
		Dimension preferredScrollableTableSize = new Dimension(jtSubdirs.getPreferredScrollableViewportSize().width, 5 * jtSubdirs.getRowHeight());
		jtSubdirs.setPreferredScrollableViewportSize(preferredScrollableTableSize);
		jtSubdirs.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);

		spSubdirs.addMouseListener(this);
		updateColWidthsFromSettingsManager();
		jtSubdirs.getColumnModel().addColumnModelListener(this);
		jtSubdirs.getTableHeader().setReorderingAllowed(false);
		jtSubdirs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		popupSubdirs.add(menuItemNew);
		popupSubdirs.add(menuItemUp);
		popupSubdirs.add(menuItemDown);
		popupSubdirs.add(menuItemDelete);
		menuItemNew.addActionListener(this);
		menuItemUp.addActionListener(this);
		menuItemDown.addActionListener(this);
		menuItemDelete.addActionListener(this);

		pnlSubdirButtons.setLayout(new SpringLayout());
		pnlSubdirButtons.add(btnSubdirNew);
		pnlSubdirButtons.add(btnSubdirUp);
		pnlSubdirButtons.add(btnSubdirDown);
		pnlSubdirButtons.add(btnSubdirDelete);
		pnlSubdirButtons.add(btnSubdirHelp);
		SpringUtilities.makeCompactGrid(pnlSubdirButtons, 5, 1, 0, 0, 5, 5);
		btnSubdirNew.addActionListener(this);
		btnSubdirUp.addActionListener(this);
		btnSubdirDown.addActionListener(this);
		btnSubdirDelete.addActionListener(this);
		btnSubdirHelp.addActionListener(this);

		pnlRegexReplacePageTitle.setBorder(BorderFactory.createTitledBorder(Localization.getString("RegexReplacePageTitle")));

		pnlRegexReplaceFilename.setBorder(BorderFactory.createTitledBorder(Localization.getString("RegexReplaceFilename")));

		GridBagConstraints gbc = new GridBagConstraints();

		// Paths
		int i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblPaths, gbc, lblStdSavePath, pnlPaths);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblPaths, gbc, txtStdSavePath, pnlPaths);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblPaths, gbc, btnStdSavePath, pnlPaths);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblPaths, gbc, lblRememberLastUsedPath, pnlPaths);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblPaths, gbc, chkRememberLastUsedPath, pnlPaths);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblPaths, gbc, lblSubdirsEnabled, pnlPaths);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblPaths, gbc, chkSubdirsEnabled, pnlPaths);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblPaths, gbc, lblSubdirsResolutionMode, pnlPaths);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblPaths, gbc, cmbSubdirsResolutionMode, pnlPaths);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.9, 0.3);
		GridBagLayoutTool.addItemToPanel(gblPaths, gbc, spSubdirs, pnlPaths);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblPaths, gbc, pnlSubdirButtons, pnlPaths);

		// Connection
		i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, rbNoProxy, pnlConnection);
		gbc = gblt.getGBC(1, i, 4, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, rbHTTP, pnlConnection);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, lblProxyName, pnlConnection);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, txtProxyName, pnlConnection);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, lblProxyPort, pnlConnection);
		gbc = gblt.getGBC(3, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, txtProxyPort, pnlConnection);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, cbAuth, pnlConnection);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, lblProxyUser, pnlConnection);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, txtProxyUser, pnlConnection);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, lblProxyPassword, pnlConnection);
		gbc = gblt.getGBC(3, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, txtProxyPassword, pnlConnection);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, lblConnectionCount, pnlConnection);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, sldConnectionCount, pnlConnection);
		gbc = gblt.getGBC(5, i, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, txtConnectionCount, pnlConnection);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, lblConnectionCountPerHost, pnlConnection);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, sldConnectionCountPerHost, pnlConnection);
		gbc = gblt.getGBC(5, i, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, txtConnectionCountPerHost, pnlConnection);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, lblTimeout, pnlConnection);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, txtTimeout, pnlConnection);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, lblCookies, pnlConnection);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, cmbCookies, pnlConnection);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, lblCookiesOpera, pnlConnection);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, pnlCookiesOpera, pnlConnection);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, lblCookiesOperaNew, pnlConnection);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, pnlCookiesOperaNew, pnlConnection);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, lblCookiesFirefox, pnlConnection);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, pnlCookiesFirefox, pnlConnection);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, lblCookiesPaleMoon, pnlConnection);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, pnlCookiesPaleMoon, pnlConnection);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, lblUserAgent, pnlConnection);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblConnection, gbc, txtUserAgent, pnlConnection);

		// GUI
		i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, lblLAF, pnlGUI);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, cmbLAF, pnlGUI);
		// i++;
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, lblLanguage, pnlGUI);
		gbc = gblt.getGBC(3, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, cmbLanguage, pnlGUI);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, lblSizeView, pnlGUI);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, cmbSizeView, pnlGUI);
		// i++;
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, lblProgressView, pnlGUI);
		gbc = gblt.getGBC(3, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, cmbProgressView, pnlGUI);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, lblDownloadRate, pnlGUI);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, chkDownloadRate, pnlGUI);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, lblWindowSizePos, pnlGUI);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, chkWindowSizePos, pnlGUI);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, lblDownloadSelectionWindowSizePos, pnlGUI);
		gbc = gblt.getGBC(3, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, chkDownloadSelectionWindowSizePos, pnlGUI);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, lblSaveTableColumnSizes, pnlGUI);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, chkSaveTableColumnSizes, pnlGUI);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, lblSaveTableSortOrders, pnlGUI);
		gbc = gblt.getGBC(3, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, chkSaveTableSortOrders, pnlGUI);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, lblDownloadsCompleteNotification, pnlGUI);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, chkDownloadsCompleteNotification, pnlGUI);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, lblDownloadPreviews, pnlGUI);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, chkDownloadPreviews, pnlGUI);
		i++;
		gbc = gblt.getGBC(0, i, 4, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblGUI, gbc, pnlRegexReplacePageTitle, pnlGUI);

		// Keywords
		i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblKeywords, gbc, lblKeywordMatchMode, pnlKeywords);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblKeywords, gbc, cmbKeywordMatchMode, pnlKeywords);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblKeywords, gbc, lblDeselectNoKeyword, pnlKeywords);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblKeywords, gbc, chkDeselectNoKeyword, pnlKeywords);
		i++;

		// Download
		i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblDownload, gbc, lblSaveLogs, pnlDownload);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblDownload, gbc, chkSaveLogs, pnlDownload);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblDownload, gbc, lblAutoStartDownloads, pnlDownload);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblDownload, gbc, chkAutoStartDownloads, pnlDownload);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblDownload, gbc, lblAutoRetryAfterDownloadsComplete, pnlDownload);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblDownload, gbc, chkAutoRetryAfterDownloadsComplete, pnlDownload);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblDownload, gbc, lblAllowedFilenameChars, pnlDownload);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblDownload, gbc, cmbAllowedFilenameChars, pnlDownload);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblDownload, gbc, lblMaxFailedCount, pnlDownload);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblDownload, gbc, pnlMaxFailedCount, pnlDownload);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblDownload, gbc, lblMinFilesize, pnlDownload);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblDownload, gbc, txtMinFilesize, pnlDownload);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblDownload, gbc, lblSortDownloadsOnStart, pnlDownload);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblDownload, gbc, cmbSortDownloadsOnStart, pnlDownload);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblDownload, gbc, pnlRegexReplaceFilename, pnlDownload);

		// Other
		i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, lblUpdates, pnlOther);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, chkUpdates, pnlOther);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, lblCheckClipboard, pnlOther);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, chkCheckClipboard, pnlOther);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, lblWebExtensionPort, pnlOther);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, txtWebExtensionPort, pnlOther);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, lblAlwaysAddTitle, pnlOther);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, chkAlwaysAddTitle, pnlOther);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, lblRulesBefore, pnlOther);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, chkRulesBefore, pnlOther);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, lblBackupDB, pnlOther);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, chkBackupDB, pnlOther);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, lblDefragDB, pnlOther);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, chkDefragDB, pnlOther);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, lblDefragMinFilesize, pnlOther);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, txtDefragMinFilesize, pnlOther);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, lblDebugLevel, pnlOther);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, cmbDebugLevel, pnlOther);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, lblThreadCount, pnlOther);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, sldThreadCount, pnlOther);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER);
		GridBagLayoutTool.addItemToPanel(gblOther, gbc, txtThreadCount, pnlOther);

		gbc = new GridBagConstraints();
		gbc = gblt.getGBC(0, 0, 1, 1, 1.0, 0.8);
		GridBagLayoutTool.addItemToDialog(gbl, gbc, tp, this);
		gbc = gblt.getGBC(0, 1, 1, 1, 0.1, 0.0);
		GridBagLayoutTool.addItemToDialog(gbl, gbc, pnlButtons, this);

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtCookiesFirefox);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtCookiesPaleMoon);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtCookiesOpera);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtCookiesOperaNew);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtDefragMinFilesize);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtMaxFailedCount);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtMinFilesize);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtProxyName);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtProxyPort);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtProxyUser);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtTimeout);

		addWindowListener(this);

		SettingsManager.instance().addSettingsListener(this);

		pack();
		setLocationRelativeTo(owner);

		setVisible(true);
	}

	/**
	 * Initialize
	 */
	private void init() {
		txtStdSavePath.setText(sm.getSavePath());
		txtConnectionCount.setText(String.valueOf(sm.getConnections()));
		txtConnectionCountPerHost.setText(String.valueOf(sm.getConnectionsPerHost()));
		txtThreadCount.setText(String.valueOf(sm.getThreadCount()));
		txtMaxFailedCount.setText(String.valueOf(sm.getMaxFailedCount()));
		txtMinFilesize.setText(String.valueOf(sm.getMinFilesize()));
		txtTimeout.setText(String.valueOf(sm.getTimeout()));
		txtDefragMinFilesize.setText(String.valueOf(sm.getDefragMinFilesize()));
		txtUserAgent.setText(sm.getUserAgent());
		chkUpdates.setSelected(sm.isUpdates());
		chkSaveLogs.setSelected(sm.isSaveLogs());
		chkAutoStartDownloads.setSelected(sm.isAutoStartDownloads());
		chkAutoRetryAfterDownloadsComplete.setSelected(sm.isAutoRetryAfterDownloadsComplete());
		chkCheckClipboard.setSelected(sm.isCheckClipboard());
		txtWebExtensionPort.setText(String.valueOf(sm.getWebExtensionPort()));
		chkDefragDB.setSelected(sm.isDefragDBOnStart());
		chkBackupDB.setSelected(sm.isBackupDbOnStart());
		chkSubdirsEnabled.setSelected(sm.isSubdirsEnabled());
		chkDownloadRate.setSelected(sm.isDownloadRate());
		chkDownloadsCompleteNotification.setSelected(sm.isDownloadsCompleteNotification());
		chkDownloadPreviews.setSelected(sm.isDownloadPreviews());
		sldConnectionCount.setValue(sm.getConnections());
		sldConnectionCountPerHost.setValue(sm.getConnectionsPerHost());
		sldThreadCount.setValue(sm.getThreadCount());

		String lang = sm.getLanguage();
		if (lang.equals("de_DE")) {
			cmbLanguage.setSelectedIndex(0);
		} else {
			cmbLanguage.setSelectedIndex(1);
		}
		cmbSizeView.setSelectedIndex(sm.getSizeView());

		cmbProgressView.setSelectedIndex(sm.getProgessView());

		cmbKeywordMatchMode.setSelectedIndex(sm.getKeywordMatchMode());

		cmbLAF.setSelectedIndex(sm.getLookAndFeel());

		cmbCookies.setSelectedIndex(sm.getCookiesFromBrowser());

		cmbSortDownloadsOnStart.setSelectedIndex(sm.getSortDownloadsOnStart());

		cmbSubdirsResolutionMode.setSelectedIndex(sm.getSubdirsResolutionMode());

		boolean bOpera = (sm.getCookiesFromBrowser() == BrowserCookies.BROWSER_OPERA);
		boolean bOperaNew = (sm.getCookiesFromBrowser() == BrowserCookies.BROWSER_OPERA_NEW);
		boolean bFirefox = (sm.getCookiesFromBrowser() == BrowserCookies.BROWSER_FIREFOX);
		boolean bPaleMoon = (sm.getCookiesFromBrowser() == BrowserCookies.BROWSER_PALE_MOON);
		lblCookiesOpera.setVisible(bOpera);
		cbCookiesOperaFixed.setVisible(bOpera);
		txtCookiesOpera.setVisible(bOpera);
		btnCookiesOpera.setVisible(bOpera);

		lblCookiesOperaNew.setVisible(bOperaNew);
		cbCookiesOperaNewFixed.setVisible(bOperaNew);
		txtCookiesOperaNew.setVisible(bOperaNew);
		btnCookiesOperaNew.setVisible(bOperaNew);

		lblCookiesFirefox.setVisible(bFirefox);
		cbCookiesFirefoxFixed.setVisible(bFirefox);
		txtCookiesFirefox.setVisible(bFirefox);
		btnCookiesFirefox.setVisible(bFirefox);

		lblCookiesPaleMoon.setVisible(bPaleMoon);
		cbCookiesPaleMoonFixed.setVisible(bPaleMoon);
		txtCookiesPaleMoon.setVisible(bPaleMoon);
		btnCookiesPaleMoon.setVisible(bPaleMoon);

		btnCookiesOpera.setEnabled(cbCookiesOperaFixed.isSelected());
		btnCookiesOperaNew.setEnabled(cbCookiesOperaNewFixed.isSelected());
		btnCookiesFirefox.setEnabled(cbCookiesFirefoxFixed.isSelected());
		btnCookiesPaleMoon.setEnabled(cbCookiesPaleMoonFixed.isSelected());

		cbCookiesOperaFixed.setSelected(sm.isCookieFileOperaFixed());
		cbCookiesOperaNewFixed.setSelected(sm.isCookieFileOperaNewFixed());
		cbCookiesFirefoxFixed.setSelected(sm.isCookieFileFirefoxFixed());
		cbCookiesPaleMoonFixed.setSelected(sm.isCookieFilePaleMoonFixed());

		txtCookiesOpera.setText(CookieManager.getCookieFileForOpera(false));
		txtCookiesOperaNew.setText(CookieManager.getCookieFileForOperaNew());
		txtCookiesFirefox.setText(CookieManager.getCookieFileForFirefox());
		txtCookiesPaleMoon.setText(CookieManager.getCookieFileForPaleMoon());

		cmbAllowedFilenameChars.setSelectedIndex(sm.getAllowedFilenameChars());

		String debugLevel = sm.getDebugLevel();
		for (int i = 0; i < cmbDebugLevel.getItemCount(); i++) {
			if (cmbDebugLevel.getItemAt(i).equals(debugLevel)) {
				cmbDebugLevel.setSelectedIndex(i);
				break;
			}
		}

		chkWindowSizePos.setSelected(sm.isSaveWindowSizePosition());

		chkDownloadSelectionWindowSizePos.setSelected(sm.isSaveDownloadSelectionWindowSizePosition());

		chkSaveTableColumnSizes.setSelected(sm.isSaveTableColumnSizes());

		chkSaveTableSortOrders.setSelected(sm.isSaveTableSortOrders());

		chkRememberLastUsedPath.setSelected(sm.isSaveLastPath());

		chkAlwaysAddTitle.setSelected(sm.isAlwaysAddTitle());

		chkDeselectNoKeyword.setSelected(sm.isDeselectNoKeyword());

		chkRulesBefore.setSelected(sm.isRulesBeforeClasses());

		pm.readFromSettings();
		txtProxyName.setText(pm.getProxyname());
		txtProxyPort.setText(String.valueOf(pm.getProxyport()));
		txtProxyUser.setText(pm.getProxyuser());
		txtProxyPassword.setText(pm.getProxypassword());

		int mode = pm.getMode();
		if (mode == ProxyManager.DIRECT_CONNECTION) {
			rbNoProxy.setSelected(true);
		} else if (mode == ProxyManager.HTTP_PROXY) {
			rbHTTP.setSelected(true);
		}
		boolean b = pm.isAuth();
		if (b && (mode > 0)) {
			cbAuth.setSelected(true);
		}

		lblProxyName.setEnabled(!rbNoProxy.isSelected());
		lblProxyPort.setEnabled(!rbNoProxy.isSelected());
		txtProxyName.setEnabled(!rbNoProxy.isSelected());
		txtProxyPort.setEnabled(!rbNoProxy.isSelected());
		cbAuth.setEnabled(!rbNoProxy.isSelected());

		lblProxyUser.setEnabled(cbAuth.isSelected());
		lblProxyPassword.setEnabled(cbAuth.isSelected());
		txtProxyUser.setEnabled(cbAuth.isSelected());
		txtProxyPassword.setEnabled(cbAuth.isSelected());

		subdirModel.removeAllRows();
		List<Subdir> v = sm.getSubdirs();
		for (int s = 0; s < v.size(); s++) {
			subdirModel.addRow(v.get(s));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnSave) {
			applySettings();
			Main.instance().setMessage(Localization.getString("SavingSettings"));
			boolean b = sm.writeSettings(true);
			if (b) {
				Main.instance().setMessage(Localization.getString("SettingsSaved"));
			} else {
				Main.instance().setMessage(Localization.getString("SettingsSaveFailed"));
			}
			SettingsManager.instance().removeSettingsListener(this);
			this.dispose();
		} else if (e.getSource() == btnReset) {
			init();
		} else if (e.getSource() == btnCancel) {
			SettingsManager.instance().removeSettingsListener(this);
			this.dispose();
		} else if (e.getSource() == btnStdSavePath) {
			File file = FileDialogTool.showFolderDialog(this, txtStdSavePath.getText(), null);
			if (file != null) {
				String folder = file.getAbsolutePath() + FileTool.FILE_SEPERATOR;
				txtStdSavePath.setText(folder);
				file = null;
			}
		} else if (e.getSource() == btnMaxFailedCountPlus) {
			int val = Integer.parseInt(txtMaxFailedCount.getText());
			if (val < 20) {
				val += 1;
				txtMaxFailedCount.setText(String.valueOf(val));
			}
		} else if (e.getSource() == btnMaxFailedCountMinus) {
			int val = Integer.parseInt(txtMaxFailedCount.getText());
			if (val > 0) {
				val -= 1;
				txtMaxFailedCount.setText(String.valueOf(val));
			}
		} else if (e.getSource() == btnCookiesOpera) {
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.getName().equals("cookies4.dat") || f.isDirectory()) {
						return true;
					}
					return false;
				}

				@Override
				public String getDescription() {
					return "cookies4.dat";
				}
			};
			File file = FileDialogTool.showFileOpenDialog(this, txtCookiesOpera.getText(), filter);
			if (file != null) {
				String strFile = file.getAbsolutePath();
				txtCookiesOpera.setText(strFile);
				strFile = null;
			}
			file = null;
		} else if (e.getSource() == btnCookiesOperaNew) {
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.getName().equals("Cookies") || f.isDirectory()) {
						return true;
					}
					return false;
				}

				@Override
				public String getDescription() {
					return "Cookies";
				}
			};
			File file = FileDialogTool.showFileOpenDialog(this, txtCookiesOperaNew.getText(), filter);
			if (file != null) {
				String strFile = file.getAbsolutePath();
				txtCookiesOperaNew.setText(strFile);
				strFile = null;
			}
			file = null;
		} else if (e.getSource() == btnCookiesFirefox) {
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.getName().equals("cookies.txt") || f.getName().equals("cookies.sqlite") || f.isDirectory()) {
						return true;
					}
					return false;
				}

				@Override
				public String getDescription() {
					return "cookies.txt, cookies.sqlite";
				}
			};
			File file = FileDialogTool.showFileOpenDialog(this, txtCookiesFirefox.getText(), filter);
			if (file != null) {
				String strFile = file.getAbsolutePath();
				txtCookiesFirefox.setText(strFile);
				strFile = null;
			}
			file = null;
		} else if (e.getSource() == btnCookiesPaleMoon) {
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.getName().equals("cookies.sqlite") || f.isDirectory()) {
						return true;
					}
					return false;
				}

				@Override
				public String getDescription() {
					return "cookies.sqlite";
				}
			};
			File file = FileDialogTool.showFileOpenDialog(this, txtCookiesPaleMoon.getText(), filter);
			if (file != null) {
				String strFile = file.getAbsolutePath();
				txtCookiesPaleMoon.setText(strFile);
				strFile = null;
			}
			file = null;
		} else if (e.getSource() == menuItemNew || e.getSource() == btnSubdirNew) {
			subdirModel.addEmptyRow();
		} else if (e.getSource() == menuItemDelete || e.getSource() == btnSubdirDelete) {
			int rows[] = jtSubdirs.getSelectedRows();
			for (int i = 0; i < rows.length; i++) {
				subdirModel.removeRow(rows[i]);
			}
		} else if (e.getSource() == menuItemUp || e.getSource() == btnSubdirUp) {
			int selectedRow = jtSubdirs.getSelectedRow();
			if (selectedRow > 0) {
				subdirModel.moveRow(selectedRow, selectedRow, selectedRow - 1);
				jtSubdirs.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
			}
		} else if (e.getSource() == menuItemDown || e.getSource() == btnSubdirDown) {
			int selectedRow = jtSubdirs.getSelectedRow();
			if (selectedRow < (subdirModel.getRowCount() - 1)) {
				subdirModel.moveRow(selectedRow, selectedRow, selectedRow + 1);
				jtSubdirs.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
			}
		} else if (e.getSource() == btnSubdirHelp) {
			JOptionPane.showMessageDialog(this, Localization.getString("SubdirHelp"), Localization.getString("Help"), JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Apply Settings
	 */
	private void applySettings() {
		SettingsManager.instance().removeSettingsListener(this);
		Main.instance().setMessage(Localization.getString("ApplyingSettings"));
		sm.setConnections(sldConnectionCount.getValue());
		sm.setConnectionsPerHost(sldConnectionCountPerHost.getValue());
		sm.setThreadCount(sldThreadCount.getValue());
		sm.setMaxFailedCount(Integer.parseInt(txtMaxFailedCount.getText()));
		try {
			sm.setMinFilesize(Integer.parseInt(txtMinFilesize.getText()));
		} catch (NumberFormatException nfe) {
			logger.error(nfe.getMessage(), nfe);
			txtMinFilesize.setText(String.valueOf(sm.getMinFilesize()));
		}
		try {
			sm.setDefragMinFilesize(Integer.parseInt(txtDefragMinFilesize.getText()));
		} catch (NumberFormatException nfe) {
			logger.error(nfe.getMessage(), nfe);
			txtDefragMinFilesize.setText(String.valueOf(sm.getDefragMinFilesize()));
		}
		try {
			int val = Integer.parseInt(txtTimeout.getText());
			if (val <= 1000) {
				txtTimeout.setText(String.valueOf(sm.getTimeout()));
			} else {
				sm.setTimeout(val);
			}
		} catch (NumberFormatException nfe) {
			logger.error(nfe.getMessage(), nfe);
			txtTimeout.setText(String.valueOf(sm.getTimeout()));
		}
		sm.setSavePath(txtStdSavePath.getText());
		sm.setUserAgent(txtUserAgent.getText());
		sm.setUpdates(chkUpdates.isSelected());
		sm.setSaveLogs(chkSaveLogs.isSelected());
		sm.setAutoStartDownloads(chkAutoStartDownloads.isSelected());
		sm.setAutoRetryAfterDownloadsComplete(chkAutoRetryAfterDownloadsComplete.isSelected());
		sm.setCheckClipboard(chkCheckClipboard.isSelected());
		try {
			int val = Integer.parseInt(txtWebExtensionPort.getText());
			if (val < 0 || val > 65535) {
				txtWebExtensionPort.setText(String.valueOf(sm.getWebExtensionPort()));
			} else {
				sm.setWebExtensionPort(val);
			}
		} catch (NumberFormatException nfe) {
			logger.error("WebExtensionPort is not an integer: {}", txtWebExtensionPort.getText(), nfe);
			txtWebExtensionPort.setText(String.valueOf(sm.getWebExtensionPort()));
		}
		sm.setDownloadRate(chkDownloadRate.isSelected());
		if (cmbLanguage.getSelectedIndex() == 0) {
			sm.setLanguage("de_DE");
		} else {
			sm.setLanguage("en_EN");
		}
		sm.setSizeView(cmbSizeView.getSelectedIndex());
		sm.setProgessView(cmbProgressView.getSelectedIndex());
		sm.setKeywordMatchMode(cmbKeywordMatchMode.getSelectedIndex());
		int previousLookAndFeel = sm.getLookAndFeel();
		sm.setLookAndFeel(cmbLAF.getSelectedIndex());
		sm.setCookiesFromBrowser(cmbCookies.getSelectedIndex());
		sm.setCookieFileOperaFixed(cbCookiesOperaFixed.isSelected());
		sm.setCookieFileOpera(txtCookiesOpera.getText());
		sm.setCookieFileOperaNewFixed(cbCookiesOperaNewFixed.isSelected());
		sm.setCookieFileOperaNew(txtCookiesOperaNew.getText());
		sm.setCookieFileFirefoxFixed(cbCookiesFirefoxFixed.isSelected());
		sm.setCookieFileFirefox(txtCookiesFirefox.getText());
		sm.setCookieFilePaleMoonFixed(cbCookiesPaleMoonFixed.isSelected());
		sm.setCookieFilePaleMoon(txtCookiesPaleMoon.getText());
		sm.setAllowedFilenameChars(cmbAllowedFilenameChars.getSelectedIndex());
		sm.setDebugLevel((String)cmbDebugLevel.getSelectedItem());
		sm.setSaveWindowSizePosition(chkWindowSizePos.isSelected());
		sm.setSaveDownloadSelectionWindowSizePosition(chkDownloadSelectionWindowSizePos.isSelected());
		sm.setSaveTableColumnSizes(chkSaveTableColumnSizes.isSelected());
		sm.setSaveTableSortOrders(chkSaveTableSortOrders.isSelected());
		sm.setSaveLastPath(chkRememberLastUsedPath.isSelected());
		sm.setAlwaysAddTitle(chkAlwaysAddTitle.isSelected());
		sm.setDeselectNoKeyword(chkDeselectNoKeyword.isSelected());
		sm.setDefragDBOnStart(chkDefragDB.isSelected());
		sm.setBackupDbOnStart(chkBackupDB.isSelected());
		sm.setSubdirsEnabled(chkSubdirsEnabled.isSelected());
		sm.setDownloadsCompleteNotification(chkDownloadsCompleteNotification.isSelected());
		sm.setDownloadPreviews(chkDownloadPreviews.isSelected());
		sm.setSortDownloadsOnStart(cmbSortDownloadsOnStart.getSelectedIndex());
		sm.setSubdirsResolutionMode(cmbSubdirsResolutionMode.getSelectedIndex());

		List<Subdir> v = new ArrayList<>();
		for (int i = 0; i < jtSubdirs.getRowCount(); i++) {
			String name = (String)jtSubdirs.getValueAt(i, 0);
			long min = (Long)jtSubdirs.getValueAt(i, 1);
			long max = (Long)jtSubdirs.getValueAt(i, 2);
			int resMinW = 0;
			int resMinH = 0;
			int resMaxW = 0;
			int resMaxH = 0;
			String resMin = (String)jtSubdirs.getValueAt(i, 3);
			String resMax = (String)jtSubdirs.getValueAt(i, 4);

			String arr[] = resMin.split("x");
			if (arr.length == 2) {
				resMinW = SubdirResolutionCellEditorComponent.parseIntVal(arr[0]);
				resMinH = SubdirResolutionCellEditorComponent.parseIntVal(arr[1]);
			}

			arr = resMax.split("x");
			if (arr.length == 2) {
				resMaxW = SubdirResolutionCellEditorComponent.parseIntVal(arr[0]);
				resMaxH = SubdirResolutionCellEditorComponent.parseIntVal(arr[1]);
			}

			v.add(new Subdir(name, min, max, resMinW, resMinH, resMaxW, resMaxH));
		}
		sm.addSubdirs(v, true);

		if (DownloadQueueManager.instance().isDownloading()) {
			chkRulesBefore.setSelected(sm.isRulesBeforeClasses());
		} else {
			sm.setRulesBeforeClasses(chkRulesBefore.isSelected());
			HostManager.instance().reInitHosterList();
		}
		pm.setAuth(cbAuth.isSelected());
		if (rbNoProxy.isSelected()) {
			pm.setMode(ProxyManager.DIRECT_CONNECTION);
		} else if (rbHTTP.isSelected()) {
			pm.setMode(ProxyManager.HTTP_PROXY);
		}
		pm.setProxyname(txtProxyName.getText());
		try {
			pm.setProxyport(Integer.parseInt(txtProxyPort.getText()));
		} catch (NumberFormatException nfe) {
			txtProxyPort.setText(String.valueOf(pm.getProxyport()));
		}
		pm.setProxyuser(txtProxyUser.getText());
		pm.setProxypassword(String.valueOf(txtProxyPassword.getPassword()));
		ProxyManager.instance().writeToSettings();
		Main.instance().setMessage(Localization.getString("SettingsApplied"));
		if (cmbLAF.getSelectedIndex() != previousLookAndFeel) {
			String strLAF = SettingsManager.LAF_CLASSPATHES[cmbLAF.getSelectedIndex()];
			if (strLAF.length() > 0) {
				try {
					UIManager.setLookAndFeel(strLAF);
					SwingUtilities.updateComponentTreeUI(Main.instance());
				} catch (ClassNotFoundException e1) {
					logger.error(e1.getMessage(), e1);
				} catch (InstantiationException e1) {
					logger.error(e1.getMessage(), e1);
				} catch (IllegalAccessException e1) {
					logger.error(e1.getMessage(), e1);
				} catch (UnsupportedLookAndFeelException e1) {
					logger.error(e1.getMessage(), e1);
				}
			}
		}
		SettingsManager.instance().addSettingsListener(this);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == sldConnectionCount) {
			if (sldConnectionCount.getValue() == 0) {
				sldConnectionCount.setValue(1);
			} else {
				txtConnectionCount.setText(String.valueOf(sldConnectionCount.getValue()));
			}
		} else if (e.getSource() == sldConnectionCountPerHost) {
			txtConnectionCountPerHost.setText(String.valueOf(sldConnectionCountPerHost.getValue()));
		} else if (e.getSource() == sldThreadCount) {
			txtThreadCount.setText(String.valueOf(sldThreadCount.getValue()));
		} else if (e.getSource() == cbAuth) {
			lblProxyUser.setEnabled(cbAuth.isSelected());
			lblProxyPassword.setEnabled(cbAuth.isSelected());
			txtProxyUser.setEnabled(cbAuth.isSelected());
			txtProxyPassword.setEnabled(cbAuth.isSelected());
		} else if ((e.getSource() == rbNoProxy) || (e.getSource() == rbHTTP)) {
			boolean b1 = rbNoProxy.isSelected();
			boolean b2 = rbHTTP.isSelected();
			boolean b = false;
			if (b1) {
				b = false;
			} else if (b2) {
				b = true;
			}
			lblProxyName.setEnabled(b);
			lblProxyPort.setEnabled(b);
			txtProxyName.setEnabled(b);
			txtProxyPort.setEnabled(b);
			cbAuth.setEnabled(!(b1));
			if (b1) {
				cbAuth.setSelected(false);
			}
		}
	}

	@Override
	public void settingsChanged() {
		init();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == cmbCookies) {
			boolean bOpera = (cmbCookies.getSelectedIndex() == BrowserCookies.BROWSER_OPERA);
			boolean bOperaNew = (cmbCookies.getSelectedIndex() == BrowserCookies.BROWSER_OPERA_NEW);
			boolean bFirefox = (cmbCookies.getSelectedIndex() == BrowserCookies.BROWSER_FIREFOX);
			boolean bPaleMoon = (cmbCookies.getSelectedIndex() == BrowserCookies.BROWSER_PALE_MOON);
			lblCookiesOpera.setVisible(bOpera);
			cbCookiesOperaFixed.setVisible(bOpera);
			txtCookiesOpera.setVisible(bOpera);
			btnCookiesOpera.setVisible(bOpera);
			pnlCookiesOpera.setVisible(bOpera);
			lblCookiesOperaNew.setVisible(bOperaNew);
			cbCookiesOperaNewFixed.setVisible(bOperaNew);
			txtCookiesOperaNew.setVisible(bOperaNew);
			btnCookiesOperaNew.setVisible(bOperaNew);
			pnlCookiesOperaNew.setVisible(bOperaNew);
			lblCookiesFirefox.setVisible(bFirefox);
			cbCookiesFirefoxFixed.setVisible(bFirefox);
			txtCookiesFirefox.setVisible(bFirefox);
			btnCookiesFirefox.setVisible(bFirefox);
			pnlCookiesFirefox.setVisible(bFirefox);
			lblCookiesPaleMoon.setVisible(bPaleMoon);
			cbCookiesPaleMoonFixed.setVisible(bPaleMoon);
			txtCookiesPaleMoon.setVisible(bPaleMoon);
			btnCookiesPaleMoon.setVisible(bPaleMoon);
			pnlCookiesPaleMoon.setVisible(bPaleMoon);
		} else if (e.getSource() == cbCookiesOperaFixed) {
			btnCookiesOpera.setEnabled(cbCookiesOperaFixed.isSelected());
		} else if (e.getSource() == cbCookiesOperaNewFixed) {
			btnCookiesOperaNew.setEnabled(cbCookiesOperaNewFixed.isSelected());
		} else if (e.getSource() == cbCookiesFirefoxFixed) {
			btnCookiesFirefox.setEnabled(cbCookiesFirefoxFixed.isSelected());
		} else if (e.getSource() == cbCookiesPaleMoonFixed) {
			btnCookiesPaleMoon.setEnabled(cbCookiesPaleMoonFixed.isSelected());
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (((e.getSource() == jtSubdirs) || (e.getSource() == spSubdirs)) && (e.getButton() == 3)) {
			if (jtSubdirs.getSelectedRowCount() > 0) {
				menuItemDelete.setEnabled(true);
			} else {
				menuItemDelete.setEnabled(false);
			}
			int w = 0;
			int h = 0;
			if (e.getSource() == spSubdirs) {
				h = jtSubdirs.getTableHeader().getSize().height;
				Insets insets = spSubdirs.getInsets();
				h += insets.top + insets.bottom;
				w = insets.left + insets.right;
			}
			popupSubdirs.show(jtSubdirs, e.getX() - w, e.getY() - h);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	/**
	 * updateColWidthsToSettingsManager
	 */
	private void updateColWidthsToSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false) {
			return;
		}
		SettingsManager.instance().setColWidthsSubdirs(TableTool.serializeColWidthSetting(jtSubdirs));
		SettingsManager.instance().writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false) {
			return;
		}
		TableTool.applyColWidths(jtSubdirs, SettingsManager.instance().getColWidthsSubdirs());
	}

	@Override
	public void columnAdded(TableColumnModelEvent e) {
	}

	@Override
	public void columnMarginChanged(ChangeEvent e) {
		updateColWidthsToSettingsManager();
	}

	@Override
	public void columnMoved(TableColumnModelEvent e) {
	}

	@Override
	public void columnRemoved(TableColumnModelEvent e) {
	}

	@Override
	public void columnSelectionChanged(ListSelectionEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		SettingsManager.instance().removeSettingsListener(this);
		this.dispose();
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}

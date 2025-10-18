package ch.supertomcat.bh.gui.adder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.clipboard.ClipboardObserver;
import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.BHIcons;
import ch.supertomcat.bh.gui.queue.FileRenameDialog;
import ch.supertomcat.bh.gui.queue.PathRenameDialog;
import ch.supertomcat.bh.gui.renderer.AdderColorRowRenderer;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.hoster.Hoster;
import ch.supertomcat.bh.hoster.hosteroptions.IHosterOverrideDirectoryOption;
import ch.supertomcat.bh.hoster.hosteroptions.OverrideDirectoryOption;
import ch.supertomcat.bh.hoster.urlchecker.HostURLCheckerListener;
import ch.supertomcat.bh.hoster.urlchecker.HostURLCheckerRunnable;
import ch.supertomcat.bh.importexport.Tsv;
import ch.supertomcat.bh.keywords.Keyword;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.keywords.KeywordSearchThread;
import ch.supertomcat.bh.keywords.KeywordSearchThreadListener;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.pic.URLList;
import ch.supertomcat.bh.preview.DownloadPreviewRunnable;
import ch.supertomcat.bh.preview.PreviewCache;
import ch.supertomcat.bh.preview.PreviewCache.PreviewCacheListener;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.xml.AutoTargetDirMode;
import ch.supertomcat.bh.settings.xml.WindowSettings;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.dialog.FileDialogUtil;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;
import ch.supertomcat.supertomcatutils.gui.progress.IProgressObserver;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.hider.TableColumnHider;
import ch.supertomcat.supertomcatutils.gui.table.hider.TableColumnHiderSizeBasedImpl;
import ch.supertomcat.supertomcatutils.gui.table.hider.TableHeaderColumnSelector;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultBooleanColorRowRenderer;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultNumberColorRowRenderer;
import ch.supertomcat.supertomcatutils.image.ImageUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;
import ch.supertomcat.supertomcatutils.regex.RegexReplacePipeline;

/**
 * Panel for selecting files to download
 */
public class AdderWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	/**
	 * Minimum Preview Height
	 */
	private static final int MINIMUM_PREVIEW_HEIGHT = 100;

	/**
	 * Limit for visible previews (To avoid OutOfMemory)
	 */
	private static final int MAX_VISIBLE_PREVIEWS = 100;

	/**
	 * Maximum visible previews per direction
	 */
	private static final int MAX_VISIBLE_PREVIEWS_PER_DIRECTION = MAX_VISIBLE_PREVIEWS / 2;

	/**
	 * How many rows needed to be scrolled before loading or unloading takes place
	 */
	private static final int NO_LOAD_ROW_DISTANCE = 10;

	private static final int MIN_SCROLL_UNIT_COUNTER = 0 - (MAX_VISIBLE_PREVIEWS_PER_DIRECTION - NO_LOAD_ROW_DISTANCE);

	private static final int MAX_SCROLL_UNIT_COUNTER = 0 + (MAX_VISIBLE_PREVIEWS_PER_DIRECTION - NO_LOAD_ROW_DISTANCE);

	/**
	 * Preview Cache (Static to share between instances)
	 */
	private static PreviewCache previewCache = new PreviewCache();

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	private PreviewCacheListener previewCacheListener = new PreviewCacheListener() {
		@Override
		public void allPreviewsAdded() {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					loadAndUnloadPreviews(true);
					previewCache.removeListener(previewCacheListener);
				}
			});
		}
	};

	/**
	 * Prefix for Window Title
	 */
	private final String windowTitlePrefix = ApplicationProperties.getProperty(ApplicationMain.APPLICATION_SHORT_NAME) + " - ";

	/**
	 * LayoutManager
	 */
	private GridBagLayout gbl = new GridBagLayout();

	/**
	 * Class for easier usage of GridBagLayout
	 */
	private GridBagLayoutUtil gblt = new GridBagLayoutUtil();

	/**
	 * Label
	 */
	private JLabel lblReferrer = new JLabel(Localization.getString("Referrer") + ": ");

	/**
	 * Textfeld
	 */
	private JTextField txtReferrer = new JTextField(100);

	/**
	 * Label
	 */
	private JLabel lblTitle = new JLabel(Localization.getString("Title") + ":");

	/**
	 * Textfeld
	 */
	private JTextField txtTitle = new JTextField(100);

	/**
	 * Button
	 */
	private JButton btnTitleUpdate = new JButton(Icons.getTangoIcon("actions/view-refresh.png", 16));

	/**
	 * Label
	 */
	private JLabel lblTargetDir = new JLabel(Localization.getString("Folder") + ":");

	/**
	 * Textfeld
	 */
	private JComboBox<String> txtTargetDir = new JComboBox<>();

	/**
	 * Button
	 */
	private JButton btnTargetDir = new JButton("...");

	/**
	 * Menu
	 */
	private JPopupMenu menuTarget = new JPopupMenu("");

	/**
	 * MenuItem
	 */
	private JMenuItem itemTargetBySelection = new JMenuItem(Localization.getString("ByPathSelection"), Icons.getTangoIcon("places/folder.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemTargetByInput = new JMenuItem(Localization.getString("ByPathInput"), Icons.getTangoIcon("apps/accessories-text-editor.png", 16));

	/**
	 * Panel for display options
	 */
	private JPanel pnlDisplayOptions = new JPanel(new FlowLayout(FlowLayout.LEFT));

	/**
	 * btnShowPreviews
	 */
	private JToggleButton btnShowPreviews = new JToggleButton(Icons.getTangoIcon("mimetypes/image-x-generic.png", 16));

	/**
	 * Dummy Image, which is displayed, when no preview available
	 */
	private final BufferedImage imgDummy;
	private final ImageIcon iconDummy;

	/**
	 * Download Preview Runnable
	 */
	private DownloadPreviewRunnable previewRunnable = null;

	/**
	 * Start row of region where previews are currently loaded
	 */
	private int lastLoadedPreviewRowStart = -1;

	/**
	 * End row of region where previews are currently loaded
	 */
	private int lastLoadedPreviewRowEnd = -1;

	/**
	 * Panel
	 */
	private JPanel pnlTargetDirAuto = new JPanel(new FlowLayout(FlowLayout.CENTER));

	private JLabel lblTargetDirAuto = new JLabel(Icons.getTangoIcon("emblems/emblem-favorite.png", 16));

	/**
	 * Checkbox
	 */
	private JCheckBox cbTargetDirAuto;

	/**
	 * Buttongroup
	 */
	private ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * RadioButton
	 */
	private JRadioButton rbTitle = new JRadioButton(Localization.getString("FromTitle"), true);

	/**
	 * RadioButton
	 */
	private JRadioButton rbFilename = new JRadioButton(Localization.getString("FromFilename"), false);

	/**
	 * Button
	 */
	private JButton btnSearchAgain = new JButton(Localization.getString("SearchAgain"), Icons.getTangoIcon("actions/system-search.png", 16));

	/**
	 * Button
	 */
	private JButton btnNewKeyword = new JButton(Localization.getString("NewKeyword"), Icons.getTangoIcon("actions/document-new.png", 16));

	/**
	 * Tablemodel
	 */
	private AdderTableModel model = new AdderTableModel();

	/**
	 * Table Row Sorter
	 */
	private TableRowSorter<AdderTableModel> sorter = new TableRowSorter<>(model);

	/**
	 * Table
	 */
	private JTable jtAdder = new JTable(model);

	/**
	 * Table Column Hider
	 */
	private TableColumnHider tableColumnHider = new TableColumnHiderSizeBasedImpl(jtAdder);

	/**
	 * Table Header Column Selector
	 */
	private TableHeaderColumnSelector tableHeaderColumnSelector = new TableHeaderColumnSelector(tableColumnHider, jtAdder);

	/**
	 * Scrollpane
	 */
	private JScrollPane jsp = new JScrollPane(jtAdder);

	/**
	 * Default Row Height (When no previews are displayed)
	 */
	private final int defaultRowHeight;

	/**
	 * Preview Height
	 */
	private final int previewHeight;

	/**
	 * TableRowRenderer
	 */
	private AdderColorRowRenderer crr = new AdderColorRowRenderer();

	/**
	 * ProgressBar
	 */
	private JProgressBar pg = new JProgressBar(0, 100);

	/**
	 * Panel
	 */
	private JPanel pnlSelect = new JPanel();

	/**
	 * Button
	 */
	private JButton btnSelectAll = new JButton("", Icons.getTangoIcon("actions/edit-select-all.png", 16));

	/**
	 * Button
	 */
	private JButton btnSelectNothing = new JButton("", Icons.getTangoIcon("actions/edit-clear.png", 16));

	/**
	 * Button
	 */
	private JButton btnSelectOther = new JButton("", Icons.getTangoIcon("actions/view-refresh.png", 16));

	/**
	 * Panel
	 */
	private JPanel pnlPath = new JPanel();

	/**
	 * Button
	 */
	private JButton btnAddTitle = new JButton(Localization.getString("AddTitle"), Icons.getTangoIcon("actions/list-add.png", 16));

	/**
	 * ComboBox
	 */
	private JComboBox<String> cbAdd = new JComboBox<>();

	/**
	 * Button
	 */
	private JButton btnAdd = new JButton("", Icons.getTangoIcon("actions/list-add.png", 16));

	/**
	 * Button
	 */
	private JButton btnImportIrada = new JButton(Localization.getString("ImportIrada"));

	/**
	 * Panel
	 */
	private JPanel pnlOKCancel = new JPanel();

	/**
	 * Button
	 */
	private JButton btnOK = new JButton(Localization.getString("OK"));

	/**
	 * Button
	 */
	private JButton btnCancel = new JButton(Localization.getString("Cancel"));

	/**
	 * PopupMenu
	 */
	private JPopupMenu popupMenu = new JPopupMenu();

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemSelectKeyword = new JMenuItem(Localization.getString("SelectKeyword"), Icons.getTangoIcon("emblems/emblem-favorite.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemChangeTargetfilename = new JMenuItem(Localization.getString("ChangeTargetFilename"), Icons.getTangoIcon("apps/accessories-text-editor.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemChangeTargetByInput = new JMenuItem(Localization.getString("ChangeTargetByInput"), Icons.getTangoIcon("apps/accessories-text-editor.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemChangeTargetBySelection = new JMenuItem(Localization.getString("ChangeTargetBySelection"), Icons.getTangoIcon("apps/accessories-text-editor.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemCopyURL = new JMenuItem(Localization.getString("CopyURL"), Icons.getTangoIcon("actions/edit-copy.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemOpenURL = new JMenuItem(Localization.getString("OpenURL"), Icons.getTangoIcon("apps/internet-web-browser.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemSelect = new JMenuItem(Localization.getString("Select"), Icons.getTangoIcon("actions/edit-select-all.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemDeselect = new JMenuItem(Localization.getString("Deselect"), Icons.getTangoIcon("actions/edit-clear.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemSelectOther = new JMenuItem(Localization.getString("SelectOther"), Icons.getTangoIcon("actions/view-refresh.png", 16));

	/**
	 * HostURLCheckerRunnable
	 */
	private HostURLCheckerRunnable huc = null;

	private AdderHostURLCheckerListener hostURLCheckerListener = new AdderHostURLCheckerListener();

	/**
	 * KeywordSearchThread
	 */
	private KeywordSearchThread kst = null;

	private AdderKeywordSearchThreadListener keywordSearchThreadListener = new AdderKeywordSearchThreadListener();

	/**
	 * alreadyDownloadedLinksCount
	 */
	private int alreadyDownloadedLinksCount = 0;

	/**
	 * URLs
	 */
	private final URLList urlList;

	/**
	 * Local files
	 */
	private final boolean localFiles;

	/**
	 * Log Manager
	 */
	private final LogManager logManager;

	/**
	 * Queue Manager
	 */
	private final QueueManager queueManager;

	/**
	 * Keyword Manager
	 */
	private final KeywordManager keywordManager;

	/**
	 * Proxy Manager
	 */
	private final ProxyManager proxyManager;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Host Manager
	 */
	private final HostManager hostManager;

	/**
	 * Clipboard Observer
	 */
	private final ClipboardObserver clipboardObserver;

	/**
	 * Constructor
	 * 
	 * @param parent Parent Component
	 * @param urlList URLList
	 * @param logManager Log Manager
	 * @param queueManager Queue Manager
	 * @param keywordManager Keyword Manager
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param hostManager Host Manager
	 * @param clipboardObserver Clipboard Observer
	 */
	public AdderWindow(Component parent, URLList urlList, LogManager logManager, QueueManager queueManager, KeywordManager keywordManager, ProxyManager proxyManager, SettingsManager settingsManager,
			HostManager hostManager, ClipboardObserver clipboardObserver) {
		this(parent, false, urlList, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver);
	}

	/**
	 * Constructor
	 * 
	 * @param parent Parent Component
	 * @param localFiles Local files
	 * @param urlList URLList
	 * @param logManager Log Manager
	 * @param queueManager Queue Manager
	 * @param keywordManager Keyword Manager
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param hostManager Host Manager
	 * @param clipboardObserver Clipboard Observer
	 */
	public AdderWindow(Component parent, boolean localFiles, URLList urlList, LogManager logManager, QueueManager queueManager, KeywordManager keywordManager, ProxyManager proxyManager,
			SettingsManager settingsManager, HostManager hostManager, ClipboardObserver clipboardObserver) {
		super(ApplicationProperties.getProperty(ApplicationMain.APPLICATION_SHORT_NAME) + " - " + Localization.getString("DownloadSelection"));
		this.localFiles = localFiles;
		this.urlList = urlList;
		this.logManager = logManager;
		this.queueManager = queueManager;
		this.keywordManager = keywordManager;
		this.proxyManager = proxyManager;
		this.settingsManager = settingsManager;
		this.hostManager = hostManager;
		this.clipboardObserver = clipboardObserver;

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setIconImage(BHIcons.getBHMultiResImage("BH.png"));
		setLayout(gbl);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				cleanUp();
			}
		});

		previewCache.addListener(previewCacheListener);

		int configuredPreviewHeight = settingsManager.getGUISettings().getPreviewSize();
		previewHeight = Integer.max(MINIMUM_PREVIEW_HEIGHT, configuredPreviewHeight);
		imgDummy = ImageUtil.generatePreviewImage(BHIcons.getBHImage("Dummy.png", 16), -1, previewHeight);
		iconDummy = new ImageIcon(imgDummy);

		jsp.getVerticalScrollBar().addAdjustmentListener(new PreviewsAdjustmentListener());

		TableUtil.internationalizeColumns(jtAdder);

		int selectionTableHeaderWidth = TableUtil.calculateColumnHeaderWidth(jtAdder, jtAdder.getColumn("Selection"), 2);
		jtAdder.getColumn("Selection").setMaxWidth(selectionTableHeaderWidth);
		jtAdder.getColumn("Selection").setPreferredWidth(selectionTableHeaderWidth);
		jtAdder.getColumn("Selection").setResizable(false);

		int blacklistTableHeaderWidth = TableUtil.calculateColumnHeaderWidth(jtAdder, jtAdder.getColumn("Blacklist"), 2);
		jtAdder.getColumn("Blacklist").setMaxWidth(blacklistTableHeaderWidth);
		jtAdder.getColumn("Blacklist").setPreferredWidth(blacklistTableHeaderWidth);
		jtAdder.getColumn("Blacklist").setResizable(false);

		jtAdder.getColumn("Preview").setMaxWidth(previewHeight);
		jtAdder.getColumn("Preview").setPreferredWidth(previewHeight);
		jtAdder.getColumn("Preview").setResizable(false);

		updateColWidthsFromSettingsManager();
		jtAdder.getColumnModel().addColumnModelListener(new AdderColumnListener());

		tableColumnHider.hideColumn("Thumb");
		tableColumnHider.hideColumn("FilenameOverride");
		tableColumnHider.hideColumn("TargetFolderOverride");
		tableColumnHider.hideColumn("TargetFolderOverrideValue");
		if (!localFiles) {
			/*
			 * Delete file is only available when sorting files
			 */
			tableColumnHider.hideColumn("DeleteFile");
		}
		tableColumnHider.hideColumn("LastModified");
		tableColumnHider.hideColumn("Host");
		tableColumnHider.hideColumn("Preview");
		tableColumnHider.hideColumn("Keyword");
		tableColumnHider.hideColumn("AlreadyDownloaded");

		tableHeaderColumnSelector.addColumn("Thumb");
		tableHeaderColumnSelector.addColumn("FilenameOverride");
		tableHeaderColumnSelector.addColumn("LastModified");
		tableHeaderColumnSelector.addColumn("Host");
		tableHeaderColumnSelector.addColumn("TargetFolderOverride");
		tableHeaderColumnSelector.addColumn("TargetFolderOverrideValue");

		jtAdder.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);

		jtAdder.setRowSorter(sorter);
		jtAdder.setDefaultRenderer(Object.class, crr);
		DefaultBooleanColorRowRenderer dbcrr = new DefaultBooleanColorRowRenderer();
		jtAdder.getColumn("Selection").setCellRenderer(dbcrr);
		jtAdder.getColumn("Blacklist").setCellRenderer(dbcrr);
		jtAdder.getColumn("FilenameOverride").setCellRenderer(dbcrr);
		jtAdder.getColumn("TargetFolderOverride").setCellRenderer(dbcrr);
		jtAdder.getColumn("DeleteFile").setCellRenderer(dbcrr);
		jtAdder.getColumn("LastModified").setCellRenderer(new DefaultNumberColorRowRenderer());

		jtAdder.getTableHeader().setReorderingAllowed(false);

		jtAdder.setRowHeight(TableUtil.calculateRowHeight(jtAdder, false, true));
		defaultRowHeight = jtAdder.getRowHeight();

		jtAdder.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					invertSelection(true);
				}
			}
		});

		jtAdder.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger() && jtAdder.getSelectedRowCount() > 0) {
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger() && jtAdder.getSelectedRowCount() > 0) {
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		popupMenu.add(menuItemSelectKeyword);
		popupMenu.add(menuItemChangeTargetfilename);
		popupMenu.add(menuItemChangeTargetBySelection);
		popupMenu.add(menuItemChangeTargetByInput);
		popupMenu.add(menuItemCopyURL);
		popupMenu.add(menuItemOpenURL);
		popupMenu.add(menuItemSelect);
		popupMenu.add(menuItemDeselect);
		popupMenu.add(menuItemSelectOther);
		menuItemSelectKeyword.addActionListener(this);
		menuItemChangeTargetfilename.addActionListener(this);
		menuItemChangeTargetBySelection.addActionListener(this);
		menuItemChangeTargetByInput.addActionListener(this);
		menuItemCopyURL.addActionListener(this);
		menuItemOpenURL.addActionListener(this);
		menuItemSelect.addActionListener(this);
		menuItemDeselect.addActionListener(this);
		menuItemSelectOther.addActionListener(this);

		txtReferrer.setEditable(false);
		btnTitleUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateAllPathColumns();
			}
		});
		txtTargetDir.setEditable(false);
		txtTargetDir.addItem(settingsManager.getSavePath());
		for (String strAdd : settingsManager.getGUISettings().getTargetDirChangeHistory()) {
			txtTargetDir.addItem(strAdd);
		}
		txtTargetDir.setSelectedIndex(0);
		txtTargetDir.addActionListener(this);
		btnTargetDir.addActionListener(this);
		itemTargetBySelection.addActionListener(this);
		itemTargetByInput.addActionListener(this);
		menuTarget.add(itemTargetBySelection);
		menuTarget.add(itemTargetByInput);

		if (settingsManager.getGUISettings().isDownloadPreviews()) {
			pnlDisplayOptions.add(btnShowPreviews);
		}

		boolean autoTargetDirByTitle = settingsManager.getDirectorySettings().getAutoTargetDirMode() == AutoTargetDirMode.BY_TITLE;
		rbTitle.setSelected(autoTargetDirByTitle);
		rbFilename.setSelected(!autoTargetDirByTitle);

		cbTargetDirAuto = new JCheckBox(Localization.getString("AutomaticRecognition"), settingsManager.getDirectorySettings().isAutoTargetDir());
		boolean autoTargetDirEnabled = cbTargetDirAuto.isSelected();
		rbTitle.setEnabled(autoTargetDirEnabled);
		rbFilename.setEnabled(autoTargetDirEnabled);
		lblTargetDirAuto.setEnabled(autoTargetDirEnabled);
		btnSearchAgain.setEnabled(false);
		btnNewKeyword.setEnabled(false);

		buttonGroup.add(rbTitle);
		buttonGroup.add(rbFilename);

		cbTargetDirAuto.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean b = cbTargetDirAuto.isSelected();
				settingsManager.getDirectorySettings().setAutoTargetDir(b);
				settingsManager.writeSettings(true);
				lblTargetDirAuto.setEnabled(b);
				rbTitle.setEnabled(b);
				rbFilename.setEnabled(b);
				btnSearchAgain.setEnabled(b);
				btnNewKeyword.setEnabled(b);

				if (b) {
					searchForKeywords();
				} else {
					clearAllKeywords();
				}
			}
		});
		rbTitle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (rbTitle.isSelected()) {
					settingsManager.getDirectorySettings().setAutoTargetDirMode(AutoTargetDirMode.BY_TITLE);
					settingsManager.writeSettings(true);
				}
			}
		});
		rbFilename.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (rbFilename.isSelected()) {
					settingsManager.getDirectorySettings().setAutoTargetDirMode(AutoTargetDirMode.BY_FILENAME);
					settingsManager.writeSettings(true);
				}
			}
		});

		pnlTargetDirAuto.add(lblTargetDirAuto);
		pnlTargetDirAuto.add(cbTargetDirAuto);
		pnlTargetDirAuto.add(rbTitle);
		pnlTargetDirAuto.add(rbFilename);
		pnlTargetDirAuto.add(btnSearchAgain);
		pnlTargetDirAuto.add(btnNewKeyword);
		if (settingsManager.getGUISettings().isDownloadPreviews()) {
			btnShowPreviews.addActionListener(this);
		}
		btnSearchAgain.addActionListener(this);
		btnNewKeyword.addActionListener(this);

		btnSelectAll.setToolTipText(Localization.getString("SelectAll"));
		btnSelectNothing.setToolTipText(Localization.getString("SelectNothing"));
		btnSelectOther.setToolTipText(Localization.getString("SelectOther"));
		btnSelectAll.addActionListener(this);
		btnSelectNothing.addActionListener(this);
		btnSelectOther.addActionListener(this);
		pnlSelect.setLayout(new BoxLayout(pnlSelect, BoxLayout.LINE_AXIS));
		pnlSelect.add(new JLabel(Localization.getString("Selection") + ":"));
		pnlSelect.add(Box.createRigidArea(new Dimension(5, 0)));
		pnlSelect.add(btnSelectAll);
		pnlSelect.add(Box.createRigidArea(new Dimension(5, 0)));
		pnlSelect.add(btnSelectNothing);
		pnlSelect.add(Box.createRigidArea(new Dimension(5, 0)));
		pnlSelect.add(btnSelectOther);

		for (String strAdd : settingsManager.getGUISettings().getDownloadSelectionAddStrings()) {
			cbAdd.addItem(strAdd);
		}
		cbAdd.setEditable(true);
		cbAdd.setPrototypeDisplayValue("XXXXXXXXXX");
		btnAddTitle.addActionListener(this);
		btnAdd.addActionListener(this);
		btnImportIrada.addActionListener(this);
		pnlPath.setLayout(new BoxLayout(pnlPath, BoxLayout.LINE_AXIS));
		pnlPath.add(new JLabel(Localization.getString("TargetFolder") + ":"));
		pnlPath.add(Box.createRigidArea(new Dimension(5, 0)));
		pnlPath.add(btnAddTitle);
		pnlPath.add(Box.createRigidArea(new Dimension(15, 0)));
		pnlPath.add(cbAdd);
		pnlPath.add(Box.createRigidArea(new Dimension(2, 0)));
		pnlPath.add(btnAdd);
		pnlPath.add(Box.createRigidArea(new Dimension(15, 0)));
		pnlPath.add(btnImportIrada);

		JPanel pnlActions = new JPanel(new BorderLayout(10, 0));
		pnlActions.add(pnlSelect, BorderLayout.WEST);
		pnlActions.add(pnlPath, BorderLayout.CENTER);

		btnOK.addActionListener(this);
		btnOK.setDefaultCapable(true);
		btnCancel.setMnemonic(KeyEvent.VK_C);
		btnCancel.addActionListener(this);
		pnlOKCancel.add(btnOK);
		pnlOKCancel.add(btnCancel);

		GridBagConstraints gbc = new GridBagConstraints();
		int i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToFrame(gbl, gbc, lblReferrer, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToFrame(gbl, gbc, txtReferrer, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToFrame(gbl, gbc, lblTitle, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToFrame(gbl, gbc, txtTitle, this);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToFrame(gbl, gbc, btnTitleUpdate, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToFrame(gbl, gbc, lblTargetDir, this);
		gbc = gblt.getGBC(1, i, 1, 1, 1.0, 0.0);
		GridBagLayoutUtil.addItemToFrame(gbl, gbc, txtTargetDir, this);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToFrame(gbl, gbc, btnTargetDir, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToFrame(gbl, gbc, pnlDisplayOptions, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToFrame(gbl, gbc, pnlTargetDirAuto, this);
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToFrame(gbl, gbc, new JLabel(), this);
		i++;
		gbc = gblt.getGBC(0, i, 3, 1, 1.0, 1.0);
		GridBagLayoutUtil.addItemToFrame(gbl, gbc, jsp, this);
		i++;
		gbc = gblt.getGBC(0, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToFrame(gbl, gbc, pnlActions, this);
		i++;
		gbc = gblt.getGBC(0, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToFrame(gbl, gbc, pg, this);
		i++;
		gbc = gblt.getGBC(0, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToFrame(gbl, gbc, pnlOKCancel, this);

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtReferrer);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtTitle);

		setComponentsEnabled(false, false);

		// Enter, Escape and F2 (before setVisible(true)!)
		ActionMap am = this.getRootPane().getActionMap();
		InputMap im = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		Object windowCloseKey = new Object();
		Object windowOkKey = new Object();
		Object changeTargetFilenameKey = new Object();
		KeyStroke windowCloseStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		Action windowCloseAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				btnCancel.doClick();
			}
		};
		KeyStroke windowOkStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		Action windowOkAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				btnOK.doClick();
			}
		};
		KeyStroke changeTargetFilenameStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
		Action changeTargetFilenameAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				actionChangeTargetFilename();
			}
		};
		im.put(changeTargetFilenameStroke, changeTargetFilenameKey);
		am.put(changeTargetFilenameKey, changeTargetFilenameAction);
		this.jtAdder.getActionMap().put(changeTargetFilenameKey, changeTargetFilenameAction);
		this.jtAdder.getInputMap().put(changeTargetFilenameStroke, changeTargetFilenameKey);
		im.put(windowCloseStroke, windowCloseKey);
		am.put(windowCloseKey, windowCloseAction);
		im.put(windowOkStroke, windowOkKey);
		am.put(windowOkKey, windowOkAction);

		WindowSettings mainWindowSettings = settingsManager.getGUISettings().getMainWindow();
		if (mainWindowSettings.isSave() && mainWindowSettings.getWidth() > 0 && mainWindowSettings.getHeight() > 0) {
			this.setSize(mainWindowSettings.getWidth(), mainWindowSettings.getHeight());
			this.setLocation(mainWindowSettings.getX(), mainWindowSettings.getY());
		} else {
			pack();
			setLocationRelativeTo(parent);
		}

		addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
				// Nothing to do
			}

			@Override
			public void componentResized(ComponentEvent e) {
				WindowSettings mainWindowSettings = settingsManager.getGUISettings().getMainWindow();
				mainWindowSettings.setWidth(getWidth());
				mainWindowSettings.setHeight(getHeight());
				settingsManager.writeSettings(true);
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				WindowSettings mainWindowSettings = settingsManager.getGUISettings().getMainWindow();
				mainWindowSettings.setX(getX());
				mainWindowSettings.setY(getY());
				settingsManager.writeSettings(true);
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				// Nothing to do
			}
		});

		btnOK.requestFocusInWindow();
		setVisible(true);
	}

	/**
	 * Initialize (Remove duplicates, Check links, ...)
	 */
	public void init() {
		String title = urlList.getTitle();
		RegexReplacePipeline regexPipe = settingsManager.getRegexReplacePipelinePageTitle();
		txtTitle.setText(regexPipe.getReplacedPageTitle(title));

		txtReferrer.setText(urlList.getReferrer());

		ProgressObserver progress = new ProgressObserver();
		progress.addProgressListener(new AdderProgressListener());

		setComponentsEnabled(false, false);
		progress.progressChanged(true);
		progress.progressChanged(0, urlList.getUrls().size(), 0);
		progress.progressChanged(Localization.getString("CheckingLinks") + "...");
		int urlCount = 0;
		for (URL url : urlList.getUrls()) {
			url.findHostForURL(hostManager);
			urlCount++;
			progress.progressChanged(urlCount);
		}
		progress.progressChanged(false);

		setComponentsEnabled(false, false);
		HostManager.removeDuplicates(urlList.getUrls(), progress, settingsManager);

		this.setPGText(Localization.getString("CheckingLinks") + "...");
		this.setPGEnabled(true);
		setComponentsEnabled(false, true);
		huc = new HostURLCheckerRunnable(urlList, hostManager);
		huc.addHMListener(hostURLCheckerListener);
		huc.checkURLs();
	}

	/**
	 * Clean up resources, when dialog is not needed anymore
	 */
	private void cleanUp() {
		previewCache.removeListener(previewCacheListener);

		if (this.huc != null) {
			this.huc.stopAdding();
		}

		if (kst != null) {
			kst.removeKeywordSearchThreadListener(keywordSearchThreadListener);
			kst = null;
		}

		if (previewRunnable != null) {
			previewRunnable.stop();
		}

		imgDummy.flush();
	}

	/**
	 * Adds links to table, searching for keywords, ...
	 * 
	 * @param urls URLs
	 */
	public void setData(List<URL> urls) {
		setComponentsEnabled(false, false);

		// Search for blacklisted links
		logManager.searchBlacklist(urls, this);

		// Search for already downloaded links
		logManager.searchLogs(urls, this);

		// URLs without blacklisted URLs
		final List<URL> clearedUpURLs = new ArrayList<>();

		List<Pic> queue = queueManager.getQueue();
		for (URL url : urls) {
			if (url.isBlacklisted()) {
				continue;
			} else if (!url.isAlreadyDownloaded()) {
				for (Pic pic : queue) {
					if (pic.getContainerURL().equals(url.getURL())) {
						url.setAlreadyDownloaded(true);
						break;
					}
				}
			}
			clearedUpURLs.add(url);
		}

		setPGVisible(true);
		setPGText(Localization.getString("AddingLinks") + "...");
		setPGEnabled(true);

		alreadyDownloadedLinksCount = 0;

		if (settingsManager.getGUISettings().isDownloadPreviews()) {
			previewRunnable = new DownloadPreviewRunnable(previewCache, previewHeight, clearedUpURLs);
			Thread previewThread = new Thread(previewRunnable);
			previewThread.setName("Preview-Download-Wait" + previewThread.threadId());
			previewThread.start();
		}

		for (URL url : clearedUpURLs) {
			Object[] data = new Object[15];
			data[0] = Boolean.valueOf(!url.isAlreadyDownloaded());
			data[1] = url.getURL();
			String filename = BHUtil.filterFilename(url.getFilenameCorrected(), settingsManager);
			filename = filename.replace("%20", " ");
			data[2] = filename;
			if (url.getTargetPath() != null && !url.getTargetPath().isEmpty()) {
				data[3] = url.getTargetPath();
				data[13] = Boolean.valueOf(true);
				data[14] = url.getTargetPath();
			} else {
				data[3] = txtTargetDir.getSelectedItem();
				data[13] = Boolean.valueOf(false);
				data[14] = null;
			}
			data[4] = url.getThumb();
			data[5] = Boolean.valueOf(false);
			data[6] = Boolean.valueOf(false);
			data[7] = Long.valueOf(0);
			data[8] = url.getHost();
			data[9] = iconDummy;
			data[10] = Boolean.valueOf(false);
			data[11] = null;
			data[12] = url.isAlreadyDownloaded();

			model.addRow(data);

			if (url.isAlreadyDownloaded()) {
				alreadyDownloadedLinksCount++;
			}
		}

		if (model.getRowCount() > 0) {
			model.fireTableRowsUpdated(0, model.getRowCount() - 1);
		} else if (model.getRowCount() == 0) {
			setPGEnabled(false);
			setPGVisible(false);
			this.toFront();
			JOptionPane.showMessageDialog(this, Localization.getString("NoPicsFound"), "Error", JOptionPane.ERROR_MESSAGE);
			this.dispose();
			if (previewRunnable != null) {
				previewRunnable.stop();
			}
			return;
		}

		urls.clear();

		this.setTitle(windowTitlePrefix + Localization.getString("DownloadSelection") + " - " + createTableSummaryString());

		setComponentsEnabled(false, true);
		if (cbTargetDirAuto.isSelected()) {
			searchForKeywords();
		} else {
			updateAllPathColumns();
			setPGEnabled(false);
			setPGVisible(false);
			setComponentsEnabled(true, true);
		}

		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				setTitle(windowTitlePrefix + Localization.getString("DownloadSelection") + " - " + createTableSummaryString());
			}
		});

		this.toFront();
		this.requestFocus();
	}

	private void addLinksToQueue() {
		if (previewRunnable != null) {
			previewRunnable.stop();
		}

		setComponentsEnabled(false, false);

		int rowCount = jtAdder.getRowCount();
		pg.setVisible(true);
		pg.setValue(0);
		pg.setMaximum(rowCount);

		String progressBarTextFormat = Localization.getString("AddingURLs") + "... %d%%";
		setPGText(String.format(progressBarTextFormat, 0));

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				int progressCounter = 0;
				int progressChangeInterval = rowCount / 100;
				if (progressChangeInterval <= 0) {
					progressChangeInterval = 1;
				}
				String referrer = txtReferrer.getText();

				int selectionColumnModelIndex = jtAdder.getColumn("Selection").getModelIndex();
				int urlColumnModelIndex = jtAdder.getColumn("URL").getModelIndex();
				int filenameColumnModelIndex = jtAdder.getColumn("Filename").getModelIndex();
				int filenameOverrideColumnModelIndex = jtAdder.getColumn("FilenameOverride").getModelIndex();
				int folderColumnModelIndex = jtAdder.getColumn("TargetFolder").getModelIndex();
				int thumbnailColumnModelIndex = jtAdder.getColumn("Thumb").getModelIndex();
				int deleteColumnModelIndex = jtAdder.getColumn("DeleteFile").getModelIndex();
				int lastModifiedColumnModelIndex = jtAdder.getColumn("LastModified").getModelIndex();
				int blacklistColumnModelIndex = jtAdder.getColumn("Blacklist").getModelIndex();
				int hostColumnModelIndex = jtAdder.getColumn("Host").getModelIndex();

				List<Pic> picsToAdd = new ArrayList<>();
				for (int i = 0; i < rowCount; i++) {
					int rowModelIndex = jtAdder.convertRowIndexToModel(i);

					boolean bBlacklist = (boolean)model.getValueAt(rowModelIndex, blacklistColumnModelIndex);
					boolean bDownload = (boolean)model.getValueAt(rowModelIndex, selectionColumnModelIndex);
					String url = (String)model.getValueAt(rowModelIndex, urlColumnModelIndex);

					if (bBlacklist) {
						logManager.addUrlToBlacklist(url);
					} else if (bDownload) {
						if (localFiles && (boolean)model.getValueAt(rowModelIndex, deleteColumnModelIndex)) {
							/*
							 * When sorting files it is possible to mark files to be deleted
							 */
							File f = new File(url);
							try {
								Files.deleteIfExists(f.toPath());
							} catch (IOException e) {
								logger.error("File could not be deleted: {}", f);
							}
						} else {
							String filenameCorrected = (String)model.getValueAt(rowModelIndex, filenameColumnModelIndex);
							String targetDir = (String)model.getValueAt(rowModelIndex, folderColumnModelIndex);
							Pic p = new Pic(url, filenameCorrected, targetDir);
							p.setThumb((String)model.getValueAt(rowModelIndex, thumbnailColumnModelIndex));
							boolean bFixedFilename = (Boolean)model.getValueAt(rowModelIndex, filenameOverrideColumnModelIndex);
							p.setFixedTargetFilename(bFixedFilename);
							p.setThreadURL(referrer);
							Object oMod = model.getValueAt(rowModelIndex, lastModifiedColumnModelIndex);
							if (oMod instanceof Long) {
								long lMod = (long)oMod;
								if (lMod > 0) {
									p.setLastModified(lMod);
									p.setFixedLastModified(true);
								}
							}
							p.setHoster((Hoster)model.getValueAt(rowModelIndex, hostColumnModelIndex));
							picsToAdd.add(p);
						}
					}

					progressCounter++;
					if (progressCounter >= progressChangeInterval) {
						progressCounter = 0;
						pg.setValue(i + 1);
						setPGText(String.format(progressBarTextFormat, (i + 1) * 100 / rowCount));
					}
				}
				queueManager.addPics(picsToAdd);

				dispose();
				queueManager.saveDatabase();
			}
		});
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	private void loadAndUnloadPreviews(boolean force) {
		// Get the first visible row
		JViewport viewport = jsp.getViewport();
		Point p = viewport.getViewPosition();
		int firstVisibleRowIndex = jtAdder.rowAtPoint(p);
		if (firstVisibleRowIndex < 0) {
			return;
		}

		// Get count of visible rows
		int visibleRowCount = viewport.getSize().height / previewHeight;

		// Get middle visible row
		int middleVisibleRowIndex = firstVisibleRowIndex + visibleRowCount / 2;

		// Calculate row region to load previews for
		int firstLoadedRowIndex = middleVisibleRowIndex - MAX_VISIBLE_PREVIEWS_PER_DIRECTION;
		int lastLoadedRowIndex = middleVisibleRowIndex + MAX_VISIBLE_PREVIEWS_PER_DIRECTION;
		if (firstLoadedRowIndex < 0) {
			firstLoadedRowIndex = 0;
		}
		if (lastLoadedRowIndex >= jtAdder.getRowCount()) {
			lastLoadedRowIndex = jtAdder.getRowCount() - 1;
		}

		int minFirstLoaded = lastLoadedPreviewRowStart - NO_LOAD_ROW_DISTANCE;
		int maxFirstLoaded = lastLoadedPreviewRowStart + NO_LOAD_ROW_DISTANCE;
		int minLastLoaded = lastLoadedPreviewRowEnd - NO_LOAD_ROW_DISTANCE;
		int maxLastLoaded = lastLoadedPreviewRowEnd + NO_LOAD_ROW_DISTANCE;
		// If not enough was scrolled, we don't need to load anything
		if (!force && lastLoadedPreviewRowStart > -1 && firstLoadedRowIndex >= minFirstLoaded && firstLoadedRowIndex <= maxFirstLoaded && lastLoadedRowIndex >= minLastLoaded
				&& lastLoadedRowIndex <= maxLastLoaded) {
			return;
		}

		lastLoadedPreviewRowStart = firstLoadedRowIndex;
		lastLoadedPreviewRowEnd = lastLoadedRowIndex;

		int firstModelIndex = jtAdder.convertRowIndexToModel(firstLoadedRowIndex);
		int lastModelIndex = jtAdder.convertRowIndexToModel(lastLoadedRowIndex);

		for (int i = 0; i < model.getRowCount(); i++) {
			Object val = model.getValueAt(i, 9);
			ImageIcon icon = val instanceof ImageIcon imageIcon ? imageIcon : null;
			Image img = icon != null ? icon.getImage() : null;
			if (i >= firstModelIndex && i <= lastModelIndex) {
				BufferedImage preview = previewCache.getPreview((String)model.getValueAt(i, 4));
				if (preview != null) {
					model.setValueAt(new ImageIcon(preview), i, 9);
				}
			} else {
				if (icon != iconDummy && img != null) {
					img.flush();
					model.setValueAt(iconDummy, i, 9);
				}
			}
		}
	}

	/**
	 * Show/Hide Progressbar
	 * 
	 * @param b Visible
	 * @param min Minimum
	 * @param max Maximum
	 * @param val Value
	 */
	public void setPGEnabled(final boolean b, final int min, final int max, final int val) {
		if (EventQueue.isDispatchThread()) {
			pg.setVisible(b);
			pg.setIndeterminate(false);
			pg.setMinimum(min);
			pg.setMaximum(max);
			pg.setValue(val);
		} else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					pg.setVisible(b);
					pg.setIndeterminate(false);
					pg.setMinimum(min);
					pg.setMaximum(max);
					pg.setValue(val);
				}
			});
		}
	}

	/**
	 * Show/Hide indeterminate Progressbar
	 * 
	 * @param b Visible
	 */
	public void setPGEnabled(final boolean b) {
		if (EventQueue.isDispatchThread()) {
			pg.setVisible(b);
			pg.setIndeterminate(b);
		} else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					pg.setVisible(b);
					pg.setIndeterminate(b);
				}
			});
		}
	}

	/**
	 * Change progressbar value
	 * 
	 * @param val Value
	 */
	public void setPGValue(final int val) {
		if (EventQueue.isDispatchThread()) {
			pg.setValue(val);
		} else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					pg.setValue(val);
				}
			});
		}
	}

	/**
	 * Change progressbar text
	 * 
	 * @param s Text
	 */
	public void setPGText(final String s) {
		if (EventQueue.isDispatchThread()) {
			pg.setStringPainted(true);
			pg.setString(s);
		} else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					pg.setStringPainted(true);
					pg.setString(s);
				}
			});
		}
	}

	/**
	 * Show/Hide Progressbar
	 * 
	 * @param b Visible
	 */
	public void setPGVisible(final boolean b) {
		if (EventQueue.isDispatchThread()) {
			pg.setVisible(b);
		} else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					pg.setVisible(b);
				}
			});
		}
	}

	/**
	 * Lock/Unlock Components
	 * 
	 * @param b Enabled (Unlocked)
	 * @param cancel Unlock Cancel-Button
	 */
	private void setComponentsEnabled(boolean b, boolean cancel) {
		btnOK.setEnabled(b);
		btnCancel.setEnabled(cancel);
		btnSelectAll.setEnabled(b);
		btnSelectNothing.setEnabled(b);
		btnSelectOther.setEnabled(b);

		cbTargetDirAuto.setEnabled(b);
		boolean bAuto = cbTargetDirAuto.isSelected() && b;
		lblTargetDirAuto.setEnabled(bAuto);
		rbFilename.setEnabled(bAuto);
		rbTitle.setEnabled(bAuto);
		boolean bSearchAgain = bAuto;
		if (btnSearchAgain.getText().equals(Localization.getString("CancelSearch"))) {
			bSearchAgain = true;
		}
		btnSearchAgain.setEnabled(bSearchAgain);
		btnNewKeyword.setEnabled(bAuto);

		jsp.setEnabled(b);
		jtAdder.setEnabled(b);
		txtTitle.setEnabled(b);
		btnTitleUpdate.setEnabled(b);
		btnAddTitle.setEnabled(b);
		cbAdd.setEnabled(b);
		btnAdd.setEnabled(b);
		txtTargetDir.setEnabled(b);
		btnTargetDir.setEnabled(b);
		btnImportIrada.setEnabled(b);
		btnShowPreviews.setEnabled(b);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnOK) {
			addLinksToQueue();
		} else if (e.getSource() == btnCancel) {
			if (this.huc != null) {
				this.huc.stopAdding();
				btnCancel.setEnabled(false);
				return;
			}
			this.dispose();
		} else if (e.getSource() == btnSelectAll) {
			select(false);
		} else if (e.getSource() == btnSelectNothing) {
			deselect(false);
		} else if (e.getSource() == btnSelectOther) {
			invertSelection(false);
		} else if (e.getSource() == itemTargetBySelection) {
			File file = FileDialogUtil.showFolderOpenDialog(this, (String)txtTargetDir.getSelectedItem(), null);
			if (file != null) {
				String folder = file.getAbsolutePath() + FileUtil.FILE_SEPERATOR;
				int folderIndex = getIndexForFolder(folder);
				if (folderIndex < 0) {
					txtTargetDir.addItem(folder);
					txtTargetDir.setSelectedIndex(txtTargetDir.getItemCount() - 1);
					if (!settingsManager.getGUISettings().getTargetDirChangeHistory().contains(folder)) {
						settingsManager.getGUISettings().getTargetDirChangeHistory().add(folder);
					}
					settingsManager.writeSettings(true);
				} else {
					txtTargetDir.setSelectedIndex(folderIndex);
				}
				if (settingsManager.getDirectorySettings().isRememberLastUsedPath()) {
					settingsManager.getDirectorySettings().setSavePath(folder);
					settingsManager.writeSettings(true);
				}
			}
		} else if (e.getSource() == itemTargetByInput) {
			String input = PathRenameDialog.showPathRenameDialog(this, (String)txtTargetDir.getSelectedItem());
			if ((input != null) && (input.length() > 2)) {
				if (!input.endsWith("/") && !input.endsWith("\\")) {
					input += FileUtil.FILE_SEPERATOR;
				}
				input = BHUtil.filterPath(input, settingsManager);
				input = BHUtil.reducePathLength(input, settingsManager);
				int folderIndex = getIndexForFolder(input);
				if (folderIndex < 0) {
					txtTargetDir.addItem(input);
					txtTargetDir.setSelectedIndex(txtTargetDir.getItemCount() - 1);
					settingsManager.getGUISettings().getTargetDirChangeHistory().add(input);
					settingsManager.writeSettings(true);
				} else {
					txtTargetDir.setSelectedIndex(folderIndex);
				}
				if (settingsManager.getDirectorySettings().isRememberLastUsedPath()) {
					settingsManager.getDirectorySettings().setSavePath(input);
					settingsManager.writeSettings(true);
				}
			}
		} else if (e.getSource() == btnAddTitle) {
			String title = txtTitle.getText();
			if (title.isEmpty()) {
				return;
			}
			addToPaths(title);
		} else if (e.getSource() == btnSearchAgain) {
			if (kst != null) {
				kst.removeKeywordSearchThreadListener(keywordSearchThreadListener);
				kst = null;
				btnSearchAgain.setText(Localization.getString("SearchAgain"));
				setPGEnabled(false);
			} else {
				searchForKeywords();
			}
		} else if (e.getSource() == btnNewKeyword) {
			Keyword k = AdderKeywordAddDialog.openAddKeywordDialog(this, settingsManager, txtTitle.getSelectedText());
			if (k != null) {
				keywordManager.addKeyword(k);
				searchForKeywords();
			}
		} else if (e.getSource() == btnAdd) {
			String addValue = (String)cbAdd.getSelectedItem();
			if (addValue == null || addValue.isEmpty()) {
				return;
			}
			addToPaths(addValue);

			boolean available = false;
			String strAdd = cbAdd.getSelectedItem().toString();
			for (int a = 0; a < cbAdd.getItemCount(); a++) {
				if (cbAdd.getItemAt(a).equals(strAdd)) {
					available = true;
					break;
				}
			}
			if (!available) {
				cbAdd.insertItemAt(strAdd, 0);
				settingsManager.getGUISettings().getDownloadSelectionAddStrings().add(strAdd);
				settingsManager.writeSettings(true);
			}
		} else if (e.getSource() == menuItemSelectKeyword) {
			List<Keyword> vk = keywordManager.getKeywords();
			Collections.sort(vk);
			AdderKeywordSelectorTitle aks = new AdderKeywordSelectorTitle(this, Localization.getString("SelectKeyword"), true, null, vk, false, settingsManager);
			if (aks.isOkPressed()) {
				Keyword keyword = aks.getSelectedKeyword();
				if (aks.isNewKeywordCreated()) {
					keywordManager.addKeyword(keyword);
				}

				model.setFireTableCellUpdatedEnabled(false);
				int[] selectedRows = jtAdder.getSelectedRows();
				int keywordColumnModelIndex = jtAdder.getColumn("Keyword").getModelIndex();
				for (int x = 0; x < selectedRows.length; x++) {
					model.setValueAt(keyword, jtAdder.convertRowIndexToModel(selectedRows[x]), keywordColumnModelIndex);
					updatePathColumn(selectedRows[x]);
				}
				model.setFireTableCellUpdatedEnabled(true);
				model.fireTableRowsUpdated(0, model.getRowCount() - 1);
			}
		} else if (e.getSource() == menuItemChangeTargetfilename) {
			actionChangeTargetFilename();
		} else if (e.getSource() == btnImportIrada) {
			importIrada();
		} else if (e.getSource() == menuItemChangeTargetBySelection) {
			actionChangeTargetBySelection();
		} else if (e.getSource() == menuItemChangeTargetByInput) {
			actionChangeTargetByInput();
		} else if (e.getSource() == menuItemSelect) {
			select(true);
		} else if (e.getSource() == menuItemDeselect) {
			deselect(true);
		} else if (e.getSource() == menuItemSelectOther) {
			invertSelection(true);
		} else if (e.getSource() == txtTargetDir) {
			updateAllPathColumns();
		} else if (e.getSource() == menuItemCopyURL) {
			actionCopyURLs();
		} else if (e.getSource() == menuItemOpenURL) {
			actionOpenURLs();
		} else if (e.getSource() == btnShowPreviews) {
			tableColumnHider.setVisible("Preview", btnShowPreviews.isSelected());
			int newRowHeight = btnShowPreviews.isSelected() ? previewHeight : defaultRowHeight;
			// If Default row height is bigger, then use the default. This prevents the rows from getting too small on large displays.
			jtAdder.setRowHeight(Integer.max(newRowHeight, defaultRowHeight));
		} else if (e.getSource() == btnTargetDir) {
			Point mousePosition = MouseInfo.getPointerInfo().getLocation();
			Rectangle buttonBounds = btnTargetDir.getBounds();
			buttonBounds.setLocation(btnTargetDir.getLocationOnScreen());
			if (buttonBounds.contains(mousePosition)) {
				Point mouseLocationRelativeToComp = new Point(mousePosition.x, mousePosition.y);
				SwingUtilities.convertPointFromScreen(mouseLocationRelativeToComp, btnTargetDir);
				menuTarget.show(btnTargetDir, mouseLocationRelativeToComp.x, mouseLocationRelativeToComp.y);
			} else {
				Dimension buttonSize = btnTargetDir.getSize();
				menuTarget.show(btnTargetDir, buttonSize.width / 2, buttonSize.height / 2);
			}
		}
	}

	/**
	 * Search for keywords
	 */
	private void searchForKeywords() {
		if (kst != null) {
			return;
		}
		btnSearchAgain.setText(Localization.getString("CancelSearch"));
		this.setComponentsEnabled(false, false);

		this.setPGEnabled(true);
		this.setPGText(Localization.getString("SearchForKeywords"));
		if (rbTitle.isSelected()) {
			String strSearch = txtTitle.getText();
			kst = new KeywordSearchThread(strSearch, this, false, true, localFiles, keywordManager, settingsManager);
			kst.setKeywordSearchThreadListener(keywordSearchThreadListener);
			kst.setPriority(Thread.MIN_PRIORITY);
			kst.start();
		} else {
			int urlColumnModelIndex = jtAdder.getColumn("URL").getModelIndex();
			String[] strSearchA = new String[jtAdder.getRowCount()];
			for (int i = 0; i < jtAdder.getRowCount(); i++) {
				int modelIndex = jtAdder.convertRowIndexToModel(i);
				strSearchA[i] = (String)model.getValueAt(modelIndex, urlColumnModelIndex);
			}
			kst = new KeywordSearchThread(strSearchA, this, true, false, localFiles, keywordManager, settingsManager);
			kst.setKeywordSearchThreadListener(keywordSearchThreadListener);
			kst.setPriority(Thread.MIN_PRIORITY);
			kst.start();
		}
	}

	private void keywordSearchDone(final Keyword[] retval) {
		// Update keyword and selected column
		int keywordColumnModelIndex = jtAdder.getColumn("Keyword").getModelIndex();
		int selectionColumnModelIndex = jtAdder.getColumn("Selection").getModelIndex();
		model.setFireTableCellUpdatedEnabled(false);
		for (int i = 0; i < jtAdder.getRowCount(); i++) {
			int modelIndex = jtAdder.convertRowIndexToModel(i);
			if (rbFilename.isSelected()) {
				Keyword keyword = retval[i];
				if (keyword != null) {
					model.setValueAt(keyword, modelIndex, keywordColumnModelIndex);
				} else {
					boolean deselectNoKeyword = settingsManager.getKeywordsSettings().isDeselectNoKeyword();
					boolean deleteNoKeyword = settingsManager.getKeywordsSettings().isDeleteNoKeyword() && localFiles;
					if (deselectNoKeyword && !deleteNoKeyword) {
						model.setValueAt(false, modelIndex, selectionColumnModelIndex);
					}
					/*
					 * In the GUI there is no Setting anymore to define this option.
					 * In the SettingsManager the option still exists, but i don't know why.
					 * So i think it is the best way, to remove the functionality, that this option has.
					 * Especially that this option can delete files on the harddisk of the users.
					 */
					/*
					 * if (deleteNoKeyword) {
					 * model.setValueAt(true, i, 6);
					 * model.fireTableCellUpdated(i, 6);
					 * }
					 */
				}
			} else {
				Keyword keyword = retval[0];
				if (keyword != null) {
					model.setValueAt(keyword, modelIndex, keywordColumnModelIndex);
				}
			}
			updatePathColumn(i);
		}
		model.setFireTableCellUpdatedEnabled(true);
		model.fireTableRowsUpdated(0, model.getRowCount() - 1);

		kst.removeKeywordSearchThreadListener(keywordSearchThreadListener);
		kst = null;
		btnSearchAgain.setText(Localization.getString("SearchAgain"));
		setComponentsEnabled(true, true);
	}

	private void clearAllKeywords() {
		int keywordColumnModelIndex = jtAdder.getColumn("Keyword").getModelIndex();
		model.setFireTableCellUpdatedEnabled(false);
		for (int i = 0; i < jtAdder.getRowCount(); i++) {
			model.setValueAt(null, i, keywordColumnModelIndex);
			updatePathColumn(i);
		}
		model.setFireTableCellUpdatedEnabled(true);
		model.fireTableRowsUpdated(0, model.getRowCount() - 1);
	}

	private void updateAllPathColumns() {
		model.setFireTableCellUpdatedEnabled(false);
		for (int i = 0; i < jtAdder.getRowCount(); i++) {
			updatePathColumn(i);
		}
		model.setFireTableCellUpdatedEnabled(true);
		model.fireTableRowsUpdated(0, model.getRowCount() - 1);
	}

	private void updatePathColumn(int rowIndex) {
		int rowModelIndex = jtAdder.convertRowIndexToModel(rowIndex);

		int hosterColumnModelIndex = jtAdder.getColumn("Host").getModelIndex();

		boolean bPathOverride = false;
		String pathOverrideVal = "";
		boolean bPathOverrideSubdirs = false;

		Object hosterObj = model.getValueAt(rowModelIndex, hosterColumnModelIndex);
		if (hosterObj instanceof IHosterOverrideDirectoryOption iOption) {
			OverrideDirectoryOption option = iOption.getOverrideDirectoryOption();
			bPathOverride = option.isPathOverride();
			bPathOverrideSubdirs = option.isPathOverrideSubdirsAllowed();
			pathOverrideVal = option.getPathOverrideVal();
		}

		int folderOverrideColumnModelIndex = jtAdder.getColumn("TargetFolderOverride").getModelIndex();
		int folderOverrideValueColumnModelIndex = jtAdder.getColumn("TargetFolderOverrideValue").getModelIndex();
		boolean folderOverride = (Boolean)model.getValueAt(rowModelIndex, folderOverrideColumnModelIndex);
		String folderOverrideValue = null;
		if (folderOverride) {
			folderOverrideValue = (String)model.getValueAt(rowModelIndex, folderOverrideValueColumnModelIndex);
		}

		String defaultPath = (String)txtTargetDir.getSelectedItem();

		String actualBasePath = folderOverride ? folderOverrideValue : defaultPath;

		Keyword keyword = (Keyword)model.getValueAt(rowModelIndex, jtAdder.getColumn("Keyword").getModelIndex());
		boolean keywordFound = keyword != null;

		StringBuilder sbNewPath = new StringBuilder();
		if (keywordFound) {
			String keywordDownloadPath = keyword.isRelativePath() ? actualBasePath + keyword.getRelativeDownloadPath() : keyword.getDownloadPath();

			if (bPathOverride) {
				sbNewPath.append(pathOverrideVal);
				if (keyword.isRelativePath() && bPathOverrideSubdirs) {
					sbNewPath.append(keyword.getRelativeDownloadPath());
				}
			} else {
				sbNewPath.append(keywordDownloadPath);
			}
		} else {
			if (bPathOverride) {
				sbNewPath.append(pathOverrideVal);
			} else {
				sbNewPath.append(actualBasePath);
			}
		}

		if (sbNewPath.length() > 0) {
			char lastChar = sbNewPath.charAt(sbNewPath.length() - 1);
			if (lastChar != '/' && lastChar != '\\') {
				sbNewPath.append(FileUtil.FILE_SEPERATOR);
			}
		}

		if (settingsManager.getGUISettings().isAlwaysAddTitle()) {
			String title = BHUtil.filterFilename(txtTitle.getText(), settingsManager);
			if (!title.isEmpty()) {
				sbNewPath.append(title);
				sbNewPath.append(FileUtil.FILE_SEPERATOR);
			}
		}

		String newPath = sbNewPath.toString();
		newPath = BHUtil.filterPath(newPath, settingsManager);
		newPath = BHUtil.reducePathLength(newPath, settingsManager);

		int pathColumnModelIndex = jtAdder.getColumn("TargetFolder").getModelIndex();
		model.setValueAt(newPath, rowModelIndex, pathColumnModelIndex);
	}

	private void addToPaths(String value) {
		int pathColumnModelIndex = jtAdder.getColumn("TargetFolder").getModelIndex();
		model.setFireTableCellUpdatedEnabled(false);
		for (int i = 0; i < jtAdder.getRowCount(); i++) {
			int rowModelIndex = jtAdder.convertRowIndexToModel(i);
			String oldPath = (String)model.getValueAt(rowModelIndex, pathColumnModelIndex);
			String newPath = oldPath + BHUtil.filterFilename(value, settingsManager) + FileUtil.FILE_SEPERATOR;
			newPath = BHUtil.filterPath(newPath, settingsManager);
			newPath = BHUtil.reducePathLength(newPath, settingsManager);
			model.setValueAt(newPath, rowModelIndex, pathColumnModelIndex);
		}
		model.setFireTableCellUpdatedEnabled(true);
		model.fireTableRowsUpdated(0, model.getRowCount() - 1);
	}

	private void importIrada() {
		IradaTsvDialog itd = new IradaTsvDialog(this, proxyManager, settingsManager);
		List<Tsv> result = itd.getResult();
		if (itd.isOkPressed() && result != null) {
			int urlColumnModelIndex = jtAdder.getColumn("URL").getModelIndex();
			int filenameColumnModelIndex = jtAdder.getColumn("Filename").getModelIndex();
			int filenameOverrideColumnModelIndex = jtAdder.getColumn("FilenameOverride").getModelIndex();
			int folderColumnModelIndex = jtAdder.getColumn("TargetFolder").getModelIndex();
			int lastModifiedColumnModelIndex = jtAdder.getColumn("LastModified").getModelIndex();

			model.setFireTableCellUpdatedEnabled(false);
			for (Tsv tsv : result) {
				int correspondingRowModelIndex = -1;

				for (int i = 0; i < jtAdder.getRowCount(); i++) {
					int rowModelIndex = jtAdder.convertRowIndexToModel(i);
					if (tsv.containerURL().equals(model.getValueAt(rowModelIndex, urlColumnModelIndex))) {
						correspondingRowModelIndex = rowModelIndex;
						break;
					}
				}

				if (correspondingRowModelIndex < 0) {
					continue;
				}

				String rPath = tsv.relativePath();
				if (rPath.startsWith("\\") || rPath.startsWith("/")) {
					rPath = rPath.substring(1);
				}
				int pos = rPath.lastIndexOf("\\");
				if (pos < 0) {
					pos = rPath.lastIndexOf("/");
				}

				String oFilename;
				if (pos > 0) {
					oFilename = rPath.substring(pos + 1);
					rPath = rPath.substring(0, pos + 1);
				} else {
					oFilename = rPath;
					rPath = "";
				}

				String oldPath = (String)model.getValueAt(correspondingRowModelIndex, folderColumnModelIndex);
				model.setValueAt(oldPath + rPath, correspondingRowModelIndex, folderColumnModelIndex);
				if (!oFilename.isEmpty()) {
					model.setValueAt(oFilename, correspondingRowModelIndex, filenameColumnModelIndex);
					model.setValueAt(true, correspondingRowModelIndex, filenameOverrideColumnModelIndex);
				}
				model.setValueAt(tsv.lastModified(), correspondingRowModelIndex, lastModifiedColumnModelIndex);
			}
			model.setFireTableCellUpdatedEnabled(true);
			model.fireTableRowsUpdated(0, model.getRowCount() - 1);
		}
	}

	private void select(boolean onlySelectedRows) {
		setSelection(true, onlySelectedRows);
	}

	private void deselect(boolean onlySelectedRows) {
		setSelection(false, onlySelectedRows);
	}

	private void setSelection(boolean selectValue, boolean onlySelectedRows) {
		int selectionColumnModelIndex = jtAdder.getColumn("Selection").getModelIndex();
		int[] selectedRows = jtAdder.getSelectedRows();
		model.setFireTableCellUpdatedEnabled(false);
		int rowCount = onlySelectedRows ? selectedRows.length : jtAdder.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			int modelIndex = jtAdder.convertRowIndexToModel(onlySelectedRows ? selectedRows[i] : i);
			model.setValueAt(selectValue, modelIndex, selectionColumnModelIndex);
		}
		model.setFireTableCellUpdatedEnabled(true);
		model.fireTableRowsUpdated(0, model.getRowCount() - 1);
	}

	private void invertSelection(boolean onlySelectedRows) {
		int selectionColumnModelIndex = jtAdder.getColumn("Selection").getModelIndex();
		int[] selectedRows = jtAdder.getSelectedRows();
		model.setFireTableCellUpdatedEnabled(false);
		int rowCount = onlySelectedRows ? selectedRows.length : jtAdder.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			int modelIndex = jtAdder.convertRowIndexToModel(onlySelectedRows ? selectedRows[i] : i);
			boolean currentValue = (boolean)model.getValueAt(modelIndex, selectionColumnModelIndex);
			model.setValueAt(!currentValue, modelIndex, selectionColumnModelIndex);
		}
		model.setFireTableCellUpdatedEnabled(true);
		model.fireTableRowsUpdated(0, model.getRowCount() - 1);
	}

	/**
	 * ChangeTargetFilename
	 */
	private synchronized void actionChangeTargetFilename() {
		int filenameColumnModelIndex = jtAdder.getColumn("Filename").getModelIndex();
		int filenameOverrideColumnModelIndex = jtAdder.getColumn("FilenameOverride").getModelIndex();

		int[] selectedRows = jtAdder.getSelectedRows();
		if (selectedRows.length > 0) {
			String defaultvalue = (String)model.getValueAt(jtAdder.convertRowIndexToModel(selectedRows[0]), filenameColumnModelIndex);
			defaultvalue = defaultvalue.substring(defaultvalue.lastIndexOf(FileUtil.FILE_SEPERATOR) + 1);
			String[] input = FileRenameDialog.showFileRenameDialog(this, "", defaultvalue, selectedRows.length, settingsManager);
			if (input != null) {
				int index = Integer.parseInt(input[1]);
				int step = Integer.parseInt(input[2]);
				boolean keepOriginal = !input[5].isEmpty();
				boolean clearFilename = !input[6].isEmpty();
				model.setFireTableCellUpdatedEnabled(false);
				for (int i = 0; i < selectedRows.length; i++) {
					int modelIndex = jtAdder.convertRowIndexToModel(selectedRows[i]);

					String newFilename = null;
					if (!clearFilename) {
						String filenameFormat;
						if (keepOriginal) {
							filenameFormat = (String)model.getValueAt(modelIndex, filenameColumnModelIndex);
						} else {
							filenameFormat = input[0];
						}
						filenameFormat = input[3] + filenameFormat + input[4];
						newFilename = FileUtil.getNumberedFilename(filenameFormat, index);
					}

					if (newFilename == null || newFilename.isEmpty()) {
						model.setValueAt(Localization.getString("Unkown"), modelIndex, filenameColumnModelIndex);
						model.setValueAt(false, modelIndex, filenameOverrideColumnModelIndex);
					} else {
						model.setValueAt(newFilename, modelIndex, filenameColumnModelIndex);
						model.setValueAt(true, modelIndex, filenameOverrideColumnModelIndex);
					}
					index += step;
				}
				model.setFireTableCellUpdatedEnabled(true);
				model.fireTableRowsUpdated(0, model.getRowCount() - 1);
			}
		}
	}

	/**
	 * ChangeTargetByInput
	 */
	private synchronized void actionChangeTargetByInput() {
		int folderColumnModelIndex = jtAdder.getColumn("TargetFolder").getModelIndex();
		int folderOverrideColumnModelIndex = jtAdder.getColumn("TargetFolderOverride").getModelIndex();
		int[] selectedRows = jtAdder.getSelectedRows();

		String defaultPath = (String)txtTargetDir.getSelectedItem();
		if (selectedRows.length == 1) {
			defaultPath = (String)model.getValueAt(jtAdder.convertRowIndexToModel(selectedRows[0]), folderColumnModelIndex);
		}
		String input = PathRenameDialog.showPathRenameDialog(this, defaultPath);
		if (input != null && input.length() > 2) {
			if (!input.endsWith("/") && !input.endsWith("\\")) {
				input += FileUtil.FILE_SEPERATOR;
			}
			model.setFireTableCellUpdatedEnabled(false);
			for (int i = 0; i < selectedRows.length; i++) {
				int modelIndex = jtAdder.convertRowIndexToModel(selectedRows[i]);
				String newPath = BHUtil.filterPath(input, settingsManager);
				newPath = BHUtil.reducePathLength(newPath, settingsManager);
				model.setValueAt(newPath, modelIndex, folderColumnModelIndex);
				model.setValueAt(false, modelIndex, folderOverrideColumnModelIndex);
			}
			model.setFireTableCellUpdatedEnabled(true);
			model.fireTableRowsUpdated(0, model.getRowCount() - 1);
		}
	}

	/**
	 * ChangeTargetBySelection
	 */
	private synchronized void actionChangeTargetBySelection() {
		File file = FileDialogUtil.showFolderOpenDialog(this, (String)txtTargetDir.getSelectedItem(), null);
		if (file != null) {
			String folder = file.getAbsolutePath() + FileUtil.FILE_SEPERATOR;
			int[] selectedRows = jtAdder.getSelectedRows();
			model.setFireTableCellUpdatedEnabled(false);
			for (int i = 0; i < selectedRows.length; i++) {
				int modelIndex = jtAdder.convertRowIndexToModel(selectedRows[i]);
				model.setValueAt(folder, modelIndex, 3);
			}
			model.setFireTableCellUpdatedEnabled(true);
			model.fireTableRowsUpdated(0, model.getRowCount() - 1);
		}
	}

	/**
	 * CopyURLs
	 */
	private synchronized void actionCopyURLs() {
		int urlColumnModelIndex = jtAdder.getColumn("URL").getModelIndex();
		int[] selectedRows = jtAdder.getSelectedRows();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				StringBuilder content = new StringBuilder();
				for (int i = 0; i < selectedRows.length; i++) {
					int modelIndex = jtAdder.convertRowIndexToModel(selectedRows[i]);
					content.append((String)model.getValueAt(modelIndex, urlColumnModelIndex));
					if (selectedRows.length > 1) {
						content.append("\n");
					}
				}
				if (content.length() > 0) {
					clipboardObserver.setClipboardContent(content.toString());
				}
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * OpenURLs
	 */
	private synchronized void actionOpenURLs() {
		if (Desktop.isDesktopSupported()) {
			int urlColumnModelIndex = jtAdder.getColumn("URL").getModelIndex();
			int[] selectedRows = jtAdder.getSelectedRows();
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < selectedRows.length; i++) {
						int modelIndex = jtAdder.convertRowIndexToModel(selectedRows[i]);
						String url = (String)model.getValueAt(modelIndex, urlColumnModelIndex);
						try {
							Desktop.getDesktop().browse(new URI(url));
						} catch (IOException | URISyntaxException e) {
							logger.error("Could not open URL: {}", url, e);
						}

					}
				}
			});
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		} else {
			logger.error("Could not open URLs, because Desktop is not supported");
		}
	}

	/**
	 * @param folder Folder
	 * @return Index
	 */
	private int getIndexForFolder(String folder) {
		for (int i = 0; i < txtTargetDir.getItemCount(); i++) {
			if (txtTargetDir.getItemAt(i).equals(folder)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @return Table Summary String
	 */
	private String createTableSummaryString() {
		int selectedLinkCount = 0;

		int selectionColumnModelIndex = jtAdder.getColumn("Selection").getModelIndex();
		for (int i = 0; i < model.getRowCount(); i++) {
			boolean selected = (Boolean)model.getValueAt(i, selectionColumnModelIndex);
			if (selected) {
				selectedLinkCount++;
			}
		}

		return Localization.getString("Links") + ": " + model.getRowCount() + " | " + Localization.getString("LinksSelected") + ": " + selectedLinkCount + " | "
				+ Localization.getString("LinksAlreadyDownloaded") + ": " + alreadyDownloadedLinksCount;
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (!settingsManager.isSaveTableColumnSizes()) {
			return;
		}
		TableUtil.applyColWidths(jtAdder, settingsManager.getColWidthsAdder());
	}

	/**
	 * Listener for URL Checking
	 */
	private class AdderHostURLCheckerListener implements HostURLCheckerListener {
		@Override
		public void linksChecked(List<URL> urls) {
			if (huc != null) {
				huc.removeHMListener(hostURLCheckerListener);
			}
			huc = null;

			if (urls.isEmpty()) {
				toFront();
				JOptionPane.showMessageDialog(AdderWindow.this, Localization.getString("NoPicsFound"), "Error", JOptionPane.ERROR_MESSAGE);
				dispose();
				return;
			}

			/*
			 * We have to use the removeDuplicates-Method here again, because
			 * new links could have been added by HostURLCheckerRunnable.
			 */
			setComponentsEnabled(false, false);
			ProgressObserver progress = new ProgressObserver();
			progress.addProgressListener(new AdderProgressListener());
			HostManager.removeDuplicates(urls, progress, settingsManager);

			// Add links to table
			setData(urls);
		}

		@Override
		public void progressChanged(int val) {
			setPGValue(val);
		}

		@Override
		public void progressChanged(int min, int max, int val) {
			setPGEnabled(true, min, max, val);
		}

		@Override
		public void progressChanged(String text) {
			setPGText(text);
		}
	}

	/**
	 * Listener for keyword search
	 */
	private class AdderKeywordSearchThreadListener implements KeywordSearchThreadListener {
		@Override
		public void searchDone(Keyword[] retval) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					keywordSearchDone(retval);
				}
			});
		}

		@Override
		public void progressBarChanged(int min, int max, int val) {
			setPGEnabled(true, min, max, val);
		}

		@Override
		public void progressBarChanged(int val) {
			setPGValue(val);
		}

		@Override
		public void progressBarStatusChanged(boolean enabled) {
			setPGEnabled(enabled);
		}
	}

	/**
	 * Listener for changes of column widths
	 */
	private class AdderColumnListener implements TableColumnModelListener {
		@Override
		public void columnAdded(TableColumnModelEvent e) {
			// Nothing to do
		}

		@Override
		public void columnRemoved(TableColumnModelEvent e) {
			// Nothing to do
		}

		@Override
		public void columnMoved(TableColumnModelEvent e) {
			// Nothing to do
		}

		@Override
		public void columnMarginChanged(ChangeEvent e) {
			updateColWidthsToSettingsManager();
		}

		@Override
		public void columnSelectionChanged(ListSelectionEvent e) {
			// Nothing to do
		}

		/**
		 * updateColWidthsToSettingsManager
		 */
		private void updateColWidthsToSettingsManager() {
			if (!settingsManager.isSaveTableColumnSizes()) {
				return;
			}
			settingsManager.setColWidthsAdder(TableUtil.serializeColWidthSetting(jtAdder));
			settingsManager.writeSettings(true);
		}
	}

	/**
	 * Progress Listener
	 */
	private class AdderProgressListener implements IProgressObserver {
		@Override
		public void progressChanged(boolean visible) {
			// Nothing to do
		}

		@Override
		public void progressChanged(String text) {
			setPGText(text);
		}

		@Override
		public void progressChanged(int min, int max, int val) {
			setPGEnabled(true, min, max, val);
		}

		@Override
		public void progressChanged(int val) {
			setPGValue(val);
		}

		@Override
		public void progressIncreased() {
			// Nothing to do
		}

		@Override
		public void progressModeChanged(boolean indeterminate) {
			// Nothing to do
		}

		@Override
		public void progressCompleted() {
			// Nothing to do
		}
	}

	/**
	 * AdjustmentListener for loading and unloading previews
	 */
	private class PreviewsAdjustmentListener implements AdjustmentListener {
		/**
		 * Counter for rows scrolled
		 */
		private int unitDecrementCounter = 0;

		@Override
		public void adjustmentValueChanged(AdjustmentEvent e) {
			if (e.getValueIsAdjusting() || !btnShowPreviews.isSelected()) {
				return;
			}

			/*
			 * For performance we do not execute the loadAndUnloadPreviews method
			 * for unit in/decrements.
			 * But if a user only scrolls by 1 unit all the time, we do have to
			 * execute the method before the user scrolled to a row were the preview
			 * is not loaded.
			 * So we use the unitDecrementCounter as indication.
			 * As long as the value is near 0, there's no need to use the method.
			 * But if the value is near MAX_SCROLL_UNIT_COUNTER or MIN_SCROLL_UNIT_COUNTER
			 * then we have to.
			 */

			switch (e.getAdjustmentType()) {
				case AdjustmentEvent.UNIT_INCREMENT:
					// Scrolled 1 row
					unitDecrementCounter++;
					if (unitDecrementCounter >= MAX_SCROLL_UNIT_COUNTER) {
						unitDecrementCounter = 0;
						loadAndUnloadPreviews(false);
					}
					break;
				case AdjustmentEvent.UNIT_DECREMENT:
					// Scrolled 1 row
					unitDecrementCounter--;
					if (unitDecrementCounter <= MIN_SCROLL_UNIT_COUNTER) {
						unitDecrementCounter = 0;
						loadAndUnloadPreviews(false);
					}
					break;
				case AdjustmentEvent.BLOCK_INCREMENT, AdjustmentEvent.BLOCK_DECREMENT, AdjustmentEvent.TRACK:
					// Scrolled multiple rows or Scrollbar dragged
					unitDecrementCounter = 0;
					loadAndUnloadPreviews(false);
					break;
				default:
					break;
			}
		}
	}
}

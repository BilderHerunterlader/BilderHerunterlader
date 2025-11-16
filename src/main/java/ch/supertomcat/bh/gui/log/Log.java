package ch.supertomcat.bh.gui.log;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import ch.supertomcat.bh.clipboard.ClipboardObserver;
import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.importexport.ExportLogs;
import ch.supertomcat.bh.log.ILogManagerListener;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.log.LogPageInfo;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.FileExplorerUtil;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;

/**
 * Panel containing Logs
 */
public class Log extends JPanel implements ILogManagerListener {
	private static final long serialVersionUID = 1L;

	/**
	 * Table
	 */
	private JTable jtLog = null;

	/**
	 * TabelModel
	 */
	private LogTableModel model = new LogTableModel();

	/**
	 * PopupMenu
	 */
	private JPopupMenu popupMenu = new JPopupMenu();

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemCopyURL = new JMenuItem(Localization.getString("CopyURL"), Icons.getTangoSVGIcon("actions/edit-copy.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemOpenURL = new JMenuItem(Localization.getString("OpenURL"), Icons.getTangoSVGIcon("apps/internet-web-browser.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemOpenDirectory = new JMenuItem(Localization.getString("OpenDirectory"), Icons.getTangoSVGIcon("places/folder.svg", 16));

	/**
	 * Label
	 */
	private JLabel lblStatus = new JLabel(Localization.getString("OnlyTheLastFilesAreShownLog"));

	/**
	 * Button
	 */
	private JButton btnFirst = new JButton("", Icons.getTangoSVGIcon("actions/go-first.svg", 16));

	/**
	 * Button
	 */
	private JButton btnNext = new JButton("", Icons.getTangoSVGIcon("actions/go-next.svg", 16));

	/**
	 * Button
	 */
	private JButton btnPrevious = new JButton("", Icons.getTangoSVGIcon("actions/go-previous.svg", 16));

	/**
	 * Button
	 */
	private JButton btnLast = new JButton("", Icons.getTangoSVGIcon("actions/go-last.svg", 16));

	/**
	 * Panel
	 */
	private JPanel pnlTopButtons = new JPanel();

	/**
	 * Button
	 */
	private JButton btnExport = new JButton(Localization.getString("Export"), Icons.getTangoSVGIcon("actions/document-save-as.svg", 16));

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * DefaultStringColorRowRenderer
	 */
	private DefaultStringColorRowRenderer crr;

	/**
	 * CurrentPage
	 */
	private LogPageInfo currentPage = new LogPageInfo(0, 0, 0, 0);

	/**
	 * Flag if the last 100 lines of the logfiles are displayed
	 */
	private boolean last = true;

	/**
	 * Flag if new lines are available in the logfile
	 */
	private boolean changed = true;

	/**
	 * Log Manager
	 */
	private final LogManager logManager;

	/**
	 * Main Window Access
	 */
	private final MainWindowAccess mainWindowAccess;

	/**
	 * Parent Window
	 */
	private final JFrame parentWindow;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Clipboard Observer
	 */
	private final ClipboardObserver clipboardObserver;

	/**
	 * Constructor
	 * 
	 * @param logManager Log Manager
	 * @param downloadQueueManager Download Queue Manager
	 * @param mainWindowAccess Main Window Access
	 * @param parentWindow Parent Window
	 * @param settingsManager Settings Manager
	 * @param clipboardObserver Clipboard Observer
	 */
	public Log(LogManager logManager, DownloadQueueManager downloadQueueManager, MainWindowAccess mainWindowAccess, JFrame parentWindow, SettingsManager settingsManager,
			ClipboardObserver clipboardObserver) {
		this.logManager = logManager;
		this.mainWindowAccess = mainWindowAccess;
		this.parentWindow = parentWindow;
		this.settingsManager = settingsManager;
		this.clipboardObserver = clipboardObserver;

		jtLog = new JTable(model);

		TableUtil.internationalizeColumns(jtLog);

		setLayout(new BorderLayout());
		jtLog.getColumn("DateTime").setMinWidth(100);
		jtLog.getColumn("DateTime").setMaxWidth(150);
		jtLog.getColumn("Size").setMinWidth(100);
		jtLog.getColumn("Size").setMaxWidth(150);
		updateColWidthsFromSettingsManager(settingsManager);
		jtLog.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
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
				updateColWidthsToSettingsManager(settingsManager);
			}

			@Override
			public void columnAdded(TableColumnModelEvent e) {
				// Nothing to do
			}
		});
		jtLog.getTableHeader().setReorderingAllowed(false);
		jtLog.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		jtLog.setRowHeight(TableUtil.calculateRowHeight(jtLog, false, true));

		JScrollPane jsp = new JScrollPane(jtLog);
		add(jsp, BorderLayout.CENTER);

		btnFirst.addActionListener(e -> {
			updateIndexAndStatus(logManager.readLogs(0, model));
			last = false;
		});
		btnPrevious.addActionListener(e -> {
			if (currentPage.start() <= 0) {
				updateIndexAndStatus(logManager.readLogs(0, model));
			} else {
				updateIndexAndStatus(logManager.readLogs(currentPage.start() - LogManager.LOG_ENTRIES_PER_PAGE, model));
			}
			last = false;
		});
		btnNext.addActionListener(e -> {
			updateIndexAndStatus(logManager.readLogs(currentPage.start() + LogManager.LOG_ENTRIES_PER_PAGE, model));
			last = false;
		});
		btnLast.addActionListener(e -> {
			updateIndexAndStatus(logManager.readLogs(-1, model));
			last = true;
		});

		pnlTopButtons.add(btnFirst);
		pnlTopButtons.add(btnPrevious);
		pnlTopButtons.add(btnNext);
		pnlTopButtons.add(btnLast);

		lblStatus.setHorizontalAlignment(SwingConstants.LEFT);
		JPanel pnlStatus = new JPanel();
		pnlStatus.setLayout(new BoxLayout(pnlStatus, BoxLayout.LINE_AXIS));
		pnlStatus.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		pnlStatus.add(lblStatus);

		JPanel pnlTop = new JPanel(new BorderLayout());
		pnlTop.add(pnlStatus, BorderLayout.WEST);
		pnlTop.add(pnlTopButtons, BorderLayout.EAST);
		add(pnlTop, BorderLayout.NORTH);

		btnExport.addActionListener(e -> actionExport());
		pnlButtons.add(btnExport);
		add(pnlButtons, BorderLayout.SOUTH);

		popupMenu.add(menuItemCopyURL);
		popupMenu.add(menuItemOpenURL);
		popupMenu.add(menuItemOpenDirectory);
		menuItemCopyURL.addActionListener(e -> actionCopyURLs());
		menuItemOpenURL.addActionListener(e -> actionOpenURLs());
		menuItemOpenDirectory.addActionListener(e -> actionOpenDirectories());

		jtLog.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger() && !downloadQueueManager.isDownloading() && jtLog.getSelectedRowCount() > 0) {
					SwingUtilities.updateComponentTreeUI(popupMenu);
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger() && !downloadQueueManager.isDownloading() && jtLog.getSelectedRowCount() > 0) {
					SwingUtilities.updateComponentTreeUI(popupMenu);
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		crr = new DefaultStringColorRowRenderer();
		jtLog.setDefaultRenderer(Object.class, crr);

		logManager.addLogManagerListener(this);
	}

	/**
	 * CopyURLs
	 */
	private void actionCopyURLs() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				int[] s = jtLog.getSelectedRows();
				StringBuilder content = new StringBuilder();
				for (int i = 0; i < s.length; i++) {
					content.append((String)model.getValueAt(s[i], 1));
					if (s.length > 1) {
						content.append("\n");
					}
				}
				if (s.length > 0) {
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
	private void actionOpenURLs() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				int[] s = jtLog.getSelectedRows();
				for (int i = 0; i < s.length; i++) {
					String url = (String)model.getValueAt(s[i], 1);
					FileExplorerUtil.openURL(url);
				}
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * OpenDirectories
	 */
	private void actionOpenDirectories() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				HashSet<String> dirs = new HashSet<>();
				int[] s = jtLog.getSelectedRows();
				for (int i = 0; i < s.length; i++) {
					String directory = (String)model.getValueAt(s[i], 2);
					dirs.add(directory);
				}
				for (String dir : dirs) {
					FileExplorerUtil.openDirectory(dir);
				}
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	private void actionExport() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				new ExportLogs(parentWindow, mainWindowAccess, logManager, settingsManager).exportLogs();
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * Updated index and status
	 * 
	 * @param loadedPage Loaded Page
	 */
	private void updateIndexAndStatus(LogPageInfo loadedPage) {
		int adjustedStart = loadedPage.adjustedStart();
		int end = loadedPage.end();
		int lineCount = loadedPage.lineCount();
		this.currentPage = loadedPage;
		if (lineCount == 0) {
			lblStatus.setText("Downloads: " + lineCount + " | " + Localization.getString("ShowNothing"));
		} else {
			lblStatus.setText("Downloads: " + lineCount + " | " + Localization.getString("ShowingFrom") + " " + adjustedStart + " " + Localization.getString("To") + " " + end);
		}
	}

	/**
	 * Reload logs
	 */
	public void reloadLogs() {
		if (changed && last && mainWindowAccess.isTabSelected(this)) {
			if (EventQueue.isDispatchThread()) {
				updateIndexAndStatus(logManager.readLogs(-1, model));
				changed = false;
			} else {
				EventQueue.invokeLater(() -> {
					updateIndexAndStatus(logManager.readLogs(-1, model));
					changed = false;
				});
			}
		}
	}

	/**
	 * updateColWidthsToSettingsManager
	 * 
	 * @param settingsManager Settings Manager
	 */
	private void updateColWidthsToSettingsManager(SettingsManager settingsManager) {
		if (!settingsManager.isSaveTableColumnSizes()) {
			return;
		}
		settingsManager.setColWidthsLog(TableUtil.serializeColWidthSetting(jtLog));
		settingsManager.writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 * 
	 * @param settingsManager Settings Manager
	 */
	private void updateColWidthsFromSettingsManager(SettingsManager settingsManager) {
		if (!settingsManager.isSaveTableColumnSizes()) {
			return;
		}
		TableUtil.applyColWidths(jtLog, settingsManager.getColWidthsLog());
	}

	@Override
	public void logChanged() {
		changed = true;
		reloadLogs();
	}
}

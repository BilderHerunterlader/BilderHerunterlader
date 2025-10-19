package ch.supertomcat.bh.gui.log;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Paths;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
import ch.supertomcat.bh.log.ILogManagerListener;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.FileExplorerUtil;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;
import ch.supertomcat.supertomcatutils.io.FileUtil;

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
	private JPanel pnlButtons = new JPanel();

	/**
	 * DefaultStringColorRowRenderer
	 */
	private DefaultStringColorRowRenderer crr;

	/**
	 * Current Start Index (Index of the line in the logfile)
	 */
	private long currentStart = 1;

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
	 * Clipboard Observer
	 */
	private final ClipboardObserver clipboardObserver;

	/**
	 * Constructor
	 * 
	 * @param logManager Log Manager
	 * @param downloadQueueManager Download Queue Manager
	 * @param mainWindowAccess Main Window Access
	 * @param settingsManager Settings Manager
	 * @param clipboardObserver Clipboard Observer
	 */
	public Log(LogManager logManager, DownloadQueueManager downloadQueueManager, MainWindowAccess mainWindowAccess, SettingsManager settingsManager, ClipboardObserver clipboardObserver) {
		this.logManager = logManager;
		this.mainWindowAccess = mainWindowAccess;
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
			updateIndexAndStatus(logManager.readLogs(1, model));
			last = false;
		});
		btnPrevious.addActionListener(e -> {
			if (currentStart > 100) {
				updateIndexAndStatus(logManager.readLogs(currentStart - 100, model));
			} else {
				updateIndexAndStatus(logManager.readLogs(1, model));
			}
			last = false;
		});
		btnNext.addActionListener(e -> {
			updateIndexAndStatus(logManager.readLogs(currentStart + 100, model));
			last = false;
		});
		btnLast.addActionListener(e -> {
			updateIndexAndStatus(logManager.readLogs(-1, model));
			last = true;
		});

		pnlButtons.add(btnFirst);
		pnlButtons.add(btnPrevious);
		pnlButtons.add(btnNext);
		pnlButtons.add(btnLast);

		lblStatus.setHorizontalAlignment(SwingConstants.LEFT);
		JPanel pnlStatus = new JPanel();
		pnlStatus.setLayout(new BoxLayout(pnlStatus, BoxLayout.LINE_AXIS));
		pnlStatus.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		pnlStatus.add(lblStatus);

		JPanel pnlTop = new JPanel(new BorderLayout());
		pnlTop.add(pnlStatus, BorderLayout.WEST);
		pnlTop.add(pnlButtons, BorderLayout.EAST);
		add(pnlTop, BorderLayout.NORTH);

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
				String file = "";
				for (int i = 0; i < s.length; i++) {
					file = (String)model.getValueAt(s[i], 2);
					file = FileUtil.getPathFromFile(Paths.get(file));
					dirs.add(file);
				}
				for (String dir : dirs) {
					FileExplorerUtil.openDirectory(dir);
				}
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * @param vals Values
	 */
	private void updateIndexAndStatus(long[] vals) {
		if (vals.length != 3) {
			return;
		}
		long localCurrentStart = vals[0];
		long end = vals[1];
		long lineCount = vals[2];
		this.currentStart = localCurrentStart;
		if (lineCount == 0) {
			lblStatus.setText("Downloads: " + lineCount + " | " + Localization.getString("ShowNothing"));
		} else {
			lblStatus.setText("Downloads: " + lineCount + " | " + Localization.getString("ShowingFrom") + " " + localCurrentStart + " " + Localization.getString("To") + " " + end);
		}
	}

	/**
	 * Reload logs
	 */
	public void reloadLogs() {
		if (changed && last && mainWindowAccess.isTabSelected(this)) {
			updateIndexAndStatus(logManager.readLogs(-1, model));
			changed = false;
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
		if (EventQueue.isDispatchThread()) {
			changed = true;
			reloadLogs();
		} else {
			EventQueue.invokeLater(() -> {
				changed = true;
				reloadLogs();
			});
		}
	}

	@Override
	public void currentLogFileChanged() {
		if (EventQueue.isDispatchThread()) {
			updateIndexAndStatus(logManager.readLogs(-1, model));
			last = true;
		} else {
			EventQueue.invokeLater(() -> {
				updateIndexAndStatus(logManager.readLogs(-1, model));
				last = true;
			});
		}
	}
}

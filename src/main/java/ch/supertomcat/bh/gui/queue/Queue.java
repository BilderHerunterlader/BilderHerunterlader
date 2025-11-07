package ch.supertomcat.bh.gui.queue;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.clipboard.ClipboardObserver;
import ch.supertomcat.bh.cookies.CookieManager;
import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.BHIcons;
import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.gui.renderer.QueueColorRowRenderer;
import ch.supertomcat.bh.gui.renderer.QueueProgressColumnRenderer;
import ch.supertomcat.bh.gui.renderer.QueueSizeColumnRenderer;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.importexport.ExportQueue;
import ch.supertomcat.bh.importexport.ImportHTML;
import ch.supertomcat.bh.importexport.ImportLinkList;
import ch.supertomcat.bh.importexport.ImportLocalFiles;
import ch.supertomcat.bh.importexport.ImportQueue;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.queue.IDownloadQueueManagerListener;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcatutils.gui.FileExplorerUtil;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.dialog.FileDialogUtil;
import ch.supertomcat.supertomcatutils.gui.formatter.UnitFormatUtil;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.hider.TableColumnHider;
import ch.supertomcat.supertomcatutils.gui.table.hider.TableColumnHiderSizeBasedImpl;
import ch.supertomcat.supertomcatutils.gui.table.hider.TableHeaderColumnSelector;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Queue-Panel
 */
public class Queue extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * TabelModel
	 */
	private QueueTableModel model;

	/**
	 * Table
	 */
	private JTable jtQueue;

	/**
	 * Table Column Hider
	 */
	private TableColumnHider tableColumnHider;

	/**
	 * Table Header Column Selector
	 */
	private TableHeaderColumnSelector tableHeaderColumnSelector;

	/**
	 * Button
	 */
	private JButton btnStart = new JButton(Localization.getString("Start"), Icons.getTangoSVGIcon("actions/media-playback-start.svg", 16));

	/**
	 * Button
	 */
	private JButton btnStop = new JButton(Localization.getString("Stop"), Icons.getTangoSVGIcon("actions/media-playback-stop.svg", 16));

	/**
	 * MenuItem
	 */
	private JButton btnImportLinks = new JButton(Localization.getString("ImportLinks"), Icons.getTangoSVGIcon("emblems/emblem-symbolic-link.svg", 16));

	/**
	 * MenuItem
	 */
	private JButton btnParseLinks = new JButton(Localization.getString("ParseLinks"), Icons.getTangoSVGIcon("emblems/emblem-symbolic-link.svg", 16));

	/**
	 * Button
	 */
	private JButton btnSortFiles = new JButton(Localization.getString("SortFiles"), BHIcons.getBHSVGIcon("actions/data-transfer.svg", 16));

	/**
	 * Button
	 */
	private JButton btnImportExport = new JButton(Localization.getString("ImportExport"), Icons.getTangoSVGIcon("actions/document-open.svg", 16));

	/**
	 * PopupMenu
	 */
	private JPopupMenu menuImportExport = new JPopupMenu(Localization.getString("ImportExport"));

	/**
	 * MenuItem
	 */
	private JMenuItem itemImportHTML = new JMenuItem(Localization.getString("ImportHTML"), Icons.getTangoSVGIcon("mimetypes/text-html.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemImportText = new JMenuItem(Localization.getString("ImportText"), Icons.getTangoSVGIcon("mimetypes/text-x-generic.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemImportQueue = new JMenuItem(Localization.getString("QueueImport"), Icons.getTangoSVGIcon("actions/document-open.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemExportQueue = new JMenuItem(Localization.getString("QueueExport"), Icons.getTangoSVGIcon("actions/document-open.svg", 16));

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * Label for Row Count
	 */
	private JLabel lblRowCount = new JLabel("");

	/**
	 * Label
	 */
	private JLabel lblStatus = new JLabel("");

	/**
	 * PopupMenu
	 */
	private JPopupMenu popupMenu = new JPopupMenu();

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemDelete = new JMenuItem(Localization.getString("Delete"), Icons.getTangoSVGIcon("actions/edit-delete.svg", 16));

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
	private JMenuItem menuItemOpenThreadURL = new JMenuItem(Localization.getString("OpenThreadURL"), Icons.getTangoSVGIcon("apps/internet-web-browser.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemActivate = new JMenuItem(Localization.getString("Activate"), Icons.getTangoSVGIcon("actions/media-record.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemDeactivate = new JMenuItem(Localization.getString("Deactivate"), Icons.getTangoSVGIcon("emblems/emblem-readonly.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemChangeTargetfilename = new JMenuItem(Localization.getString("ChangeTargetFilename"), Icons.getTangoSVGIcon("apps/accessories-text-editor.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemChangeTargetByInput = new JMenuItem(Localization.getString("ChangeTargetByInput"), Icons.getTangoSVGIcon("apps/accessories-text-editor.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemChangeTargetBySelection = new JMenuItem(Localization.getString("ChangeTargetBySelection"), Icons.getTangoSVGIcon("apps/accessories-text-editor.svg", 16));

	/**
	 * Scrollpane
	 */
	private JScrollPane jsp;

	/**
	 * Parent Window
	 */
	private final JFrame parentWindow;

	/**
	 * Main Window Access
	 */
	private final MainWindowAccess mainWindowAccess;

	/**
	 * Queue Manager
	 */
	private final QueueManager queueManager;

	/**
	 * Download Queue Manager
	 */
	private final DownloadQueueManager downloadQueueManager;

	/**
	 * Log Manager
	 */
	private final LogManager logManager;

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
	 * Cookie Manager
	 */
	private final CookieManager cookieManager;

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
	 * @param parentWindow Parent Window
	 * @param mainWindowAccess Main Window Access
	 * @param queueManager Queue Manager
	 * @param downloadQueueManager Download Queue Manager
	 * @param logManager Log Manager
	 * @param keywordManager Keyword Manager
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param cookieManager Cookie Manager
	 * @param hostManager Host Manager
	 * @param clipboardObserver Clipboard Observer
	 */
	public Queue(JFrame parentWindow, MainWindowAccess mainWindowAccess, QueueManager queueManager, DownloadQueueManager downloadQueueManager, LogManager logManager, KeywordManager keywordManager,
			ProxyManager proxyManager, SettingsManager settingsManager, CookieManager cookieManager, HostManager hostManager, ClipboardObserver clipboardObserver) {
		this.parentWindow = parentWindow;
		this.mainWindowAccess = mainWindowAccess;
		this.queueManager = queueManager;
		this.downloadQueueManager = downloadQueueManager;
		this.logManager = logManager;
		this.keywordManager = keywordManager;
		this.proxyManager = proxyManager;
		this.settingsManager = settingsManager;
		this.cookieManager = cookieManager;
		this.hostManager = hostManager;
		this.clipboardObserver = clipboardObserver;

		setLayout(new BorderLayout());

		this.model = this.queueManager.getTableModel();
		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getColumn() == TableModelEvent.ALL_COLUMNS) {
					updateRowCountLabel();
				}
			}
		});
		jtQueue = new JTable(model);
		jsp = new JScrollPane(jtQueue);
		tableColumnHider = new TableColumnHiderSizeBasedImpl(jtQueue);
		tableHeaderColumnSelector = new TableHeaderColumnSelector(tableColumnHider, jtQueue);

		TableUtil.internationalizeColumns(jtQueue);

		QueueSizeColumnRenderer scr = new QueueSizeColumnRenderer(settingsManager);
		jtQueue.getColumn("Size").setCellRenderer(scr);

		QueueProgressColumnRenderer pcr = new QueueProgressColumnRenderer(settingsManager);
		jtQueue.getColumn("Progress").setCellRenderer(pcr);

		int urlOrTargetTableHeaderWidth = TableUtil.calculateColumnHeaderWidth(jtQueue, jtQueue.getColumn("URL"), 47);
		jtQueue.getColumn("URL").setPreferredWidth(urlOrTargetTableHeaderWidth);
		jtQueue.getColumn("Target").setPreferredWidth(urlOrTargetTableHeaderWidth);
		updateColWidthsFromSettingsManager();
		jtQueue.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnAdded(TableColumnModelEvent e) {
				// Nothing to do
			}

			@Override
			public void columnMarginChanged(ChangeEvent e) {
				updateColWidthsToSettingsManager();
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
				// Nothing to do
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
				// Nothing to do
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
				// Nothing to do
			}
		});
		jtQueue.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger() && jtQueue.getSelectedRowCount() > 0) {
					showTablePopupMenu(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger() && jtQueue.getSelectedRowCount() > 0) {
					showTablePopupMenu(e);
				}
			}
		});
		jtQueue.getTableHeader().setReorderingAllowed(false);

		tableColumnHider.hideColumn("ThumbURL");
		tableColumnHider.hideColumn("Host");
		tableColumnHider.hideColumn("ThreadURL");
		tableColumnHider.hideColumn("DownloadURL");
		tableColumnHider.hideColumn("AddedDate");

		tableHeaderColumnSelector.addColumn("ThumbURL");
		tableHeaderColumnSelector.addColumn("Host");
		tableHeaderColumnSelector.addColumn("ThreadURL");
		tableHeaderColumnSelector.addColumn("DownloadURL");
		tableHeaderColumnSelector.addColumn("AddedDate");

		jtQueue.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		jtQueue.setRowHeight(TableUtil.calculateRowHeight(jtQueue, false, true));

		add(jsp, BorderLayout.CENTER);

		btnStart.setMnemonic(KeyEvent.VK_S);
		btnStop.setMnemonic(KeyEvent.VK_T);
		btnImportExport.setMnemonic(KeyEvent.VK_I);
		btnImportLinks.setMnemonic(KeyEvent.VK_L);
		btnParseLinks.setMnemonic(KeyEvent.VK_P);

		btnStart.addActionListener(e -> actionStart());
		btnStop.addActionListener(e -> actionStop());
		btnStop.setToolTipText(Localization.getString("StopTooltip"));
		btnImportLinks.addActionListener(e -> actionImportLinks());
		btnParseLinks.addActionListener(e -> actionParseLinks());
		btnSortFiles.addActionListener(e -> actionSort());
		btnImportExport.addActionListener(e -> {
			SwingUtilities.updateComponentTreeUI(menuImportExport);
			int x = 0;
			int y = 0;
			int buttonWidth = btnImportExport.getWidth();
			int buttonHeight = btnImportExport.getHeight();
			boolean mouseOnComponent = false;
			PointerInfo pointInfo = MouseInfo.getPointerInfo();
			if (pointInfo != null) {
				Point mousePosition = pointInfo.getLocation();
				SwingUtilities.convertPointFromScreen(mousePosition, btnImportExport);
				if (btnImportExport.contains(mousePosition)) {
					x = mousePosition.x;
					y = mousePosition.y;
					mouseOnComponent = true;
				}
			}

			if (!mouseOnComponent && buttonWidth > 0 && buttonHeight > 0) {
				x = buttonWidth / 2;
				y = buttonHeight / 2;
			}
			menuImportExport.show(btnImportExport, x, y);
		});

		pnlButtons.add(btnStart);
		pnlButtons.add(btnStop);
		pnlButtons.add(btnImportLinks);
		pnlButtons.add(btnParseLinks);
		pnlButtons.add(btnSortFiles);
		pnlButtons.add(btnImportExport);
		add(pnlButtons, BorderLayout.SOUTH);

		JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pnlStatus.add(lblRowCount);
		pnlStatus.add(lblStatus);
		add(pnlStatus, BorderLayout.NORTH);
		downloadQueueManager.addDownloadQueueManagerListener(new IDownloadQueueManagerListener() {
			@Override
			public void totalDownloadRateCalculated(double downloadRate) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateStatus();
					}
				});
			}

			@Override
			public void sessionDownloadedFilesChanged(int count) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateStatus();
					}
				});
			}

			@Override
			public void sessionDownloadedBytesChanged(long count) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateStatus();
					}
				});
			}

			@Override
			public void queueChanged(int queue, int openSlots, int maxSlots) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						long overallDownloadedFiles = settingsManager.getDownloadsSettings().getOverallDownloadedFiles();
						String overallDownloadedBytes = UnitFormatUtil.getSizeString(settingsManager.getDownloadsSettings().getOverallDownloadedBytes(), settingsManager.getSizeView());
						int sessionDownloadedFiles = downloadQueueManager.getSessionDownloadedFiles();
						String sessionDownloadedBytes = UnitFormatUtil.getSizeString(downloadQueueManager.getSessionDownloadedBytes(), settingsManager.getSizeView());
						String downloadRate = UnitFormatUtil.getBitrateString(downloadQueueManager.getTotalDownloadBitrate());
						if (downloadRate.isEmpty()) {
							downloadRate = Localization.getString("NotAvailable");
						}
						updateStatus(openSlots, maxSlots, overallDownloadedFiles, overallDownloadedBytes, sessionDownloadedFiles, sessionDownloadedBytes, downloadRate);
					}
				});
			}

			@Override
			public void downloadsComplete(int queue, int openSlots, int maxSlots) {
				// Nothing to do
			}

			@Override
			public void queueEmpty() {
				// Nothing to do
			}
		});

		popupMenu.add(menuItemChangeTargetByInput);
		popupMenu.add(menuItemChangeTargetBySelection);
		popupMenu.add(menuItemChangeTargetfilename);
		popupMenu.add(menuItemCopyURL);
		popupMenu.add(menuItemOpenURL);
		popupMenu.add(menuItemOpenThreadURL);
		popupMenu.add(menuItemActivate);
		popupMenu.add(menuItemDeactivate);
		popupMenu.add(menuItemDelete);

		menuItemChangeTargetByInput.addActionListener(e -> actionChangeTargetByInput());
		menuItemChangeTargetBySelection.addActionListener(e -> actionChangeTargetBySelection());
		menuItemChangeTargetfilename.addActionListener(e -> actionChangeTargetFilename());
		menuItemCopyURL.addActionListener(e -> actionCopyURLs());
		menuItemOpenURL.addActionListener(e -> actionOpenURLs());
		menuItemOpenThreadURL.addActionListener(e -> actionOpenThreadURLs());
		menuItemActivate.addActionListener(e -> actionActivate());
		menuItemDeactivate.addActionListener(e -> actionDeactivate());
		menuItemDelete.addActionListener(e -> actionDelete());

		itemImportHTML.addActionListener(e -> actionImportHTML());
		itemImportText.addActionListener(e -> actionImportTextfile());
		itemImportQueue.addActionListener(e -> actionImportQueue());
		itemExportQueue.addActionListener(e -> actionExportQueue());
		menuImportExport.add(itemImportHTML);
		menuImportExport.add(itemImportText);
		menuImportExport.add(itemImportQueue);
		menuImportExport.add(itemExportQueue);

		QueueColorRowRenderer crr = new QueueColorRowRenderer(settingsManager);
		jtQueue.setDefaultRenderer(Object.class, crr);

		updateRowCountLabel();
		updateStatus();

		// Register Key
		ActionMap am = getActionMap();
		InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		Object deleteKey = new Object();

		KeyStroke deleteStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		Action deleteAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (downloadQueueManager.isDownloading()) {
					return;
				}
				actionDelete();
			}
		};
		im.put(deleteStroke, deleteKey);
		am.put(deleteKey, deleteAction);

		Object changeTargetFilenameKey = new Object();
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
		this.jtQueue.getActionMap().put(changeTargetFilenameKey, changeTargetFilenameAction);
		this.jtQueue.getInputMap().put(changeTargetFilenameStroke, changeTargetFilenameKey);
	}

	/**
	 * Start
	 */
	private void actionStart() {
		executeInNewThread("StartDownloadThread-", queueManager::startDownload);
	}

	/**
	 * Stop
	 */
	private void actionStop() {
		executeInNewThread("StopDownloadThread-", queueManager::stopDownload);
	}

	/**
	 * CopyURLs
	 */
	private void actionCopyURLs() {
		StringJoiner content = new StringJoiner("\n");
		for (int selectedRow : jtQueue.getSelectedRows()) {
			content.add((String)model.getValueAt(jtQueue.convertRowIndexToModel(selectedRow), QueueTableModel.URL_COLUMN_INDEX));
		}

		if (content.length() > 0) {
			clipboardObserver.setClipboardContent(content.toString());
		}
	}

	/**
	 * OpenURLs
	 */
	private void actionOpenURLs() {
		for (int selectedRow : jtQueue.getSelectedRows()) {
			String url = (String)model.getValueAt(jtQueue.convertRowIndexToModel(selectedRow), QueueTableModel.URL_COLUMN_INDEX);
			FileExplorerUtil.openURL(url);
		}
	}

	/**
	 * OpenThreadURLs
	 */
	private void actionOpenThreadURLs() {
		for (int selectedRow : jtQueue.getSelectedRows()) {
			Pic pic = (Pic)model.getValueAt(jtQueue.convertRowIndexToModel(selectedRow), QueueTableModel.PROGRESS_COLUMN_INDEX);
			FileExplorerUtil.openURL(pic.getThreadURL());
		}
	}

	/**
	 * @param sortByIndex True if list should be sorted by (model) index, false otherwise
	 * @return Selected Pics
	 */
	private List<Pic> getSelectedPics(boolean sortByIndex) {
		int[] selectedRows = jtQueue.getSelectedRows();
		int[] selectedModelRows = TableUtil.convertRowIndexToModel(jtQueue, selectedRows, sortByIndex);

		List<Pic> selectedPics = new ArrayList<>();
		for (int selectedModelRow : selectedModelRows) {
			Pic pic = (Pic)model.getValueAt(selectedModelRow, QueueTableModel.PROGRESS_COLUMN_INDEX);
			selectedPics.add(pic);
		}
		return selectedPics;
	}

	/**
	 * Delete
	 */
	private void actionDelete() {
		int retval = JOptionPane.showConfirmDialog(parentWindow, Localization.getString("QueueReallyDelete"), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, Icons
				.getTangoSVGIcon("status/dialog-warning.svg", 32));
		if (retval == JOptionPane.NO_OPTION) {
			return;
		}

		disableComponents();

		List<Pic> picsToRemove = getSelectedPics(true);

		executeInNewThread("QueueDeleteThread-", () -> {
			ProgressObserver pg = new ProgressObserver();
			pg.progressModeChanged(true);
			pg.progressChanged(Localization.getString("DeleteEntries"));
			try {
				mainWindowAccess.addProgressObserver(pg);

				queueManager.removePics(picsToRemove);

				mainWindowAccess.setMessage(Localization.getString("EntriesDeleted"));
			} finally {
				mainWindowAccess.removeProgressObserver(pg);
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						enableComponents();
					}
				});
			}
		});
	}

	/**
	 * ImportHTML
	 */
	private void actionImportHTML() {
		executeInNewThread("QueueImportHTMLThread-", () -> new ImportHTML(parentWindow, mainWindowAccess, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, cookieManager, clipboardObserver)
				.importHTML());
	}

	/**
	 * ImportTextfile
	 */
	private void actionImportTextfile() {
		executeInNewThread("QueueImportTextFileThread-", () -> new ImportLinkList(parentWindow, mainWindowAccess, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver)
				.importLinkList());
	}

	/**
	 * ImportLinks
	 */
	private void actionImportLinks() {
		executeInNewThread("QueueImportLinksThread-", () -> {
			DownloadAddDialog dlg = new DownloadAddDialog(parentWindow, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver);
			dlg.setVisible(true);
		});
	}

	/**
	 * ParseLinks
	 */
	private void actionParseLinks() {
		executeInNewThread("QueueParseLinksThread-", () -> {
			ParsePagesDialog dlg = new ParsePagesDialog(parentWindow, mainWindowAccess, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, cookieManager, clipboardObserver);
			dlg.setVisible(true);
		});
	}

	/**
	 * ImportQueue
	 */
	private void actionImportQueue() {
		executeInNewThread("QueueImportQueueThread-", () -> new ImportQueue(parentWindow, mainWindowAccess, queueManager, settingsManager, hostManager).importQueue());
	}

	/**
	 * ExportQueue
	 */
	private void actionExportQueue() {
		if (downloadQueueManager.isDownloading()) {
			JOptionPane.showMessageDialog(parentWindow, Localization.getString("ExportWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		executeInNewThread("QueueExportQueueThread-", () -> new ExportQueue(parentWindow, mainWindowAccess, queueManager, settingsManager).exportQueue());
	}

	/**
	 * ChangeTargetByInput
	 */
	private void actionChangeTargetByInput() {
		List<Pic> picsToChange = getSelectedPics(false);
		if (picsToChange.isEmpty()) {
			return;
		}

		String defaultPath;
		if (picsToChange.size() == 1) {
			defaultPath = picsToChange.get(0).getTargetPath();
		} else {
			defaultPath = settingsManager.getSavePath();
		}

		String input = PathRenameDialog.showPathRenameDialog(parentWindow, defaultPath);
		if (input == null || input.length() <= 2) {
			return;
		}
		if (!input.endsWith("/") && !input.endsWith("\\")) {
			input += FileUtil.FILE_SEPERATOR;
		}

		String newPath = input;
		newPath = BHUtil.filterPath(newPath, settingsManager);
		newPath = BHUtil.reducePathLength(newPath, settingsManager);
		final String newPathToSet = newPath;

		picsToChange.stream().forEach(pic -> {
			pic.setTargetPath(newPathToSet);
			queueManager.updatePic(pic);
		});
	}

	/**
	 * ChangeTargetBySelection
	 */
	private void actionChangeTargetBySelection() {
		List<Pic> picsToChange = getSelectedPics(false);
		if (picsToChange.isEmpty()) {
			return;
		}

		File folder = FileDialogUtil.showFolderSaveDialog(this, settingsManager.getSavePath(), null);
		if (folder == null) {
			return;
		}
		String folderPath = folder.getAbsolutePath() + FileUtil.FILE_SEPERATOR;
		picsToChange.stream().forEach(pic -> {
			pic.setTargetPath(folderPath);
			queueManager.updatePic(pic);
		});
	}

	/**
	 * ChangeTargetFilename
	 */
	private void actionChangeTargetFilename() {
		List<Pic> picsToChange = getSelectedPics(false);
		if (picsToChange.isEmpty()) {
			return;
		}

		String defaultValue = picsToChange.get(0).getTargetFilename();
		String[] input = FileRenameDialog.showFileRenameDialog(parentWindow, "", defaultValue, picsToChange.size(), settingsManager);
		if (input == null) {
			return;
		}

		String value = input[0];
		int start = Integer.parseInt(input[1]);
		int step = Integer.parseInt(input[2]);
		String prefix = input[3];
		String suffix = input[4];
		boolean keepOriginal = !input[5].isEmpty();
		boolean clearFilename = !input[6].isEmpty();

		AtomicInteger indexForFilename = new AtomicInteger(start);
		picsToChange.stream().forEach(pic -> {
			String filenameToSet;
			if (clearFilename) {
				filenameToSet = "";
			} else {
				String newFilename;
				if (keepOriginal) {
					newFilename = prefix + pic.getTargetFilename() + suffix;
				} else {
					newFilename = prefix + value + suffix;
				}

				filenameToSet = FileUtil.getNumberedFilename(newFilename, indexForFilename.getAndAdd(step));
			}

			pic.setTargetFilename(filenameToSet);
			pic.setFixedTargetFilename(!clearFilename);
			queueManager.updatePic(pic);
		});
	}

	/**
	 * Sort
	 */
	private void actionSort() {
		final File folder = FileDialogUtil.showFolderOpenDialog(parentWindow, settingsManager.getSavePath(), null);
		if (folder != null) {
			executeInNewThread("QueueSortThread-", () -> {
				try (Stream<Path> stream = Files.list(folder.toPath())) {
					List<Path> files = stream.filter(Files::isRegularFile).toList();

					new ImportLocalFiles(parentWindow, mainWindowAccess, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver)
							.importLocalFiles(files, folder.getName());
				} catch (IOException e) {
					logger.error("Could not list files in directory: {}", folder, e);
				}
			});
		}
	}

	/**
	 * Activate
	 */
	private void actionActivate() {
		List<Pic> picsToUpdate = new ArrayList<>();
		for (int selectedRow : jtQueue.getSelectedRows()) {
			Pic pic = (Pic)model.getValueAt(jtQueue.convertRowIndexToModel(selectedRow), QueueTableModel.PROGRESS_COLUMN_INDEX);
			pic.setDeactivated(false, false);
			picsToUpdate.add(pic);
		}
		queueManager.updatePics(picsToUpdate);
		model.fireTableDataChanged();
	}

	/**
	 * Deactivate
	 */
	private void actionDeactivate() {
		List<Pic> picsToUpdate = new ArrayList<>();
		for (int selectedRow : jtQueue.getSelectedRows()) {
			Pic pic = (Pic)model.getValueAt(jtQueue.convertRowIndexToModel(selectedRow), QueueTableModel.PROGRESS_COLUMN_INDEX);
			pic.setDeactivated(true, false);
			picsToUpdate.add(pic);
		}
		queueManager.updatePics(picsToUpdate);
		model.fireTableDataChanged();
	}

	private void disableComponents() {
		jtQueue.setEnabled(false);
		btnStart.setEnabled(false);
		btnStop.setEnabled(false);
		btnImportExport.setEnabled(false);
		btnImportLinks.setEnabled(false);
		btnParseLinks.setEnabled(false);
		btnSortFiles.setEnabled(false);
	}

	private void enableComponents() {
		jtQueue.setEnabled(true);
		btnStart.setEnabled(true);
		btnStop.setEnabled(true);
		btnImportExport.setEnabled(true);
		btnImportLinks.setEnabled(true);
		btnParseLinks.setEnabled(true);
		btnSortFiles.setEnabled(true);
	}

	private void showTablePopupMenu(MouseEvent e) {
		boolean bIsDownloading = downloadQueueManager.isDownloading();
		boolean bTableEnabled = jtQueue.isEnabled();
		boolean enableMenuItems = !bIsDownloading && bTableEnabled;
		menuItemChangeTargetByInput.setEnabled(enableMenuItems);
		menuItemChangeTargetBySelection.setEnabled(enableMenuItems);
		menuItemChangeTargetfilename.setEnabled(enableMenuItems);
		menuItemActivate.setEnabled(enableMenuItems);
		menuItemDeactivate.setEnabled(enableMenuItems);
		menuItemDelete.setEnabled(enableMenuItems);
		SwingUtilities.updateComponentTreeUI(popupMenu);
		popupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * Update Row Count Label
	 */
	private void updateRowCountLabel() {
		int queueCount = jtQueue.getRowCount();
		lblRowCount.setText(Localization.getString("Queue") + ": " + queueCount + " | ");
	}

	/**
	 * Updates the status-display
	 */
	private void updateStatus() {
		int openDownloadSlots = downloadQueueManager.getOpenSlots();
		int connectionCount = downloadQueueManager.getMaxConnectionCount();
		long overallDownloadedFiles = settingsManager.getDownloadsSettings().getOverallDownloadedFiles();
		String overallDownloadedBytes = UnitFormatUtil.getSizeString(settingsManager.getDownloadsSettings().getOverallDownloadedBytes(), settingsManager.getSizeView());
		int sessionDownloadedFiles = downloadQueueManager.getSessionDownloadedFiles();
		String sessionDownloadedBytes = UnitFormatUtil.getSizeString(downloadQueueManager.getSessionDownloadedBytes(), settingsManager.getSizeView());
		String downloadRate = UnitFormatUtil.getBitrateString(downloadQueueManager.getTotalDownloadBitrate());
		if (downloadRate.isEmpty()) {
			downloadRate = Localization.getString("NotAvailable");
		}
		updateStatus(openDownloadSlots, connectionCount, overallDownloadedFiles, overallDownloadedBytes, sessionDownloadedFiles, sessionDownloadedBytes, downloadRate);
	}

	/**
	 * Updates the status-display
	 * 
	 * @param openDownloadSlots Open Download Slots
	 * @param connectionCount Connection Count
	 * @param overallDownloadedFiles Overall Downloaded Files
	 * @param overallDownloadedBytes Overall Downloaded Bytes
	 * @param sessionDownloadedFiles Session Downloaded Files
	 * @param sessionDownloadedBytes Session Downloaded Bytes
	 * @param downloadRate Download Rate
	 */
	private void updateStatus(int openDownloadSlots, int connectionCount, long overallDownloadedFiles, String overallDownloadedBytes, int sessionDownloadedFiles, String sessionDownloadedBytes,
			String downloadRate) {
		lblStatus.setText(Localization.getString("FreeSlots") + ": " + openDownloadSlots + "/" + connectionCount + " | " + Localization.getString("DownloadedFiles") + ": " + overallDownloadedFiles
				+ " (" + sessionDownloadedFiles + ") | " + Localization.getString("DownloadedBytes") + ": " + overallDownloadedBytes + " (" + sessionDownloadedBytes + ") | "
				+ Localization.getString("DownloadBitrate") + ": " + downloadRate);
	}

	/**
	 * updateColWidthsToSettingsManager
	 */
	private void updateColWidthsToSettingsManager() {
		if (!settingsManager.isSaveTableColumnSizes()) {
			return;
		}
		settingsManager.setColWidthsQueue(TableUtil.serializeColWidthSetting(jtQueue));
		settingsManager.writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (!settingsManager.isSaveTableColumnSizes()) {
			return;
		}
		TableUtil.applyColWidths(jtQueue, settingsManager.getColWidthsQueue());
	}

	/**
	 * Execute task in new Thread
	 * 
	 * @param threadNamePrefix Thread Name Prefix
	 * @param task Task
	 */
	private void executeInNewThread(String threadNamePrefix, Runnable task) {
		Thread t = new Thread(task);
		t.setName(threadNamePrefix + t.threadId());
		t.start();
	}
}

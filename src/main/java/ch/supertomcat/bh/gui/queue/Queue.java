package ch.supertomcat.bh.gui.queue;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

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
import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.Icons;
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
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.dialog.FileDialogUtil;
import ch.supertomcat.supertomcatutils.gui.formatter.UnitFormatUtil;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Queue-Panel
 */
public class Queue extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * Logger for this class
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
	 * Button
	 */
	private JButton btnStart = new JButton(Localization.getString("Start"), Icons.getTangoIcon("actions/media-playback-start.png", 16));

	/**
	 * Button
	 */
	private JButton btnStop = new JButton(Localization.getString("Stop"), Icons.getTangoIcon("actions/media-playback-stop.png", 16));

	/**
	 * Button
	 */
	private JButton btnImport = new JButton(Localization.getString("ImportExport"), Icons.getTangoIcon("actions/document-open.png", 16));

	/**
	 * PopupMenu
	 */
	private JPopupMenu menuImport = new JPopupMenu(Localization.getString("ImportExport"));

	/**
	 * MenuItem
	 */
	private JMenuItem itemImportHTML = new JMenuItem(Localization.getString("ImportHTML"), Icons.getTangoIcon("mimetypes/text-html.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemImportText = new JMenuItem(Localization.getString("ImportText"), Icons.getTangoIcon("mimetypes/text-x-generic.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemImportQueue = new JMenuItem(Localization.getString("QueueImport"), Icons.getTangoIcon("actions/document-open.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemExportQueue = new JMenuItem(Localization.getString("ExportQueue"), Icons.getTangoIcon("actions/document-save-as.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemSort = new JMenuItem(Localization.getString("SortFiles"), Icons.getBHIcon("actions/data-transfer.png", 16));

	/**
	 * MenuItem
	 */
	private JButton btnImportLinks = new JButton(Localization.getString("ImportLinks"), Icons.getTangoIcon("emblems/emblem-symbolic-link.png", 16));

	/**
	 * MenuItem
	 */
	private JButton btnParseLinks = new JButton(Localization.getString("ParseLinks"), Icons.getTangoIcon("emblems/emblem-symbolic-link.png", 16));

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
	private JMenuItem menuItemDelete = new JMenuItem(Localization.getString("Delete"), Icons.getTangoIcon("actions/edit-delete.png", 16));

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
	private JMenuItem menuItemOpenThreadURL = new JMenuItem(Localization.getString("OpenThreadURL"), Icons.getTangoIcon("apps/internet-web-browser.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemActivate = new JMenuItem(Localization.getString("Activate"), Icons.getTangoIcon("actions/media-record.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemDeactivate = new JMenuItem(Localization.getString("Deactivate"), Icons.getTangoIcon("emblems/emblem-readonly.png", 16));

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
				if (e.getSource() == jtQueue && e.isPopupTrigger() && jtQueue.getSelectedRowCount() > 0) {
					showTablePopupMenu(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getSource() == jtQueue && e.isPopupTrigger() && jtQueue.getSelectedRowCount() > 0) {
					showTablePopupMenu(e);
				}
			}
		});
		jtQueue.getTableHeader().setReorderingAllowed(false);

		jtQueue.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		jtQueue.setRowHeight(TableUtil.calculateRowHeight(jtQueue, false, true));

		add(jsp, BorderLayout.CENTER);

		btnStart.setMnemonic(KeyEvent.VK_S);
		btnStop.setMnemonic(KeyEvent.VK_T);
		btnImport.setMnemonic(KeyEvent.VK_I);
		btnImportLinks.setMnemonic(KeyEvent.VK_L);
		btnParseLinks.setMnemonic(KeyEvent.VK_P);

		btnStart.addActionListener(e -> actionStart());
		btnStop.addActionListener(e -> actionStop());
		btnImport.addActionListener(e -> {
			SwingUtilities.updateComponentTreeUI(menuImport);
			menuImport.show(btnImport, menuImport.getX(), menuImport.getY());
		});
		btnImport.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getSource() == btnImport && e.isPopupTrigger()) {
					menuImport.show(btnImport, e.getX(), e.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getSource() == btnImport && e.isPopupTrigger()) {
					menuImport.show(btnImport, e.getX(), e.getY());
				}
			}
		});
		btnStop.setToolTipText(Localization.getString("StopTooltip"));
		pnlButtons.add(btnStart);
		pnlButtons.add(btnStop);
		pnlButtons.add(btnImport);
		pnlButtons.add(btnImportLinks);
		pnlButtons.add(btnParseLinks);
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
						long overallDownloadedFiles = settingsManager.getOverallDownloadedFiles();
						String overallDownloadedBytes = UnitFormatUtil.getSizeString(settingsManager.getOverallDownloadedBytes(), settingsManager.getSizeView());
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
		btnImportLinks.addActionListener(e -> actionImportLinks());
		btnParseLinks.addActionListener(e -> actionParseLinks());
		itemExportQueue.addActionListener(e -> actionExportQueue());
		itemImportQueue.addActionListener(e -> actionImportQueue());
		itemSort.addActionListener(e -> actionSort());

		menuImport.add(itemImportHTML);
		menuImport.add(itemImportText);
		menuImport.add(itemImportQueue);
		menuImport.add(itemExportQueue);
		menuImport.add(itemSort);

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
		Thread t = new Thread(() -> queueManager.startDownload());
		t.start();
	}

	/**
	 * Stop
	 */
	private void actionStop() {
		Thread t = new Thread(() -> queueManager.stopDownload());
		t.start();
	}

	/**
	 * CopyURLs
	 */
	private void actionCopyURLs() {
		StringJoiner content = new StringJoiner("\n");
		synchronized (queueManager.getSyncObject()) {
			for (int selectedRow : jtQueue.getSelectedRows()) {
				content.add((String)model.getValueAt(jtQueue.convertRowIndexToModel(selectedRow), 0));
			}
		}
		if (content.length() > 0) {
			clipboardObserver.setClipboardContent(content.toString());
		}
	}

	/**
	 * OpenURLs
	 */
	private void actionOpenURLs() {
		final List<String> urls = new ArrayList<>();
		synchronized (queueManager.getSyncObject()) {
			for (int selectedRow : jtQueue.getSelectedRows()) {
				urls.add((String)model.getValueAt(jtQueue.convertRowIndexToModel(selectedRow), 0));
			}
		}

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				for (String url : urls) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(new URI(url));
						} catch (IOException | URISyntaxException e) {
							logger.error("Could not open URL: {}", url, e);
						}
					} else {
						logger.error("Could not open URL, because Desktop is not supported: {}", url);
					}
				}
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * OpenThreadURLs
	 */
	private void actionOpenThreadURLs() {
		final List<String> urls = new ArrayList<>();
		synchronized (queueManager.getSyncObject()) {
			for (int selectedRow : jtQueue.getSelectedRows()) {
				Pic pic = queueManager.getPicByIndex(jtQueue.convertRowIndexToModel(selectedRow));
				if (pic != null) {
					String url = pic.getThreadURL();
					if (!url.isEmpty()) {
						urls.add(url);
					}
				}
			}
		}

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				for (String url : urls) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(new URI(url));
						} catch (IOException | URISyntaxException e) {
							logger.error("Could not open URL: {}", url, e);
						}
					} else {
						logger.error("Could not open URL, because Desktop is not supported: {}", url);
					}
				}
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		t = null;
	}

	/**
	 * Delete
	 */
	private void actionDelete() {
		int retval = JOptionPane.showConfirmDialog(parentWindow, Localization.getString("QueueReallyDelete"), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, Icons
				.getTangoIcon("status/dialog-warning.png", 32));
		if (retval == JOptionPane.NO_OPTION) {
			return;
		}

		disableComponents();

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ProgressObserver pg = new ProgressObserver();
					mainWindowAccess.addProgressObserver(pg);
					pg.progressModeChanged(true);
					pg.progressChanged(Localization.getString("DeleteEntries"));
					synchronized (queueManager.getSyncObject()) {
						int[] selectedRows = jtQueue.getSelectedRows();
						int[] selectedModelRows = TableUtil.convertRowIndexToModel(jtQueue, selectedRows, true);

						// Convert first removed row index, before removing any rows
						int firstRemovedRowViewIndex = jtQueue.convertRowIndexToView(selectedModelRows[0]);

						queueManager.removePics(selectedModelRows);

						int rowCount = jtQueue.getRowCount();
						int aboveFirstRemovedRowViewIndex = firstRemovedRowViewIndex - 1;
						if (firstRemovedRowViewIndex < rowCount) {
							jtQueue.setRowSelectionInterval(firstRemovedRowViewIndex, firstRemovedRowViewIndex);
						} else if (aboveFirstRemovedRowViewIndex >= 0 && aboveFirstRemovedRowViewIndex < rowCount) {
							jtQueue.setRowSelectionInterval(aboveFirstRemovedRowViewIndex, aboveFirstRemovedRowViewIndex);
						}
					}
					mainWindowAccess.removeProgressObserver(pg);
					mainWindowAccess.setMessage(Localization.getString("EntriesDeleted"));
				} finally {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							enableComponents();
						}
					});
				}
			}
		});
		t.setName("QueueDeleteThread-" + t.getId());
		t.start();
	}

	/**
	 * ImportHTML
	 */
	private void actionImportHTML() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				new ImportHTML(parentWindow, mainWindowAccess, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, cookieManager, clipboardObserver).importHTML();
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * ImportTextfile
	 */
	private void actionImportTextfile() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				new ImportLinkList(parentWindow, mainWindowAccess, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver).importLinkList();
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * ImportLinks
	 */
	private void actionImportLinks() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				DownloadAddDialog dlg = new DownloadAddDialog(parentWindow, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver);
				dlg.setVisible(true);
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * ParseLinks
	 */
	private void actionParseLinks() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				ParsePagesDialog dlg = new ParsePagesDialog(parentWindow, mainWindowAccess, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, cookieManager, clipboardObserver);
				dlg.setVisible(true);
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * ImportQueue
	 */
	private void actionImportQueue() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				new ImportQueue(parentWindow, mainWindowAccess, queueManager, settingsManager).importQueue();
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * ExportQueue
	 */
	private void actionExportQueue() {
		if (downloadQueueManager.isDownloading()) {
			JOptionPane.showMessageDialog(parentWindow, Localization.getString("ExportWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				new ExportQueue(parentWindow, mainWindowAccess, queueManager, settingsManager).exportQueue();
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * ChangeTargetByInput
	 */
	private void actionChangeTargetByInput() {
		synchronized (queueManager.getSyncObject()) {
			int[] s = jtQueue.getSelectedRows();
			String defaultPath = settingsManager.getSavePath();
			if (s.length == 1) {
				defaultPath = queueManager.getPicByIndex(jtQueue.convertRowIndexToModel(s[0])).getTargetPath();
			}
			String input = PathRenameDialog.showPathRenameDialog(parentWindow, defaultPath);
			if ((input != null) && (input.length() > 2)) {
				boolean b1 = input.endsWith("/");
				boolean b2 = input.endsWith("\\");
				if ((b1 == false) && (b2 == false)) {
					input += FileUtil.FILE_SEPERATOR;
				}
				for (int i = 0; i < s.length; i++) {
					String newPath = input;
					newPath = BHUtil.filterPath(newPath, settingsManager);
					newPath = FileUtil.reducePathLength(newPath);

					Pic pic = queueManager.getPicByIndex(jtQueue.convertRowIndexToModel(s[i]));
					if (pic != null) {
						pic.setTargetPath(newPath);
						queueManager.updatePic(pic);
					}
				}
				queueManager.asyncSaveDatabase();
			}
		}
	}

	/**
	 * ChangeTargetBySelection
	 */
	private void actionChangeTargetBySelection() {
		synchronized (queueManager.getSyncObject()) {
			File file = FileDialogUtil.showFolderSaveDialog(this, settingsManager.getSavePath(), null);
			if (file != null) {
				String folder = file.getAbsolutePath() + FileUtil.FILE_SEPERATOR;
				int s[] = jtQueue.getSelectedRows();
				for (int i = 0; i < s.length; i++) {
					Pic pic = queueManager.getPicByIndex(jtQueue.convertRowIndexToModel(s[i]));
					if (pic != null) {
						pic.setTargetPath(folder);
						queueManager.updatePic(pic);
					}
				}
				file = null;
				queueManager.asyncSaveDatabase();
			}
		}
	}

	/**
	 * ChangeTargetFilename
	 */
	private void actionChangeTargetFilename() {
		synchronized (queueManager.getSyncObject()) {
			String defaultvalue = "";
			int s[] = jtQueue.getSelectedRows();
			if (s.length > 0) {
				defaultvalue = (String)model.getValueAt(jtQueue.convertRowIndexToModel(s[0]), 1);
				defaultvalue = defaultvalue.substring(defaultvalue.lastIndexOf(FileUtil.FILE_SEPERATOR) + 1);
				String input[] = FileRenameDialog.showFileRenameDialog(parentWindow, "", defaultvalue, s.length, settingsManager);
				if ((input != null)) {
					int index = Integer.parseInt(input[1]);
					int step = Integer.parseInt(input[2]);
					boolean keepOriginal = (input[5].length() > 0);
					boolean clearFilename = (input[6].length() > 0);
					for (int i = 0; i < s.length; i++) {
						int modelIndex = jtQueue.convertRowIndexToModel(s[i]);
						String out = "";
						if (clearFilename == false) {
							String fname = input[0];
							if (keepOriginal) {
								fname = (String)model.getValueAt(modelIndex, 1);
								fname = fname.substring(fname.lastIndexOf(FileUtil.FILE_SEPERATOR) + 1);
							}
							fname = input[3] + fname + input[4];
							out = FileUtil.getNumberedFilename(fname, index);
						}

						Pic pic = queueManager.getPicByIndex(modelIndex);
						if (pic != null) {
							pic.setTargetFilename(out);
							pic.setFixedTargetFilename(true);
							queueManager.updatePic(pic);
						}

						index += step;
					}
					queueManager.asyncSaveDatabase();
				}
			}
		}
	}

	/**
	 * Sort
	 */
	private void actionSort() {
		final File folder = FileDialogUtil.showFolderOpenDialog(parentWindow, settingsManager.getSavePath(), null);
		if (folder != null) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					File files[] = folder.listFiles();
					new ImportLocalFiles(parentWindow, mainWindowAccess, logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver)
							.importLocalFiles(files, folder.getName());
				}
			});
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}
	}

	/**
	 * Activate
	 */
	private void actionActivate() {
		synchronized (queueManager.getSyncObject()) {
			int s[] = jtQueue.getSelectedRows();
			List<Pic> picsToUpdate = new ArrayList<>();
			for (int i = 0; i < s.length; i++) {
				Pic pic = queueManager.getPicByIndex(jtQueue.convertRowIndexToModel(s[i]));
				pic.setDeactivated(false, false);
				picsToUpdate.add(pic);
			}
			queueManager.updatePics(picsToUpdate);
			model.fireTableDataChanged();
			queueManager.asyncSaveDatabase();
		}
	}

	/**
	 * Deactivate
	 */
	private void actionDeactivate() {
		synchronized (queueManager.getSyncObject()) {
			int s[] = jtQueue.getSelectedRows();
			List<Pic> picsToUpdate = new ArrayList<>();
			for (int i = 0; i < s.length; i++) {
				Pic pic = queueManager.getPicByIndex(jtQueue.convertRowIndexToModel(s[i]));
				pic.setDeactivated(true, false);
				picsToUpdate.add(pic);
			}
			queueManager.updatePics(picsToUpdate);
			model.fireTableDataChanged();
			queueManager.asyncSaveDatabase();
		}
	}

	private void disableComponents() {
		jtQueue.setEnabled(false);
		btnStart.setEnabled(false);
		btnStop.setEnabled(false);
		btnImport.setEnabled(false);
		btnImportLinks.setEnabled(false);
		btnParseLinks.setEnabled(false);
	}

	private void enableComponents() {
		jtQueue.setEnabled(true);
		btnStart.setEnabled(true);
		btnStop.setEnabled(true);
		btnImport.setEnabled(true);
		btnImportLinks.setEnabled(true);
		btnParseLinks.setEnabled(true);
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
		long overallDownloadedFiles = settingsManager.getOverallDownloadedFiles();
		String overallDownloadedBytes = UnitFormatUtil.getSizeString(settingsManager.getOverallDownloadedBytes(), settingsManager.getSizeView());
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
		if (settingsManager.isSaveTableColumnSizes() == false) {
			return;
		}
		settingsManager.setColWidthsQueue(TableUtil.serializeColWidthSetting(jtQueue));
		settingsManager.writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (settingsManager.isSaveTableColumnSizes() == false) {
			return;
		}
		TableUtil.applyColWidths(jtQueue, settingsManager.getColWidthsQueue());
	}
}

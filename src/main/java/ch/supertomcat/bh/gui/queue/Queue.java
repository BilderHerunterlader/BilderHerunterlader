package ch.supertomcat.bh.gui.queue;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import ch.supertomcat.bh.importexport.ExportQueue;
import ch.supertomcat.bh.importexport.ImportHTML;
import ch.supertomcat.bh.importexport.ImportLinkList;
import ch.supertomcat.bh.importexport.ImportLocalFiles;
import ch.supertomcat.bh.importexport.ImportQueue;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.queue.IDownloadQueueManagerListener;
import ch.supertomcat.bh.queue.QueueManager;
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
public class Queue extends JPanel implements ActionListener, IDownloadQueueManagerListener, MouseListener, TableColumnModelListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 5907100131845566233L;

	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Synchronization Object for changes of the table. This object is used instead of
	 * the Queue (this) itself to prevent deadlocks, when EventQueue.invokeAndWait is used.
	 * 
	 * A deadlock happened when SwingUtilities.updateComponentTreeUI was called, while another Thread called picProgressBarUpdated.
	 * picProgressBarUpdated locked the Queue(this) and SwingUtilities.updateComponentTreeUI did too. This causes the deadlock.
	 */
	private Object syncObject = new Object();

	/**
	 * TabelModel
	 */
	private QueueTableModel model;

	/**
	 * Table
	 */
	private JTable jtQueue;

	/**
	 * Renderer for ProgressBars
	 */
	private QueueProgressColumnRenderer pcr = new QueueProgressColumnRenderer(SettingsManager.instance());

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
	private JLabel lblRowCount = new JLabel(Localization.getString("Queue") + ": 0 | ");

	/**
	 * Label
	 */
	private JLabel lblStatus = new JLabel(Localization.getString("FreeSlots") + ": " + SettingsManager.instance().getConnections() + "/" + SettingsManager.instance().getConnections() + " | "
			+ Localization.getString("DownloadedFiles") + ": " + SettingsManager.instance().getOverallDownloadedFiles() + " (0) | " + Localization.getString("DownloadedBytes") + ": "
			+ UnitFormatUtil.getSizeString(SettingsManager.instance().getOverallDownloadedBytes(), SettingsManager.instance().getSizeView()) + " (0) | " + Localization.getString("DownloadBitrate")
			+ ": " + Localization.getString("NotAvailable"));

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
	 * QueueColorRowRenderer
	 */
	private QueueColorRowRenderer crr = new QueueColorRowRenderer();

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
	 * @param clipboardObserver Clipboard Observer
	 */
	public Queue(JFrame parentWindow, MainWindowAccess mainWindowAccess, QueueManager queueManager, DownloadQueueManager downloadQueueManager, LogManager logManager,
			ClipboardObserver clipboardObserver) {
		this.parentWindow = parentWindow;
		this.mainWindowAccess = mainWindowAccess;
		this.queueManager = queueManager;
		this.downloadQueueManager = downloadQueueManager;
		this.logManager = logManager;
		this.clipboardObserver = clipboardObserver;

		setLayout(new BorderLayout());

		this.model = this.queueManager.getTableModel();
		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.INSERT || e.getType() == TableModelEvent.DELETE) {
					updateRowCountLabel();
				}
			}
		});
		jtQueue = new JTable(model);
		jsp = new JScrollPane(jtQueue);

		TableUtil.internationalizeColumns(jtQueue);

		jtQueue.getColumn("Progress").setCellRenderer(pcr);

		int urlOrTargetTableHeaderWidth = TableUtil.calculateColumnHeaderWidth(jtQueue, jtQueue.getColumn("URL"), 47);
		jtQueue.getColumn("URL").setPreferredWidth(urlOrTargetTableHeaderWidth);
		jtQueue.getColumn("Target").setPreferredWidth(urlOrTargetTableHeaderWidth);
		updateColWidthsFromSettingsManager();
		jtQueue.getColumnModel().addColumnModelListener(this);
		jtQueue.addMouseListener(this);
		jtQueue.getTableHeader().setReorderingAllowed(false);

		jtQueue.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		jtQueue.setRowHeight(TableUtil.calculateRowHeight(jtQueue, false, true));

		add(jsp, BorderLayout.CENTER);

		btnStart.setMnemonic(KeyEvent.VK_S);
		btnStop.setMnemonic(KeyEvent.VK_T);
		btnImport.setMnemonic(KeyEvent.VK_I);
		btnImportLinks.setMnemonic(KeyEvent.VK_L);
		btnParseLinks.setMnemonic(KeyEvent.VK_P);

		btnStart.addActionListener(this);
		btnStop.addActionListener(this);
		btnImport.addActionListener(this);
		btnImport.addMouseListener(this);
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
		downloadQueueManager.addDownloadQueueManagerListener(this);

		popupMenu.add(menuItemChangeTargetByInput);
		popupMenu.add(menuItemChangeTargetBySelection);
		popupMenu.add(menuItemChangeTargetfilename);
		popupMenu.add(menuItemCopyURL);
		popupMenu.add(menuItemOpenURL);
		popupMenu.add(menuItemOpenThreadURL);
		popupMenu.add(menuItemActivate);
		popupMenu.add(menuItemDeactivate);
		popupMenu.add(menuItemDelete);

		menuItemChangeTargetByInput.addActionListener(this);
		menuItemChangeTargetBySelection.addActionListener(this);
		menuItemChangeTargetfilename.addActionListener(this);
		menuItemCopyURL.addActionListener(this);
		menuItemOpenURL.addActionListener(this);
		menuItemOpenThreadURL.addActionListener(this);
		menuItemActivate.addActionListener(this);
		menuItemDeactivate.addActionListener(this);
		menuItemDelete.addActionListener(this);

		itemImportHTML.addActionListener(this);
		itemImportText.addActionListener(this);
		btnImportLinks.addActionListener(this);
		btnParseLinks.addActionListener(this);
		itemExportQueue.addActionListener(this);
		itemImportQueue.addActionListener(this);
		itemSort.addActionListener(this);

		menuImport.add(itemImportHTML);
		menuImport.add(itemImportText);
		menuImport.add(itemImportQueue);
		menuImport.add(itemExportQueue);
		menuImport.add(itemSort);

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
		synchronized (syncObject) {
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
		synchronized (syncObject) {
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
			synchronized (syncObject) {
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
						synchronized (syncObject) {
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
				new ImportHTML(parentWindow, mainWindowAccess, logManager, queueManager, clipboardObserver).importHTML();
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
				new ImportLinkList(parentWindow, mainWindowAccess, logManager, queueManager, clipboardObserver).importLinkList();
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
				DownloadAddDialog dlg = new DownloadAddDialog(parentWindow, logManager, queueManager, clipboardObserver);
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
				ParsePagesDialog dlg = new ParsePagesDialog(parentWindow, mainWindowAccess, logManager, queueManager, clipboardObserver);
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
				new ImportQueue(parentWindow, mainWindowAccess, queueManager).importQueue();
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
				new ExportQueue(parentWindow, mainWindowAccess, queueManager).exportQueue();
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
			synchronized (syncObject) {
				int[] s = jtQueue.getSelectedRows();
				String defaultPath = SettingsManager.instance().getSavePath();
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
						newPath = BHUtil.filterPath(newPath);
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
	}

	/**
	 * ChangeTargetBySelection
	 */
	private void actionChangeTargetBySelection() {
		synchronized (queueManager.getSyncObject()) {
			synchronized (syncObject) {
				File file = FileDialogUtil.showFolderSaveDialog(this, SettingsManager.instance().getSavePath(), null);
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
	}

	/**
	 * ChangeTargetFilename
	 */
	private void actionChangeTargetFilename() {
		synchronized (queueManager.getSyncObject()) {
			synchronized (syncObject) {
				String defaultvalue = "";
				int s[] = jtQueue.getSelectedRows();
				if (s.length > 0) {
					defaultvalue = (String)model.getValueAt(jtQueue.convertRowIndexToModel(s[0]), 1);
					defaultvalue = defaultvalue.substring(defaultvalue.lastIndexOf(FileUtil.FILE_SEPERATOR) + 1);
					String input[] = FileRenameDialog.showFileRenameDialog(parentWindow, "", defaultvalue, s.length);
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
	}

	/**
	 * Sort
	 */
	private void actionSort() {
		synchronized (syncObject) {
			final File folder = FileDialogUtil.showFolderOpenDialog(parentWindow, SettingsManager.instance().getSavePath(), null);
			if (folder != null) {

				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						File files[] = folder.listFiles();
						new ImportLocalFiles(parentWindow, mainWindowAccess, logManager, queueManager, clipboardObserver).importLocalFiles(files);
					}
				});
				t.setPriority(Thread.MIN_PRIORITY);
				t.start();
			}
		}
	}

	/**
	 * Activate
	 */
	private void actionActivate() {
		synchronized (queueManager.getSyncObject()) {
			synchronized (syncObject) {
				int s[] = jtQueue.getSelectedRows();
				for (int i = 0; i < s.length; i++) {
					queueManager.getPicByIndex(jtQueue.convertRowIndexToModel(s[i])).setDeactivated(false);
				}
				model.fireTableDataChanged();
				queueManager.asyncSaveDatabase();
			}
		}
	}

	/**
	 * Deactivate
	 */
	private void actionDeactivate() {
		synchronized (queueManager.getSyncObject()) {
			synchronized (syncObject) {
				int s[] = jtQueue.getSelectedRows();
				for (int i = 0; i < s.length; i++) {
					queueManager.getPicByIndex(jtQueue.convertRowIndexToModel(s[i])).setDeactivated(true);
				}
				model.fireTableDataChanged();
				queueManager.asyncSaveDatabase();
			}
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnStart) {
			actionStart();
		} else if (e.getSource() == btnStop) {
			actionStop();
		} else if (e.getSource() == menuItemCopyURL) {
			actionCopyURLs();
		} else if (e.getSource() == menuItemOpenURL) {
			actionOpenURLs();
		} else if (e.getSource() == menuItemDelete) {
			actionDelete();
		} else if (e.getSource() == itemImportHTML) {
			actionImportHTML();
		} else if (e.getSource() == itemImportText) {
			actionImportTextfile();
		} else if (e.getSource() == btnImportLinks) {
			actionImportLinks();
		} else if (e.getSource() == btnParseLinks) {
			actionParseLinks();
		} else if (e.getSource() == itemImportQueue) {
			actionImportQueue();
		} else if (e.getSource() == itemExportQueue) {
			actionExportQueue();
		} else if (e.getSource() == itemSort) {
			actionSort();
		} else if (e.getSource() == menuItemChangeTargetByInput) {
			actionChangeTargetByInput();
		} else if (e.getSource() == menuItemChangeTargetBySelection) {
			actionChangeTargetBySelection();
		} else if (e.getSource() == menuItemChangeTargetfilename) {
			actionChangeTargetFilename();
		} else if (e.getSource() == menuItemOpenThreadURL) {
			actionOpenThreadURLs();
		} else if (e.getSource() == menuItemActivate) {
			actionActivate();
		} else if (e.getSource() == menuItemDeactivate) {
			actionDeactivate();
		} else if (e.getSource() == btnImport) {
			SwingUtilities.updateComponentTreeUI(menuImport);
			menuImport.show(btnImport, menuImport.getX(), menuImport.getY());
		}
	}

	@Override
	public void queueChanged(final int queue, final int openSlots, final int maxSlots) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				long overallDownloadedFiles = SettingsManager.instance().getOverallDownloadedFiles();
				String overallDownloadedBytes = UnitFormatUtil.getSizeString(SettingsManager.instance().getOverallDownloadedBytes(), SettingsManager.instance().getSizeView());
				int sessionDownloadedFiles = downloadQueueManager.getSessionDownloadedFiles();
				String sessionDownloadedBytes = UnitFormatUtil.getSizeString(downloadQueueManager.getSessionDownloadedBytes(), SettingsManager.instance().getSizeView());
				String downloadRate = UnitFormatUtil.getBitrateString(downloadQueueManager.getDownloadBitrate());
				if (downloadRate.isEmpty()) {
					downloadRate = Localization.getString("NotAvailable");
				}
				updateStatus(openSlots, maxSlots, overallDownloadedFiles, overallDownloadedBytes, sessionDownloadedFiles, sessionDownloadedBytes, downloadRate);
			}
		});
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

	@Override
	public void downloadsComplete(int queue, int openSlots, int maxSlots) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getSource() == btnImport && e.isPopupTrigger()) {
			menuImport.show(btnImport, e.getX(), e.getY());
		} else if (e.getSource() == jtQueue && e.isPopupTrigger() && jtQueue.getSelectedRowCount() > 0) {
			showTablePopupMenu(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getSource() == btnImport && e.isPopupTrigger()) {
			menuImport.show(btnImport, e.getX(), e.getY());
		} else if (e.getSource() == jtQueue && e.isPopupTrigger() && jtQueue.getSelectedRowCount() > 0) {
			showTablePopupMenu(e);
		}
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
		int openDownloadSlots = downloadQueueManager.getOpenDownloadSlots();
		int connectionCount = downloadQueueManager.getConnectionCount();
		long overallDownloadedFiles = SettingsManager.instance().getOverallDownloadedFiles();
		String overallDownloadedBytes = UnitFormatUtil.getSizeString(SettingsManager.instance().getOverallDownloadedBytes(), SettingsManager.instance().getSizeView());
		int sessionDownloadedFiles = downloadQueueManager.getSessionDownloadedFiles();
		String sessionDownloadedBytes = UnitFormatUtil.getSizeString(downloadQueueManager.getSessionDownloadedBytes(), SettingsManager.instance().getSizeView());
		String downloadRate = UnitFormatUtil.getBitrateString(downloadQueueManager.getDownloadBitrate());
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
	public void sessionDownloadedFilesChanged(int count) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateStatus();
			}
		});
	}

	@Override
	public void totalDownloadRateCalculated(double downloadRate) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateStatus();
			}
		});
	}

	/**
	 * updateColWidthsToSettingsManager
	 */
	private void updateColWidthsToSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false) {
			return;
		}
		SettingsManager.instance().setColWidthsQueue(TableUtil.serializeColWidthSetting(jtQueue));
		SettingsManager.instance().writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false) {
			return;
		}
		TableUtil.applyColWidths(jtQueue, SettingsManager.instance().getColWidthsQueue());
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
}

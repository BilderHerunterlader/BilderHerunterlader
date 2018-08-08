package ch.supertomcat.bh.gui.queue;

import static ch.supertomcat.supertomcattools.fileiotools.FileTool.*;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.clipboard.ClipboardObserver;
import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.gui.Main;
import ch.supertomcat.bh.gui.renderer.QueueColorRowRenderer;
import ch.supertomcat.bh.gui.renderer.QueueProgressColumnRenderer;
import ch.supertomcat.bh.importexport.ExportQueue;
import ch.supertomcat.bh.importexport.ImportHTML;
import ch.supertomcat.bh.importexport.ImportLinkList;
import ch.supertomcat.bh.importexport.ImportLocalFiles;
import ch.supertomcat.bh.importexport.ImportQueue;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.PicState;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.queue.IDownloadQueueManagerListener;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.queue.QueueManagerListener;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcattools.fileiotools.FileTool;
import ch.supertomcat.supertomcattools.guitools.FileDialogTool;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.TableTool;
import ch.supertomcat.supertomcattools.guitools.UnitFormatTool;
import ch.supertomcat.supertomcattools.guitools.progressmonitor.ProgressObserver;

/**
 * Queue-Panel
 */
public class Queue extends JPanel implements ActionListener, QueueManagerListener, IDownloadQueueManagerListener, MouseListener, TableColumnModelListener {
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
	private QueueTableModel model = new QueueTableModel();

	/**
	 * Table
	 */
	private JTable jtQueue = new JTable(model);

	/**
	 * Renderer for ProgressBars
	 */
	private QueueProgressColumnRenderer pcr = new QueueProgressColumnRenderer();

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
	 * Label
	 */
	private JLabel lblStatus = new JLabel(Localization.getString("Queue") + ": 0 | " + Localization.getString("FreeSlots") + ": " + SettingsManager.instance().getConnections() + "/"
			+ SettingsManager.instance().getConnections() + " | " + Localization.getString("DownloadedFiles") + ": " + SettingsManager.instance().getOverallDownloadedFiles() + " (0) | "
			+ Localization.getString("DownloadedBytes") + ": " + UnitFormatTool.getSizeString(SettingsManager.instance().getOverallDownloadedBytes(), SettingsManager.instance().getSizeView())
			+ " (0) | " + Localization.getString("DownloadBitrate") + ": " + Localization.getString("NotAvailable"));

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
	private JScrollPane jsp = new JScrollPane(jtQueue);

	/**
	 * QueueColorRowRenderer
	 */
	private QueueColorRowRenderer crr = new QueueColorRowRenderer();

	/**
	 * Constructor
	 */
	public Queue() {
		setLayout(new BorderLayout());

		TableTool.internationalizeColumns(jtQueue);

		jtQueue.getColumn("Progress").setCellRenderer(pcr);

		int urlOrTargetTableHeaderWidth = TableTool.calculateColumnHeaderWidth(jtQueue, jtQueue.getColumn("URL"), 47);
		jtQueue.getColumn("URL").setPreferredWidth(urlOrTargetTableHeaderWidth);
		jtQueue.getColumn("Target").setPreferredWidth(urlOrTargetTableHeaderWidth);
		updateColWidthsFromSettingsManager();
		jtQueue.getColumnModel().addColumnModelListener(this);
		jtQueue.addMouseListener(this);
		jtQueue.getTableHeader().setReorderingAllowed(false);

		jtQueue.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		jtQueue.setRowHeight(TableTool.calculateRowHeight(jtQueue, false, true));

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
		add(lblStatus, BorderLayout.NORTH);
		DownloadQueueManager.instance().addDownloadQueueManagerListener(this);

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

		// Load downloadqueue
		List<Pic> pics = QueueManager.instance().getQueue();
		for (Pic pic : pics) {
			model.addRow(pic);
		}
		updateStatus();

		QueueManager.instance().addListener(this);

		// Register Key
		ActionMap am = getActionMap();
		InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		Object deleteKey = new Object();

		KeyStroke deleteStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		Action deleteAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (DownloadQueueManager.instance().isDownloading()) {
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
		Thread t = new Thread(() -> QueueManager.instance().startDownload());
		t.start();
	}

	/**
	 * Stop
	 */
	private void actionStop() {
		Thread t = new Thread(() -> QueueManager.instance().stopDownload());
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
			ClipboardObserver.instance().setClipboardContent(content.toString());
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
		synchronized (QueueManager.instance().getSyncObject()) {
			synchronized (syncObject) {
				for (int selectedRow : jtQueue.getSelectedRows()) {
					Pic pic = QueueManager.instance().getPicByIndex(jtQueue.convertRowIndexToModel(selectedRow));
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
		int retval = JOptionPane.showConfirmDialog(Main.instance(), Localization.getString("QueueReallyDelete"), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, Icons
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
					Main.instance().addProgressObserver(pg);
					pg.progressModeChanged(true);
					pg.progressChanged(Localization.getString("DeleteEntries"));
					synchronized (QueueManager.instance().getSyncObject()) {
						synchronized (syncObject) {
							int[] selectedRows = jtQueue.getSelectedRows();
							int[] selectedModelRows = TableTool.convertRowIndexToModel(jtQueue, selectedRows, true);
							QueueManager.instance().removePics(selectedModelRows);
						}
					}
					Main.instance().removeProgressObserver(pg);
					Main.instance().setMessage(Localization.getString("EntriesDeleted"));
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
				ImportHTML.importHTML();
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
				ImportLinkList.importLinkList();
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
				DownloadAddDialog dlg = new DownloadAddDialog(Main.instance());
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
				ParsePagesDialog dlg = new ParsePagesDialog(Main.instance());
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
				ImportQueue.importQueue();
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * ExportQueue
	 */
	private void actionExportQueue() {
		if (DownloadQueueManager.instance().isDownloading()) {
			JOptionPane.showMessageDialog(Main.instance(), Localization.getString("ExportWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				ExportQueue.exportQueue();
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * ChangeTargetByInput
	 */
	private void actionChangeTargetByInput() {
		synchronized (QueueManager.instance().getSyncObject()) {
			synchronized (syncObject) {
				int[] s = jtQueue.getSelectedRows();
				String defaultPath = SettingsManager.instance().getSavePath();
				if (s.length == 1) {
					defaultPath = QueueManager.instance().getPicByIndex(jtQueue.convertRowIndexToModel(s[0])).getTargetPath();
				}
				String input = PathRenameDialog.showPathRenameDialog(Main.instance(), defaultPath);
				if ((input != null) && (input.length() > 2)) {
					boolean b1 = input.endsWith("/");
					boolean b2 = input.endsWith("\\");
					if ((b1 == false) && (b2 == false)) {
						input += FileTool.FILE_SEPERATOR;
					}
					for (int i = 0; i < s.length; i++) {
						String newPath = input;
						newPath = BHUtil.filterPath(newPath);
						newPath = FileTool.reducePathLength(newPath);

						Pic pic = QueueManager.instance().getPicByIndex(jtQueue.convertRowIndexToModel(s[i]));
						if (pic != null) {
							pic.setTargetPath(newPath);
							QueueManager.instance().updatePic(pic);
						}
					}
					QueueManager.instance().asyncSaveDatabase();
				}
			}
		}
	}

	/**
	 * ChangeTargetBySelection
	 */
	private void actionChangeTargetBySelection() {
		synchronized (QueueManager.instance().getSyncObject()) {
			synchronized (syncObject) {
				File file = FileDialogTool.showFolderDialog(this, SettingsManager.instance().getSavePath(), null);
				if (file != null) {
					String folder = file.getAbsolutePath() + FileTool.FILE_SEPERATOR;
					int s[] = jtQueue.getSelectedRows();
					for (int i = 0; i < s.length; i++) {
						Pic pic = QueueManager.instance().getPicByIndex(jtQueue.convertRowIndexToModel(s[i]));
						if (pic != null) {
							pic.setTargetPath(folder);
							QueueManager.instance().updatePic(pic);
						}
					}
					file = null;
					QueueManager.instance().asyncSaveDatabase();
				}
			}
		}
	}

	/**
	 * ChangeTargetFilename
	 */
	private void actionChangeTargetFilename() {
		synchronized (QueueManager.instance().getSyncObject()) {
			synchronized (syncObject) {
				String defaultvalue = "";
				int s[] = jtQueue.getSelectedRows();
				if (s.length > 0) {
					defaultvalue = (String)model.getValueAt(jtQueue.convertRowIndexToModel(s[0]), 1);
					defaultvalue = defaultvalue.substring(defaultvalue.lastIndexOf(FileTool.FILE_SEPERATOR) + 1);
					String input[] = FileRenameDialog.showFileRenameDialog(Main.instance(), "", defaultvalue, s.length);
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
									fname = fname.substring(fname.lastIndexOf(FileTool.FILE_SEPERATOR) + 1);
								}
								fname = input[3] + fname + input[4];
								out = getNumberedFilename(fname, index);
							}

							Pic pic = QueueManager.instance().getPicByIndex(modelIndex);
							if (pic != null) {
								pic.setTargetFilename(out);
								pic.setFixedTargetFilename(true);
								QueueManager.instance().updatePic(pic);
							}

							index += step;
						}
						QueueManager.instance().asyncSaveDatabase();
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
			final File folder = FileDialogTool.showFolderDialog(Main.instance(), SettingsManager.instance().getSavePath(), null);
			if (folder != null) {

				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						File files[] = folder.listFiles();
						ImportLocalFiles.importLocalFiles(files);
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
		synchronized (QueueManager.instance().getSyncObject()) {
			synchronized (syncObject) {
				int s[] = jtQueue.getSelectedRows();
				for (int i = 0; i < s.length; i++) {
					QueueManager.instance().getPicByIndex(jtQueue.convertRowIndexToModel(s[i])).setDeactivated(false);
				}
				model.fireTableDataChanged();
				QueueManager.instance().asyncSaveDatabase();
			}
		}
	}

	/**
	 * Deactivate
	 */
	private void actionDeactivate() {
		synchronized (QueueManager.instance().getSyncObject()) {
			synchronized (syncObject) {
				int s[] = jtQueue.getSelectedRows();
				for (int i = 0; i < s.length; i++) {
					QueueManager.instance().getPicByIndex(jtQueue.convertRowIndexToModel(s[i])).setDeactivated(true);
				}
				model.fireTableDataChanged();
				QueueManager.instance().asyncSaveDatabase();
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
				int sessionDownloadedFiles = DownloadQueueManager.instance().getSessionDownloadedFiles();
				String sessionDownloadedBytes = UnitFormatTool.getSizeString(DownloadQueueManager.instance().getSessionDownloadedBytes(), SettingsManager.instance().getSizeView());
				String downloadRate = UnitFormatTool.getBitrateString(DownloadQueueManager.instance().getDownloadBitrate());
				if (downloadRate.isEmpty()) {
					downloadRate = Localization.getString("NotAvailable");
				}
				lblStatus.setText(Localization.getString("Queue") + ": " + jtQueue.getRowCount() + " | " + Localization.getString("FreeSlots") + ": " + openSlots + "/" + maxSlots + " | "
						+ Localization.getString("DownloadedFiles") + ": " + SettingsManager.instance().getOverallDownloadedFiles() + " (" + sessionDownloadedFiles + ") | "
						+ Localization.getString("DownloadedBytes") + ": "
						+ UnitFormatTool.getSizeString(SettingsManager.instance().getOverallDownloadedBytes(), SettingsManager.instance().getSizeView()) + " (" + sessionDownloadedBytes + ") | "
						+ Localization.getString("DownloadBitrate") + ": " + downloadRate);
			}
		});
	}

	private void showTablePopupMenu(MouseEvent e) {
		boolean bIsDownloading = DownloadQueueManager.instance().isDownloading();
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
	 * Updates the status-display
	 */
	private void updateStatus() {
		DownloadQueueManager queue = DownloadQueueManager.instance();
		int queueCount = jtQueue.getRowCount();
		int openDownloadSlots = queue.getOpenDownloadSlots();
		int connectionCount = queue.getConnectionCount();
		long overallDownloadedFiles = SettingsManager.instance().getOverallDownloadedFiles();
		String overallDownloadedBytes = UnitFormatTool.getSizeString(SettingsManager.instance().getOverallDownloadedBytes(), SettingsManager.instance().getSizeView());
		int sessionDownloadedFiles = queue.getSessionDownloadedFiles();
		String sessionDownloadedBytes = UnitFormatTool.getSizeString(queue.getSessionDownloadedBytes(), SettingsManager.instance().getSizeView());
		String downloadRate = UnitFormatTool.getBitrateString(queue.getDownloadBitrate());
		if (downloadRate.isEmpty()) {
			downloadRate = Localization.getString("NotAvailable");
		}
		lblStatus.setText(Localization.getString("Queue") + ": " + queueCount + " | " + Localization.getString("FreeSlots") + ": " + openDownloadSlots + "/" + connectionCount + " | "
				+ Localization.getString("DownloadedFiles") + ": " + overallDownloadedFiles + " (" + sessionDownloadedFiles + ") | " + Localization.getString("DownloadedBytes") + ": "
				+ overallDownloadedBytes + " (" + sessionDownloadedBytes + ") | " + Localization.getString("DownloadBitrate") + ": " + downloadRate);
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
		SettingsManager.instance().setColWidthsQueue(TableTool.serializeColWidthSetting(jtQueue));
		SettingsManager.instance().writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false) {
			return;
		}
		TableTool.applyColWidths(jtQueue, SettingsManager.instance().getColWidthsQueue());
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
	public void picAdded(final Pic pic) {
		synchronized (syncObject) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					model.addRow(pic);
					updateStatus();
				}
			};
			if (EventQueue.isDispatchThread()) {
				r.run();
			} else {
				try {
					EventQueue.invokeAndWait(r);
				} catch (InvocationTargetException e) {
					logger.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public void picRemoved(final Pic pic, final int index) {
		synchronized (syncObject) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					model.removeRow(index);
					model.fireTableDataChanged();
					updateStatus();
				}
			};
			if (EventQueue.isDispatchThread()) {
				r.run();
			} else {
				try {
					EventQueue.invokeAndWait(r);
				} catch (InvocationTargetException e) {
					logger.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public void picsAdded(final List<Pic> pics) {
		synchronized (syncObject) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					Iterator<Pic> it = pics.iterator();
					while (it.hasNext()) {
						Pic pic = it.next();
						model.addRow(pic);
					}
					updateStatus();
				}
			};
			if (EventQueue.isDispatchThread()) {
				r.run();
			} else {
				try {
					EventQueue.invokeAndWait(r);
				} catch (InvocationTargetException e) {
					logger.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public void picsRemoved(final int[] removedIndeces) {
		synchronized (syncObject) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					// Convert first removed row index, before removing any rows
					int firstRemovedRowViewIndex = jtQueue.convertRowIndexToView(removedIndeces[0]);

					for (int i = removedIndeces.length - 1; i > -1; i--) {
						model.removeRow(removedIndeces[i]);
					}
					model.fireTableDataChanged();

					int rowCount = jtQueue.getRowCount();
					int aboveFirstRemovedRowViewIndex = firstRemovedRowViewIndex - 1;
					if (firstRemovedRowViewIndex < rowCount) {
						jtQueue.setRowSelectionInterval(firstRemovedRowViewIndex, firstRemovedRowViewIndex);
					} else if (aboveFirstRemovedRowViewIndex >= 0 && aboveFirstRemovedRowViewIndex < rowCount) {
						jtQueue.setRowSelectionInterval(aboveFirstRemovedRowViewIndex, aboveFirstRemovedRowViewIndex);
					}
					updateStatus();
				}
			};
			if (EventQueue.isDispatchThread()) {
				r.run();
			} else {
				try {
					EventQueue.invokeAndWait(r);
				} catch (InvocationTargetException e) {
					logger.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public void picProgressBarChanged(final Pic pic, final int min, final int max, final int val, final String s, final String errMsg, final int index) {
		synchronized (syncObject) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					int row = index;
					if ((row > -1) && (model.getRowCount() > row)) {
						// force cell to be updated
						Object o = model.getValueAt(row, 3);

						boolean valueIsAlreadyProgressBar = o instanceof JProgressBar;
						boolean picIsNotDownloading = pic.getStatus() == PicState.FAILED || pic.getStatus() == PicState.FAILED_FILE_NOT_EXIST || pic.getStatus() == PicState.SLEEPING
								|| pic.getStatus() == PicState.WAITING || pic.getStatus() == PicState.FAILED_FILE_TEMPORARY_OFFLINE;

						if (picIsNotDownloading && !valueIsAlreadyProgressBar) {
							model.setValueAt(pic, row, 3);
							model.fireTableCellUpdated(row, 3);
							return;
						}

						JProgressBar pg = null;
						if (valueIsAlreadyProgressBar) {
							pg = (JProgressBar)o;
						} else {
							pg = new JProgressBar();
						}

						pg.setMinimum(min);
						pg.setMaximum(max);
						pg.setValue(val);
						pg.setStringPainted(true);
						pg.setString(s);
						if (errMsg != null && errMsg.length() > 0) {
							pg.setToolTipText(errMsg);
						} else {
							pg.setToolTipText(s);
						}

						if (!valueIsAlreadyProgressBar) {
							model.setValueAt(pg, row, 3);
						}

						model.fireTableCellUpdated(row, 3);
					}
				}
			};
			if (EventQueue.isDispatchThread()) {
				r.run();
			} else {
				try {
					EventQueue.invokeAndWait(r);
				} catch (InvocationTargetException e) {
					logger.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public void picSizeChanged(final Pic pic, final int index) {
		synchronized (syncObject) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					int row = index;
					if ((row > -1) && (model.getRowCount() > row)) {
						// Change Cell
						long size = pic.getSize();
						if (size < 1) {
							model.setValueAt(Localization.getString("Unkown"), row, 2);
						} else {
							model.setValueAt(UnitFormatTool.getSizeString(size, SettingsManager.instance().getSizeView()), row, 2);
						}
						model.fireTableCellUpdated(row, 2);
					}
				}
			};
			if (EventQueue.isDispatchThread()) {
				r.run();
			} else {
				try {
					EventQueue.invokeAndWait(r);
				} catch (InvocationTargetException e) {
					logger.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public void picTargetChanged(final Pic pic, final int index) {
		synchronized (syncObject) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					int row = index;
					if ((row > -1) && (model.getRowCount() > row)) {
						// Change Cell
						model.setValueAt(pic.getTarget(), row, 1);
						model.fireTableCellUpdated(row, 1);
					}
				}
			};
			if (EventQueue.isDispatchThread()) {
				r.run();
			} else {
				try {
					EventQueue.invokeAndWait(r);
				} catch (InvocationTargetException e) {
					logger.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public void picStatusChanged(final Pic pic, final int index) {
		synchronized (syncObject) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					if (pic.getStatus() == PicState.FAILED || pic.getStatus() == PicState.FAILED_FILE_NOT_EXIST || pic.getStatus() == PicState.SLEEPING || pic.getStatus() == PicState.WAITING
							|| pic.getStatus() == PicState.FAILED_FILE_TEMPORARY_OFFLINE) {
						int row = index;
						if ((row > -1) && (model.getRowCount() > row)) {
							model.setValueAt(pic, row, 3);
							model.fireTableCellUpdated(row, 3);
						}
					}
				}
			};
			if (EventQueue.isDispatchThread()) {
				r.run();
			} else {
				try {
					EventQueue.invokeAndWait(r);
				} catch (InvocationTargetException e) {
					logger.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public void picDeactivatedChanged(Pic pic, final int index) {
		synchronized (syncObject) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					int row = index;
					if ((row > -1) && (model.getRowCount() > row)) {
						model.fireTableRowsUpdated(row, row);
					}
				}
			};
			if (EventQueue.isDispatchThread()) {
				r.run();
			} else {
				try {
					EventQueue.invokeAndWait(r);
				} catch (InvocationTargetException e) {
					logger.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}
}

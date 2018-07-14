package ch.supertomcat.bh.gui.log;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import ch.supertomcat.bh.log.ILogManagerListener;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.settings.ISettingsListener;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.fileiotools.FileTool;
import ch.supertomcat.supertomcattools.guitools.FileExplorerTool;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.TableTool;
import ch.supertomcat.supertomcattools.guitools.tablerenderer.DefaultStringColorRowRenderer;

/**
 * Panel containing Logs
 */
public class Log extends JPanel implements ActionListener, MouseListener, TableColumnModelListener, ISettingsListener, ILogManagerListener {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(Log.class);

	/**
	 * UID
	 */
	private static final long serialVersionUID = 5907100131845566233L;

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
	private JMenuItem menuItemCopyURL = new JMenuItem(Localization.getString("CopyURL"), Icons.getTangoIcon("actions/edit-copy.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemOpenURL = new JMenuItem(Localization.getString("OpenURL"), Icons.getTangoIcon("apps/internet-web-browser.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemOpenDirectory = new JMenuItem(Localization.getString("OpenDirectory"), Icons.getTangoIcon("places/folder.png", 16));

	/**
	 * Label
	 */
	private JLabel lblStatus = new JLabel(Localization.getString("OnlyTheLastFilesAreShownLog"));

	/**
	 * Button
	 */
	private JButton btnFirst = new JButton("", Icons.getTangoIcon("actions/go-first.png", 16));

	/**
	 * Button
	 */
	private JButton btnNext = new JButton("", Icons.getTangoIcon("actions/go-next.png", 16));

	/**
	 * Button
	 */
	private JButton btnPrevious = new JButton("", Icons.getTangoIcon("actions/go-previous.png", 16));

	/**
	 * Button
	 */
	private JButton btnLast = new JButton("", Icons.getTangoIcon("actions/go-last.png", 16));

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
	 * Constructor
	 */
	public Log() {
		jtLog = new JTable(model);

		TableTool.internationalizeColumns(jtLog);

		setLayout(new BorderLayout());
		jtLog.getColumn("DateTime").setMinWidth(100);
		jtLog.getColumn("DateTime").setMaxWidth(150);
		jtLog.getColumn("Size").setMinWidth(100);
		jtLog.getColumn("Size").setMaxWidth(150);
		updateColWidthsFromSettingsManager();
		jtLog.getColumnModel().addColumnModelListener(this);
		jtLog.getTableHeader().setReorderingAllowed(false);
		jtLog.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		jtLog.setRowHeight(TableTool.calculateRowHeight(jtLog, false, true));

		JScrollPane jsp = new JScrollPane(jtLog);
		add(jsp, BorderLayout.CENTER);

		btnFirst.addActionListener(this);
		btnPrevious.addActionListener(this);
		btnNext.addActionListener(this);
		btnLast.addActionListener(this);

		pnlButtons.add(btnFirst);
		pnlButtons.add(btnPrevious);
		pnlButtons.add(btnNext);
		pnlButtons.add(btnLast);

		lblStatus.setHorizontalAlignment(JLabel.LEFT);
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
		menuItemCopyURL.addActionListener(this);
		menuItemOpenURL.addActionListener(this);
		menuItemOpenDirectory.addActionListener(this);

		jtLog.addMouseListener(this);

		crr = new DefaultStringColorRowRenderer();
		jtLog.setDefaultRenderer(Object.class, crr);

		SettingsManager.instance().addSettingsListener(this);
		LogManager.instance().addLogManagerListener(this);
	}

	/**
	 * CopyURLs
	 */
	private void actionCopyURLs() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				int s[] = jtLog.getSelectedRows();
				StringBuilder content = new StringBuilder("");
				for (int i = 0; i < s.length; i++) {
					content.append((String)model.getValueAt(s[i], 1));
					if (s.length > 1) {
						content.append("\n");
					}
				}
				if (s.length > 0) {
					ClipboardObserver.instance().setClipboardContent(content.toString());
				}
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		t = null;
	}

	/**
	 * OpenURLs
	 */
	private void actionOpenURLs() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				int s[] = jtLog.getSelectedRows();
				for (int i = 0; i < s.length; i++) {
					String url = (String)model.getValueAt(s[i], 1);
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
	 * OpenDirectories
	 */
	private void actionOpenDirectories() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				HashSet<String> dirs = new HashSet<>();
				int s[] = jtLog.getSelectedRows();
				String file = "";
				for (int i = 0; i < s.length; i++) {
					file = (String)model.getValueAt(s[i], 2);
					file = FileTool.getPathFromFile(new File(file));
					dirs.add(file);
				}
				for (String dir : dirs) {
					FileExplorerTool.openDirectoryInFilemanager(dir);
				}
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		t = null;
	}

	/**
	 * @param vals Values
	 */
	private void updateIndexAndStatus(long vals[]) {
		if (vals.length != 3) {
			return;
		}
		long currentStart = vals[0];
		long end = vals[1];
		long lineCount = vals[2];
		this.currentStart = currentStart;
		if (lineCount == 0) {
			lblStatus.setText("Downloads: " + lineCount + " | " + Localization.getString("ShowNothing"));
		} else {
			lblStatus.setText("Downloads: " + lineCount + " | " + Localization.getString("ShowingFrom") + " " + currentStart + " " + Localization.getString("To") + " " + end);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnFirst) {
			updateIndexAndStatus(LogManager.instance().readLogs(1, model));
			last = false;
		} else if (e.getSource() == btnPrevious) {
			if (currentStart > 100) {
				updateIndexAndStatus(LogManager.instance().readLogs(currentStart - 100, model));
			} else {
				updateIndexAndStatus(LogManager.instance().readLogs(1, model));
			}
			last = false;
		} else if (e.getSource() == btnNext) {
			updateIndexAndStatus(LogManager.instance().readLogs(currentStart + 100, model));
			last = false;
		} else if (e.getSource() == btnLast) {
			updateIndexAndStatus(LogManager.instance().readLogs(-1, model));
			last = true;
		} else if (e.getSource() == menuItemCopyURL) {
			actionCopyURLs();
		} else if (e.getSource() == menuItemOpenURL) {
			actionOpenURLs();
		} else if (e.getSource() == menuItemOpenDirectory) {
			actionOpenDirectories();
		}
	}

	/**
	 * Reload logs
	 */
	public void reloadLogs() {
		if (changed && last && Main.instance().isTabSelected(this)) {
			updateIndexAndStatus(LogManager.instance().readLogs(-1, model));
			changed = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if ((e.getButton() == 3) && (!DownloadQueueManager.instance().isDownloading()) && (jtLog.getSelectedRowCount() > 0)) { // Rightclick
			SwingUtilities.updateComponentTreeUI(popupMenu);
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
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
		SettingsManager.instance().setColWidthsLog(TableTool.serializeColWidthSetting(jtLog));
		SettingsManager.instance().writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false) {
			return;
		}
		TableTool.applyColWidths(jtLog, SettingsManager.instance().getColWidthsLog());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TableColumnModelListener#columnAdded(javax.swing.event.TableColumnModelEvent)
	 */
	@Override
	public void columnAdded(TableColumnModelEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TableColumnModelListener#columnMarginChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void columnMarginChanged(ChangeEvent e) {
		updateColWidthsToSettingsManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TableColumnModelListener#columnMoved(javax.swing.event.TableColumnModelEvent)
	 */
	@Override
	public void columnMoved(TableColumnModelEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TableColumnModelListener#columnRemoved(javax.swing.event.TableColumnModelEvent)
	 */
	@Override
	public void columnRemoved(TableColumnModelEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TableColumnModelListener#columnSelectionChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void columnSelectionChanged(ListSelectionEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.settings.ISettingsListener#settingsChanged()
	 */
	@Override
	public void settingsChanged() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.log.ILogManagerListener#logChanged()
	 */
	@Override
	public void logChanged() {
		changed = true;
		if (changed && last && Main.instance().isTabSelected(this)) {
			updateIndexAndStatus(LogManager.instance().readLogs(-1, model));
			changed = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.log.ILogManagerListener#currentLogFileChanged()
	 */
	@Override
	public void currentLogFileChanged() {
		updateIndexAndStatus(LogManager.instance().readLogs(-1, model));
		last = true;
	}
}

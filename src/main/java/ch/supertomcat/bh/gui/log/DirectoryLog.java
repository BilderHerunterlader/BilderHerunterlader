package ch.supertomcat.bh.gui.log;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.log.DirectoryLogObject;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.guitools.FileExplorerTool;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.TableTool;
import ch.supertomcat.supertomcattools.guitools.progressmonitor.IProgressObserver;
import ch.supertomcat.supertomcattools.guitools.progressmonitor.ProgressObserver;
import ch.supertomcat.supertomcattools.guitools.tablerenderer.DefaultBooleanColorRowRenderer;
import ch.supertomcat.supertomcattools.guitools.tablerenderer.DefaultStringColorRowRenderer;

/**
 * 
 */
public class DirectoryLog extends JPanel implements ActionListener, IProgressObserver, MouseListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 5907100131845566233L;

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(DirectoryLog.class);

	/**
	 * Table
	 */
	private JTable jtLog = null;

	/**
	 * TabelModel
	 */
	private DirectoryLogTableModel model = new DirectoryLogTableModel();

	/**
	 * Label
	 */
	private JLabel lblStatus = new JLabel(Localization.getString("DirectoryLogLoading"));

	private JProgressBar pgStatus = new JProgressBar();

	/**
	 * Panel
	 */
	private JPanel pnlFilter = new JPanel();

	/**
	 * Panel
	 */
	private JPanel pnlInfo = new JPanel();

	/**
	 * btnReload
	 */
	private JButton btnReload = new JButton(Localization.getString("Reload"), Icons.getTangoIcon("actions/view-refresh.png", 16));

	/**
	 * Button
	 */
	private JToggleButton btnFilter = new JToggleButton(Localization.getString("Filter"), Icons.getTangoIcon("actions/edit-find.png", 16), false);

	/**
	 * lblFilter
	 */
	private JLabel lblDirCount = new JLabel(Localization.getString("DirectoryLogDirCount"));

	/**
	 * txtFilter
	 */
	private JTextField txtDirCount = new JTextField(String.valueOf(SettingsManager.instance().getDirectoryLogDirCount()), 5);

	/**
	 * txtFilter
	 */
	private JTextField txtFilter = new JTextField("Filter...", 40);

	/**
	 * chkFilterOnlyExistingDirs
	 */
	private JCheckBox chkFilterOnlyExistingDirs = new JCheckBox(Localization.getString("OnlyExistingDirectories"), SettingsManager.instance().isDirectoryLogOnlyExisting());

	/**
	 * btnFilter
	 */
	private JButton btnFilterApply = new JButton(Localization.getString("Apply"));

	/**
	 * PopupMenu
	 */
	private JPopupMenu popupMenu = new JPopupMenu();

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemOpenDirectory = new JMenuItem(Localization.getString("OpenDirectory"), Icons.getTangoIcon("places/folder.png", 16));

	/**
	 * DefaultStringColorRowRenderer
	 */
	private DefaultStringColorRowRenderer crr;

	private boolean initialized = false;

	/**
	 * Constructor
	 */
	public DirectoryLog() {
		jtLog = new JTable(model);
		TableTool.internationalizeColumns(jtLog);
		setLayout(new BorderLayout());
		jtLog.getColumn("DateTime").setMinWidth(100);
		jtLog.getColumn("DateTime").setMaxWidth(150);
		jtLog.getColumn("FolderExists").setMinWidth(100);
		jtLog.getColumn("FolderExists").setMaxWidth(150);
		jtLog.getColumn("FolderExists").setCellRenderer(new DefaultBooleanColorRowRenderer());
		jtLog.getTableHeader().setReorderingAllowed(false);
		jtLog.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		jtLog.setRowHeight(TableTool.calculateRowHeight(jtLog, false, true));

		popupMenu.add(menuItemOpenDirectory);
		menuItemOpenDirectory.addActionListener(this);

		TitledBorder brdFilter = BorderFactory.createTitledBorder(Localization.getString("Filter"));
		pnlFilter.setBorder(brdFilter);
		pnlFilter.setLayout(new BorderLayout());

		pnlFilter.add(chkFilterOnlyExistingDirs, BorderLayout.WEST);
		JPanel pnlFilterTextField = new JPanel();
		pnlFilterTextField.add(lblDirCount);
		pnlFilterTextField.add(txtDirCount);
		pnlFilterTextField.add(txtFilter);
		pnlFilter.add(pnlFilterTextField, BorderLayout.CENTER);
		pnlFilter.add(btnFilterApply, BorderLayout.EAST);

		txtFilter.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (txtFilter.getText().equals("Filter...")) {
					txtFilter.setText("");
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (txtFilter.getText().length() == 0) {
					txtFilter.setText("Filter...");
				}
			}
		});

		JPanel pnlStatus = new JPanel();
		pnlStatus.setLayout(new BoxLayout(pnlStatus, BoxLayout.LINE_AXIS));
		pnlStatus.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		pnlStatus.add(lblStatus);
		pnlStatus.add(pgStatus);

		pgStatus.setVisible(false);

		JPanel pnlInternalButtons = new JPanel();
		btnReload.addActionListener(this);
		pnlInternalButtons.add(btnReload);
		pnlInternalButtons.add(btnFilter);

		pnlInfo = new JPanel();
		pnlInfo.setLayout(new BorderLayout());
		pnlInfo.add(pnlStatus, BorderLayout.WEST);
		pnlInfo.add(pnlInternalButtons, BorderLayout.EAST);

		JPanel pnlP = new JPanel();
		pnlP.setLayout(new BorderLayout());
		pnlP.add(pnlInfo, BorderLayout.NORTH);
		pnlP.add(pnlFilter, BorderLayout.CENTER);

		JScrollPane jsp = new JScrollPane(jtLog);
		add(jsp, BorderLayout.CENTER);

		btnFilterApply.addActionListener(this);

		add(pnlP, BorderLayout.NORTH);

		crr = new DefaultStringColorRowRenderer();
		jtLog.setDefaultRenderer(Object.class, crr);

		jtLog.addMouseListener(this);
	}

	/**
	 * Reads logs the first time this method is called
	 */
	public void init() {
		if (initialized) {
			return;
		}
		initialized = true;

		boolean filterEnabled = SettingsManager.instance().isDirectoryLogFilterEnabled();
		btnFilter.setSelected(filterEnabled);
		pnlFilter.setVisible(filterEnabled);
		btnFilter.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				pnlFilter.setVisible(btnFilter.isSelected());
				SettingsManager.instance().setDirectoryLogFilterEnabled(btnFilter.isSelected());
				SettingsManager.instance().writeSettings(true);
			}
		});

		readLogs();
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
					file = (String)model.getValueAt(s[i], 1);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnFilterApply || e.getSource() == btnReload) {
			try {
				int dirCount = Integer.parseInt(txtDirCount.getText());
				SettingsManager.instance().setDirectoryLogDirCount(dirCount);
			} catch (NumberFormatException nfe) {
				txtDirCount.setText(String.valueOf(SettingsManager.instance().getDirectoryLogDirCount()));
			}
			SettingsManager.instance().setDirectoryLogOnlyExisting(chkFilterOnlyExistingDirs.isSelected());
			SettingsManager.instance().writeSettings(true);
			readLogs();
		} else if (e.getSource() == menuItemOpenDirectory) {
			actionOpenDirectories();
		}
	}

	/**
	 * Get-Method
	 * 
	 * @param dateTime DateTime
	 * @return Formatted date and time
	 */
	private String getDateTime(long dateTime) {
		String retval = "";
		if (dateTime < 0) {
			return retval;
		}
		DateFormat df = new SimpleDateFormat();
		retval = df.format(new Date(dateTime));
		return retval;
	}

	/**
	 * Read logs
	 */
	private void readLogs() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {

				lblStatus.setText(Localization.getString("DirectoryLogLoading"));

				model.removeAllRows();

				Pattern pattern = null;
				String filter = txtFilter.getText();
				if (filter.equals("Filter...")) {
					filter = "";
				}
				if (filter.length() > 0) {
					try {
						pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
					} catch (PatternSyntaxException pse) {
						logger.debug(pse.getMessage(), pse);
						pattern = null;
					} catch (IllegalArgumentException iae) {
						logger.debug(iae.getMessage(), iae);
						pattern = null;
					}
				}
				ProgressObserver progress = new ProgressObserver();
				progress.addProgressListener(DirectoryLog.this);
				List<DirectoryLogObject> dirs = LogManager.instance().readDirectoryLog(pattern, chkFilterOnlyExistingDirs.isSelected(), progress);
				if (dirs != null) {

					int maxDirs = SettingsManager.instance().getDirectoryLogDirCount();

					int maxSize = dirs.size() < maxDirs ? dirs.size() : maxDirs;

					for (int i = dirs.size() - maxSize; i < dirs.size(); i++) {
						model.addRow(getDateTime(dirs.get(i).getDateTime()), dirs.get(i).getDirectory(), dirs.get(i).isExists());
					}
					String status = Localization.getString("OnlyTheLastFilesAreShownDirectoryLog");
					status = status.replaceAll("\\$DIRCOUNT", String.valueOf(maxDirs));
					lblStatus.setText(status);
				} else {
					lblStatus.setText(Localization.getString("DirectoryLogLoadingFailed"));
				}
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	@Override
	public void progressChanged(final int val) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				pgStatus.setValue(val);
			}
		});
	}

	@Override
	public void progressChanged(final int min, final int max, final int val) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				pgStatus.setMinimum(min);
				pgStatus.setMaximum(max);
				pgStatus.setValue(val);
			}
		});
	}

	@Override
	public void progressChanged(final String text) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				pgStatus.setString(text);
			}
		});
	}

	@Override
	public void progressChanged(final boolean visible) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				pgStatus.setVisible(visible);
			}
		});
	}

	@Override
	public void progressIncreased() {
	}

	@Override
	public void progressModeChanged(boolean indeterminate) {
	}

	@Override
	public void progressCompleted() {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if ((e.getButton() == 3) && (jtLog.getSelectedRowCount() > 0)) { // Rightclick
			SwingUtilities.updateComponentTreeUI(popupMenu);
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}

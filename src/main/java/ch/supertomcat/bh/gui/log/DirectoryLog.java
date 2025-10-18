package ch.supertomcat.bh.gui.log;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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
import ch.supertomcat.bh.log.DirectoryLogObject;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.FileExplorerUtil;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.IProgressObserver;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultBooleanColorRowRenderer;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;

/**
 * Directory Log
 */
public class DirectoryLog extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * Dir Count Replace Pattern
	 */
	private static final Pattern DIR_COUNT_REPLACE_PATTERN = Pattern.compile("\\$DIRCOUNT");

	/**
	 * Date Format
	 */
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);

	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * TabelModel
	 */
	private DirectoryLogTableModel model = new DirectoryLogTableModel();

	/**
	 * Table
	 */
	private JTable jtLog = new JTable(model);

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
	private JButton btnReload = new JButton(Localization.getString("Reload"), Icons.getTangoSVGIcon("actions/view-refresh.svg", 16));

	/**
	 * Button
	 */
	private JToggleButton btnFilter = new JToggleButton(Localization.getString("Filter"), Icons.getTangoSVGIcon("actions/edit-find.svg", 16), false);

	/**
	 * lblFilter
	 */
	private JLabel lblDirCount = new JLabel(Localization.getString("DirectoryLogDirCount"));

	/**
	 * txtFilter
	 */
	private JTextField txtDirCount;

	/**
	 * txtFilter
	 */
	private JTextField txtFilter = new JTextField("Filter...", 40);

	/**
	 * chkFilterOnlyExistingDirs
	 */
	private JCheckBox chkFilterOnlyExistingDirs;

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
	private JMenuItem menuItemOpenDirectory = new JMenuItem(Localization.getString("OpenDirectory"), Icons.getTangoSVGIcon("places/folder.svg", 16));

	/**
	 * DefaultStringColorRowRenderer
	 */
	private DefaultStringColorRowRenderer crr = new DefaultStringColorRowRenderer();

	private boolean initialized = false;

	/**
	 * Log Manager
	 */
	private final LogManager logManager;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Constructor
	 * 
	 * @param logManager Log Manager
	 * @param settingsManager Settings Manager
	 */
	public DirectoryLog(LogManager logManager, SettingsManager settingsManager) {
		this.logManager = logManager;
		this.settingsManager = settingsManager;
		TableUtil.internationalizeColumns(jtLog);
		setLayout(new BorderLayout());
		jtLog.getColumn("DateTime").setMinWidth(100);
		jtLog.getColumn("DateTime").setMaxWidth(150);
		jtLog.getColumn("FolderExists").setMinWidth(100);
		jtLog.getColumn("FolderExists").setMaxWidth(150);
		jtLog.getColumn("FolderExists").setCellRenderer(new DefaultBooleanColorRowRenderer());
		jtLog.getTableHeader().setReorderingAllowed(false);
		jtLog.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		jtLog.setRowHeight(TableUtil.calculateRowHeight(jtLog, false, true));

		popupMenu.add(menuItemOpenDirectory);
		menuItemOpenDirectory.addActionListener(e -> actionOpenDirectories());

		this.txtDirCount = new JTextField(String.valueOf(settingsManager.getGUISettings().getDirectoryLogDirCount()), 5);
		this.chkFilterOnlyExistingDirs = new JCheckBox(Localization.getString("OnlyExistingDirectories"), settingsManager.getGUISettings().isDirectoryLogOnlyExisting());

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
				if (txtFilter.getText().isEmpty()) {
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
		btnReload.addActionListener(e -> reloadLogs());
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

		btnFilterApply.addActionListener(e -> reloadLogs());

		add(pnlP, BorderLayout.NORTH);

		jtLog.setDefaultRenderer(Object.class, crr);

		jtLog.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger() && jtLog.getSelectedRowCount() > 0) {
					SwingUtilities.updateComponentTreeUI(popupMenu);
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger() && jtLog.getSelectedRowCount() > 0) {
					SwingUtilities.updateComponentTreeUI(popupMenu);
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}

	/**
	 * Reads logs the first time this method is called
	 */
	public void init() {
		if (initialized) {
			return;
		}
		initialized = true;

		boolean filterEnabled = settingsManager.getGUISettings().isDirectoryLogFilterEnabled();
		btnFilter.setSelected(filterEnabled);
		pnlFilter.setVisible(filterEnabled);
		btnFilter.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				pnlFilter.setVisible(btnFilter.isSelected());
				settingsManager.getGUISettings().setDirectoryLogFilterEnabled(btnFilter.isSelected());
				settingsManager.writeSettings(true);
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
				int[] s = jtLog.getSelectedRows();
				String file = "";
				for (int i = 0; i < s.length; i++) {
					file = (String)model.getValueAt(s[i], 1);
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

	private void reloadLogs() {
		try {
			int dirCount = Integer.parseInt(txtDirCount.getText());
			settingsManager.getGUISettings().setDirectoryLogDirCount(dirCount);
		} catch (NumberFormatException nfe) {
			txtDirCount.setText(String.valueOf(settingsManager.getGUISettings().getDirectoryLogDirCount()));
		}
		settingsManager.getGUISettings().setDirectoryLogOnlyExisting(chkFilterOnlyExistingDirs.isSelected());
		settingsManager.writeSettings(true);
		readLogs();
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
				if (!filter.isEmpty()) {
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
				DirectoryLogProgressObserver progressListener = new DirectoryLogProgressObserver();
				progress.addProgressListener(progressListener);
				List<DirectoryLogObject> dirs = logManager.readDirectoryLog(pattern, chkFilterOnlyExistingDirs.isSelected(), progress);
				if (dirs != null) {

					int maxDirs = settingsManager.getGUISettings().getDirectoryLogDirCount();

					int maxSize = dirs.size() < maxDirs ? dirs.size() : maxDirs;

					for (int i = dirs.size() - maxSize; i < dirs.size(); i++) {
						LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(dirs.get(i).getDateTime()), ZoneId.systemDefault());
						model.addRow(dateTime.format(DATE_FORMAT), dirs.get(i).getDirectory(), dirs.get(i).isExists());
					}
					String status = Localization.getString("OnlyTheLastFilesAreShownDirectoryLog");
					status = DIR_COUNT_REPLACE_PATTERN.matcher(status).replaceAll(String.valueOf(maxDirs));
					lblStatus.setText(status);
				} else {
					lblStatus.setText(Localization.getString("DirectoryLogLoadingFailed"));
				}
				progress.removeProgressListener(progressListener);
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * Progress Observer for reading log
	 */
	private class DirectoryLogProgressObserver implements IProgressObserver {
		@Override
		public void progressIncreased() {
			// Nothing to do
		}

		@Override
		public void progressChanged(int val) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					pgStatus.setValue(val);
				}
			});
		}

		@Override
		public void progressChanged(int min, int max, int val) {
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
		public void progressChanged(String text) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					pgStatus.setString(text);
				}
			});
		}

		@Override
		public void progressChanged(boolean visible) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					pgStatus.setVisible(visible);
				}
			});
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
}

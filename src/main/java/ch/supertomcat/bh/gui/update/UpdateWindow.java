package ch.supertomcat.bh.gui.update;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.BHIcons;
import ch.supertomcat.bh.gui.GuiEvent;
import ch.supertomcat.bh.gui.renderer.UpdateColumnRenderer;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.update.UpdateException;
import ch.supertomcat.bh.update.UpdateManager;
import ch.supertomcat.bh.update.UpdateManagerListener;
import ch.supertomcat.bh.update.containers.UpdateActionType;
import ch.supertomcat.bh.update.containers.UpdateType;
import ch.supertomcat.bh.update.containers.WrappedUpdateData;
import ch.supertomcat.bh.update.containers.WrappedUpdates;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.application.ApplicationUtil;
import ch.supertomcat.supertomcatutils.gui.FileExplorerUtil;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;

/**
 * Update-Window
 */
public class UpdateWindow extends JDialog {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -2094701933418666832L;

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(UpdateWindow.class);

	/**
	 * Update Manager
	 */
	private final UpdateManager updateManager;

	/**
	 * Queue Manager
	 */
	private final QueueManager queueManager;

	/**
	 * Keyword Manager
	 */
	private final KeywordManager keywordManager;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Update Manager Listener
	 */
	private final UpdateWindowUpdateManagerListener updateManagerListener = new UpdateWindowUpdateManagerListener();

	/**
	 * Label
	 */
	private JLabel lblMain = new JLabel();

	/**
	 * Button
	 */
	private JButton btnWebsite = new JButton(Localization.getString("Website"));

	/**
	 * Button
	 */
	private JButton btnChanges = new JButton(Localization.getString("Changes"));

	/**
	 * Label
	 */
	private JLabel lblStatus = new JLabel(" ");

	/**
	 * Label
	 */
	private JTextArea lblMessages = new JTextArea(15, 100);

	/**
	 * Panel
	 */
	private JPanel pnlProgressBar = new JPanel();

	/**
	 * Progressbar
	 */
	private JProgressBar prgUpdate = new JProgressBar();

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * Button
	 */
	private JButton btnCheck = new JButton(Localization.getString("Check"), Icons.getTangoSVGIcon("actions/system-search.svg", 16));

	/**
	 * Button
	 */
	private JButton btnUpdate = new JButton(Localization.getString("Update"), Icons.getTangoSVGIcon("status/software-update-available.svg", 16));

	/**
	 * TableModel
	 */
	private UpdateTableModel model = new UpdateTableModel();

	/**
	 * Table
	 */
	private JTable table = new JTable(model);

	/**
	 * UpdateActionColumnRenderer
	 */
	private UpdateColumnRenderer uacr = new UpdateColumnRenderer();

	/**
	 * Scrollpane
	 */
	private JScrollPane spTable;

	/**
	 * Scrollpane
	 */
	private JScrollPane spMessages;

	/**
	 * GridBagLayout
	 */
	private GridBagLayout gbl = new GridBagLayout();

	/**
	 * GridBagLayoutUtil
	 */
	private GridBagLayoutUtil gblt = new GridBagLayoutUtil(1, 1, 1, 1);

	/**
	 * Update List
	 */
	private WrappedUpdates updateList = null;

	/**
	 * Changelog
	 */
	private String changelog = "";

	/**
	 * updateRunned
	 */
	private boolean updateRunned = false;

	/**
	 * updateSuccessfull
	 */
	private boolean updateSuccessfull = false;

	/**
	 * Constructor
	 * 
	 * @param updateManager UpdateManager
	 * @param owner Owner
	 * @param queueManager Queue Manager
	 * @param keywordManager Keyword Manager
	 * @param settingsManager Settings Manager
	 * @param guiEvent GUI Event
	 */
	public UpdateWindow(UpdateManager updateManager, Window owner, QueueManager queueManager, KeywordManager keywordManager, SettingsManager settingsManager, GuiEvent guiEvent) {
		this.updateManager = updateManager;
		this.queueManager = queueManager;
		this.keywordManager = keywordManager;
		this.settingsManager = settingsManager;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setIconImage(BHIcons.getBHMultiResImage("BH.png"));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				guiEvent.updateWindowClosed(updateRunned, updateSuccessfull);
			}

			@Override
			public void windowOpened(WindowEvent e) {
				guiEvent.updateWindowOpened();
			}
		});

		setModal(true);
		getContentPane().setLayout(gbl);
		setTitle(ApplicationProperties.getProperty(ApplicationMain.APPLICATION_SHORT_NAME) + " - " + Localization.getString("Update"));

		pnlProgressBar.add(prgUpdate);

		pnlButtons.add(btnCheck);
		pnlButtons.add(btnUpdate);

		JPanel pnlMain = new JPanel();
		pnlMain.add(lblMain);
		pnlMain.add(btnWebsite);
		pnlMain.add(btnChanges);

		btnWebsite.addActionListener(e -> {
			String url;
			if (settingsManager.getGUISettings().getLanguage().equals("de_DE")) {
				url = "http://bihe.berlios.de/page/?loc=bilderherunterlader/download&lng=de";
			} else {
				url = "http://bihe.berlios.de/page/?loc=bilderherunterlader/download&lng=en";
			}
			FileExplorerUtil.openURL(url);
		});
		btnWebsite.setVisible(false);

		btnChanges.addActionListener(e -> {
			String message = changelog.replace("\\n", "\n");
			UpdateChangesDialog dlg = new UpdateChangesDialog(this, message, Localization.getString("Changes"));
			dlg.setVisible(true);
		});
		btnChanges.setVisible(false);

		btnCheck.addActionListener(e -> {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					checkForUpdates();
				}
			});
			t.setName("UpdateWindow-Check-Thread-" + t.threadId());
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		});

		btnUpdate.addActionListener(e -> {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					startUpdate();
				}
			});
			t.setName("UpdateWindow-Thread-" + t.threadId());
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		});
		btnUpdate.setEnabled(false);
		prgUpdate.setVisible(false);

		TableUtil.internationalizeColumns(table);

		table.setDefaultRenderer(Object.class, new DefaultStringColorRowRenderer());
		int updateActionTableHeaderWidth = TableUtil.calculateColumnHeaderWidth(table, table.getColumn("UpdateAction"), 20);
		table.getColumn("UpdateAction").setCellRenderer(uacr);
		table.getColumn("UpdateAction").setPreferredWidth(updateActionTableHeaderWidth);
		int updateTypeTableHeaderWidth = TableUtil.calculateColumnHeaderWidth(table, table.getColumn("UpdateType"), 20);
		table.getColumn("UpdateType").setCellRenderer(uacr);
		table.getColumn("UpdateType").setPreferredWidth(updateTypeTableHeaderWidth);
		int updateNameTableHeaderWidth = TableUtil.calculateColumnHeaderWidth(table, table.getColumn("Name"), 50);
		table.getColumn("Name").setPreferredWidth(updateNameTableHeaderWidth);
		int updateVersionTableHeaderWidth = TableUtil.calculateColumnHeaderWidth(table, table.getColumn("Version"), 15);
		table.getColumn("Version").setPreferredWidth(updateVersionTableHeaderWidth);
		int updateSourceNoteTableHeaderWidth = TableUtil.calculateColumnHeaderWidth(table, table.getColumn("UpdateSourceNote"), 15);
		table.getColumn("UpdateSourceNote").setPreferredWidth(updateSourceNoteTableHeaderWidth);
		updateColWidthsFromSettingsManager();

		table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {

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

		table.getTableHeader().setReorderingAllowed(false);
		table.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		table.setRowHeight(TableUtil.calculateRowHeight(table, false, true));
		Dimension preferredScrollableTableSize = new Dimension(table.getPreferredScrollableViewportSize().width, 15 * table.getRowHeight());
		table.setPreferredScrollableViewportSize(preferredScrollableTableSize);
		spTable = new JScrollPane(table);

		lblMessages.setEditable(false);
		lblMessages.setFont(UIManager.getFont("Label.font"));
		spMessages = new JScrollPane(lblMessages);

		JPanel pnlCenter = new JPanel(new BorderLayout());
		pnlCenter.add(spMessages, BorderLayout.NORTH);
		pnlCenter.add(spTable, BorderLayout.CENTER);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc = gblt.getGBC(0, 0, 1, 1, 1.0, 0.0);
		GridBagLayoutUtil.addItemToDialog(gbl, gbc, pnlMain, this);
		gbc = gblt.getGBC(0, 1, 1, 1, 1.0, 0.0);
		GridBagLayoutUtil.addItemToDialog(gbl, gbc, lblStatus, this);
		gbc = gblt.getGBC(0, 2, 1, 1, 1.0, 0.7);
		GridBagLayoutUtil.addItemToDialog(gbl, gbc, pnlCenter, this);
		gbc = gblt.getGBC(0, 4, 1, 1, 1.0, 0.0);
		GridBagLayoutUtil.addItemToDialog(gbl, gbc, pnlProgressBar, this);
		gbc = gblt.getGBC(0, 5, 1, 1, 1.0, 0.0);
		GridBagLayoutUtil.addItemToDialog(gbl, gbc, pnlButtons, this);

		pack();
		setLocationRelativeTo(owner);
	}

	/**
	 * Check for updates
	 * 
	 * @return True if updates are available
	 */
	public boolean checkForUpdates() {
		btnCheck.setEnabled(false);
		prgUpdate.setIndeterminate(true);
		prgUpdate.setVisible(true);
		try {
			try {
				updateList = updateManager.checkForUpdates();
				if (!updateList.isUpdateRequired() && !updateList.isSubUpdateRequired()) {
					lblMain.setText(Localization.getString("NoNewProgramVersion"));
					lblStatus.setText(Localization.getString("NoUpdatesAvailable"));
					btnChanges.setVisible(false);
					btnUpdate.setEnabled(false);
					btnWebsite.setVisible(false);
					btnCheck.setEnabled(true);
					return false;
				}
			} catch (UpdateException e) {
				logger.error("CheckForUpdates failed", e);
				lblMessages.insert("CheckForUpdates failed:\n" + ApplicationUtil.formatStackTrace(e) + "\n", 0);
				btnChanges.setVisible(false);
				btnUpdate.setEnabled(false);
				btnWebsite.setVisible(false);
				btnCheck.setEnabled(true);
				return false;
			}

			btnUpdate.setEnabled(true);

			if (updateList.isUpdateRequired()) {
				lblMain.setText(Localization.getString("NewProgramVersion") + " (v" + updateList.getVersion() + ")");
				changelog = updateList.getChangeLog().replace("\\n", "\n");
				btnChanges.setVisible(true);
				btnWebsite.setVisible(true);
			} else {
				lblMain.setText(Localization.getString("NoNewProgramVersion"));
				changelog = "";
				btnChanges.setVisible(false);
				btnWebsite.setVisible(false);
			}

			model.removeAllRows();

			updateList.getHosterUpdates().stream().filter(WrappedUpdateData::isUpdateRequired).forEachOrdered(model::addRow);
			updateList.getRedirectUpdates().stream().filter(WrappedUpdateData::isUpdateRequired).forEachOrdered(model::addRow);
			updateList.getRuleUpdates().stream().filter(WrappedUpdateData::isUpdateRequired).forEachOrdered(model::addRow);

			if (model.getRowCount() > 0) {
				lblStatus.setText(Localization.getString("UpdatesAvailable"));
			} else {
				lblStatus.setText(Localization.getString("NoUpdatesAvailable"));
			}

			btnCheck.setEnabled(false);
			return true;
		} finally {
			prgUpdate.setIndeterminate(false);
			prgUpdate.setVisible(false);
		}
	}

	private void startUpdate() {
		// Save and close databases
		queueManager.closeDatabase();
		keywordManager.closeDatabase();

		lblMessages.setText("");
		updateRunned = true;
		btnUpdate.setEnabled(false);
		prgUpdate.setIndeterminate(true);
		prgUpdate.setVisible(true);
		updateManager.addListener(updateManagerListener);
		updateManager.startUpdate(updateList, this);
	}

	/**
	 * updateColWidthsToSettingsManager
	 */
	private void updateColWidthsToSettingsManager() {
		if (!settingsManager.isSaveTableColumnSizes()) {
			return;
		}
		settingsManager.setColWidthsUpdate(TableUtil.serializeColWidthSetting(table));
		settingsManager.writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (!settingsManager.isSaveTableColumnSizes()) {
			return;
		}
		TableUtil.applyColWidths(table, settingsManager.getColWidthsUpdate());
	}

	private class UpdateWindowUpdateManagerListener implements UpdateManagerListener {
		@Override
		public void updatesStarted() {
			// Nothing to do
		}

		@Override
		public void updatesComplete() {
			lblStatus.setText(Localization.getString("UpdatesInstalled"));
			updateSuccessfull = true;
			updateManager.removeListener(this);
			prgUpdate.setIndeterminate(false);
			prgUpdate.setVisible(false);
		}

		@Override
		public void updatesFailed() {
			lblMessages.insert(Localization.getString("DownloadUpdateFailed"), 0);
			updateSuccessfull = false;
			updateManager.removeListener(this);
			prgUpdate.setIndeterminate(false);
			prgUpdate.setVisible(false);
		}

		@Override
		public void newProgramVersionInstalled() {
			lblMain.setText(Localization.getString("NewProgramVersionInstalled"));
		}

		@Override
		public void newProgramVersionInstallFailed() {
			lblMain.setText(Localization.getString("NewProgramVersionInstallFailed"));
			btnChanges.setVisible(false);
		}

		@Override
		public void updateDownloadStarted(UpdateType updateType, UpdateActionType updateActionType, String source, String target) {
			String message = Localization.getString("DownloadUpdate") + " (" + source + " -> " + target + ")\n";
			lblMessages.insert(message, 0);
		}

		@Override
		public void updateDownloadComplete(UpdateType updateType, UpdateActionType updateActionType) {
			// Nothing to do
		}

		@Override
		public void updateUnpackStarted(UpdateType updateType, UpdateActionType updateActionType, String source, String target) {
			String message = Localization.getString("UnpackUpdate") + " (" + source + " -> " + target + ")\n";
			lblMessages.insert(message, 0);
		}

		@Override
		public void updateUnpackComplete(UpdateType updateType, UpdateActionType updateActionType) {
			// Nothing to do
		}

		@Override
		public void updateCopyStarted(String source, String target) {
			String message = Localization.getString("CopyUpdate") + " (" + source + " -> " + target + ")\n";
			lblMessages.insert(message, 0);
		}

		@Override
		public void updateCopyComplete() {
			// Nothing to do
		}

		@Override
		public void errorOccured(String message, Exception ex) {
			lblMessages.insert(message + "\n" + ApplicationUtil.formatStackTrace(ex) + "\n", 0);
		}
	}
}

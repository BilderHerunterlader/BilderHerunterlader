package ch.supertomcat.bh.gui.update;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.GuiEvent;
import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.gui.renderer.UpdateActionColumnRenderer;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.update.UpdateException;
import ch.supertomcat.bh.update.UpdateManager;
import ch.supertomcat.bh.update.UpdateManagerListener;
import ch.supertomcat.bh.update.containers.UpdateList;
import ch.supertomcat.bh.update.containers.UpdateObject;
import ch.supertomcat.bh.update.containers.UpdateObject.UpdateActionType;
import ch.supertomcat.bh.update.containers.UpdateObject.UpdateType;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.application.ApplicationUtil;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;

/**
 * Update-Window
 */
public class UpdateWindow extends JDialog implements ActionListener, TableColumnModelListener, WindowListener, UpdateManagerListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -2094701933418666832L;

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(UpdateWindow.class);

	/**
	 * Label
	 */
	private JLabel lblMain = new JLabel("");

	/**
	 * Panel
	 */
	private JPanel pnlMain = new JPanel();

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
	private JButton btnCheck = new JButton(Localization.getString("Check"), Icons.getTangoIcon("actions/system-search.png", 16));

	/**
	 * Button
	 */
	private JButton btnUpdate = new JButton(Localization.getString("Update"), Icons.getTangoIcon("status/software-update-available.png", 16));

	/**
	 * Table
	 */
	private JTable table = new JTable();

	/**
	 * TableModel
	 */
	private UpdateTableModel model = new UpdateTableModel();

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

	private UpdateManager updateManager = null;

	private UpdateList updateList = null;

	/**
	 * Changelog DE
	 */
	private String changelogDE = "";

	/**
	 * Changelog EN
	 */
	private String changelogEN = "";

	/**
	 * updateRunned
	 */
	private boolean updateRunned = false;

	/**
	 * updateSuccessfull
	 */
	private boolean updateSuccessfull = false;

	/**
	 * UpdateActionColumnRenderer
	 */
	private UpdateActionColumnRenderer uacr = new UpdateActionColumnRenderer();

	/**
	 * Queue Manager
	 */
	private final QueueManager queueManager;

	/**
	 * Keyword Manager
	 */
	private final KeywordManager keywordManager;

	/**
	 * Constructor
	 * 
	 * @param updateManager UpdateManager
	 * @param owner Owner
	 * @param queueManager Queue Manager
	 * @param keywordManager Keyword Manager
	 */
	public UpdateWindow(UpdateManager updateManager, Window owner, QueueManager queueManager, KeywordManager keywordManager) {
		this.updateManager = updateManager;
		this.queueManager = queueManager;
		this.keywordManager = keywordManager;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setIconImage(Icons.getBHImage("BH.png"));
		addWindowListener(this);

		setModal(true);
		getContentPane().setLayout(gbl);
		setTitle(ApplicationProperties.getProperty("ApplicationShortName") + " - " + Localization.getString("Update"));

		pnlProgressBar.add(prgUpdate);

		pnlButtons.add(btnCheck);
		pnlButtons.add(btnUpdate);

		pnlMain.add(lblMain);
		pnlMain.add(btnWebsite);
		pnlMain.add(btnChanges);
		btnWebsite.addActionListener(this);
		btnWebsite.setVisible(false);
		btnChanges.addActionListener(this);
		btnChanges.setVisible(false);

		btnCheck.addActionListener(this);
		btnUpdate.addActionListener(this);
		btnUpdate.setEnabled(false);
		prgUpdate.setVisible(false);

		table.setModel(model);

		TableUtil.internationalizeColumns(table);

		table.setDefaultRenderer(Object.class, new DefaultStringColorRowRenderer());
		int updateActionTableHeaderWidth = TableUtil.calculateColumnHeaderWidth(table, table.getColumn("UpdateAction"), 20);
		table.getColumn("UpdateAction").setCellRenderer(uacr);
		table.getColumn("UpdateAction").setPreferredWidth(updateActionTableHeaderWidth);
		int updateTypeTableHeaderWidth = TableUtil.calculateColumnHeaderWidth(table, table.getColumn("UpdateType"), 20);
		table.getColumn("UpdateType").setPreferredWidth(updateTypeTableHeaderWidth);
		int updateNameTableHeaderWidth = TableUtil.calculateColumnHeaderWidth(table, table.getColumn("Name"), 50);
		table.getColumn("Name").setPreferredWidth(updateNameTableHeaderWidth);
		int updateVersionTableHeaderWidth = TableUtil.calculateColumnHeaderWidth(table, table.getColumn("Version"), 15);
		table.getColumn("Version").setPreferredWidth(updateVersionTableHeaderWidth);
		int updateSourceNoteTableHeaderWidth = TableUtil.calculateColumnHeaderWidth(table, table.getColumn("UpdateSourceNote"), 15);
		table.getColumn("UpdateSourceNote").setPreferredWidth(updateSourceNoteTableHeaderWidth);
		updateColWidthsFromSettingsManager();
		table.getColumnModel().addColumnModelListener(this);
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
		boolean retval = false;

		boolean updateAvailable = false;
		try {
			updateList = updateManager.checkForUpdates();
			updateAvailable = updateList != null;
		} catch (UpdateException e) {
			logger.error("CheckForUpdates failed", e);
			String cause = e.getCause() != null ? e.getCause().getMessage() : "Unkown cause";
			lblMessages.setText(e.getMessage() + ": " + cause + "\n" + lblMessages.getText());
			updateAvailable = false;
		}
		if (updateAvailable == false) {
			btnCheck.setEnabled(true);
			prgUpdate.setIndeterminate(false);
			prgUpdate.setVisible(false);
			return false;
		}

		UpdateObject updateBH = updateList.getApplicationUpdate();
		if (updateBH == null) {
			btnCheck.setEnabled(true);
			prgUpdate.setIndeterminate(false);
			prgUpdate.setVisible(false);
			// Revert Text
			lblMain.setText(Localization.getString("NoNewProgramVersion"));
			logger.error("Could not check for updates. updateBH is null");
			lblMessages.setText("Check For Updates failed: updateBH is null\n" + lblMessages.getText());
			return false;
		}
		List<UpdateObject> updateRules = updateList.getRulesUpdates();
		List<UpdateObject> updateHostPlugins = updateList.getHostPluginUpdates();
		List<UpdateObject> updateRedirectPlugins = updateList.getRedirectPluginUpdates();

		// Main
		if (ApplicationUtil.compareVersions(updateBH.getVersion(), ApplicationProperties.getProperty("ApplicationVersion")) > 0) {
			lblMain.setText(Localization.getString("NewProgramVersion") + " (v" + updateBH.getVersion() + ")");
			btnWebsite.setVisible(false);
			btnChanges.setVisible(true);
			btnUpdate.setEnabled(true);
			retval = true;
			updateBH.setAction(UpdateObject.UpdateActionType.ACTION_UPDATE);
			changelogDE = updateBH.getChangeLog("DE");
			changelogEN = updateBH.getChangeLog("EN");
		} else {
			lblMain.setText(Localization.getString("NoNewProgramVersion"));
			updateBH.setAction(UpdateObject.UpdateActionType.ACTION_NONE);
		}

		// Hosts
		for (int i = 0; i < updateHostPlugins.size(); i++) {
			UpdateObject update = updateHostPlugins.get(i);
			if (update == null) {
				btnCheck.setEnabled(true);
				prgUpdate.setIndeterminate(false);
				prgUpdate.setVisible(false);
				// Revert enabled buttons
				btnChanges.setVisible(false);
				btnUpdate.setEnabled(false);
				// Revert Text
				lblMain.setText(Localization.getString("NoNewProgramVersion"));
				// Revert table entries
				model.removeAllRows();
				logger.error("Could not check for updates. update is null");
				lblMessages.setText("Check For Updates failed: update is null\n" + lblMessages.getText());
				return false;
			}

			String v = HostManager.instance().getHostVersion(update.getName());
			if (!v.equals("")) {
				if (update.getAction() == UpdateObject.UpdateActionType.ACTION_REMOVE) {
					model.addRow(update);
				} else if (ApplicationUtil.compareVersions(update.getVersion(), v) > 0) {
					if (UpdateManager.checkMinMaxVersions(update.getBhMinVersion(), update.getBhMaxVersion())) {
						update.setAction(UpdateObject.UpdateActionType.ACTION_UPDATE);
						model.addRow(update);
					}
				}
			} else {
				if (update.getAction() == UpdateObject.UpdateActionType.ACTION_REMOVE) {
					update.setAction(UpdateObject.UpdateActionType.ACTION_NONE);
				} else if (UpdateManager.checkMinMaxVersions(update.getBhMinVersion(), update.getBhMaxVersion())) {
					update.setAction(UpdateObject.UpdateActionType.ACTION_NEW);
					model.addRow(update);
				}
			}
		}

		// Redirects
		for (int i = 0; i < updateRedirectPlugins.size(); i++) {
			UpdateObject update = updateRedirectPlugins.get(i);
			if (update == null) {
				btnCheck.setEnabled(true);
				prgUpdate.setIndeterminate(false);
				prgUpdate.setVisible(false);
				// Revert enabled buttons
				btnChanges.setVisible(false);
				btnUpdate.setEnabled(false);
				// Revert Text
				lblMain.setText(Localization.getString("NoNewProgramVersion"));
				// Revert table entries
				model.removeAllRows();
				logger.error("Could not check for updates. update is null");
				lblMessages.setText("Check For Updates failed: update is null\n" + lblMessages.getText());
				return false;
			}

			String v = HostManager.instance().getRedirectManager().getRedirectVersion(update.getName());
			if (!v.equals("")) {
				if (update.getAction() == UpdateObject.UpdateActionType.ACTION_REMOVE) {
					model.addRow(update);
				} else if (ApplicationUtil.compareVersions(update.getVersion(), v) > 0) {
					if (UpdateManager.checkMinMaxVersions(update.getBhMinVersion(), update.getBhMaxVersion())) {
						update.setAction(UpdateObject.UpdateActionType.ACTION_UPDATE);
						model.addRow(update);
					}
				}
			} else {
				if (update.getAction() == UpdateObject.UpdateActionType.ACTION_REMOVE) {
					update.setAction(UpdateObject.UpdateActionType.ACTION_NONE);
				} else if (UpdateManager.checkMinMaxVersions(update.getBhMinVersion(), update.getBhMaxVersion())) {
					update.setAction(UpdateObject.UpdateActionType.ACTION_NEW);
					model.addRow(update);
				}
			}
		}

		// Rules
		int sizer = HostManager.instance().getHostRules().getRules().size();

		// We create first an array with name and version of all rules
		String ruleVersions[][] = new String[sizer][2];
		Iterator<Rule> it = HostManager.instance().getHostRules().getRules().iterator();
		int ir = 0;
		Rule r;
		while (it.hasNext()) {
			r = it.next();
			ruleVersions[ir][0] = r.getFile().getName();
			ruleVersions[ir][1] = r.getVersion();
			ir++;
		}

		String v;
		for (int i = 0; i < updateRules.size(); i++) {
			UpdateObject update = updateRules.get(i);
			if (update == null) {
				btnCheck.setEnabled(true);
				prgUpdate.setIndeterminate(false);
				prgUpdate.setVisible(false);
				// Revert enabled buttons
				btnChanges.setVisible(false);
				btnUpdate.setEnabled(false);
				// Revert Text
				lblMain.setText(Localization.getString("NoNewProgramVersion"));
				// Revert table entries
				model.removeAllRows();
				logger.error("Could not check for updates. update is null");
				lblMessages.setText("Check For Updates failed: update is null\n" + lblMessages.getText());
				return false;
			}

			// Now we try to find the version of the current rule to update
			v = "";
			for (int x = 0; x < ruleVersions.length; x++) {
				if ((ruleVersions[x][0] == null) || (ruleVersions[x][1] == null)) {
					logger.error("NullPointer in vr-Array");
					continue;
				}
				if (ruleVersions[x][0].equals(update.getName())) {
					v = ruleVersions[x][1];
				}
			}

			if (!v.equals("")) {
				if (update.getAction() == UpdateObject.UpdateActionType.ACTION_REMOVE) {
					model.addRow(update);
				} else if (ApplicationUtil.compareVersions(update.getVersion(), v) > 0) {
					if (UpdateManager.checkMinMaxVersions(update.getBhMinVersion(), update.getBhMaxVersion())) {
						update.setAction(UpdateObject.UpdateActionType.ACTION_UPDATE);
						model.addRow(update);
					}
				}
			} else {
				if (update.getAction() == UpdateObject.UpdateActionType.ACTION_REMOVE) {
					update.setAction(UpdateObject.UpdateActionType.ACTION_NONE);
				} else if (UpdateManager.checkMinMaxVersions(update.getBhMinVersion(), update.getBhMaxVersion())) {
					update.setAction(UpdateObject.UpdateActionType.ACTION_NEW);
					model.addRow(update);
				}
			}
		}

		if (model.getRowCount() > 0) {
			lblStatus.setText(Localization.getString("UpdatesAvailable"));
			btnUpdate.setEnabled(true);
			retval = true;
		} else {
			lblStatus.setText(Localization.getString("NoUpdatesAvailable"));
		}
		prgUpdate.setIndeterminate(false);
		prgUpdate.setVisible(false);
		btnCheck.setEnabled(false);
		return retval;
	}

	private void startUpdate() {
		// Save and close databases
		queueManager.closeDatabase();
		keywordManager.closeDatabase();

		updateRunned = true;
		btnUpdate.setEnabled(false);
		prgUpdate.setIndeterminate(true);
		prgUpdate.setVisible(true);
		updateManager.addListener(this);
		updateManager.startUpdate(updateList, this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnCheck) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					checkForUpdates();
				}
			});
			t.setName("UpdateWindow-Check-Thread-" + t.getId());
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		} else if (e.getSource() == btnUpdate) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					startUpdate();
				}
			});
			t.setName("UpdateWindow-Thread-" + t.getId());
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		} else if (e.getSource() == btnWebsite) {
			String url;
			if (SettingsManager.instance().getLanguage().equals("de_DE")) {
				url = "http://bihe.berlios.de/page/?loc=bilderherunterlader/download&lng=de";
			} else {
				url = "http://bihe.berlios.de/page/?loc=bilderherunterlader/download&lng=en";
			}
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (IOException | URISyntaxException ex) {
					logger.error("Could not open URL: {}", url, ex);
				}
			} else {
				logger.error("Could not open URL, because Desktop is not supported: {}", url);
			}
		} else if (e.getSource() == btnChanges) {
			String message = changelogDE;
			if (SettingsManager.instance().getLanguage() == "EN") {
				message = changelogEN;
			}
			message = message.replaceAll("\\\\n", "\n");
			UpdateChangesDialog dlg = new UpdateChangesDialog(this, message, Localization.getString("Changes"));
			dlg.setVisible(true);
		}
	}

	/**
	 * updateColWidthsToSettingsManager
	 */
	private void updateColWidthsToSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false) {
			return;
		}
		SettingsManager.instance().setColWidthsUpdate(TableUtil.serializeColWidthSetting(table));
		SettingsManager.instance().writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false) {
			return;
		}
		TableUtil.applyColWidths(table, SettingsManager.instance().getColWidthsUpdate());
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
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowActivated(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosed(WindowEvent e) {
		GuiEvent.instance().updateWindowClosed(updateRunned, updateSuccessfull);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosing(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowIconified(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowOpened(WindowEvent e) {
		GuiEvent.instance().updateWindowOpened();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.update.UpdateManagerListener#updatesStarted()
	 */
	@Override
	public void updatesStarted() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.update.UpdateManagerListener#updatesComplete()
	 */
	@Override
	public void updatesComplete() {
		updateSuccessfull = true;
		updateManager.removeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.update.UpdateManagerListener#updatesFailed()
	 */
	@Override
	public void updatesFailed() {
		updateSuccessfull = false;
		updateManager.removeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.update.UpdateManagerListener#updateInstallStarted(ch.supertomcat.bh.update.UpdateObject.UpdateType,
	 * ch.supertomcat.bh.update.UpdateObject.UpdateActionType,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void updateInstallStarted(UpdateType updateType, UpdateActionType updateActionType, String source, String target) {
		String action = "Unknown Action";
		String message = "Unknown Message";
		switch (updateActionType) {
			case ACTION_NEW:
				action = Localization.getString("DownloadUpdate");
				message = source;
				break;
			case ACTION_UPDATE:
				action = Localization.getString("DownloadUpdate");
				message = source;
				break;
			case ACTION_REMOVE:
				action = Localization.getString("DeleteUpdate");
				message = target;
				break;
			default:
				break;
		}
		lblMessages.setText(action + " (" + message + ")\n" + lblMessages.getText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.update.UpdateManagerListener#updateInstallComplete(ch.supertomcat.bh.update.UpdateObject.UpdateType,
	 * ch.supertomcat.bh.update.UpdateObject.UpdateActionType)
	 */
	@Override
	public void updateInstallComplete(UpdateType updateType, UpdateActionType updateActionType) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.update.UpdateManagerListener#updateInstallFailed(ch.supertomcat.bh.update.UpdateObject.UpdateType,
	 * ch.supertomcat.bh.update.UpdateObject.UpdateActionType)
	 */
	@Override
	public void updateInstallFailed(UpdateType updateType, UpdateActionType updateActionType) {
		String message = "Unknown Message";
		switch (updateActionType) {
			case ACTION_NEW:
				message = Localization.getString("DownloadUpdateFailed");
				break;
			case ACTION_UPDATE:
				message = Localization.getString("DownloadUpdateFailed");
				break;
			case ACTION_REMOVE:
				message = Localization.getString("DeleteUpdateFailed");
				break;
			default:
				break;
		}
		lblMessages.setText(message + "\n" + lblMessages.getText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.update.UpdateManagerListener#errorOccured(java.lang.String)
	 */
	@Override
	public void errorOccured(String message) {
		lblMessages.setText(message + "\n" + lblMessages.getText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.update.UpdateManagerListener#updatesInstalled(int)
	 */
	@Override
	public void updatesInstalled(int updateCount) {
		if (updateCount == 0) {
			if (!(lblStatus.getText().equals(Localization.getString("NoUpdatesAvailable")))) {
				lblStatus.setText(Localization.getString("NoUpdatesInstalled"));
			}
		} else {
			lblStatus.setText(updateCount + " " + Localization.getString("UpdatesInstalled"));
		}
		prgUpdate.setIndeterminate(false);
		prgUpdate.setVisible(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.update.UpdateManagerListener#newProgramVersionInstalled()
	 */
	@Override
	public void newProgramVersionInstalled() {
		lblMain.setText(Localization.getString("NewProgramVersionInstalled"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.update.UpdateManagerListener#newProgramVersionInstallFailed()
	 */
	@Override
	public void newProgramVersionInstallFailed() {
		lblMain.setText(Localization.getString("NewProgramVersionInstallFailed"));
		btnChanges.setVisible(false);
	}
}

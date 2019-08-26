package ch.supertomcat.bh.gui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.settings.Settings;
import ch.supertomcat.bh.gui.update.UpdateWindow;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.update.UpdateManager;
import ch.supertomcat.bh.update.sources.httpxml.HTTPXMLUpdateSource;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * 
 *
 */
public class MainMenuBar implements ActionListener {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(MainMenuBar.class);

	/**
	 * MenuBar
	 */
	private JMenuBar mb = new JMenuBar();

	/**
	 * Menu
	 */
	private JMenu menuFile = new JMenu(Localization.getString("File"));

	/**
	 * MenuItem
	 */
	private JMenuItem itemExit = new JMenuItem(Localization.getString("Exit"), Icons.getTangoIcon("actions/system-log-out.png", 16));

	/**
	 * Menu
	 */
	private JMenu menuSettings = new JMenu(Localization.getString("Settings"));

	/**
	 * MenuItem
	 */
	private JMenuItem itemSettings = new JMenuItem(Localization.getString("Settings"), Icons.getTangoIcon("categories/preferences-system.png", 16));

	/**
	 * Menu
	 */
	private JMenu menuHelp = new JMenu(Localization.getString("Help"));

	/**
	 * MenuItem
	 */
	private JMenuItem itemUpdate = new JMenuItem(Localization.getString("Update"), Icons.getTangoIcon("apps/system-software-update.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemLogFolder = new JMenuItem(Localization.getString("OpenLogFolder"), Icons.getTangoIcon("places/folder.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemTutorial = new JMenuItem(Localization.getString("Tutorial"), Icons.getTangoIcon("apps/internet-web-browser.png", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem itemAbout = new JMenuItem(Localization.getString("About"), Icons.getTangoIcon("apps/help-browser.png", 16));

	/**
	 * lblLogFile
	 */
	private JLabel lblLogFile = new JLabel(Localization.getString("ActiveDownloadLogFile") + " ");

	/**
	 * cmbLogFile
	 */
	private JComboBox<String> cmbLogFile = new JComboBox<>();

	/**
	 * Constructor
	 */
	public MainMenuBar() {
		menuFile.add(itemExit);

		menuSettings.add(itemSettings);

		menuHelp.add(itemUpdate);
		menuHelp.add(itemLogFolder);
		menuHelp.add(itemTutorial);
		menuHelp.add(itemAbout);

		itemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		itemSettings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
		itemUpdate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.ALT_MASK));
		itemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, ActionEvent.SHIFT_MASK));
		itemTutorial.setAccelerator(KeyStroke.getKeyStroke("F1"));

		itemExit.addActionListener(this);

		itemSettings.addActionListener(this);

		itemUpdate.addActionListener(this);
		itemLogFolder.addActionListener(this);
		itemTutorial.addActionListener(this);
		itemAbout.addActionListener(this);

		String logFiles[] = LogManager.instance().getAvailableLogFileNames();
		for (int i = 0; i < logFiles.length; i++) {
			cmbLogFile.addItem(logFiles[i]);
		}
		int currentLogFileIndex = LogManager.instance().getCurrentLogFileIndexForArray(logFiles);
		cmbLogFile.setSelectedIndex(currentLogFileIndex);
		cmbLogFile.addActionListener(this);
		cmbLogFile.setMaximumSize(new Dimension(133, 20));
		cmbLogFile.setFocusable(false);
		if (logFiles.length == 1) {
			lblLogFile.setVisible(false);
			cmbLogFile.setVisible(false);
		}

		mb.add(menuFile);
		mb.add(menuSettings);
		mb.add(menuHelp);
		mb.add(Box.createGlue());
		mb.add(lblLogFile);
		mb.add(cmbLogFile);
		mb.add(Box.createGlue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == itemExit) {
			GuiEvent.instance().exitApp(false);
		} else if (e.getSource() == itemUpdate) {
			if (DownloadQueueManager.instance().isDownloading()) {
				JOptionPane.showMessageDialog(null, Localization.getString("UpdatesWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				UpdateWindow update = new UpdateWindow(new UpdateManager(new HTTPXMLUpdateSource()), Main.instance());
				update.setVisible(true);
				update.toFront();
			}
		} else if (e.getSource() == itemSettings) {
			new Settings(Main.instance());
		} else if (e.getSource() == itemAbout) {
			new About(Main.instance());
		} else if (e.getSource() == itemTutorial) {
			String url = ApplicationProperties.getProperty("TutorialURL");
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (IOException | URISyntaxException ex) {
					logger.error("Could not open URL: {}", url, ex);
				}
			} else {
				logger.error("Could not open URL, because Desktop is not supported: {}", url);
			}
		} else if (e.getSource() == itemLogFolder) {
			File logDir = new File(ApplicationProperties.getProperty("LogsPath"));
			try {
				Desktop.getDesktop().open(logDir);
			} catch (IOException e1) {
				logger.error("Could not open Directory: {}", logDir.getAbsolutePath(), e1);
			}
		} else if (e.getSource() == cmbLogFile) {
			SettingsManager.instance().setCurrentDownloadLogFile((String)cmbLogFile.getSelectedItem());
			SettingsManager.instance().writeSettings(true);
		}
	}

	/**
	 * @return JMenuBar
	 */
	public JMenuBar getJMenuBar() {
		return mb;
	}
}

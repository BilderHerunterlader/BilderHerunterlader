package ch.supertomcat.bh.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.applicationtool.ApplicationProperties;
import ch.supertomcat.supertomcattools.applicationtool.libraries.LibraryInfo;
import ch.supertomcat.supertomcattools.applicationtool.libraries.LibraryInfoUtil;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.UnitFormatTool;

/**
 * About-Panel
 */
public class About extends JDialog implements ActionListener, WindowListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 8193518716557926388L;

	/**
	 * Logger for this Klasse
	 */
	private static Logger logger = LoggerFactory.getLogger(About.class);

	private JTabbedPane tabPane = new JTabbedPane();

	/**
	 * Label
	 */
	private JTextArea lblAboutProgram = new JTextArea(25, 100);

	/**
	 * Label
	 */
	private JTextPane lblAboutLibs = new JTextPane();

	/**
	 * Label
	 */
	private JLabel lblMaxTotalMemory = new JLabel("");

	/**
	 * ProgressBar
	 */
	private JProgressBar pgMemUsed = new JProgressBar();

	/**
	 * ProgressBar
	 */
	private JProgressBar pgMemTotal = new JProgressBar();

	/**
	 * Timer
	 */
	private Timer timerMemory = null;

	/**
	 * Button
	 */
	private JButton btnWebsite = new JButton(Localization.getString("Website"), Icons.getTangoIcon("apps/internet-web-browser.png", 16));

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 */
	public About(Window owner) {
		setTitle(Localization.getString("About"));
		setIconImage(Icons.getBHImage("BH.png"));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());

		lblAboutProgram.setEditable(false);
		lblAboutProgram.setFont(UIManager.getFont("Label.font"));

		lblAboutLibs.setEditable(false);
		FontMetrics fontMetrics = lblAboutLibs.getFontMetrics(lblAboutLibs.getFont());
		int fontHeight = fontMetrics.getLeading() + fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();
		lblAboutLibs.setPreferredSize(new Dimension(100 * fontMetrics.charWidth('A'), 25 * fontHeight));

		StringBuilder aboutProgram = new StringBuilder();
		aboutProgram.append(ApplicationProperties.getProperty("ApplicationName"));
		aboutProgram.append(" (");
		aboutProgram.append(ApplicationProperties.getProperty("ApplicationVersion"));
		aboutProgram.append(")\nProgram Path: ");
		aboutProgram.append(new File(ApplicationProperties.getProperty("ApplicationPath")).getAbsolutePath());
		File profilePath = new File(ApplicationProperties.getProperty("ProfilePath"));
		aboutProgram.append("\nProfile Path: ");
		aboutProgram.append(profilePath.getAbsolutePath());

		File databasePath = new File(ApplicationProperties.getProperty("DatabasePath"));
		if (!databasePath.equals(profilePath)) {
			aboutProgram.append("\nDatabase Path: ");
			aboutProgram.append(databasePath.getAbsolutePath());
		}
		File settingsPath = new File(ApplicationProperties.getProperty("SettingsPath"));
		if (!settingsPath.equals(profilePath)) {
			aboutProgram.append("\nSettings Path: ");
			aboutProgram.append(settingsPath.getAbsolutePath());
		}
		File downloadLogPath = new File(ApplicationProperties.getProperty("DownloadLogPath"));
		if (!downloadLogPath.equals(profilePath)) {
			aboutProgram.append("\nDownloadLog Path: ");
			aboutProgram.append(downloadLogPath.getAbsolutePath());
		}
		File logsPath = new File(ApplicationProperties.getProperty("LogsPath"));
		if (!logsPath.equals(profilePath)) {
			aboutProgram.append("\nLogs Path: ");
			aboutProgram.append(logsPath.getAbsolutePath());
		}
		aboutProgram.append("\n\n");

		aboutProgram.append("Operating System:\n");
		aboutProgram.append(System.getProperty("os.name"));
		aboutProgram.append(" ");
		aboutProgram.append(System.getProperty("os.version"));
		aboutProgram.append(" ");
		aboutProgram.append(System.getProperty("os.arch"));
		aboutProgram.append("\nProcessors: ");
		aboutProgram.append(Runtime.getRuntime().availableProcessors());
		aboutProgram.append("\n\nJava:\nVersion: ");
		aboutProgram.append(System.getProperty("java.version"));
		aboutProgram.append("\n");
		aboutProgram.append(System.getProperty("java.vendor"));
		aboutProgram.append("\n");
		aboutProgram.append(System.getProperty("java.vm.name"));
		aboutProgram.append("\n");
		aboutProgram.append(System.getProperty("java.home"));
		aboutProgram.append("\n\n");

		aboutProgram.append("E-Mail: ");
		aboutProgram.append(ApplicationProperties.getProperty("MailAddress"));
		aboutProgram.append("\n\n");

		String licenseName = ApplicationProperties.getProperty("LicenseName");
		if (licenseName.length() > 0) {
			aboutProgram.append("License:\n");
			aboutProgram.append(licenseName);
			aboutProgram.append("\n");
		}
		String licenseText = ApplicationProperties.getProperty("LicenseText");
		if (licenseText.length() > 0) {
			aboutProgram.append(licenseText);
			aboutProgram.append("\n");
		}

		aboutProgram.append("\n" + ApplicationProperties.getProperty("AdditionalAboutText") + "\n");

		lblAboutProgram.setText(aboutProgram.toString());

		List<Point> boldPositions = new ArrayList<>();
		StringBuilder aboutLibs = new StringBuilder();
		for (LibraryInfo libInfo : LibraryInfoUtil.getLibraries()) {
			int startPos = aboutLibs.length();
			int length = libInfo.getName().length();
			boldPositions.add(new Point(startPos, length));
			aboutLibs.append(libInfo.getName());
			if (!libInfo.getVersion().isEmpty()) {
				aboutLibs.append("\nVersion: ");
				aboutLibs.append(libInfo.getVersion());
			}
			if (!libInfo.getLicense().isEmpty()) {
				aboutLibs.append("\nLicense: ");
				aboutLibs.append(libInfo.getLicense());
			}
			aboutLibs.append("\n\n");
		}

		lblAboutLibs.setText(aboutLibs.toString());

		for (Point p : boldPositions) {
			SimpleAttributeSet sas = new SimpleAttributeSet();
			StyleConstants.setBold(sas, true);
			lblAboutLibs.getStyledDocument().setCharacterAttributes(p.x, p.y, sas, false);
		}

		lblAboutProgram.setCaretPosition(0);
		lblAboutLibs.setCaretPosition(0);

		tabPane.addTab("Program", new JScrollPane(lblAboutProgram));
		tabPane.addTab("Libraries", new JScrollPane(lblAboutLibs));

		JPanel pnlMemory = new JPanel();
		pnlMemory.setBorder(BorderFactory.createTitledBorder(Localization.getString("MemoryUsage")));
		pnlMemory.setLayout(new GridLayout(3, 1));
		pnlMemory.add(lblMaxTotalMemory);
		pnlMemory.add(pgMemUsed);
		pnlMemory.add(pgMemTotal);

		pgMemUsed.setStringPainted(true);
		pgMemTotal.setStringPainted(true);
		updateMemory();

		JPanel pnlMain = new JPanel(new BorderLayout());
		pnlMain.add(tabPane, BorderLayout.CENTER);
		pnlMain.add(pnlMemory, BorderLayout.SOUTH);

		pnlButtons.add(btnWebsite);
		add(pnlMain, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);
		btnWebsite.addActionListener(this);

		timerMemory = new Timer();
		timerMemory.scheduleAtFixedRate(new MemoryUpdateTimerTask(), 0, 2000);

		addWindowListener(this);

		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	/**
	 * Update display
	 */
	public void updateMemory() {
		long max = Runtime.getRuntime().maxMemory();
		long free = Runtime.getRuntime().freeMemory();
		long total = Runtime.getRuntime().totalMemory();
		long used = total - free;

		int mode = SettingsManager.instance().getSizeView();

		lblMaxTotalMemory.setText(Localization.getString("MaximumAvailableMemory") + ": " + UnitFormatTool.getSizeString(max, mode));

		int val = (int)((used * 100.0d) / max);
		pgMemUsed.setMinimum(0);
		pgMemUsed.setMaximum(100);
		pgMemUsed.setValue(val);
		pgMemUsed.setString(Localization.getString("Used") + ": " + UnitFormatTool.getSizeString(used, mode) + " / " + UnitFormatTool.getSizeString(max, mode) + " (" + val + " %)");

		val = (int)((total * 100.0d) / max);
		pgMemTotal.setMinimum(0);
		pgMemTotal.setMaximum(100);
		pgMemTotal.setValue(val);
		pgMemTotal.setString(Localization.getString("CurrentlyAllocated") + ": " + UnitFormatTool.getSizeString(total, mode) + " / " + UnitFormatTool.getSizeString(max, mode) + " (" + val + " %)");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnWebsite) {
			String url = ApplicationProperties.getProperty("WebsiteURL");
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (IOException | URISyntaxException ex) {
					logger.error("Could not open URL: {}", url, ex);
				}
			} else {
				logger.error("Could not open URL, because Desktop is not supported: {}", url);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowOpened(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		if (timerMemory != null) {
			timerMemory.cancel();
			timerMemory = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosed(WindowEvent e) {
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
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowDeiconified(WindowEvent e) {
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
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	/**
	 * This timer sets a flag in the Pic to true, causing the Pic to recalculate
	 * the downloadrate.
	 * The is used to, recalculate the downloadrate only each 10 seconds.
	 */
	private class MemoryUpdateTimerTask extends TimerTask {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {
			About.this.updateMemory();
		}
	}
}

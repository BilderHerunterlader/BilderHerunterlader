package ch.supertomcat.bh.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.clipboard.ClipboardObserver;
import ch.supertomcat.bh.cookies.CookieManager;
import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.gui.hoster.HosterPanel;
import ch.supertomcat.bh.gui.keywords.Keywords;
import ch.supertomcat.bh.gui.log.DirectoryLog;
import ch.supertomcat.bh.gui.log.Log;
import ch.supertomcat.bh.gui.queue.Queue;
import ch.supertomcat.bh.gui.rules.Rules;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.importexport.ImportHTML;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.keywords.KeywordManagerListener;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.xml.WindowSettings;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.gui.svg.jsvg.JSVGAnimatedIconLabel;

/**
 * Main-Window
 */
public class MainWindow extends JFrame implements MainWindowAccess {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1091699671455131110L;

	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Queue-Panel
	 */
	private Queue queue;

	/**
	 * Log-Panel
	 */
	private Log log;

	/**
	 * directoryLog
	 */
	private DirectoryLog directoryLog;

	/**
	 * Keywords-Panel
	 */
	private Keywords keywords;

	/**
	 * Hoster-Panel
	 */
	private HosterPanel hosts;

	/**
	 * Rules-Panel
	 */
	private Rules rules;

	/**
	 * Tabpane
	 */
	private JTabbedPane tab = new JTabbedPane();

	/**
	 * Label
	 */
	private JLabel lblMessage = new JLabel(Localization.getString("BHStarted"));

	/**
	 * Panel
	 */
	private JPanel pnlMessage = new JPanel();

	private final Icon emptyProgressLoadingIcon = BHIcons.getBHIcon("Dummy.png", 16);

	private final Icon progressLoadingIcon = BHIcons.getBHSVGIcon("animations/process-working.svg", 16);

	private JSVGAnimatedIconLabel lblProgress = new JSVGAnimatedIconLabel(emptyProgressLoadingIcon);

	private String windowTitlePrefix = ApplicationProperties.getProperty(ApplicationMain.APPLICATION_SHORT_NAME) + " - ";
	private String windowTitleSuffix = " (" + ApplicationProperties.getProperty(ApplicationMain.APPLICATION_VERSION) + ")";

	/**
	 * mainMenuBar
	 */
	private MainMenuBar mainMenuBar;

	/**
	 * mainProgressPopup
	 */
	private MainProgressPopup mainProgressPopup = new MainProgressPopup();

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 * @param logManager Log Manager
	 * @param queueManager Queue Manager
	 * @param downloadQueueManager Download Queue Manager
	 * @param keywordManager Keyword Manager
	 * @param proxyManger Proxy Manager
	 * @param cookieManager Cookie Manager
	 * @param hostManager Host Manager
	 * @param clipboardObserver Clipboard Observer
	 * @param guiEvent GUI Event
	 */
	public MainWindow(SettingsManager settingsManager, LogManager logManager, QueueManager queueManager, DownloadQueueManager downloadQueueManager, KeywordManager keywordManager,
			ProxyManager proxyManger, CookieManager cookieManager, HostManager hostManager, ClipboardObserver clipboardObserver, GuiEvent guiEvent) {
		this.mainMenuBar = new MainMenuBar(this, this, logManager, downloadQueueManager, queueManager, keywordManager, proxyManger, settingsManager, cookieManager, hostManager, guiEvent);
		this.queue = new Queue(this, this, queueManager, downloadQueueManager, logManager, keywordManager, proxyManger, settingsManager, cookieManager, hostManager, clipboardObserver);
		this.log = new Log(logManager, downloadQueueManager, this, this, settingsManager, clipboardObserver);
		this.directoryLog = new DirectoryLog(logManager, settingsManager);
		this.keywords = new Keywords(this, this, keywordManager, settingsManager);
		this.hosts = new HosterPanel(hostManager, downloadQueueManager, settingsManager);
		this.rules = new Rules(this, this, downloadQueueManager, settingsManager, hostManager);
		setIconImage(BHIcons.getBHMultiResImage("BH.png"));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		tab.setFocusable(false);
		tab.addTab(Localization.getString("Queue"), Icons.getTangoSVGIcon("actions/go-down.svg", 22), queue);
		tab.addTab(Localization.getString("Log"), Icons.getTangoSVGIcon("mimetypes/text-x-generic.svg", 22), log);
		tab.addTab(Localization.getString("DirectoryLog"), Icons.getTangoSVGIcon("mimetypes/text-x-generic.svg", 22), directoryLog);
		tab.addTab(Localization.getString("Keywords"), Icons.getTangoSVGIcon("emblems/emblem-favorite.svg", 22), keywords);
		tab.addTab(Localization.getString("Rules"), BHIcons.getBHSVGIcon("rule.svg", 22), rules);
		tab.addTab(Localization.getString("HosterPlugins"), BHIcons.getBHSVGIcon("hostplugin.svg", 22), hosts);

		DropTargetAdapter dtl = new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent e) {
				try {
					Transferable tr = e.getTransferable();
					DataFlavor[] flavors = tr.getTransferDataFlavors();
					for (int i = 0; i < flavors.length; i++) {
						if (flavors[i].isFlavorTextType()) {
							e.acceptDrop(e.getDropAction());
							Object td = tr.getTransferData(flavors[i]);
							if (td instanceof InputStreamReader reader) {
								@SuppressWarnings("resource")
								BufferedReader br = new BufferedReader(reader);
								String line;
								StringJoiner sjData = new StringJoiner("\n");
								while ((line = br.readLine()) != null) {
									sjData.add(line);
								}
								String title = Localization.getString("Unkown");
								String referrer = Localization.getString("Unkown");
								byte[] stringBytes = sjData.toString().getBytes(StandardCharsets.UTF_8);
								ByteArrayInputStream bais = new ByteArrayInputStream(stringBytes);
								new ImportHTML(MainWindow.this, MainWindow.this, logManager, queueManager, keywordManager, proxyManger, settingsManager, hostManager, cookieManager, clipboardObserver)
										.importHTML(bais, referrer, title);
								br.close();
								e.dropComplete(true);
								return;
							}
						}
					}
				} catch (Throwable t) {
					logger.error(t.getMessage(), t);
				}
				e.rejectDrop();
			}
		};
		new DropTarget(tab, dtl);

		setJMenuBar(mainMenuBar.getJMenuBar());

		lblProgress.setIcon(emptyProgressLoadingIcon);

		pnlMessage.setLayout(new BorderLayout());
		pnlMessage.add(lblMessage, BorderLayout.WEST);
		pnlMessage.add(lblProgress, BorderLayout.EAST);
		lblProgress.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (e.getSource() instanceof JComponent comp) {
					Dimension d = mainProgressPopup.getPreferredSize();
					int w = d.width;
					int h = d.height;
					if (w < 300) {
						w = 300;
					}
					int x = getX() + getInsets().left + pnlMessage.getX() + comp.getX() + comp.getWidth() - w;
					int y = getY() + getInsets().top + mainMenuBar.getJMenuBar().getHeight() + pnlMessage.getY() + comp.getY() - h;
					mainProgressPopup.setBounds(x, y, w, h);
					mainProgressPopup.setVisible(true);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				mainProgressPopup.setVisible(false);
			}
		});

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tab, BorderLayout.CENTER);
		getContentPane().add(pnlMessage, BorderLayout.SOUTH);

		keywordManager.addListener(new KeywordManagerListener() {

			@Override
			public void keywordsChanged() {
				keywords.reloadKeywords();
			}
		});

		tab.addChangeListener(e -> {
			String tabTitle = tab.getTitleAt(tab.getSelectedIndex());
			this.setTitle(windowTitlePrefix + tabTitle + windowTitleSuffix);
			if (tabTitle.equals(Localization.getString("Log"))) {
				log.reloadLogs();
			} else if (tabTitle.equals(Localization.getString("DirectoryLog"))) {
				directoryLog.init();
			}
		});
		this.setTitle(windowTitlePrefix + Localization.getString("Queue") + windowTitleSuffix);

		WindowSettings mainWindowSettings = settingsManager.getGUISettings().getMainWindow();
		if (mainWindowSettings.isSave() && mainWindowSettings.getWidth() > 0 && mainWindowSettings.getHeight() > 0) {
			this.setSize(mainWindowSettings.getWidth(), mainWindowSettings.getHeight());
			this.setLocation(mainWindowSettings.getX(), mainWindowSettings.getY());
			this.setExtendedState(mainWindowSettings.getState());
		} else {
			pack();
			setLocationRelativeTo(null);
		}

		addComponentListener(new ComponentListener() {
			@Override
			public void componentHidden(ComponentEvent e) {
				// Nothing to do
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				WindowSettings mainWindowSettings = settingsManager.getGUISettings().getMainWindow();
				mainWindowSettings.setX(MainWindow.this.getX());
				mainWindowSettings.setY(MainWindow.this.getY());
				settingsManager.writeSettings(true);
			}

			@Override
			public void componentResized(ComponentEvent e) {
				WindowSettings mainWindowSettings = settingsManager.getGUISettings().getMainWindow();
				mainWindowSettings.setWidth(MainWindow.this.getWidth());
				mainWindowSettings.setHeight(MainWindow.this.getHeight());
				mainWindowSettings.setState(MainWindow.this.getExtendedState());
				settingsManager.writeSettings(true);
			}

			@Override
			public void componentShown(ComponentEvent e) {
				// Nothing to do
			}
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (!guiEvent.isSystemTray()) {
					if (JOptionPane.showConfirmDialog(MainWindow.this, Localization.getString("ReallyExit"), Localization.getString("Exit"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
						return;
					}
					// If no systemtray exit application
					guiEvent.exitApp(false, false);
				} else {
					setVisible(false);
				}
			}
		});
	}

	@Override
	public String getMessage() {
		return lblMessage.getText();
	}

	@Override
	public void setMessage(String message) {
		this.lblMessage.setText(message);
	}

	@Override
	public synchronized void addProgressObserver(ProgressObserver progress) {
		mainProgressPopup.addProgressObserver(progress);
		lblProgress.setIcon(progressLoadingIcon);
		lblProgress.startAnimation();
	}

	@Override
	public synchronized void removeProgressObserver(ProgressObserver progress) {
		mainProgressPopup.removeProgressObserver(progress);
		if (mainProgressPopup.getProgressObserverCount() == 0) {
			lblProgress.stopAnimation();
			lblProgress.setIcon(emptyProgressLoadingIcon);
		}
	}

	@Override
	public void clearKeywordFilters() {
		keywords.clearFilters();
	}

	@Override
	public boolean isTabSelected(Component tabComponent) {
		return tab.getSelectedComponent() == tabComponent;
	}
}

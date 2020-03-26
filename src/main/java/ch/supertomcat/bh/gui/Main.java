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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.clipboard.ClipboardObserver;
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
import ch.supertomcat.bh.settings.CookieManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.systemtray.SystemTrayTool;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;

/**
 * Main-Window
 */
public class Main extends JFrame implements ChangeListener, ComponentListener, WindowListener, KeywordManagerListener, MouseListener, MainWindowAccess {
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

	private JLabel lblProgress = new JLabel("");

	private String windowTitlePrefix = ApplicationProperties.getProperty("ApplicationShortName") + " - ";
	private String windowTitleSuffix = " (" + ApplicationProperties.getProperty("ApplicationVersion") + ")";

	/**
	 * mainMenuBar
	 */
	private MainMenuBar mainMenuBar;

	/**
	 * mainProgressPopup
	 */
	private MainProgressPopup mainProgressPopup = new MainProgressPopup();

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * GUI Event
	 */
	private final GuiEvent guiEvent;

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
	public Main(SettingsManager settingsManager, LogManager logManager, QueueManager queueManager, DownloadQueueManager downloadQueueManager, KeywordManager keywordManager, ProxyManager proxyManger,
			CookieManager cookieManager, HostManager hostManager, ClipboardObserver clipboardObserver, GuiEvent guiEvent) {
		this.settingsManager = settingsManager;
		this.guiEvent = guiEvent;
		this.mainMenuBar = new MainMenuBar(this, this, logManager, downloadQueueManager, queueManager, keywordManager, proxyManger, settingsManager, cookieManager, hostManager, guiEvent);
		this.queue = new Queue(this, this, queueManager, downloadQueueManager, logManager, keywordManager, proxyManger, settingsManager, cookieManager, hostManager, clipboardObserver);
		this.log = new Log(logManager, downloadQueueManager, this, settingsManager, clipboardObserver);
		this.directoryLog = new DirectoryLog(logManager, settingsManager);
		this.keywords = new Keywords(this, this, keywordManager, settingsManager);
		this.hosts = new HosterPanel(hostManager, downloadQueueManager, settingsManager);
		this.rules = new Rules(this, this, downloadQueueManager, settingsManager, hostManager);
		setIconImage(Icons.getBHImage("BH.png"));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		tab.setFocusable(false);
		tab.addTab(Localization.getString("Queue"), Icons.getTangoIcon("actions/go-down.png", 22), queue);
		tab.addTab(Localization.getString("Log"), Icons.getTangoIcon("mimetypes/text-x-generic.png", 22), log);
		tab.addTab(Localization.getString("DirectoryLog"), Icons.getTangoIcon("mimetypes/text-x-generic.png", 22), directoryLog);
		tab.addTab(Localization.getString("Keywords"), Icons.getTangoIcon("emblems/emblem-favorite.png", 22), keywords);
		tab.addTab(Localization.getString("Rules"), Icons.getBHIcon("actions/approval.png", 22), rules);
		tab.addTab(Localization.getString("HosterPlugins"), Icons.getBHIcon("actions/approval.png", 22), hosts);

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
							if (td instanceof InputStreamReader) {
								@SuppressWarnings("resource")
								BufferedReader br = new BufferedReader((InputStreamReader)td);
								String line;
								StringBuilder sbData = new StringBuilder();
								while ((line = br.readLine()) != null) {
									sbData.append(line + "\n");
								}
								String title = Localization.getString("Unkown");
								String referrer = Localization.getString("Unkown");
								byte[] stringBytes = sbData.toString().getBytes();
								ByteArrayInputStream bais = new ByteArrayInputStream(stringBytes);
								new ImportHTML(Main.this, Main.this, logManager, queueManager, keywordManager, proxyManger, settingsManager, hostManager, cookieManager, clipboardObserver)
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

		lblProgress.setIcon(Icons.getBHIcon("Dummy.png", 16));

		pnlMessage.setLayout(new BorderLayout());
		pnlMessage.add(lblMessage, BorderLayout.WEST);
		pnlMessage.add(lblProgress, BorderLayout.EAST);
		lblProgress.addMouseListener(this);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tab, BorderLayout.CENTER);
		getContentPane().add(pnlMessage, BorderLayout.SOUTH);

		keywordManager.addListener(this);

		tab.addChangeListener(this);
		this.setTitle(windowTitlePrefix + Localization.getString("Queue") + windowTitleSuffix);

		SettingsManager sm = settingsManager;
		if (sm.isSaveWindowSizePosition()) {
			this.setSize(sm.getWindowWidth(), sm.getWindowHeight());
			this.setLocation(sm.getWindowXPos(), sm.getWindowYPos());
			this.setExtendedState(sm.getWindowState());
		} else {
			pack();
			setLocationRelativeTo(null);
		}

		addComponentListener(this);
		addWindowListener(this);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		String tabTitle = tab.getTitleAt(tab.getSelectedIndex());
		this.setTitle(windowTitlePrefix + tabTitle + windowTitleSuffix);
		if (tabTitle.equals(Localization.getString("Log"))) {
			log.reloadLogs();
		} else if (tabTitle.equals(Localization.getString("DirectoryLog"))) {
			directoryLog.init();
		}
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		settingsManager.setWindowXPos(this.getX());
		settingsManager.setWindowYPos(this.getY());
		settingsManager.writeSettings(true);
	}

	@Override
	public void componentResized(ComponentEvent e) {
		settingsManager.setWindowWidth(this.getWidth());
		settingsManager.setWindowHeight(this.getHeight());
		settingsManager.setWindowState(this.getExtendedState());
		settingsManager.writeSettings(true);
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (!SystemTrayTool.isTraySupported()) {
			if (JOptionPane.showConfirmDialog(this, Localization.getString("ReallyExit"), Localization.getString("Exit"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
				return;
			}
			// If no systemtray exit application
			guiEvent.exitApp(false);
		} else {
			setVisible(false);
		}
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
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
		lblProgress.setIcon(Icons.getBHIcon("animations/process-working.gif", 16));
	}

	@Override
	public synchronized void removeProgressObserver(ProgressObserver progress) {
		mainProgressPopup.removeProgressObserver(progress);
		if (mainProgressPopup.getProgressObserverCount() == 0) {
			lblProgress.setIcon(Icons.getBHIcon("Dummy.png", 16));
		}
	}

	@Override
	public void keywordsChanged() {
		keywords.reloadKeywords();
	}

	@Override
	public void clearKeywordFilters() {
		keywords.clearFilters();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (e.getSource() instanceof JComponent) {
			JComponent comp = (JComponent)e.getSource();
			Dimension d = mainProgressPopup.getPreferredSize();
			int w = d.width;
			int h = d.height;
			if (w < 300) {
				w = 300;
			}
			int x = this.getX() + this.getInsets().left + pnlMessage.getX() + comp.getX() + comp.getWidth() - w;
			int y = this.getY() + this.getInsets().top + mainMenuBar.getJMenuBar().getHeight() + pnlMessage.getY() + comp.getY() - h;
			mainProgressPopup.setBounds(x, y, w, h);
			mainProgressPopup.setVisible(true);
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		mainProgressPopup.setVisible(false);
	}

	@Override
	public boolean isTabSelected(Component tabComponent) {
		return tab.getSelectedComponent() == tabComponent;
	}
}

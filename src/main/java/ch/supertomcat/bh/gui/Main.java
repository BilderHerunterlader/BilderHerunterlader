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

import ch.supertomcat.bh.gui.hoster.HosterPanel;
import ch.supertomcat.bh.gui.keywords.Keywords;
import ch.supertomcat.bh.gui.log.DirectoryLog;
import ch.supertomcat.bh.gui.log.Log;
import ch.supertomcat.bh.gui.queue.Queue;
import ch.supertomcat.bh.gui.rules.Rules;
import ch.supertomcat.bh.importexport.ImportHTML;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.keywords.KeywordManagerListener;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.systemtray.SystemTrayTool;
import ch.supertomcat.supertomcattools.applicationtool.ApplicationProperties;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.progressmonitor.ProgressObserver;

/**
 * Main-Window
 */
public class Main extends JFrame implements ChangeListener, ComponentListener, WindowListener, KeywordManagerListener, MouseListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1091699671455131110L;

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 * Singleton
	 */
	private static Main instance;

	/**
	 * Queue-Panel
	 */
	private Queue queue = new Queue();

	/**
	 * Log-Panel
	 */
	private Log log = new Log();

	/**
	 * directoryLog
	 */
	private DirectoryLog directoryLog = new DirectoryLog();

	/**
	 * Keywords-Panel
	 */
	private Keywords keywords = new Keywords();

	/**
	 * Hoster-Panel
	 */
	private HosterPanel hosts = new HosterPanel();

	/**
	 * Rules-Panel
	 */
	private Rules rules = new Rules();

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
	private MainMenuBar mainMenuBar = new MainMenuBar();

	/**
	 * mainProgressPopup
	 */
	private MainProgressPopup mainProgressPopup = new MainProgressPopup();

	/**
	 * Constructor
	 */
	private Main() {
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
								ImportHTML.importHTML(bais, referrer, title);
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

		KeywordManager.instance().addListener(this);

		tab.addChangeListener(this);
		this.setTitle(windowTitlePrefix + Localization.getString("Queue") + windowTitleSuffix);

		SettingsManager sm = SettingsManager.instance();
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

	/**
	 * Returns the instance
	 * 
	 * @return Instance
	 */
	public static Main instance() {
		if (instance == null) {
			instance = new Main();
		}
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentHidden(ComponentEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentMoved(ComponentEvent e) {
		SettingsManager sm = SettingsManager.instance();
		sm.setWindowXPos(this.getX());
		sm.setWindowYPos(this.getY());
		sm.writeSettings(true);
		sm = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentResized(ComponentEvent e) {
		SettingsManager sm = SettingsManager.instance();
		sm.setWindowWidth(this.getWidth());
		sm.setWindowHeight(this.getHeight());
		sm.setWindowState(this.getExtendedState());
		sm.writeSettings(true);
		sm = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentShown(ComponentEvent e) {
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		if (!SystemTrayTool.isTraySupported()) {
			if (JOptionPane.showConfirmDialog(Main.instance(), Localization.getString("ReallyExit"), Localization.getString("Exit"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
				return;
			}
			// If no systemtray exit application
			GuiEvent.instance().exitApp(false);
		} else {
			setVisible(false);
		}
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
	}

	/**
	 * Get-Method
	 * 
	 * @return Message
	 */
	public String getMessage() {
		return lblMessage.getText();
	}

	/**
	 * Set-Method
	 * 
	 * @param message Message
	 */
	public void setMessage(String message) {
		this.lblMessage.setText(message);
	}

	/**
	 * @param progress Progress
	 */
	public synchronized void addProgressObserver(ProgressObserver progress) {
		mainProgressPopup.addProgressObserver(progress);
		lblProgress.setIcon(Icons.getBHIcon("animations/process-working.gif", 16));
	}

	/**
	 * @param progress Progress
	 */
	public synchronized void removeProgressObserver(ProgressObserver progress) {
		mainProgressPopup.removeProgressObserver(progress);
		if (mainProgressPopup.getProgressObserverCount() == 0) {
			lblProgress.setIcon(Icons.getBHIcon("Dummy.png", 16));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.supertomcat.bh.keywords.KeywordManagerListener#keywordsChanged()
	 */
	@Override
	public void keywordsChanged() {
		keywords.reloadKeywords();
	}

	/**
	 * Clear Keyword filters
	 */
	public void clearKeywordFilters() {
		keywords.clearFilters();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		mainProgressPopup.setVisible(false);
	}

	/**
	 * Checks if the tab with the given component is selected
	 * 
	 * @param tabComponent Tab Component
	 * @return True if the tab with the given component is selected, false otherwise
	 */
	public boolean isTabSelected(Component tabComponent) {
		return tab.getSelectedComponent() == tabComponent;
	}
}

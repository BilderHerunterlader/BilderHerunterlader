package ch.supertomcat.bh.gui.settings;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import ch.supertomcat.bh.cookies.CookieManager;
import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.gui.BHIcons;
import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.gui.settings.tabs.ConnectionSettingsPanel;
import ch.supertomcat.bh.gui.settings.tabs.DetectionSettingsPanel;
import ch.supertomcat.bh.gui.settings.tabs.DownloadSettingsPanel;
import ch.supertomcat.bh.gui.settings.tabs.GUISettingsPanel;
import ch.supertomcat.bh.gui.settings.tabs.KeywordsSettingsPanel;
import ch.supertomcat.bh.gui.settings.tabs.OtherSettingsPanel;
import ch.supertomcat.bh.gui.settings.tabs.PathsSettingsPanel;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.settings.BHSettingsListener;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.xml.LookAndFeelSetting;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Settings-Panel
 */
public class SettingsDialog extends JDialog {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * TabbedPane
	 */
	private JTabbedPane tabPane = new JTabbedPane();

	/**
	 * Scrollpane
	 */
	private JScrollPane spGUI;

	/**
	 * Scrollpane
	 */
	private JScrollPane spConnection;

	/**
	 * Scrollpane
	 */
	private JScrollPane spPaths;

	/**
	 * Scrollpane
	 */
	private JScrollPane spKeywords;

	/**
	 * Scrollpane
	 */
	private JScrollPane spDownload;

	/**
	 * Scrollpane
	 */
	private JScrollPane spDetection;

	/**
	 * Scrollpane
	 */
	private JScrollPane spOther;

	/**
	 * Panel
	 */
	private GUISettingsPanel pnlGUI;

	/**
	 * Panel
	 */
	private ConnectionSettingsPanel pnlConnection;

	/**
	 * Panel
	 */
	private PathsSettingsPanel pnlPaths;

	/**
	 * Panel
	 */
	private KeywordsSettingsPanel pnlKeywords;

	/**
	 * Panel
	 */
	private DownloadSettingsPanel pnlDownload;

	/**
	 * Panel
	 */
	private DetectionSettingsPanel pnlDetection;

	/**
	 * Panel
	 */
	private OtherSettingsPanel pnlOther;

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * Button
	 */
	private JButton btnSave = new JButton(Localization.getString("SaveAndApply"), Icons.getTangoSVGIcon("actions/document-save.svg", 16));

	/**
	 * Button
	 */
	private JButton btnReset = new JButton(Localization.getString("Reset"), Icons.getTangoSVGIcon("actions/edit-undo.svg", 16));

	/**
	 * Button
	 */
	private JButton btnCancel = new JButton(Localization.getString("Cancel"), Icons.getTangoSVGIcon("emblems/emblem-unreadable.svg", 16));

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Main Window Access
	 */
	private final MainWindowAccess mainWindowAccess;

	/**
	 * SettingsListener
	 */
	private final BHSettingsListener settingsListener = new BHSettingsListener() {
		@Override
		public void settingsChanged() {
			init();
		}

		@Override
		public void lookAndFeelChanged(LookAndFeelSetting lookAndFeel) {
			// Nothing to do
		}
	};

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param mainWindowAccess Main Window Access
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param cookieManager Cookie Manager
	 * @param hostManager Host Manager
	 */
	public SettingsDialog(Window owner, MainWindowAccess mainWindowAccess, ProxyManager proxyManager, SettingsManager settingsManager, CookieManager cookieManager, HostManager hostManager) {
		this.mainWindowAccess = mainWindowAccess;
		this.settingsManager = settingsManager;
		setTitle(Localization.getString("Settings"));
		setModal(true);
		setIconImage(BHIcons.getBHMultiResImage("BH.png"));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLayout(new BorderLayout());

		this.pnlGUI = new GUISettingsPanel(settingsManager, owner);
		this.pnlConnection = new ConnectionSettingsPanel(settingsManager, proxyManager, cookieManager);
		this.pnlPaths = new PathsSettingsPanel(settingsManager);
		this.pnlKeywords = new KeywordsSettingsPanel(settingsManager);
		this.pnlDownload = new DownloadSettingsPanel(settingsManager);
		this.pnlDetection = new DetectionSettingsPanel(settingsManager);
		this.pnlOther = new OtherSettingsPanel(settingsManager, hostManager);

		spGUI = new JScrollPane(pnlGUI);
		spConnection = new JScrollPane(pnlConnection);
		spPaths = new JScrollPane(pnlPaths);
		spKeywords = new JScrollPane(pnlKeywords);
		spDownload = new JScrollPane(pnlDownload);
		spDetection = new JScrollPane(pnlDetection);
		spOther = new JScrollPane(pnlOther);

		btnCancel.setMnemonic(KeyEvent.VK_C);

		pnlButtons.add(btnSave);
		pnlButtons.add(btnReset);
		pnlButtons.add(btnCancel);
		btnSave.addActionListener(e -> {
			applySettings();
			mainWindowAccess.setMessage(Localization.getString("SavingSettings"));
			boolean b = settingsManager.writeSettings(true);
			if (b) {
				mainWindowAccess.setMessage(Localization.getString("SettingsSaved"));
			} else {
				mainWindowAccess.setMessage(Localization.getString("SettingsSaveFailed"));
			}
			settingsManager.removeSettingsListener(settingsListener);
			dispose();
		});
		btnReset.addActionListener(e -> init());
		btnCancel.addActionListener(e -> {
			settingsManager.removeSettingsListener(settingsListener);
			dispose();
		});

		tabPane.setFocusable(false);
		tabPane.setTabPlacement(SwingConstants.TOP);
		tabPane.addTab(Localization.getString("SettingsGUI"), Icons.getTangoSVGIcon("apps/preferences-system-windows.svg", 22), spGUI);
		tabPane.addTab(Localization.getString("SettingsConnection"), Icons.getTangoSVGIcon("status/network-idle.svg", 22), spConnection);
		tabPane.addTab(Localization.getString("SettingsPaths"), Icons.getTangoSVGIcon("places/folder.svg", 22), spPaths);
		tabPane.addTab(Localization.getString("SettingsKeywords"), Icons.getTangoSVGIcon("emblems/emblem-favorite.svg", 22), spKeywords);
		tabPane.addTab(Localization.getString("SettingsDownload"), Icons.getTangoSVGIcon("actions/go-down.svg", 22), spDownload);
		tabPane.addTab(Localization.getString("SettingsDetection"), Icons.getTangoSVGIcon("actions/edit-find.svg", 22), spDetection);
		tabPane.addTab(Localization.getString("SettingsOther"), Icons.getTangoSVGIcon("categories/preferences-system.svg", 22), spOther);

		add(tabPane, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);

		init();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				settingsManager.removeSettingsListener(settingsListener);
				dispose();
			}
		});

		settingsManager.addSettingsListener(settingsListener);

		pack();
		setLocationRelativeTo(owner);

		setVisible(true);
	}

	/**
	 * Initialize
	 */
	private void init() {
		pnlGUI.init();
		pnlConnection.init();
		pnlPaths.init();
		pnlKeywords.init();
		pnlDownload.init();
		pnlDetection.init();
		pnlOther.init();
	}

	/**
	 * Apply Settings
	 */
	private void applySettings() {
		settingsManager.removeSettingsListener(settingsListener);
		mainWindowAccess.setMessage(Localization.getString("ApplyingSettings"));

		pnlGUI.applySettings();
		pnlConnection.applySettings();
		pnlPaths.applySettings();
		pnlKeywords.applySettings();
		pnlDownload.applySettings();
		pnlDetection.applySettings();
		pnlOther.applySettings();

		settingsManager.fireSettingsChanged();

		mainWindowAccess.setMessage(Localization.getString("SettingsApplied"));
		settingsManager.addSettingsListener(settingsListener);
	}
}

package ch.supertomcat.bh.gui.settings.tabs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import ch.supertomcat.bh.cookies.CookieManager;
import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.gui.settings.BrowserCookiesModeComboBoxRenderer;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.xml.BrowserCookiesMode;
import ch.supertomcat.bh.settings.xml.BrowserCookiesSetting;
import ch.supertomcat.bh.settings.xml.ConnectionSettings;
import ch.supertomcat.bh.settings.xml.ProxyMode;
import ch.supertomcat.bh.settings.xml.Settings;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.dialog.FileDialogUtil;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;
import ch.supertomcat.supertomcatutils.http.cookies.BrowserCookies;

/**
 * Connection Settings Panel
 */
public class ConnectionSettingsPanel extends SettingsPanelBase {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Proxy Manager
	 */
	private final ProxyManager proxyManager;

	/**
	 * Cookie Manager
	 */
	private final CookieManager cookieManager;

	/**
	 * ButtonGroup
	 */
	private ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * RadioButton
	 */
	private JRadioButton rbNoProxy = new JRadioButton(Localization.getString("ProxyDirectConnection"), true);

	/**
	 * RadioButton
	 */
	private JRadioButton rbHTTP = new JRadioButton(Localization.getString("ProxyHTTP"), false);

	/**
	 * CheckBox
	 */
	private JCheckBox cbAuth = new JCheckBox(Localization.getString("ProxyAuthentication"), false);

	/**
	 * Label
	 */
	private JLabel lblProxyName = new JLabel(Localization.getString("ProxyName"));

	/**
	 * TextField
	 */
	private JTextField txtProxyName = new JTextField("127.0.0.1", 20);

	/**
	 * Label
	 */
	private JLabel lblProxyPort = new JLabel(Localization.getString("ProxyPort"));

	/**
	 * TextField
	 */
	private JTextField txtProxyPort = new JTextField("80", 20);

	/**
	 * Label
	 */
	private JLabel lblProxyUser = new JLabel(Localization.getString("ProxyUser"));

	/**
	 * TextField
	 */
	private JTextField txtProxyUser = new JTextField("", 20);

	/**
	 * Label
	 */
	private JLabel lblProxyPassword = new JLabel(Localization.getString("ProxyPassword"));

	/**
	 * TextField
	 */
	private JPasswordField txtProxyPassword = new JPasswordField("", 20);

	/**
	 * Label
	 */
	private JLabel lblConnectionCount = new JLabel(Localization.getString("ConnectionCount"));

	/**
	 * TextField
	 */
	private JTextField txtConnectionCount = new JTextField("", 3);

	/**
	 * Slider
	 */
	private JSlider sldConnectionCount = new JSlider();

	/**
	 * Label
	 */
	private JLabel lblConnectionCountPerHost = new JLabel(Localization.getString("ConnectionCountPerHost"));

	/**
	 * TextField
	 */
	private JTextField txtConnectionCountPerHost = new JTextField("", 3);

	/**
	 * Slider
	 */
	private JSlider sldConnectionCountPerHost = new JSlider();

	/**
	 * Label
	 */
	private JLabel lblConnectTimeout = new JLabel(Localization.getString("ConnectTimeout"));

	/**
	 * TextField
	 */
	private JTextField txtConnectTimeout = new JTextField("30000");

	/**
	 * Label
	 */
	private JLabel lblSocketTimeout = new JLabel(Localization.getString("SocketTimeout"));

	/**
	 * TextField
	 */
	private JTextField txtSocketTimeout = new JTextField("30000");

	/**
	 * Label
	 */
	private JLabel lblConnectionRequestTimeout = new JLabel(Localization.getString("ConnectionRequestTimeout"));

	/**
	 * TextField
	 */
	private JTextField txtConnectionRequestTimeout = new JTextField("30000");

	/**
	 * Label
	 */
	private JLabel lblCookies = new JLabel(Localization.getString("CookieFromBrowser"));

	/**
	 * ComboBox
	 */
	private JComboBox<BrowserCookiesMode> cmbCookies = new JComboBox<>();

	/**
	 * Label
	 */
	private JLabel lblCookiesOpera = new JLabel(Localization.getString("CookiesOpera"));

	/**
	 * Panel
	 */
	private JPanel pnlCookiesOpera = new JPanel(new BorderLayout());

	/**
	 * Checkbox
	 */
	private JCheckBox cbCookiesOperaFixed = new JCheckBox();

	/**
	 * TextField
	 */
	private JTextField txtCookiesOpera = new JTextField(60);

	/**
	 * Button
	 */
	private JButton btnCookiesOpera = new JButton("...");

	/**
	 * Label
	 */
	private JLabel lblCookiesOperaNew = new JLabel(Localization.getString("CookiesOperaNew"));

	/**
	 * Panel
	 */
	private JPanel pnlCookiesOperaNew = new JPanel(new BorderLayout());

	/**
	 * Checkbox
	 */
	private JCheckBox cbCookiesOperaNewFixed = new JCheckBox();

	/**
	 * TextField
	 */
	private JTextField txtCookiesOperaNew = new JTextField(60);

	/**
	 * Button
	 */
	private JButton btnCookiesOperaNew = new JButton("...");

	/**
	 * Label
	 */
	private JLabel lblCookiesFirefox = new JLabel(Localization.getString("CookiesFirefox"));

	/**
	 * Panel
	 */
	private JPanel pnlCookiesFirefox = new JPanel(new BorderLayout());

	/**
	 * Checkbox
	 */
	private JCheckBox cbCookiesFirefoxFixed = new JCheckBox();

	/**
	 * TextField
	 */
	private JTextField txtCookiesFirefox = new JTextField(60);

	/**
	 * Button
	 */
	private JButton btnCookiesFirefox = new JButton("...");

	/**
	 * Label
	 */
	private JLabel lblCookiesPaleMoon = new JLabel(Localization.getString("CookiesPaleMoon"));

	/**
	 * Panel
	 */
	private JPanel pnlCookiesPaleMoon = new JPanel(new BorderLayout());

	/**
	 * Checkbox
	 */
	private JCheckBox cbCookiesPaleMoonFixed = new JCheckBox();

	/**
	 * TextField
	 */
	private JTextField txtCookiesPaleMoon = new JTextField(60);

	/**
	 * Button
	 */
	private JButton btnCookiesPaleMoon = new JButton("...");

	/**
	 * CheckBox
	 */
	private JCheckBox cbCookieDatabase = new JCheckBox(Localization.getString("CookieDatabase"), false);

	/**
	 * Button
	 */
	private JButton btnDeleteAllCookies = new JButton(Localization.getString("DeleteAllCookiesInDatabase"));

	/**
	 * Label
	 */
	private JLabel lblDefaultUserAgent = new JLabel(Localization.getString("DefaultUserAgent"));

	/**
	 * TextField
	 */
	private JTextField txtDefaultUserAgent = new JTextField("", 80);

	/**
	 * Label
	 */
	private JLabel lblUserAgent = new JLabel(Localization.getString("UserAgent"));

	/**
	 * TextField
	 */
	private JTextField txtUserAgent = new JTextField("", 80);

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 * @param proxyManager Proxy Manager
	 * @param cookieManager Cookie Manager
	 */
	public ConnectionSettingsPanel(SettingsManager settingsManager, ProxyManager proxyManager, CookieManager cookieManager) {
		super(settingsManager);
		this.proxyManager = proxyManager;
		this.cookieManager = cookieManager;

		txtConnectionCount.setEditable(false);
		txtConnectionCountPerHost.setEditable(false);

		txtDefaultUserAgent.setEditable(false);
		txtDefaultUserAgent.setToolTipText(Localization.getString("DefaultUserAgentToolTip"));

		sldConnectionCount.setSnapToTicks(true);
		sldConnectionCount.setMajorTickSpacing(10);
		sldConnectionCount.setMinorTickSpacing(1);
		sldConnectionCount.setPaintTicks(true);
		sldConnectionCount.setPaintLabels(true);
		sldConnectionCount.setMinimum(0);
		sldConnectionCount.setMaximum(50);
		sldConnectionCount.addChangeListener(e -> {
			if (sldConnectionCount.getValue() == 0) {
				sldConnectionCount.setValue(1);
			} else {
				txtConnectionCount.setText(String.valueOf(sldConnectionCount.getValue()));
			}
		});

		sldConnectionCountPerHost.setSnapToTicks(true);
		sldConnectionCountPerHost.setMajorTickSpacing(10);
		sldConnectionCountPerHost.setMinorTickSpacing(1);
		sldConnectionCountPerHost.setPaintTicks(true);
		sldConnectionCountPerHost.setPaintLabels(true);
		sldConnectionCountPerHost.setMinimum(0);
		sldConnectionCountPerHost.setMaximum(50);
		sldConnectionCountPerHost.addChangeListener(e -> txtConnectionCountPerHost.setText(String.valueOf(sldConnectionCountPerHost.getValue())));

		txtConnectionCountPerHost.setToolTipText(Localization.getString("MaxConnectionCountToolTip"));
		sldConnectionCountPerHost.setToolTipText(Localization.getString("MaxConnectionCountToolTip"));

		buttonGroup.add(rbNoProxy);
		buttonGroup.add(rbHTTP);
		txtProxyPassword.setEchoChar('*');

		cmbCookies.addItem(BrowserCookiesMode.NO_COOKIES);
		cmbCookies.addItem(BrowserCookiesMode.BROWSER_IE);
		cmbCookies.addItem(BrowserCookiesMode.BROWSER_FIREFOX);
		cmbCookies.addItem(BrowserCookiesMode.BROWSER_OPERA);
		cmbCookies.addItem(BrowserCookiesMode.BROWSER_PALE_MOON);
		cmbCookies.addItem(BrowserCookiesMode.BROWSER_OPERA_NEW);
		cmbCookies.setRenderer(new BrowserCookiesModeComboBoxRenderer());
		cmbCookies.addItemListener(e -> {
			boolean bOpera = (cmbCookies.getSelectedIndex() == BrowserCookies.BROWSER_OPERA);
			boolean bOperaNew = (cmbCookies.getSelectedIndex() == BrowserCookies.BROWSER_OPERA_NEW);
			boolean bFirefox = (cmbCookies.getSelectedIndex() == BrowserCookies.BROWSER_FIREFOX);
			boolean bPaleMoon = (cmbCookies.getSelectedIndex() == BrowserCookies.BROWSER_PALE_MOON);
			lblCookiesOpera.setVisible(bOpera);
			cbCookiesOperaFixed.setVisible(bOpera);
			txtCookiesOpera.setVisible(bOpera);
			btnCookiesOpera.setVisible(bOpera);
			pnlCookiesOpera.setVisible(bOpera);
			lblCookiesOperaNew.setVisible(bOperaNew);
			cbCookiesOperaNewFixed.setVisible(bOperaNew);
			txtCookiesOperaNew.setVisible(bOperaNew);
			btnCookiesOperaNew.setVisible(bOperaNew);
			pnlCookiesOperaNew.setVisible(bOperaNew);
			lblCookiesFirefox.setVisible(bFirefox);
			cbCookiesFirefoxFixed.setVisible(bFirefox);
			txtCookiesFirefox.setVisible(bFirefox);
			btnCookiesFirefox.setVisible(bFirefox);
			pnlCookiesFirefox.setVisible(bFirefox);
			lblCookiesPaleMoon.setVisible(bPaleMoon);
			cbCookiesPaleMoonFixed.setVisible(bPaleMoon);
			txtCookiesPaleMoon.setVisible(bPaleMoon);
			btnCookiesPaleMoon.setVisible(bPaleMoon);
			pnlCookiesPaleMoon.setVisible(bPaleMoon);
		});

		pnlCookiesOpera.add(cbCookiesOperaFixed, BorderLayout.WEST);
		pnlCookiesOpera.add(txtCookiesOpera, BorderLayout.CENTER);
		pnlCookiesOpera.add(btnCookiesOpera, BorderLayout.EAST);
		pnlCookiesOpera.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		cbCookiesOperaFixed.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		txtCookiesOpera.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		btnCookiesOpera.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		txtCookiesOpera.setEditable(false);
		btnCookiesOpera.addActionListener(e -> cookiesOpera());
		cbCookiesOperaFixed.addItemListener(e -> btnCookiesOpera.setEnabled(cbCookiesOperaFixed.isSelected()));

		pnlCookiesOperaNew.add(cbCookiesOperaNewFixed, BorderLayout.WEST);
		pnlCookiesOperaNew.add(txtCookiesOperaNew, BorderLayout.CENTER);
		pnlCookiesOperaNew.add(btnCookiesOperaNew, BorderLayout.EAST);
		pnlCookiesOperaNew.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		cbCookiesOperaNewFixed.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		txtCookiesOperaNew.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		btnCookiesOperaNew.setToolTipText(Localization.getString("CookiesOperaToolTip"));
		txtCookiesOperaNew.setEditable(false);
		btnCookiesOperaNew.addActionListener(e -> cookiesOperaNew());
		cbCookiesOperaNewFixed.addItemListener(e -> btnCookiesOperaNew.setEnabled(cbCookiesOperaNewFixed.isSelected()));

		pnlCookiesFirefox.add(cbCookiesFirefoxFixed, BorderLayout.WEST);
		pnlCookiesFirefox.add(txtCookiesFirefox, BorderLayout.CENTER);
		pnlCookiesFirefox.add(btnCookiesFirefox, BorderLayout.EAST);
		pnlCookiesFirefox.setToolTipText(Localization.getString("CookiesFirefoxToolTip"));
		cbCookiesFirefoxFixed.setToolTipText(Localization.getString("CookiesFirefoxToolTip"));
		txtCookiesFirefox.setToolTipText(Localization.getString("CookiesFirefoxToolTip"));
		btnCookiesFirefox.setToolTipText(Localization.getString("CookiesFirefoxToolTip"));
		txtCookiesFirefox.setEditable(false);
		btnCookiesFirefox.addActionListener(e -> cookiesFirefox());
		cbCookiesFirefoxFixed.addItemListener(e -> btnCookiesFirefox.setEnabled(cbCookiesFirefoxFixed.isSelected()));

		pnlCookiesPaleMoon.add(cbCookiesPaleMoonFixed, BorderLayout.WEST);
		pnlCookiesPaleMoon.add(txtCookiesPaleMoon, BorderLayout.CENTER);
		pnlCookiesPaleMoon.add(btnCookiesPaleMoon, BorderLayout.EAST);
		pnlCookiesPaleMoon.setToolTipText(Localization.getString("CookiesPaleMoonToolTip"));
		cbCookiesPaleMoonFixed.setToolTipText(Localization.getString("CookiesPaleMoonToolTip"));
		txtCookiesPaleMoon.setToolTipText(Localization.getString("CookiesPaleMoonToolTip"));
		btnCookiesPaleMoon.setToolTipText(Localization.getString("CookiesPaleMoonToolTip"));
		txtCookiesPaleMoon.setEditable(false);
		btnCookiesPaleMoon.addActionListener(e -> cookiePaleMoon());
		cbCookiesPaleMoonFixed.addItemListener(e -> btnCookiesPaleMoon.setEnabled(cbCookiesPaleMoonFixed.isSelected()));

		btnDeleteAllCookies.addActionListener(e -> {
			int retval = JOptionPane.showConfirmDialog(this, Localization.getString("ReallyDeleteAllCookies"), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (retval == JOptionPane.NO_OPTION) {
				return;
			}
			cookieManager.deleteAllCookiesInDatabase();
		});

		rbNoProxy.addChangeListener(e -> proxyModeChanged());
		rbHTTP.addChangeListener(e -> proxyModeChanged());
		cbAuth.addChangeListener(e -> {
			lblProxyUser.setEnabled(cbAuth.isSelected());
			lblProxyPassword.setEnabled(cbAuth.isSelected());
			txtProxyUser.setEnabled(cbAuth.isSelected());
			txtProxyPassword.setEnabled(cbAuth.isSelected());
		});

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtCookiesFirefox);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtCookiesPaleMoon);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtCookiesOpera);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtCookiesOperaNew);

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtProxyName);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtProxyPort);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtProxyUser);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtConnectTimeout);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtSocketTimeout);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtConnectionRequestTimeout);

		GridBagConstraints gbc = new GridBagConstraints();

		int i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, rbNoProxy, this);
		gbc = gblt.getGBC(1, i, 4, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, rbHTTP, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblProxyName, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtProxyName, this);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblProxyPort, this);
		gbc = gblt.getGBC(3, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtProxyPort, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cbAuth, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblProxyUser, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtProxyUser, this);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblProxyPassword, this);
		gbc = gblt.getGBC(3, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtProxyPassword, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblConnectionCount, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, sldConnectionCount, this);
		gbc = gblt.getGBC(5, i, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtConnectionCount, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblConnectionCountPerHost, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, sldConnectionCountPerHost, this);
		gbc = gblt.getGBC(5, i, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtConnectionCountPerHost, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblConnectTimeout, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtConnectTimeout, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblSocketTimeout, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtSocketTimeout, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblConnectionRequestTimeout, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtConnectionRequestTimeout, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblCookies, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cmbCookies, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblCookiesOpera, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, pnlCookiesOpera, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblCookiesOperaNew, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, pnlCookiesOperaNew, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblCookiesFirefox, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, pnlCookiesFirefox, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblCookiesPaleMoon, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, pnlCookiesPaleMoon, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cbCookieDatabase, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, btnDeleteAllCookies, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblDefaultUserAgent, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtDefaultUserAgent, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblUserAgent, this);
		gbc = gblt.getGBC(1, i, 3, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtUserAgent, this);
	}

	@Override
	public void init() {
		Settings settings = settingsManager.getSettings();

		ConnectionSettings connectionSettings = settings.getConnectionSettings();

		txtConnectionCount.setText(String.valueOf(connectionSettings.getMaxConnections()));
		sldConnectionCount.setValue(connectionSettings.getMaxConnections());

		txtConnectionCountPerHost.setText(String.valueOf(connectionSettings.getMaxConnectionsPerHost()));
		sldConnectionCountPerHost.setValue(connectionSettings.getMaxConnectionsPerHost());

		txtConnectTimeout.setText(String.valueOf(connectionSettings.getConnectTimeout()));
		txtSocketTimeout.setText(String.valueOf(connectionSettings.getSocketTimeout()));
		txtConnectionRequestTimeout.setText(String.valueOf(connectionSettings.getConnectionRequestTimeout()));

		txtDefaultUserAgent.setText(settingsManager.getDefaultUserAgent());
		txtUserAgent.setText(connectionSettings.getUserAgent());

		txtProxyName.setText(proxyManager.getProxyname());
		txtProxyPort.setText(String.valueOf(proxyManager.getProxyport()));
		txtProxyUser.setText(proxyManager.getProxyuser());
		txtProxyPassword.setText(proxyManager.getProxypassword());

		ProxyMode mode = proxyManager.getMode();
		if (mode == ProxyMode.DIRECT_CONNECTION) {
			rbNoProxy.setSelected(true);
		} else if (mode == ProxyMode.PROXY) {
			rbHTTP.setSelected(true);
		}
		boolean b = proxyManager.isAuth();
		if (b && (mode != ProxyMode.DIRECT_CONNECTION)) {
			cbAuth.setSelected(true);
		}

		lblProxyName.setEnabled(!rbNoProxy.isSelected());
		lblProxyPort.setEnabled(!rbNoProxy.isSelected());
		txtProxyName.setEnabled(!rbNoProxy.isSelected());
		txtProxyPort.setEnabled(!rbNoProxy.isSelected());
		cbAuth.setEnabled(!rbNoProxy.isSelected());

		lblProxyUser.setEnabled(cbAuth.isSelected());
		lblProxyPassword.setEnabled(cbAuth.isSelected());
		txtProxyUser.setEnabled(cbAuth.isSelected());
		txtProxyPassword.setEnabled(cbAuth.isSelected());

		BrowserCookiesMode browserCookiesMode = connectionSettings.getBrowserCookiesMode();
		cmbCookies.setSelectedItem(browserCookiesMode);

		boolean bOpera = browserCookiesMode == BrowserCookiesMode.BROWSER_OPERA;
		boolean bOperaNew = browserCookiesMode == BrowserCookiesMode.BROWSER_OPERA_NEW;
		boolean bFirefox = browserCookiesMode == BrowserCookiesMode.BROWSER_FIREFOX;
		boolean bPaleMoon = browserCookiesMode == BrowserCookiesMode.BROWSER_PALE_MOON;

		BrowserCookiesSetting operaBrowserCookiesSetting = connectionSettings.getBrowserCookiesOpera();
		lblCookiesOpera.setVisible(bOpera);
		cbCookiesOperaFixed.setVisible(bOpera);
		txtCookiesOpera.setVisible(bOpera);
		btnCookiesOpera.setVisible(bOpera);
		btnCookiesOpera.setEnabled(cbCookiesOperaFixed.isSelected());
		cbCookiesOperaFixed.setSelected(operaBrowserCookiesSetting.isCookieFileFixed());
		txtCookiesOpera.setText(cookieManager.getCookieFileForOpera(false));

		BrowserCookiesSetting operaNewBrowserCookiesSetting = connectionSettings.getBrowserCookiesOperaNew();
		lblCookiesOperaNew.setVisible(bOperaNew);
		cbCookiesOperaNewFixed.setVisible(bOperaNew);
		txtCookiesOperaNew.setVisible(bOperaNew);
		btnCookiesOperaNew.setVisible(bOperaNew);
		btnCookiesOperaNew.setEnabled(cbCookiesOperaNewFixed.isSelected());
		cbCookiesOperaNewFixed.setSelected(operaNewBrowserCookiesSetting.isCookieFileFixed());
		txtCookiesOperaNew.setText(cookieManager.getCookieFileForOperaNew());

		BrowserCookiesSetting firefoxBrowserCookiesSetting = connectionSettings.getBrowserCookiesFirefox();
		lblCookiesFirefox.setVisible(bFirefox);
		cbCookiesFirefoxFixed.setVisible(bFirefox);
		txtCookiesFirefox.setVisible(bFirefox);
		btnCookiesFirefox.setVisible(bFirefox);
		btnCookiesFirefox.setEnabled(cbCookiesFirefoxFixed.isSelected());
		cbCookiesFirefoxFixed.setSelected(firefoxBrowserCookiesSetting.isCookieFileFixed());
		txtCookiesFirefox.setText(cookieManager.getCookieFileForFirefox());

		BrowserCookiesSetting palemoonBrowserCookiesSetting = connectionSettings.getBrowserCookiesPaleMoon();
		lblCookiesPaleMoon.setVisible(bPaleMoon);
		cbCookiesPaleMoonFixed.setVisible(bPaleMoon);
		txtCookiesPaleMoon.setVisible(bPaleMoon);
		btnCookiesPaleMoon.setVisible(bPaleMoon);
		btnCookiesPaleMoon.setEnabled(cbCookiesPaleMoonFixed.isSelected());
		cbCookiesPaleMoonFixed.setSelected(palemoonBrowserCookiesSetting.isCookieFileFixed());
		txtCookiesPaleMoon.setText(cookieManager.getCookieFileForPaleMoon());

		cbCookieDatabase.setSelected(connectionSettings.isCookieDatabase());
	}

	@Override
	public void applySettings() {
		Settings settings = settingsManager.getSettings();

		ConnectionSettings connectionSettings = settings.getConnectionSettings();

		connectionSettings.setMaxConnections(sldConnectionCount.getValue());
		connectionSettings.setMaxConnectionsPerHost(sldConnectionCountPerHost.getValue());

		int connectTimeout = parseTimeoutSetting(txtConnectTimeout, connectionSettings.getConnectTimeout());
		int socketTimeout = parseTimeoutSetting(txtSocketTimeout, connectionSettings.getSocketTimeout());
		int connectionRequestTimeout = parseTimeoutSetting(txtConnectionRequestTimeout, connectionSettings.getConnectionRequestTimeout());
		connectionSettings.setConnectTimeout(connectTimeout);
		connectionSettings.setSocketTimeout(socketTimeout);
		connectionSettings.setConnectionRequestTimeout(connectionRequestTimeout);

		connectionSettings.setUserAgent(txtUserAgent.getText());

		proxyManager.setAuth(cbAuth.isSelected());
		if (rbNoProxy.isSelected()) {
			proxyManager.setMode(ProxyMode.DIRECT_CONNECTION);
		} else if (rbHTTP.isSelected()) {
			proxyManager.setMode(ProxyMode.PROXY);
		}
		proxyManager.setProxyname(txtProxyName.getText());
		try {
			proxyManager.setProxyport(Integer.parseInt(txtProxyPort.getText()));
		} catch (NumberFormatException nfe) {
			txtProxyPort.setText(String.valueOf(proxyManager.getProxyport()));
		}
		proxyManager.setProxyuser(txtProxyUser.getText());
		proxyManager.setProxypassword(String.valueOf(txtProxyPassword.getPassword()));
		proxyManager.writeToSettings();

		connectionSettings.setBrowserCookiesMode((BrowserCookiesMode)cmbCookies.getSelectedItem());

		BrowserCookiesSetting operaBrowserCookiesSetting = connectionSettings.getBrowserCookiesOpera();
		operaBrowserCookiesSetting.setCookieFileFixed(cbCookiesOperaFixed.isSelected());
		operaBrowserCookiesSetting.setCookieFile(txtCookiesOpera.getText());

		BrowserCookiesSetting operaNewBrowserCookiesSetting = connectionSettings.getBrowserCookiesOperaNew();
		operaNewBrowserCookiesSetting.setCookieFileFixed(cbCookiesOperaNewFixed.isSelected());
		operaNewBrowserCookiesSetting.setCookieFile(txtCookiesOperaNew.getText());

		BrowserCookiesSetting firefoxBrowserCookiesSetting = connectionSettings.getBrowserCookiesFirefox();
		firefoxBrowserCookiesSetting.setCookieFileFixed(cbCookiesFirefoxFixed.isSelected());
		firefoxBrowserCookiesSetting.setCookieFile(txtCookiesFirefox.getText());

		BrowserCookiesSetting palemoonBrowserCookiesSetting = connectionSettings.getBrowserCookiesPaleMoon();
		palemoonBrowserCookiesSetting.setCookieFileFixed(cbCookiesPaleMoonFixed.isSelected());
		palemoonBrowserCookiesSetting.setCookieFile(txtCookiesPaleMoon.getText());

		connectionSettings.setCookieDatabase(cbCookieDatabase.isSelected());
	}

	private void proxyModeChanged() {
		boolean b1 = rbNoProxy.isSelected();
		boolean b2 = rbHTTP.isSelected();
		boolean b = false;
		if (b2) {
			b = true;
		}
		lblProxyName.setEnabled(b);
		lblProxyPort.setEnabled(b);
		txtProxyName.setEnabled(b);
		txtProxyPort.setEnabled(b);
		cbAuth.setEnabled(!(b1));
		if (b1) {
			cbAuth.setSelected(false);
		}
	}

	private int parseTimeoutSetting(JTextField textField, int defaultValue) {
		try {
			int val = Integer.parseInt(textField.getText());
			if (val <= 1000) {
				textField.setText(String.valueOf(defaultValue));
				return defaultValue;
			} else {
				return val;
			}
		} catch (NumberFormatException nfe) {
			logger.error(nfe.getMessage(), nfe);
			textField.setText(String.valueOf(defaultValue));
			return defaultValue;
		}
	}

	private void cookiesOpera() {
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.getName().equals("cookies4.dat") || f.isDirectory();
			}

			@Override
			public String getDescription() {
				return "cookies4.dat";
			}
		};
		File file = FileDialogUtil.showFileOpenDialog(this, txtCookiesOpera.getText(), filter);
		if (file != null) {
			String strFile = file.getAbsolutePath();
			txtCookiesOpera.setText(strFile);
		}
	}

	private void cookiesOperaNew() {
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.getName().equals("Cookies") || f.isDirectory();
			}

			@Override
			public String getDescription() {
				return "Cookies";
			}
		};
		File file = FileDialogUtil.showFileOpenDialog(this, txtCookiesOperaNew.getText(), filter);
		if (file != null) {
			String strFile = file.getAbsolutePath();
			txtCookiesOperaNew.setText(strFile);
		}
	}

	private void cookiesFirefox() {
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.getName().equals("cookies.txt") || f.getName().equals("cookies.sqlite") || f.isDirectory();
			}

			@Override
			public String getDescription() {
				return "cookies.txt, cookies.sqlite";
			}
		};
		File file = FileDialogUtil.showFileOpenDialog(this, txtCookiesFirefox.getText(), filter);
		if (file != null) {
			String strFile = file.getAbsolutePath();
			txtCookiesFirefox.setText(strFile);
		}
	}

	private void cookiePaleMoon() {
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.getName().equals("cookies.sqlite") || f.isDirectory();
			}

			@Override
			public String getDescription() {
				return "cookies.sqlite";
			}
		};
		File file = FileDialogUtil.showFileOpenDialog(this, txtCookiesPaleMoon.getText(), filter);
		if (file != null) {
			String strFile = file.getAbsolutePath();
			txtCookiesPaleMoon.setText(strFile);
		}
	}
}

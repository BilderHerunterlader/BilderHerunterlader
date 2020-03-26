package ch.supertomcat.bh.gui.queue;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import ch.supertomcat.bh.clipboard.ClipboardObserver;
import ch.supertomcat.bh.gui.adder.AdderPanel;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.pic.URLList;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.bh.settings.ProxyManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;

/**
 * Dialog to add new links to queue by a TextArea
 */
public class DownloadAddDialog extends JDialog {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -546191770179553756L;

	/**
	 * Thread URL set, when URL's are added over the download add dialog.
	 * Some host classes check over this value, how the URL was added.
	 */
	public static final String DOWNLOAD_ADD_DIALOG_THREAD_URL = "DownloadAddDialog";

	/**
	 * Textarea
	 */
	private JTextArea txtLinks = new JTextArea(10, 85);

	/**
	 * Label
	 */
	private JLabel lblDescription = new JLabel(Localization.getString("OneLinkPerLine"));

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * Button
	 */
	private JButton btnOK = new JButton(Localization.getString("OK"));

	/**
	 * Button
	 */
	private JButton btnCancel = new JButton(Localization.getString("Cancel"));

	/**
	 * Log Manager
	 */
	private final LogManager logManager;

	/**
	 * Queue Manager
	 */
	private final QueueManager queueManager;

	/**
	 * Keyword Manager
	 */
	private final KeywordManager keywordManager;

	/**
	 * Proxy Manager
	 */
	private final ProxyManager proxyManager;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Host Manager
	 */
	private final HostManager hostManager;

	/**
	 * Clipboard Observer
	 */
	private final ClipboardObserver clipboardObserver;

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param logManager Log Manager
	 * @param queueManager Queue Manager
	 * @param keywordManager Keyword Manager
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 * @param hostManager Host Manager
	 * @param clipboardObserver Clipboard Observer
	 */
	public DownloadAddDialog(JFrame owner, LogManager logManager, QueueManager queueManager, KeywordManager keywordManager, ProxyManager proxyManager, SettingsManager settingsManager,
			HostManager hostManager, ClipboardObserver clipboardObserver) {
		super(owner);
		this.logManager = logManager;
		this.queueManager = queueManager;
		this.keywordManager = keywordManager;
		this.proxyManager = proxyManager;
		this.settingsManager = settingsManager;
		this.hostManager = hostManager;
		this.clipboardObserver = clipboardObserver;

		setLayout(new BorderLayout());
		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);

		setTitle(Localization.getString("DownloadAdd"));

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtLinks);

		btnOK.addActionListener(e -> okPressed());
		btnCancel.addActionListener(e -> cancelPressed());

		add(lblDescription, BorderLayout.NORTH);
		add(new JScrollPane(txtLinks), BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);
		setModal(true);
		pack();
		setLocationRelativeTo(owner);
		txtLinks.requestFocusInWindow();

		// Enter und Escape (before setVisible(true)!)
		ActionMap am = getRootPane().getActionMap();
		InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		Object windowCloseKey = new Object();
		Object windowOkKey = new Object();
		KeyStroke windowCloseStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		Action windowCloseAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				cancelPressed();
			}
		};
		KeyStroke windowOkStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		Action windowOkAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed();
			}
		};
		im.put(windowCloseStroke, windowCloseKey);
		am.put(windowCloseKey, windowCloseAction);
		im.put(windowOkStroke, windowOkKey);
		am.put(windowOkKey, windowOkAction);
	}

	/**
	 * Ok-Button-Pressed-Method
	 */
	public void okPressed() {
		this.dispose();
		String links = txtLinks.getText();
		if (!links.isEmpty()) {

			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					String strArr[];
					if (links.contains("\n") == false) {
						strArr = new String[1];
						strArr[0] = links;
					} else {
						strArr = links.split("\n");
					}
					ArrayList<URL> urls = new ArrayList<>();
					for (int i = 0; i < strArr.length; i++) {
						URL urlToAdd = new URL(strArr[i]);
						urlToAdd.setThreadURL(DOWNLOAD_ADD_DIALOG_THREAD_URL);
						urls.add(urlToAdd);
					}

					AdderPanel adderpnl = new AdderPanel(getOwner(), new URLList(Localization.getString("Unkown") + ": " + Localization.getString("Title"), Localization.getString("Unkown") + ": "
							+ Localization.getString("Referrer"), urls), logManager, queueManager, keywordManager, proxyManager, settingsManager, hostManager, clipboardObserver);
					adderpnl.init();
					adderpnl = null;
				}
			});
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}
	}

	/**
	 * Cancel-Button-Pressed-Method
	 */
	private void cancelPressed() {
		this.dispose();
	}
}

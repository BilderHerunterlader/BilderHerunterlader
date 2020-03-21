package ch.supertomcat.bh.gui.queue;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

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
import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.importexport.ImportURL;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.queue.QueueManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;

/**
 * Dialog to add URL's to pages from which the links will be extracted by a TextArea
 */
public class ParsePagesDialog extends JDialog {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -546191770179553756L;

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
	 * URL Importer
	 */
	private final ImportURL urlImporter;

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param mainWindowAccess Main Window Access
	 * @param logManager Log Manager
	 * @param queueManager Queue Manager
	 * @param clipboardObserver Clipboard Observer
	 */
	public ParsePagesDialog(JFrame owner, MainWindowAccess mainWindowAccess, LogManager logManager, QueueManager queueManager, ClipboardObserver clipboardObserver) {
		super(owner);
		this.urlImporter = new ImportURL(owner, mainWindowAccess, logManager, queueManager, clipboardObserver);

		setLayout(new BorderLayout());
		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);

		setTitle(Localization.getString("ParsePages"));

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
		final String links = txtLinks.getText();
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

					for (int i = 0; i < strArr.length; i++) {
						urlImporter.importURL(strArr[i], strArr[i], false);
					}
				}
			});
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}
	}

	/**
	 * Cancel-Button-Pressed-Method
	 */
	public void cancelPressed() {
		this.dispose();
	}
}

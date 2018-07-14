package ch.supertomcat.bh.gui.queue;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import ch.supertomcat.bh.importexport.Import;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.copyandpaste.JTextComponentCopyAndPaste;

/**
 * Dialog to add URL's to pages from which the links will be extracted by a TextArea
 */
public class ParsePagesDialog extends JDialog implements ActionListener {
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
	 * Constructor
	 * 
	 * @param owner Owner
	 */
	public ParsePagesDialog(JFrame owner) {
		super(owner);

		setLayout(new BorderLayout());
		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);

		setTitle(Localization.getString("ParsePages"));

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtLinks);

		btnOK.addActionListener(this);
		btnCancel.addActionListener(this);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnOK) {
			okPressed();
		} else if (e.getSource() == btnCancel) {
			cancelPressed();
		}
	}

	/**
	 * Ok-Button-Pressed-Method
	 */
	public void okPressed() {
		this.dispose();
		final String links;
		if ((links = txtLinks.getText()).length() > 0) {

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
						// ImportHTML.importHTML(strArr[i], strArr[i], false);
						Import.importURL(strArr[i], strArr[i], false);
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

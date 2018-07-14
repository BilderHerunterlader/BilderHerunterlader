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
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.copyandpaste.JTextComponentCopyAndPaste;

/**
 * Dialog for renaming directories
 */
public class PathRenameDialog extends JDialog implements ActionListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1704231042429412312L;

	/**
	 * Panel
	 */
	private JPanel pnlRename = new JPanel();

	/**
	 * Panel
	 */
	private JPanel pnlPath = new JPanel();

	/**
	 * Label
	 */
	private JLabel lblPath = new JLabel(Localization.getString("Path"));

	/**
	 * TextField
	 */
	private JTextField txtPath = new JTextField("", 80);

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
	 * Flag if cancel was pressed
	 */
	private boolean canceled = true;

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param defaultvalue Default-Value
	 */
	public PathRenameDialog(JFrame owner, String defaultvalue) {
		super(owner);

		setLayout(new BorderLayout());
		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);

		pnlPath.add(lblPath);
		pnlPath.add(txtPath);

		pnlRename.add(pnlPath);

		txtPath.setText(defaultvalue);

		setTitle(Localization.getString("PathInput"));

		btnOK.addActionListener(this);
		btnCancel.addActionListener(this);

		add(pnlRename, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);
		setModal(true);
		pack();
		setLocationRelativeTo(owner);
		txtPath.requestFocusInWindow();

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtPath);

		// Enter und Escape (before setVisible(true)!)
		ActionMap am = getRootPane().getActionMap();
		InputMap im = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
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

		setVisible(true);
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
			canceled = true;
			this.dispose();
		}
	}

	/**
	 * Ok-Button-Pressed-Method
	 */
	public void okPressed() {
		canceled = false;
		if (!canceled) {
			this.dispose();
		}
	}

	/**
	 * Cancel-Button-Pressed-Method
	 */
	public void cancelPressed() {
		canceled = true;
		this.dispose();
	}

	/**
	 * Get-Methode
	 * 
	 * @return Value
	 */
	private String getValue() {
		return txtPath.getText();
	}

	/**
	 * Get-Methode
	 * 
	 * @return True if canceled
	 */
	private boolean isCanceled() {
		return canceled;
	}

	/**
	 * Show dialog
	 * 
	 * @param owner Owner
	 * @param defaultvalue Default-Value
	 * @return Value
	 */
	public static String showPathRenameDialog(JFrame owner, String defaultvalue) {
		PathRenameDialog frd = new PathRenameDialog(owner, defaultvalue);
		if (frd.isCanceled()) {
			return null;
		}
		String retval = frd.getValue();
		return retval;
	}
}

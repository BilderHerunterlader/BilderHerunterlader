package ch.supertomcat.bh.gui.update;

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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Dialog which displays the changelog
 */
public class UpdateChangesDialog extends JDialog implements ActionListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -227661013031476028L;

	/**
	 * TextPane
	 */
	private JTextArea tp = new JTextArea(20, 80);

	/**
	 * Scrollpane
	 */
	private JScrollPane sp;

	/**
	 * Button
	 */
	private JButton btnOK = new JButton(Localization.getString("OK"));

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param message Message
	 * @param title Title
	 */
	public UpdateChangesDialog(JDialog owner, String message, String title) {
		super(owner);
		setTitle(title);
		setLayout(new BorderLayout());
		pnlButtons.add(btnOK);
		sp = new JScrollPane(tp);
		tp.setText(message);
		tp.setEditable(false);
		tp.setFont(UIManager.getFont("Label.font"));

		add(sp, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);
		btnOK.addActionListener(this);
		tp.setCaretPosition(0);
		pack();
		setLocationRelativeTo(owner);

		// Enter und Escape (before setVisible(true)!)
		ActionMap am = getRootPane().getActionMap();
		InputMap im = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		Object windowOkKey = new Object();
		KeyStroke windowOkStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		Action windowOkAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				btnOK.doClick();
			}
		};
		im.put(windowOkStroke, windowOkKey);
		am.put(windowOkKey, windowOkAction);

		setModal(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnOK) {
			this.dispose();
		}
	}
}

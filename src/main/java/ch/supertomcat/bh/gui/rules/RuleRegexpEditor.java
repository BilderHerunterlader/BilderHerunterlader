package ch.supertomcat.bh.gui.rules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import ch.supertomcat.bh.rules.RuleRegExp;
import ch.supertomcat.supertomcattools.guitools.GridBagLayoutTool;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.copyandpaste.JTextComponentCopyAndPaste;

/**
 * Rule-Regexp-Editor-Dialog
 */
public class RuleRegexpEditor extends JDialog implements ActionListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Label
	 */
	private JLabel lblSearch = new JLabel(Localization.getString("SearchPattern"));

	/**
	 * TextField
	 */
	private JTextField txtSearch = new JTextField(80);

	/**
	 * Label
	 */
	private JLabel lblReplace = new JLabel(Localization.getString("ReplacePattern"));

	/**
	 * TextField
	 */
	private JTextField txtReplace = new JTextField(80);

	/**
	 * Panel
	 */
	private JPanel pnlMain = new JPanel();

	/**
	 * Label
	 */
	private JLabel lblError = new JLabel(Localization.getString("Error") + ":");

	/**
	 * TextPane
	 */
	private JTextArea txtError = new JTextArea(4, 40);

	/**
	 * Label
	 */
	private JLabel lblNote = new JLabel(Localization.getString("Note") + ":");

	/**
	 * TextPane
	 */
	private JTextArea txtNote = new JTextArea(8, 80);

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
	 * RuleRegExp
	 */
	private RuleRegExp rre = null;

	/**
	 * GridBagLayout
	 */
	private GridBagLayout gbl = new GridBagLayout();

	/**
	 * GridBagLayoutTool
	 */
	private GridBagLayoutTool gblt = new GridBagLayoutTool(5, 10, 5, 5);

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param rre RuleRegExp
	 */
	public RuleRegexpEditor(JDialog owner, RuleRegExp rre) {
		this(owner, rre, false);
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param rre RuleRegExp
	 * @param bSearchOnly Search Only
	 */
	public RuleRegexpEditor(JDialog owner, RuleRegExp rre, boolean bSearchOnly) {
		super(owner);
		this.rre = rre;
		setTitle(Localization.getString("RegExpEditor"));
		setLayout(new BorderLayout());
		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);
		btnOK.addActionListener(this);
		btnCancel.addActionListener(this);

		txtSearch.setText(this.rre.getSearch());
		txtReplace.setText(this.rre.getReplace());

		Font textAreaFont = new Font("Monospaced", Font.PLAIN, txtSearch.getFont().getSize());
		txtNote.setText(Localization.getString("RuleReplaceVariables"));
		txtError.setEditable(false);
		lblError.setBackground(Color.WHITE);
		txtError.setBackground(Color.WHITE);
		lblError.setForeground(Color.RED);
		txtError.setForeground(Color.RED);
		txtError.setFont(textAreaFont);
		txtNote.setEditable(false);
		txtNote.setFont(textAreaFont);

		if (bSearchOnly) {
			lblNote.setVisible(false);
			txtNote.setVisible(false);
			lblReplace.setVisible(false);
			txtReplace.setVisible(false);
		}

		pnlMain.setLayout(gbl);

		GridBagConstraints gbc = new GridBagConstraints();

		int i = 0;
		gbc = gblt.getGBC(0, i, 2, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gbl, gbc, lblNote, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.0, 0.5);
		GridBagLayoutTool.addItemToPanel(gbl, gbc, txtNote, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.0, 0.0);
		GridBagLayoutTool.addItemToPanel(gbl, gbc, lblError, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.0, 0.5);
		GridBagLayoutTool.addItemToPanel(gbl, gbc, txtError, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.2, 0.0);
		GridBagLayoutTool.addItemToPanel(gbl, gbc, lblSearch, pnlMain);
		gbc = gblt.getGBC(1, i, 1, 1, 0.8, 0.0);
		GridBagLayoutTool.addItemToPanel(gbl, gbc, txtSearch, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.2, 0.0);
		GridBagLayoutTool.addItemToPanel(gbl, gbc, lblReplace, pnlMain);
		gbc = gblt.getGBC(1, i, 1, 1, 0.8, 0.0);
		GridBagLayoutTool.addItemToPanel(gbl, gbc, txtReplace, pnlMain);

		add(pnlMain, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtReplace);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtSearch);

		setModal(true);
		pack();
		setLocationRelativeTo(owner);

		txtError.setVisible(false);
		lblError.setVisible(false);

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
				btnCancel.doClick();
			}
		};
		KeyStroke windowOkStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		Action windowOkAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				btnOK.doClick();
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
			try {
				this.rre.setSearch(txtSearch.getText());
			} catch (PatternSyntaxException pse) {
				txtError.setText(pse.getLocalizedMessage());
				txtError.setVisible(true);
				lblError.setVisible(true);
				return;
			}
			this.rre.setReplace(txtReplace.getText());
			canceled = false;
			this.dispose();
		} else if (e.getSource() == btnCancel) {
			this.dispose();
		}
	}

	/**
	 * Get-Method
	 * 
	 * @return True if canceled
	 */
	public boolean getCanceled() {
		return this.canceled;
	}
}

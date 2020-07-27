package ch.supertomcat.bh.gui.rules.editor.base;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;
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

import ch.supertomcat.bh.rules.xml.RuleRegex;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;

/**
 * Rule-Regexp-Editor-Dialog
 */
public class RuleRegexpEditor extends JDialog {
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
	private JLabel lblReplacement = new JLabel(Localization.getString("ReplacePattern"));

	/**
	 * TextField
	 */
	private JTextField txtReplacement = new JTextField(80);

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
	private boolean canceled = false;

	/**
	 * GridBagLayout
	 */
	private GridBagLayout gbl = new GridBagLayout();

	/**
	 * GridBagLayoutUtil
	 */
	private GridBagLayoutUtil gblt = new GridBagLayoutUtil(5, 10, 5, 5);

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 */
	public RuleRegexpEditor(JDialog owner) {
		this(owner, "", "", false);
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param bSearchOnly Search Only Flag
	 */
	public RuleRegexpEditor(JDialog owner, boolean bSearchOnly) {
		this(owner, "", "", bSearchOnly);
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param ruleRegex RuleRegex
	 */
	public RuleRegexpEditor(JDialog owner, RuleRegex ruleRegex) {
		this(owner, ruleRegex, false);
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param ruleRegex RuleRegex
	 * @param bSearchOnly Search Only Flag
	 */
	public RuleRegexpEditor(JDialog owner, RuleRegex ruleRegex, boolean bSearchOnly) {
		this(owner, ruleRegex.getPattern(), ruleRegex.getReplacement(), bSearchOnly);
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param search Search
	 */
	public RuleRegexpEditor(JDialog owner, String search) {
		this(owner, search, "", true);
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param search Search
	 * @param replacement Replacement
	 */
	public RuleRegexpEditor(JDialog owner, String search, String replacement) {
		this(owner, search, replacement, false);
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param search Search
	 * @param replacement Replacement
	 * @param bSearchOnly Search Only Flag
	 */
	public RuleRegexpEditor(JDialog owner, String search, String replacement, boolean bSearchOnly) {
		super(owner);
		setTitle(Localization.getString("RegExpEditor"));
		setLayout(new BorderLayout());
		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);
		btnOK.addActionListener(e -> actionOK());
		btnCancel.addActionListener(e -> actionCancel());

		txtSearch.setText(search);
		txtReplacement.setText(replacement);

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
			lblReplacement.setVisible(false);
			txtReplacement.setVisible(false);
		}

		pnlMain.setLayout(gbl);

		GridBagConstraints gbc = new GridBagConstraints();

		int i = 0;
		gbc = gblt.getGBC(0, i, 2, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblNote, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.0, 0.5);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtNote, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblError, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.0, 0.5);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtError, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.2, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblSearch, pnlMain);
		gbc = gblt.getGBC(1, i, 1, 1, 0.8, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtSearch, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.2, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblReplacement, pnlMain);
		gbc = gblt.getGBC(1, i, 1, 1, 0.8, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtReplacement, pnlMain);

		add(pnlMain, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtReplacement);
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

	private void actionOK() {
		try {
			/*
			 * Compile pattern to make sure it's a valid regex
			 */
			Pattern.compile(txtSearch.getText());
		} catch (PatternSyntaxException pse) {
			txtError.setText(pse.getLocalizedMessage());
			txtError.setVisible(true);
			lblError.setVisible(true);
			return;
		}

		canceled = false;
		dispose();
	}

	private void actionCancel() {
		canceled = true;
		dispose();
	}

	/**
	 * Returns the canceled
	 * 
	 * @return canceled
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Returns the search
	 * 
	 * @return search
	 */
	public String getSearch() {
		return txtSearch.getText();
	}

	/**
	 * Returns the replace
	 * 
	 * @return replace
	 */
	public String getReplacement() {
		return txtReplacement.getText();
	}
}

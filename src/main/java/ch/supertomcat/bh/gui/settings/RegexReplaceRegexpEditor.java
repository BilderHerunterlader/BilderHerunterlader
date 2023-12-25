package ch.supertomcat.bh.gui.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
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

import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;
import ch.supertomcat.supertomcatutils.regex.RegexReplace;

/**
 * Rule-Regexp-Editor-Dialog
 */
public class RegexReplaceRegexpEditor extends JDialog implements ActionListener {
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
	private RegexReplace rrre = null;

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
	 * @param rrre Regex Replace
	 */
	public RegexReplaceRegexpEditor(Window owner, RegexReplace rrre) {
		super(owner);
		this.rrre = rrre;
		init(owner);
	}

	private void init(Component owner) {
		setTitle(Localization.getString("RegExpEditor"));
		setLayout(new BorderLayout());
		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);
		btnOK.addActionListener(this);
		btnCancel.addActionListener(this);

		txtSearch.setText(this.rrre.getSearch());
		txtReplace.setText(this.rrre.getReplace());
		txtError.setEditable(false);
		lblError.setBackground(Color.WHITE);
		txtError.setBackground(Color.WHITE);
		lblError.setForeground(Color.RED);
		txtError.setForeground(Color.RED);
		txtError.setFont(new Font("Monospaced", Font.PLAIN, txtSearch.getFont().getSize()));

		pnlMain.setLayout(gbl);

		GridBagConstraints gbc;

		int i = 0;
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
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblReplace, pnlMain);
		gbc = gblt.getGBC(1, i, 1, 1, 0.8, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtReplace, pnlMain);

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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnOK) {
			try {
				this.rrre.setSearch(txtSearch.getText());
			} catch (PatternSyntaxException pse) {
				txtError.setText(pse.getLocalizedMessage());
				txtError.setVisible(true);
				lblError.setVisible(true);
				return;
			}
			this.rrre.setReplace(txtReplace.getText());
			canceled = false;
			this.dispose();
		} else if (e.getSource() == btnCancel) {
			canceled = true;
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

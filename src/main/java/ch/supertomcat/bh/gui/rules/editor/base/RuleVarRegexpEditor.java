package ch.supertomcat.bh.gui.rules.editor.base;

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
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import ch.supertomcat.bh.gui.rules.editor.urlpipe.varregex.RulePipelineVarRuleRegexPanel;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Rule-Var-Regexp-Editor-Dialog
 * TODO Probably not needed
 */
public class RuleVarRegexpEditor extends JDialog {
	private static final long serialVersionUID = 1L;

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
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 * @param varRuleRegexPanel Var Rule Regex Panel
	 */
	public RuleVarRegexpEditor(JDialog owner, SettingsManager settingsManager, RulePipelineVarRuleRegexPanel varRuleRegexPanel) {
		super(owner);
		setTitle(Localization.getString("VarRegExpEditor"));
		setLayout(new BorderLayout());
		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);
		btnOK.addActionListener(e -> actionOK());
		btnCancel.addActionListener(e -> actionCancel());

		add(varRuleRegexPanel, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);

		setModal(true);
		pack();
		setLocationRelativeTo(owner);

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
}

package ch.supertomcat.bh.gui.rules;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.RuleMode;
import ch.supertomcat.bh.rules.RuleURLMode;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;

/**
 * Rule-Test-Dialog
 */
public class RuleTest extends JDialog implements ActionListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Label
	 */
	private JLabel lblMessage = new JLabel(Localization.getString("Message"));

	/**
	 * Label
	 */
	private JLabel lblContainer = new JLabel(Localization.getString("ContainerURL"));

	/**
	 * Label
	 */
	private JLabel lblThumbnail = new JLabel(Localization.getString("ThumbnailURL"));

	/**
	 * Label
	 */
	private JLabel lblResultURL = new JLabel(Localization.getString("URL"));

	/**
	 * Label
	 */
	private JLabel lblResultFilename = new JLabel(Localization.getString("Filename"));

	/**
	 * Label
	 */
	private JLabel lblResultPageSourceCode = new JLabel(Localization.getString("filenameContainerPageSourcecode"));

	/**
	 * TextField
	 */
	private JTextField txtMessage = new JTextField(80);

	/**
	 * TextField
	 */
	private JTextField txtContainer = new JTextField(80);

	/**
	 * TextField
	 */
	private JTextField txtThumbnail = new JTextField(80);

	/**
	 * TextField
	 */
	private JTextField txtResultURL = new JTextField(80);

	/**
	 * TextField
	 */
	private JTextField txtResultFilename = new JTextField(80);

	/**
	 * TextField
	 */
	private JTextArea txtResultPageSourceCode = new JTextArea(15, 80);

	/**
	 * Button
	 */
	private JButton btnTest = new JButton("Test", Icons.getTangoIcon("apps/utilities-system-monitor.png", 16));

	/**
	 * Button
	 */
	private JButton btnClose = new JButton("Close", Icons.getTangoIcon("emblems/emblem-unreadable.png", 16));

	/**
	 * Panel
	 */
	private JPanel pnlMain = new JPanel();

	/**
	 * Rule
	 */
	private Rule rule = null;

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

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
	 * @param rule Rule
	 * @param owner Owner
	 */
	public RuleTest(Rule rule, JDialog owner) {
		super(owner);
		setTitle(Localization.getString("RuleTest"));
		setLayout(new BorderLayout());
		this.rule = rule;
		pnlButtons.add(btnTest);
		pnlButtons.add(btnClose);
		add(pnlButtons, BorderLayout.SOUTH);

		JScrollPane scrollpane = new JScrollPane(txtResultPageSourceCode);

		txtMessage.setEditable(false);

		pnlMain.setLayout(gbl);

		GridBagConstraints gbc = new GridBagConstraints();

		int i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.1, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblMessage, pnlMain);
		gbc = gblt.getGBC(1, i, 1, 1, 0.9, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtMessage, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.1, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblContainer, pnlMain);
		gbc = gblt.getGBC(1, i, 1, 1, 0.9, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtContainer, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.1, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblThumbnail, pnlMain);
		gbc = gblt.getGBC(1, i, 1, 1, 0.9, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtThumbnail, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.1, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblResultURL, pnlMain);
		gbc = gblt.getGBC(1, i, 1, 1, 0.9, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtResultURL, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.1, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblResultFilename, pnlMain);
		gbc = gblt.getGBC(1, i, 1, 1, 0.9, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtResultFilename, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.1, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblResultPageSourceCode, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.9, 0.5);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, scrollpane, pnlMain);

		add(pnlMain, BorderLayout.CENTER);

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtContainer);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtThumbnail);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtResultURL);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtResultFilename);

		btnTest.addActionListener(this);
		btnClose.addActionListener(this);
		pack();
		setLocationRelativeTo(owner);
		setModal(true);
		txtContainer.requestFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnClose) {
			this.dispose();
		} else if (e.getSource() == btnTest) {
			txtMessage.setText("");
			if (rule.getPipelines().size() > 0 && rule.getPipelines().get(0).getMode() == RuleMode.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL
					&& rule.getPipelines().get(0).getURLMode() == RuleURLMode.RULEPIPELINE_MODE_THUMBNAIL_URL && txtThumbnail.getText().length() == 0) {
				txtMessage.setText(Localization.getString("ThumbnailURLMissing"));
				return;
			}
			if (txtContainer.getText().matches(rule.getPattern()) == false) {
				txtMessage.setText(Localization.getString("ContainerURLNotMatch"));
				return;
			}
			Pic p = new Pic(txtContainer.getText(), "", "");
			p.setThumb(txtThumbnail.getText());
			final URLParseObject upo = new URLParseObject(txtContainer.getText(), txtThumbnail.getText(), p);
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						String result[] = rule.getURLAndFilename(upo, true);
						txtResultURL.setText(result[0]);
						txtResultFilename.setText(result[1]);
					} catch (HostException he) {
						txtResultURL.setText(he.getMessage());
						txtResultFilename.setText("");
					}
				}
			});
			t.start();
			try {
				t.join();
			} catch (InterruptedException e1) {
			}
			txtResultPageSourceCode.setText((String)upo.getInfo("PageSourceCode"));
		}
	}

}

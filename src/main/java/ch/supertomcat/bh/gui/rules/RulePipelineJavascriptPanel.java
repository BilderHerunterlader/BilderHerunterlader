package ch.supertomcat.bh.gui.rules;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.RuleMode;
import ch.supertomcat.bh.rules.RulePipeline;
import ch.supertomcat.supertomcattools.guitools.Localization;
import de.sciss.syntaxpane.DefaultSyntaxKit;

/**
 * Rule-Pipeline-Panel
 */
public class RulePipelineJavascriptPanel extends JPanel implements IRulePipelineURLPanel {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	private JEditorPane txtJavascript;

	/**
	 * ScrollPane
	 */
	private JScrollPane sp;

	/**
	 * RulePipeline
	 */
	private RulePipeline pipe = null;

	/**
	 * Rule
	 */
	@SuppressWarnings("unused")
	private Rule rule = null;

	/**
	 * lblWaitBeforeExecute
	 */
	private JLabel lblWaitBeforeExecute = new JLabel(Localization.getString("RulePipelineWaitBeforeExecute"));

	/**
	 * txtWaitBeforeExecute
	 */
	private JTextField txtWaitBeforeExecute = new JTextField(5);

	private JCheckBox chkURLDecodeResult = new JCheckBox(Localization.getString("RulePipelineURLDecodeResult"), false);

	private JCheckBox chkSendCookies = new JCheckBox(Localization.getString("RulePipelineSendCookies"), true);

	/**
	 * TextPane
	 */
	private JTextArea txtNote = new JTextArea();

	private JToggleButton btnHelp = new JToggleButton(Localization.getString("Help"), Icons.getTangoIcon("apps/help-browser.png", 16));

	private JScrollPane spHelp = null;

	/**
	 * Panel
	 */
	private JPanel pnlRB = new JPanel();

	private JPanel pnlTop = new JPanel();

	/**
	 * Owner
	 */
	@SuppressWarnings("unused")
	private JDialog owner = null;

	/**
	 * Constructor
	 * 
	 * @param rule Rule
	 * @param pipe Pipeline
	 * @param owner Owner
	 */
	public RulePipelineJavascriptPanel(Rule rule, RulePipeline pipe, JDialog owner) {
		super();
		this.owner = owner;
		this.rule = rule;
		this.pipe = pipe;
		setLayout(new BorderLayout());

		TitledBorder brd = BorderFactory.createTitledBorder(Localization.getString("PipelineJavascript"));
		this.setBorder(brd);

		DefaultSyntaxKit.initKit();

		txtJavascript = new JEditorPane();
		txtJavascript.setContentType("text/javascript");
		txtJavascript.setText(pipe.getJavascript());
		FontMetrics fontMetrics = txtJavascript.getFontMetrics(txtJavascript.getFont());
		int fontHeight = fontMetrics.getLeading() + fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();
		txtJavascript.setPreferredSize(new Dimension(120 * fontMetrics.charWidth('A'), 20 * fontHeight));
		sp = new JScrollPane(txtJavascript);

		txtWaitBeforeExecute.setText(String.valueOf(this.pipe.getWaitBeforeExecute()));

		chkURLDecodeResult.setToolTipText(Localization.getString("RulePipelineURLDecodeResultToolTip"));
		chkURLDecodeResult.setSelected(this.pipe.isUrlDecodeResult());

		chkSendCookies.setSelected(this.pipe.isSendCookies());

		txtNote.setText(Localization.getString("RuleJavascript"));
		txtNote.setEditable(false);
		txtNote.setLineWrap(true);
		txtNote.setWrapStyleWord(true);
		txtNote.setRows(7);
		txtNote.setFont(txtWaitBeforeExecute.getFont());
		spHelp = new JScrollPane(txtNote);
		spHelp.setVisible(false);
		btnHelp.setSelected(false);

		pnlRB.setLayout(new GridLayout(2, 3));
		pnlRB.add(lblWaitBeforeExecute);
		pnlRB.add(txtWaitBeforeExecute);
		pnlRB.add(btnHelp);
		pnlRB.add(chkURLDecodeResult);
		pnlRB.add(chkSendCookies);
		pnlRB.add(new JLabel());

		btnHelp.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				spHelp.setVisible(btnHelp.isSelected());
				pnlTop.revalidate();
				pnlTop.repaint();
			}
		});

		pnlTop = new JPanel(new BorderLayout());
		pnlTop.add(pnlRB, BorderLayout.NORTH);
		pnlTop.add(spHelp, BorderLayout.CENTER);

		add(pnlTop, BorderLayout.NORTH);
		add(sp, BorderLayout.CENTER);
	}

	/**
	 * Apply
	 */
	@Override
	public void apply() {
		pipe.setMode(RuleMode.RULE_MODE_JAVASCRIPT);
		pipe.setJavascript(txtJavascript.getText());
		int waitBeforeExecute = 0;
		try {
			waitBeforeExecute = Integer.parseInt(txtWaitBeforeExecute.getText());
		} catch (NumberFormatException nfe) {
		}
		pipe.setWaitBeforeExecute(waitBeforeExecute);
		pipe.setUrlDecodeResult(chkURLDecodeResult.isSelected());
		pipe.setSendCookies(chkSendCookies.isSelected());
	}
}

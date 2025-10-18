package ch.supertomcat.bh.gui.rules.editor;

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
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.RuleHtmlCode;
import ch.supertomcat.bh.rules.RulePipelineURLRegex;
import ch.supertomcat.bh.rules.trace.RuleTraceInfo;
import ch.supertomcat.bh.rules.xml.URLMode;
import ch.supertomcat.bh.rules.xml.URLRegexPipelineMode;
import ch.supertomcat.supertomcatutils.gui.Icons;
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
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

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
	private JLabel lblResultFilenameDownloadSelection = new JLabel(Localization.getString("FilenameDownloadSelection"));

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
	private JTextField txtResultFilenameDownloadSelection = new JTextField(80);

	/**
	 * TextField
	 */
	private JTextField txtResultFilename = new JTextField(80);

	/**
	 * TextField
	 */
	private JTextArea txtResultPageSourceCodeLast = new JTextArea(15, 80);

	/**
	 * TextField
	 */
	private JTextArea txtResultPageSourceCodeFirst = new JTextArea(15, 80);

	/**
	 * TextField
	 */
	private JTextArea txtResultPageSourceCodeFromFirstURL = new JTextArea(15, 80);

	/**
	 * TextField
	 */
	private JTextArea txtResultPageSourceCodeFilename = new JTextArea(15, 80);

	/**
	 * TextField
	 */
	private JTextArea txtRuleTraceInfo = new JTextArea(15, 80);

	/**
	 * Button
	 */
	private JButton btnTest = new JButton("Test", Icons.getTangoSVGIcon("apps/utilities-system-monitor.svg", 16));

	/**
	 * Button
	 */
	private JButton btnClose = new JButton("Close", Icons.getTangoSVGIcon("emblems/emblem-unreadable.svg", 16));

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

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("RuleTraceInfo", new JScrollPane(txtRuleTraceInfo));
		tabs.addTab("LastPageSourceCode", new JScrollPane(txtResultPageSourceCodeLast));
		tabs.addTab("FirstPageSourceCode", new JScrollPane(txtResultPageSourceCodeFirst));
		tabs.addTab("PageSourceCodeFromFirstURL", new JScrollPane(txtResultPageSourceCodeFromFirstURL));
		tabs.addTab("PageSourceCodeFilename", new JScrollPane(txtResultPageSourceCodeFilename));

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
		gbc = gblt.getGBC(0, i, 1, 1, 0.1, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblResultFilenameDownloadSelection, pnlMain);
		gbc = gblt.getGBC(1, i, 1, 1, 0.9, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtResultFilenameDownloadSelection, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.1, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblResultPageSourceCode, pnlMain);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.9, 0.5);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, tabs, pnlMain);

		add(pnlMain, BorderLayout.CENTER);

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtContainer);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtThumbnail);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtResultURL);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtResultFilenameDownloadSelection);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtResultFilename);

		btnTest.addActionListener(this);
		btnClose.addActionListener(this);
		pack();
		setLocationRelativeTo(owner);
		setModal(true);
		txtContainer.requestFocus();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnClose) {
			this.dispose();
		} else if (e.getSource() == btnTest) {
			txtMessage.setText("");
			if (!rule.getPipelines().isEmpty() && rule.getPipelines().get(0) instanceof RulePipelineURLRegex) {
				RulePipelineURLRegex urlRegexPipeline = (RulePipelineURLRegex)rule.getPipelines().get(0);
				if (urlRegexPipeline.getDefinition().getMode() == URLRegexPipelineMode.CONTAINER_OR_THUMBNAIL_URL && urlRegexPipeline.getDefinition().getUrlMode() == URLMode.THUMBNAIL_URL
						&& txtThumbnail.getText().isEmpty()) {
					txtMessage.setText(Localization.getString("ThumbnailURLMissing"));
					return;
				}
			}
			if (!txtContainer.getText().matches(rule.getDefinition().getUrlPattern())) {
				txtMessage.setText(Localization.getString("ContainerURLNotMatch"));
				return;
			}
			Pic p = new Pic(txtContainer.getText(), "", "");
			p.setThumb(txtThumbnail.getText());
			final URLParseObject upo = new URLParseObject(txtContainer.getText(), txtThumbnail.getText(), p);

			BasicCookieStore cookieStore = new BasicCookieStore();
			HttpClientContext context = ContextBuilder.create().useCookieStore(cookieStore).build();

			upo.addInfo(URLParseObject.DOWNLOADER_HTTP_COOKIE_STORE, cookieStore);
			upo.addInfo(URLParseObject.DOWNLOADER_HTTP_CONTEXT, context);

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						String[] result = rule.getURLAndFilename(upo, true);
						txtResultURL.setText(result[0]);
						txtResultFilename.setText(result[1]);
					} catch (HostException he) {
						txtResultURL.setText(he.getMessage());
						txtResultFilename.setText("");
						logger.error("Could not get URL and filename", he);
					}

					RuleTraceInfo ruleTraceInfo = (RuleTraceInfo)upo.getInfo(URLParseObject.RULE_TRACE_INFO);
					String resultFilenameDownloadSelection = rule.getFilename(upo.getContainerURL(), ruleTraceInfo);
					txtResultFilenameDownloadSelection.setText(resultFilenameDownloadSelection);
				}
			});
			t.start();
			try {
				t.join();
			} catch (InterruptedException ex) {
				// Nothing to do
			}

			setHtmlSourceCode(upo, "PageSourceCodeLast", txtResultPageSourceCodeLast);
			setHtmlSourceCode(upo, "PageSourceCodeFirst", txtResultPageSourceCodeFirst);
			setHtmlSourceCode(upo, "PageSourceCodeFromFirstURL", txtResultPageSourceCodeFromFirstURL);
			setHtmlSourceCode(upo, "PageSourceCodeFilename", txtResultPageSourceCodeFilename);
			setRuleTraceInfo(upo, txtRuleTraceInfo);
		}
	}

	private void setHtmlSourceCode(URLParseObject upo, String upoInfoKey, JTextArea txt) {
		RuleHtmlCode ruleHtmlCode = (RuleHtmlCode)upo.getInfo(upoInfoKey);
		if (ruleHtmlCode != null && ruleHtmlCode.isAvailable()) {
			txt.setText(ruleHtmlCode.getHtmlCode());
		} else {
			txt.setText("Not Available");
		}
	}

	private void setRuleTraceInfo(URLParseObject upo, JTextArea txt) {
		RuleTraceInfo ruleTraceInfo = (RuleTraceInfo)upo.getInfo(URLParseObject.RULE_TRACE_INFO);
		if (ruleTraceInfo != null) {
			txt.setText(ruleTraceInfo.toString());
		} else {
			txt.setText("Not Available");
		}
	}
}

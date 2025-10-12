package ch.supertomcat.bh.gui.adder;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.downloader.ProxyManager;
import ch.supertomcat.bh.importexport.ImportIradaTsv;
import ch.supertomcat.bh.importexport.Tsv;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.dialog.FileDialogUtil;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Irada-Tsv-Import-Dialog
 * 
 * @see ch.supertomcat.bh.importexport.ImportIradaTsv
 */
public class IradaTsvDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * ButtonGroup
	 */
	private ButtonGroup bg = new ButtonGroup();

	/**
	 * RadioButton
	 */
	private JRadioButton rbFile = new JRadioButton(Localization.getString("File"), true);

	/**
	 * RadioButton
	 */
	private JRadioButton rbURL = new JRadioButton(Localization.getString("URL"), false);

	/**
	 * Panel
	 */
	private JPanel pnlFile = new JPanel();

	/**
	 * Label
	 */
	private JLabel lblFile = new JLabel(Localization.getString("File") + ":");

	/**
	 * TextField
	 */
	private JTextField txtFile = new JTextField("", 35);

	/**
	 * Button
	 */
	private JButton btnFile = new JButton("...");

	/**
	 * Label
	 */
	private JLabel lblURL = new JLabel(Localization.getString("URL") + ":");

	/**
	 * TextField
	 */
	private JTextField txtURL = new JTextField("", 40);

	/**
	 * Button
	 */
	private JButton btnOK = new JButton(Localization.getString("OK"));

	/**
	 * Button
	 */
	private JButton btnCancel = new JButton(Localization.getString("Cancel"));

	/**
	 * Array containing Tsv-Objects
	 */
	private List<Tsv> result = null;

	/**
	 * Flag if ok was pressed
	 */
	private boolean okPressed = false;

	/**
	 * Proxy Manager
	 */
	private final ProxyManager proxyManager;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param proxyManager Proxy Manager
	 * @param settingsManager Settings Manager
	 */
	public IradaTsvDialog(Window owner, ProxyManager proxyManager, SettingsManager settingsManager) {
		super(owner);
		this.proxyManager = proxyManager;
		this.settingsManager = settingsManager;

		JPanel pnlRB = new JPanel();
		pnlRB.add(rbFile);
		pnlRB.add(rbURL);
		bg.add(rbFile);
		bg.add(rbURL);
		rbFile.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateComponentVisibility();
			}
		});
		rbURL.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateComponentVisibility();
			}
		});

		txtFile.setEditable(false);

		pnlFile.add(lblFile);
		pnlFile.add(txtFile);
		pnlFile.add(btnFile);
		btnFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = FileDialogUtil.showFileOpenDialog(owner, settingsManager.getDirectorySettings().getLastUsedImportPath(), new TsvFileFilter());
				if (file != null) {
					settingsManager.getDirectorySettings().setLastUsedImportPath(FileUtil.getPathFromFile(file.toPath()));
					settingsManager.writeSettings(true);
					txtFile.setText(file.getAbsolutePath());
				}
			}
		});

		pnlFile.add(lblURL);
		pnlFile.add(txtURL);

		lblURL.setVisible(false);
		txtURL.setVisible(false);

		setLayout(new BorderLayout());

		JPanel pnlButtons = new JPanel();
		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);

		btnOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (rbFile.isSelected()) {
					if (loadTsvFile()) {
						okPressed = true;
						dispose();
					}
				} else {
					if (loadTsvURL()) {
						okPressed = true;
						dispose();
					}
				}
			}
		});
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed = false;
				dispose();
			}
		});

		add(pnlRB, BorderLayout.NORTH);
		add(pnlFile, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(owner);
		setTitle(Localization.getString("IradaTsvImport"));

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtFile);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtURL);

		// Enter und Escape (bevor setVisible(true)!)
		ActionMap am = this.getRootPane().getActionMap();
		InputMap im = this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
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

		setModal(true);
		setVisible(true);
	}

	/**
	 * Get-Method
	 * 
	 * @return Array containing Tsv-Objects
	 */
	public List<Tsv> getResult() {
		return result;
	}

	/**
	 * Get-Method
	 * 
	 * @return True if ok was pressed
	 */
	public boolean isOkPressed() {
		return okPressed;
	}

	private boolean loadTsvFile() {
		try (FileInputStream in = new FileInputStream(txtFile.getText())) {
			result = ImportIradaTsv.importTsv(in);
			return true;
		} catch (IOException ex) {
			logger.error("Could not load file: {}", txtFile.getText(), ex);
			displayErrorMessage(ex);
			return false;
		}
	}

	private boolean loadTsvURL() {
		try (CloseableHttpClient client = proxyManager.getHTTPClient()) {
			String encodedURL = HTTPUtil.encodeURL(txtURL.getText());
			HttpGet method = new HttpGet(encodedURL);
			method.setHeader(HttpHeaders.USER_AGENT, settingsManager.getUserAgent());

			client.execute(method, response -> {
				StatusLine statusLine = new StatusLine(response);
				int statusCode = statusLine.getStatusCode();
				if (statusCode != 200) {
					displayErrorMessage("HTTP-Error: " + statusCode + " " + statusLine.getReasonPhrase());
					return false;
				} else {
					try (@SuppressWarnings("resource")
					InputStream in = response.getEntity().getContent()) {
						result = ImportIradaTsv.importTsv(in);
					}
				}
				return null;
			});
			return true;
		} catch (Exception ex) {
			logger.error("Could not load Tsv from URL: {}", txtURL.getText(), ex);
			displayErrorMessage(ex);
			return false;
		}
	}

	private void displayErrorMessage(String errorMessage) {
		JTextArea txtErrorMessage = new JTextArea(errorMessage, 20, 80);
		JScrollPane sp = new JScrollPane(txtErrorMessage);
		JOptionPane.showMessageDialog(this, sp, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private void displayErrorMessage(Throwable t) {
		StringWriter errorMessageWriter = new StringWriter();
		t.printStackTrace(new PrintWriter(errorMessageWriter));
		displayErrorMessage(errorMessageWriter.toString());
	}

	private void updateComponentVisibility() {
		boolean b = rbFile.isSelected();
		lblFile.setVisible(b);
		txtFile.setVisible(b);
		btnFile.setVisible(b);
		lblURL.setVisible(!b);
		txtURL.setVisible(!b);
		repaint();
	}

	/**
	 * Tsv File Filter
	 */
	private static class TsvFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {
			return f.isDirectory() || f.getName().endsWith(".tsv");
		}

		@Override
		public String getDescription() {
			return "Irada-Tsv-File (*.tsv)";
		}
	}
}

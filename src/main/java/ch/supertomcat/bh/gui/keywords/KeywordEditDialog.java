package ch.supertomcat.bh.gui.keywords;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

import ch.supertomcat.bh.gui.SpringUtilities;
import ch.supertomcat.bh.keywords.Keyword;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.copyandpaste.JTextComponentCopyAndPaste;

/**
 * Dialog for editing a new keyword
 */
public class KeywordEditDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	/**
	 * Panel for Buttons
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
	 * Panel for Labels and TextFields
	 */
	private JPanel pnlLabels = new JPanel();

	/**
	 * Label
	 */
	private JLabel lblTitle = new JLabel(Localization.getString("Title"));

	/**
	 * Label
	 */
	private JLabel lblKeywords = new JLabel(Localization.getString("Keywords"));

	/**
	 * Label
	 */
	private JLabel lblPath = new JLabel(Localization.getString("Folder"));

	/**
	 * Label
	 */
	private JLabel lblRPath = new JLabel(Localization.getString("RelativeFolder"));

	/**
	 * Label
	 */
	private JLabel lblBRPath = new JLabel(Localization.getString("RelativPath"));

	/**
	 * TextField
	 */
	protected JTextField txtTitle = new JTextField(40);

	/**
	 * TextField
	 */
	protected JTextField txtKeywords = new JTextField(40);

	/**
	 * TextField
	 */
	protected KeywordCellEditorComponent txtPath = new KeywordCellEditorComponent();

	/**
	 * TextField
	 */
	protected KeywordCellEditorComponent txtRPath = new KeywordCellEditorComponent();

	/**
	 * Checkbox
	 */
	protected JCheckBox chkBRPath = new JCheckBox(Localization.getString("RelativPath"), true);

	/**
	 * Flag if ok was pressed
	 */
	private boolean okPressed = false;

	/**
	 * Constructor
	 * 
	 * @param owner Owner-Dialog
	 * @param title Title
	 */
	public KeywordEditDialog(Window owner, String title) {
		this(owner, title, "", "", SettingsManager.instance().getSavePath(), true, "");
	}

	/**
	 * Constructor
	 * 
	 * @param owner Owner-Dialog
	 * @param title Title
	 * @param keywordTitle Keyword title
	 * @param keywords keywords
	 * @param downloadPath absolute Path
	 * @param relativePath use relative Path
	 * @param relativeDownloadPath relative Path
	 */
	public KeywordEditDialog(Window owner, String title, String keywordTitle, String keywords, String downloadPath, boolean relativePath, String relativeDownloadPath) {
		super(owner, title);
		init(keywordTitle, keywords, downloadPath, relativePath, relativeDownloadPath);
	}

	/**
	 * Initialize GUI-Components
	 * 
	 * @param keywordTitle Keyword title
	 * @param keywords keywords
	 * @param downloadPath absolute Path
	 * @param relativePath use relative Path
	 * @param relativeDownloadPath relative Path
	 */
	private void init(String keywordTitle, String keywords, String downloadPath, boolean relativePath, String relativeDownloadPath) {
		txtPath.setRelative(false);
		txtRPath.setRelative(true);

		txtTitle.setText(keywordTitle);
		txtKeywords.setText(keywords);
		txtPath.setText(downloadPath);
		txtRPath.setText(relativeDownloadPath);
		chkBRPath.setSelected(relativePath);

		pnlLabels.setLayout(new SpringLayout());
		pnlLabels.add(lblTitle);
		pnlLabels.add(txtTitle);
		pnlLabels.add(lblKeywords);
		pnlLabels.add(txtKeywords);
		pnlLabels.add(lblPath);
		pnlLabels.add(txtPath);
		pnlLabels.add(lblRPath);
		pnlLabels.add(txtRPath);
		pnlLabels.add(lblBRPath);
		pnlLabels.add(chkBRPath);

		SpringUtilities.makeCompactGrid(pnlLabels, 5, 2, 5, 5, 5, 5);

		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);
		setLayout(new BorderLayout());
		btnOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed = true;
				dispose();
			}
		});
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed = false;
				dispose();
			}
		});
		add(pnlLabels, BorderLayout.NORTH);
		add(pnlButtons, BorderLayout.SOUTH);
		setModal(true);
		pack();
		setMinimumSize(this.getSize());
		setLocationRelativeTo(super.getOwner());

		txtTitle.requestFocusInWindow();

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtTitle);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtKeywords);

		// Enter und Escape (bevor setVisible(true)!)
		ActionMap am = getRootPane().getActionMap();
		InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
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
	}

	/**
	 * @return Title
	 */
	public String getKeywordTitle() {
		return txtTitle.getText();
	}

	/**
	 * @return Keywords
	 */
	public String getKeywords() {
		return txtKeywords.getText();
	}

	/**
	 * @return Path
	 */
	public String getPath() {
		return txtPath.getPath();
	}

	/**
	 * @return Relative Path
	 */
	public String getRelativePath() {
		return txtRPath.getPath();
	}

	/**
	 * @return Relative Path Selected
	 */
	public boolean isRelativePathSelected() {
		return chkBRPath.isSelected();
	}

	/**
	 * Get-Method
	 * 
	 * @return Keyword
	 */
	public Keyword getKeyword() {
		return new Keyword(txtTitle.getText(), txtKeywords.getText(), txtPath.getPath(), chkBRPath.isSelected(), txtRPath.getPath());
	}

	/**
	 * Get-Method
	 * 
	 * @return True if ok was pressed
	 */
	public boolean isOkPressed() {
		return okPressed;
	}

	/**
	 * Open the Dialog
	 * 
	 * @param owner Owner
	 * @param title Title
	 * @param keywordTitle Keyword title
	 * @param keywords keywords
	 * @param downloadPath absolute Path
	 * @param relativePath use relative Path
	 * @param relativeDownloadPath relative Path
	 * @return KeywordEditDialog or null if cancelled
	 */
	public static KeywordEditDialog openKeywordEditDialog(JDialog owner, String title, String keywordTitle, String keywords, String downloadPath, boolean relativePath, String relativeDownloadPath) {
		KeywordEditDialog dialog = new KeywordEditDialog(owner, title, keywordTitle, keywords, downloadPath, relativePath, relativeDownloadPath);
		dialog.setVisible(true);
		if (dialog.isOkPressed()) {
			return dialog;
		}
		return null;
	}

	/**
	 * Open the Dialog
	 * 
	 * @param owner Owner
	 * @param title Title
	 * @param keywordTitle Keyword title
	 * @param keywords keywords
	 * @param downloadPath absolute Path
	 * @param relativePath use relative Path
	 * @param relativeDownloadPath relative Path
	 * @return KeywordEditDialog or null if cancelled
	 */
	public static KeywordEditDialog openKeywordEditDialog(JFrame owner, String title, String keywordTitle, String keywords, String downloadPath, boolean relativePath, String relativeDownloadPath) {
		KeywordEditDialog dialog = new KeywordEditDialog(owner, title, keywordTitle, keywords, downloadPath, relativePath, relativeDownloadPath);
		dialog.setVisible(true);
		if (dialog.isOkPressed()) {
			return dialog;
		}
		return null;
	}
}

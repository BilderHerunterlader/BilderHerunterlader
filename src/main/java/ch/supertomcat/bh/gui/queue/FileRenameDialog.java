package ch.supertomcat.bh.gui.queue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;

/**
 * Dialog for renaming files
 */
public class FileRenameDialog extends JDialog implements ActionListener, ItemListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1704231042429412312L;

	/**
	 * Description
	 */
	private JTextArea lblDescription = new JTextArea();

	/**
	 * Panel
	 */
	private JPanel pnlClear = new JPanel();

	/**
	 * Panel
	 */
	private JPanel pnlRename = new JPanel();

	/**
	 * Panel
	 */
	private JPanel pnlFilename = new JPanel();

	/**
	 * Panel
	 */
	private JPanel pnlStartNumber = new JPanel();

	/**
	 * Label
	 */
	private JLabel lblFilename = new JLabel(Localization.getString("Filename"));

	/**
	 * Label
	 */
	private JLabel lblStartNumber = new JLabel(Localization.getString("RenameStartNumber"));

	/**
	 * Label
	 */
	private JLabel lblStep = new JLabel(Localization.getString("RenameStep"));

	/**
	 * ComboBox
	 */
	private JComboBox<String> txtFilename = new JComboBox<>();

	/**
	 * Button
	 */
	private JButton btnClear = new JButton(Localization.getString("Clear"), Icons.getTangoIcon("actions/edit-clear.png", 16));

	/**
	 * TextField
	 */
	private JTextField txtStartNumber = new JTextField(20);

	/**
	 * TextField
	 */
	private JTextField txtStep = new JTextField(20);

	/**
	 * Panel
	 */
	private JPanel pnlAppendPrefixAppendix = new JPanel();

	/**
	 * TextField
	 */
	private final JTextField txtPrefix;

	/**
	 * TextField
	 */
	private final JTextField txtAppendix;

	/**
	 * CheckBox
	 */
	private final JCheckBox cbPrefix;

	/**
	 * CheckBox
	 */
	private final JCheckBox cbAppendix;

	/**
	 * CheckBox
	 */
	private final JCheckBox cbOriginalFilenames;

	/**
	 * CheckBox
	 */
	private JCheckBox cbClear = new JCheckBox(Localization.getString("RenameDialogClear"), false);

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
	 * Count of files
	 */
	private int files = 0;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param description Description
	 * @param defaultvalue Default-Value
	 * @param files Count of files
	 * @param settingsManager Settings Manager
	 */
	public FileRenameDialog(JFrame owner, String description, String defaultvalue, int files, SettingsManager settingsManager) {
		super(owner);
		this.files = files;
		this.settingsManager = settingsManager;

		this.txtPrefix = new JTextField(settingsManager.getGUISettings().getFilenameChangePrefix(), 20);
		this.txtAppendix = new JTextField(settingsManager.getGUISettings().getFilenameChangeAppendix(), 20);
		this.cbPrefix = new JCheckBox(Localization.getString("AppendPrefix"), settingsManager.getGUISettings().isAppendPrefixFilenameChange());
		this.cbAppendix = new JCheckBox(Localization.getString("AppendAppendix"), settingsManager.getGUISettings().isAppendAppendixFilenameChange());
		this.cbOriginalFilenames = new JCheckBox(Localization.getString("KeepOriginalFilenames"), settingsManager.getGUISettings().isFilenameChangeKeepOriginal());

		setTitle(Localization.getString("FilenameInput"));

		setLayout(new BorderLayout());

		if (description.length() > 0) {
			lblDescription.setText(description);
		} else {
			lblDescription.setText(Localization.getString("RenameDialogDescriptionA") + "\n" + Localization.getString("RenameDialogDescriptionB") + "\n"
					+ Localization.getString("RenameDialogDescriptionC") + "\n" + Localization.getString("RenameDialogDescriptionD") + "\n" + "...");
		}

		lblDescription.setEditable(false);
		lblDescription.setOpaque(false);

		boolean b = cbOriginalFilenames.isSelected();
		lblFilename.setEnabled(!b);
		txtFilename.setEnabled(!b);
		btnClear.setEnabled(!b);

		btnOK.addActionListener(this);
		btnCancel.addActionListener(this);
		btnClear.addActionListener(this);
		cbClear.addItemListener(this);
		cbOriginalFilenames.addItemListener(this);

		pnlRename.setLayout(new BoxLayout(pnlRename, BoxLayout.Y_AXIS));

		Dimension fillerMinSize = new Dimension(5, 5);
		Dimension fillerPrefSize = new Dimension(5, 5);
		Dimension fillerMaxSize = new Dimension(5, Integer.MAX_VALUE);

		pnlClear.add(cbClear);

		limitHeightToPreferredHeight(pnlClear);
		pnlRename.add(pnlClear);

		pnlAppendPrefixAppendix.setLayout(new BoxLayout(pnlAppendPrefixAppendix, BoxLayout.X_AXIS));
		pnlAppendPrefixAppendix.add(cbOriginalFilenames);
		pnlAppendPrefixAppendix.add(cbPrefix);
		pnlAppendPrefixAppendix.add(txtPrefix);
		pnlAppendPrefixAppendix.add(cbAppendix);
		pnlAppendPrefixAppendix.add(txtAppendix);

		limitHeightToPreferredHeight(pnlAppendPrefixAppendix);
		pnlRename.add(pnlAppendPrefixAppendix);

		txtStartNumber.setText("1");
		txtStep.setText("1");
		pnlStartNumber.add(lblStartNumber);
		pnlStartNumber.add(txtStartNumber);
		pnlStartNumber.add(lblStep);
		pnlStartNumber.add(txtStep);

		limitHeightToPreferredHeight(pnlStartNumber);
		pnlRename.add(pnlStartNumber);

		/*
		 * Make sure ComboBox is large enough by providing a String with the desired minimum length
		 */
		String comboBoxPrototypeDisplayValue = "A/Really/Long/Path/AndAReallyLongFilenameeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee.jpg";
		txtFilename.setPrototypeDisplayValue(comboBoxPrototypeDisplayValue);

		txtFilename.addItem("");
		txtFilename.addItem(defaultvalue);
		List<String> lstAdd = settingsManager.getGUISettings().getFilenameChangeHistory();
		for (int i = lstAdd.size() - 1; i >= 0; i--) {
			txtFilename.addItem(lstAdd.get(i));
		}

		txtFilename.setSelectedIndex(1);
		txtFilename.setEditable(true);

		pnlFilename.setLayout(new BoxLayout(pnlFilename, BoxLayout.X_AXIS));
		pnlFilename.add(new Box.Filler(fillerMinSize, fillerPrefSize, fillerMaxSize));
		pnlFilename.add(lblFilename);
		pnlFilename.add(new Box.Filler(fillerMinSize, fillerPrefSize, fillerMaxSize));
		pnlFilename.add(txtFilename);
		pnlFilename.add(new Box.Filler(fillerMinSize, fillerPrefSize, fillerMaxSize));
		pnlFilename.add(btnClear);

		limitHeightToPreferredHeight(pnlFilename);
		pnlRename.add(pnlFilename);

		pnlRename.add(Box.createVerticalGlue());

		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);

		add(lblDescription, BorderLayout.NORTH);
		add(new JScrollPane(pnlRename), BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);
		setModal(true);
		pack();
		setLocationRelativeTo(owner);
		txtFilename.requestFocusInWindow();

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtAppendix);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtPrefix);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtStartNumber);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtStep);

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
				cancelPressed();
			}
		};
		KeyStroke windowOkStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		Action windowOkAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed();
			}
		};
		im.put(windowCloseStroke, windowCloseKey);
		am.put(windowCloseKey, windowCloseAction);
		im.put(windowOkStroke, windowOkKey);
		am.put(windowOkKey, windowOkAction);

		setVisible(true);
	}

	/**
	 * Limits the height of the component to it's preferred height. Component can still be increased in width.
	 * 
	 * @param comp Comp
	 */
	private void limitHeightToPreferredHeight(Component comp) {
		comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, comp.getPreferredSize().height));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnOK) {
			okPressed();
		} else if (e.getSource() == btnCancel) {
			canceled = true;
			this.dispose();
		} else if (e.getSource() == btnClear) {
			txtFilename.setSelectedIndex(0);
		}
	}

	/**
	 * Ok-Button-Pressed-Method
	 */
	public void okPressed() {
		canceled = false;

		// Check Start- and Step-Value
		Integer start = null;
		Integer step = null;
		try {
			start = Integer.parseInt(txtStartNumber.getText());
			txtStartNumber.setForeground(Color.BLACK);
			if (start < 0) {
				txtStartNumber.setForeground(Color.RED);
				canceled = true;
			}
		} catch (NumberFormatException nfe) {
			txtStartNumber.setForeground(Color.RED);
			canceled = true;
		}

		try {
			step = Integer.parseInt(txtStep.getText());
			txtStep.setForeground(Color.BLACK);
			if (step < 1) {
				txtStep.setForeground(Color.RED);
				canceled = true;
			}
			if (start != null) {
				@SuppressWarnings("unused")
				int maxval = start + (step * this.files);
			}
		} catch (NumberFormatException nfe) {
			txtStep.setForeground(Color.RED);
			canceled = true;
		}

		if (!canceled) {
			// If not canceled

			if (cbClear.isSelected()) {
				txtFilename.setSelectedIndex(0);
			}
			String f = "";
			if (!cbClear.isSelected()) {
				f = (String)txtFilename.getSelectedItem();
			}

			if (f.length() > 0) {
				boolean available = false;
				for (int a = 0; a < txtFilename.getItemCount(); a++) {
					if (txtFilename.getItemAt(a).equals(f)) {
						available = true;
						txtFilename.setSelectedIndex(a);
						break;
					}
				}
				if (!available) {
					if (txtFilename.getItemCount() > 2) {
						txtFilename.insertItemAt(f, 2);
					} else {
						txtFilename.addItem(f);
					}
					txtFilename.setSelectedItem(f);
					settingsManager.getGUISettings().getFilenameChangeHistory().add(f);
				}
			}
			settingsManager.getGUISettings().setFilenameChangePrefix(txtPrefix.getText());
			settingsManager.getGUISettings().setFilenameChangeAppendix(txtAppendix.getText());
			settingsManager.getGUISettings().setAppendPrefixFilenameChange(cbPrefix.isSelected());
			settingsManager.getGUISettings().setAppendAppendixFilenameChange(cbAppendix.isSelected());
			settingsManager.getGUISettings().setFilenameChangeKeepOriginal(cbOriginalFilenames.isSelected());
			settingsManager.writeSettings(true);
			this.dispose();
		}
	}

	/**
	 * Cancel-Button-Pressed-Method
	 */
	public void cancelPressed() {
		canceled = true;
		this.dispose();
	}

	/**
	 * Get-Methode
	 * 
	 * @return Value
	 */
	private String getValue() {
		return (String)txtFilename.getSelectedItem();
	}

	/**
	 * Get-Methode
	 * 
	 * @return True if canceled
	 */
	private boolean isCanceled() {
		return canceled;
	}

	/**
	 * Get-Methode
	 * 
	 * @return Start-Number
	 */
	private String getStart() {
		return txtStartNumber.getText();
	}

	/**
	 * Get-Methode
	 * 
	 * @return Step
	 */
	private String getStep() {
		return txtStep.getText();
	}

	/**
	 * Get-Methode
	 * 
	 * @return Prefix
	 */
	private String getPrefix() {
		return (cbPrefix.isSelected() && !cbClear.isSelected()) ? txtPrefix.getText() : "";
	}

	/**
	 * Get-Methode
	 * 
	 * @return Suffix
	 */
	private String getSuffix() {
		return (cbAppendix.isSelected() && !cbClear.isSelected()) ? txtAppendix.getText() : "";
	}

	/**
	 * Returns an empty string if original filenames shouldn't be kept
	 * 
	 * @return Empty string if original filenames shouldn't be kept
	 */
	private String getKeepOriginalFilename() {
		return cbOriginalFilenames.isSelected() ? "KeepOriginal" : "";
	}

	/**
	 * Returns an empty string if the filenames should'nt be cleared
	 * 
	 * @return Empty string if the filenames should'nt be cleared
	 */
	private String getClearFilename() {
		return cbClear.isSelected() ? "ClearFilename" : "";
	}

	/**
	 * Show dialog
	 * 
	 * @param owner Owner
	 * @param description Description
	 * @param defaultvalue Default-Value
	 * @param files Count of Files
	 * @param settingsManager Settings Manager
	 * @return Value
	 */
	public static String[] showFileRenameDialog(JFrame owner, String description, String defaultvalue, int files, SettingsManager settingsManager) {
		FileRenameDialog frd = new FileRenameDialog(owner, description, defaultvalue, files, settingsManager);
		if (frd.isCanceled()) {
			return null;
		}
		String[] retval = new String[7];
		retval[0] = frd.getValue();
		retval[1] = frd.getStart();
		retval[2] = frd.getStep();
		retval[3] = frd.getPrefix();
		retval[4] = frd.getSuffix();
		retval[5] = frd.getKeepOriginalFilename();
		retval[6] = frd.getClearFilename();
		return retval;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == cbClear) {
			boolean b = cbClear.isSelected();
			lblFilename.setEnabled(!b);
			lblStartNumber.setEnabled(!b);
			lblStep.setEnabled(!b);
			txtFilename.setEnabled(!b);
			btnClear.setEnabled(!b);
			txtStartNumber.setEnabled(!b);
			txtStep.setEnabled(!b);
			txtPrefix.setEnabled(!b);
			txtAppendix.setEnabled(!b);
			cbPrefix.setEnabled(!b);
			cbAppendix.setEnabled(!b);
			cbOriginalFilenames.setEnabled(!b);
		} else if (e.getSource() == cbOriginalFilenames) {
			boolean b = cbOriginalFilenames.isSelected();
			lblFilename.setEnabled(!b);
			txtFilename.setEnabled(!b);
			btnClear.setEnabled(!b);
		}
	}
}

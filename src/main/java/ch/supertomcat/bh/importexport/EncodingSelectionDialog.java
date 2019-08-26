package ch.supertomcat.bh.importexport;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * EncodingSelectionDialog
 */
public class EncodingSelectionDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7301641200215578960L;

	private JComboBox<String> cbEncoding = new JComboBox<>();

	private List<String> encodingArr = new ArrayList<>();

	/**
	 * Panel fuer Buttons
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * OK
	 */
	private JButton btnOK = new JButton(Localization.getString("OK"));

	/**
	 * Cancel
	 */
	private JButton btnCancel = new JButton(Localization.getString("Cancel"));

	private boolean canceled = true;

	/**
	 * Constructor
	 * 
	 * @param comp Component
	 */
	public EncodingSelectionDialog(Frame comp) {
		super(comp, true);
		init();
	}

	/**
	 * Constructor
	 * 
	 * @param comp Component
	 */
	public EncodingSelectionDialog(Dialog comp) {
		super(comp, true);
		init();
	}

	private void init() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle(Localization.getString("ChooseFileEncoding"));
		setLayout(new BorderLayout());

		/*
		 * Make sure ComboBox is large enough by providing a String with the desired minimum length
		 */
		cbEncoding.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

		SortedMap<String, Charset> encodings = Charset.availableCharsets();
		Iterator<String> ite = encodings.keySet().iterator();
		Charset charset = null;
		int index = 0;
		int i = 0;

		while (ite.hasNext()) {
			charset = encodings.get(ite.next());
			if (charset == Charset.defaultCharset()) {
				index = i;
			}
			encodingArr.add(charset.name());
			cbEncoding.addItem(charset.displayName());
			i++;
		}
		cbEncoding.setSelectedIndex(index);

		add(cbEncoding, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);
		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);
		btnOK.addActionListener(this);
		btnCancel.addActionListener(this);

		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnOK) {
			canceled = false;
			dispose();
		} else if (e.getSource() == btnCancel) {
			canceled = true;
			dispose();
		}
	}

	/**
	 * Get-Method
	 * 
	 * @return Encoding
	 */
	public String getChosenEncoding() {
		if (canceled) {
			return null;
		}
		return encodingArr.get(cbEncoding.getSelectedIndex());
	}
}

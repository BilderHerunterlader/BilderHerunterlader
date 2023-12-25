package ch.supertomcat.bh.gui.settings;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;

/**
 * Editor-Component for Subdirs (Filesize)
 */
public class SubdirCellEditorComponent extends JPanel {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(SubdirCellEditorComponent.class);

	/**
	 * UID
	 */
	private static final long serialVersionUID = -1695172104892889362L;

	/**
	 * DefaultValue
	 */
	private String defaultVal = "0";

	/**
	 * TextField
	 */
	private JTextField txtVal = new JTextField("", 9);

	/**
	 * Constructor
	 */
	public SubdirCellEditorComponent() {
		txtVal.setHorizontalAlignment(SwingConstants.LEFT);
		this.setLayout(new BorderLayout());
		add(txtVal, BorderLayout.CENTER);

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtVal);
	}

	/**
	 * Set-Method
	 * 
	 * @param val Value
	 */
	public void setText(String val) {
		if (checkVal(val)) {
			defaultVal = val;
		}
		txtVal.setText(defaultVal);
	}

	/**
	 * Get-Method
	 * 
	 * @return Value
	 */
	public String getVal() {
		String val = txtVal.getText();
		if (checkVal(val)) {
			return val;
		} else {
			return defaultVal;
		}
	}

	/**
	 * Get-Method
	 * 
	 * @return Value
	 */
	public long getLongVal() {
		String val = txtVal.getText();
		return parseVal(val);
	}

	@Override
	public String toString() {
		String val = txtVal.getText();
		if (checkVal(val)) {
			return val;
		} else {
			return defaultVal;
		}
	}

	/**
	 * Checks if val is a long-value
	 * 
	 * @param val Value
	 * @return True if val is a long-value
	 */
	private boolean checkVal(String val) {
		try {
			Long.parseLong(val);
			return true;
		} catch (NumberFormatException nfe) {
			logger.error(nfe.getMessage(), nfe);
		}
		return false;
	}

	/**
	 * Returns the long in the val-String or 0 if not possible
	 * 
	 * @param val Value
	 * @return Long-Value
	 */
	private long parseVal(String val) {
		try {
			return Long.parseLong(val);
		} catch (NumberFormatException nfe) {
			logger.error(nfe.getMessage(), nfe);
		}
		try {
			return Long.parseLong(defaultVal);
		} catch (NumberFormatException nfe) {
			logger.error(nfe.getMessage(), nfe);
		}
		return 0;
	}
}

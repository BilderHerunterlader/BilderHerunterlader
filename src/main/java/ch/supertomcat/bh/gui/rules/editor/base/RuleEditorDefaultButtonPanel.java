package ch.supertomcat.bh.gui.rules.editor.base;

import java.awt.event.ActionListener;

import javax.swing.JButton;

import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Panel with buttons to edit a table or list
 */
public class RuleEditorDefaultButtonPanel extends RuleEditorButtonPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * Button
	 */
	protected JButton btnNew = new JButton(Localization.getString("New"), Icons.getTangoSVGIcon("actions/document-new.svg", 16));

	/**
	 * Button
	 */
	protected JButton btnEdit = new JButton(Localization.getString("Edit"), Icons.getTangoSVGIcon("apps/accessories-text-editor.svg", 16));

	/**
	 * Button
	 */
	protected JButton btnUp = new JButton(Localization.getString("Up"), Icons.getTangoSVGIcon("actions/go-up.svg", 16));

	/**
	 * Button
	 */
	protected JButton btnDown = new JButton(Localization.getString("Down"), Icons.getTangoSVGIcon("actions/go-down.svg", 16));

	/**
	 * Button
	 */
	protected JButton btnDelete = new JButton(Localization.getString("Delete"), Icons.getTangoSVGIcon("actions/edit-delete.svg", 16));

	/**
	 * Constructor
	 * 
	 * @param editButton True if edit button is available, false otherwise
	 */
	public RuleEditorDefaultButtonPanel(boolean editButton) {
		if (editButton) {
			addButtons(btnNew, btnEdit, btnUp, btnDown, btnDelete);
		} else {
			addButtons(btnNew, btnUp, btnDown, btnDelete);
		}
	}

	/**
	 * Add ActionListener to New Button
	 * 
	 * @param l ActionListener
	 */
	public void addNewActionListener(ActionListener l) {
		btnNew.addActionListener(l);
	}

	/**
	 * Add ActionListener to Edit Button
	 * 
	 * @param l ActionListener
	 */
	public void addEditActionListener(ActionListener l) {
		btnEdit.addActionListener(l);
	}

	/**
	 * Add ActionListener to Up Button
	 * 
	 * @param l ActionListener
	 */
	public void addUpActionListener(ActionListener l) {
		btnUp.addActionListener(l);
	}

	/**
	 * Add ActionListener to Down Button
	 * 
	 * @param l ActionListener
	 */
	public void addDownActionListener(ActionListener l) {
		btnDown.addActionListener(l);
	}

	/**
	 * Add ActionListener to Delete Button
	 * 
	 * @param l ActionListener
	 */
	public void addDeleteActionListener(ActionListener l) {
		btnDelete.addActionListener(l);
	}
}

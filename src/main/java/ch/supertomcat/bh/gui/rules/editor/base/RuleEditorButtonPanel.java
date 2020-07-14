package ch.supertomcat.bh.gui.rules.editor.base;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.gui.SpringUtilities;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Panel with buttons to edit a table or list
 */
public abstract class RuleEditorButtonPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * Button
	 */
	protected JButton btnNew = new JButton(Localization.getString("New"), Icons.getTangoIcon("actions/document-new.png", 16));

	/**
	 * Button
	 */
	protected JButton btnEdit = new JButton(Localization.getString("Edit"), Icons.getTangoIcon("apps/accessories-text-editor.png", 16));

	/**
	 * Button
	 */
	protected JButton btnUp = new JButton(Localization.getString("Up"), Icons.getTangoIcon("actions/go-up.png", 16));

	/**
	 * Button
	 */
	protected JButton btnDown = new JButton(Localization.getString("Down"), Icons.getTangoIcon("actions/go-down.png", 16));

	/**
	 * Button
	 */
	protected JButton btnDelete = new JButton(Localization.getString("Delete"), Icons.getTangoIcon("actions/edit-delete.png", 16));

	/**
	 * Constructor
	 * 
	 * @param editButton True if edit button is available, false otherwise
	 */
	public RuleEditorButtonPanel(boolean editButton) {
		setLayout(new SpringLayout());
		add(btnNew);
		if (editButton) {
			add(btnEdit);
		}
		add(btnUp);
		add(btnDown);
		add(btnDelete);
		int buttonCount = 4;
		if (editButton) {
			buttonCount++;
		}
		SpringUtilities.makeCompactGrid(this, buttonCount, 1, 0, 0, 5, 5);

		btnNew.addActionListener(e -> actionNew(e));
		btnEdit.addActionListener(e -> actionEdit(e));
		btnUp.addActionListener(e -> actionUp(e));
		btnDown.addActionListener(e -> actionDown(e));
		btnDelete.addActionListener(e -> actionDelete(e));
	}

	/**
	 * Action New
	 * 
	 * @param e ActionEvent
	 */
	protected abstract void actionNew(ActionEvent e);

	/**
	 * Action Edit
	 * 
	 * @param e ActionEvent
	 */
	protected abstract void actionEdit(ActionEvent e);

	/**
	 * Action Up
	 * 
	 * @param e ActionEvent
	 */
	protected abstract void actionUp(ActionEvent e);

	/**
	 * Action Down
	 * 
	 * @param e ActionEvent
	 */
	protected abstract void actionDown(ActionEvent e);

	/**
	 * Action Delete
	 * 
	 * @param e ActionEvent
	 */
	protected abstract void actionDelete(ActionEvent e);
}

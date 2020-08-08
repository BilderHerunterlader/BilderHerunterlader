package ch.supertomcat.bh.gui.rules.editor.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import ch.supertomcat.supertomcatutils.gui.layout.SpringUtilities;

/**
 * Panel with buttons
 */
public abstract class RuleEditorButtonPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * List of Buttons
	 */
	protected final List<JButton> buttons = new ArrayList<>();

	/**
	 * Constructor
	 */
	public RuleEditorButtonPanel() {
		this((List<JButton>)null);
	}

	/**
	 * Constructor
	 * 
	 * @param buttons Buttons
	 */
	public RuleEditorButtonPanel(JButton... buttons) {
		this(Arrays.asList(buttons));
	}

	/**
	 * Constructor
	 * 
	 * @param buttons Buttons
	 */
	public RuleEditorButtonPanel(List<JButton> buttons) {
		setLayout(new SpringLayout());

		if (buttons != null) {
			addButtons(buttons);
		}
	}

	/**
	 * Update Layout
	 */
	protected void updateLayout() {
		SpringUtilities.makeCompactGrid(this, buttons.size(), 1, 0, 0, 5, 5);
	}

	/**
	 * Add Button
	 * 
	 * @param button Button
	 */
	public void addButton(JButton button) {
		buttons.add(button);
		add(button);
		updateLayout();
	}

	/**
	 * Add Buttons
	 * 
	 * @param buttons Buttons
	 */
	public void addButtons(List<JButton> buttons) {
		for (JButton button : buttons) {
			addButton(button);
		}
	}

	/**
	 * Add Buttons
	 * 
	 * @param buttons Buttons
	 */
	public void addButtons(JButton... buttons) {
		addButtons(Arrays.asList(buttons));
	}

	/**
	 * Remove Button
	 * 
	 * @param button Button
	 */
	public void removeButton(JButton button) {
		buttons.remove(button);
		remove(button);
		updateLayout();
	}

	/**
	 * Remove Buttons
	 * 
	 * @param buttons Buttons
	 */
	public void removeButtons(List<JButton> buttons) {
		for (JButton button : buttons) {
			removeButton(button);
		}
	}

	/**
	 * Remove Buttons
	 * 
	 * @param buttons Buttons
	 */
	public void removeButtons(JButton... buttons) {
		removeButtons(Arrays.asList(buttons));
	}

	/**
	 * Enable Buttons
	 */
	public void enableButtons() {
		for (JButton button : buttons) {
			button.setEnabled(true);
		}
	}

	/**
	 * Disable Buttons
	 */
	public void disableButtons() {
		for (JButton button : buttons) {
			button.setEnabled(false);
		}
	}
}

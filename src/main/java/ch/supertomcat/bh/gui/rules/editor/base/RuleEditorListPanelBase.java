package ch.supertomcat.bh.gui.rules.editor.base;

import java.awt.BorderLayout;
import java.util.function.Supplier;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * Panel with buttons to edit a list
 * 
 * @param <E> Element Type
 * @param <T> Table Model Type
 * @param <B> Button Panel Type
 */
public class RuleEditorListPanelBase<E, T extends DefaultListModel<E>, B extends RuleEditorButtonPanel> extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * List Model
	 */
	protected final T model;

	/**
	 * List
	 */
	protected final JList<E> list;

	/**
	 * ScrollPane
	 */
	protected final JScrollPane scrollPane;

	/**
	 * Supplier for action new
	 */
	protected final Supplier<E> actionNewSupplier;

	/**
	 * Button Panel
	 */
	protected final B buttonPanel;

	/**
	 * Constructor
	 * 
	 * @param model Table Model
	 * @param actionNewSupplier Supplier for action new
	 * @param buttonPanel Button Panel
	 */
	public RuleEditorListPanelBase(T model, Supplier<E> actionNewSupplier, B buttonPanel) {
		this.model = model;
		this.actionNewSupplier = actionNewSupplier;
		this.list = new JList<>(model);
		this.scrollPane = new JScrollPane(this.list);
		this.buttonPanel = buttonPanel;

		this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.list.setVisibleRowCount(10);

		initLayout();
	}

	/**
	 * Initialize Layout
	 */
	protected void initLayout() {
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.EAST);
	}

	/**
	 * Action New
	 */
	protected void actionNew() {
		E element = actionNewSupplier.get();
		if (element != null) {
			model.addElement(element);
		}
	}

	/**
	 * Action Edit
	 */
	protected void actionEdit() {
		// Nothing to do, can be overridden if necessary
	}

	/**
	 * Action Up
	 */
	protected void actionUp() {
		int row = list.getSelectedIndex();
		if (row < 0) {
			return;
		}

		if (row > 0) {
			swap(row, row - 1);
			list.setSelectedIndex(row - 1);
		}
	}

	/**
	 * Action Down
	 */
	protected void actionDown() {
		int row = list.getSelectedIndex();
		if (row < 0) {
			return;
		}

		if (row > -1 && row < model.getSize() - 1) {
			swap(row, row + 1);
			list.setSelectedIndex(row + 1);
		}
	}

	/**
	 * Swap Elements
	 * 
	 * @param index1 First Element Index
	 * @param index2 Second Element Index
	 */
	protected void swap(int index1, int index2) {
		E element1 = model.getElementAt(index1);
		E element2 = model.getElementAt(index2);
		model.set(index1, element2);
		model.set(index2, element1);
	}

	/**
	 * Action Delete
	 */
	protected void actionDelete() {
		int row = list.getSelectedIndex();
		if (row < 0) {
			return;
		}

		model.remove(row);
		if (model.getSize() > 0) {
			if (model.getSize() > row) {
				list.setSelectedIndex(row);
			} else {
				list.setSelectedIndex(row - 1);
			}
		}
	}

	/**
	 * Enable List
	 */
	public void enableList() {
		list.setEnabled(true);
		buttonPanel.enableButtons();
	}

	/**
	 * Disable List
	 */
	public void disableList() {
		list.setEnabled(false);
		buttonPanel.disableButtons();
	}
}

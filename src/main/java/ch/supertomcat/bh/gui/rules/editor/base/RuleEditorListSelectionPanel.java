package ch.supertomcat.bh.gui.rules.editor.base;

import java.awt.BorderLayout;
import java.util.function.Supplier;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Panel with buttons to edit list, but uses a separate component for editing a single element, which is displayed, when a element is selected
 * 
 * @param <E> Element Type
 * @param <T> Table Model Type
 */
public class RuleEditorListSelectionPanel<E extends JComponent, T extends DefaultListModel<E>> extends RuleEditorListPanelBase<E, T, RuleEditorDefaultButtonPanel> {
	private static final long serialVersionUID = 1L;

	/**
	 * Current Displayed Element
	 */
	private JComponent currentDisplayedElement = null;

	/**
	 * Constructor
	 * 
	 * @param model Table Model
	 * @param actionNewSupplier Supplier for action new
	 */
	public RuleEditorListSelectionPanel(T model, Supplier<E> actionNewSupplier) {
		super(model, actionNewSupplier, new RuleEditorDefaultButtonPanel(false));

		buttonPanel.addNewActionListener(e -> actionNew());
		buttonPanel.addEditActionListener(e -> actionEdit());
		buttonPanel.addUpActionListener(e -> actionUp());
		buttonPanel.addDownActionListener(e -> actionDown());
		buttonPanel.addDeleteActionListener(e -> actionDelete());

		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (model.size() > 0) {
					int selectedIndex = list.getSelectedIndex();
					if (selectedIndex > -1) {
						displayElement(model.get(selectedIndex));
					} else {
						clearDisplayedElement();
					}
				} else {
					clearDisplayedElement();
				}
				revalidate();
				repaint();
			}
		});

		if (model.getSize() > 0) {
			list.setSelectedIndex(0);
		}
	}

	@Override
	protected void initLayout() {
		setLayout(new BorderLayout());

		JPanel pnlList = new JPanel();
		pnlList.setLayout(new BorderLayout());
		pnlList.add(scrollPane, BorderLayout.CENTER);
		pnlList.add(buttonPanel, BorderLayout.EAST);
		add(pnlList, BorderLayout.WEST);
	}

	/**
	 * Display Element
	 * 
	 * @param element Element
	 */
	private void displayElement(E element) {
		if (currentDisplayedElement != null) {
			remove(currentDisplayedElement);
		}
		add(element, BorderLayout.CENTER);
		currentDisplayedElement = element;
	}

	/**
	 * Clear displayed element
	 */
	private void clearDisplayedElement() {
		if (currentDisplayedElement != null) {
			remove(currentDisplayedElement);
		}
		currentDisplayedElement = null;
	}
}

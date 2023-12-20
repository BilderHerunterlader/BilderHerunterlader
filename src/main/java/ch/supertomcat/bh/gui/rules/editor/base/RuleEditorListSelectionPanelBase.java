package ch.supertomcat.bh.gui.rules.editor.base;

import java.awt.BorderLayout;
import java.util.function.Supplier;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Panel with buttons to edit list, but uses a separate component for editing a single element, which is displayed, when a element is selected
 * 
 * @param <E> Element Type
 * @param <T> Table Model Type
 * @param <B> Button Panel Type
 */
public abstract class RuleEditorListSelectionPanelBase<E extends JComponent, T extends DefaultListModel<E>, B extends RuleEditorButtonPanel> extends RuleEditorListPanelBase<E, T, B> {
	private static final long serialVersionUID = 1L;

	/**
	 * Current Displayed Element
	 */
	protected E currentDisplayedElement = null;

	/**
	 * Constructor
	 * 
	 * @param model Table Model
	 * @param actionNewSupplier Supplier for action new
	 * @param buttonPanel Button Panel
	 * @param renderer Renderer or null
	 */
	public RuleEditorListSelectionPanelBase(T model, Supplier<E> actionNewSupplier, B buttonPanel, ListCellRenderer<? super E> renderer) {
		super(model, actionNewSupplier, buttonPanel, renderer);

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
	protected void displayElement(E element) {
		if (currentDisplayedElement != null) {
			remove(currentDisplayedElement);
		}
		add(element, BorderLayout.CENTER);
		currentDisplayedElement = element;
	}

	/**
	 * Clear displayed element
	 */
	protected void clearDisplayedElement() {
		if (currentDisplayedElement != null) {
			remove(currentDisplayedElement);
		}
		currentDisplayedElement = null;
	}
}

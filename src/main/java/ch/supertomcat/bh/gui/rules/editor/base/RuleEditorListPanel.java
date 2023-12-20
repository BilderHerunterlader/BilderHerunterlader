package ch.supertomcat.bh.gui.rules.editor.base;

import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;

/**
 * Panel with buttons to edit a list
 * 
 * @param <E> Element Type
 * @param <T> Table Model Type
 */
public class RuleEditorListPanel<E, T extends DefaultListModel<E>> extends RuleEditorListPanelBase<E, T, RuleEditorDefaultButtonPanel> {
	private static final long serialVersionUID = 1L;

	/**
	 * Function for action edit
	 */
	protected final Function<E, E> actionEditFunction;

	/**
	 * Constructor
	 * 
	 * @param model Table Model
	 * @param actionNewSupplier Supplier for action new
	 * @param actionEditFunction Function for action edit
	 * @param renderer Renderer or null
	 */
	public RuleEditorListPanel(T model, Supplier<E> actionNewSupplier, Function<E, E> actionEditFunction, ListCellRenderer<? super E> renderer) {
		super(model, actionNewSupplier, new RuleEditorDefaultButtonPanel(true), renderer);
		this.actionEditFunction = actionEditFunction;

		buttonPanel.addNewActionListener(e -> actionNew());
		buttonPanel.addEditActionListener(e -> actionEdit());
		buttonPanel.addUpActionListener(e -> actionUp());
		buttonPanel.addDownActionListener(e -> actionDown());
		buttonPanel.addDeleteActionListener(e -> actionDelete());
	}

	@Override
	protected void actionEdit() {
		int row = list.getSelectedIndex();
		if (row < 0) {
			return;
		}

		E originalElement = model.get(row);
		E changedElement = actionEditFunction.apply(originalElement);
		if (changedElement == null) {
			return;
		}
		model.set(row, changedElement);
	}
}

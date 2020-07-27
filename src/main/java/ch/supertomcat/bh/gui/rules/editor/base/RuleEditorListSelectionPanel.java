package ch.supertomcat.bh.gui.rules.editor.base;

import java.util.function.Supplier;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.ListCellRenderer;

/**
 * Panel with buttons to edit list, but uses a separate component for editing a single element, which is displayed, when a element is selected
 * 
 * @param <E> Element Type
 * @param <T> Table Model Type
 */
public class RuleEditorListSelectionPanel<E extends JComponent, T extends DefaultListModel<E>> extends RuleEditorListSelectionPanelBase<E, T, RuleEditorDefaultButtonPanel> {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param model Table Model
	 * @param actionNewSupplier Supplier for action new
	 * @param renderer Renderer or null
	 */
	public RuleEditorListSelectionPanel(T model, Supplier<E> actionNewSupplier, ListCellRenderer<? super E> renderer) {
		super(model, actionNewSupplier, new RuleEditorDefaultButtonPanel(false), renderer);

		buttonPanel.addNewActionListener(e -> actionNew());
		buttonPanel.addEditActionListener(e -> actionEdit());
		buttonPanel.addUpActionListener(e -> actionUp());
		buttonPanel.addDownActionListener(e -> actionDown());
		buttonPanel.addDeleteActionListener(e -> actionDelete());
	}
}

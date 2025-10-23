package ch.supertomcat.bh.gui.renderer;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.bh.gui.BHIcons;
import ch.supertomcat.bh.update.containers.UpdateActionType;
import ch.supertomcat.bh.update.containers.UpdateType;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;

/**
 * Renderer to display icons for the update action type
 */
public class UpdateColumnRenderer extends DefaultStringColorRowRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	private static final Icon ACTION_NONE_ICON = Icons.getApplIcon("dummy.png", 16);

	private static final Icon ACTION_NEW_ICON = Icons.getTangoSVGIcon("actions/list-add.svg", 16);

	private static final Icon ACTION_UPDATE_ICON = Icons.getTangoSVGIcon("apps/system-software-update.svg", 16);

	private static final Icon ACTION_REMOVE_ICON = Icons.getTangoSVGIcon("actions/list-remove.svg", 16);

	private static final Icon RULE_ICON = BHIcons.getBHSVGIcon("rule.svg", 22);

	private static final Icon HOST_PLUGIN_ICON = BHIcons.getBHSVGIcon("hostplugin.svg", 22);

	/**
	 * Constructor
	 */
	public UpdateColumnRenderer() {
		setOpaque(true);
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	public void prepareValueText(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof UpdateActionType updateActionType) {
			label.setText("");
			label.setToolTipText("");
			switch (updateActionType) {
				case ACTION_NEW:
					label.setIcon(ACTION_NEW_ICON);
					break;
				case ACTION_UPDATE:
					label.setIcon(ACTION_UPDATE_ICON);
					break;
				case ACTION_REMOVE:
					label.setIcon(ACTION_REMOVE_ICON);
					break;
				case ACTION_NONE:
				default:
					label.setIcon(ACTION_NONE_ICON);
					break;
			}
		} else if (value instanceof UpdateType updateType) {
			label.setText(updateType.getName());
			label.setToolTipText(updateType.getName());
			switch (updateType) {
				case TYPE_HOST_PLUGIN, TYPE_REDIRECT_PLUGIN:
					label.setIcon(HOST_PLUGIN_ICON);
					break;
				case TYPE_RULE:
					label.setIcon(RULE_ICON);
					break;
				case TYPE_BH:
				default:
					label.setIcon(ACTION_NONE_ICON);
					break;
			}
		} else {
			super.prepareValueText(label, table, value, isSelected, hasFocus, row, column);
		}
	}
}

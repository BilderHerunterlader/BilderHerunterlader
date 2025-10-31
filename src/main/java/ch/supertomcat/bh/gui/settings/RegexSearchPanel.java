package ch.supertomcat.bh.gui.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.layout.SpringUtilities;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;
import ch.supertomcat.supertomcatutils.regex.RegexSearch;

/**
 * Rule-Pipeline-Panel
 */
public class RegexSearchPanel extends JPanel {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Table
	 */
	private JTable table;

	/**
	 * ScrollPane
	 */
	private JScrollPane sp;

	/**
	 * TableModel
	 */
	private RegexSearchReplaceTableModel model = new RegexSearchReplaceTableModel(true);

	/**
	 * Button
	 */
	private JButton btnNew = new JButton(Localization.getString("New"), Icons.getTangoSVGIcon("actions/document-new.svg", 16));

	/**
	 * Button
	 */
	private JButton btnEdit = new JButton(Localization.getString("Edit"), Icons.getTangoSVGIcon("apps/accessories-text-editor.svg", 16));

	/**
	 * Button
	 */
	private JButton btnUp = new JButton(Localization.getString("Up"), Icons.getTangoSVGIcon("actions/go-up.svg", 16));

	/**
	 * Button
	 */
	private JButton btnDown = new JButton(Localization.getString("Down"), Icons.getTangoSVGIcon("actions/go-down.svg", 16));

	/**
	 * Button
	 */
	private JButton btnDelete = new JButton(Localization.getString("Delete"), Icons.getTangoSVGIcon("actions/edit-delete.svg", 16));

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * Constructor
	 * 
	 * @param regexSearchList Regex Search List
	 */
	public RegexSearchPanel(List<RegexSearch> regexSearchList) {
		setLayout(new BorderLayout());

		table = new JTable(model);

		TableUtil.internationalizeColumns(table);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Object.class, new DefaultStringColorRowRenderer());
		table.getTableHeader().setReorderingAllowed(false);
		table.setRowHeight(TableUtil.calculateRowHeight(table, false, true));

		regexSearchList.stream().forEachOrdered(x -> model.addRow(x.getSearch(), null));

		Dimension preferredScrollableTableSize = new Dimension(table.getPreferredScrollableViewportSize().width, 15 * table.getRowHeight());
		table.setPreferredScrollableViewportSize(preferredScrollableTableSize);

		sp = new JScrollPane(table);

		add(sp, BorderLayout.CENTER);

		pnlButtons.setLayout(new SpringLayout());
		pnlButtons.add(btnNew);
		pnlButtons.add(btnEdit);
		pnlButtons.add(btnUp);
		pnlButtons.add(btnDown);
		pnlButtons.add(btnDelete);
		SpringUtilities.makeCompactGrid(pnlButtons, 5, 1, 0, 0, 5, 5);
		add(pnlButtons, BorderLayout.EAST);

		btnNew.addActionListener(e -> {
			JDialog parent = null;
			if (this.getTopLevelAncestor() instanceof JDialog dialog) {
				parent = dialog;
			}
			RegexSearch regexSearch = new RegexSearch();
			RegexSearchRegexpEditor rme = new RegexSearchRegexpEditor(parent, regexSearch);
			if (rme.getCanceled()) {
				return;
			}
			model.addRow(regexSearch.getSearch(), null);
		});
		btnEdit.addActionListener(e -> {
			int row = table.getSelectedRow();
			if (row < 0) {
				return;
			}
			JDialog parent = null;
			if (this.getTopLevelAncestor() instanceof JDialog dialog) {
				parent = dialog;
			}
			RegexSearch regexSearch = new RegexSearch((String)model.getValueAt(row, 0));
			RegexSearchRegexpEditor rme = new RegexSearchRegexpEditor(parent, regexSearch);
			if (rme.getCanceled()) {
				return;
			}
			model.setValueAt(regexSearch.getSearch(), row, 0);
		});
		btnUp.addActionListener(e -> {
			int row = table.getSelectedRow();
			if (row > 0) {
				model.moveRow(row, row, row - 1);
				table.setRowSelectionInterval(row - 1, row - 1);
			}
		});
		btnDown.addActionListener(e -> {
			int row = table.getSelectedRow();
			if (row > -1 && row < model.getRowCount() - 1) {
				model.moveRow(row, row, row + 1);
				table.setRowSelectionInterval(row + 1, row + 1);
			}
		});
		btnDelete.addActionListener(e -> {
			int row = table.getSelectedRow();
			if (row < 0) {
				return;
			}
			model.removeRow(row);
		});
	}

	/**
	 * @return Regex Search list
	 */
	public List<RegexSearch> getRegexSearches() {
		List<RegexSearch> regexSearches = new ArrayList<>();
		for (int i = 0; i < model.getRowCount(); i++) {
			String search = (String)model.getValueAt(i, 0);
			regexSearches.add(new RegexSearch(search));
		}
		return regexSearches;
	}
}

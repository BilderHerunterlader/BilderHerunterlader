package ch.supertomcat.bh.gui.rules;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.gui.SpringUtilities;
import ch.supertomcat.bh.pic.PicState;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.RulePipeline;
import ch.supertomcat.bh.rules.RuleRegExp;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;

/**
 * Rule-Pipeline-Panel
 */
public class RulePipelineFailuresPanel extends JPanel implements ActionListener, TableColumnModelListener {
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
	private RulePipelineFailuresTableModel model = new RulePipelineFailuresTableModel();

	/**
	 * RulePipeline
	 */
	private RulePipeline pipe = null;

	/**
	 * Rule
	 */
	@SuppressWarnings("unused")
	private Rule rule = null;

	private JComboBox<PicState> cmbFailureType = new JComboBox<>();

	private JCheckBox cbURL = new JCheckBox(Localization.getString("ContainerURL"));

	private JCheckBox cbThumbURL = new JCheckBox(Localization.getString("ThumbnailURL"));

	private JCheckBox cbContainerPage = new JCheckBox(Localization.getString("ContainerPage"));

	/**
	 * Panel
	 */
	private JPanel pnlRB = new JPanel();

	/**
	 * Button
	 */
	private JButton btnNew = new JButton(Localization.getString("New"), Icons.getTangoIcon("actions/document-new.png", 16));

	/**
	 * Button
	 */
	private JButton btnEdit = new JButton(Localization.getString("Edit"), Icons.getTangoIcon("apps/accessories-text-editor.png", 16));

	/**
	 * Button
	 */
	private JButton btnUp = new JButton(Localization.getString("Up"), Icons.getTangoIcon("actions/go-up.png", 16));

	/**
	 * Button
	 */
	private JButton btnDown = new JButton(Localization.getString("Down"), Icons.getTangoIcon("actions/go-down.png", 16));

	/**
	 * Button
	 */
	private JButton btnDelete = new JButton(Localization.getString("Delete"), Icons.getTangoIcon("actions/edit-delete.png", 16));

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * Owner
	 */
	private JDialog owner = null;

	/**
	 * Constructor
	 * 
	 * @param rule Rule
	 * @param pipe Pipeline
	 * @param owner Owner
	 */
	public RulePipelineFailuresPanel(Rule rule, RulePipeline pipe, JDialog owner) {
		super();
		this.owner = owner;
		this.rule = rule;
		this.pipe = pipe;
		setLayout(new BorderLayout());

		TitledBorder brd = BorderFactory.createTitledBorder(Localization.getString("Pipeline"));
		this.setBorder(brd);

		table = new JTable(model);

		TableUtil.internationalizeColumns(table);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Object.class, new DefaultStringColorRowRenderer());
		updateColWidthsFromSettingsManager();
		table.getColumnModel().addColumnModelListener(this);
		table.getTableHeader().setReorderingAllowed(false);
		table.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		table.setRowHeight(TableUtil.calculateRowHeight(table, false, true));

		Iterator<RuleRegExp> it = pipe.getRegexps().iterator();
		while (it.hasNext()) {
			RuleRegExp rre = it.next();
			model.addRow(rre.getSearch());
		}
		sp = new JScrollPane(table);

		cmbFailureType.addItem(PicState.FAILED);
		cmbFailureType.addItem(PicState.FAILED_FILE_NOT_EXIST);
		cmbFailureType.addItem(PicState.FAILED_FILE_TEMPORARY_OFFLINE);
		cmbFailureType.addItem(PicState.SLEEPING);
		cmbFailureType.addItem(PicState.COMPLETE);
		cmbFailureType.setSelectedItem(pipe.getFailureType());

		cbURL.setSelected(pipe.isCheckURL());
		cbThumbURL.setSelected(pipe.isCheckThumbURL());
		cbContainerPage.setSelected(pipe.isCheckPageSourceCode());

		pnlRB.add(cmbFailureType);
		pnlRB.add(cbURL);
		pnlRB.add(cbThumbURL);
		pnlRB.add(cbContainerPage);

		add(pnlRB, BorderLayout.NORTH);

		add(sp, BorderLayout.CENTER);

		pnlButtons.setLayout(new SpringLayout());
		pnlButtons.add(btnNew);
		pnlButtons.add(btnEdit);
		pnlButtons.add(btnUp);
		pnlButtons.add(btnDown);
		pnlButtons.add(btnDelete);
		SpringUtilities.makeCompactGrid(pnlButtons, 5, 1, 0, 0, 5, 5);
		add(pnlButtons, BorderLayout.EAST);

		btnNew.addActionListener(this);
		btnEdit.addActionListener(this);
		btnUp.addActionListener(this);
		btnDown.addActionListener(this);
		btnDelete.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnNew) {
			RuleRegExp rre = new RuleRegExp();
			RuleRegexpEditor rme = new RuleRegexpEditor(owner, rre, true);
			if (rme.getCanceled()) {
				return;
			}
			model.addRow(rre.getSearch());
			pipe.addRegExp(rre);
		} else if (e.getSource() == btnEdit) {
			int row = table.getSelectedRow();
			if (row < 0) {
				return;
			}
			RuleRegExp rre = pipe.getRegexp(row);
			RuleRegexpEditor rme = new RuleRegexpEditor(owner, rre, true);
			if (rme.getCanceled()) {
				return;
			}
			model.setValueAt(rre.getSearch(), row, 0);
		} else if (e.getSource() == btnDelete) {
			int row = table.getSelectedRow();
			if (row < 0) {
				return;
			}
			model.removeRow(row);
			pipe.removeRegExp(row);
		} else if (e.getSource() == btnUp) {
			int row = table.getSelectedRow();
			if (row > 0) {
				pipe.swapRegExp(row, row - 1);
				model.moveRow(row, row, row - 1);
				table.setRowSelectionInterval(row - 1, row - 1);
			}
		} else if (e.getSource() == btnDown) {
			int row = table.getSelectedRow();
			if (row > -1 && row < model.getRowCount() - 1) {
				pipe.swapRegExp(row, row + 1);
				model.moveRow(row, row, row + 1);
				table.setRowSelectionInterval(row + 1, row + 1);
			}
		}
	}

	/**
	 * Apply
	 */
	public void apply() {
		pipe.setFailureType((PicState)cmbFailureType.getSelectedItem());
		pipe.setCheckURL(cbURL.isSelected());
		pipe.setCheckThumbURL(cbThumbURL.isSelected());
		pipe.setCheckPageSourceCode(cbContainerPage.isSelected());
	}

	/**
	 * updateColWidthsToSettingsManager
	 */
	private void updateColWidthsToSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false) {
			return;
		}
		SettingsManager.instance().setColWidthsRulesEditor(TableUtil.serializeColWidthSetting(table));
		SettingsManager.instance().writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false) {
			return;
		}
		TableUtil.applyColWidths(table, SettingsManager.instance().getColWidthsRulesEditor());
	}

	@Override
	public void columnAdded(TableColumnModelEvent e) {
	}

	@Override
	public void columnMarginChanged(ChangeEvent e) {
		updateColWidthsToSettingsManager();
	}

	@Override
	public void columnMoved(TableColumnModelEvent e) {
	}

	@Override
	public void columnRemoved(TableColumnModelEvent e) {
	}

	@Override
	public void columnSelectionChanged(ListSelectionEvent e) {
	}
}

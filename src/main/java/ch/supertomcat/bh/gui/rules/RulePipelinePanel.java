package ch.supertomcat.bh.gui.rules;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.RuleMode;
import ch.supertomcat.bh.rules.RulePipeline;
import ch.supertomcat.bh.rules.RuleRegExp;
import ch.supertomcat.bh.rules.RuleURLMode;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;

/**
 * Rule-Pipeline-Panel
 */
public class RulePipelinePanel extends JPanel implements IRulePipelineURLPanel, ActionListener {
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
	private RulePipelineTableModel model = new RulePipelineTableModel();

	/**
	 * RulePipeline
	 */
	private RulePipeline pipe = null;

	/**
	 * Rule
	 */
	@SuppressWarnings("unused")
	private Rule rule = null;

	/**
	 * Mode
	 */
	private RuleMode mode = RuleMode.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL;

	/**
	 * ButtonGroup
	 */
	private ButtonGroup bgMode = new ButtonGroup();

	/**
	 * RadioButton
	 */
	private JRadioButton rbModeZero = new JRadioButton(Localization.getString("RuleModeZeroShort"), true);

	/**
	 * RadioButton
	 */
	private JRadioButton rbModeOne = new JRadioButton(Localization.getString("RuleModeOneShort"), false);

	/**
	 * URL-Mode
	 */
	private RuleURLMode urlMode = RuleURLMode.RULEPIPELINE_MODE_CONTAINER_URL;

	/**
	 * ButtonGroup
	 */
	private ButtonGroup bgURLMode = new ButtonGroup();

	/**
	 * RadioButton
	 */
	private JRadioButton rbURLModeZero = new JRadioButton(Localization.getString("RuleURLModeZero"), true);

	/**
	 * RadioButton
	 */
	private JRadioButton rbURLModeOne = new JRadioButton(Localization.getString("RuleURLModeOne"), false);

	/**
	 * lblWaitBeforeExecute
	 */
	private JLabel lblWaitBeforeExecute = new JLabel(Localization.getString("RulePipelineWaitBeforeExecute"));

	/**
	 * txtWaitBeforeExecute
	 */
	private JTextField txtWaitBeforeExecute = new JTextField(5);

	private JCheckBox chkURLDecodeResult = new JCheckBox(Localization.getString("RulePipelineURLDecodeResult"), false);

	private JCheckBox chkSendCookies = new JCheckBox(Localization.getString("RulePipelineSendCookies"), true);

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
	 * @param mode Mode
	 * @param rule Rule
	 * @param pipe Pipeline
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 */
	public RulePipelinePanel(RuleMode mode, Rule rule, RulePipeline pipe, JDialog owner, SettingsManager settingsManager) {
		super();
		this.owner = owner;
		this.rule = rule;
		this.pipe = pipe;
		this.mode = mode;
		this.urlMode = pipe.getURLMode();
		setLayout(new BorderLayout());

		TitledBorder brd = BorderFactory.createTitledBorder(Localization.getString("Pipeline"));
		this.setBorder(brd);

		table = new JTable(model);

		TableUtil.internationalizeColumns(table);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Object.class, new DefaultStringColorRowRenderer());
		updateColWidthsFromSettingsManager(settingsManager);
		table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnAdded(TableColumnModelEvent e) {
			}

			@Override
			public void columnMarginChanged(ChangeEvent e) {
				updateColWidthsToSettingsManager(settingsManager);
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
		});
		table.getTableHeader().setReorderingAllowed(false);
		table.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		table.setRowHeight(TableUtil.calculateRowHeight(table, false, true));

		Iterator<RuleRegExp> it = pipe.getRegexps().iterator();
		while (it.hasNext()) {
			RuleRegExp rre = it.next();
			model.addRow(rre.getSearch(), rre.getReplace());
		}

		Dimension preferredScrollableTableSize = new Dimension(table.getPreferredScrollableViewportSize().width, 15 * table.getRowHeight());
		table.setPreferredScrollableViewportSize(preferredScrollableTableSize);
		sp = new JScrollPane(table);

		boolean bMode = (mode == RuleMode.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL);
		rbModeZero.setSelected(bMode);
		rbModeOne.setSelected(!bMode);
		bgMode.add(rbModeZero);
		bgMode.add(rbModeOne);

		rbURLModeZero.setEnabled(bMode);
		rbURLModeOne.setEnabled(bMode);

		bgURLMode.add(rbURLModeZero);
		bgURLMode.add(rbURLModeOne);

		if (this.mode == RuleMode.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL) {
			boolean b = (urlMode == RuleURLMode.RULEPIPELINE_MODE_CONTAINER_URL);
			rbURLModeZero.setSelected(b);
			rbURLModeOne.setSelected(!b);
		}

		txtWaitBeforeExecute.setText(String.valueOf(this.pipe.getWaitBeforeExecute()));

		chkURLDecodeResult.setToolTipText(Localization.getString("RulePipelineURLDecodeResultToolTip"));
		chkURLDecodeResult.setSelected(this.pipe.isUrlDecodeResult());

		chkSendCookies.setSelected(this.pipe.isSendCookies());

		pnlRB.setLayout(new GridLayout(4, 3));
		pnlRB.add(rbModeZero);
		pnlRB.add(rbURLModeZero);
		pnlRB.add(lblWaitBeforeExecute);
		pnlRB.add(rbModeOne);
		pnlRB.add(rbURLModeOne);
		pnlRB.add(txtWaitBeforeExecute);
		pnlRB.add(new JLabel());
		pnlRB.add(new JLabel());
		pnlRB.add(chkURLDecodeResult);
		pnlRB.add(new JLabel());
		pnlRB.add(new JLabel());
		pnlRB.add(chkSendCookies);
		add(pnlRB, BorderLayout.NORTH);

		rbModeOne.addActionListener(this);
		rbModeZero.addActionListener(this);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnNew) {
			RuleRegExp rre = new RuleRegExp();
			RuleRegexpEditor rme = new RuleRegexpEditor(owner, rre);
			if (rme.getCanceled()) {
				return;
			}
			model.addRow(rre.getSearch(), rre.getReplace());
			pipe.addRegExp(rre);
		} else if (e.getSource() == btnEdit) {
			int row = table.getSelectedRow();
			if (row < 0) {
				return;
			}
			RuleRegExp rre = pipe.getRegexp(row);
			RuleRegexpEditor rme = new RuleRegexpEditor(owner, rre);
			if (rme.getCanceled()) {
				return;
			}
			model.setValueAt(rre.getSearch(), row, 0);
			model.setValueAt(rre.getReplace(), row, 1);
		} else if (e.getSource() == btnDelete) {
			int row = table.getSelectedRow();
			if (row < 0) {
				return;
			}
			model.removeRow(row);
			pipe.removeRegExp(row);
		} else if (e.getSource() == rbModeZero || e.getSource() == rbModeOne) {
			rbURLModeZero.setEnabled(rbModeZero.isSelected());
			rbURLModeOne.setEnabled(rbModeZero.isSelected());
			mode = rbModeZero.isSelected() ? RuleMode.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL : RuleMode.RULE_MODE_CONTAINER_PAGE_SOURCECODE;
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
	@Override
	public void apply() {
		pipe.setMode(mode);
		if (this.mode == RuleMode.RULE_MODE_CONTAINER_OR_THUMBNAIL_URL) {
			if (rbURLModeZero.isSelected()) {
				pipe.setURLMode(RuleURLMode.RULEPIPELINE_MODE_CONTAINER_URL);
			} else {
				pipe.setURLMode(RuleURLMode.RULEPIPELINE_MODE_THUMBNAIL_URL);
			}
		}
		int waitBeforeExecute = 0;
		try {
			waitBeforeExecute = Integer.parseInt(txtWaitBeforeExecute.getText());
		} catch (NumberFormatException nfe) {
		}
		pipe.setWaitBeforeExecute(waitBeforeExecute);
		pipe.setUrlDecodeResult(chkURLDecodeResult.isSelected());
		pipe.setSendCookies(chkSendCookies.isSelected());
	}

	/**
	 * updateColWidthsToSettingsManager
	 * 
	 * @param settingsManager SettingsManager
	 */
	private void updateColWidthsToSettingsManager(SettingsManager settingsManager) {
		if (settingsManager.isSaveTableColumnSizes() == false) {
			return;
		}
		settingsManager.setColWidthsRulesEditor(TableUtil.serializeColWidthSetting(table));
		settingsManager.writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 * 
	 * @param settingsManager SettingsManager
	 */
	private void updateColWidthsFromSettingsManager(SettingsManager settingsManager) {
		if (settingsManager.isSaveTableColumnSizes() == false) {
			return;
		}
		TableUtil.applyColWidths(table, settingsManager.getColWidthsRulesEditor());
	}
}

package ch.supertomcat.bh.gui.settings.tabs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.settings.SubdirCellEditor;
import ch.supertomcat.bh.gui.settings.SubdirResolutionCellEditor;
import ch.supertomcat.bh.gui.settings.SubdirResolutionCellEditorComponent;
import ch.supertomcat.bh.gui.settings.SubdirsResolutionModeComboBoxRenderer;
import ch.supertomcat.bh.gui.settings.SubdirsTableModel;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.options.Subdir;
import ch.supertomcat.bh.settings.xml.DirectorySettings;
import ch.supertomcat.bh.settings.xml.Settings;
import ch.supertomcat.bh.settings.xml.SubdirsResolutionMode;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.dialog.FileDialogUtil;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;
import ch.supertomcat.supertomcatutils.gui.layout.SpringUtilities;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultNumberColorRowRenderer;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Paths Settings Panel
 */
public class PathsSettingsPanel extends SettingsPanelBase {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Label
	 */
	private JLabel lblStdSavePath = new JLabel(Localization.getString("StdSavePath"));

	/**
	 * TextField
	 */
	private final JTextField txtStdSavePath;

	/**
	 * Button
	 */
	private JButton btnStdSavePath = new JButton("...");

	/**
	 * Label
	 */
	private JLabel lblRememberLastUsedPath = new JLabel(Localization.getString("StdSavePath"));

	/**
	 * Checkbox
	 */
	private JCheckBox chkRememberLastUsedPath = new JCheckBox(Localization.getString("RememberLastUsedPath"), false);

	/**
	 * Label
	 */
	private JLabel lblSubdirsEnabled = new JLabel(Localization.getString("Subdirs"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkSubdirsEnabled = new JCheckBox(Localization.getString("SubdirsEnabled"), false);

	/**
	 * Label
	 */
	private JLabel lblSubdirsResolutionMode = new JLabel(Localization.getString("SubdirResolutionMode"));

	/**
	 * ComboBox
	 */
	private JComboBox<SubdirsResolutionMode> cmbSubdirsResolutionMode = new JComboBox<>();

	/**
	 * TableModel
	 */
	private SubdirsTableModel subdirModel = new SubdirsTableModel();

	/**
	 * Table
	 */
	private JTable jtSubdirs = new JTable(subdirModel);

	/**
	 * Scrollpane
	 */
	private JScrollPane spSubdirs = new JScrollPane(jtSubdirs);

	/**
	 * Panel
	 */
	private JPanel pnlSubdirButtons = new JPanel();

	/**
	 * Button
	 */
	private JButton btnSubdirNew = new JButton(Localization.getString("New"), Icons.getTangoSVGIcon("actions/document-new.svg", 16));

	/**
	 * Button
	 */
	private JButton btnSubdirUp = new JButton(Localization.getString("Up"), Icons.getTangoSVGIcon("actions/go-up.svg", 16));

	/**
	 * Button
	 */
	private JButton btnSubdirDown = new JButton(Localization.getString("Down"), Icons.getTangoSVGIcon("actions/go-down.svg", 16));

	/**
	 * Button
	 */
	private JButton btnSubdirDelete = new JButton(Localization.getString("Delete"), Icons.getTangoSVGIcon("actions/edit-delete.svg", 16));

	/**
	 * Button
	 */
	private JButton btnSubdirHelp = new JButton(Localization.getString("Help"), Icons.getTangoSVGIcon("apps/help-browser.svg", 16));

	/**
	 * PopupMenu
	 */
	private JPopupMenu popupSubdirs = new JPopupMenu();

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemNew = new JMenuItem(Localization.getString("New"), Icons.getTangoSVGIcon("actions/document-new.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemDelete = new JMenuItem(Localization.getString("Delete"), Icons.getTangoSVGIcon("actions/edit-delete.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemUp = new JMenuItem(Localization.getString("Up"), Icons.getTangoSVGIcon("actions/go-up.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemDown = new JMenuItem(Localization.getString("Down"), Icons.getTangoSVGIcon("actions/go-down.svg", 16));

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 */
	public PathsSettingsPanel(SettingsManager settingsManager) {
		super(settingsManager);

		txtStdSavePath = new JTextField(settingsManager.getSavePath(), 40);
		txtStdSavePath.setEditable(false);

		btnStdSavePath.addActionListener(e -> {
			File file = FileDialogUtil.showFolderSaveDialog(this, txtStdSavePath.getText(), null);
			if (file != null) {
				String folder = file.getAbsolutePath() + FileUtil.FILE_SEPERATOR;
				txtStdSavePath.setText(folder);
			}
		});

		cmbSubdirsResolutionMode.addItem(SubdirsResolutionMode.RESOLUTION_ONLY_LOWER);
		cmbSubdirsResolutionMode.addItem(SubdirsResolutionMode.RESOLUTION_ONLY_HIGHER);
		cmbSubdirsResolutionMode.addItem(SubdirsResolutionMode.RESOLUTION_ONLY_WIDTH);
		cmbSubdirsResolutionMode.addItem(SubdirsResolutionMode.RESOLUTION_ONLY_HEIGHT);
		cmbSubdirsResolutionMode.addItem(SubdirsResolutionMode.RESOLUTION_BOTH);
		cmbSubdirsResolutionMode.setRenderer(new SubdirsResolutionModeComboBoxRenderer());

		TableUtil.internationalizeColumns(jtSubdirs);

		jtSubdirs.getColumn("SubdirMinimum").setCellEditor(new SubdirCellEditor());
		jtSubdirs.getColumn("SubdirMaximum").setCellEditor(new SubdirCellEditor());
		jtSubdirs.getColumn("SubdirResolutionMinimum").setCellEditor(new SubdirResolutionCellEditor());
		jtSubdirs.getColumn("SubdirResolutionMaximum").setCellEditor(new SubdirResolutionCellEditor());
		jtSubdirs.setToolTipText(Localization.getString("SubdirsToolTip"));
		jtSubdirs.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == 3) {
					menuItemDelete.setEnabled(jtSubdirs.getSelectedRowCount() > 0);
					popupSubdirs.show(jtSubdirs, e.getX(), e.getY());
				}
			}
		});
		jtSubdirs.setDefaultRenderer(Object.class, new DefaultStringColorRowRenderer());
		jtSubdirs.getColumn("SubdirMinimum").setCellRenderer(new DefaultNumberColorRowRenderer());
		jtSubdirs.getColumn("SubdirMaximum").setCellRenderer(new DefaultNumberColorRowRenderer());
		jtSubdirs.getColumn("SubdirResolutionMinimum").setCellRenderer(new DefaultStringColorRowRenderer());
		jtSubdirs.getColumn("SubdirResolutionMaximum").setCellRenderer(new DefaultStringColorRowRenderer());
		jtSubdirs.setRowHeight(TableUtil.calculateRowHeight(jtSubdirs, true, true));
		Dimension preferredScrollableTableSize = new Dimension(jtSubdirs.getPreferredScrollableViewportSize().width, 5 * jtSubdirs.getRowHeight());
		jtSubdirs.setPreferredScrollableViewportSize(preferredScrollableTableSize);
		jtSubdirs.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);

		spSubdirs.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == 3) {
					menuItemDelete.setEnabled(jtSubdirs.getSelectedRowCount() > 0);
					int h = jtSubdirs.getTableHeader().getSize().height;
					Insets insets = spSubdirs.getInsets();
					h += insets.top + insets.bottom;
					int w = insets.left + insets.right;

					popupSubdirs.show(jtSubdirs, e.getX() - w, e.getY() - h);
				}
			}
		});
		updateColWidthsFromSettingsManager();
		jtSubdirs.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnAdded(TableColumnModelEvent e) {
				// Nothing to do
			}

			@Override
			public void columnMarginChanged(ChangeEvent e) {
				updateColWidthsToSettingsManager();
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
				// Nothing to do
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
				// Nothing to do
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
				// Nothing to do
			}

		});
		jtSubdirs.getTableHeader().setReorderingAllowed(false);
		jtSubdirs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		popupSubdirs.add(menuItemNew);
		popupSubdirs.add(menuItemUp);
		popupSubdirs.add(menuItemDown);
		popupSubdirs.add(menuItemDelete);

		menuItemNew.addActionListener(e -> subdirNew());
		menuItemUp.addActionListener(e -> subdirUp());
		menuItemDown.addActionListener(e -> subdirDown());
		menuItemDelete.addActionListener(e -> subdirDelete());

		pnlSubdirButtons.setLayout(new SpringLayout());
		pnlSubdirButtons.add(btnSubdirNew);
		pnlSubdirButtons.add(btnSubdirUp);
		pnlSubdirButtons.add(btnSubdirDown);
		pnlSubdirButtons.add(btnSubdirDelete);
		pnlSubdirButtons.add(btnSubdirHelp);
		SpringUtilities.makeCompactGrid(pnlSubdirButtons, 5, 1, 0, 0, 5, 5);
		btnSubdirNew.addActionListener(e -> subdirNew());
		btnSubdirUp.addActionListener(e -> subdirUp());
		btnSubdirDown.addActionListener(e -> subdirDown());
		btnSubdirDelete.addActionListener(e -> subdirDelete());

		btnSubdirHelp.addActionListener(e -> JOptionPane.showMessageDialog(this, Localization.getString("SubdirHelp"), Localization.getString("Help"), JOptionPane.INFORMATION_MESSAGE));

		GridBagConstraints gbc = new GridBagConstraints();

		int i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblStdSavePath, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, txtStdSavePath, this);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, btnStdSavePath, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblRememberLastUsedPath, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkRememberLastUsedPath, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblSubdirsEnabled, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, chkSubdirsEnabled, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblSubdirsResolutionMode, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cmbSubdirsResolutionMode, this);
		i++;
		gbc = gblt.getGBC(0, i, 2, 1, 0.9, 0.3);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, spSubdirs, this);
		gbc = gblt.getGBC(2, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, pnlSubdirButtons, this);
	}

	@Override
	public void init() {
		Settings settings = settingsManager.getSettings();

		DirectorySettings directorySettings = settings.getDirectorySettings();

		txtStdSavePath.setText(directorySettings.getSavePath());
		chkRememberLastUsedPath.setSelected(directorySettings.isRememberLastUsedPath());

		chkSubdirsEnabled.setSelected(directorySettings.isSubDirsEnabled());
		cmbSubdirsResolutionMode.setSelectedItem(directorySettings.getSubDirsResolutionMode());

		subdirModel.removeAllRows();
		List<Subdir> v = settingsManager.getSubDirs();
		for (int s = 0; s < v.size(); s++) {
			subdirModel.addRow(v.get(s));
		}
	}

	@Override
	public void applySettings() {
		Settings settings = settingsManager.getSettings();

		DirectorySettings directorySettings = settings.getDirectorySettings();

		directorySettings.setSavePath(txtStdSavePath.getText());
		directorySettings.setRememberLastUsedPath(chkRememberLastUsedPath.isSelected());

		directorySettings.setSubDirsEnabled(chkSubdirsEnabled.isSelected());
		directorySettings.setSubDirsResolutionMode((SubdirsResolutionMode)cmbSubdirsResolutionMode.getSelectedItem());

		List<Subdir> subDirs = new ArrayList<>();
		for (int i = 0; i < jtSubdirs.getRowCount(); i++) {
			String name = (String)jtSubdirs.getValueAt(i, 0);
			long min = (Long)jtSubdirs.getValueAt(i, 1);
			long max = (Long)jtSubdirs.getValueAt(i, 2);
			int resMinW = 0;
			int resMinH = 0;
			int resMaxW = 0;
			int resMaxH = 0;
			String resMin = (String)jtSubdirs.getValueAt(i, 3);
			String resMax = (String)jtSubdirs.getValueAt(i, 4);

			String[] arr = resMin.split("x");
			if (arr.length == 2) {
				resMinW = SubdirResolutionCellEditorComponent.parseIntVal(arr[0]);
				resMinH = SubdirResolutionCellEditorComponent.parseIntVal(arr[1]);
			}

			arr = resMax.split("x");
			if (arr.length == 2) {
				resMaxW = SubdirResolutionCellEditorComponent.parseIntVal(arr[0]);
				resMaxH = SubdirResolutionCellEditorComponent.parseIntVal(arr[1]);
			}

			subDirs.add(new Subdir(name, min, max, resMinW, resMinH, resMaxW, resMaxH));
		}
		settingsManager.setSubdirs(subDirs);
	}

	private void subdirNew() {
		subdirModel.addEmptyRow();
	}

	private void subdirDelete() {
		int[] rows = jtSubdirs.getSelectedRows();
		for (int i = 0; i < rows.length; i++) {
			subdirModel.removeRow(rows[i]);
		}
	}

	private void subdirUp() {
		int selectedRow = jtSubdirs.getSelectedRow();
		if (selectedRow > 0) {
			subdirModel.moveRow(selectedRow, selectedRow, selectedRow - 1);
			jtSubdirs.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
		}
	}

	private void subdirDown() {
		int selectedRow = jtSubdirs.getSelectedRow();
		if (selectedRow < (subdirModel.getRowCount() - 1)) {
			subdirModel.moveRow(selectedRow, selectedRow, selectedRow + 1);
			jtSubdirs.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
		}
	}

	/**
	 * updateColWidthsToSettingsManager
	 */
	private void updateColWidthsToSettingsManager() {
		if (!settingsManager.isSaveTableColumnSizes()) {
			return;
		}
		settingsManager.setColWidthsSubdirs(TableUtil.serializeColWidthSetting(jtSubdirs));
		settingsManager.writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (!settingsManager.isSaveTableColumnSizes()) {
			return;
		}
		TableUtil.applyColWidths(jtSubdirs, settingsManager.getColWidthsSubdirs());
	}
}

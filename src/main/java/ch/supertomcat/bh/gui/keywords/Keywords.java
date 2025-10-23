package ch.supertomcat.bh.gui.keywords;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableRowSorter;

import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.gui.renderer.KeywordsStringColorRowRenderer;
import ch.supertomcat.bh.importexport.ExportKeywords;
import ch.supertomcat.bh.importexport.ImportKeywords;
import ch.supertomcat.bh.keywords.Keyword;
import ch.supertomcat.bh.keywords.KeywordManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultBooleanColorRowRenderer;

/**
 * Panel containing the Keywords-Table
 */
public class Keywords extends JPanel {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -5829519139175122738L;

	/**
	 * TabelModel
	 */
	private KeywordsTableModel model = new KeywordsTableModel();

	/**
	 * Table Row Sorter
	 */
	private TableRowSorter<KeywordsTableModel> sorter = new TableRowSorter<>(model);

	/**
	 * Table
	 */
	private JTable jtKeywords = new JTable(model);

	/**
	 * Label
	 */
	private JLabel lblInfo = new JLabel(Localization.getString("Count") + ": 0");

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
	private JButton btnDelete = new JButton(Localization.getString("Delete"), Icons.getTangoSVGIcon("actions/edit-delete.svg", 16));

	/**
	 * Button
	 */
	private JButton btnExport = new JButton(Localization.getString("Export"), Icons.getTangoSVGIcon("actions/document-save-as.svg", 16));

	/**
	 * Button
	 */
	private JButton btnImport = new JButton(Localization.getString("Import"), Icons.getTangoSVGIcon("actions/document-open.svg", 16));

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * PopupMenu
	 */
	private JPopupMenu popupMenu = new JPopupMenu();

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemTitleToKeyword = new JMenuItem(Localization.getString("TitleToKeyword"), Icons.getTangoSVGIcon("apps/accessories-text-editor.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemTitleToDirectory = new JMenuItem(Localization.getString("TitleToDirectory"), Icons.getTangoSVGIcon("apps/accessories-text-editor.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemTitleToRelativeDirectory = new JMenuItem(Localization.getString("TitleToRelativeDirectory"), Icons.getTangoSVGIcon("apps/accessories-text-editor.svg", 16));

	/**
	 * MenuItem
	 */
	private JMenuItem menuItemAbsoluteToRelativeDirectory = new JMenuItem(Localization.getString("AbsoluteToRelativeDirectory"), Icons.getTangoSVGIcon("apps/accessories-text-editor.svg", 16));

	/**
	 * Panel
	 */
	private JPanel pnlFilter = new JPanel();

	/**
	 * Panel
	 */
	private JPanel pnlInfo = new JPanel();

	/**
	 * TextField
	 */
	private JTextField txtTitle = new JTextField(20);

	/**
	 * TextField
	 */
	private JTextField txtKeywords = new JTextField(20);

	/**
	 * TextField
	 */
	private JTextField txtPath = new JTextField(20);

	/**
	 * TextField
	 */
	private JTextField txtRPath = new JTextField(20);

	/**
	 * Label
	 */
	private JLabel lblTitle = new JLabel(Localization.getString("Title"));

	/**
	 * Label
	 */
	private JLabel lblKeywords = new JLabel(Localization.getString("Keywords"));

	/**
	 * Label
	 */
	private JLabel lblPath = new JLabel(Localization.getString("Folder"));

	/**
	 * Label
	 */
	private JLabel lblRPath = new JLabel(Localization.getString("RelativeFolder"));

	/**
	 * Filter-Patterns
	 */
	private String patterns[] = new String[4];

	/**
	 * Columns
	 */
	private int cols[] = new int[4];

	/**
	 * DocumentListener
	 */
	private DocumentListener dl0 = null;

	/**
	 * DocumentListener
	 */
	private DocumentListener dl1 = null;

	/**
	 * DocumentListener
	 */
	private DocumentListener dl2 = null;

	/**
	 * DocumentListener
	 */
	private DocumentListener dl3 = null;

	/**
	 * Button
	 */
	private JToggleButton btnFilter = new JToggleButton(Localization.getString("Filter"), Icons.getTangoIcon("actions/edit-find.png", 16));

	/**
	 * Flag if a process is running
	 */
	private boolean running = false;

	/**
	 * Parent Window
	 */
	private final JFrame parentWindow;

	/**
	 * Main Window Access
	 */
	private final MainWindowAccess mainWindowAccess;

	/**
	 * Keyword Manager
	 */
	private final KeywordManager keywordManager;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Mouse Listener
	 */
	private final MouseAdapter mouseListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			actionMousePressed(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			actionMousePressed(e);
		}

		private void actionMousePressed(MouseEvent e) {
			if (running) {
				return;
			}

			if (e.getSource() == jtKeywords && e.isPopupTrigger() && jtKeywords.getSelectedRowCount() > 0) {
				showTablePopupMenu(e);
			}
		}
	};

	/**
	 * Constructor
	 * 
	 * @param parentWindow Parent Window
	 * @param mainWindowAccess Main Window Access
	 * @param keywordManager Keyword Manager
	 * @param settingsManager Settings Manager
	 */
	public Keywords(JFrame parentWindow, MainWindowAccess mainWindowAccess, KeywordManager keywordManager, SettingsManager settingsManager) {
		this.parentWindow = parentWindow;
		this.mainWindowAccess = mainWindowAccess;
		this.keywordManager = keywordManager;
		this.settingsManager = settingsManager;
		TableUtil.internationalizeColumns(jtKeywords);

		jtKeywords.setRowSorter(sorter);
		setLayout(new BorderLayout());
		jtKeywords.getColumn("Folder").setMinWidth(200);
		jtKeywords.getColumn("RelativeFolder").setMinWidth(200);
		updateColWidthsFromSettingsManager();
		jtKeywords.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
				// Nothing to do
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
				// Nothing to do
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
				// Nothing to do
			}

			@Override
			public void columnMarginChanged(ChangeEvent e) {
				updateColWidthsToSettingsManager();
			}

			@Override
			public void columnAdded(TableColumnModelEvent e) {
				// Nothing to do
			}
		});
		jtKeywords.setRowHeight(TableUtil.calculateRowHeight(jtKeywords, true, true));
		jtKeywords.getTableHeader().setReorderingAllowed(false);
		jtKeywords.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);

		patterns[0] = txtTitle.getText();
		patterns[1] = txtKeywords.getText();
		patterns[2] = txtPath.getText();
		patterns[3] = txtRPath.getText();
		cols[0] = jtKeywords.getColumn("Title").getModelIndex();
		cols[1] = jtKeywords.getColumn("Keywords").getModelIndex();
		cols[2] = jtKeywords.getColumn("Folder").getModelIndex();
		cols[3] = jtKeywords.getColumn("RelativeFolder").getModelIndex();

		JScrollPane jsp = new JScrollPane(jtKeywords);
		add(jsp, BorderLayout.CENTER);

		btnNew.addActionListener(e -> actionNew());
		btnEdit.addActionListener(e -> actionEdit());
		btnDelete.addActionListener(e -> actionDelete());

		btnImport.addActionListener(e -> actionImport());
		btnExport.addActionListener(e -> actionExport());

		pnlButtons.add(btnNew);
		pnlButtons.add(btnEdit);
		pnlButtons.add(btnDelete);
		pnlButtons.add(btnImport);
		pnlButtons.add(btnExport);
		add(pnlButtons, BorderLayout.SOUTH);

		TitledBorder brdFilter = BorderFactory.createTitledBorder(Localization.getString("Filter"));
		pnlFilter.setBorder(brdFilter);
		pnlFilter.setLayout(new GridLayout(2, 4, 2, 2));
		pnlFilter.add(lblTitle);
		pnlFilter.add(lblKeywords);
		pnlFilter.add(lblPath);
		pnlFilter.add(lblRPath);
		pnlFilter.add(txtTitle);
		pnlFilter.add(txtKeywords);
		pnlFilter.add(txtPath);
		pnlFilter.add(txtRPath);
		dl0 = createDocumentListener(0);
		dl1 = createDocumentListener(1);
		dl2 = createDocumentListener(2);
		dl3 = createDocumentListener(3);
		txtTitle.getDocument().addDocumentListener(dl0);
		txtKeywords.getDocument().addDocumentListener(dl1);
		txtPath.getDocument().addDocumentListener(dl2);
		txtRPath.getDocument().addDocumentListener(dl3);

		lblInfo.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		pnlInfo = new JPanel();
		pnlInfo.setLayout(new BorderLayout());
		pnlInfo.add(lblInfo, BorderLayout.WEST);
		pnlInfo.add(btnFilter, BorderLayout.EAST);

		JPanel pnlP = new JPanel();
		pnlP.setLayout(new BorderLayout());
		pnlP.add(pnlInfo, BorderLayout.NORTH);
		pnlP.add(pnlFilter, BorderLayout.CENTER);
		add(pnlP, BorderLayout.NORTH);
		boolean filterEnabled = settingsManager.getKeywordsSettings().isFilterEnabled();
		btnFilter.setSelected(filterEnabled);
		pnlFilter.setVisible(filterEnabled);
		btnFilter.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				pnlFilter.setVisible(btnFilter.isSelected());
				settingsManager.getKeywordsSettings().setFilterEnabled(btnFilter.isSelected());
				settingsManager.writeSettings(true);
			}
		});

		popupMenu.add(menuItemTitleToKeyword);
		popupMenu.add(menuItemTitleToDirectory);
		popupMenu.add(menuItemTitleToRelativeDirectory);
		popupMenu.add(menuItemAbsoluteToRelativeDirectory);
		menuItemTitleToKeyword.addActionListener(e -> actionTitleToKeyword());
		menuItemTitleToDirectory.addActionListener(e -> actionTitleToDirectory());
		menuItemTitleToRelativeDirectory.addActionListener(e -> actionTitleToRelativeDirectory());
		menuItemAbsoluteToRelativeDirectory.addActionListener(e -> actionAbsoluteToRelativeDirectory());

		List<Keyword> keywords = keywordManager.getKeywords();
		for (Keyword k : keywords) {
			addKeyword(k);
		}

		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		sorter.sort();

		lblInfo.setText(Localization.getString("Count") + ": " + jtKeywords.getRowCount());
		jtKeywords.addMouseListener(mouseListener);
		jtKeywords.setDefaultRenderer(Object.class, new KeywordsStringColorRowRenderer());
		jtKeywords.getColumn("RelativPath").setCellRenderer(new DefaultBooleanColorRowRenderer());
		updateTableSortOrdersFromSettingsManager();
		jtKeywords.getTableHeader().addMouseListener(mouseListener);

		sorter.addRowSorterListener(new RowSorterListener() {
			@Override
			public void sorterChanged(RowSorterEvent e) {
				if (running) {
					return;
				}
				updateTableSortOrdersToSettingsManager();
			}
		});

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtPath);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtKeywords);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtRPath);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtTitle);
	}

	/**
	 * Reload keywords
	 */
	public void reloadKeywords() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				enableComponents(false);
				model.removeAllRows();

				List<Keyword> keywords = keywordManager.getKeywords();
				for (Keyword k : keywords) {
					addKeyword(k);
				}

				lblInfo.setText(Localization.getString("Count") + ": " + jtKeywords.getRowCount());
				enableComponents(true);
			}
		};

		if (EventQueue.isDispatchThread()) {
			runnable.run();
		} else {
			EventQueue.invokeLater(runnable);
		}
	}

	/**
	 * Adds a keyword
	 * 
	 * @param keyword Keyword
	 */
	public void addKeyword(Keyword keyword) {
		model.addRow(keyword);
		lblInfo.setText(Localization.getString("Count") + ": " + jtKeywords.getRowCount());
	}

	/**
	 * Creates a DocumentListener for a column
	 * 
	 * @param col Column
	 * @return DocumentListener
	 */
	private DocumentListener createDocumentListener(final int col) {
		return new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				filterTable(col);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				filterTable(col);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				filterTable(col);
			}
		};
	}

	/**
	 * Filter Table
	 * 
	 * @param col Column
	 */
	private synchronized void filterTable(int col) {
		switch (col) {
			case 0:
				patterns[0] = txtTitle.getText();
				break;
			case 1:
				patterns[1] = txtKeywords.getText();
				break;
			case 2:
				patterns[2] = txtPath.getText();
				break;
			case 3:
				patterns[3] = txtRPath.getText();
				break;
			default:
				return;
		}
		List<RowFilter<KeywordsTableModel, Object>> filters = new ArrayList<>(4);
		for (int i = 0; i < cols.length; i++) {
			boolean filterSyntaxError = false;
			String filterErrorMessage = "";
			try {
				RowFilter<KeywordsTableModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + patterns[i], cols[i]);
				filters.add(rowFilter);
			} catch (PatternSyntaxException pse) {
				filterSyntaxError = true;
				filterErrorMessage = pse.getMessage();
			}
			Color backgroundColor;
			if (filterSyntaxError) {
				backgroundColor = Color.RED;
			} else {
				backgroundColor = UIManager.getColor("TextField.background");
			}
			switch (i) {
				case 0:
					txtTitle.setBackground(backgroundColor);
					txtTitle.repaint();
					txtTitle.setToolTipText(filterErrorMessage);
					break;
				case 1:
					txtKeywords.setBackground(backgroundColor);
					txtKeywords.repaint();
					txtKeywords.setToolTipText(filterErrorMessage);
					break;
				case 2:
					txtPath.setBackground(backgroundColor);
					txtPath.repaint();
					txtPath.setToolTipText(filterErrorMessage);
					break;
				case 3:
					txtRPath.setBackground(backgroundColor);
					txtRPath.repaint();
					txtRPath.setToolTipText(filterErrorMessage);
					break;
				default:
					// Nothing to do
					break;
			}
		}
		RowFilter<KeywordsTableModel, Object> filter = RowFilter.andFilter(filters);
		sorter.setRowFilter(filter);
	}

	/**
	 * Lock/Unlock Components
	 * 
	 * @param b Enabled (Unlocked)
	 */
	private void enableComponents(boolean b) {
		if (!b) {
			running = true;
			jtKeywords.removeMouseListener(mouseListener);
		}
		txtTitle.setEnabled(b);
		txtKeywords.setEditable(b);
		txtPath.setEnabled(b);
		txtRPath.setEnabled(b);
		btnNew.setEnabled(b);
		btnEdit.setEnabled(b);
		btnDelete.setEnabled(b);
		btnImport.setEnabled(b);
		btnExport.setEnabled(b);
		jtKeywords.setEnabled(b);
		if (b) {
			jtKeywords.addMouseListener(mouseListener);
			running = false;
		}
	}

	private void actionNew() {
		if (running) {
			return;
		}
		KeywordEditDialog dialog = KeywordEditDialog.openKeywordEditDialog(parentWindow, Localization.getString("Add"), "", "", settingsManager.getSavePath(), true, "", settingsManager);
		if (dialog != null) {
			keywordManager.addKeyword(dialog.getKeyword());
		}
	}

	private void actionDelete() {
		if (running) {
			return;
		}
		int retval = JOptionPane.showConfirmDialog(parentWindow, Localization.getString("KeywordsReallyDelete"), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, Icons
				.getTangoIcon("status/dialog-warning.png", 32));
		if (retval == JOptionPane.NO_OPTION) {
			return;
		}

		enableComponents(false);

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ProgressObserver pg = new ProgressObserver();
					mainWindowAccess.addProgressObserver(pg);
					pg.progressChanged(Localization.getString("DeleteEntries"));
					pg.progressModeChanged(true);

					int[] selectedRows = jtKeywords.getSelectedRows();
					int[] selectedModelRows = TableUtil.convertRowIndexToModel(jtKeywords, selectedRows, true);

					keywordManager.removeKeywords(selectedModelRows);

					lblInfo.setText(Localization.getString("Count") + ": " + jtKeywords.getRowCount());
					mainWindowAccess.removeProgressObserver(pg);
					mainWindowAccess.setMessage(Localization.getString("EntriesDeleted"));
				} finally {
					enableComponents(true);
				}
			}
		});
		t.setName("DeleteKeywordsThread-" + t.threadId());
		t.start();
	}

	private void actionEdit() {
		if (running) {
			return;
		}
		if (jtKeywords.getSelectedRowCount() < 1) {
			return;
		}
		if (jtKeywords.getSelectedRowCount() > 1) {
			JOptionPane.showMessageDialog(parentWindow, Localization.getString("NoMultipleRowEdit"), "Edit", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		int row = jtKeywords.getSelectedRow();
		row = jtKeywords.convertRowIndexToModel(row);
		Keyword k = keywordManager.getKeywordByIndex(row);
		KeywordEditDialog dialog = KeywordEditDialog.openKeywordEditDialog(parentWindow, Localization.getString("Edit"), k.getTitle(), k.getKeywords(), k.getDownloadPath(), k.isRelativePath(), k
				.getRelativeDownloadPath(), settingsManager);
		if (dialog != null) {
			k.setTitle(dialog.getKeywordTitle());
			k.setKeywords(dialog.getKeywords());
			k.setDownloadPath(dialog.getPath());
			k.setRelativePath(dialog.isRelativePathSelected());
			k.setRelativeDownloadPath(dialog.getRelativePath());
			keywordManager.updateKeyword(k);
		}
	}

	private void actionImport() {
		if (running) {
			return;
		}
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				enableComponents(false);
				new ImportKeywords(parentWindow, mainWindowAccess, keywordManager, settingsManager).importKeywords();
				lblInfo.setText(Localization.getString("Count") + ": " + jtKeywords.getRowCount());
				enableComponents(true);
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	private void actionExport() {
		if (running) {
			return;
		}
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				enableComponents(false);
				new ExportKeywords(parentWindow, mainWindowAccess, keywordManager, settingsManager).exportKeywords();
				enableComponents(true);
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	private void actionTitleToKeyword() {
		if (running) {
			return;
		}
		enableComponents(false);
		int[] s = jtKeywords.getSelectedRows();
		String val = "";
		for (int i = 0; i < s.length; i++) {
			val = (String)jtKeywords.getValueAt(s[i], 0);
			jtKeywords.setValueAt(val, s[i], 1);
		}
		enableComponents(true);
	}

	private void actionTitleToDirectory() {
		if (running) {
			return;
		}
		enableComponents(false);
		int[] s = jtKeywords.getSelectedRows();
		String val = "";
		String path = settingsManager.getSavePath();
		for (int i = 0; i < s.length; i++) {
			val = path + (String)jtKeywords.getValueAt(s[i], 0);
			jtKeywords.setValueAt(val, s[i], 2);
		}
		enableComponents(true);
	}

	private void actionTitleToRelativeDirectory() {
		if (running) {
			return;
		}
		enableComponents(false);
		int[] s = jtKeywords.getSelectedRows();
		String val = "";
		for (int i = 0; i < s.length; i++) {
			val = (String)jtKeywords.getValueAt(s[i], 0);
			jtKeywords.setValueAt(val, s[i], 3);
		}
		enableComponents(true);
	}

	private void actionAbsoluteToRelativeDirectory() {
		if (running) {
			return;
		}
		enableComponents(false);
		int[] s = jtKeywords.getSelectedRows();
		String val = "";
		for (int i = 0; i < s.length; i++) {
			val = (String)jtKeywords.getValueAt(s[i], 2);
			String dlFolder = settingsManager.getSavePath();
			int pos = val.indexOf(dlFolder);
			if (pos == 0) {
				val = val.substring(dlFolder.length());
			}
			val = val.replace(":", "");
			jtKeywords.setValueAt(val, s[i], 3);
		}
		enableComponents(true);
	}

	private void showTablePopupMenu(MouseEvent e) {
		SwingUtilities.updateComponentTreeUI(popupMenu);
		popupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * Clear filters
	 */
	public synchronized void clearFilters() {
		txtTitle.setText("");
		txtKeywords.setText("");
		txtPath.setText("");
		txtRPath.setText("");
	}

	/**
	 * updateTableSortOrdersToSettingsManager
	 */
	private void updateTableSortOrdersToSettingsManager() {
		if (settingsManager.isSaveTableSortOrders()) {
			settingsManager.setTableSortOrdersKeywords(TableUtil.serializeTableSortOrderSetting(jtKeywords));
			settingsManager.writeSettings(true);
		}
	}

	/**
	 * updateTableSortOrdersFromSettingsManager
	 */
	private void updateTableSortOrdersFromSettingsManager() {
		if (settingsManager.isSaveTableSortOrders()) {
			TableUtil.applyTableSortOrder(jtKeywords, settingsManager.getTableSortOrdersKeywords());
		}
	}

	/**
	 * updateColWidthsToSettingsManager
	 */
	private void updateColWidthsToSettingsManager() {
		if (settingsManager.isSaveTableColumnSizes()) {
			settingsManager.setColWidthsKeywords(TableUtil.serializeColWidthSetting(jtKeywords));
			settingsManager.writeSettings(true);
		}
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (settingsManager.isSaveTableColumnSizes()) {
			TableUtil.applyColWidths(jtKeywords, settingsManager.getColWidthsKeywords());
		}
	}
}

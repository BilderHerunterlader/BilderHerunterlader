package ch.supertomcat.bh.gui.adder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.gui.renderer.AdderKeywordSelectorColorRowRenderer;
import ch.supertomcat.bh.keywords.Keyword;
import ch.supertomcat.bh.keywords.KeywordMatch;
import ch.supertomcat.bh.keywords.KeywordMatch.KeywordMatchType;
import ch.supertomcat.bh.keywords.KeywordMatch.KeywordMatchTypeComparator;
import ch.supertomcat.supertomcattools.guitools.Localization;
import ch.supertomcat.supertomcattools.guitools.TableTool;
import ch.supertomcat.supertomcattools.guitools.copyandpaste.JTextComponentCopyAndPaste;

/**
 * Dialog for selecting a keyword (Search by title)
 */
public class AdderKeywordSelectorTitle extends JDialog {
	private static final long serialVersionUID = 1L;

	/**
	 * Panel
	 */
	private JPanel pnlFilter = new JPanel();

	/**
	 * Label
	 */
	private JLabel lblFilter = new JLabel(Localization.getString("Filter"));

	/**
	 * TextField
	 */
	private JTextField txtFilter = new JTextField(70);

	/**
	 * cbDisplayAllKeywords
	 */
	private JCheckBox cbDisplayAllKeywords = new JCheckBox(Localization.getString("ShowAllKeywords"), false);

	/**
	 * Tablemodel
	 */
	private AdderKeywordSelectorTitleTableModel model = new AdderKeywordSelectorTitleTableModel();

	/**
	 * Table
	 */
	private JTable table = new JTable(model);

	/**
	 * Table Row Sorter
	 */
	private TableRowSorter<AdderKeywordSelectorTitleTableModel> sorter = new TableRowSorter<>(model);

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * CheckBox
	 */
	private JCheckBox cbChooseDefault = new JCheckBox(Localization.getString("AlwaysChooseThis"), false);

	/**
	 * Button
	 */
	private JButton btnNew = new JButton(Localization.getString("NewKeyword"), Icons.getTangoIcon("actions/document-new.png", 16));

	/**
	 * Button
	 */
	private JButton btnOK = new JButton(Localization.getString("OK"));

	/**
	 * Button
	 */
	private JButton btnCancel = new JButton(Localization.getString("Cancel"));

	/**
	 * Flag if ok was pressed
	 */
	private boolean okPressed = false;

	/**
	 * New Keyword created by user
	 */
	private Keyword newKeyword = null;

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param title Title
	 * @param modal Modal
	 * @param matches Matches
	 * @param additionalKeywords Additional Keywords, which can be displayed
	 * @param byFilename Search by filename (True if old style search for filenames is used, open this dialog for every file)
	 */
	public AdderKeywordSelectorTitle(JFrame owner, String title, boolean modal, List<KeywordMatch> matches, List<Keyword> additionalKeywords, boolean byFilename) {
		super(owner, title, modal);
		this.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);

		setLayout(new BorderLayout());

		TableTool.internationalizeColumns(table);

		table.setRowSorter(sorter);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Object.class, new AdderKeywordSelectorColorRowRenderer());
		table.setRowHeight(TableTool.calculateRowHeight(table, false, true));
		TableTool.setVisibleRowCount(table, 20);

		int selectedIndex = 0;

		List<Keyword> additionalKeywordsFiltered;
		if (additionalKeywords != null) {
			additionalKeywordsFiltered = new ArrayList<>(additionalKeywords);
		} else {
			additionalKeywordsFiltered = new ArrayList<>();
		}

		boolean matchesFound = false;
		if (matches != null) {
			matchesFound = !matches.isEmpty();

			boolean exactMatch = false;

			int count = 0;
			for (KeywordMatch match : matches) {
				Keyword keyword = match.getKeyword();
				KeywordMatchType matchType = match.getMatchType();

				if (matchType == KeywordMatchType.MATCHED_EXACT) {
					exactMatch = true;
					selectedIndex = count;
				} else if (!exactMatch && matchType == KeywordMatchType.MATCHED_ALL_KEYWORDS) {
					selectedIndex = count;
				}

				if (additionalKeywordsFiltered.contains(keyword)) {
					additionalKeywordsFiltered.remove(keyword);
				}

				model.addRow(keyword, matchType);
				count++;
			}
		}

		for (Keyword additionalKeyword : additionalKeywordsFiltered) {
			model.addRow(additionalKeyword, KeywordMatchType.MATCHED_NOT);
		}

		cbDisplayAllKeywords.setSelected(!matchesFound);
		cbDisplayAllKeywords.setEnabled(matchesFound);

		sorter.setComparator(1, new KeywordMatchTypeComparator());

		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		sorter.sort();
		filterTable();

		selectedIndex = table.convertRowIndexToView(selectedIndex);

		if (table.getRowCount() > 0 && selectedIndex < table.getRowCount()) {
			table.setRowSelectionInterval(selectedIndex, selectedIndex);
		}

		pnlFilter.add(lblFilter);
		pnlFilter.add(txtFilter);
		txtFilter.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				filterTable();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				filterTable();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				filterTable();
			}
		});
		pnlFilter.add(cbDisplayAllKeywords);
		cbDisplayAllKeywords.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				filterTable();
			}
		});
		add(pnlFilter, BorderLayout.NORTH);

		JScrollPane sp = new JScrollPane(table);
		add(sp, BorderLayout.CENTER);

		pnlButtons.add(cbChooseDefault);
		pnlButtons.add(btnNew);
		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);
		add(pnlButtons, BorderLayout.SOUTH);
		this.getRootPane().setDefaultButton(btnOK);
		btnNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Keyword k = AdderKeywordAddDialog.openAddKeywordDialog(AdderKeywordSelectorTitle.this);
				if (k != null) {
					newKeyword = k;
					okPressed = true;
					dispose();
				}
			}
		});
		btnOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed = true;
				dispose();
			}
		});
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed = false;
				dispose();
			}
		});
		if (!byFilename) {
			cbChooseDefault.setVisible(false);
		}

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtFilter);

		pack();
		setLocationRelativeTo(owner);

		if (table.getRowCount() > 0 && selectedIndex < table.getRowCount()) {
			table.scrollRectToVisible(table.getCellRect(selectedIndex, 0, true));
		}

		// Enter and Escape (before setVisible(true)!)
		ActionMap am = getRootPane().getActionMap();
		InputMap im = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		Object windowCloseKey = new Object();
		Object windowOkKey = new Object();
		KeyStroke windowCloseStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		Action windowCloseAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				btnCancel.doClick();
			}
		};
		KeyStroke windowOkStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		Action windowOkAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				btnOK.doClick();
			}
		};
		im.put(windowCloseStroke, windowCloseKey);
		am.put(windowCloseKey, windowCloseAction);
		im.put(windowOkStroke, windowOkKey);
		am.put(windowOkKey, windowOkAction);

		if (matchesFound) {
			btnOK.requestFocusInWindow();
		} else {
			btnNew.setVisible(false);
			txtFilter.requestFocusInWindow();
		}

		setVisible(true);
	}

	/**
	 * @return Selected Keyword or null if none selected
	 */
	public Keyword getSelectedKeyword() {
		if (newKeyword != null) {
			return newKeyword;
		} else {
			int tableIndex = table.getSelectedRow();
			if (tableIndex > -1) {
				int modelIndex = this.table.convertRowIndexToModel(this.table.getSelectedRow());
				return (Keyword)model.getValueAt(modelIndex, 0);
			}
			return null;
		}
	}

	/**
	 * Get-Method
	 * 
	 * @return True if ok was pressed
	 */
	public boolean isOkPressed() {
		return okPressed;
	}

	/**
	 * @return True if choose default, false otherwise
	 */
	public boolean isChooseDefault() {
		return cbChooseDefault.isSelected();
	}

	/**
	 * Filter the Table
	 */
	private synchronized void filterTable() {
		String pattern = txtFilter.getText();
		List<RowFilter<AdderKeywordSelectorTitleTableModel, Object>> filters = new ArrayList<>();
		try {
			RowFilter<AdderKeywordSelectorTitleTableModel, Object> filterTitle = RowFilter.regexFilter("(?i)" + pattern, 0);
			filters.add(filterTitle);
			txtFilter.setBackground(UIManager.getColor("TextField.background"));
			txtFilter.repaint();
			txtFilter.setToolTipText("");
		} catch (PatternSyntaxException pse) {
			txtFilter.setBackground(Color.RED);
			txtFilter.repaint();
			txtFilter.setToolTipText(pse.getMessage());
		}
		if (!cbDisplayAllKeywords.isSelected()) {
			RowFilter<AdderKeywordSelectorTitleTableModel, Object> filterMatchType = new KeywordNoMatchTypeFilter();
			filters.add(filterMatchType);
		}

		if (!filters.isEmpty()) {
			sorter.setRowFilter(RowFilter.andFilter(filters));
		}
	}

	/**
	 * Filter for match type
	 */
	private static class KeywordNoMatchTypeFilter extends RowFilter<AdderKeywordSelectorTitleTableModel, Object> {
		@Override
		public boolean include(Entry<? extends AdderKeywordSelectorTitleTableModel, ? extends Object> entry) {
			for (int i = entry.getValueCount() - 1; i >= 0; i--) {
				if (entry.getValue(i) instanceof KeywordMatchType && (KeywordMatchType)entry.getValue(i) == KeywordMatchType.MATCHED_NOT) {
					return false;
				}
			}
			return true;
		}
	}
}

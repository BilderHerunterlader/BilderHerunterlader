package ch.supertomcat.bh.gui.adder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridLayout;
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
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;

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
	private JLabel lblFilterTitle = new JLabel(Localization.getString("Title"));

	/**
	 * TextField
	 */
	private JTextField txtFilterTitle = new JTextField(35);

	/**
	 * Label
	 */
	private JLabel lblFilterKeywords = new JLabel(Localization.getString("Keywords"));

	/**
	 * TextField
	 */
	private JTextField txtFilterKeywords = new JTextField(35);

	/**
	 * cbDisplayAllKeywords
	 */
	private JCheckBox cbDisplayAllKeywords = new JCheckBox(Localization.getString("ShowAllKeywords"), false);

	/**
	 * Tablemodel
	 */
	private AdderKeywordSelectorTitleTableModel model;

	/**
	 * Table
	 */
	private JTable table;

	/**
	 * Table Row Sorter
	 */
	private TableRowSorter<AdderKeywordSelectorTitleTableModel> sorter;

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

		boolean includeMatchType = matches != null ? true : false;

		model = new AdderKeywordSelectorTitleTableModel(includeMatchType);
		table = new JTable(model);
		sorter = new TableRowSorter<>(model);

		TableUtil.internationalizeColumns(table);

		table.setRowSorter(sorter);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Object.class, new AdderKeywordSelectorColorRowRenderer(includeMatchType));
		table.setRowHeight(TableUtil.calculateRowHeight(table, false, true));
		TableUtil.setVisibleRowCount(table, 20);

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

		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		if (includeMatchType) {
			int matchTypeColumnModelIndex = table.getColumn("MatchType").getModelIndex();
			sorter.setComparator(matchTypeColumnModelIndex, new KeywordMatchTypeComparator());
			sortKeys.add(new RowSorter.SortKey(matchTypeColumnModelIndex, SortOrder.ASCENDING));
		}

		int titleColumnModelIndex = table.getColumn("Title").getModelIndex();
		sortKeys.add(new RowSorter.SortKey(titleColumnModelIndex, SortOrder.ASCENDING));

		int keywordsColumnModelIndex = table.getColumn("Keywords").getModelIndex();
		sortKeys.add(new RowSorter.SortKey(keywordsColumnModelIndex, SortOrder.ASCENDING));

		sorter.setSortKeys(sortKeys);
		sorter.sort();
		filterTable();

		selectedIndex = table.convertRowIndexToView(selectedIndex);

		if (table.getRowCount() > 0 && selectedIndex < table.getRowCount()) {
			table.setRowSelectionInterval(selectedIndex, selectedIndex);
		}

		pnlFilter.setLayout(new GridLayout(2, 3, 2, 2));
		pnlFilter.add(lblFilterTitle);
		pnlFilter.add(lblFilterKeywords);
		pnlFilter.add(cbDisplayAllKeywords);
		cbDisplayAllKeywords.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				filterTable();
			}
		});
		pnlFilter.add(txtFilterTitle);
		txtFilterTitle.getDocument().addDocumentListener(new DocumentListener() {
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
		pnlFilter.add(txtFilterKeywords);
		txtFilterKeywords.getDocument().addDocumentListener(new DocumentListener() {
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

		add(pnlFilter, BorderLayout.NORTH);
		pnlFilter.add(new JLabel());
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

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtFilterTitle);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtFilterKeywords);

		pack();
		setLocationRelativeTo(owner);

		if (table.getRowCount() > 0 && selectedIndex < table.getRowCount()) {
			table.scrollRectToVisible(table.getCellRect(selectedIndex, table.convertColumnIndexToView(titleColumnModelIndex), true));
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
			txtFilterTitle.requestFocusInWindow();
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
				int rowModelIndex = table.convertRowIndexToModel(this.table.getSelectedRow());
				int columnModelIndex = table.getColumn("Title").getModelIndex();
				return (Keyword)model.getValueAt(rowModelIndex, columnModelIndex);
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
		String patternTitle = txtFilterTitle.getText();
		String patternKeywords = txtFilterKeywords.getText();
		List<RowFilter<AdderKeywordSelectorTitleTableModel, Object>> filters = new ArrayList<>();

		try {
			int titleColumnModelIndex = table.getColumn("Title").getModelIndex();
			RowFilter<AdderKeywordSelectorTitleTableModel, Object> filterTitle = RowFilter.regexFilter("(?i)" + patternTitle, titleColumnModelIndex);
			filters.add(filterTitle);
			txtFilterTitle.setBackground(UIManager.getColor("TextField.background"));
			txtFilterTitle.repaint();
			txtFilterTitle.setToolTipText("");
		} catch (PatternSyntaxException pse) {
			txtFilterTitle.setBackground(Color.RED);
			txtFilterTitle.repaint();
			txtFilterTitle.setToolTipText(pse.getMessage());
		}

		try {
			int keywordsColumnModelIndex = table.getColumn("Keywords").getModelIndex();
			RowFilter<AdderKeywordSelectorTitleTableModel, Object> filterKeywords = RowFilter.regexFilter("(?i)" + patternKeywords, keywordsColumnModelIndex);
			filters.add(filterKeywords);
			txtFilterKeywords.setBackground(UIManager.getColor("TextField.background"));
			txtFilterKeywords.repaint();
			txtFilterKeywords.setToolTipText("");
		} catch (PatternSyntaxException pse) {
			txtFilterKeywords.setBackground(Color.RED);
			txtFilterKeywords.repaint();
			txtFilterKeywords.setToolTipText(pse.getMessage());
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

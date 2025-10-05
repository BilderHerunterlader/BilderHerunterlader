package ch.supertomcat.bh.gui.adder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import ch.supertomcat.bh.gui.renderer.AdderKeywordSelectorFilenameComboBoxColumnRenderer;
import ch.supertomcat.bh.keywords.Keyword;
import ch.supertomcat.bh.keywords.KeywordFilenameMatches;
import ch.supertomcat.bh.keywords.KeywordMatch;
import ch.supertomcat.bh.keywords.KeywordMatch.KeywordMatchType;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.table.TableUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultStringColorRowRenderer;

/**
 * Dialog for selecting keywords (Search by filename)
 */
public class AdderKeywordSelectorFilename extends JDialog {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Tablemodel
	 */
	private AdderKeywordSelectorFilenameTableModel model = new AdderKeywordSelectorFilenameTableModel();

	/**
	 * ComboBoxRenderer
	 */
	private AdderKeywordSelectorFilenameComboBoxColumnRenderer ccr = new AdderKeywordSelectorFilenameComboBoxColumnRenderer();

	/**
	 * Table
	 */
	private JTable table = new JTable(model);

	/**
	 * Panel
	 */
	private JPanel pnlButtons = new JPanel();

	/**
	 * Button
	 */
	private JButton btnOK = new JButton(Localization.getString("OK"));

	/**
	 * Button
	 */
	private JButton btnCancel = new JButton(Localization.getString("Cancel"));

	/**
	 * Checkbox
	 */
	private JCheckBox chkDeleteNoKeyword = new JCheckBox(Localization.getString("DeleteNoKeyword"), false);

	/**
	 * Flag if dialog was cancelled
	 */
	private boolean cancelled = true;

	/**
	 * Flag if urls are links or path to local files in the table
	 */
	private boolean localFiles = false;

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param title Title
	 * @param modal Modal
	 * @param matchesByURL Keyword Matches
	 * @param localFiles Local files
	 * @param settingsManager Settings Manager
	 */
	public AdderKeywordSelectorFilename(JFrame owner, String title, boolean modal, List<KeywordFilenameMatches> matchesByURL, boolean localFiles, SettingsManager settingsManager) {
		super(owner, title, modal);
		this.localFiles = localFiles;
		this.settingsManager = settingsManager;

		setLayout(new BorderLayout());

		TableUtil.internationalizeColumns(table);

		table.getTableHeader().setReorderingAllowed(false);
		table.setDefaultRenderer(Object.class, new DefaultStringColorRowRenderer());
		table.getColumn("Keywords").setCellRenderer(ccr);
		table.getColumn("Keywords").setCellEditor(new AdderKeywordSelectorFilenameComboBoxCellEditor(this));

		if (!updateColWidthsFromSettingsManager()) {
			FontMetrics fontMetrics = table.getFontMetrics(table.getFont());
			int charWidth = fontMetrics.charWidth('A');
			int preferredTableWidth = 120 * charWidth;
			table.setPreferredScrollableViewportSize(new Dimension(preferredTableWidth, table.getPreferredScrollableViewportSize().height));
		}
		table.setRowHeight(TableUtil.calculateRowHeight(table, true, new JComboBox<>()));

		table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
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

		for (KeywordFilenameMatches matches : matchesByURL) {
			String url = matches.getUrl();
			List<KeywordMatch> keywordMatches = matches.getMatches();

			JComboBox<KeywordMatch> cb = new JComboBox<>();
			boolean exact = false;
			int select = 0;
			cb.addItem(null);

			int index = 1;
			for (KeywordMatch keywordMatch : keywordMatches) {
				KeywordMatchType matchType = keywordMatch.getMatchType();

				cb.addItem(keywordMatch);

				if (matchType == KeywordMatchType.MATCHED_EXACT) {
					select = index;
					exact = true;
				} else if (!exact && matchType == KeywordMatchType.MATCHED_ALL_KEYWORDS) {
					select = index;
				}

				index++;
			}

			cb.setSelectedIndex(select);

			Object[] data = new Object[2];
			data[0] = url;
			data[1] = cb;
			model.addRow(data);
		}

		if (localFiles) {
			chkDeleteNoKeyword.setSelected(settingsManager.getKeywordsSettings().isDeleteNoKeyword());
			chkDeleteNoKeyword.setVisible(true);
		} else {
			chkDeleteNoKeyword.setVisible(false);
		}
		pnlButtons.add(chkDeleteNoKeyword);
		btnOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelled = false;
				if (localFiles) {
					settingsManager.getKeywordsSettings().setDeleteNoKeyword(chkDeleteNoKeyword.isSelected());
					settingsManager.writeSettings(true);
				}
				dispose();
			}
		});
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelled = true;
				dispose();
			}
		});
		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);
		add(pnlButtons, BorderLayout.SOUTH);
		add(new JScrollPane(table), BorderLayout.CENTER);

		pack();
		setLocationRelativeTo(owner);

		// Enter und Escape (before setVisible(true)!)
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

		setVisible(true);
	}

	/**
	 * Get-Method
	 * 
	 * @param index Index
	 * @return ComboBox
	 */
	@SuppressWarnings("unchecked")
	public JComboBox<String> getComboBox(int index) {
		return (JComboBox<String>)model.getValueAt(index, table.getColumn("Keywords").getModelIndex());
	}

	/**
	 * Returns the cancelled
	 * 
	 * @return cancelled
	 */
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * @return Chosen Keywords
	 */
	public Keyword[] getChosenKeywords() {
		int rowCount = model.getRowCount();
		Keyword[] chosen = new Keyword[rowCount];
		for (int i = 0; i < rowCount; i++) {
			if (cancelled) {
				chosen[i] = null;
			} else {
				@SuppressWarnings("unchecked")
				JComboBox<KeywordMatch> cb = (JComboBox<KeywordMatch>)model.getValueAt(i, table.getColumn("Keywords").getModelIndex());
				KeywordMatch keywordMatch = (KeywordMatch)cb.getSelectedItem();
				if (keywordMatch != null) {
					chosen[i] = keywordMatch.getKeyword();
				} else {
					chosen[i] = null;
				}
			}
		}
		return chosen;
	}

	/**
	 * Get-Method
	 * 
	 * @return the localFiles
	 */
	public boolean isLocalFiles() {
		return localFiles;
	}

	/**
	 * Get-Method
	 * 
	 * @return DeleteNoKeyword
	 */
	public boolean isDeleteNoKeyword() {
		if (localFiles) {
			return chkDeleteNoKeyword.isSelected();
		}
		return false;
	}

	private void updateColWidthsToSettingsManager() {
		if (settingsManager.isSaveTableColumnSizes()) {
			settingsManager.setColWidthsAdderKeywordSelectorFilename(TableUtil.serializeColWidthSetting(table));
			settingsManager.writeSettings(true);
		}
	}

	private boolean updateColWidthsFromSettingsManager() {
		if (settingsManager.isSaveTableColumnSizes()) {
			TableUtil.applyColWidths(table, settingsManager.getColWidthsAdderKeywordSelectorFilename());
			return true;
		}
		return false;
	}
}

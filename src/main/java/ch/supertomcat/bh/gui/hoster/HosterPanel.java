package ch.supertomcat.bh.gui.hoster;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.BHGUIConstants;
import ch.supertomcat.bh.gui.renderer.HosterColorRowRenderer;
import ch.supertomcat.bh.gui.renderer.HosterOptionsColumnRenderer;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.hoster.IRedirect;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.guitools.TableTool;
import ch.supertomcat.supertomcattools.guitools.tablerenderer.DefaultBooleanColorRowRenderer;

/**
 * Panel for Hostclasses
 */
public class HosterPanel extends JPanel {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * TabelModel
	 */
	private HosterTableModel model = new HosterTableModel();

	/**
	 * Table
	 */
	private JTable jtHoster = new JTable(model);

	/**
	 * Special renderer for option buttons
	 */
	private HosterOptionsColumnRenderer ocr = new HosterOptionsColumnRenderer();

	/**
	 * Constructor
	 */
	public HosterPanel() {
		TableTool.internationalizeColumns(jtHoster);

		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent tme) {
				if (tme.getColumn() == 4) {
					int firstChangedRow = tme.getFirstRow();
					boolean b = (Boolean)model.getValueAt(firstChangedRow, 4);

					Object hostValue = model.getValueAt(firstChangedRow, 0);
					if (hostValue instanceof IRedirect) {
						((IRedirect)hostValue).setEnabled(b);
					} else if (hostValue instanceof Host) {
						((Host)hostValue).setEnabled(b);
					} else if (hostValue == null) {
						logger.error("Unknown class type in column 0: null");
					} else {
						logger.error("Unknown class type in column 0: {}", hostValue.getClass());
					}
				}
			}
		});

		jtHoster.setDefaultRenderer(Object.class, new HosterColorRowRenderer());
		jtHoster.setDefaultRenderer(Boolean.class, new DefaultBooleanColorRowRenderer());
		jtHoster.getColumn("Settings").setCellRenderer(ocr);
		jtHoster.getColumn("Settings").setCellEditor(new OptionsCellEditor(model));
		updateColWidthsFromSettingsManager();
		jtHoster.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
			}

			@Override
			public void columnMarginChanged(ChangeEvent e) {
				updateColWidthsToSettingsManager();
			}

			@Override
			public void columnAdded(TableColumnModelEvent e) {
			}
		});
		jtHoster.getTableHeader().setReorderingAllowed(false);
		jtHoster.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jtHoster.setGridColor(BHGUIConstants.TABLE_GRID_COLOR);
		jtHoster.setRowHeight(TableTool.calculateRowHeight(jtHoster, true, true));

		setLayout(new BorderLayout());

		JScrollPane jsp = new JScrollPane(jtHoster);
		add(jsp, BorderLayout.CENTER);

		loadHoster();
	}

	/**
	 * Load hosters
	 */
	private void loadHoster() {
		for (IRedirect redirect : HostManager.instance().getRedirectManager().getRedirects()) {
			model.addRow(redirect);
		}

		for (Host host : HostManager.instance().getHosters()) {
			model.addRow(host);
		}
	}

	/**
	 * updateColWidthsToSettingsManager
	 */
	private void updateColWidthsToSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false) {
			return;
		}
		SettingsManager.instance().setColWidthsHosts(TableTool.serializeColWidthSetting(jtHoster));
		SettingsManager.instance().writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false) {
			return;
		}
		TableTool.applyColWidths(jtHoster, SettingsManager.instance().getColWidthsHosts());
	}
}

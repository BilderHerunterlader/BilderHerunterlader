package ch.supertomcat.bh.gui.hoster;

import java.awt.BorderLayout;
import java.util.List;

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
public class HosterPanel extends JPanel implements TableColumnModelListener {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Table
	 */
	private JTable jtHoster;

	/**
	 * TabelModel
	 */
	private HosterTableModel model = new HosterTableModel();

	/**
	 * Special renderer for option buttons
	 */
	private HosterOptionsColumnRenderer ocr = new HosterOptionsColumnRenderer();

	/**
	 * HostManager
	 */
	private HostManager hm = HostManager.instance();

	/**
	 * Constructor
	 */
	public HosterPanel() {
		jtHoster = new JTable(model);

		TableTool.internationalizeColumns(jtHoster);

		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent tme) {
				if (tme.getColumn() == 3) {
					int firstRow = tme.getFirstRow();
					boolean b = (Boolean)model.getValueAt(firstRow, 3);
					int redirectCount = hm.getRedirectManager().getRedirects().size();
					if (firstRow < redirectCount) {
						IRedirect r = hm.getRedirectManager().getRedirect(firstRow);
						r.setEnabled(b);
					} else {
						Host h = hm.getHost(firstRow - redirectCount + 1);
						h.setEnabled(b);
					}
				}
			}
		});

		jtHoster.setDefaultRenderer(Object.class, new HosterColorRowRenderer());
		jtHoster.setDefaultRenderer(Boolean.class, new DefaultBooleanColorRowRenderer());
		jtHoster.getColumn("Settings").setCellRenderer(ocr);
		jtHoster.getColumn("Settings").setCellEditor(new OptionsCellEditor(model));
		updateColWidthsFromSettingsManager();
		jtHoster.getColumnModel().addColumnModelListener(this);
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
		List<IRedirect> redirects = HostManager.instance().getRedirectManager().getRedirects();
		for (int i = 0; i < redirects.size(); i++) {
			model.addRow(redirects.get(i));
		}

		List<Host> hosts = HostManager.instance().getHosters();
		for (int i = 0; i < hosts.size(); i++) {
			model.addRow(hosts.get(i));
		}
	}

	/**
	 * updateColWidthsToSettingsManager
	 */
	private void updateColWidthsToSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false)
			return;
		SettingsManager.instance().setColWidthsHosts(TableTool.serializeColWidthSetting(jtHoster));
		SettingsManager.instance().writeSettings(true);
	}

	/**
	 * updateColWidthsFromSettingsManager
	 */
	private void updateColWidthsFromSettingsManager() {
		if (SettingsManager.instance().isSaveTableColumnSizes() == false)
			return;
		TableTool.applyColWidths(jtHoster, SettingsManager.instance().getColWidthsHosts());
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

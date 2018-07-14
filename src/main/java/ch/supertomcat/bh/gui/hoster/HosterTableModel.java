package ch.supertomcat.bh.gui.hoster;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.gui.Main;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.hoster.HostRules;
import ch.supertomcat.bh.hoster.HostSortImages;
import ch.supertomcat.bh.hoster.IRedirect;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.settings.ISettingsListener;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcattools.guitools.Localization;

/**
 * TableModel for Hostclasses
 */
public class HosterTableModel extends DefaultTableModel implements ISettingsListener {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(HosterTableModel.class);

	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Panels
	 */
	private List<JPanel> panels = new ArrayList<>();

	/**
	 * Constructor
	 */
	public HosterTableModel() {
		super();
		this.addColumn("Hoster");
		this.addColumn("Version");
		this.addColumn("Settings");
		this.addColumn("Enabled");
		SettingsManager.instance().addSettingsListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 2 && this.getValueAt(row, 2) instanceof JPanel) {
			return true;
		}

		String name = (String)this.getValueAt(row, 0);

		if (column == 3 && !name.equals(HostSortImages.NAME) && !name.equals(HostRules.NAME)) {
			return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 3) {
			return Boolean.class;
		}
		return super.getColumnClass(columnIndex);
	}

	/**
	 * Add host
	 * 
	 * @param host Host
	 */
	public void addRow(final Host host) {
		Object data[] = new Object[4];

		if (host.getName().equals("HostDefaultFiles")) {
			data[0] = host.getName() + " (" + Localization.getString("DirectLinkedFiles") + ")";
		} else {
			if (host.isDeveloper()) {
				data[0] = "Developer: " + host.getName();
			} else {
				data[0] = host.getName();
			}
		}

		data[1] = host.getVersion();
		Method mx = null;

		if (HostManager.instance().hasInterface(host, "ch.supertomcat.bh.hoster.hosteroptions.IHosterOptions")) {
			try {
				mx = host.getClass().getDeclaredMethod("openOptionsDialog");
			} catch (NoSuchMethodException e) {
				logger.error("openOptionsDialog method is missing in class: {}", host.getClass().getName(), e);
			}
		}

		final Method mOpen;
		if (mx != null) {
			mOpen = mx;
			JButton btn = new JButton(Localization.getString("Settings"), Icons.getTangoIcon("categories/preferences-system.png", 16));
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (DownloadQueueManager.instance().isDownloading()) {
						JOptionPane.showMessageDialog(Main.instance(), Localization.getString("HosterChangeWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						mOpen.invoke(host, new Object[] {});
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
				}
			});
			JPanel pnl = new JPanel();
			pnl.add(btn);
			data[2] = pnl;
			panels.add(pnl);
		} else {
			data[2] = "";
			panels.add(null);
		}
		data[3] = host.isEnabled();
		this.addRow(data);
	}

	/**
	 * Add Redirect
	 * 
	 * @param redirect Redirect
	 */
	public void addRow(final IRedirect redirect) {
		Object data[] = new Object[4];

		if (redirect == HostManager.instance().getHr()) {
			return;
		}

		data[0] = "Redirect: " + redirect.getName();
		data[1] = redirect.getVersion();
		data[2] = "";
		data[3] = redirect.isEnabled();
		panels.add(null);
		this.addRow(data);
	}

	/**
	 * Returns the ComboBox
	 * 
	 * @param index Index
	 * @return ComboBox
	 */
	public JPanel getOptionPanel(int index) {
		return panels.get(index);
	}

	@Override
	public void settingsChanged() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				Iterator<JPanel> it = panels.iterator();
				String strLAF = SettingsManager.LAF_CLASSPATHES[SettingsManager.instance().getLookAndFeel()];
				if (strLAF.length() > 0) {
					while (it.hasNext()) {
						JPanel currentPanel = it.next();
						if (currentPanel != null) {
							SwingUtilities.updateComponentTreeUI(currentPanel);
						}
					}
				}
			}
		});
	}
}

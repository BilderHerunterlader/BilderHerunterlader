package ch.supertomcat.bh.gui.hoster;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.gui.Main;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.HostManager;
import ch.supertomcat.bh.hoster.IRedirect;
import ch.supertomcat.bh.hoster.hosteroptions.IHosterOptions;
import ch.supertomcat.bh.queue.DownloadQueueManager;
import ch.supertomcat.bh.settings.ISettingsListener;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * TableModel for Hostclasses
 */
public class HosterTableModel extends DefaultTableModel implements ISettingsListener {
	private static final long serialVersionUID = 1L;

	/**
	 * Panels
	 */
	private List<JPanel> panels = new ArrayList<>();

	/**
	 * Constructor
	 */
	public HosterTableModel() {
		this.addColumn("Hoster");
		this.addColumn("Version");
		this.addColumn("Type");
		this.addColumn("Settings");
		this.addColumn("Enabled");
		SettingsManager.instance().addSettingsListener(this);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 3 && this.getValueAt(row, 3) instanceof JPanel) {
			return true;
		}

		Host host = (Host)this.getValueAt(row, 0);
		if (column == 4 && host.canBeDisabled()) {
			return true;
		}

		return false;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 4) {
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
		Object data[] = new Object[5];
		data[0] = host;
		data[1] = host.getVersion();
		StringBuilder sbType = new StringBuilder();
		if (host.isDeveloper()) {
			sbType.append("Developer ");
		}
		sbType.append("Host");
		if (host instanceof IRedirect) {
			sbType.append(" / Redirect");
		}
		data[2] = sbType.toString();
		JPanel optionPanel = createOptionPanelForHost(host);
		data[3] = optionPanel != null ? optionPanel : "";
		panels.add(optionPanel);
		data[4] = host.isEnabled();
		this.addRow(data);
	}

	/**
	 * @param host Host
	 * @return Option-Panel for host or null if not available
	 */
	private JPanel createOptionPanelForHost(Host host) {
		if (host instanceof IHosterOptions) {
			IHosterOptions hostOptions = (IHosterOptions)host;

			JButton btn = new JButton(Localization.getString("Settings"), Icons.getTangoIcon("categories/preferences-system.png", 16));
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (DownloadQueueManager.instance().isDownloading()) {
						JOptionPane.showMessageDialog(Main.instance(), Localization.getString("HosterChangeWhileDownloading"), "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					hostOptions.openOptionsDialog();
				}
			});
			JPanel pnl = new JPanel();
			pnl.add(btn);
			return pnl;
		}
		return null;
	}

	/**
	 * Add Redirect
	 * 
	 * @param redirect Redirect
	 */
	public void addRow(final IRedirect redirect) {
		if (redirect == HostManager.instance().getHostRules()) {
			/*
			 * Prevent double insertion of HostRules, because it is a Host, but also a IRedirect
			 */
			return;
		}

		Object data[] = new Object[5];
		data[0] = redirect;
		data[1] = redirect.getVersion();
		data[2] = "Redirect";
		data[3] = "";
		data[4] = redirect.isEnabled();
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

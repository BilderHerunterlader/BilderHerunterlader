package ch.supertomcat.bh.gui;

import java.awt.Desktop;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;

import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.dialog.AboutDialog;

/**
 * BH About Dialog
 */
public class BHAboutDialog extends AboutDialog {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 */
	public BHAboutDialog(Window owner, SettingsManager settingsManager) {
		super(owner, Localization.getString("About"), Icons.getBHImage("BH.png"));

		setVisible(true);
	}

	@Override
	protected void configureComponents() {
		super.configureComponents();

		JButton btnWebsite = new JButton(Localization.getString("Website"), Icons.getTangoIcon("apps/internet-web-browser.png", 16));
		pnlButtons.add(btnWebsite);
		btnWebsite.addActionListener(e -> {
			String url = ApplicationProperties.getProperty("WebsiteURL");
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (IOException | URISyntaxException ex) {
					logger.error("Could not open URL: {}", url, ex);
				}
			} else {
				logger.error("Could not open URL, because Desktop is not supported: {}", url);
			}
		});
	}

	@Override
	protected void fillApplicationPathsInformation(StringBuilder sbApplicationInfo) {
		super.fillApplicationPathsInformation(sbApplicationInfo);

		File profilePath = new File(ApplicationProperties.getProperty("ProfilePath"));

		File databasePath = new File(ApplicationProperties.getProperty("DatabasePath"));
		if (!databasePath.equals(profilePath)) {
			sbApplicationInfo.append("Database Path: ");
			sbApplicationInfo.append(databasePath.getAbsolutePath());
			sbApplicationInfo.append("\n");
		}

		File settingsPath = new File(ApplicationProperties.getProperty("SettingsPath"));
		if (!settingsPath.equals(profilePath)) {
			sbApplicationInfo.append("Settings Path: ");
			sbApplicationInfo.append(settingsPath.getAbsolutePath());
			sbApplicationInfo.append("\n");
		}

		File downloadLogPath = new File(ApplicationProperties.getProperty("DownloadLogPath"));
		if (!downloadLogPath.equals(profilePath)) {
			sbApplicationInfo.append("DownloadLog Path: ");
			sbApplicationInfo.append(downloadLogPath.getAbsolutePath());
			sbApplicationInfo.append("\n");
		}
	}

	@Override
	protected void fillApplicationLicenseInformation(StringBuilder sbApplicationInfo) {
		sbApplicationInfo.append("E-Mail: ");
		sbApplicationInfo.append(ApplicationProperties.getProperty("MailAddress"));
		sbApplicationInfo.append("\n\n");

		super.fillApplicationLicenseInformation(sbApplicationInfo);
	}
}

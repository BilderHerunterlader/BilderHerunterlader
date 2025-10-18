package ch.supertomcat.bh.gui;

import java.awt.Window;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;

import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.dialog.about.AboutDialog;

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
		super(owner, Localization.getString("About"), BHIcons.getBHMultiResImage("BH.png"));

		setVisible(true);
	}

	@Override
	protected void configureComponents() {
		super.configureComponents();

		JButton btnWebsite = new JButton(Localization.getString("Website"), Icons.getTangoSVGIcon("apps/internet-web-browser.svg", 16));
		pnlButtons.add(btnWebsite);
		btnWebsite.addActionListener(e -> openURL(ApplicationProperties.getProperty("WebsiteURL")));
	}

	@Override
	protected void fillProgramInformation() {
		super.fillProgramInformation();
		pnlProgram.addProgramContactInformation(ApplicationProperties.getProperty("MailAddress"));
	}

	@Override
	protected void fillApplicationLicenseInformation() {
		super.fillApplicationLicenseInformation();
		pnlLicense.fillLicenseAdditionalInformation(ApplicationProperties.getProperty("AdditionalAboutText"));
	}

	@Override
	protected void fillApplicationPathsInformation() {
		super.fillApplicationPathsInformation();
		Path profilePath = Paths.get(ApplicationProperties.getProperty(ApplicationMain.PROFILE_PATH));
		pnlProgram.addProgramFolderInformation("Database Path:", ApplicationProperties.getProperty(ApplicationMain.DATABASE_PATH), profilePath);
		pnlProgram.addProgramFolderInformation("Settings Path:", ApplicationProperties.getProperty(ApplicationMain.SETTINGS_PATH), profilePath);
		pnlProgram.addProgramFolderInformation("DownloadLog Path:", ApplicationProperties.getProperty("DownloadLogPath"), profilePath);
	}
}

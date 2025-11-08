package ch.supertomcat.bh.importexport;

import java.awt.Component;
import java.io.File;
import java.nio.file.Path;

import javax.swing.JOptionPane;

import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.importexport.base.ImportExportBase;
import ch.supertomcat.bh.log.LogManager;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;

/**
 * Class for exporting logs
 */
public class ExportLogs extends ImportExportBase {
	/**
	 * Log Manager
	 */
	private final LogManager logManager;

	/**
	 * Constructor
	 * 
	 * @param parentComponent Parent Component
	 * @param mainWindowAccess Main Window Access
	 * @param logManager Log Manager
	 * @param settingsManager Settings Manager
	 */
	public ExportLogs(Component parentComponent, MainWindowAccess mainWindowAccess, LogManager logManager, SettingsManager settingsManager) {
		super(parentComponent, mainWindowAccess, settingsManager);
		this.logManager = logManager;
	}

	/**
	 * Export keywords to a tab-seperated textfile
	 */
	public void exportLogs() {
		File file = getTextFileFromFileChooserDialog(".+\\.txt", "Textfiles (.txt)", true);
		if (file != null) {
			setLastUsedExportPath(file);
			// export the keywords
			exportLogs(file.toPath());
		}
	}

	/**
	 * Export keywords to a tab-seperated textfile
	 * A line looks like this:
	 * Title Keywords AbsolutePath RelativePath isRelativePath
	 * If one of this values is not available, then there will be a #
	 * An example: There is no AbsolutePath then the line looks like this:
	 * Title Keywords # RelativePath isRelativePath
	 * This can be done for all values excepts the title!
	 * 
	 * @param file File
	 */
	public void exportLogs(Path file) {
		ProgressObserver pg = new ProgressObserver();
		try {
			mainWindowAccess.addProgressObserver(pg);
			pg.progressChanged(-1, -1, -1);
			pg.progressChanged(Localization.getString("LogExporting") + "...");
			logManager.exportAllEntriesToTextFile(file);
			mainWindowAccess.removeProgressObserver(pg);
			mainWindowAccess.setMessage(Localization.getString("LogExported"));
		} catch (Exception e) {
			logger.error("Could not export logs", e);
			mainWindowAccess.removeProgressObserver(pg);
			mainWindowAccess.setMessage(Localization.getString("LogExportFailed"));
		}
		JOptionPane.showMessageDialog(parentComponent, Localization.getString("LogExported"), Localization.getString("LogExport"), JOptionPane.INFORMATION_MESSAGE);
	}
}

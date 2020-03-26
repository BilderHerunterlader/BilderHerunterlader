package ch.supertomcat.bh.importexport.base;

import java.awt.Component;
import java.awt.Frame;
import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.MainWindowAccess;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.dialog.FileDialogUtil;

/**
 * Base class for Import or Export classes
 */
public abstract class ImportExportBase {
	/**
	 * Logger for this class
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Parent Component
	 */
	protected final Component parentComponent;

	/**
	 * Main Window Access
	 */
	protected final MainWindowAccess mainWindowAccess;

	/**
	 * Settings Manager
	 */
	protected final SettingsManager settingsManager;

	/**
	 * Constructor
	 * 
	 * @param parentComponent Parent Component
	 * @param mainWindowAccess Main Window Access
	 * @param settingsManager Settings Manager
	 */
	public ImportExportBase(Component parentComponent, MainWindowAccess mainWindowAccess, SettingsManager settingsManager) {
		this.parentComponent = parentComponent;
		this.mainWindowAccess = mainWindowAccess;
		this.settingsManager = settingsManager;
	}

	/**
	 * @param filterPattern Filter Pattern
	 * @param description Description
	 * @param save Save or Open
	 * @return Text-File
	 */
	protected File getTextFileFromFileChooserDialog(String filterPattern, String description, boolean save) {
		// Choose a file
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.getName().matches(filterPattern) || f.isDirectory();
			}

			@Override
			public String getDescription() {
				return description;
			}
		};
		if (save) {
			return FileDialogUtil.showFileSaveDialog(parentComponent, settingsManager.getLastUsedImportDialogPath(), filter);
		} else {
			return FileDialogUtil.showFileOpenDialog(parentComponent, settingsManager.getLastUsedImportDialogPath(), filter);
		}
	}

	/**
	 * @return Encoding Selection Dialog
	 */
	protected EncodingSelectionDialog getEncodingSelectionDialog() {
		return new EncodingSelectionDialog(parentComponent instanceof Frame ? (Frame)parentComponent : null);
	}
}

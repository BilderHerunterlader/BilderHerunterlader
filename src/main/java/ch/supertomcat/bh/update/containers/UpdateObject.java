package ch.supertomcat.bh.update.containers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import ch.supertomcat.bh.update.UpdateException;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.io.ZipUtil;

/**
 * Update Object
 */
public class UpdateObject {
	/**
	 * Update Action Type enum
	 */
	public enum UpdateActionType {
		/**
		 * ACTION_NONE
		 */
		ACTION_NONE,

		/**
		 * ACTION_NEW
		 */
		ACTION_NEW,

		/**
		 * ACTION_UPDATE
		 */
		ACTION_UPDATE,

		/**
		 * ACTION_REMOVE
		 */
		ACTION_REMOVE;
	}

	/**
	 * Update Type enum
	 */
	public enum UpdateType {
		/**
		 * TYPE_BH
		 */
		TYPE_BH(Localization.getString("UpdateTypeBH")),

		/**
		 * TYPE_RULE
		 */
		TYPE_RULE(Localization.getString("UpdateTypeRule")),

		/**
		 * TYPE_HOST_PLUGIN
		 */
		TYPE_HOST_PLUGIN(Localization.getString("UpdateTypeHostPlugin")),

		/**
		 * TYPE_REDIRECT_PLUGIN
		 */
		TYPE_REDIRECT_PLUGIN(Localization.getString("UpdateTypeRedirectPlugin"));

		private String name = "";

		private UpdateType(String name) {
			this.name = name;
		}

		/**
		 * Returns the name
		 * 
		 * @return name
		 */
		public String getName() {
			return name;
		}
	}

	/**
	 * action
	 */
	private UpdateActionType action = UpdateActionType.ACTION_NONE;

	/**
	 * type
	 */
	private UpdateType type = UpdateType.TYPE_BH;

	private String name = "";

	private String version = "";

	private String bhMinVersion = "";

	private String bhMaxVersion = "";

	private List<UpdateSourceFile> sources = null;

	private List<String> targets = null;

	private String comment = "";

	private boolean additionalRemove = false;

	private boolean deleteFileWritten = false;

	private String changelog = "";

	/**
	 * Constructor
	 * 
	 * @param type Type
	 * @param name Name
	 * @param version Version
	 * @param sources Sources
	 */
	public UpdateObject(UpdateType type, String name, String version, List<UpdateSourceFile> sources) {
		this(type, name, version, sources, "", "");
	}

	/**
	 * Constructor
	 * 
	 * @param type Type
	 * @param name Name
	 * @param version Version
	 * @param sources Sources
	 * @param bhMinVersion BH Minimum Version
	 * @param bhMaxVersion BH Maximum Version
	 */
	public UpdateObject(UpdateType type, String name, String version, List<UpdateSourceFile> sources, String bhMinVersion, String bhMaxVersion) {
		this.type = type;
		this.name = name;
		this.version = version;
		this.bhMinVersion = bhMinVersion;
		this.bhMaxVersion = bhMaxVersion;

		this.sources = sources;
		this.targets = new ArrayList<>();

		String targetDirectory = "";
		switch (this.type) {
			case TYPE_BH:
				targetDirectory = ApplicationProperties.getProperty(ApplicationMain.APPLICATION_PATH);
				break;
			case TYPE_HOST_PLUGIN, TYPE_REDIRECT_PLUGIN:
				targetDirectory = ApplicationProperties.getProperty(ApplicationMain.APPLICATION_PATH) + "hosts/";
				break;
			case TYPE_RULE:
				targetDirectory = ApplicationProperties.getProperty(ApplicationMain.APPLICATION_PATH) + "rules/";
				break;
			default:
				// nothing to do
				break;
		}

		for (int i = 0; i < this.sources.size(); i++) {
			if ((i > 0) && (this.sources.get(i).isDelete())) {
				additionalRemove = true;
			}

			this.targets.add(targetDirectory + this.sources.get(i).getTargetFilename());
		}
	}

	/**
	 * Returns the action
	 * 
	 * @return action
	 */
	public UpdateActionType getAction() {
		return action;
	}

	/**
	 * Sets the action
	 * 
	 * @param action action
	 */
	public void setAction(UpdateActionType action) {
		this.action = action;
	}

	/**
	 * Returns the type
	 * 
	 * @return type
	 */
	public UpdateType getType() {
		return type;
	}

	/**
	 * Returns the type as String
	 * 
	 * @return Type as String
	 */
	public String getTypeAsString() {
		return this.type.getName();
	}

	/**
	 * Returns the name
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the version
	 * 
	 * @return version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Returns the bhMinVersion
	 * 
	 * @return bhMinVersion
	 */
	public String getBhMinVersion() {
		return bhMinVersion;
	}

	/**
	 * Returns the bhMaxVersion
	 * 
	 * @return bhMaxVersion
	 */
	public String getBhMaxVersion() {
		return bhMaxVersion;
	}

	/**
	 * Returns the source
	 * 
	 * @return source
	 */
	public List<UpdateSourceFile> getSources() {
		return sources;
	}

	/**
	 * Returns the target
	 * 
	 * @return target
	 */
	public List<String> getTargets() {
		return targets;
	}

	/**
	 * Returns the comment
	 * 
	 * @return comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Sets the comment
	 * 
	 * @param comment comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Sets the changelog for the give language
	 * 
	 * @param changelog Changelog
	 */
	public void setChangeLog(String changelog) {
		this.changelog = changelog;
	}

	/**
	 * Returns the changelog
	 * 
	 * @return changelog for the given language or an empty String if not available
	 */
	public String getChangeLog() {
		return changelog;
	}

	/**
	 * Returns the additionalRemove
	 * 
	 * @return additionalRemove
	 */
	public boolean isAdditionalRemove() {
		return additionalRemove;
	}

	/**
	 * This method returns true if an update is required.
	 * You can use this method as a shortcut if you don't want to check the action.
	 * If the action is NONE false is returned.
	 * If the action is ADD, UPDATE or REMOVE true is returned
	 * 
	 * @return True if an update is required
	 */
	public boolean updateRequired() {
		return this.action != UpdateActionType.ACTION_NONE;
	}

	/**
	 * Download update
	 * If the action is NONE or REMOVE this method just returns false and does nothing
	 * If the type is BH or LIB then .new is added to the target filename. But the target-variable of this object is not changed.
	 * 
	 * @return True if successfull
	 * @throws UpdateException
	 */
	public boolean downloadUpdate() throws UpdateException {
		boolean b = false;

		if (additionalRemove) {
			writeDeleteFile();
		}

		UpdateSourceFile sourceFile = null;
		for (int i = 0; i < this.sources.size(); i++) {
			sourceFile = this.sources.get(i);
			if (sourceFile.isDelete()) {
				continue;
			}
			b = sourceFile.downloadUpdate(this.type, this.action, this.targets.get(i));
			if (!b) {
				return b;
			}
		}

		return b;
	}

	/**
	 * This method does the final update process. It does not download the Update!
	 * So you have to call the downloadUpdate-Method first.
	 * This Method does not check if the update was downloaded! In this case for REDIRECT_PLUGIN, HOST_PLUGIN and RULE
	 * true is returned (only if action is not REMOVE), for the other types an exception is thrown or false is returned.
	 * If the action is NONE this method just returns false and does nothing
	 * 
	 * @return True if successfull
	 * @throws Exception
	 */
	public boolean doFinalUpdateProcess() throws Exception {
		if (this.action == UpdateActionType.ACTION_NONE) {
			return false;
		}
		if ((this.action != UpdateActionType.ACTION_REMOVE) && (this.type == UpdateType.TYPE_HOST_PLUGIN || this.type == UpdateType.TYPE_REDIRECT_PLUGIN || this.type == UpdateType.TYPE_RULE)) {
			return true;
		}

		if (this.action == UpdateActionType.ACTION_REMOVE) {
			writeDeleteFile();
			return true;
		} else {
			for (int i = 0; i < this.targets.size(); i++) {
				String target = this.targets.get(i);

				Path fTempTarget = Paths.get(target);
				Path targetDir = fTempTarget.toAbsolutePath().getParent();
				try {
					ZipUtil.extractZipFile(fTempTarget, targetDir);
					Files.delete(fTempTarget);
				} catch (IOException e) {
					return false;
				}
			}

			return true;
		}
	}

	private synchronized void writeDeleteFile() throws UpdateException {
		if (deleteFileWritten) {
			return;
		}

		Path deleteUpdateFile = Paths.get(ApplicationProperties.getProperty(ApplicationMain.APPLICATION_PATH), "delete_update.txt");

		try (BufferedWriter writer = Files.newBufferedWriter(deleteUpdateFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
			for (int i = 0; i < this.targets.size(); i++) {
				if (this.action == UpdateActionType.ACTION_REMOVE || this.sources.get(i).isDelete()) {
					writer.write(this.targets.get(i) + "\n");
				}
			}
			writer.flush();
			deleteFileWritten = true;
		} catch (IOException e) {
			throw new UpdateException("Could not write delete update file: " + deleteUpdateFile, e);
		}
	}
}

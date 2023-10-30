package ch.supertomcat.bh.gui.rules.editor.filename;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import ch.supertomcat.bh.rules.xml.FilenameMode;
import ch.supertomcat.bh.rules.xml.FilenamePipeline;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.combobox.renderer.LocalizedEnumComboBoxRenderer;

/**
 * Rule-Pipeline-Panel
 */
public class RulePipelineFilenamePanel extends RulePipelineFilenamePanelBase<FilenamePipeline, FilenameMode> {
	private static final long serialVersionUID = 1L;

	/**
	 * CheckBox
	 */
	private JCheckBox chkUseContentDisposition = new JCheckBox(Localization.getString("UseContentDisposition"));

	/**
	 * CheckBox
	 */
	private JCheckBox chkReducePathLength = new JCheckBox(Localization.getString("ReducePathLength"), true);

	/**
	 * CheckBox
	 */
	private JCheckBox chkReduceFilenameLength = new JCheckBox(Localization.getString("ReduceFilenameLength"), true);

	/**
	 * Constructor
	 * 
	 * @param rule Rule
	 * @param pipe Pipeline
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 */
	public RulePipelineFilenamePanel(RuleDefinition rule, FilenamePipeline pipe, JDialog owner, SettingsManager settingsManager) {
		super(rule, pipe, owner, settingsManager);

		Map<FilenameMode, String> filenameModeLocalizationStrings = new HashMap<>();
		filenameModeLocalizationStrings.put(FilenameMode.CONTAINER_URL_FILENAME_PART, "filenameContainerUrlFilenamePart");
		filenameModeLocalizationStrings.put(FilenameMode.CONTAINER_URL, "filenameContainerUrl");
		filenameModeLocalizationStrings.put(FilenameMode.THUMBNAIL_URL_FILENAME_PART, "filenameThumbnailUrlFilenamePart");
		filenameModeLocalizationStrings.put(FilenameMode.THUMBNAIL_URL, "filenameThumbnailUrl");
		filenameModeLocalizationStrings.put(FilenameMode.CONTAINER_PAGE_SOURCECODE, "filenameContainerPageSourcecode");
		filenameModeLocalizationStrings.put(FilenameMode.DOWNLOAD_URL, "filenameDownloadUrl");
		filenameModeLocalizationStrings.put(FilenameMode.DOWNLOAD_URL_FILENAME_PART, "filenameDownloadUrlFilenamePart");
		filenameModeLocalizationStrings.put(FilenameMode.LAST_CONTAINER_URL_FILENAME_PART, "filenameLastContainerUrlFilenamePart");
		filenameModeLocalizationStrings.put(FilenameMode.LAST_CONTAINER_URL, "filenameLastContainerUrl");
		filenameModeLocalizationStrings.put(FilenameMode.FIRST_CONTAINER_PAGE_SOURCECODE, "filenameFirstContainerPageSourcecode");
		filenameModeLocalizationStrings.put(FilenameMode.LAST_CONTAINER_PAGE_SOURCECODE, "filenameLastContainerPageSourcecode");
		LocalizedEnumComboBoxRenderer<FilenameMode> filenameModeRenderer = new LocalizedEnumComboBoxRenderer<>(FilenameMode.class, filenameModeLocalizationStrings);
		cbFilenameMode.setRenderer(filenameModeRenderer);
		for (FilenameMode duplicateRemoveMode : FilenameMode.values()) {
			cbFilenameMode.addItem(duplicateRemoveMode);
		}
		cbFilenameMode.setSelectedItem(pipe.getMode());

		chkUseContentDisposition.setSelected(rule.isUseContentDisposition());
		chkReducePathLength.setSelected(rule.isReducePathLength());
		chkReduceFilenameLength.setSelected(rule.isReduceFilenameLength());

		JPanel pnlFilenameMode = new JPanel();
		pnlFilenameMode.add(lblSource);
		pnlFilenameMode.add(cbFilenameMode);

		pnlRB.setLayout(new GridLayout(4, 1));
		pnlRB.add(chkUseContentDisposition);
		pnlRB.add(chkReducePathLength);
		pnlRB.add(chkReduceFilenameLength);
		pnlRB.add(pnlFilenameMode);
		add(pnlRB, BorderLayout.NORTH);

		updateCompomentEnabledState();

		chkUseContentDisposition.addItemListener(e -> {
			updateCompomentEnabledState();
		});
	}

	@Override
	protected void updateCompomentEnabledState() {
		if (redirectEnabled) {
			pnlTable.disableTable();
		} else {
			pnlTable.enableTable();
		}
		chkUseContentDisposition.setEnabled(!redirectEnabled);
		cbFilenameMode.setEnabled(!redirectEnabled);
		lblSource.setEnabled(!redirectEnabled);

		if (!redirectEnabled) {
			if (chkUseContentDisposition.isSelected()) {
				pnlTable.disableTable();
			} else {
				pnlTable.enableTable();
			}
			cbFilenameMode.setEnabled(!chkUseContentDisposition.isSelected());
			lblSource.setEnabled(!chkUseContentDisposition.isSelected());
		}
	}

	@Override
	public boolean apply() {
		pipe.setMode((FilenameMode)cbFilenameMode.getSelectedItem());

		rule.setUseContentDisposition(chkUseContentDisposition.isSelected());
		rule.setReducePathLength(chkReducePathLength.isSelected());
		rule.setReduceFilenameLength(chkReduceFilenameLength.isSelected());

		return super.apply();
	}
}

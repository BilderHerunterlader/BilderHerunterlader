package ch.supertomcat.bh.gui.rules.editor.filename;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JPanel;

import ch.supertomcat.bh.rules.xml.FilenameDownloadSelectionMode;
import ch.supertomcat.bh.rules.xml.FilenameDownloadSelectionPipeline;
import ch.supertomcat.bh.rules.xml.RuleDefinition;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.combobox.renderer.LocalizedEnumComboBoxRenderer;

/**
 * Rule-Pipeline-Panel
 */
public class RulePipelineFilenameDownloadSelectionPanel extends RulePipelineFilenamePanelBase<FilenameDownloadSelectionPipeline, FilenameDownloadSelectionMode> {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param rule Rule
	 * @param pipe Pipeline
	 * @param owner Owner
	 * @param settingsManager Settings Manager
	 */
	@SuppressWarnings("unchecked")
	public RulePipelineFilenameDownloadSelectionPanel(RuleDefinition rule, FilenameDownloadSelectionPipeline pipe, JDialog owner, SettingsManager settingsManager) {
		super(rule, pipe, owner, settingsManager);

		Map<FilenameDownloadSelectionMode, String> filenameModeLocalizationStrings = new HashMap<>();
		filenameModeLocalizationStrings.put(FilenameDownloadSelectionMode.CONTAINER_URL_FILENAME_PART, "filenameContainerUrlFilenamePart");
		filenameModeLocalizationStrings.put(FilenameDownloadSelectionMode.CONTAINER_URL, "filenameContainerUrl");
		LocalizedEnumComboBoxRenderer<FilenameDownloadSelectionMode> filenameModeRenderer = new LocalizedEnumComboBoxRenderer<>(FilenameDownloadSelectionMode.class, filenameModeLocalizationStrings);
		cbFilenameMode.setRenderer(filenameModeRenderer);
		for (FilenameDownloadSelectionMode duplicateRemoveMode : FilenameDownloadSelectionMode.values()) {
			cbFilenameMode.addItem(duplicateRemoveMode);
		}
		cbFilenameMode.setSelectedItem(pipe.getMode());

		JPanel pnlFilenameDownloadSelectionMode = new JPanel();
		pnlFilenameDownloadSelectionMode.add(lblSource);
		pnlFilenameDownloadSelectionMode.add(cbFilenameMode);
		pnlRB.setLayout(new GridLayout(1, 1));
		pnlRB.add(pnlFilenameDownloadSelectionMode);
		add(pnlRB, BorderLayout.NORTH);
	}

	@Override
	public boolean apply() {
		pipe.setMode((FilenameDownloadSelectionMode)cbFilenameMode.getSelectedItem());
		return super.apply();
	}

	@Override
	protected void updateCompomentEnabledState() {
		// Nothing to do
	}
}

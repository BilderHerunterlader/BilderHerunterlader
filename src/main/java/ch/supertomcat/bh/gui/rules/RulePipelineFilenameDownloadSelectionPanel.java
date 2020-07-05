package ch.supertomcat.bh.gui.rules;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JPanel;

import ch.supertomcat.bh.gui.renderer.LocalizedEnumComboBoxRenderer;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.RulePipelineFilenameDownloadSelection;
import ch.supertomcat.bh.rules.xml.FilenameDownloadSelectionMode;
import ch.supertomcat.bh.settings.SettingsManager;

/**
 * Rule-Pipeline-Panel
 */
public class RulePipelineFilenameDownloadSelectionPanel extends RulePipelineFilenamePanelBase<RulePipelineFilenameDownloadSelection, FilenameDownloadSelectionMode> {
	/**
	 * UID
	 */
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
	public RulePipelineFilenameDownloadSelectionPanel(Rule rule, RulePipelineFilenameDownloadSelection pipe, JDialog owner, SettingsManager settingsManager) {
		super(rule, pipe, owner, settingsManager);

		Map<FilenameDownloadSelectionMode, String> filenameModeLocalizationStrings = new HashMap<>();
		filenameModeLocalizationStrings.put(FilenameDownloadSelectionMode.CONTAINER_URL_FILENAME_PART, "filenameContainerUrlFilenamePart");
		filenameModeLocalizationStrings.put(FilenameDownloadSelectionMode.CONTAINER_URL, "filenameContainerUrl");
		LocalizedEnumComboBoxRenderer<FilenameDownloadSelectionMode> filenameModeRenderer = new LocalizedEnumComboBoxRenderer<>(FilenameDownloadSelectionMode.class, filenameModeLocalizationStrings);
		cbFilenameMode.setRenderer(filenameModeRenderer);
		for (FilenameDownloadSelectionMode duplicateRemoveMode : FilenameDownloadSelectionMode.values()) {
			cbFilenameMode.addItem(duplicateRemoveMode);
		}
		cbFilenameMode.setSelectedItem(pipe.getDefinition().getMode());

		JPanel pnlFilenameDownloadSelectionMode = new JPanel();
		pnlFilenameDownloadSelectionMode.add(lblSource);
		pnlFilenameDownloadSelectionMode.add(cbFilenameMode);
		pnlRB.setLayout(new GridLayout(1, 1));
		pnlRB.add(pnlFilenameDownloadSelectionMode);
		add(pnlRB, BorderLayout.NORTH);
	}

	@Override
	public void apply() {
		pipe.getDefinition().setMode((FilenameDownloadSelectionMode)cbFilenameMode.getSelectedItem());
	}
}

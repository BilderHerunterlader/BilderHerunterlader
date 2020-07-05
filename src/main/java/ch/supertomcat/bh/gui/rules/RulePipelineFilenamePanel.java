package ch.supertomcat.bh.gui.rules;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.util.Arrays;

import javax.swing.JDialog;
import javax.swing.JPanel;

import ch.supertomcat.bh.gui.renderer.LocalizedEnumComboBoxRenderer;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.RulePipelineFilename;
import ch.supertomcat.bh.rules.xml.FilenameMode;
import ch.supertomcat.bh.settings.SettingsManager;

/**
 * Rule-Pipeline-Panel
 */
public class RulePipelineFilenamePanel extends RulePipelineFilenamePanelBase<RulePipelineFilename, FilenameMode> {
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
	public RulePipelineFilenamePanel(Rule rule, RulePipelineFilename pipe, JDialog owner, SettingsManager settingsManager) {
		super(rule, pipe, owner, settingsManager);

		LocalizedEnumComboBoxRenderer<FilenameMode> filenameModeRenderer = new LocalizedEnumComboBoxRenderer<>(FilenameMode.class, Arrays
				.asList("filenameContainerUrlFilenamePart", "filenameContainerUrl", "filenameThumbnailUrlFilenamePart", "filenameThumbnailUrl", "filenameContainerPageSourcecode", "filenameDownloadUrl", "filenameDownloadUrlFilenamePart", "filenameLastContainerUrlFilenamePart", "filenameLastContainerUrl", "filenameFirstContainerPageSourcecode", "filenameLastContainerPageSourcecode"));
		cbFilenameMode.setRenderer(filenameModeRenderer);
		for (FilenameMode duplicateRemoveMode : FilenameMode.values()) {
			cbFilenameMode.addItem(duplicateRemoveMode);
		}
		cbFilenameMode.setSelectedItem(pipe.getDefinition().getMode());

		chkUseContentDisposition.setSelected(rule.getDefinition().isUseContentDisposition());
		chkReducePathLength.setSelected(rule.getDefinition().isReducePathLength());
		chkReduceFilenameLength.setSelected(rule.getDefinition().isReduceFilenameLength());
		table.setEnabled(!chkUseContentDisposition.isSelected());
		btnNew.setEnabled(!chkUseContentDisposition.isSelected());
		btnEdit.setEnabled(!chkUseContentDisposition.isSelected());
		btnUp.setEnabled(!chkUseContentDisposition.isSelected());
		btnDown.setEnabled(!chkUseContentDisposition.isSelected());
		btnDelete.setEnabled(!chkUseContentDisposition.isSelected());
		cbFilenameMode.setEnabled(!chkUseContentDisposition.isSelected());
		lblSource.setEnabled(!chkUseContentDisposition.isSelected());
		JPanel pnlFilenameMode = new JPanel();
		pnlFilenameMode.add(lblSource);
		pnlFilenameMode.add(cbFilenameMode);
		pnlRB.setLayout(new GridLayout(4, 1));
		pnlRB.add(chkUseContentDisposition);
		pnlRB.add(chkReducePathLength);
		pnlRB.add(chkReduceFilenameLength);
		pnlRB.add(pnlFilenameMode);
		add(pnlRB, BorderLayout.NORTH);
	}

	@Override
	public void apply() {
		rule.getDefinition().setUseContentDisposition(chkUseContentDisposition.isSelected());
		rule.getDefinition().setReducePathLength(chkReducePathLength.isSelected());
		rule.getDefinition().setReduceFilenameLength(chkReduceFilenameLength.isSelected());
		pipe.getDefinition().setMode((FilenameMode)cbFilenameMode.getSelectedItem());
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		super.itemStateChanged(e);
		if (e.getSource() == chkUseContentDisposition) {
			cbFilenameMode.setEnabled(!chkUseContentDisposition.isSelected());
		}
	}

	@Override
	public void redirectEnabled(boolean enabled) {
		super.redirectEnabled(enabled);
		if (!enabled) {
			cbFilenameMode.setEnabled(!chkUseContentDisposition.isSelected());
		}
	}
}

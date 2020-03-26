package ch.supertomcat.bh.gui.keywords;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.dialog.FileDialogUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Editor-Component for Keywords
 */
public class KeywordCellEditorComponent extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * TextField
	 */
	private JTextField txtPath = new JTextField(15);

	/**
	 * Button
	 */
	private JButton btnPath = new JButton("...");

	/**
	 * Settings Manager
	 */
	private final SettingsManager settingsManager;

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 */
	public KeywordCellEditorComponent(SettingsManager settingsManager) {
		super(new BorderLayout(5, 5));
		this.settingsManager = settingsManager;
		add(txtPath, BorderLayout.CENTER);
		add(btnPath, BorderLayout.EAST);

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtPath);
		txtPath.setEditable(false);
		btnPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String dir = txtPath.getText();
				File fDir = new File(dir);
				if (!(fDir.exists() && fDir.isDirectory())) {
					dir = settingsManager.getSavePath();
				}
				File file = FileDialogUtil.showFolderOpenDialog(KeywordCellEditorComponent.this, dir, null);
				if (file != null) {
					String folder = file.getAbsolutePath() + FileUtil.FILE_SEPERATOR;
					txtPath.setText(folder);
				}
			}
		});
	}

	/**
	 * Set-Method
	 * 
	 * @param b True/False
	 */
	public void setRelative(boolean b) {
		txtPath.setEditable(b);
		btnPath.setEnabled(!b);
		btnPath.setVisible(!b);
	}

	/**
	 * Set-Method
	 * 
	 * @param text Path
	 */
	public void setText(String text) {
		txtPath.setText(text);
	}

	/**
	 * Get-Method
	 * 
	 * @return Path
	 */
	public String getPath() {
		String newPath = txtPath.getText();
		newPath = BHUtil.filterPath(newPath, settingsManager);
		newPath = FileUtil.reducePathLength(newPath);
		return newPath;
	}

	@Override
	public String toString() {
		return getPath();
	}
}

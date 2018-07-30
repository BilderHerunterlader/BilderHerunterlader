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
import ch.supertomcat.supertomcattools.fileiotools.FileTool;
import ch.supertomcat.supertomcattools.guitools.FileDialogTool;
import ch.supertomcat.supertomcattools.guitools.copyandpaste.JTextComponentCopyAndPaste;

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
	 * Constructor
	 */
	public KeywordCellEditorComponent() {
		super(new BorderLayout(5, 5));
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
					dir = SettingsManager.instance().getSavePath();
				}
				File file = FileDialogTool.showFolderDialog(KeywordCellEditorComponent.this, dir, null);
				if (file != null) {
					String folder = file.getAbsolutePath() + FileTool.FILE_SEPERATOR;
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
		newPath = BHUtil.filterPath(newPath);
		newPath = FileTool.reducePathLength(newPath);
		return newPath;
	}

	@Override
	public String toString() {
		return getPath();
	}
}

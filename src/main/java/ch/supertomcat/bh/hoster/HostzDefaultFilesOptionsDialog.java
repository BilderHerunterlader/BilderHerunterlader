package ch.supertomcat.bh.hoster;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import ch.supertomcat.bh.gui.Main;
import ch.supertomcat.supertomcattools.guitools.Localization;

/**
 * HostzDefaultFilesOptionsDialog
 */
public class HostzDefaultFilesOptionsDialog extends JDialog implements ActionListener, ItemListener {
	private static final long serialVersionUID = 1391940632265954571L;

	private JPanel pnlButtons = new JPanel();

	private JButton btnOK = new JButton("OK");

	private JButton btnCancel = new JButton("Cancel");

	private JPanel pnlCenter = new JPanel();

	private JCheckBox cbContentType;

	private JCheckBox cbAllTypes;

	private JCheckBox cbImages;

	private JCheckBox cbVideo;

	private JCheckBox cbAudio;

	private JCheckBox cbArchive;

	private boolean okPressed = false;

	/**
	 * Constructor
	 * 
	 * @param checkContentType Check Content Type
	 * @param allFileTypes All File Types accepted
	 * @param images Image Files accepted
	 * @param video Video Files accepted
	 * @param audio Audio Files accepted
	 * @param archive Archive Files accepted
	 */
	protected HostzDefaultFilesOptionsDialog(boolean checkContentType, boolean allFileTypes, boolean images, boolean video, boolean audio, boolean archive) {
		setTitle(Localization.getString("DirectLinkedFiles"));
		setModal(true);

		pnlButtons = new JPanel();
		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);

		cbContentType = new JCheckBox(Localization.getString("CheckCTDefaultImages"), checkContentType);
		String strImages = Localization.getString("Images") + " (bmp, gif, jpe, jpg, jpeg, png, tif, tiff)";
		String strVideo = Localization.getString("Videos") + " (3g2, 3gp, 3gp2, 3gpp, amr, asf, divx, evo, flv, hdmov, m2t, m2ts, m2v";
		String strAudio = Localization.getString("Audio") + " (aac, ac3, au, dts, flac, m1a, m2a, m4a, m4b, mid, midi, mka, mp2, mp3";
		String strArchive = Localization.getString("Archives") + " (7z, arj, bz2, bzip2, cab, cpio, deb, dmg, gz, gzip, hfs, iso, lha, lzh, lzma";
		cbAllTypes = new JCheckBox(Localization.getString("AllFileTypes"), allFileTypes);
		cbImages = new JCheckBox(strImages, images);
		cbVideo = new JCheckBox(strVideo + "...)", video);
		cbAudio = new JCheckBox(strAudio + "...)", audio);
		cbArchive = new JCheckBox(strArchive + "...)", archive);
		strVideo += ", m4v, mkv, m1v, mov, mp2v, mp4, mpe, mpeg, mpg, mts, ogm, ogv, pva, pss, qt, rm, ram, rpm, rmm, ts, tp, tpr, vob, wmv, wmp)";
		strAudio += ", mpa, oga, ogg, ra, rmi, snd, wav, wma)";
		strArchive += ", rar, rpm, split, swm, tar, taz, tbz, tbz2, tgz, tpz, wim, xar, z, zip)";
		cbImages.setToolTipText(strImages);
		cbVideo.setToolTipText(strVideo);
		cbAudio.setToolTipText(strAudio);
		cbArchive.setToolTipText(strArchive);

		cbAllTypes.addItemListener(this);
		cbContentType.setEnabled(!cbAllTypes.isSelected());
		cbImages.setEnabled(!cbAllTypes.isSelected());
		cbVideo.setEnabled(!cbAllTypes.isSelected());
		cbAudio.setEnabled(!cbAllTypes.isSelected());
		cbArchive.setEnabled(!cbAllTypes.isSelected());

		pnlCenter.setLayout(new GridLayout(7, 1));
		pnlCenter.add(cbAllTypes);
		pnlCenter.add(cbContentType);
		pnlCenter.add(cbImages);
		pnlCenter.add(cbVideo);
		pnlCenter.add(cbAudio);
		pnlCenter.add(cbArchive);
		JTextArea txtAddPatterns = new JTextArea(Localization.getString("DirectLinkedFilesAddPatterns"));
		txtAddPatterns.setEditable(false);
		txtAddPatterns.setFont(new Font("Dialog.plain", Font.PLAIN, 12));
		pnlCenter.add(txtAddPatterns);

		btnOK.addActionListener(this);
		btnCancel.addActionListener(this);

		setLayout(new BorderLayout());
		add(pnlButtons, BorderLayout.SOUTH);
		add(pnlCenter, BorderLayout.CENTER);

		pack();
		setLocationRelativeTo(Main.instance());
		setVisible(true);
	}

	/**
	 * Get-Method
	 * 
	 * @return TRUE/FALSE
	 */
	public boolean isCheckContentType() {
		return cbContentType.isSelected();
	}

	/**
	 * Get-Method
	 * 
	 * @return TRUE/FALSE
	 */
	public boolean isAllFileTypes() {
		return cbAllTypes.isSelected();
	}

	/**
	 * Get-Method
	 * 
	 * @return TRUE/FALSE
	 */
	public boolean isImage() {
		return cbImages.isSelected();
	}

	/**
	 * Get-Method
	 * 
	 * @return TRUE/FALSE
	 */
	public boolean isVideo() {
		return cbVideo.isSelected();
	}

	/**
	 * Get-Method
	 * 
	 * @return TRUE/FALSE
	 */
	public boolean isAudio() {
		return cbAudio.isSelected();
	}

	/**
	 * Get-Method
	 * 
	 * @return TRUE/FALSE
	 */
	public boolean isArchive() {
		return cbArchive.isSelected();
	}

	/**
	 * Get-Method
	 * 
	 * @return TRUE/FALSE
	 */
	public boolean isOkPressed() {
		return okPressed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnOK) {
			okPressed = true;
			this.dispose();
		} else if (e.getSource() == btnCancel) {
			this.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == cbAllTypes) {
			cbContentType.setEnabled(!cbAllTypes.isSelected());
			cbImages.setEnabled(!cbAllTypes.isSelected());
			cbVideo.setEnabled(!cbAllTypes.isSelected());
			cbAudio.setEnabled(!cbAllTypes.isSelected());
			cbArchive.setEnabled(!cbAllTypes.isSelected());
		}
	}
}

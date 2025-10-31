package ch.supertomcat.bh.gui.settings.tabs;

import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;

import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.xml.DetectionSettings;
import ch.supertomcat.bh.settings.xml.Settings;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.layout.GridBagLayoutUtil;

/**
 * Detection Settings Panel
 */
public class DetectionSettingsPanel extends SettingsPanelBase {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * CheckBox
	 */
	private JCheckBox cbCheckContentType;

	/**
	 * CheckBox
	 */
	private JCheckBox cbAllTypes;

	/**
	 * CheckBox
	 */
	private JCheckBox cbImage;

	/**
	 * CheckBox
	 */
	private JCheckBox cbVideo;

	/**
	 * CheckBox
	 */
	private JCheckBox cbAudio;

	/**
	 * CheckBox
	 */
	private JCheckBox cbArchive;

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 */
	public DetectionSettingsPanel(SettingsManager settingsManager) {
		super(settingsManager);
		// TODO Auto-generated constructor stub

		cbCheckContentType = new JCheckBox(Localization.getString("CheckCTDefaultImages"), false);
		String strImages = Localization.getString("Images") + " (bmp, gif, jpe, jpg, jpeg, png, tif, tiff, webp)";
		String strVideo = Localization.getString("Videos") + " (3g2, 3gp, 3gp2, 3gpp, amr, asf, divx, evo, flv, hdmov, m2t, m2ts, m2v";
		String strAudio = Localization.getString("Audio") + " (aac, ac3, au, dts, flac, m1a, m2a, m4a, m4b, mid, midi, mka, mp2, mp3";
		String strArchive = Localization.getString("Archives") + " (7z, arj, bz2, bzip2, cab, cpio, deb, dmg, gz, gzip, hfs, iso, lha, lzh, lzma";
		cbAllTypes = new JCheckBox(Localization.getString("AllFileTypes"), false);
		cbImage = new JCheckBox(strImages, true);
		cbVideo = new JCheckBox(strVideo + "...)", true);
		cbAudio = new JCheckBox(strAudio + "...)", true);
		cbArchive = new JCheckBox(strArchive + "...)", true);
		strVideo += ", m4v, mkv, m1v, mov, mp2v, mp4, mpe, mpeg, mpg, mts, ogm, ogv, pva, pss, qt, rm, ram, rpm, rmm, ts, tp, tpr, vob, wmv, wmp)";
		strAudio += ", mpa, oga, ogg, ra, rmi, snd, wav, wma)";
		strArchive += ", rar, rpm, split, swm, tar, taz, tbz, tbz2, tgz, tpz, wim, xar, z, zip)";
		cbImage.setToolTipText(strImages);
		cbVideo.setToolTipText(strVideo);
		cbAudio.setToolTipText(strAudio);
		cbArchive.setToolTipText(strArchive);

		cbAllTypes.addItemListener(e -> {
			cbCheckContentType.setEnabled(!cbAllTypes.isSelected());
			cbImage.setEnabled(!cbAllTypes.isSelected());
			cbVideo.setEnabled(!cbAllTypes.isSelected());
			cbAudio.setEnabled(!cbAllTypes.isSelected());
			cbArchive.setEnabled(!cbAllTypes.isSelected());
		});
		cbCheckContentType.setEnabled(!cbAllTypes.isSelected());
		cbImage.setEnabled(!cbAllTypes.isSelected());
		cbVideo.setEnabled(!cbAllTypes.isSelected());
		cbAudio.setEnabled(!cbAllTypes.isSelected());
		cbArchive.setEnabled(!cbAllTypes.isSelected());

		GridBagConstraints gbc = new GridBagConstraints();

		int i = 0;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		// GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblStdSavePath, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cbAllTypes, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		// GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblRememberLastUsedPath, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cbCheckContentType, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		// GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblSubdirsEnabled, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cbImage, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		// GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblSubdirsResolutionMode, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cbVideo, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		// GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblSubdirsResolutionMode, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cbAudio, this);
		i++;
		gbc = gblt.getGBC(0, i, 1, 1, 0.0, 0.0);
		// GridBagLayoutUtil.addItemToPanel(gbl, gbc, lblSubdirsResolutionMode, this);
		gbc = gblt.getGBC(1, i, 1, 1, 0.0, 0.0);
		GridBagLayoutUtil.addItemToPanel(gbl, gbc, cbArchive, this);
		i++;
		// TODO Add patterns
	}

	@Override
	public void init() {
		Settings settings = settingsManager.getSettings();

		DetectionSettings detectionSettings = settings.getDetectionSettings();

		cbCheckContentType.setSelected(detectionSettings.isCheckContentType());
		cbAllTypes.setSelected(detectionSettings.isAllFileTypes());
		cbImage.setSelected(detectionSettings.isImage());
		cbVideo.setSelected(detectionSettings.isVideo());
		cbAudio.setSelected(detectionSettings.isAudio());
		cbArchive.setSelected(detectionSettings.isArchive());
	}

	@Override
	public void applySettings() {
		Settings settings = settingsManager.getSettings();

		DetectionSettings detectionSettings = settings.getDetectionSettings();

		detectionSettings.setCheckContentType(cbCheckContentType.isSelected());
		detectionSettings.setAllFileTypes(cbAllTypes.isSelected());
		detectionSettings.setImage(cbImage.isSelected());
		detectionSettings.setVideo(cbVideo.isSelected());
		detectionSettings.setAudio(cbAudio.isSelected());
		detectionSettings.setArchive(cbArchive.isSelected());
	}
}

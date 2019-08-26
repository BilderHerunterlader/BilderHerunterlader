package ch.supertomcat.bh.hoster.captcha;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostIOException;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Klasse for Catpcha-Dialogs
 */
public class CaptchaDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private JPanel pnlButtons = new JPanel();

	private JLabel lblMessage = new JLabel("");

	private JPanel pnlCenter = new JPanel();

	private JLabel lblCaptcha = new JLabel("");

	private JTextField txtCaptcha = new JTextField("", 10);

	private JButton btnOK = new JButton("OK");

	/**
	 * Constructor
	 * The Constructor does nothing, you have to call the init-Method do Display the dialog!
	 * 
	 * @param title Title
	 * @param owner Owner
	 */
	public CaptchaDialog(String title, Dialog owner) {
		super(owner, title, true);
	}

	/**
	 * Constructor
	 * The Constructor does nothing, you have to call the init-Method do Display the dialog!
	 * 
	 * @param title Title
	 * @param owner Owner
	 */
	public CaptchaDialog(String title, Frame owner) {
		super(owner, title, true);
	}

	/**
	 * @param captchaURL Captcha URL
	 * @param bWrongCode Wrong Code
	 * @return CaptchaUserInput
	 * @throws HostException
	 */
	public String init(String captchaURL, boolean bWrongCode) throws HostException {
		lblMessage.setForeground(Color.RED);
		if (bWrongCode) {
			lblMessage.setText(Localization.getString("CaptchaWrongCode"));
		}
		this.getRootPane().setDefaultButton(btnOK);

		BufferedImage imgCaptcha = null;
		try {
			imgCaptcha = ImageIO.read(new URL(captchaURL));
			if (imgCaptcha == null) {
				throw new HostIOException("Could not download Captcha-Image");
			}
		} catch (IOException ioe) {
			throw new HostIOException("Could not download Captcha-Image", ioe);
		}

		lblCaptcha.setIcon(new ImageIcon(imgCaptcha));

		pnlButtons.add(btnOK);

		pnlCenter.setLayout(new FlowLayout());
		pnlCenter.add(lblCaptcha);
		pnlCenter.add(txtCaptcha);
		btnOK.addActionListener(e -> dispose());

		this.setLayout(new BorderLayout());
		this.add(pnlButtons, BorderLayout.SOUTH);
		this.add(pnlCenter, BorderLayout.CENTER);
		this.add(lblMessage, BorderLayout.NORTH);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
		return txtCaptcha.getText();
	}
}

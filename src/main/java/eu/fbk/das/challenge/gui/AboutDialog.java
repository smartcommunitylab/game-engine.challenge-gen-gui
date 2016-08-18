package eu.fbk.das.challenge.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class AboutDialog extends JDialog {

	private static final long serialVersionUID = -6244602179383845535L;

	/**
	 * Create the dialog.
	 */
	public AboutDialog() {
		setBounds(100, 100, 378, 247);

		setLocationRelativeTo(null);

		JLabel lblCreatedByDas = new JLabel("Created by DAS Unit in FBK - 2016");
		lblCreatedByDas.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblCreatedByDas.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(lblCreatedByDas, BorderLayout.NORTH);

		try {
			URL resource = getClass().getResource("/images/das-logo-en.png");
			BufferedImage image = ImageIO.read(resource);
			JLabel picLabel = new JLabel(new ImageIcon(image));
			lblCreatedByDas.setLabelFor(picLabel);
			picLabel.setBorder(null);
			picLabel.setBounds(10, 11, 644, 226);
			getContentPane().add(picLabel);
			getContentPane().repaint();
		} catch (Exception e) {
			// logger.error(e.getMessage(), e);
		}
	}

}

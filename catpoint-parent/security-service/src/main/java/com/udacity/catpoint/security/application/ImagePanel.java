package com.udacity.catpoint.security.application;

import com.udacity.catpoint.security.data.AlarmStatus;
import com.udacity.catpoint.security.service.SecurityService;
import com.udacity.catpoint.security.service.StyleService;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class ImagePanel extends JPanel implements StatusListener {
    private final SecurityService securityService;
    private final JLabel cameraHeader;
    private final JLabel cameraLabel;
    private BufferedImage currentCameraImage;

    private static final int IMAGE_WIDTH = 300;
    private static final int IMAGE_HEIGHT = 225;

    public ImagePanel(SecurityService securityService) {
        super();
        setLayout(new MigLayout());

        this.securityService = Objects.requireNonNull(securityService, "SecurityService cannot be null");
        securityService.addStatusListener(this);

        this.cameraHeader = new JLabel("Camera Feed");
        cameraHeader.setFont(StyleService.HEADING_FONT);

        this.cameraLabel = new JLabel();
        cameraLabel.setBackground(Color.WHITE);
        cameraLabel.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
        cameraLabel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        JButton addPictureButton = createAddPictureButton();

        JButton scanPictureButton = createScanPictureButton();

        add(cameraHeader, "span 3, wrap");
        add(cameraLabel, "span 3, wrap");
        add(addPictureButton);
        add(scanPictureButton);
    }

    private JButton createAddPictureButton() {
        JButton button = new JButton("Refresh Camera");
        button.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("."));
            chooser.setDialogTitle("Select Picture");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File selectedFile = chooser.getSelectedFile();
                    if (selectedFile != null) {
                        BufferedImage image = ImageIO.read(selectedFile);
                        if (image != null) {
                            currentCameraImage = image;
                            Image scaledImage = new ImageIcon(image)
                                    .getImage()
                                    .getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
                            cameraLabel.setIcon(new ImageIcon(scaledImage));
                        }
                    }
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(null, "Invalid image selected: " + ioe.getMessage());
                }
                repaint();
            }
        });
        return button;
    }

    private JButton createScanPictureButton() {
        JButton button = new JButton("Scan Picture");
        button.addActionListener(e -> {
            if (currentCameraImage != null) {
                securityService.processImage(currentCameraImage);
            } else {
                JOptionPane.showMessageDialog(null, "No image selected to scan");
            }
        });
        return button;
    }

    @Override
    public void notify(AlarmStatus status) {
        // No behavior necessary
    }

    @Override
    public void catDetected(boolean catDetected) {
        EventQueue.invokeLater(() -> {
            if (catDetected) {
                cameraHeader.setText("DANGER - CAT DETECTED");
            } else {
                cameraHeader.setText("Camera Feed - No Cats Detected");
            }
        });
    }

    @Override
    public void sensorStatusChanged() {
        // No behavior necessary
    }
}
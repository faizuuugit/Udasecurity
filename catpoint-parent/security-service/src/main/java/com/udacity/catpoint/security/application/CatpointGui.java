package com.udacity.catpoint.security.application;

import com.udacity.catpoint.security.data.PretendDatabaseSecurityRepositoryImpl;
import com.udacity.catpoint.security.data.SecurityRepository;
import com.udacity.catpoint.image.FakeImageService;
import com.udacity.catpoint.security.service.SecurityService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class CatpointGui extends JFrame {
    private final transient SecurityRepository securityRepository = new PretendDatabaseSecurityRepositoryImpl();
    private final transient FakeImageService imageService = new FakeImageService();
    private final transient SecurityService securityService = new SecurityService(securityRepository, imageService);
    private final DisplayPanel displayPanel = new DisplayPanel(securityService);
    private final ControlPanel controlPanel = new ControlPanel(securityService);
    private final SensorPanel sensorPanel = new SensorPanel(securityService);
    private final ImagePanel imagePanel = new ImagePanel(securityService);

    public CatpointGui() {
        setLocation(100, 100);
        setSize(600, 850);
        setTitle("Very Secure App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new MigLayout());
        mainPanel.add(displayPanel, "wrap");
        mainPanel.add(imagePanel, "wrap");
        mainPanel.add(controlPanel, "wrap");
        mainPanel.add(sensorPanel);

        getContentPane().add(mainPanel);
    }
}
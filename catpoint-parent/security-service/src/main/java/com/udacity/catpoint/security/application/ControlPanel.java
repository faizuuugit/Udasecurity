package com.udacity.catpoint.security.application;

import com.udacity.catpoint.security.data.ArmingStatus;
import com.udacity.catpoint.security.service.SecurityService;
import com.udacity.catpoint.security.service.StyleService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class ControlPanel extends JPanel {

    private final SecurityService securityService;
    private final Map<ArmingStatus, JButton> buttonMap;

    public ControlPanel(SecurityService securityService) {
        super();
        setLayout(new MigLayout());

        this.securityService = Objects.requireNonNull(securityService,
                "SecurityService cannot be null - this is a required dependency");

        this.buttonMap = createImmutableButtonMap();

        initializeUI();
    }

    private Map<ArmingStatus, JButton> createImmutableButtonMap() {
        Map<ArmingStatus, JButton> map = new EnumMap<>(ArmingStatus.class);

        Arrays.stream(ArmingStatus.values()).forEach(status -> {
            JButton button = new JButton(status.getDescription());
            button.addActionListener(e -> handleStatusChange(status));
            map.put(status, button);
        });

        return new EnumMap<>(map);
    }

    private void handleStatusChange(ArmingStatus newStatus) {
        securityService.setArmingStatus(newStatus);
        updateButtonColors(newStatus);
    }

    private void initializeUI() {
        JLabel panelLabel = new JLabel("System Control");
        panelLabel.setFont(StyleService.HEADING_FONT);
        add(panelLabel, "span 3, wrap");

        Arrays.stream(ArmingStatus.values())
                .map(buttonMap::get)
                .forEach(this::add);

        updateButtonColors(securityService.getArmingStatus());
    }

    private void updateButtonColors(ArmingStatus currentStatus) {
        buttonMap.forEach((status, button) ->
                button.setBackground(status == currentStatus ? status.getColor() : null));
    }
}
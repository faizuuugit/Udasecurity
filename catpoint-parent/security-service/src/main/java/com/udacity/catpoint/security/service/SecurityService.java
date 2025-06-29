package com.udacity.catpoint.security.service;

import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.AlarmStatus;
import com.udacity.catpoint.security.data.ArmingStatus;
import com.udacity.catpoint.security.data.SecurityRepository;
import com.udacity.catpoint.security.data.Sensor;
import com.udacity.catpoint.image.ImageService;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;

public final class SecurityService {
    private final SecurityRepository securityRepository;
    private final ImageService imageService;
    private final Set<StatusListener> statusListeners = ConcurrentHashMap.newKeySet();
    private volatile boolean catDetected = false;

    public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
        this.securityRepository = Objects.requireNonNull(securityRepository, "SecurityRepository cannot be null");
        this.imageService = Objects.requireNonNull(imageService, "ImageService cannot be null");
    }

    public void setArmingStatus(ArmingStatus armingStatus) {
        Objects.requireNonNull(armingStatus, "ArmingStatus cannot be null");

        if (armingStatus != ArmingStatus.DISARMED) {
            deactivateAllSensors();
            if (armingStatus == ArmingStatus.ARMED_HOME && catDetected) {
                setAlarmStatus(AlarmStatus.ALARM);
            }
        } else {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }
        securityRepository.setArmingStatus(armingStatus);
    }

    private void deactivateAllSensors() {
        getSensors().forEach(sensor -> {
            boolean wasActive = sensor.getActive();
            sensor.setActive(false);
            if (wasActive || getArmingStatus() != ArmingStatus.DISARMED) {
                securityRepository.updateSensor(sensor);
            }
        });
    }

    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        Objects.requireNonNull(sensor, "Sensor cannot be null");
        Objects.requireNonNull(active, "Active status cannot be null");

        if (getAlarmStatus() == AlarmStatus.ALARM) {
            return;
        }

        boolean wasActive = sensor.getActive();
        sensor.setActive(active);
        securityRepository.updateSensor(sensor);

        handleSensorStateChange(wasActive, active);
    }

    private void handleSensorStateChange(boolean wasActive, boolean isActive) {
        AlarmStatus alarmStatus = getAlarmStatus();

        if (isActive) {
            handleSensorActivation(alarmStatus, wasActive);
        } else if (wasActive) {
            handleSensorDeactivation(alarmStatus);
        }
    }

    private void handleSensorActivation(AlarmStatus alarmStatus, boolean wasActive) {
        if (getArmingStatus() == ArmingStatus.DISARMED) return;

        if (alarmStatus == AlarmStatus.PENDING_ALARM || wasActive) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else {
            setAlarmStatus(AlarmStatus.PENDING_ALARM);
        }
    }

    private void handleSensorDeactivation(AlarmStatus alarmStatus) {
        if (alarmStatus == AlarmStatus.PENDING_ALARM) {
            checkSensorsAndUpdateStatus();
        }
    }

    public void processImage(BufferedImage image) {
        if (image == null) return;

        catDetected = imageService.imageContainsCat(image, 50.0f);
        evaluateCatDetection();
    }

    private void evaluateCatDetection() {
        if (catDetected && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else if (!catDetected && allSensorsInactive()) {
            if (getAlarmStatus() != AlarmStatus.ALARM) {
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        }
        notifyCatDetection();
    }

    private boolean allSensorsInactive() {
        return getSensors().stream().noneMatch(Sensor::getActive);
    }

    private void notifyCatDetection() {
        statusListeners.forEach(listener -> listener.catDetected(catDetected));
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return Collections.unmodifiableSet(securityRepository.getSensors());
    }

    public void addSensor(Sensor sensor) {
        Objects.requireNonNull(sensor, "Sensor cannot be null");
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        Objects.requireNonNull(sensor, "Sensor cannot be null");
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }

    public void addStatusListener(StatusListener statusListener) {
        Objects.requireNonNull(statusListener, "StatusListener cannot be null");
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        Objects.requireNonNull(statusListener, "StatusListener cannot be null");
        statusListeners.remove(statusListener);
    }

    public void setAlarmStatus(AlarmStatus status) {
        Objects.requireNonNull(status, "AlarmStatus cannot be null");
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(listener -> listener.notify(status));
    }

    public void checkSensorsAndUpdateStatus() {
        if (allSensorsInactive()) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }
    }
}
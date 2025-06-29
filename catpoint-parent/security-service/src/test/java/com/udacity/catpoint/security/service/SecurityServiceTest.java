package com.udacity.catpoint.security.service;

import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.*;
import com.udacity.catpoint.image.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.awt.image.BufferedImage;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    private SecurityService securityService;

    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private StatusListener statusListener;

    @Mock
    private StatusListener extraListener;

    @BeforeEach
    void prepareTestEnvironment() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    @Test
    void verifyDisarmedStateResetsAlarm() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void confirmArmedStateDeactivatesSensors() {
        Sensor doorSensor = new Sensor("Front Door", SensorType.DOOR);
        Sensor windowSensor = new Sensor("Living Window", SensorType.WINDOW);
        doorSensor.setActive(true);
        windowSensor.setActive(true);
        when(securityRepository.getSensors()).thenReturn(Set.of(doorSensor, windowSensor));

        securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);

        assertFalse(doorSensor.getActive());
        assertFalse(windowSensor.getActive());
        verify(securityRepository).updateSensor(doorSensor);
        verify(securityRepository).updateSensor(windowSensor);
    }

    @Test
    void checkArmedHomeWithCatSetsAlarm() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(BufferedImage.class), eq(50.0f))).thenReturn(true);

        BufferedImage testImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        securityService.processImage(testImage);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void ensureSensorActivationInArmedStateTriggersPending() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        Sensor motionSensor = new Sensor("Hallway", SensorType.MOTION);

        securityService.changeSensorActivationStatus(motionSensor, true);

        assertTrue(motionSensor.getActive());
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    void validatePendingToAlarmTransitionOnSensorReactivation() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Sensor sensor = new Sensor("Back Door", SensorType.DOOR);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void testNoAlarmChangeDuringActiveAlarm() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        Sensor sensor = new Sensor("Garage", SensorType.MOTION);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void armedAndPendingAlarm_sensorActivated_setsAlarm(ArmingStatus armingStatus) {
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Sensor sensor = new Sensor("Test Sensor", SensorType.DOOR);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void pendingAlarmWithAllSensorsInactive_resetsToNoAlarm() {
        Sensor sensor = new Sensor("Test", SensorType.WINDOW);
        sensor.setActive(true);
        when(securityRepository.getSensors()).thenReturn(Set.of(sensor));
        securityService.addSensor(sensor);
        securityService.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void activatingTwoSensorsInArmedState_triggersAlarm(ArmingStatus armingStatus) {
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Sensor sensor = new Sensor("Test Sensor", SensorType.DOOR);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void disarmedThenCatDetectedThenArmedHome_setsAlarm() {
        when(imageService.imageContainsCat(any(), eq(50.0f))).thenReturn(true);

        securityService.setArmingStatus(ArmingStatus.DISARMED);
        securityService.processImage(new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB));

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void noCatImageWithActiveSensor_doesNotChangeAlarmStatus() {
        Sensor sensor = new Sensor("Window", SensorType.WINDOW);
        sensor.setActive(true);
        when(securityRepository.getSensors()).thenReturn(Set.of(sensor));
        when(imageService.imageContainsCat(any(), eq(50.0f))).thenReturn(false);

        securityService.processImage(new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB));

        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void verifyNoCatAndInactiveSensorsClearsAlarm() {
        when(imageService.imageContainsCat(any(BufferedImage.class), eq(50.0f))).thenReturn(false);
        Sensor inactiveSensor = new Sensor("Basement", SensorType.MOTION);
        inactiveSensor.setActive(false);
        when(securityRepository.getSensors()).thenReturn(Set.of(inactiveSensor));

        securityService.processImage(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB));

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void ensureNullImageDoesNotAffectSystem() {
        securityService.processImage(null);
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test
    void validateSensorAddition() {
        Sensor newSensor = new Sensor("Porch", SensorType.DOOR);
        securityService.addSensor(newSensor);
        verify(securityRepository, times(1)).addSensor(newSensor);
    }

    @Test
    void confirmSensorRemoval() {
        Sensor oldSensor = new Sensor("Attic", SensorType.WINDOW);
        securityService.removeSensor(oldSensor);
        verify(securityRepository, times(1)).removeSensor(oldSensor);
    }

    @Test
    void testCatDetectionNotifiesListeners() {
        securityService.addStatusListener(statusListener);
        when(imageService.imageContainsCat(any(BufferedImage.class), eq(50.0f))).thenReturn(true);

        securityService.processImage(new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB));

        verify(statusListener).catDetected(true);
    }

    @Test
    void verifyRemovedListenerReceivesNoNotification() {
        securityService.addStatusListener(statusListener);
        securityService.removeStatusListener(statusListener);
        when(imageService.imageContainsCat(any(BufferedImage.class), eq(50.0f))).thenReturn(true);

        securityService.processImage(new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB));

        verify(statusListener, never()).catDetected(anyBoolean());
    }

    @Test
    void checkArmedAwayIgnoresCatDetection() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        when(imageService.imageContainsCat(any(BufferedImage.class), eq(50.0f))).thenReturn(true);

        securityService.processImage(new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB));

        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void ensureInactiveSensorDeactivationHasNoEffect() {
        Sensor sensor = new Sensor("Kitchen", SensorType.MOTION);
        sensor.setActive(false);

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test
    void testCatDetectedThenArmedHomeTriggersAlarm() {
        when(imageService.imageContainsCat(any(BufferedImage.class), eq(50.0f))).thenReturn(true);
        securityService.processImage(new BufferedImage(60, 60, BufferedImage.TYPE_INT_RGB));

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void verifyMultipleListenersWithSelectiveRemoval() {
        securityService.addStatusListener(statusListener);
        securityService.addStatusListener(extraListener);
        securityService.removeStatusListener(statusListener);
        when(imageService.imageContainsCat(any(BufferedImage.class), eq(50.0f))).thenReturn(false);

        securityService.processImage(new BufferedImage(70, 70, BufferedImage.TYPE_INT_RGB));

        verify(statusListener, never()).catDetected(anyBoolean());
        verify(extraListener).catDetected(false);
    }
}
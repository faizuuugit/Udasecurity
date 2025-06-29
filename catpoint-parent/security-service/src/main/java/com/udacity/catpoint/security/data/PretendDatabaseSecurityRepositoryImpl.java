package com.udacity.catpoint.security.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collections;
import java.util.prefs.Preferences;
import java.util.Objects;

public final class PretendDatabaseSecurityRepositoryImpl implements SecurityRepository {

    private final Set<Sensor> sensors;
    private AlarmStatus alarmStatus;
    private ArmingStatus armingStatus;

    private static final String SENSORS = "SENSORS";
    private static final String ALARM_STATUS = "ALARM_STATUS";
    private static final String ARMING_STATUS = "ARMING_STATUS";

    private static final Preferences prefs = Preferences.userNodeForPackage(PretendDatabaseSecurityRepositoryImpl.class);
    private static final Gson gson = new Gson();

    public PretendDatabaseSecurityRepositoryImpl() {
        Set<Sensor> loadedSensors = new TreeSet<>();
        AlarmStatus loadedAlarmStatus = AlarmStatus.NO_ALARM;
        ArmingStatus loadedArmingStatus = ArmingStatus.DISARMED;

        try {
            loadedAlarmStatus = AlarmStatus.valueOf(
                    Objects.requireNonNull(prefs.get(ALARM_STATUS, AlarmStatus.NO_ALARM.toString())));

            loadedArmingStatus = ArmingStatus.valueOf(
                    Objects.requireNonNull(prefs.get(ARMING_STATUS, ArmingStatus.DISARMED.toString())));

            String sensorString = prefs.get(SENSORS, null);
            if (sensorString != null) {
                Type type = new TypeToken<Set<Sensor>>() {}.getType();
                Set<Sensor> parsedSensors = gson.fromJson(sensorString, type);
                if (parsedSensors != null) {
                    loadedSensors = new TreeSet<>(parsedSensors);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading preferences: " + e.getMessage());
        } finally {
            this.sensors = Collections.synchronizedSet(new TreeSet<>(loadedSensors));
            this.alarmStatus = loadedAlarmStatus;
            this.armingStatus = loadedArmingStatus;
        }
    }

    @Override
    public void addSensor(Sensor sensor) {
        Objects.requireNonNull(sensor, "Sensor cannot be null");
        synchronized (sensors) {
            sensors.add(sensor);
            prefs.put(SENSORS, gson.toJson(sensors));
        }
    }

    @Override
    public void removeSensor(Sensor sensor) {
        Objects.requireNonNull(sensor, "Sensor cannot be null");
        synchronized (sensors) {
            sensors.remove(sensor);
            prefs.put(SENSORS, gson.toJson(sensors));
        }
    }

    @Override
    public void updateSensor(Sensor sensor) {
        Objects.requireNonNull(sensor, "Sensor cannot be null");
        synchronized (sensors) {
            sensors.remove(sensor);
            sensors.add(sensor);
            prefs.put(SENSORS, gson.toJson(sensors));
        }
    }

    @Override
    public void setAlarmStatus(AlarmStatus alarmStatus) {
        this.alarmStatus = Objects.requireNonNull(alarmStatus, "AlarmStatus cannot be null");
        prefs.put(ALARM_STATUS, this.alarmStatus.toString());
    }

    @Override
    public void setArmingStatus(ArmingStatus armingStatus) {
        this.armingStatus = Objects.requireNonNull(armingStatus, "ArmingStatus cannot be null");
        prefs.put(ARMING_STATUS, this.armingStatus.toString());
    }

    @Override
    public Set<Sensor> getSensors() {
        synchronized (sensors) {
            return Collections.unmodifiableSet(new TreeSet<>(sensors));
        }
    }

    @Override
    public AlarmStatus getAlarmStatus() {
        return alarmStatus;
    }

    @Override
    public ArmingStatus getArmingStatus() {
        return armingStatus;
    }
}
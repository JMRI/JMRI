package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between the a JMRI sensor and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 */
public abstract class AbstractSensorServer {

    private static final String ERROR_SENDING_STATUS = "Error Sending Status";
    private final HashMap<String, SensorListener> sensors;
    private static final Logger log = LoggerFactory.getLogger(AbstractSensorServer.class);

    public AbstractSensorServer(){
        sensors = new HashMap<>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    public abstract void sendStatus(String sensor, int Status) throws IOException;

    public abstract void sendErrorStatus(String sensor) throws IOException;

    public abstract void parseStatus(String statusString) throws JmriException, IOException;

    protected synchronized void addSensorToList(String sensorName) {
        if (!sensors.containsKey(sensorName)) {
            Sensor s = InstanceManager.getDefault(SensorManager.class).getSensor(sensorName);
            if(s!=null) {
               SensorListener sl = new SensorListener(sensorName);
               s.addPropertyChangeListener(sl);
               sensors.put(sensorName, sl );
            }
        }
    }

    protected synchronized void removeSensorFromList(String sensorName) {
        if (sensors.containsKey(sensorName)) {
            Sensor s = InstanceManager.getDefault(SensorManager.class).getSensor(sensorName);
            if(s!=null) {
               s.removePropertyChangeListener(sensors.get(sensorName));
               sensors.remove(sensorName);
            }
        }
    }

    public Sensor initSensor(String sensorName) {
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provideSensor(sensorName);
        this.addSensorToList(sensorName);
        return sensor;
    }

    public void setSensorActive(String sensorName) {
        Sensor sensor;
        // load address from sensorAddrTextField
        try {
            addSensorToList(sensorName);
            sensor = InstanceManager.getDefault(SensorManager.class).getSensor(sensorName);
            if (sensor == null) {
                log.error("Sensor {} is not available", sensorName);
            } else {
                if (sensor.getKnownState() != Sensor.ACTIVE) {
                    // set state to ACTIVE
                    log.debug("changing sensor '{}' to Active ({}->{})", sensorName, sensor.getKnownState(), Sensor.ACTIVE);
                    sensor.setKnownState(Sensor.ACTIVE);
                } else {
                    // just notify the client.
                    log.debug("not changing sensor '{}', already Active ({})", sensorName, sensor.getKnownState());
                    sendStatusWithErrorHandling(sensorName,Sensor.ACTIVE);
                }
            }
        } catch (JmriException ex) {
            log.error("set sensor active", ex);
        }
    }

    public void dispose() {
        for (Map.Entry<String, SensorListener> sensor : this.sensors.entrySet()) {
            Sensor s = InstanceManager.getDefault(SensorManager.class).getSensor(sensor.getKey());
            if(s!=null) {
               s.removePropertyChangeListener(sensor.getValue());
            }
        }
        this.sensors.clear();
    }

    public void setSensorInactive(String sensorName) {
        Sensor sensor;
        try {
            addSensorToList(sensorName);
            sensor = InstanceManager.getDefault(SensorManager.class).getSensor(sensorName);

            if (sensor == null) {
                log.error("Sensor {} is not available",sensorName);
            } else {
                if (sensor.getKnownState() != Sensor.INACTIVE) {
                    // set state to INACTIVE
                    log.debug("changing sensor '{}' to InActive ({}->{})", sensorName, sensor.getKnownState(), Sensor.INACTIVE);
                    sensor.setKnownState(Sensor.INACTIVE);
                } else {
                    // just notify the client.
                    log.debug("not changing sensor '{}', already InActive ({})", sensorName, sensor.getKnownState());
                    sendStatusWithErrorHandling(sensorName,Sensor.INACTIVE);
                }
            }
        } catch (JmriException ex) {
            log.error("set sensor inactive", ex);
        }
    }

    private void sendStatusWithErrorHandling(String sensorName,int status){
        try {
            sendStatus(sensorName, status);
        } catch (IOException ie) {
            log.error(ERROR_SENDING_STATUS);
        }
    }

    class SensorListener implements PropertyChangeListener {

        SensorListener(String sensorName) {
            name = sensorName;
            sensor = InstanceManager.getDefault(SensorManager.class).getSensor(sensorName);
        }

        // update state as state of sensor changes
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            // If the Commanded State changes, show transition state as "<inconsistent>"
            if (e.getPropertyName().equals("KnownState")) {
                int now = ((Integer) e.getNewValue()).intValue();
                try {
                    sendStatus(name, now);
                } catch (IOException ie) {
                    log.debug(ERROR_SENDING_STATUS);
                    // if we get an error, de-register
                    sensor.removePropertyChangeListener(this);
                    removeSensorFromList(name);
                }
            }
        }
        String name = null;
        Sensor sensor = null;
    }
}

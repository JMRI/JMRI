package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between the a JMRI sensor and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 */
abstract public class AbstractSensorServer {

    private final HashMap<String, SensorListener> sensors;
    private final static Logger log = LoggerFactory.getLogger(AbstractSensorServer.class);

    public AbstractSensorServer() {
        sensors = new HashMap<String, SensorListener>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendStatus(String sensor, int Status) throws IOException;

    abstract public void sendErrorStatus(String sensor) throws IOException;

    abstract public void parseStatus(String statusString) throws JmriException, IOException;

    synchronized protected void addSensorToList(String sensorName) {
        if (!sensors.containsKey(sensorName)) {
            Sensor s = InstanceManager.sensorManagerInstance().getSensor(sensorName);
            if(s!=null) {
               SensorListener sl = new SensorListener(sensorName);
               s.addPropertyChangeListener(sl);
               sensors.put(sensorName, sl );
            }
        }
    }

    synchronized protected void removeSensorFromList(String sensorName) {
        if (sensors.containsKey(sensorName)) {
            Sensor s = InstanceManager.sensorManagerInstance().getSensor(sensorName);
            if(s!=null) {
               s.removePropertyChangeListener(sensors.get(sensorName));
               sensors.remove(sensorName);
            }
        }
    }

    public Sensor initSensor(String sensorName) throws IllegalArgumentException {
        Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
        this.addSensorToList(sensorName);
        return sensor;
    }

    public void setSensorActive(String sensorName) {
        Sensor sensor;
        // load address from sensorAddrTextField
        try {
            addSensorToList(sensorName);
            sensor = InstanceManager.sensorManagerInstance().getSensor(sensorName);
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
                    try {
                        sendStatus(sensorName, Sensor.ACTIVE);
                    } catch (IOException ie) {
                        log.error("Error Sending Status");
                    }
                }
            }
        } catch (JmriException ex) {
            log.error("set sensor active", ex);
        }
    }

    public void dispose() {
        for (Map.Entry<String, SensorListener> sensor : this.sensors.entrySet()) {
            Sensor s = InstanceManager.sensorManagerInstance().getSensor(sensor.getKey());
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
            sensor = InstanceManager.sensorManagerInstance().getSensor(sensorName);

            if (sensor == null) {
                log.error("Sensor " + sensorName
                        + " is not available");
            } else {
                if (sensor.getKnownState() != Sensor.INACTIVE) {
                    // set state to INACTIVE
                    log.debug("changing sensor '{}' to InActive ({}->{})", sensorName, sensor.getKnownState(), Sensor.INACTIVE);
                    sensor.setKnownState(Sensor.INACTIVE);
                } else {
                    // just notify the client.
                    log.debug("not changing sensor '{}', already InActive ({})", sensorName, sensor.getKnownState());
                    try {
                        sendStatus(sensorName, Sensor.INACTIVE);
                    } catch (IOException ie) {
                        log.error("Error Sending Status");
                    }
                }
            }
        } catch (JmriException ex) {
            log.error("set sensor inactive", ex);
        }
    }

    class SensorListener implements PropertyChangeListener {

        SensorListener(String sensorName) {
            name = sensorName;
            sensor = InstanceManager.sensorManagerInstance().getSensor(sensorName);
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
                    log.debug("Error Sending Status");
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

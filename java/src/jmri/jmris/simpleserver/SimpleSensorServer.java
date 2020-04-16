package jmri.jmris.simpleserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmris.AbstractSensorServer;
import jmri.jmris.JmriConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Server interface between the JMRI Sensor manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class SimpleSensorServer extends AbstractSensorServer {

    private static final String SENSOR = "SENSOR ";
    private DataOutputStream output;
    private JmriConnection connection;

    public SimpleSensorServer(JmriConnection connection){
        super();
        this.connection = connection;
    }

    public SimpleSensorServer(DataInputStream inStream, DataOutputStream outStream) {
        super();
        output = outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String sensorName, int Status) throws IOException {
        addSensorToList(sensorName);

        if (Status == Sensor.INACTIVE) {
            this.sendMessage(SENSOR + sensorName + " INACTIVE\n");
        } else if (Status == Sensor.ACTIVE) {
            this.sendMessage(SENSOR + sensorName + " ACTIVE\n");
        } else {
            this.sendMessage(SENSOR + sensorName + " UNKNOWN\n");
        }
    }

    @Override
    public void sendErrorStatus(String sensorName) throws IOException {
        this.sendMessage("SENSOR ERROR\n");
    }

    @Override
    public void parseStatus(String statusString) throws jmri.JmriException, java.io.IOException {
        int index;
        index = statusString.indexOf(' ') + 1;
        if (statusString.contains("INACTIVE")) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Sensor INACTIVE");
            }
            initSensor(statusString.substring(index, statusString.indexOf(' ' , index + 1)));
            setSensorInactive(statusString.substring(index, statusString.indexOf(' ', index + 1)));
        } else if (statusString.contains("ACTIVE")) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Sensor ACTIVE");
            }
            initSensor(statusString.substring(index, statusString.indexOf(' ', index + 1)));
            setSensorActive(statusString.substring(index, statusString.indexOf(' ', index + 1)));
        } else {
            // default case, return status for this sensor/
            String sensorName = statusString.substring(index);
            if(sensorName.contains("\n")){
                // remove anything following the newline
                sensorName = sensorName.substring(0,sensorName.indexOf('\n'));
            }
            if( sensorName.contains(" ") ){
                // remove anything following the space.
                sensorName = sensorName.substring(0,sensorName.indexOf(' '));
            }
            try {
                Sensor sensor = InstanceManager.getDefault(SensorManager.class).provideSensor(sensorName);
                sendStatus(sensorName, sensor.getKnownState());
            } catch (IllegalArgumentException ex) {
                log.warn("Failed to provide Sensor \"{}\" in sendStatus", sensorName);
            }
        }
    }

    private void sendMessage(String message) throws IOException {
        if (this.output != null) {
            this.output.writeBytes(message);
        } else {
            this.connection.sendMessage(message);
        }
    }
    private static final Logger log = LoggerFactory.getLogger(SimpleSensorServer.class);
}

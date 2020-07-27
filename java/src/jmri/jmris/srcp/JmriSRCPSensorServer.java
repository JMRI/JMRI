package jmri.jmris.srcp;

import java.beans.PropertyChangeListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmris.AbstractSensorServer;
import jmri.SystemConnectionMemo;

/**
 * SRCP Server interface between the JMRI Sensor manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2011
 */
public class JmriSRCPSensorServer extends AbstractSensorServer implements PropertyChangeListener {

    private static final String ERROR = "Error499";
    private OutputStream output;

    public JmriSRCPSensorServer(DataInputStream inStream, OutputStream outStream) {
        super();
        output = outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String sensorName, int Status) throws IOException {
        int bus = 0;
        int address = 0;
        java.util.List<SystemConnectionMemo> list = InstanceManager.getList(SystemConnectionMemo.class);
        for (SystemConnectionMemo memo : list) {
            String prefix = memo.getSystemPrefix();
            if (sensorName.startsWith(prefix)) {
                try {
                    address = Integer.parseInt(sensorName.substring(prefix.length() + 1));
                    break;
                } catch (NumberFormatException ne) {
                    // we expect this if the prefix doesn't match
                }
            }
            bus++;
        }

        if (bus > list.size()) {
            output.write(Bundle.getMessage(ERROR).getBytes());
            return;
        }

        if (Status == Sensor.ACTIVE) {
            output.write(( "100 INFO " + bus + " FB " + address + " 1\n\r").getBytes());
        } else if (Status == Sensor.INACTIVE) {
            output.write(("100 INFO " + bus + " FB " + address + " 0\n\r").getBytes());
        } else {
            //  unknown state
            output.write( Bundle.getMessage("Error411").getBytes());
        }

    }

    public void sendStatus(int bus, int address) throws IOException {
        log.debug("send Status called with bus {} and address {}",bus,address);
        java.util.List<SystemConnectionMemo> list = InstanceManager.getList(SystemConnectionMemo.class);
        SystemConnectionMemo memo;
        try {
            memo = list.get(bus);
        } catch (java.lang.IndexOutOfBoundsException obe) {
            output.write(Bundle.getMessage("Error412").getBytes());
            return;
        }
        String sensorName = memo.getSystemPrefix()
                + "S" + address;
        try {
            int Status = InstanceManager.getDefault(SensorManager.class).provideSensor(sensorName).getKnownState();
            if (Status == Sensor.ACTIVE) {
                output.write(("100 INFO " + bus + " FB " + address + " 1\n\r").getBytes());
            } else if (Status == Sensor.INACTIVE) {
                output.write(("100 INFO " + bus + " FB " + address + " 0\n\r").getBytes());
            } else {
                //  unknown state
                output.write(Bundle.getMessage("Error411").getBytes());
            }
        } catch (IllegalArgumentException ex) {
            log.warn("Failed to provide Sensor \"{}\" in sendStatus", sensorName);
        }
    }

    @Override
    public void sendErrorStatus(String sensorName) throws IOException {
        output.write(Bundle.getMessage(ERROR).getBytes());
    }

    @Override
    public void parseStatus(String statusString) throws jmri.JmriException, java.io.IOException {
        output.write(Bundle.getMessage(ERROR).getBytes());
    }

    /*
     * for SRCP, we're doing the parsing elsewhere, so we just need to build
     * the correct string from the provided compoents.
     */
    public void parseStatus(int bus, int address, int value) throws java.io.IOException {
        log.debug("parse Status called with bus {} address {} and value {}",bus,address,value);
        java.util.List<SystemConnectionMemo> list = InstanceManager.getList(SystemConnectionMemo.class);
        SystemConnectionMemo memo;
        try {
            memo = list.get(bus - 1);
        } catch (java.lang.IndexOutOfBoundsException obe) {
            output.write("412 ERROR wrong value\n\r".getBytes());
            return;
        }
        String sensorName = memo.getSystemPrefix()
                + "S" + address;
        this.initSensor(sensorName);
        if (value == 0) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Sensor INACTIVE");
            }
            setSensorInactive(sensorName);
        } else if (value == 1) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Sensor ACTIVE");
            }
            setSensorActive(sensorName);
        }
    }

    @Override
    protected synchronized void addSensorToList(String sensorName) {
        Sensor s = InstanceManager.getDefault(SensorManager.class).getSensor(sensorName);
        if(s!=null) {
            s.addPropertyChangeListener(this);
        }
    }

    @Override
    protected synchronized void removeSensorFromList(String sensorName) {
        Sensor s = InstanceManager.getDefault(SensorManager.class).getSensor(sensorName);
        if(s!=null) {
            s.removePropertyChangeListener(this);
        }
    }


    // update state as state of sensor changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // If the Commanded State changes, show transition state as "<inconsistent>"
        if (e.getPropertyName().equals("KnownState")) {
            try {
                String Name = ((jmri.Sensor) e.getSource()).getSystemName();
                java.util.List<SystemConnectionMemo> memoList = InstanceManager.getList(SystemConnectionMemo.class);
                int i = 0;
                int address;
                for (SystemConnectionMemo memo : memoList) {
                    String prefix = memo.getSystemPrefix();
                    if (Name.startsWith(prefix)) {
                        address = Integer.parseInt(Name.substring(prefix.length()+1));
                        sendStatus(i, address);
                        break;
                    }
                    i++;
                }
            } catch (java.io.IOException ie) {
                log.error("Error Sending Status");
            }
        }
    }
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JmriSRCPSensorServer.class);
}

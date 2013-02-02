//AbstractSensorServer.java

package jmri.jmris;

import org.apache.log4j.Logger;
import java.io.*;
import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.Sensor;

/**
 * Abstract interface between the a JMRI sensor and a 
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision$
 */

abstract public class AbstractSensorServer {

   public AbstractSensorServer(){
      sensors= new ArrayList<String>();
   }

    /*
     * Protocol Specific Abstract Functions
     */

     abstract public void sendStatus(String sensor, int Status) throws IOException; 
     abstract public void sendErrorStatus(String sensor) throws IOException;
     abstract public void parseStatus(String statusString) throws jmri.JmriException,java.io.IOException;

    synchronized protected void addSensorToList(java.lang.String sensorName) {
         if (!sensors.contains(sensorName) ) {
             sensors.add(sensorName);
             InstanceManager.sensorManagerInstance().provideSensor(sensorName)
                     .addPropertyChangeListener(new SensorListener(sensorName));
         }
    }

    synchronized protected void removeSensorFromList(java.lang.String sensorName) {
         if (sensors.contains(sensorName) ) {
             sensors.remove(sensorName);
         }
    }

	
    public void setSensorActive(java.lang.String sensorName) {
    	Sensor sensor = null;
    	// load address from sensorAddrTextField
    	try {
    		addSensorToList(sensorName);
    		sensor= InstanceManager.sensorManagerInstance().provideSensor(sensorName);
    		if (sensor == null) {
    			log.error("Sensor " + sensorName
    					+ " is not available");
    		} else {
    			if(sensor.getKnownState()!=jmri.Sensor.ACTIVE) {
    				// set state to ACTIVE
        			if (log.isDebugEnabled()) log.debug("changing sensor '" + sensorName + "' to Active ("+sensor.getKnownState()+"->" +jmri.Sensor.ACTIVE+")");
    				sensor.setKnownState(jmri.Sensor.ACTIVE);
    			} else {
    				// just notify the client.
        			if (log.isDebugEnabled()) log.debug("not changing sensor '" + sensorName + "', already Active ("+sensor.getKnownState()+")");
    				try {
    					sendStatus(sensorName,jmri.Sensor.ACTIVE);
    				} catch(java.io.IOException ie) {
    					log.error("Error Sending Status");
    				}
    			}
    		}
    	} catch (Exception ex) {
    		log.error("set sensor active, exception: "
    				+ ex.toString());
    	}
    }

    public void setSensorInactive(java.lang.String sensorName) {
    	Sensor sensor = null;
    	try {
    		addSensorToList(sensorName);
    		sensor= InstanceManager.sensorManagerInstance().provideSensor(sensorName); 

    		if (sensor== null) {
    			log.error("Sensor " + sensorName
    					+ " is not available");
    		} else {
    			if(sensor.getKnownState()!=jmri.Sensor.INACTIVE) {
    				// set state to INACTIVE
        			if (log.isDebugEnabled()) log.debug("changing sensor '" + sensorName + "' to InActive ("+sensor.getKnownState()+"->" +jmri.Sensor.INACTIVE+")");
    				sensor.setKnownState(jmri.Sensor.INACTIVE);
    			} else {
    				// just notify the client.
        			if (log.isDebugEnabled()) log.debug("not changing sensor '" + sensorName + "', already InActive ("+sensor.getKnownState()+")");
    				try {
    					sendStatus(sensorName,jmri.Sensor.INACTIVE);
    				} catch(java.io.IOException ie) {
    					log.error("Error Sending Status");
    				}
    			}
    		}
    	} catch (Exception ex) {
    		log.error("set sensor inactive, exception: "
    				+ ex.toString());
    	}
    }

    class SensorListener implements java.beans.PropertyChangeListener {

       SensorListener(String sensorName) {
          name=sensorName;
          sensor= InstanceManager.sensorManagerInstance().provideSensor(sensorName);
       }

       // update state as state of sensor changes
       public void propertyChange(java.beans.PropertyChangeEvent e) {
    	 // If the Commanded State changes, show transition state as "<inconsistent>" 
        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue()).intValue();
            try {
               sendStatus(name,now);
            } catch(java.io.IOException ie) {
                  log.debug("Error Sending Status");
                  // if we get an error, de-register
                  sensor.removePropertyChangeListener(this);
                  removeSensorFromList(name);
            }
         }
      }

      String name = null;
      Sensor sensor=null;
 
    }

    protected ArrayList<String> sensors = null;

    String newState = "";


    static Logger log = Logger.getLogger(AbstractSensorServer.class.getName());

}

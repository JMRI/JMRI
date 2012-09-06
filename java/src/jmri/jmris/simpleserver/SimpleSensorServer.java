//SimpleSensorServer.java

package jmri.jmris.simpleserver;

import java.io.*;

import jmri.Sensor;

import jmri.jmris.AbstractSensorServer;

/**
 * Simple Server interface between the JMRI Sensor manager and a
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision$
 */

public class SimpleSensorServer extends AbstractSensorServer {

   private DataOutputStream output;

   public SimpleSensorServer(DataInputStream inStream,DataOutputStream outStream){
        super();
        output=outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */

     public void sendStatus(String sensorName, int Status) throws IOException
     {
         addSensorToList(sensorName);

	if(Status==Sensor.INACTIVE){
		output.writeBytes("SENSOR " + sensorName + " INACTIVE\n");
        } else if (Status==Sensor.ACTIVE){
		output.writeBytes("SENSOR " + sensorName + " ACTIVE\n");
        } else {
		output.writeBytes("SENSOR " + sensorName + " UNKNOWN\n");
        }
     }

     public void sendErrorStatus(String sensorName) throws IOException {
 	output.writeBytes("SENSOR ERROR\n");
     }

     public void parseStatus(String statusString) throws jmri.JmriException,java.io.IOException {
            int index;
            index=statusString.indexOf(" ")+1;
	    if(statusString.contains("INACTIVE")){
                   if(log.isDebugEnabled())
                      log.debug("Setting Sensor INACTIVE");
                   setSensorInactive(statusString.substring(index,statusString.indexOf(" ",index+1)));
            } else if(statusString.contains("ACTIVE")){
                   if(log.isDebugEnabled())
                      log.debug("Setting Sensor ACTIVE");
                   setSensorActive(statusString.substring(index,statusString.indexOf(" ",index+1)));
            } else {
              // default case, return status for this sensor/
              Sensor sensor = jmri.InstanceManager.sensorManagerInstance().provideSensor(statusString.substring(index));
              sendStatus(statusString.substring(index),sensor.getKnownState());

            }
     }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleSensorServer.class.getName());

}

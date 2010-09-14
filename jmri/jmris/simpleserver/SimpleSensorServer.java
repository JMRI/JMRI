//SimpleSensorServer.java

package jmri.jmris.simpleserver;

import java.io.*;

import jmri.Sensor;

import jmri.jmris.AbstractSensorServer;

/**
 * Simple Server interface between the JMRI power manager and a
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision: 1.1 $
 */

public class SimpleSensorServer extends AbstractSensorServer {

   private DataOutputStream output;

   public SimpleSensorServer(DataInputStream inStream,DataOutputStream outStream){

        output=outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */

     public void sendStatus(int Status) throws IOException
     {
	if(Status==Sensor.INACTIVE){
		output.writeBytes("SENSOR " + sensor.getSystemName() + " INACTIVE\n");
        } else if (Status==Sensor.ACTIVE){
		output.writeBytes("SENSOR " + sensor.getSystemName() + " ACTIVE\n");
        } else {
               //  unknown state
        }
     }

     public void sendErrorStatus() throws IOException {
 	output.writeBytes("TURNOUT ERROR\n");
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
            }
     }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleSensorServer.class.getName());

}

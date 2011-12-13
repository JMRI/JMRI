//SimpleLightServer.java

package jmri.jmris.simpleserver;

import java.io.*;

import jmri.Light;

import jmri.jmris.AbstractLightServer;

/**
 * Simple Server interface between the JMRI light manager and a
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision$
 */

public class SimpleLightServer extends AbstractLightServer {

   private DataOutputStream output;

   public SimpleLightServer(DataInputStream inStream,DataOutputStream outStream){

        output=outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */

     public void sendStatus(String lightName, int Status) throws IOException
     {
	if(Status==Light.ON){
		output.writeBytes("LIGHT " + lightName + " ON\n");
        } else if (Status==Light.OFF){
		output.writeBytes("LIGHT " + lightName + " OFF\n");
        } else {
               //  unknown state
		output.writeBytes("LIGHT " + lightName + " UNKNOWN\n");
        }
     }

     public void sendErrorStatus(String lightName) throws IOException {
 	output.writeBytes("TURNOUT ERROR\n");
     }

     public void parseStatus(String statusString) throws jmri.JmriException,java.io.IOException {
            int index;
            index=statusString.indexOf(" ")+1;
	    if(statusString.contains("ON")){
                   if(log.isDebugEnabled())
                      log.debug("Setting Light ON");
                   lightOn(statusString.substring(index,statusString.indexOf(" ",index+1)));
            } else if(statusString.contains("OFF")){
                   if(log.isDebugEnabled())
                      log.debug("Setting Light OFF");
                   lightOff(statusString.substring(index,statusString.indexOf(" ",index+1)));
            }
     }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleLightServer.class.getName());

}

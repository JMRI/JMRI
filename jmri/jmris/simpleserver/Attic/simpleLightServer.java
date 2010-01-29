//simpleLightServer.java

package jmri.jmris.simpleserver;

import java.io.*;
import java.lang.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;

import jmri.jmris.AbstractLightServer;

/**
 * Simple Server interface between the JMRI power manager and a
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision: 1.1 $
 */

public class simpleLightServer extends AbstractLightServer {

   private DataInputStream input;
   private DataOutputStream output;

   public simpleLightServer(DataInputStream inStream,DataOutputStream outStream){

        input=inStream;
        output=outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */

     public void sendStatus(int Status) throws IOException
     {
	if(Status==Light.ON){
		output.writeBytes("LIGHT " + light.getSystemName() + " ON\n");
        } else if (Status==Light.OFF){
		output.writeBytes("LIGHT " + light.getSystemName() + " OFF\n");
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


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(simplePowerServer.class.getName());

}

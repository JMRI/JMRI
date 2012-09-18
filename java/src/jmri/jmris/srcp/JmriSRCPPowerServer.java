//JmriSRCPPowerServer.java

package jmri.jmris.srcp;

import java.io.*;

import jmri.PowerManager;

import jmri.jmris.AbstractPowerServer;

/**
 * SRCP interface between the JMRI power manager and a
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision$
 */

public class JmriSRCPPowerServer extends AbstractPowerServer {

   private DataOutputStream output;

   public JmriSRCPPowerServer(DataOutputStream outStream){
        output=outStream;
        mgrOK();
    }


    /*
     * Protocol Specific Abstract Functions
     */

     public void sendStatus(int Status) throws IOException
     {
	if(Status==PowerManager.ON){
		output.writeBytes("100 INFO 0 POWER ON\n\r");
        } else if (Status==PowerManager.OFF){
		output.writeBytes("100 INFO 0 POWER OFF\n\r");
        } else {
               // power unknown
        }
     }

     public void sendErrorStatus() throws IOException {
 	output.writeBytes("499 ERROR unspecified error\n\r");
     }

     public void parseStatus(String statusString) throws jmri.JmriException {
	    if(statusString.contains("ON")){
                   if(log.isDebugEnabled())
                      log.debug("Setting Power ON");
                   setOnStatus();
            } else if(statusString.contains("OFF")){
                   if(log.isDebugEnabled())
                      log.debug("Setting Power OFF");
                   setOffStatus();
            }
     }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JmriSRCPPowerServer.class.getName());

}

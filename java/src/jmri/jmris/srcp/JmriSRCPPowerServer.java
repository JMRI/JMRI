//JmriSRCPPowerServer.java

package jmri.jmris.srcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
		TimeStampedOutput.writeTimestamp(output,"100 INFO 0 POWER ON\n\r");
        } else if (Status==PowerManager.OFF){
		TimeStampedOutput.writeTimestamp(output,"100 INFO 0 POWER OFF\n\r");
        } else {
               // power unknown
        }
     }

     public void sendErrorStatus() throws IOException {
 	TimeStampedOutput.writeTimestamp(output,"499 ERROR unspecified error\n\r");
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


    static Logger log = LoggerFactory.getLogger(JmriSRCPPowerServer.class.getName());

}

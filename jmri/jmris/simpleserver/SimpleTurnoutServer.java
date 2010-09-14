//SimpleTurnoutServer.java

package jmri.jmris.simpleserver;

import java.io.*;

import jmri.Turnout;

import jmri.jmris.AbstractTurnoutServer;

/**
 * Simple Server interface between the JMRI power manager and a
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision: 1.1 $
 */

public class SimpleTurnoutServer extends AbstractTurnoutServer {

   private DataOutputStream output;

   public SimpleTurnoutServer(DataInputStream inStream,DataOutputStream outStream){

        output=outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */

     public void sendStatus(int Status) throws IOException
     {
	if(Status==Turnout.THROWN){
		output.writeBytes("TURNOUT " + turnout.getSystemName() + " THROWN\n");
        } else if (Status==Turnout.CLOSED){
		output.writeBytes("TURNOUT " + turnout.getSystemName() + " CLOSED\n");
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
	    if(statusString.contains("THROWN")){
                   if(log.isDebugEnabled())
                      log.debug("Setting Turnout THROWN");
                   throwTurnout(statusString.substring(index,statusString.indexOf(" ",index+1)));
            } else if(statusString.contains("CLOSED")){
                   if(log.isDebugEnabled())
                      log.debug("Setting Turnout CLOSED");
                   closeTurnout(statusString.substring(index,statusString.indexOf(" ",index+1)));
            }
            sendStatus(turnout.getKnownState());
     }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleLightServer.class.getName());

}

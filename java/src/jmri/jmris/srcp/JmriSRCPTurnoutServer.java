//JmriSRCPTurnoutServer.java

package jmri.jmris.srcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;

import jmri.Turnout;
import jmri.InstanceManager;
import jmri.jmris.AbstractTurnoutServer;

/**
 * SRCP Server interface between the JMRI Turnout manager and a
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision$
 */

public class JmriSRCPTurnoutServer extends AbstractTurnoutServer {

   private DataOutputStream output;

   public JmriSRCPTurnoutServer(DataInputStream inStream,DataOutputStream outStream){

        output=outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */

     public void sendStatus(String turnoutName,int Status) throws IOException
     {
		output.writeBytes("499 ERROR unspecified error\n\r");
     }

     public void sendStatus(int bus, int address) throws IOException
     {
       log.debug("send Status called with bus " +bus +" and address " +address);
       java.util.List<Object> list
            = jmri.InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class);
       Object memo=null;
       try {
         memo = list.get(bus-1);
       } catch( java.lang.IndexOutOfBoundsException obe) {
         output.writeBytes("412 ERROR wrong value\n\r");
         return;
       }
       String turnoutName=((jmri.jmrix.SystemConnectionMemo)memo).getSystemPrefix()
                          + "T" + address;
       int Status=InstanceManager.turnoutManagerInstance().provideTurnout(turnoutName).getKnownState();
	if(Status==Turnout.THROWN){
		output.writeBytes("100 INFO " + bus + " GA " + address + " 0 0\n\r");
        } else if (Status==Turnout.CLOSED){
		output.writeBytes("100 INFO " + bus + " GA " + address + " 1 0\n\r");
        } else {
               //  unknown state
		output.writeBytes("416 ERROR no data\n\r");
        }
     }

     public void sendErrorStatus(String turnoutName) throws IOException {
 	output.writeBytes("499 ERROR unspecified error\n\r");
     }

     public void parseStatus(String statusString) throws jmri.JmriException,java.io.IOException {
	  output.writeBytes("499 ERROR unspecified error\n\r");
     }

   /*
    * for SRCP, we're doing the parsing elsewhere, so we just need to build
    * the correct string from the provided compoents.
    */
    public void parseStatus(int bus,int address,int value) throws jmri.JmriException,java.io.IOException {

       log.debug("parse Status called with bus " +bus +" address " +address
                 + " and value " +value);
       java.util.List<Object> list
            = jmri.InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class);
       Object memo;
       try {
         memo = list.get(bus-1);
       } catch( java.lang.IndexOutOfBoundsException obe) {
         output.writeBytes("412 ERROR wrong value\n\r");
         return;
       }
       String turnoutName=((jmri.jmrix.SystemConnectionMemo)memo).getSystemPrefix()
                          + "T" + address;
	    if(value==0){
                   if(log.isDebugEnabled())
                      log.debug("Setting Turnout THROWN");
                   throwTurnout(turnoutName);
            } else if(value==1){
                   if(log.isDebugEnabled())
                      log.debug("Setting Turnout CLOSED");
                   closeTurnout(turnoutName);
            }
            sendStatus(bus,address);
    }

    // update state as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // If the Commanded State changes, show transition state as "<inconsistent>"
        if (e.getPropertyName().equals("KnownState")) {
            try {
               String Name=((jmri.Turnout)e.getSource()).getSystemName();
               java.util.List<Object> List=jmri.InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class);
               int i=0;
               int address;
               for(Object memo : List){
                   String prefix=((jmri.jmrix.SystemConnectionMemo)memo).getClass().getName();
                   if(Name.startsWith(prefix))
                   {
                      address=Integer.parseInt(Name.substring(prefix.length()));
                      sendStatus(i,address);
                      break;
                   }
                   i++;
               }
            } catch(java.io.IOException ie) {
                  log.error("Error Sending Status");
            }
        }
     }


    static Logger log = LoggerFactory.getLogger(JmriSRCPTurnoutServer.class.getName());

}

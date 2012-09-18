//AbstractProgrammerServer.java

package jmri.jmris;

import java.io.*;
import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.Programmer;
import jmri.ProgListener;

/**
 * Abstract interface between the a JMRI Programmer and a 
 * network connection
 * @author          Paul Bender Copyright (C) 2012
 * @version         $Revision$
 */

abstract public class AbstractProgrammerServer implements jmri.ProgListener {

   private Programmer p = null;
   protected int lastCV = -1;

   public AbstractProgrammerServer(){
        if (InstanceManager.programmerManagerInstance()!=null)
            p = InstanceManager.programmerManagerInstance().getGlobalProgrammer();
        else
            log.warn("no Service Mode ProgrammerManager configured, network programming disabled");
   }

    /*
     * Protocol Specific Abstract Functions
     */
     abstract public void sendStatus(int CV, int value,int status) throws IOException; 
     abstract public void sendNotAvailableStatus() throws IOException; 
     abstract public void parseRequest(String statusString) throws jmri.JmriException,java.io.IOException;
	
    public void writeCV(int mode,int CV,int value) {
       if( p == null) {
           try {
              sendNotAvailableStatus();
           } catch(java.io.IOException ioe){
		// Connection Terminated?
           }
           return;
       }
       lastCV=CV;
       try {
         p.setMode(mode); // need to check if mode is available
         p.writeCV(CV,value,this);
       } catch (jmri.ProgrammerException ex) {
         //Send failure Status.
           try {
             sendNotAvailableStatus();
           } catch(java.io.IOException ioe){
		// Connection Terminated?
           }
       }
    }

    public void readCV(int mode, int CV){
       if( p == null && !(p.getCanRead()) ) {
           try {
             sendNotAvailableStatus();
           } catch(java.io.IOException ioe){
		// Connection Terminated?
           }
           return;
       }
       lastCV=CV;
       try {
          p.setMode(mode); // need to check if mode is available
          p.readCV(CV,this);
       } catch (jmri.ProgrammerException ex) {
         //Send failure Status.
           try {
             sendNotAvailableStatus();
           } catch(java.io.IOException ioe){
		// Connection Terminated?
           }
       }
    }

     /** Receive a callback at the end of a programming operation.
         *
         * @param value  Value from a read operation, or value written on a write
         * @param status Denotes the completion code. Note that this is a
         *                    bitwise combination of the various status coded defined
         *                    in this interface.
         */
        public void programmingOpReply(int value, int status){
           if(log.isDebugEnabled()) log.debug("programmingOpReply called with value " + value + " and status "+ status);
           try {
             sendStatus(lastCV,value,status);
           } catch(java.io.IOException ioe){
		// Connection Terminated?
                if(log.isDebugEnabled()) log.debug("Exception while sending reply");
           }
        }



    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractProgrammerServer.class.getName());

}

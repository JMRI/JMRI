//JmriSRCPProgrammerServer.java

package jmri.jmris.srcp;

import java.io.*;

import jmri.Programmer;
import jmri.ProgListener;

import jmri.jmris.AbstractProgrammerServer;

/**
 * SRCP interface between the JMRI service mode programmer and a
 * network connection
 * @author          Paul Bender Copyright (C) 2012
 * @version         $Revision$
 */

public class JmriSRCPProgrammerServer extends AbstractProgrammerServer {

   private DataOutputStream output;

   public JmriSRCPProgrammerServer(DataOutputStream outStream){
        super();
        output=outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */

     public void sendStatus(int CV, int value,int status) throws IOException {
        if(log.isDebugEnabled()) log.debug("sendStatus called for CV " +CV + 
                          " with value " + value + " and status " + status );
        if(status==ProgListener.OK)
           output.writeBytes("100 INFO 1 SM " + CV + " CV " + value +"\n\r");
        else
           output.writeBytes("416 ERROR no data\n\r");
     }

     public void sendNotAvailableStatus() throws IOException {
        output.writeBytes("499 ERROR unspecified error\n");
     }

     public void parseRequest(String statusString) throws jmri.JmriException,java.io.IOException {
     }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JmriSRCPProgrammerServer.class.getName());

}

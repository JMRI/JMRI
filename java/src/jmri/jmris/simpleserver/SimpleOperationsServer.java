//SimpleOperationsServer.java

package jmri.jmris.simpleserver;

import java.io.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.operations.trains.*;
import jmri.jmrit.operations.locations.*;

/**
 * Simple interface between the JMRI operations and a 
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision: 17977 $
 */

public class SimpleOperationsServer extends jmri.jmris.AbstractOperationsServer {

   private DataOutputStream output;

   public SimpleOperationsServer(DataInputStream inStream,DataOutputStream outStream){
	super();
	output=outStream;
   }


   /*
    * Protocol Specific Simple Functions
    */

    public void sendInfoString(String statusString) throws IOException {
	output.writeBytes(statusString + "\n");
    } 

    public void sendErrorStatus() throws IOException {
    }

    public void parseStatus(String statusString) throws jmri.JmriException {
        try {
	  if(statusString.contains("LOCATIONS"))
		sendLocationList();
	  else if(statusString.contains("TRAINS"))
		sendTrainList();
	  else throw new jmri.JmriException();
       } catch (java.io.IOException ioe) {
	 throw new jmri.JmriException();
       }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleOperationsServer.class.getName());

}

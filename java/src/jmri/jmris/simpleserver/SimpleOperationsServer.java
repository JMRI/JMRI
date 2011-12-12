//SimpleOperationsServer.java

package jmri.jmris.simpleserver;

import java.io.*;

/**
 * Simple interface between the JMRI operations and a 
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision$
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
	  else if(statusString.contains("TRAINLENGTH"))
	       {
                   int index;
                   index=statusString.indexOf(" ")+1;
                   index=statusString.indexOf(" ",index)+1;
                   sendTrainLength(statusString.substring(index));
               }
	  else if(statusString.contains("TRAINLOCATION"))
	       {
                   int index,index2;
                   index=statusString.indexOf(" ")+1;
                   index=statusString.indexOf(" ",index)+1;
		   if((index2=statusString.indexOf(" ",index))>0 ) {
                      // set the location.
                      log.debug("setting location index = " + index +
                                "index 2 = " + index2 + " String " +statusString);
                      setTrainLocation(statusString.substring(index,index2),
                                       statusString.substring(index2+1));
                   } else {
                      // get the location.
                      sendTrainLocation(statusString.substring(index));
		   }
               }
	  else if(statusString.contains("TRAINSTATUS"))
	       {
                   int index;
                   index=statusString.indexOf(" ")+1;
                   index=statusString.indexOf(" ",index)+1;
                   sendTrainStatus(statusString.substring(index));
               }
	  else if(statusString.contains("TRAINS"))
		sendTrainList();
	  else if(statusString.contains("TERMINATE"))
	       {
                   int index;
                   index=statusString.indexOf(" ")+1;
                   index=statusString.indexOf(" ",index)+1;
                   terminateTrain(statusString.substring(index));
               }
	  else throw new jmri.JmriException();
       } catch (java.io.IOException ioe) {
	 throw new jmri.JmriException();
       }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleOperationsServer.class.getName());

}

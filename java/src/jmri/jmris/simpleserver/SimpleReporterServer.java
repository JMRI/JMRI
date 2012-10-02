//SimpleReporterServer.java

package jmri.jmris.simpleserver;

import java.io.*;

import org.eclipse.jetty.websocket.WebSocket.Connection;

import jmri.Reporter;

import jmri.jmris.AbstractReporterServer;

/**
 * Simple Server interface between the JMRI reporter manager and a
 * network connection
 * @author          Paul Bender Copyright (C) 2011
 * @version         $Revision$
 */

public class SimpleReporterServer extends AbstractReporterServer {

   private DataOutputStream output;
   private Connection connection;

   public SimpleReporterServer(Connection connection) {
	   super();
	   this.connection = connection;
   }
   
   public SimpleReporterServer(DataInputStream inStream,DataOutputStream outStream){
        super();
        output=outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */

     public void sendReport(String reporterName, Object r) throws IOException
     {
         addReporterToList(reporterName);
        if(r!=null)
	  this.sendMessage("REPORTER " + reporterName + " " + r.toString() +"\n");
        else
	  this.sendMessage("REPORTER " + reporterName + "\n" );
     }

     public void sendErrorStatus(String reporterName) throws IOException {
 	this.sendMessage("REPORTER ERROR\n");
     }

     public void parseStatus(String statusString) throws jmri.JmriException,java.io.IOException {
            int index,index2;
            index=statusString.indexOf(" ")+1;
            index2=statusString.indexOf(" ",index+1);
            // the string should be "REPORTER xxxxxx REPORTSTRING\n\r"
            // where xxxxxx is the reporter identifier and REPORTSTRING is
            // the report, which may contain spaces.
            if(index2>0 && statusString.substring(index2+1).length()>0){
               setReporterReport(statusString.substring(index,index2),statusString.substring(index2+1));
            }
            //} else {
              // return report for this reporter/
              Reporter reporter = jmri.InstanceManager.reporterManagerInstance().provideReporter(statusString.substring(index));
              sendReport(statusString.substring(index),reporter.getCurrentReport());

            //}
     }

     private void sendMessage(String message) throws IOException {
     	if (this.output != null) {
     		this.output.writeBytes(message);
     	} else {
     		this.connection.sendMessage(message);
     	}
     }
     
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleReporterServer.class.getName());

}

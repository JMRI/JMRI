// SimpleServer.java

package jmri.jmris.simpleserver;

import jmri.jmris.*;

import java.io.*;

import jmri.InstanceManager;

/**
 * This is an implementaiton of a simple server for JMRI.
 * There is currently no handshaking in this server.  You may just start 
 * sending commands.
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision: 1.1 $
 *
 */
public class SimpleServer extends JmriServer{

     private static JmriServer _instance = null;

     public static JmriServer instance(){
         if(_instance==null) _instance=new SimpleServer();
         return _instance;
     }

     // Create a new server using the default port
     public SimpleServer() {
	super(2048);
     }

     public SimpleServer(int port) {
	super(port);
     }

     // Handle communication to a client through inStream and outStream
	 @SuppressWarnings("deprecation")
     public void handleClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        // Listen for commands from the client until the connection closes
	String cmd; 

        // interface components
        SimplePowerServer powerServer = new SimplePowerServer(inStream,outStream);
        SimpleTurnoutServer turnoutServer = new SimpleTurnoutServer(inStream,outStream);
        SimpleLightServer lightServer = new SimpleLightServer(inStream,outStream);
        SimpleSensorServer sensorServer = new SimpleSensorServer(inStream,outStream);

        // Start by sending a welcome message
        outStream.writeBytes("JMRI " + jmri.Version.name() + " \n");

	    while(true) {
	   // Read the command from the client
           cmd = inStream.readLine();
           
           if(log.isDebugEnabled()) log.debug("Received from client: " + cmd);
              if(cmd.startsWith("POWER")){
	         try {
                     powerServer.parseStatus(cmd);
		     powerServer.sendStatus(InstanceManager.powerManagerInstance().getPower());
                     } catch(jmri.JmriException je) {
                       outStream.writeBytes("not supported\n");
                     }
                 } else if(cmd.startsWith("TURNOUT")){
	         try {
                     turnoutServer.parseStatus(cmd);
                     } catch(jmri.JmriException je) {
                       outStream.writeBytes("not supported\n");
                     }
                 } else if(cmd.startsWith("LIGHT")){
	         try {
                     lightServer.parseStatus(cmd);
                     } catch(jmri.JmriException je) {
                       outStream.writeBytes("not supported\n");
                     }
                 } else if(cmd.startsWith("SENSOR")){
	         try {
                     sensorServer.parseStatus(cmd);
                     } catch(jmri.JmriException je) {
                       outStream.writeBytes("not supported\n");
                     }
                 } else {
	      outStream.writeBytes("Unknown Command\n");
           } 
	 }	
       }

     static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleServer.class.getName());
}

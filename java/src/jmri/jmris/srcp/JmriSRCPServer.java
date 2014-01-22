// JmriSRCPServer.java

package jmri.jmris.srcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmris.*;

import java.io.*;

import java.util.ResourceBundle;

// imports for ZeroConf.
import jmri.util.zeroconf.ZeroConfService;

import jmri.jmris.srcp.parser.SRCPParser;
import jmri.jmris.srcp.parser.ParseException;
import jmri.jmris.srcp.parser.TokenMgrError;
import jmri.jmris.srcp.parser.SRCPVisitor;
import jmri.jmris.srcp.parser.SimpleNode;

/**
 * This is an implementation of SRCP for JMRI.
 * @author Paul Bender Copyright (C) 2009
 * @version $Revision$
 *
 */
public class JmriSRCPServer extends JmriServer{

    // There are 3 possible modes (from the SRCP standards).
    private static int HANDSHAKEMODE =1;
    private static int COMMANDMODE = 2;
    private static int INFOMODE = 4;

     private int SRCPSERVERMODE = 1;
     private static JmriServer _instance = null;

     static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmris.srcp.JmriSRCPServerBundle");

     public static JmriServer instance(){
         if(_instance==null) { 
             int port=java.lang.Integer.parseInt(rb.getString("JMRISRCPServerPort"));
             _instance=new JmriSRCPServer(port);
         }
         return _instance;
     }

     // Create a new server using the default port
     public JmriSRCPServer() {
	super(4303);  // 4303 is assigned to SRCP by IANA.
     }

     public JmriSRCPServer(int port) {
	super(port);
     }

     // Advertise the service with ZeroConf
     protected void advertise(){
        service = ZeroConfService.create("_srcp._tcp.local.", portNo);
        service.publish();
           }
     
     // Handle communication to a client through inStream and outStream
     @SuppressWarnings("deprecation")
     public void handleClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        // Listen for commands from the client until the connection closes
        SRCPParser parser = null;
	String cmd; 
	int index=0;
        int runmode=HANDSHAKEMODE;
        SRCPSERVERMODE=HANDSHAKEMODE; 

        // interface components
        ServiceHandler sh= new ServiceHandler();
        sh.setPowerServer(new JmriSRCPPowerServer(outStream));
        sh.setTurnoutServer(new JmriSRCPTurnoutServer(inStream,outStream));
        sh.setSensorServer(new JmriSRCPSensorServer(inStream,outStream));
        sh.setProgrammerServer(new JmriSRCPProgrammerServer(outStream));
        sh.setTimeServer(new JmriSRCPTimeServer(outStream));        

          // Start by sending a welcome message
          TimeStampedOutput.writeTimestamp(outStream,"SRCP 0.8.3\n\r");

	    while(true) {
	   // Read the command from the client
           
           index = 0;
           if(SRCPSERVERMODE == HANDSHAKEMODE) 
           {
              while((cmd = inStream.readLine()).equals("")); 
              if(log.isDebugEnabled()) log.debug("Received from client: " + cmd);
              if(cmd.startsWith("SET")){
                 index = cmd.indexOf(" ",index)+1;
                 if(cmd.substring(index).startsWith("PROTOCOL SRCP")) {
                   if(cmd.contains("0.8"))
	            TimeStampedOutput.writeTimestamp(outStream,"201 OK PROTOCOL SRCP\n\r");
                   else
	            TimeStampedOutput.writeTimestamp(outStream,"400 ERROR unsupported protocol\n\r");
                                    
                 } else if(cmd.substring(index).startsWith("CONNECTIONMODE SRCP")) {
                        index=cmd.indexOf(" ",index)+1;
                        index=cmd.indexOf(" ",index)+1;
                        if(cmd.substring(index).startsWith("COMMAND")){
                           runmode=COMMANDMODE;
                           TimeStampedOutput.writeTimestamp(outStream,"202 OK CONNECTIONMODEOK\n\r");
                        } else if(cmd.substring(index).startsWith("INFO")){
                           runmode=INFOMODE;
                           TimeStampedOutput.writeTimestamp(outStream,"202 OK CONNECTIONMODEOK\n\r");
                        } else {
                           TimeStampedOutput.writeTimestamp(outStream,"401 ERROR unsupported connection mode\n\r");
                        }
                 } else { 
	            TimeStampedOutput.writeTimestamp(outStream,"500 ERROR out of resources\n\r");
                 }
              } else if (cmd.contains("GO")){
                if(runmode==0){
                  TimeStampedOutput.writeTimestamp(outStream,"402 ERROR insufficient data\n\r");
                } else {
                  SRCPSERVERMODE = runmode;
                  if(log.isDebugEnabled()) log.debug("Switching to runmode after GO");
                  TimeStampedOutput.writeTimestamp(outStream,"200 OK GO 1\n\r");
                }
              } else {
                  TimeStampedOutput.writeTimestamp(outStream,"402 ERROR insufficient data\n\r");
	      }
           } else if (SRCPSERVERMODE == COMMANDMODE ){

              if(parser==null) parser = new SRCPParser(inStream); 
              try {
                  SimpleNode e=parser.command();
                  SRCPVisitor v = new SRCPVisitor();
                  e.jjtAccept(v,sh);
                  // for simple tasks, we're letting the visitor
                  // generate the response.  If this happens, we
                  // need to send the message out.
                  if(v.getOutputString()!=null)
                    TimeStampedOutput.writeTimestamp(outStream,v.getOutputString()+"\n\r");
              } catch (ParseException pe){
                   if(log.isDebugEnabled())
                   {
                      log.debug("Parse Exception");
                      pe.printStackTrace();
                   }
                   TimeStampedOutput.writeTimestamp(outStream,"425 ERROR not supported\n\r");
                   // recover by consuming tokens in the token stream
                   // until we reach the end of the line.
                   while((parser.getNextToken()).kind!=
                          jmri.jmris.srcp.parser.SRCPParserConstants.EOL);
              } catch (TokenMgrError tme) {
                   if(log.isDebugEnabled())
                   {
                      log.debug("Token Manager Exception");
                      tme.printStackTrace();
                   }
                   TimeStampedOutput.writeTimestamp(outStream,"410 ERROR unknown command\n\r");
              }
           } else if (SRCPSERVERMODE == INFOMODE) {
              cmd = inStream.readLine(); 
              if(log.isDebugEnabled()) log.debug("Received from client: " + cmd);
             // input commands are ignored in INFOMODE. 
           } else {
	      TimeStampedOutput.writeTimestamp(outStream,"500 ERROR out of resources\n\r");
              outStream.close();
              inStream.close();
              return;
           } 
	 }	
       }

     static Logger log = LoggerFactory.getLogger(JmriSRCPServer.class.getName());
}

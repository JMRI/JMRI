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

        // interface components
        JmriSRCPServiceHandler sh= new JmriSRCPServiceHandler(12345); // need real client port.
        sh.setPowerServer(new JmriSRCPPowerServer(outStream));
        sh.setTurnoutServer(new JmriSRCPTurnoutServer(inStream,outStream));
        sh.setSensorServer(new JmriSRCPSensorServer(inStream,outStream));
        sh.setProgrammerServer(new JmriSRCPProgrammerServer(outStream));
        sh.setTimeServer(new JmriSRCPTimeServer(outStream));        

          // Start by sending a welcome message
          TimeStampedOutput.writeTimestamp(outStream,"SRCP 0.8.3\n\r");

	    while(true) {
	   // Read the command from the client
           
           if(!(sh.getRunMode())) 
           {
              // we start in handshake mode.
              if(parser==null) parser = new SRCPParser(inStream); 
              try {
                  SimpleNode e=parser.handshakecommand();
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
                   jmri.jmris.srcp.parser.Token t = parser.getNextToken();
                   if(t.kind==jmri.jmris.srcp.parser.SRCPParserConstants.EOF){
                      // the input ended.  
                      if(log.isDebugEnabled())
                      {
                         log.debug("Closing connection due to close of input stream");
                      }
                      outStream.close();
                      inStream.close();
                      return;
                   }
                   TimeStampedOutput.writeTimestamp(outStream,"425 ERROR not supported\n\r");
                   // recover by consuming tokens in the token stream
                   // until we reach the end of the line.
                   while(t.kind!=jmri.jmris.srcp.parser.SRCPParserConstants.EOL)
                          t = parser.getNextToken();
              } 
           } else if (sh.isCommandMode() ){

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
                   jmri.jmris.srcp.parser.Token t = parser.getNextToken();
                   if(t.kind==jmri.jmris.srcp.parser.SRCPParserConstants.EOF){
                      // the input ended.  The parser may have prepared 
                      // an output string to return (if the client issued
                      // a "TERM 0 SESSION" request).
                      //if(v.getOutputString()!=null)
                      //   TimeStampedOutput.writeTimestamp(outStream,v.getOutputString()+"\n\r");
                      // and we can close the connection.
                      if(log.isDebugEnabled())
                      {
                         log.debug("Closing connection due to close of input stream");
                      }
                      outStream.close();
                      inStream.close();
                      return;
                   }
                   TimeStampedOutput.writeTimestamp(outStream,"425 ERROR not supported\n\r");
                   // recover by consuming tokens in the token stream
                   // until we reach the end of the line.
                   while(t.kind!=jmri.jmris.srcp.parser.SRCPParserConstants.EOL)
                          t = parser.getNextToken();
              } catch (TokenMgrError tme) {
                   if(log.isDebugEnabled())
                   {
                      log.debug("Token Manager Exception");
                      tme.printStackTrace();
                   }
                   TimeStampedOutput.writeTimestamp(outStream,"410 ERROR unknown command\n\r");
              }
           } else if (!sh.isCommandMode()) {
              BufferedReader d = new BufferedReader(new InputStreamReader(inStream,
                                  java.nio.charset.Charset.forName("UTF-8")));
              try {
                 String cmd = d.readLine(); 
                 if(cmd!=null) { 
                 if(log.isDebugEnabled()) log.debug("Received from client: " + cmd);
                 // input commands are ignored in INFOMODE.
                 } else {
                   // close the input stream.
                   d.close();
                   inStream.close();
                 }
              } catch ( java.io.IOException ioe ){
                 // we don't care if there is an error on input.
              }
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

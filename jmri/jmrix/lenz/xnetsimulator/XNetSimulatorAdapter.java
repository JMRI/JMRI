// XNetSimulatorAdapter.java

package jmri.jmrix.lenz.xnetsimulator;

import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetPacketizer;
import jmri.jmrix.lenz.XNetPortController;
import jmri.jmrix.lenz.XNetInitilizationManager;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.AbstractMRTrafficController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Provide access to a simulated XPressNet system.
 *
 * Currently, the XNetSimulator reacts to commands sent from the user interface
 * with messages an appropriate reply message.
 *
 **NOTE: Most XPressNet commands are still unsupported in this implementation.
 *
 * Normally controlled by the lenz.XNetSimulator.XNetSimulatorFrame class.
 *
 *NOTE: Some material in this file was modified from other portions of the 
 *      support infrastructure.
 * 
 * @author			Paul Bender, Copyright (C) 2009
 * @version			$Revision: 1.1 $
 */

public class XNetSimulatorAdapter extends XNetPortController implements Runnable{
    
    private boolean OutputBufferEmpty = true;
    private boolean CheckBuffer = true;
   
    public XNetSimulatorAdapter() {
        try {
            PipedOutputStream tempPipeI=new PipedOutputStream();
            pout=new DataOutputStream(tempPipeI);
            inpipe=new DataInputStream(new PipedInputStream(tempPipeI));
            PipedOutputStream tempPipeO=new PipedOutputStream();
            outpipe = new DataOutputStream(tempPipeO);
            pin = new DataInputStream(new PipedInputStream(tempPipeO));
        }
        catch (java.io.IOException e) {
            log.error("init (pipe): Exception: "+e.toString());
        }
        configure();
    }

    public String openPort(String portName, String appName)  {
        // open the port in XPressNet mode, check ability to set moderators
        return null; // normal operation
    }
    
    /**
     * we need a way to say if the output buffer is empty or full
     * this should only be set to false by external processes
     **/         
    synchronized public void setOutputBufferEmpty(boolean s)
    {
        OutputBufferEmpty = s;
    }
    
    /**
     * Can the port accept additional characters?
     * The state of CTS determines this, as there seems to
     * be no way to check the number of queued bytes and buffer length.
     * This might
     * go false for short intervals, but it might also stick
     * off if something goes wrong.
     */
    public boolean okToSend() {
            if(CheckBuffer) {
                log.debug("Buffer Empty: " + OutputBufferEmpty);
                return (OutputBufferEmpty);
            } else {
                log.debug("No Flow Control or Buffer Check");
                return(true);
            }
    }
    
    /**
     * set up all of the other objects to operate with a XNetSimulator
     * connected to this port
     */
    public void configure() {
        // connect to a packetizing traffic controller
        AbstractMRTrafficController packets = (AbstractMRTrafficController) (new XNetPacketizer(new LenzCommandStation()));
        packets.connectPort(this);
        
        // start operation
        // packets.startThreads();
        sourceThread = new Thread(this);
        sourceThread.start();

        new XNetInitilizationManager();
        
        jmri.jmrix.lenz.ActiveFlag.setActive();
    }
    
    // base class methods for the XNetPortController interface
    public DataInputStream getInputStream() {
        if (pin == null ) 
            log.error("getInputStream called before load(), stream not available");
        return pin;
    }
    
    public DataOutputStream getOutputStream() {
        if (pout==null) log.error("getOutputStream called before load(), stream not available");
     	return pout;
    }
    
    public boolean status() {return (pout!=null && pin!=null);}
    
    /**
     * Get an array of valid baud rates. This is currently just a message
     * saying its fixed
     */
    public String[] validBaudRates() {
        return null;
    }
    
    private boolean opened = false;
    
    static public XNetSimulatorAdapter instance() {
        if (mInstance == null) mInstance = new XNetSimulatorAdapter();
        return mInstance;
    }

    public void run(){ // start a new thread
       // this thread has one task.  It repeatedly reads from the input pipe
       // and writes modified data to the output pipe.  This is the heart
       // of the command station simulation.
       log.debug("Simulator Thread Started");
       for(;;){
         XNetMessage m=readMessage();
         log.debug("Simulator Thread received message " + m.toString() );
         XNetReply r=generateReply(m);
         writeReply(r);
         log.debug("Simulator Thread sent Reply" + r.toString() );
       }
    }

    private XNetMessage readMessage(){
      XNetMessage msg = null;
      try{
         msg= loadChars();
      } catch( java.io.IOException e){
        // should do something meaningful here.
      } 
      setOutputBufferEmpty(true);
      return(msg);
    }

    private XNetReply generateReply(XNetMessage m){
            XNetReply reply = new XNetReply();
            switch(m.getElement(0)){

               case XNetConstants.CS_REQUEST:
                   switch(m.getElement(1)){
                   case XNetConstants.CS_VERSION:
                     reply.setOpCode(XNetConstants.CS_SERVICE_MODE_RESPONSE);
                     reply.setElement(1,XNetConstants.CS_SOFTWARE_VERSION);
                     reply.setElement(2,0x36 & 0xff ); // indicate we are version 3.6
                     reply.setElement(3,0x00 &0xff ); // indicate we are an LZ100;
                     reply.setElement(4,0x00); // set the parity byte to 0
                     reply.setParity();
                     break;
                   default:
		     reply.setOpCode(XNetConstants.CS_INFO);
                     reply.setElement(1,XNetConstants.CS_NOT_SUPPORTED & 0xff);
                     reply.setElement(2,0x00); // set the parity byte to 0
                     reply.setParity();
                   } 
                   break;
               default:
		  reply.setOpCode(XNetConstants.CS_INFO);
                  reply.setElement(1,XNetConstants.CS_NOT_SUPPORTED);
                  reply.setElement(2,0x00); // set the parity byte to 0
                  reply.setParity();
            }
            return(reply);
    }

    private void writeReply(XNetReply r){
      int i;
      int len = (r.getElement(0)&0x0f)+2;  // opCode+Nbytes+ECC
      for(i=0;i<len;i++)
        try {
	   outpipe.writeByte((byte)r.getElement(i));
        } catch  ( java.io.IOException ex){
        }
    }

    /**
     * Get characters from the input source, and file a message.
     * <P>
     * Returns only when the message is complete.
     * <P>
     * Only used in the Receive thread.
     *
     * @param msg message to fill
     * @param istream character source.
     * @throws IOException when presented by the input source.
     */
    private XNetMessage loadChars() throws java.io.IOException{
        int i;
        byte char1;
        char1 =readByteProtected(inpipe);
        int len = (char1&0x0f)+2;  // opCode+Nbytes+ECC
        XNetMessage msg=new XNetMessage(len);
        msg.setElement(0,char1&0xFF);
        for (i = 1; i < len; i++) {
               char1 = readByteProtected(inpipe);
            msg.setElement(i, char1 &0xFF);
         }
         return msg;
     }

    /**
     * Read a single byte, protecting against various timeouts, etc.
     * <P>
     * When a javax.comm port is set to have a
     * receive timeout (via the enableReceiveTimeout() method),
     * some will return zero bytes or an EOFException at the end of the timeout.
     * In that case, the read should be repeated to get the next real character.
     *
     */
    protected byte readByteProtected(DataInputStream istream) throws java.io.IOException {
        byte[] rcvBuffer = new byte[1]; 
        while (true) { // loop will repeat until character found
            int nchars;
            nchars = istream.read(rcvBuffer, 0, 1);
            if (nchars>0) return rcvBuffer[0];
        }
    }

    static XNetSimulatorAdapter mInstance = null;
    private DataOutputStream pout=null; // for output to other classes
    private DataInputStream pin = null; // for input from other classes    
    // internal ends of the pipes
    private DataOutputStream outpipe = null;  // feed pin
    private DataInputStream inpipe = null; // feed pout
    private Thread sourceThread;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetSimulatorAdapter.class.getName());
    
}

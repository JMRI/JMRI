package jmri.jmrix.roco.z21.simulator;

import java.net.*;
import jmri.JmriException;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.roco.z21.Z21Adapter;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21Reply;
import jmri.jmrix.roco.z21.Z21TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provide access to a simulated z21 system.
 * <p>
 * Currently, the z21Simulator reacts to commands sent from the user interface
 * with messages an appropriate reply message.
 * <p>
 * NOTE: Some material in this file was modified from other portions of the
 * support infrastructure.
 *
 * @author	Paul Bender, Copyright (C) 2015
 */
public class Z21SimulatorAdapter extends Z21Adapter implements Runnable {

    private Thread sourceThread;
    private Z21XNetSimulatorAdapter xnetadapter = null;

    // simulation state variables
    private int flags[]={0x00,0x00,0x00,0x00}; // holds the flags sent by the client.

    public Z21SimulatorAdapter() {
        super();
        setHostName("localhost");
        // start a UDP server that we can connect to.  The server will
        // produce the appropriate responses.
        xnetadapter = new Z21XNetSimulatorAdapter();
    }

    /**
     * Set up all of the other objects to operate with a z21Simulator connected
     * to this port.
     */
    @Override
    public void configure() {
        log.debug("configure called");

        // connect to a packetizing traffic controller
        Z21TrafficController packets = new Z21TrafficController();
        packets.connectPort(this);

        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setTrafficController(packets);

        sourceThread = new Thread(this);
        sourceThread.setName("Z21SimulatorAdapter sourceThread");
        sourceThread.start();

        this.getSystemConnectionMemo().configureManagers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect() throws java.io.IOException {
        log.debug("connect called");

       setHostAddress("localhost"); // always localhost for the simulation.
       super.connect();
    }

    /**
     * Terminate service thread
     * <p>
     * This is intended to be used only by testing subclasses.
     */
    public void terminateThread() {
        threadStopRequest = true;
        if (sourceThread != null) {
            sourceThread.interrupt();
            try {
                sourceThread.join();
            } catch (InterruptedException ie){
                // interrupted during cleanup.
            }
        }
        if (socket != null) socket.close();
    }

    volatile boolean threadStopRequest;
    volatile DatagramSocket socket;

    static class LogoffException extends JmriException {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        // The server just opens a DatagramSocket using the specified port number,
        // and then goes into an infinite loop.

        // try connecting to the port up to three times
	int retryCount = 0;
	   while(retryCount<3 && !threadStopRequest) {
           try (DatagramSocket s = new DatagramSocket(COMMUNICATION_UDP_PORT)) {
   
               socket = s; // save for later close()
               s.setSoTimeout(100); // timeout periodically
               log.debug("socket created, starting loop");
               while(!threadStopRequest){
                   log.debug("simulation loop");
                   // the server waits for a client to connect, then echos the data sent back.
                   byte[] input=new byte[100]; // input from network
                   try {
   
                       // to receive the data, we create a packet.
                       DatagramPacket receivePacket = new DatagramPacket(input,100);
                       // and wait for the data to arrive.
                       s.receive(receivePacket);
                       if (threadStopRequest) return;
   
                       Z21Message msg = new Z21Message(receivePacket.getLength());
                       for(int i=0;i< receivePacket.getLength();i++)
                           msg.setElement(i,receivePacket.getData()[i]);
  
                       // to echo the data back, we need to find the IP and port to send
                       // the data to.
                       InetAddress IPAddress = receivePacket.getAddress();
                       int port = receivePacket.getPort();
   
                       log.debug("Received packet: {}, message: {}",receivePacket.getData(),msg);
   
                       Z21Reply reply;
                       // and then we create the return packet.
                       try {
                           reply = generateReply(msg);
                       } catch (LogoffException e) {
                           // the simulation ends here. break out of the loop.
                           log.debug("error generated by generateReply, exiting simulation");
                           break;
                       }
                       if(reply != null) {
                          // only attempt to send a reply if there was actually
                          // a reply generated, since some messages don't do that.
                          byte ba[] = jmri.util.StringUtil.bytesFromHexString(reply.toString());
                          DatagramPacket sendPacket = new DatagramPacket(ba,ba.length,IPAddress,port);
                          // and send it back using our socket
                          s.send(sendPacket);
                       }
                    } catch (java.net.SocketTimeoutException ste){
                       // not an error, recheck the condition on the while.
                       continue;
                    } catch (java.io.IOException ex3) {
                       if (!threadStopRequest) {
                          log.error("IO Exception", ex3);
                       } else {
                          return;
                       }
                   }
                   log.debug("Client Disconnect");
               }
           } catch (BindException bex ) {
	          retryCount++;
	          if(retryCount > 2 ) { 
                 log.error("Giving up after {} attempts.  Exception binding to port {}",retryCount,COMMUNICATION_UDP_PORT,bex);
	             return;
	           } else {
                  log.info("Attempt {}: Exception binding to port {}",retryCount,COMMUNICATION_UDP_PORT);
		          try {
		             Thread.sleep(retryCount * 1000L); // wait a few seconds before attempting to bind again.
		          } catch(InterruptedException ie){ 
                      // the sleep is just to give time for another process
                      // to exit, so it is ok if it finishes early.
                  }
               }
           } catch (SocketException ex0 ) {
               log.error("Exception opening socket", ex0);
               return; // can't continue from this
           } catch (RuntimeException rte) {
               // subclasses of RuntimeException may occur at times other than 
               // when opening the socket.
               log.error("Exception performing operation on socket", rte);
               return; // can't continue from this
           } 
       } // end of bind retry.
    } // end of run.

    // generateReply is the heart of the simulation.  It translates an
    // incoming XNetMessage into an outgoing XNetReply.
    private Z21Reply generateReply(Z21Message m) throws LogoffException {
        log.debug("generate Reply called with message {}",m);
        Z21Reply reply;
        switch (m.getOpCode()) {
             case 0x0010:
                // request for serial number
                reply = getZ21SerialNumberReply();
                break;
             case 0x001a:
             // request for hardware version info.
                reply=getHardwareVersionReply();
                break;
             case 0x0040:
                // XpressNet tunnel message.
                XNetMessage xnm = getXNetMessage(m);
                log.debug("Received XNet Message: {}",  m);
                XNetReply xnr=xnetadapter.generateReply(xnm);
                reply = getZ21ReplyFromXNet(xnr);
                break;
             case 0x0030:
                // LAN LOGOFF
                // this is the end of the simulation, throw an exception
                // to indicate this.
                throw(new LogoffException());
             case 0x0050:
                // set broadcast flags
                flags[0]=m.getElement(4)&0xff;
                flags[1]=m.getElement(5)&0xff;
                flags[2]=m.getElement(6)&0xff;
                flags[3]=m.getElement(7)&0xff;
                // per the protocol, no reply is generated.
                reply = null;
                break;
             case 0x0051:
                // get broadcast flags
                reply = getZ21BroadCastFlagsReply();
                break;
             case 0x0089:
                // Get Railcom Data
                reply = getZ21RailComDataChangedReply();
                break;
             case 0x00A2:
                  // loconet data from lan
                  reply = null; // for now, no reply to this message.
		  break;
             case 0x00A3:
                  // loconet dispatch address
                  reply = getLocoNetDispatchReply(m);
		  break;
             case 0x00A4:
                  // get loconet detector status
                  reply = getLocoNetDetectorStatusReply(m);
		  break;
             case 0x0060:
                // get loco mode
             case 0x0061:
                // set loco mode
             case 0x0070:
                // get turnout mode
             case 0x0071:
                // set turnout mode
             case 0x0081:
                // get RMBus data
             case 0x0082:
                // program RMBus module
             case 0x0085:
                // get system state
             default:
                reply=getXPressNetUnknownCommandReply();
        }
        return reply;
    }

    // canned reply messages;
    private Z21Reply getHardwareVersionReply(){
        Z21Reply reply = new Z21Reply();
        reply.setLength(0x000c);
        reply.setOpCode(0x001a);
        reply.setElement(4,0x00);
        reply.setElement(5,0x02);
        reply.setElement(6,0x00);
        reply.setElement(7,0x00);
        reply.setElement(8,0x20);
        reply.setElement(9,0x01);
        reply.setElement(10,0x00);
        reply.setElement(11,0x00);
        return reply;
    }

    private Z21Reply getXPressNetUnknownCommandReply(){
        Z21Reply reply = new Z21Reply();
        reply.setLength(0x0007);
        reply.setOpCode(0x0040);
        reply.setElement(4,0x61);
        reply.setElement(5,0x82);
        reply.setElement(6,0xE3);
        return reply;
    }

    private Z21Reply getZ21SerialNumberReply(){
        Z21Reply reply = new Z21Reply();
        reply.setLength(0x0008);
        reply.setOpCode(0x0010);
        reply.setElement(4,0x00);
        reply.setElement(5,0x00);
        reply.setElement(6,0x00);
        reply.setElement(7,0x00);
        return reply;
    }

    private Z21Reply getZ21BroadCastFlagsReply(){
        Z21Reply reply = new Z21Reply();
        reply.setLength(0x0008);
        reply.setOpCode(0x0051);
        reply.setElement(4,flags[0]);
        reply.setElement(5,flags[1]);
        reply.setElement(6,flags[2]);
        reply.setElement(7,flags[3]);
        return reply;
    }

    private Z21Reply getZ21RailComDataChangedReply(){
        Z21Reply reply = new Z21Reply();
        reply.setOpCode(0x0088);
        reply.setLength(0x0004);
        int offset=4;
        for(int i = 0;i<xnetadapter.locoCount;i++) {
            reply.setElement(offset++,xnetadapter.locoData[i].getAddressLsb());// byte 5, LocoAddress lsb.
            reply.setElement(offset++,xnetadapter.locoData[i].getAddressMsb());// byte 6, LocoAddress msb.
            reply.setElement(offset++,0x00);// bytes 7-10,32 bit reception counter.
            reply.setElement(offset++,0x00);
            reply.setElement(offset++,0x00);
            reply.setElement(offset++,0x01);
            reply.setElement(offset++,0x00);// bytes 11-14,32 bit error counter.
            reply.setElement(offset++,0x00);
            reply.setElement(offset++,0x00);
            reply.setElement(offset++,0x00);
            reply.setElement(offset++,xnetadapter.locoData[i].getSpeed());//currently reserved.Speed in firmware<=1.12
            reply.setElement(offset++,0x00);//currently reserved.Options in firmware<=1.12
            reply.setElement(offset++,0x00);//currently reserved.Temp in firmware<=1.12
            reply.setLength(0xffff & offset);
        }
        log.debug("output {} offset: {}", reply.toString(),offset);
        return reply;
    }

    // utility functions

    private XNetMessage getXNetMessage(Z21Message m) {
        if(m==null) throw new IllegalArgumentException();
        XNetMessage xnm = new XNetMessage(m.getLength()-4);
        for (int i = 4; i < m.getLength(); i++) {
           xnm.setElement(i - 4, m.getElement(i));
        }
        return xnm;
    }

    private Z21Reply getZ21ReplyFromXNet(XNetReply m) {
        if(m==null) throw new IllegalArgumentException();
        Z21Reply r=new Z21Reply();
        r.setLength(m.getNumDataElements() + 4);
        r.setOpCode(0x0040);
        for (int i = 0; i < m.getNumDataElements(); i++) {
            r.setElement(i + 4, m.getElement(i));
        }
        return(r);
    }

    private Z21Reply getLocoNetDispatchReply(Z21Message m) {
        if(m==null) throw new IllegalArgumentException();
        Z21Reply r=new Z21Reply();
        r.setLength(m.getNumDataElements() + 5);
        r.setOpCode(m.getOpCode());
	int i;
        for (i = 0; i < m.getNumDataElements(); i++) {
            r.setElement(i + 4, m.getElement(i));
        }
	r.setElement(i+4,0x00);
        return(r);
    }

    private Z21Reply getLocoNetDetectorStatusReply(Z21Message m) {
        if(m==null) throw new IllegalArgumentException();
        Z21Reply r=new Z21Reply();
        r.setLength(m.getNumDataElements() + 5);
        r.setOpCode(m.getOpCode());
	int i;
        for (i = 0; i < m.getNumDataElements(); i++) {
            r.setElement(i + 4, m.getElement(i));
        }
	r.setElement(i+4,0x00);
        return(r);
    }

    private final static Logger log = LoggerFactory.getLogger(Z21SimulatorAdapter.class);

}

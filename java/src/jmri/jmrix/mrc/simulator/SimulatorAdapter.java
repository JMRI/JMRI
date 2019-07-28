package jmri.jmrix.mrc.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jmri.jmrix.mrc.MrcMessage;
import jmri.jmrix.mrc.MrcPacketizer;
import jmri.jmrix.mrc.MrcPackets;
import jmri.jmrix.mrc.MrcPortController;
import jmri.jmrix.mrc.MrcSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MRC simulator.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Paul Bender, Copyright (C) 2009
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class SimulatorAdapter extends MrcPortController implements Runnable {

    // private control members
    private boolean opened = false;
    private Thread sourceThread;

    // streams to share with user class
    private DataOutputStream pout = null; // this is provided to classes who want to write to us
    private DataInputStream pin = null; // this is provided to classes who want data from us

    // internal ends of the pipes
    private DataOutputStream outpipe = null; // feed pin
    private DataInputStream inpipe = null; // feed pout

    public SimulatorAdapter() {
        super(new MrcSystemConnectionMemo());
    }

    @Override
    public String openPort(String portName, String appName) {
        try {
            PipedOutputStream tempPipeI = new PipedOutputStream();
            pout = new DataOutputStream(tempPipeI);
            inpipe = new DataInputStream(new PipedInputStream(tempPipeI));
            PipedOutputStream tempPipeO = new PipedOutputStream();
            outpipe = new DataOutputStream(tempPipeO);
            pin = new DataInputStream(new PipedInputStream(tempPipeO));
        } catch (java.io.IOException e) {
            log.error("init (pipe): Exception: ", e); // NOI18N
        }
        opened = true;
        return null; // indicates OK return
    }

    /**
     * Set up all of the other objects to simulate operation with an MRC command
     * station.
     */
    @Override
    public void configure() {
        MrcPacketizer tc = new MrcPacketizer();
        tc.connectPort(this);
        this.getSystemConnectionMemo().setMrcTrafficController(tc);
        tc.setAdapterMemo(this.getSystemConnectionMemo());
        //tc.connectPort(this);     

        this.getSystemConnectionMemo().configureManagers();
        tc.setCabNumber(2);

        // start the simulator
        sourceThread = new Thread(this);
        sourceThread.setName("Mrc Simulator"); // NOI18N
        sourceThread.setPriority(Thread.MIN_PRIORITY);
        sourceThread.start();
        tc.startThreads();
    }

    // base class methods for the MrcPortController interface
    @Override
    public DataInputStream getInputStream() {
        if (!opened || pin == null) {
            log.error("getInputStream called before load(), stream not available"); // NOI18N
        }
        return pin;
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (!opened || pout == null) {
            log.error("getOutputStream called before load(), stream not available"); // NOI18N
        }
        return pout;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        log.debug("validBaudRates should not have been invoked"); // NOI18N
        return new String[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{};
    }

    @Override
    public String getCurrentBaudRate() {
        return "";
    }

    @Override
    public String getCurrentPortName(){
        return "";
    }

    @Override
    public void run() { // start a new thread
        // This thread has one task. It repeatedly reads from the input pipe
        // and writes an appropriate response to the output pipe. This is the heart
        // of the MRC command station simulation.
        // report status?
        log.info("MRC Simulator Started"); // NOI18N
        int cab = 1;
        while (true) {
            try {
                synchronized (this) {
                    wait(100);
                }
            } catch (InterruptedException e) {
                log.debug("Interrupted, ending");
                return;
            }
            MrcMessage m = readMessage();
            if (log.isDebugEnabled()) {
                StringBuffer buf = new StringBuffer();
                buf.append("Mrc Simulator Thread received message: "); // NOI18N
                if (m != null) {
                    for (int i = 0; i < m.getNumDataElements(); i++) {
                        buf.append(Integer.toHexString(0xFF & m.getElement(i)) + " ");
                    }
                } else {
                    buf.append("null message buffer"); // NOI18N
                }
                log.debug(buf.toString());
            }
            if (m != null && m.getNumDataElements() > 4) {
                //Send a default good reply message
                MrcMessage r = new MrcMessage(4);
                r.setElement(0, MrcPackets.GOODCMDRECEIVEDCODE);
                r.setElement(1, 0x0);
                r.setElement(2, MrcPackets.GOODCMDRECEIVEDCODE);
                r.setElement(3, 0x0);
                writeReply(r);
                if (m.isReplyExpected()) {
                    r = generateReply(m);
                    writeReply(r);
                }
                if (log.isDebugEnabled()) {
                    StringBuffer buf = new StringBuffer();
                    buf.append("Mrc Simulator Thread sent reply: "); // NOI18N
                    for (int i = 0; i < r.getNumDataElements(); i++) {
                        buf.append(Integer.toHexString(0xFF & r.getElement(i)) + " ");
                    }
                    log.debug(buf.toString());
                }
            } else {
                if (cab > 8) {
                    cab = 1;
                }
                int[] poll = new int[]{cab, 1, cab, 0, cab, 0};
                cab++;
                MrcMessage r = new MrcMessage(poll);
                writeReply(r);
            }
        }
    }

    // readMessage reads one incoming message from the buffer
    private MrcMessage readMessage() {
        MrcMessage msg = null;
        try {
            if (inpipe.available() > 0) {
                msg = loadChars();
            }
        } catch (java.io.IOException e) {

        }
        return (msg);
    }

    /**
     * Get characters from the input source.
     *
     * @return filled message
     * @throws IOException when presented by the input source.
     */
    private MrcMessage loadChars() throws java.io.IOException {
        int nchars;
        byte[] rcvBuffer = new byte[32];

        nchars = inpipe.read(rcvBuffer, 0, 32);
        //log.debug("new message received");
        MrcMessage msg = new MrcMessage(nchars);

        for (int i = 0; i < nchars; i++) {
            msg.setElement(i, rcvBuffer[i] & 0xFF);
        }
        return msg;
    }

    // generateReply is the heart of the simulation.  It translates an 
    // incoming MrcMessage into an outgoing MrcReply.
    private MrcMessage generateReply(MrcMessage m) {
        MrcMessage reply = new MrcMessage(4);
        if (m.getNumDataElements() < 4) {
            reply.setElement(0, MrcPackets.BADCMDRECEIVEDCODE);
            reply.setElement(1, 0x0);
            reply.setElement(2, MrcPackets.BADCMDRECEIVEDCODE);
            reply.setElement(3, 0x0);
            return reply;
        }
        int command = m.getElement(0);
        if (command != m.getElement(2) && m.getElement(1) != 1) {
            reply.setElement(0, MrcPackets.BADCMDRECEIVEDCODE);
            reply.setElement(1, 0x0);
            reply.setElement(2, MrcPackets.BADCMDRECEIVEDCODE);
            reply.setElement(3, 0x0);
            return reply;
        }
        switch (command) {
            case MrcPackets.SETCLOCKRATIOCMD:  // set fast clock ratio
//   reply.setElement(0, 0x06);
//   reply.setElement(1, 0x02);
//   reply.setElement(2, 0x01);
                break;
            case MrcPackets.SETCLOCKTIMECMD: // Set clock
                break;
            case MrcPackets.SETCLOCKAMPMCMD: // Set clock mode
                break;
            case MrcPackets.THROTTLEPACKETCMD: // Set clock mode
                reply.setElement(0, MrcPackets.LOCOSOLECONTROLCODE);
                reply.setElement(1, 0x00);
                reply.setElement(2, MrcPackets.LOCOSOLECONTROLCODE);
                reply.setElement(3, 0x00);
                break;
//  case MrcMessage.READ_REG_CMD:
//   reply.setElement(0, 0xff);   // dummy data
//   //reply.setElement(1,MRC_DATA_OUT_OF_RANGE);  // forces fail
//   reply.setElement(1,MRC_OKAY);  // forces succeed
//   break;
            case MrcPackets.FUNCTIONGROUP2PACKETCMD:
                // Use this to simulate a missed poll
                reply = new MrcMessage(6);
                reply.setElement(0, 0x03);
                reply.setElement(1, 0x01);
                reply.setElement(2, 0x03);
                reply.setElement(3, 0x0);
                reply.setElement(4, 0x03);
                reply.setElement(5, 0x0);
                break;
            default:
                // we don't know what it is but presume ok
                reply.setElement(0, MrcPackets.GOODCMDRECEIVEDCODE);
                reply.setElement(1, 0x0);
                reply.setElement(2, MrcPackets.GOODCMDRECEIVEDCODE);
                reply.setElement(3, 0x0);
                break;
        }
        return reply;
    }

    private void writeReply(MrcMessage r) {
        if (r == null) {
            return;
        }
        for (int i = 0; i < r.getNumDataElements(); i++) {
            try {
                outpipe.writeByte((byte) r.getElement(i));
            } catch (java.io.IOException ex) {
            }
        }
        try {
            outpipe.flush();
        } catch (java.io.IOException ex) {
        }
    }

// private byte[] turnoutMemory = new byte[256];
// private byte[] macroMemory = new byte[256*20+16]; // and a little padding
// private byte[] consistMemory = new byte[256*6+16]; // and a little padding
// /* Read MRC memory.  This implementation simulates reading the MRC
//  * command station memory.  There are three memory blocks that are
//  * supported, turnout status, macros, and consists.  The turnout status
//  * memory is 256 bytes and starts at memory address 0xEC00. The macro memory
//  * is 256*20 or 5120 bytes and starts at memory address 0xC800. The consist
//  * memory is 256*6 or 1536 bytes and starts at memory address 0xF500.
//  * 
//  */
// private MrcReply readMemory (MrcMessage m, MrcReply reply, int num){
//  if (num>16){
//   log.error("Mrc read memory command was greater than 16");
//   return null;
//  }
//  int mrcMemoryAddress = getMrcAddress(m);
//  if (mrcMemoryAddress >= MrcTurnoutMonitor.CS_ACCY_MEMORY && mrcMemoryAddress < MrcTurnoutMonitor.CS_ACCY_MEMORY+256){
//   log.debug("Reading turnout memory: "+Integer.toHexString(mrcMemoryAddress));
//   int offset = m.getElement(2);
//   for (int i=0; i<num; i++)
//    reply.setElement(i, turnoutMemory[offset+i]);
//   return reply;
//  }
//  if (mrcMemoryAddress >= MrcCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM && mrcMemoryAddress < MrcCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM+256*6){
//   log.debug("Reading consist memory: "+Integer.toHexString(mrcMemoryAddress));
//   int offset = mrcMemoryAddress - MrcCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM;
//   for (int i=0; i<num; i++)
//    reply.setElement(i, consistMemory[offset+i]);
//   return reply;
//  }
//  if (mrcMemoryAddress >= MrcCmdStationMemory.CabMemorySerial.CS_MACRO_MEM && mrcMemoryAddress < MrcCmdStationMemory.CabMemorySerial.CS_MACRO_MEM+256*20){
//   log.debug("Reading macro memory: "+Integer.toHexString(mrcMemoryAddress));
//   int offset = mrcMemoryAddress-MrcCmdStationMemory.CabMemorySerial.CS_MACRO_MEM;
//   log.debug("offset:"+offset);
//   for (int i=0; i<num; i++)
//    reply.setElement(i, macroMemory[offset+i]);
//   return reply;
//  }
//  for (int i=0; i<num; i++)
//   reply.setElement(i, 0x00);   // default fixed data
//  return reply;
// }
// private MrcReply writeMemory (MrcMessage m, MrcReply reply, int num, boolean skipbyte){
//  if (num>16){
//   log.error("Mrc write memory command was greater than 16");
//   return null;
//  }
//  int mrcMemoryAddress = getMrcAddress(m);
//  int byteDataBegins = 3;
//  if (skipbyte)
//   byteDataBegins++;
//  if (mrcMemoryAddress >= MrcTurnoutMonitor.CS_ACCY_MEMORY && mrcMemoryAddress < MrcTurnoutMonitor.CS_ACCY_MEMORY+256){
//   log.debug("Writing turnout memory: "+Integer.toHexString(mrcMemoryAddress));
//   int offset = m.getElement(2);
//   for (int i=0; i<num; i++)
//    turnoutMemory[offset+i] = (byte)m.getElement(i+byteDataBegins);
//  }
//  if (mrcMemoryAddress >= MrcCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM && mrcMemoryAddress < MrcCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM+256*6){
//   log.debug("Writing consist memory: "+Integer.toHexString(mrcMemoryAddress));
//   int offset = mrcMemoryAddress-MrcCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM;
//   for (int i=0; i<num; i++)
//    consistMemory[offset+i] = (byte)m.getElement(i+byteDataBegins);
//  }
//  if (mrcMemoryAddress >= MrcCmdStationMemory.CabMemorySerial.CS_MACRO_MEM && mrcMemoryAddress < MrcCmdStationMemory.CabMemorySerial.CS_MACRO_MEM+256*20){
//   log.debug("Writing macro memory: "+Integer.toHexString(mrcMemoryAddress));
//   int offset = mrcMemoryAddress-MrcCmdStationMemory.CabMemorySerial.CS_MACRO_MEM;
//   log.debug("offset:"+offset);
//   for (int i=0; i<num; i++)
//    macroMemory[offset+i] = (byte)m.getElement(i+byteDataBegins);
//  }
//  reply.setElement(0, MRC_OKAY);   // Mrc okay reply!
//  return reply;
// }
// private int getMrcAddress(MrcMessage m){
//  int addr = m.getElement(1);
//  addr = addr * 256;
//  addr = addr + m.getElement(2);
//  return addr;
// }
// 
// private MrcReply accessoryCommand(MrcMessage m, MrcReply reply){
//  if (m.getElement(3) == 0x03 || m.getElement(3) == 0x04){  // 0x03 = close, 0x04 = throw
//   String operation = "close";
//   if (m.getElement(3) == 0x04)
//    operation = "throw";
//   int mrcAccessoryAddress = getMrcAddress(m);
//   log.debug("Accessory command "+operation+" NT"+mrcAccessoryAddress);
//   if (mrcAccessoryAddress > 2044){
//    log.error("Turnout address greater than 2044, address: "+mrcAccessoryAddress );
//    return null;
//   }
//   int bit = (mrcAccessoryAddress-1) & 0x07;
//   int setMask = 0x01;
//   for (int i=0; i<bit; i++){
//    setMask = setMask<<1;
//   }
//   int clearMask = 0x0FFF - setMask;
//   //log.debug("setMask:"+Integer.toHexString(setMask)+" clearMask:"+Integer.toHexString(clearMask));
//   int offset = (mrcAccessoryAddress-1)>>3;
//   int read = turnoutMemory[offset];
//   byte write = (byte)(read & clearMask & 0xFF);
//
//   if (operation.equals("close"))
//    write = (byte)(write + setMask); // set bit if closed
//   turnoutMemory[offset] = write;
//   //log.debug("wrote:"+Integer.toHexString(write)); 
//  }
//  reply.setElement(0, MRC_OKAY);   // Mrc okay reply!
//  return reply;
// }
    private final static Logger log = LoggerFactory
            .getLogger(SimulatorAdapter.class);

}

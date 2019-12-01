package jmri.jmrix.nce.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

import jmri.jmrix.nce.NceCmdStationMemory;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NcePortController;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;
import jmri.jmrix.nce.NceTurnoutMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The following was copied from the NCE Power Pro System Reference Manual. It
 * provides the background for the various NCE command that are simulated by
 * this implementation.
 * <p>
 * Implements a Command Station Simulator for the NCE system.
 * <p>
 * BINARY COMMAND SET
 * <p>
 * The RS-232 port binary commands are designed to work in a computer friendly
 * way. Command format is: {@code <command number> <data> <data> ...}
 * <p>
 * Commands range from 0x80 to 0xBF
 * <p>
 * Commands and formats supported: Commands 0xAD to 0xBF are not used and return
 * '0'
 * <p>
 * Errors returned: '0'= command not supported '1'= loco address out of range
 * '2'= cab address out of range '3'= data out of range '4'= byte count out of
 * range '!'= command completed successfully
 * <p>
 * For a complete description of Binary Commands see:
 * www.ncecorporation.com/pdf/bincmds.pdf
 * <br>
 * <pre>{@literal
 * Command Description (#bytes rtn) Responses
 * 0x80 NOP, dummy instruction (1) !
 * 0x81 xx xx yy assign loco xxxx to cab cc (1) !, 1,2
 * 0x82 read clock (2) <hours><minutes>
 * 0x83 Clock stop (1) !
 * 0x84 Clock start (1) !
 * 0x85 xx xx Set clock hr./min (1) !,3
 * 0x86 xx Set clock 12/24 (1) !,3
 * 0x87 xx Set clock ratio (1) !,3
 * 0x88 xxxx Dequeue packet by loco addr (1) !, 1,2
 * 0x89 Enable main trk, kill prog (1) !
 * 0x8A yy Return status of AIU yy (4) <current hi byte> <current lo byte> <change hi byte> <change lo byte>
 * 0x8B Kill main trk, enable prog (1) !
 * 0x8C dummy inst. returns "!" followed CR/LF(3) !, 0x0D, 0x0A
 * 0x8D xxxx mm Set speed mode of loco xxxx to mode mm, 1=14, 2=28, 3=128 (1) !, 1,3<speed mode, 0 to 3>
 * 0x8E aaaa nn<16 data bytes> Write nn bytes, start at aaaa Must have 16 data bytes, pad them out to 16 if necessary (1) !,4
 * 0x8F aaaa Read 16 bytes, start at aaaa(16) 16 bytes
 * 0x90 cc xx... Send 16 char message to Cab ccLCD line 3. xx = 16 ASCII char (1) ! ,2
 * 0x91 cc xx Send 16 char message to cab cc LCD line 4. xx=16 ASCII (1) !,2
 * 0x92 cc xx Send 8 char message to cab cc LCD line 2 right xx=8 char (1) !,2
 * 0x93 ss<3 byte packet> Queue 3 byte packet to temp _Q send ss times (1) !
 * 0x94 ss<4 byte packet> Queue 4 byte packet to temp _Q send ss times (1) !
 * 0x95 ss<5 byte packet> Queue 5 byte packet to temp_Q send ss times (1) !
 * 0x96 ss<6 byte packet> Queue 6 byte packet to temp _Q send ss times (1) !
 * 0x97 aaaa xx Write 1 byte to aaaa (1) !
 * 0x98 aaaa xx xxWrite 2 bytes to aaaa (1) !
 * 0x99 aaaa<4 data bytes> Write 4 bytes to aaaa (1) !
 * 0x9A aaaa<8 data bytes> Write 8 bytes to aaaa (1) !
 * 0x9B yy Return status of AIU yy (short form of command 0x8A) (2) <current hi byte><current lo byte><br>
 * 0x9C xx Execute macro number xx (1) !, 0,3
 * 0x9D aaaa Read 1 byte from aaaa (1) 1 byte
 * 0x9E Enter programming track mode(1) !=success 3=short circuit
 * 0x9F Exit programming track mode (1) !=success
 * 0xA0 aaaa xx Program CV aa with data xx in paged mode (1) !=success 0=program track
 * 0xA1 aaaa Read CV aaaa in paged mode Note: cv data followed by ! for OK. 0xFF followed by 3 for can't read CV (2) !, 0,3
 * 0xA2<4 data bytes> Locomotive control command (1) !,1
 * 0xA3<3 bytepacket> Queue 3 byte packet to TRK _Q (replaces any packet with same address if it exists) (1) !,1
 * 0xA4<4 byte packet> Queue 4 byte packet to TRK _Q (1) !,1
 * 0xA5<5 byte packet> Queue 5 byte packet to TRK _Q (1) !,1
 * 0xA6 rr dd Program register rr with dd (1) !=success 0=no program track
 * 0xA7 rr Read register rr. Note: cv data followed by ! for OK. 0xFF followed by 3 for can't read CV (2) !,3 0=no program track
 * 0xA8 aaaa dd Program CV aaaa with dd in direct mode. (1) !=success 0=no program track
 * 0xA9 aaaa Read CV aaaa in direct mode. Note: cv data followed by ! for OK.
 * 0xFF followed by 3 for can't read CV (2) !,3
 * 0xAA Return software revision number. Format: VV.MM.mm (3) 3 data bytes
 * 0xAB Perform soft reset of command station (like cycling power) (0) Returns nothing
 * 0xAC Perform hard reset of command station. Reset to factory defaults (Note: will change baud rate to 9600)(0) Returns nothing
 * 0xAD <4 data bytes>Accy/signal and macro commands (1) !,1
 * }</pre>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Paul Bender, Copyright (C) 2009
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class SimulatorAdapter extends NcePortController implements Runnable {

    // private control members
    private boolean opened = false;
    private Thread sourceThread;

    // streams to share with user class
    private DataOutputStream pout = null; // this is provided to classes who want to write to us
    private DataInputStream pin = null; // this is provided to classes who want data from us

    // internal ends of the pipes
    private DataOutputStream outpipe = null; // feed pin
    private DataInputStream inpipe = null; // feed pout

    // Simulator responses
    char NCE_OKAY = '!';
    char NCE_ERROR = '0';
    char NCE_LOCO_OUT_OF_RANGE = '1';
    char NCE_CAB_OUT_OF_RANGE = '2';
    char NCE_DATA_OUT_OF_RANGE = '3';
    char NCE_BYTE_OUT_OF_RANGE = '4';

    /**
     * Create a new SimulatorAdapter.
     */
    public SimulatorAdapter() {
        super(new NceSystemConnectionMemo());
    }

    /**
     * {@inheritDoc} Simulated input/output pipes.
     */
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
            log.error("init (pipe): Exception: ", e);
        }
        opened = true;
        return null; // indicates OK return
    }

    /**
     * Set up all of the other objects to simulate operation with an NCE command
     * station.
     */
    @Override
    public void configure() {
        NceTrafficController tc = new NceTrafficController();
        this.getSystemConnectionMemo().setNceTrafficController(tc);
        tc.setAdapterMemo(this.getSystemConnectionMemo());
        tc.connectPort(this);

        // setting binary mode
        this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_2006);
        tc.setCmdGroups(NceTrafficController.CMDS_MEM
                | NceTrafficController.CMDS_AUI_READ
                | NceTrafficController.CMDS_PROGTRACK
                | NceTrafficController.CMDS_OPS_PGM
                | NceTrafficController.CMDS_USB
                | NceTrafficController.CMDS_NOT_USB
                | NceTrafficController.CMDS_CLOCK
                | NceTrafficController.CMDS_ALL_SYS);
        tc.setUsbSystem(NceTrafficController.USB_SYSTEM_NONE);

        this.getSystemConnectionMemo().configureManagers();

        // start the simulator
        sourceThread = new Thread(this);
        sourceThread.setName("Nce Simulator");
        sourceThread.setPriority(Thread.MIN_PRIORITY);
        sourceThread.start();
    }

    // Base class methods for the NcePortController interface.
    /**
     * {@inheritDoc}
     */
    @Override
    public DataInputStream getInputStream() {
        if (!opened || pin == null) {
            log.error("getInputStream called before load(), stream not available");
        }
        return pin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataOutputStream getOutputStream() {
        if (!opened || pout == null) {
            log.error("getOutputStream called before load(), stream not available");
        }
        return pout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     *
     * @return null
     */
    @Override
    public String[] validBaudRates() {
        log.debug("validBaudRates should not have been invoked");
        return new String[]{};
    }

    /**
     * {@inheritDoc}
     *
     * @return null
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
    public String getCurrentPortName() {
        return "";
    }

    @Override
    public void run() { // start a new thread
        // This thread has one task.  It repeatedly reads from the input pipe
        // and writes an appropriate response to the output pipe. This is the heart
        // of the NCE command station simulation.
        // report status?
        log.info("NCE Simulator Started");
        while (true) {
            NceMessage m = readMessage();
            if (log.isDebugEnabled()) {
                StringBuilder buf = new StringBuilder();
                buf.append("Nce Simulator Thread received message: ");
                for (int i = 0; i < m.getNumDataElements(); i++) {
                    buf.append(Integer.toHexString(0xFF & m.getElement(i))).append(" ");
                }
                log.debug(buf.toString());
            }
            if (m != null) {
                NceReply r = generateReply(m);
                writeReply(r);
                if (log.isDebugEnabled() && r != null) {
                    StringBuilder buf = new StringBuilder();
                    buf.append("Nce Simulator Thread sent reply: ");
                    for (int i = 0; i < r.getNumDataElements(); i++) {
                        buf.append(Integer.toHexString(0xFF & r.getElement(i))).append(" ");
                    }
                    log.debug(buf.toString());
                }
            }
        }
    }

    // readMessage reads one incoming message from the buffer
    private NceMessage readMessage() {
        NceMessage msg = null;
        try {
            msg = loadChars();
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
    private NceMessage loadChars() throws java.io.IOException {
        int nchars;
        byte[] rcvBuffer = new byte[32];

        nchars = inpipe.read(rcvBuffer, 0, 32);
        //log.debug("new message received");
        NceMessage msg = new NceMessage(nchars);

        for (int i = 0; i < nchars; i++) {
            msg.setElement(i, rcvBuffer[i] & 0xFF);
        }
        return msg;
    }

    /**
     * This is the heart of the simulation. It translates an incoming NceMessage
     * into an outgoing NceReply.
     */
    private NceReply generateReply(NceMessage m) {
        NceReply reply = new NceReply(this.getSystemConnectionMemo().getNceTrafficController());
        int command = m.getElement(0);
        if (command < 0x80) // NOTE: NCE command station does not respond to
        {
            return null;      // command less than 0x80 (times out)
        }
        if (command > 0xBF) { // Command is out of range
            reply.setElement(0, NCE_ERROR);  // Nce command not supported
            return reply;
        }
        switch (command) {
            case NceMessage.SW_REV_CMD:  // Get Eprom revision
                reply.setElement(0, 0x06);    // Send Eprom revision 6 2 1
                reply.setElement(1, 0x02);
                reply.setElement(2, 0x01);
                break;
            case NceMessage.READ_CLOCK_CMD: // Read clock
                reply.setElement(0, 0x12);   // Return fixed time
                reply.setElement(1, 0x30);
                break;
            case NceMessage.READ_AUI4_CMD: // Read AUI 4 byte response
                reply.setElement(0, 0xFF);   // fixed data for now
                reply.setElement(1, 0xFF);   // fixed data for now
                reply.setElement(2, 0x00);   // fixed data for now
                reply.setElement(3, 0x00);   // fixed data for now
                break;
            case NceMessage.DUMMY_CMD:  // Dummy instruction
                reply.setElement(0, NCE_OKAY);  // return ! CR LF
                reply.setElement(1, 0x0D);
                reply.setElement(2, 0x0A);
                break;
            case NceMessage.READ16_CMD:  // Read 16 bytes
                readMemory(m, reply, 16);
                break;
            case NceMessage.READ_AUI2_CMD: // Read AUI 2 byte response
                reply.setElement(0, 0x00);   // fixed data for now
                reply.setElement(1, 0x00);   // fixed data for now
                break;
            case NceMessage.READ1_CMD:  // Read 1 bytes
                readMemory(m, reply, 1);
                break;
            case NceMessage.WRITE1_CMD:  // Write 1 bytes
                writeMemory(m, reply, 1, false);
                break;
            case NceMessage.WRITE2_CMD:  // Write 2 bytes
                writeMemory(m, reply, 2, false);
                break;
            case NceMessage.WRITE4_CMD:  // Write 4 bytes
                writeMemory(m, reply, 4, false);
                break;
            case NceMessage.WRITE8_CMD:  // Write 8 bytes
                writeMemory(m, reply, 8, false);
                break;
            case NceMessage.WRITE_N_CMD:  // Write n bytes
                writeMemory(m, reply, m.getElement(3), true);
                break;
            case NceMessage.SEND_ACC_SIG_MACRO_CMD:   // accessory command
                accessoryCommand(m, reply);
                break;
            case NceMessage.READ_DIR_CV_CMD:
            case NceMessage.READ_PAGED_CV_CMD:
            case NceMessage.READ_REG_CMD:
                reply.setElement(0, 123);   // dummy data
                reply.setElement(1, NCE_OKAY);  // forces succeed
                // Sample code to modify simulator response for testing purposes.
                // Uncomment and modify as desired.
//                int cvnum = (m.getElement(1) << 8) | (m.getElement(2));
//                if (cvnum == 7) {
//                    reply.setElement(0, 88);  // forces fail
//                }
//                if (cvnum == 8) {
//                    reply.setElement(0, 48);  // forces Hornby
//                }
//                if (cvnum == 159) {
//                    reply.setElement(0, 145);
//                    reply.setElement(1, NCE_DATA_OUT_OF_RANGE);  // forces fail
//                }
                break;
            default:
                reply.setElement(0, NCE_OKAY);   // Nce okay reply!
        }
        return reply;
    }

    /**
     * Write reply to output.
     *
     * @param r reply on message
     */
    private void writeReply(NceReply r) {
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

    private final byte[] turnoutMemory = new byte[256];
    private final byte[] macroMemory = new byte[256 * 20 + 16]; // and a little padding
    private final byte[] consistMemory = new byte[256 * 6 + 16]; // and a little padding

    /* Read NCE memory.  This implementation simulates reading the NCE
     * command station memory.  There are three memory blocks that are
     * supported, turnout status, macros, and consists.  The turnout status
     * memory is 256 bytes and starts at memory address 0xEC00. The macro memory
     * is 256*20 or 5120 bytes and starts at memory address 0xC800. The consist
     * memory is 256*6 or 1536 bytes and starts at memory address 0xF500.
     *
     */
    private NceReply readMemory(NceMessage m, NceReply reply, int num) {
        if (num > 16) {
            log.error("Nce read memory command was greater than 16");
            return null;
        }
        int nceMemoryAddress = getNceAddress(m);
        if (nceMemoryAddress >= NceTurnoutMonitor.CS_ACCY_MEMORY && nceMemoryAddress < NceTurnoutMonitor.CS_ACCY_MEMORY + 256) {
            log.debug("Reading turnout memory: {}", Integer.toHexString(nceMemoryAddress));
            int offset = m.getElement(2);
            for (int i = 0; i < num; i++) {
                reply.setElement(i, turnoutMemory[offset + i]);
            }
            return reply;
        }
        if (nceMemoryAddress >= NceCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM && nceMemoryAddress < NceCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM + 256 * 6) {
            log.debug("Reading consist memory: {}", Integer.toHexString(nceMemoryAddress));
            int offset = nceMemoryAddress - NceCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM;
            for (int i = 0; i < num; i++) {
                reply.setElement(i, consistMemory[offset + i]);
            }
            return reply;
        }
        if (nceMemoryAddress >= NceCmdStationMemory.CabMemorySerial.CS_MACRO_MEM && nceMemoryAddress < NceCmdStationMemory.CabMemorySerial.CS_MACRO_MEM + 256 * 20) {
            log.debug("Reading macro memory: {}", Integer.toHexString(nceMemoryAddress));
            int offset = nceMemoryAddress - NceCmdStationMemory.CabMemorySerial.CS_MACRO_MEM;
            log.debug("offset: {}", offset);
            for (int i = 0; i < num; i++) {
                reply.setElement(i, macroMemory[offset + i]);
            }
            return reply;
        }
        for (int i = 0; i < num; i++) {
            reply.setElement(i, 0x00);   // default fixed data
        }
        return reply;
    }

    private NceReply writeMemory(NceMessage m, NceReply reply, int num, boolean skipbyte) {
        if (num > 16) {
            log.error("Nce write memory command was greater than 16");
            return null;
        }
        int nceMemoryAddress = getNceAddress(m);
        int byteDataBegins = 3;
        if (skipbyte) {
            byteDataBegins++;
        }
        if (nceMemoryAddress >= NceTurnoutMonitor.CS_ACCY_MEMORY && nceMemoryAddress < NceTurnoutMonitor.CS_ACCY_MEMORY + 256) {
            log.debug("Writing turnout memory: {}", Integer.toHexString(nceMemoryAddress));
            int offset = m.getElement(2);
            for (int i = 0; i < num; i++) {
                turnoutMemory[offset + i] = (byte) m.getElement(i + byteDataBegins);
            }
        }
        if (nceMemoryAddress >= NceCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM && nceMemoryAddress < NceCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM + 256 * 6) {
            log.debug("Writing consist memory: {}", Integer.toHexString(nceMemoryAddress));
            int offset = nceMemoryAddress - NceCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM;
            for (int i = 0; i < num; i++) {
                consistMemory[offset + i] = (byte) m.getElement(i + byteDataBegins);
            }
        }
        if (nceMemoryAddress >= NceCmdStationMemory.CabMemorySerial.CS_MACRO_MEM && nceMemoryAddress < NceCmdStationMemory.CabMemorySerial.CS_MACRO_MEM + 256 * 20) {
            log.debug("Writing macro memory: {}", Integer.toHexString(nceMemoryAddress));
            int offset = nceMemoryAddress - NceCmdStationMemory.CabMemorySerial.CS_MACRO_MEM;
            log.debug("offset: {}", offset);
            for (int i = 0; i < num; i++) {
                macroMemory[offset + i] = (byte) m.getElement(i + byteDataBegins);
            }
        }
        reply.setElement(0, NCE_OKAY);   // Nce okay reply!
        return reply;
    }

    /**
     * Extract item address from a message.
     *
     * @param m received message
     * @return address from the message
     */
    private int getNceAddress(NceMessage m) {
        int addr = m.getElement(1);
        addr = addr * 256;
        addr = addr + m.getElement(2);
        return addr;
    }

    private NceReply accessoryCommand(NceMessage m, NceReply reply) {
        if (m.getElement(3) == 0x03 || m.getElement(3) == 0x04) {  // 0x03 = close, 0x04 = throw
            String operation = "close";
            if (m.getElement(3) == 0x04) {
                operation = "throw";
            }
            int nceAccessoryAddress = getNceAddress(m);
            log.debug("Accessory command {} NT {}", operation, nceAccessoryAddress);
            if (nceAccessoryAddress > 2044) {
                log.error("Turnout address greater than 2044, address: {}", nceAccessoryAddress);
                return null;
            }
            int bit = (nceAccessoryAddress - 1) & 0x07;
            int setMask = 0x01;
            for (int i = 0; i < bit; i++) {
                setMask = setMask << 1;
            }
            int clearMask = 0x0FFF - setMask;
            // log.debug("setMask: {} clearMask: {}", Integer.toHexString(setMask), Integer.toHexString(clearMask));
            int offset = (nceAccessoryAddress - 1) >> 3;
            int read = turnoutMemory[offset];
            byte write = (byte) (read & clearMask & 0xFF);

            if (operation.equals("close")) {
                write = (byte) (write + setMask); // set bit if closed
            }
            turnoutMemory[offset] = write;
            // log.debug("wrote: {}", Integer.toHexString(write));
        }
        reply.setElement(0, NCE_OKAY);   // Nce okay reply!
        return reply;
    }

    private final static Logger log = LoggerFactory.getLogger(SimulatorAdapter.class);

}

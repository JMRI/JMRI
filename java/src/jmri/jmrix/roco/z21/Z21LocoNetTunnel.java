package jmri.jmrix.roco.z21;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetMessageException;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.streamport.LnStreamPortController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface between z21 messages and an LocoNet stream.
 * <p>
 * Parts of this code are derived from the
 * jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter class.
 *
 * @author	Paul Bender Copyright (C) 2014
 */
public class Z21LocoNetTunnel implements Z21Listener, LocoNetListener , Runnable {

    LnStreamPortController lsc = null;
    private DataOutputStream pout = null; // for output to other classes
    private DataInputStream pin = null; // for input from other classes
    // internal ends of the pipes
    private DataOutputStream outpipe = null;  // feed pin
    private DataInputStream inpipe = null; // feed pout
    private Z21SystemConnectionMemo _memo = null;
    private Thread sourceThread;

    /**
     * Build a new LocoNet tunnel.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value="SC_START_IN_CTOR", justification="done at end, waits for data")
    public Z21LocoNetTunnel(Z21SystemConnectionMemo memo) {
        // save the SystemConnectionMemo.
        _memo = memo;

        // configure input and output pipes to use for
        // the communication with the LocoNet implementation.
        try {
            PipedOutputStream tempPipeI = new PipedOutputStream();
            pout = new DataOutputStream(tempPipeI);
            inpipe = new DataInputStream(new PipedInputStream(tempPipeI));
            PipedOutputStream tempPipeO = new PipedOutputStream();
            outpipe = new DataOutputStream(tempPipeO);
            pin = new DataInputStream(new PipedInputStream(tempPipeO));
        } catch (java.io.IOException e) {
            log.error("init (pipe): Exception: " + e.toString());
            return;
        }

        // start a thread to read from the input pipe.
        sourceThread = new Thread(this);
        sourceThread.setName("z21.Z21LocoNetTunnel sourceThread");
        sourceThread.setDaemon(true);
        sourceThread.start();

        // Then use those pipes as the input and output pipes for
        // a new LnStreamPortController object.
        LocoNetSystemConnectionMemo lnMemo = new LocoNetSystemConnectionMemo();
        setStreamPortController(new Z21LnStreamPortController(lnMemo,pin, pout, "None"));

        // register as a Z21Listener, so we can receive replies
        _memo.getTrafficController().addz21Listener(this);

        // start the LocoNet configuration.
        lsc.configure();
    }

    @Override
    public void run() { // start a new thread
        // this thread has one task.  It repeatedly reads from the input pipe
        // and writes modified data to the output pipe.  This is the heart
        // of the command station simulation.
        log.debug("LocoNet Tunnel Thread Started");
        for (;;) {
            LocoNetMessage m = readMessage();
            if(m != null) {
               // don't forward a null message.
               message(m);
            }
        }
    }

    /**
     * Read one incoming message from the buffer and set
     * outputBufferEmpty to true.
     */
    private LocoNetMessage readMessage() {
        LocoNetMessage msg = null;
        try {
            msg = loadChars();
        } catch (java.io.IOException|LocoNetMessageException e) {
            // should do something meaningful here.
        }
        return (msg);
    }

    /**
     * Get characters from the input source, and file a message.
     * <p>
     * Returns only when the message is complete.
     * <p>
     * Only used in the Receive thread.
     *
     * @return filled message
     * @throws IOException when presented by the input source.
     */
    private LocoNetMessage loadChars() throws java.io.IOException,LocoNetMessageException {
        int opCode;
        // start by looking for command -  skip if bit not set
        while (((opCode = (readByteProtected(inpipe) & 0xFF)) & 0x80) == 0) { // the real work is in the loop check
            log.trace("Skipping: {}", Integer.toHexString(opCode)); // NOI18N
        }
        // here opCode is OK. Create output message
        log.trace(" (RcvHandler) Start message with opcode: {}", Integer.toHexString(opCode)); // NOI18N
        LocoNetMessage msg = null;
        while (msg == null) {
           try {
              // Capture 2nd byte, always present
              int byte2 = readByteProtected(inpipe) & 0xFF;
              log.trace("Byte2: {}", Integer.toHexString(byte2)); // NOI18N
              int len = 2;
              switch ((opCode & 0x60) >> 5) {
                 case 0:
                    /* 2 byte message */
                    len = 2;
                    break;
                 case 1:
                    /* 4 byte message */
                    len = 4;
                    break;
                 case 2:
                    /* 6 byte message */

                    len = 6;
                    break;
                 case 3:
                    /* N byte message */
                    if (byte2 < 2) {
                       log.error("LocoNet message length invalid: " + byte2
                          + " opcode: " + Integer.toHexString(opCode)); // NOI18N
                    }
                    len = byte2;
                    break;
                 default:
                    log.warn("Unhandled code: {}", (opCode & 0x60) >> 5);
                 break;
              }
              msg = new LocoNetMessage(len);
              // message exists, now fill it
              msg.setOpCode(opCode);
              msg.setElement(1, byte2);
              log.trace("len: {}", len); // NOI18N
              for (int i = 2; i < len; i++) {
                 // check for message-blocking error
                 int b = readByteProtected(inpipe) & 0xFF;
                 log.trace("char {} is: {}", i, Integer.toHexString(b)); // NOI18N
                 if ((b & 0x80) != 0) {
                    log.warn("LocoNet message with opCode: " // NOI18N
                       + Integer.toHexString(opCode)
                       + " ended early. Expected length: " + len // NOI18N
                       + " seen length: " + i // NOI18N
                       + " unexpected byte: " // NOI18N
                       + Integer.toHexString(b)); // NOI18N
                    opCode = b;
                    throw new LocoNetMessageException();
                 }
                 msg.setElement(i, b);
              }
           } catch (LocoNetMessageException e) {
              // retry by destroying the existing message
              // opCode is set for the newly-started packet
              msg = null;
           }
        }
        // check parity
        if (!msg.checkParity()) {
           log.warn("Ignore LocoNet packet with bad checksum: {}", msg);
           throw new LocoNetMessageException();
        }
        // message is complete, dispatch it !!
        return msg;
    }

    /**
     * Read a single byte, protecting against various timeouts, etc.
     * <p>
     * When a port is set to have a receive timeout (via the
     * enableReceiveTimeout() method), some will return zero bytes or an
     * EOFException at the end of the timeout. In that case, the read should be
     * repeated to get the next real character.
     */
    private byte readByteProtected(DataInputStream istream) throws java.io.IOException {
        byte[] rcvBuffer = new byte[1];
        while (true) { // loop will repeat until character found
            int nchars;
            nchars = istream.read(rcvBuffer, 0, 1);
            if (nchars > 0) {
                return rcvBuffer[0];
            }
        }
    }

    // Z21Listener interface methods.

    /**
     * Member function that will be invoked by a z21Interface implementation to
     * forward a z21 message from the layout.
     *
     * @param msg The received z21 message. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    @Override
    public void reply(Z21Reply msg) {
        // This funcction forwards the payload of an LocoNet message
        // tunneled in a z21 message and forwards it to the XpressNet
        // implementation's input stream.
        if (msg.isLocoNetTunnelMessage()) {
            LocoNetMessage reply = msg.getLocoNetMessage();
            log.debug("Z21 Reply {} forwarded to XpressNet implementation as {}",
                    msg, reply);
            for (int i = 0; i < reply.getNumDataElements(); i++) {
                try {
                    outpipe.writeByte(reply.getElement(i));
                } catch (java.io.IOException ioe) {
                    log.error("Error writing XpressNet Reply to XpressNet input stream.");
                }
            }
        }
    }

    /**
     * Member function that will be invoked by a z21Interface implementation to
     * forward a z21 message sent to the layout. Normally, this function will do
     * nothing.
     *
     * @param msg The received z21 message. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    @Override
    public void message(Z21Message msg) {
        // this function does nothing.
    }

    // LocoNetListener Interface methods.

    /**
     * Member function that will be invoked by a LocoNet Interface implementation to
     * forward a LocoNet message sent to the layout. Normally, this function will
     * do nothing.
     *
     * @param msg The received LocoNet message. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    @Override
    public void message(LocoNetMessage msg) {
        // when an LocoNet message shows up here, package it in a Z21Message
        Z21Message message = new Z21Message(msg);
        log.debug("LocoNet Message {} forwarded to z21 Interface as {}",
                    msg, message);
        // and send the z21 message to the interface
        _memo.getTrafficController().sendz21Message(message, this);
    }

    /**
     * Package protected method to retrieve the stream port controller
     * associated with this tunnel.
     */
    jmri.jmrix.loconet.streamport.LnStreamPortController getStreamPortController() {
       return lsc;
    }

    /**
     * Package protected method to set the stream port controller
     * associated with this tunnel.
     */
    void setStreamPortController(LnStreamPortController x){
        lsc = x;

        // configure the XpressNet connections properties.
        lsc.getSystemConnectionMemo().setSystemPrefix("L");
        lsc.getSystemConnectionMemo().setUserName(_memo.getUserName() + "LocoNet");

    }

    @SuppressWarnings("deprecation") // Thread.stop not likely to be removed
    public void dispose(){
       if(lsc != null){
          lsc.dispose();
       }
       if(_memo != null){
          _memo.dispose();
       }
       sourceThread.stop();
       try {
          sourceThread.join();
       } catch (InterruptedException ie){
          // interrupted durrng cleanup.
       }
    }

    private final static Logger log = LoggerFactory.getLogger(Z21LocoNetTunnel.class);

}

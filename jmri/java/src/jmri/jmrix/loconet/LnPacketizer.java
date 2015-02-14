// LnPacketizer.java

package jmri.jmrix.loconet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Converts Stream-based I/O to/from LocoNet messages.  The "LocoNetInterface"
 * side sends/receives LocoNetMessage objects.  The connection to
 * a LnPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.
 *<P>
 * Messages come to this via the main GUI thread, and are forwarded back to
 * listeners in that same thread.  Reception and transmission are handled in
 * dedicated threads by RcvHandler and XmtHandler objects.  Those are internal
 * classes defined here. The thread priorities are:
 *<P><UL>
 *<LI>  RcvHandler - at highest available priority
 *<LI>  XmtHandler - down one, which is assumed to be above the GUI
 *<LI>  (everything else)
 *</UL>
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version 		$Revision$
 *
 */
public class LnPacketizer extends LnTrafficController {

    final static boolean fulldebug = false;
  
  	boolean debug = false;
  	
  	/**
  	 * true if the external hardware is not echoing messages,
  	 * so we must
  	 */
  	protected boolean echo = false;  // echo messages here, instead of in hardware
  	
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                    justification="Only used during system initialization")
    public LnPacketizer() {
    	self=this;
    	debug = log.isDebugEnabled();
   	}


    // The methods to implement the LocoNetInterface


    public boolean status() { return (ostream != null & istream != null);
    }


    /**
     * Synchronized list used as a transmit queue.
     * <P>
     * This is public to allow access from the internal class(es) when compiling with Java 1.1
     */
    public LinkedList<byte[]> xmtList = new LinkedList<byte[]>();

    /**
     * XmtHandler (a local class) object to implement the transmit thread
     */
    protected Runnable xmtHandler ;

    /**
     * RcvHandler (a local class) object to implement the receive thread
     */
    protected Runnable rcvHandler ;

    /**
     * Forward a preformatted LocoNetMessage to the actual interface.
     *
     * Checksum is computed and overwritten here, then the message
     * is converted to a byte array and queue for transmission
     * @param m Message to send; will be updated with CRC
     */
    public void sendLocoNetMessage(LocoNetMessage m) {
        // update statistics
        transmittedMsgCount++;
        
        // set the error correcting code byte(s) before transmittal
        m.setParity();

        // stream to port in single write, as that's needed by serial
        int len = m.getNumDataElements();
        byte msg[] = new byte[len];
        for (int i=0; i< len; i++)
            msg[i] = (byte) m.getElement(i);

        if (debug) log.debug("queue LocoNet packet: "+m.toString());
        // in an atomic operation, queue the request and wake the xmit thread
        try {
            synchronized(xmtHandler) {
                xmtList.addLast(msg);
                xmtHandler.notify();
            } 
        }
        catch (Exception e) {
            log.warn("passing to xmit: unexpected exception: "+e);
        }
    }

    /**
     * Implement abstract method to signal if there's a backlog
     * of information waiting to be sent.
     * @return true if busy, false if nothing waiting to send
     */
    public boolean isXmtBusy() {
        if (controller == null) return false;
        
        return (!controller.okToSend());
    }

    // methods to connect/disconnect to a source of data in a LnPortController
    // This is public to allow access from the internal class(es) when compiling with Java 1.1
    public LnPortController controller = null;

    /**
     * Make connection to existing LnPortController object.
     * @param p Port controller for connected. Save this for a later
     *              disconnect call
     */
    public void connectPort(LnPortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (controller != null)
            log.warn("connectPort: connect called while connected");
        controller = p;
    }

    /**
     * Break connection to existing LnPortController object. Once broken,
     * attempts to send via "message" member will fail.
     * @param p previously connected port
     */
    public void disconnectPort(LnPortController p) {
        istream = null;
        ostream = null;
        if (controller != p)
            log.warn("disconnectPort: disconnect called from non-connected LnPortController");
        controller = null;
    }

    // data members to hold the streams. These are public so the inner classes defined here
    // can access whem with a Java 1.1 compiler
    public DataInputStream istream = null;
    public OutputStream ostream = null;


    /**
     * Read a single byte, protecting against various timeouts, etc.
     * <P>
     * When a gnu.io port is set to have a 
     * receive timeout (via the enableReceiveTimeout() method),
     * some will return zero bytes or an EOFException at the end of the timeout.
     * In that case, the read should be repeated to get the next real character.
     * 
     */
    protected byte readByteProtected(DataInputStream istream) throws java.io.IOException {
        while (true) { // loop will repeat until character found
            int nchars;
            nchars = istream.read(rcvBuffer, 0, 1);
            if (nchars>0) return rcvBuffer[0];
        }
    }
    // Defined this way to reduce new object creation
    private byte[] rcvBuffer = new byte[1];
        
    /**
     * Handle incoming characters.  This is a permanent loop,
     * looking for input messages in character form on the
     * stream connected to the LnPortController via <code>connectPort</code>.
     * Terminates with the input stream breaking out of the try block.
     */
    @SuppressWarnings("null")
	public void run() {
        int opCode;
        while (true) {   // loop permanently, program close will exit
            try {
                // start by looking for command -  skip if bit not set
                while ( ((opCode = (readByteProtected(istream)&0xFF)) & 0x80) == 0 )  {
                    if (fulldebug && debug) log.debug("Skipping: "+Integer.toHexString(opCode));
                }
                // here opCode is OK. Create output message
                if (fulldebug)
                    log.debug("(run) Start message with opcode: "+Integer.toHexString(opCode));
                LocoNetMessage msg = null;
                while (msg == null) {
                    try {
                        // Capture 2nd byte, always present
                        int byte2 = readByteProtected(istream)&0xFF;
                        if (fulldebug)
                            log.debug("Byte2: "+Integer.toHexString(byte2));
                        // Decide length
                        switch((opCode & 0x60) >> 5)
                            {
                            case 0:     /* 2 byte message */
                                msg = new LocoNetMessage(2);
                                break;

                            case 1:     /* 4 byte message */
                                msg = new LocoNetMessage(4);
                                break;

                            case 2:     /* 6 byte message */
                                msg = new LocoNetMessage(6);
                                break;

                            case 3:     /* N byte message */
                                if (byte2<2) log.error("LocoNet message length invalid: "+byte2
                                                       +" opcode: "+Integer.toHexString(opCode));
                                msg = new LocoNetMessage(byte2);
                                break;
                            }
                        // message exists, now fill it
                        msg.setOpCode(opCode);
                        msg.setElement(1, byte2);
                        int len = msg.getNumDataElements();
                        if (fulldebug) log.debug("len: "+len);
                        for (int i = 2; i < len; i++)  {
                            // check for message-blocking error
                            int b = readByteProtected(istream)&0xFF;
                            if (fulldebug) log.debug("char "+i+" is: "+Integer.toHexString(b));
                            if ( (b&0x80) != 0) {
                                log.warn("LocoNet message with opCode: "
                                         +Integer.toHexString(opCode)
                                         +" ended early. Expected length: "+len
                                         +" seen length: "+i
                                         +" unexpected byte: "
                                         +Integer.toHexString(b));
                                opCode = b;
                                throw new LocoNetMessageException();
                            }
                            msg.setElement(i, b);
                        }
                    }
                    catch (LocoNetMessageException e) {
                        // retry by destroying the existing message
                        // opCode is set for the newly-started packet
                        msg = null;
                    }
                }
                // check parity
                if (!msg.checkParity()) {
                    log.warn("Ignore Loconet packet with bad checksum: "+msg.toString());
                    throw new LocoNetMessageException();
                }
             	// message is complete, dispatch it !!
             	{
                    final LocoNetMessage thisMsg = msg;
                    final LnPacketizer thisTC = this;
                    // return a notification via the queue to ensure end
                    Runnable r = new Runnable() {
                            LocoNetMessage msgForLater = thisMsg;
                            LnPacketizer myTC = thisTC;
                            public void run() {
                                myTC.notify(msgForLater);
                            }
                        };
                    javax.swing.SwingUtilities.invokeLater(r);
                }

             	// done with this one
            }
            catch (LocoNetMessageException e) {
                // just let it ride for now
            }
            catch (java.io.EOFException e) {
                // posted from idle port when enableReceiveTimeout used
                log.debug("EOFException, is LocoNet serial I/O using timeouts?");
                e.printStackTrace();
            }
            catch (java.io.IOException e) {
                // fired when write-end of HexFile reaches end
                log.debug("IOException, should only happen with HexFIle: "+e);
                log.info("End of file");
                disconnectPort(controller);
                return;
            }
            catch (Exception e) {
                log.warn("run: unexpected exception: "+e);
            }
        } // end of permanent loop
    }

    /**
     * Captive class to handle incoming characters.  This is a permanent loop,
     * looking for input messages in character form on the
     * stream connected to the LnPortController via <code>connectPort</code>.
     */
    class RcvHandler implements Runnable {
        /**
         * Remember the LnPacketizer object
         */
        LnPacketizer trafficController;
        public RcvHandler(LnPacketizer lt) {
            trafficController = lt;
        }

        @SuppressWarnings("null")
		public void run() {
 
            int opCode;
            while (true) {   // loop permanently, program close will exit
                try {
                    // start by looking for command -  skip if bit not set
                    while ( ((opCode = (readByteProtected(istream)&0xFF)) & 0x80) == 0 )  {
                        if (fulldebug) log.debug("Skipping: "+Integer.toHexString(opCode));
                    }
                    // here opCode is OK. Create output message
                    if (fulldebug) log.debug(" (RcvHandler) Start message with opcode: "+Integer.toHexString(opCode));
                    LocoNetMessage msg = null;
                    while (msg == null) {
                        try {
                            // Capture 2nd byte, always present
                            int byte2 = readByteProtected(istream)&0xFF;
                            if (fulldebug) log.debug("Byte2: "+Integer.toHexString(byte2));
                            // Decide length
                            switch((opCode & 0x60) >> 5) {
                            case 0:     /* 2 byte message */
                                msg = new LocoNetMessage(2);
                                break;

                            case 1:     /* 4 byte message */
                                msg = new LocoNetMessage(4);
                                break;

                            case 2:     /* 6 byte message */
                                msg = new LocoNetMessage(6);
                                break;

                            case 3:     /* N byte message */
                                if (byte2<2) log.error("LocoNet message length invalid: "+byte2
                                                       +" opcode: "+Integer.toHexString(opCode));
                                msg = new LocoNetMessage(byte2);
                                break;
                            }
                            // message exists, now fill it
                            msg.setOpCode(opCode);
                            msg.setElement(1, byte2);
                            int len = msg.getNumDataElements();
                            if (fulldebug) log.debug("len: "+len);
                            for (int i = 2; i < len; i++)  {
                                // check for message-blocking error
                                int b = readByteProtected(istream)&0xFF;
                                if (fulldebug) log.debug("char "+i+" is: "+Integer.toHexString(b));
                                if ( (b&0x80) != 0) {
                                    log.warn("LocoNet message with opCode: "
                                             +Integer.toHexString(opCode)
                                             +" ended early. Expected length: "+len
                                             +" seen length: "+i
                                             +" unexpected byte: "
                                             +Integer.toHexString(b));
                                    opCode = b;
                                    throw new LocoNetMessageException();
                                }
                                msg.setElement(i, b);
                            }
                        }
                        catch (LocoNetMessageException e) {
                            // retry by destroying the existing message
                            // opCode is set for the newly-started packet
                            msg = null;
                        }
                    }
                    // check parity
                    if (!msg.checkParity()) {
                        log.warn("Ignore Loconet packet with bad checksum: "+msg.toString());
                        throw new LocoNetMessageException();
                    }
                    // message is complete, dispatch it !!
                    {
                        if (debug) log.debug("queue message for notification: "+msg.toString());
                        final LocoNetMessage thisMsg = msg;
                        final LnPacketizer thisTC = trafficController;
                        // return a notification via the queue to ensure end
                        Runnable r = new Runnable() {
                                LocoNetMessage msgForLater = thisMsg;
                                LnPacketizer myTC = thisTC;
                                public void run() {
                                    myTC.notify(msgForLater);
                                }
                            };
                        javax.swing.SwingUtilities.invokeLater(r);
                    }

                    // done with this one
            	}
                catch (LocoNetMessageException e) {
                    // just let it ride for now
                    log.warn("run: unexpected LocoNetMessageException: "+e);
                }
                catch (java.io.EOFException e) {
                    // posted from idle port when enableReceiveTimeout used
                    if (fulldebug) log.debug("EOFException, is LocoNet serial I/O using timeouts?");
                }
                catch (java.io.IOException e) {
                    // fired when write-end of HexFile reaches end
                    if (debug) log.debug("IOException, should only happen with HexFIle: "+e);
                    log.info("End of file");
                    disconnectPort(controller);
                    return;
                }
                // normally, we don't catch the unnamed Exception, but in this
                // permanently running loop it seems wise.
                catch (Exception e) {
                    log.warn("run: unexpected Exception: "+e);
                }
            } // end of permanent loop
        }
    }

    /**
     * Captive class to handle transmission
     */
    class XmtHandler implements Runnable {
        public void run() {
            boolean debug = log.isDebugEnabled();

            while (true) {   // loop permanently
                // any input?
                try {
                    // get content; failure is a NoSuchElementException
                    if (fulldebug) log.debug("check for input");
                    byte msg[] = null;
                    synchronized (this) {
                        msg = xmtList.removeFirst();
                    }

                    // input - now send
                    try {
                        if (ostream != null) {
                            if (!controller.okToSend()) log.debug("LocoNet port not ready to receive");
                            if (debug) log.debug("start write to stream  : "+jmri.util.StringUtil.hexStringFromBytes(msg));
                            ostream.write(msg);
                            ostream.flush();
                            if (fulldebug) log.debug("end write to stream: "+jmri.util.StringUtil.hexStringFromBytes(msg));
                            messageTransmited(msg);
                        } else {
                            // no stream connected
                            log.warn("sendLocoNetMessage: no connection established");
                        }
                    }
                    catch (java.io.IOException e) {
                        log.warn("sendLocoNetMessage: IOException: "+e.toString());
                    }
                }
                catch (NoSuchElementException e) {
                    // message queue was empty, wait for input
                    if (fulldebug) log.debug("start wait");

                    new jmri.util.WaitHandler(this);  // handle synchronization, spurious wake, interruption

                    if (fulldebug) log.debug("end wait");
                }
            }
        }
    }

    /**
     * When a message is finally transmitted, forward it
     * to listeners if echoing is needed
     *
     */
     protected void messageTransmited(byte[] msg) {
        if (debug) log.debug("message transmitted");
        if (!echo) return;
        // message is queued for transmit, echo it when needed
        // return a notification via the queue to ensure end
        javax.swing.SwingUtilities.invokeLater(new Echo(this, new LocoNetMessage(msg)));
    }
    
    static class Echo implements Runnable {
        Echo(LnPacketizer t, LocoNetMessage m) {
            myTc = t;
            msgForLater = m;
        }
        LocoNetMessage msgForLater;
        LnPacketizer myTc;
       
        public void run() {
            myTc.notify(msgForLater);
        }
    }
    
    /**
     * Invoked at startup to start the threads needed here.
     */
    public void startThreads() {
        int priority = Thread.currentThread().getPriority();
        log.debug("startThreads current priority = "+priority+
                  " max available = "+Thread.MAX_PRIORITY+
                  " default = "+Thread.NORM_PRIORITY+
                  " min available = "+Thread.MIN_PRIORITY);

        // make sure that the xmt priority is no lower than the current priority
        int xmtpriority = (Thread.MAX_PRIORITY-1>priority ? Thread.MAX_PRIORITY-1 : Thread.MAX_PRIORITY);
        // start the XmtHandler in a thread of its own
        if( xmtHandler == null )
          xmtHandler = new XmtHandler();
        Thread xmtThread = new Thread(xmtHandler, "LocoNet transmit handler");
        log.debug("Xmt thread starts at priority "+xmtpriority);
        xmtThread.setDaemon(true);
        xmtThread.setPriority(Thread.MAX_PRIORITY-1);
        xmtThread.start();

        // start the RcvHandler in a thread of its own
        if( rcvHandler == null )
          rcvHandler = new RcvHandler(this) ;
        Thread rcvThread = new Thread(rcvHandler, "LocoNet receive handler");
        rcvThread.setDaemon(true);
        rcvThread.setPriority(Thread.MAX_PRIORITY);
        rcvThread.start();

    }

    static Logger log = LoggerFactory.getLogger(LnPacketizer.class.getName());
}

/* @(#)LnPacketizer.java */

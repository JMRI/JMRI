// LnOverTcpPacketizer.java

package jmri.jmrix.loconet.loconetovertcp;

import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetMessageException;

import com.sun.java.util.collections.LinkedList;
import com.sun.java.util.collections.NoSuchElementException;

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
 * @version 		$Revision: 1.2 $
 *
 */
public class LnOverTcpPacketizer extends LnPacketizer {

    public LnOverTcpPacketizer()
    {
      self=this;
      xmtHandler = new XmtHandler();
      rcvHandler = new RcvHandler(this) ;
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
        LnOverTcpPacketizer trafficController;
        public RcvHandler(LnOverTcpPacketizer lt) {
            trafficController = lt;
        }

        public void run() {
            boolean debug = log.isDebugEnabled();

            String rxLine ;
            while (true) {   // loop permanently, program close will exit
                try {
                  // start by looking for a complete line
                  rxLine = istream.readLine();
                  if (debug)
                    log.debug("Received: " + rxLine);

                    // message is complete, dispatch it !!
                }
//                catch (LocoNetMessageException e) {
//                    // just let it ride for now
//                    log.warn("run: unexpected LocoNetMessageException: "+e);
//                }
                catch (java.io.EOFException e) {
                    // posted from idle port when enableReceiveTimeout used
                    if (debug) log.debug("EOFException, is LocoNet serial I/O using timeouts?");
                }
                catch (java.io.IOException e) {
                    // fired when write-end of HexFile reaches end
                    if (debug) log.debug("IOException, should only happen with HexFIle: "+e);
                    log.info("End of file");
//                    disconnectPort(controller);
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
                    if (debug) log.debug("check for input");
                    byte msg[] = null;
                    synchronized (this) {
                        msg = (byte[])xmtList.removeFirst();
                    }

                    // input - now send
                    try {
                        if (ostream != null) {
//                            if (!controller.okToSend()) log.warn("LocoNet port not ready to receive");
                            if (debug) log.debug("start write to stream");
                            ostream.write(msg);
                            if (debug) log.debug("end write to stream");
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
                    if (debug) log.debug("start wait");
                    try {
                        synchronized(this) {
                            // Java 1.4 gets confused by "wait()" in the
                            // following line
                            ((Object)this).wait();
                        }
                    }
                    catch (java.lang.InterruptedException ei) {}
                    if (debug) log.debug("end wait");
                }
            }
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnOverTcpPacketizer.class.getName());
}

/* @(#)LnOverTcpPacketizer.java */

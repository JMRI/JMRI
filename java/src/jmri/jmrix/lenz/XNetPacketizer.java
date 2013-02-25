/**
 * XNetPacketizer.java
 */

package jmri.jmrix.lenz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from XNet messages.  The "XNetInterface"
 * side sends/receives XNetMessage objects.  The connection to
 * a XNetPortController is via a pair of *Streams, which then carry sequences
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
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version 		$Revision$
 *
 */
public class XNetPacketizer extends XNetTrafficController {

	public XNetPacketizer(LenzCommandStation pCommandStation) {
        super(pCommandStation);
        // The instance method (from XNetTrafficController) is deprecated
        // But for the moment we need to make sure we set the static
        // self variable, and hte instance method does this for us in a
        // static way (which makes findbugs happy).
        //instance();
    }


// The methods to implement the XNetInterface

	public boolean status() { return (ostream != null & istream != null);
		}
	/**
	 * Forward a preformatted XNetMessage to the actual interface.
	 *
	 * Checksum is computed and overwritten here, then the message
	 * is converted to a byte array and queue for transmission
         * @param m Message to send; will be updated with CRC
	 */
	public void sendXNetMessage(XNetMessage m, XNetListener reply) {
		if(m.length()!=0){
			sendMessage(m,reply);
                        java.lang.Thread.yield();
                }
	}


    /**
     * Add trailer to the outgoing byte stream.
     * This version adds the checksum to the last byte.
     * @param msg  The output byte stream
     * @param offset the first byte not yet used
     */
    @Override
    protected void addTrailerToOutput(byte[] msg, int offset, jmri.jmrix.AbstractMRMessage m) {
	if(m.getNumDataElements()==0) return;
	((XNetMessage)m).setParity();
        msg[offset - 1] = (byte)m.getElement(m.getNumDataElements()-1);
    }   


    /**
     * Check to see if PortController object can be sent to.
     * returns true if ready, false otherwise
     * May throw an Exception.   
     */
    @Override
    public boolean portReadyToSend(jmri.jmrix.AbstractPortController p) throws Exception {
        if (((XNetPortController)p).okToSend()) {
         ((XNetPortController)p).setOutputBufferEmpty(false);
	 return true;
        } else {
             if (log.isDebugEnabled()) log.debug ("XPressNet port not ready to receive");
		return false;
	}
     }

	static Logger log = LoggerFactory.getLogger(XNetPacketizer.class.getName());
}

/* @(#)XNetPacketizer.java */


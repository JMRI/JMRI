/**
 * XNetPacketizer.java
 */

package jmri.jmrix.lenz;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;

import com.sun.java.util.collections.LinkedList;
import com.sun.java.util.collections.NoSuchElementException;

import java.util.Vector;

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
 * @version 		$Revision: 2.1 $
 *
 */
public class XNetPacketizer extends XNetTrafficController {

	public XNetPacketizer(LenzCommandStation pCommandStation) {
        super(pCommandStation);
        self=this;
    }


// The methods to implement the XNetInterface

	public boolean status() { return (ostream != null & istream != null);
		}

	/**
	 * Synchronized list used as a transmit queue
	 */
    LinkedList xmtList = new LinkedList();

	/**
	 * Forward a preformatted XNetMessage to the actual interface.
	 *
	 * Checksum is computed and overwritten here, then the message
	 * is converted to a byte array and queue for transmission
     * @param m Message to send; will be updated with CRC
	 */
	public void sendXNetMessage(XNetMessage m, XNetListener reply) {
		// set the error correcting code byte
		int len = m.getNumDataElements();
		int chksum = 0x00;  /* the seed */
   		int loop;

    		for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
        		chksum ^= m.getElement(loop);
        	}
		m.setElement(len-1, chksum);  // checksum is last element of message
		sendMessage(m,reply);
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetPacketizer.class.getName());
}


/* @(#)XNetPacketizer.java */


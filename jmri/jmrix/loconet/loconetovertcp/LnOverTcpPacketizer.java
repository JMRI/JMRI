// LnOverTcpPacketizer.java

package jmri.jmrix.loconet.loconetovertcp;

import jmri.jmrix.loconet.LnPacketizer;

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
 * @version 		$Revision: 1.1 $
 *
 */
public class LnOverTcpPacketizer extends LnPacketizer {

    public LnOverTcpPacketizer() {self=this;}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnOverTcpPacketizer.class.getName());
}

/* @(#)LnOverTcpPacketizer.java */

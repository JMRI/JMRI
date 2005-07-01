// NceTurnout.java

package jmri.jmrix.nce;

import jmri.AbstractTurnout;
import jmri.NmraPacket;
import jmri.Turnout;

/**
 * Implement a Turnout via NCE communications.
 * <P>
 * This object doesn't listen to the NCE communications.  This is because
 * it should be the only object that is sending messages for this turnout;
 * more than one Turnout object pointing to a single device is not allowed.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 1.8 $
 */
public class NceTurnout extends AbstractTurnout {

    final String prefix = "NT";

    /**
     * NCE turnouts use the NMRA number (0-511) as their numerical identification.
     */

    public NceTurnout(int number) {
        super("NT"+number);
        _number = number;
        // At construction, register for messages
    }

    public int getNumber() { return _number; }

    // Handle a request to change state by sending a turnout command
    protected void forwardCommandChangeToLayout(int s) {
        // implementing classes will typically have a function/listener to get
        // updates from the layout, which will then call
        //		public void firePropertyChange(String propertyName,
        //										Object oldValue,
        //										Object newValue)
        // _once_ if anything has changed state (or set the commanded state directly)

        // sort out states
        if ( (s & Turnout.CLOSED) > 0) {
            // first look for the double case, which we can't handle
            if ( (s & Turnout.THROWN) > 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN "+s);
                return;
            } else {
                // send a CLOSED command
                sendMessage(true);
            }
        } else {
            // send a THROWN command
            sendMessage(false);
        }
    }

    public void dispose() {}  // no connections need to be broken

    // data members
    int _number;   // turnout number

    protected void sendMessage(boolean closed) {
        // get the packet
        byte[] bl = NmraPacket.accDecoderPkt(_number, closed);
        if (log.isDebugEnabled()) log.debug("packet: "
                                            +Integer.toHexString(0xFF & bl[0])
                                            +" "+Integer.toHexString(0xFF & bl[1])
                                            +" "+Integer.toHexString(0xFF & bl[2]));

        NceMessage m = NceMessage.sendPacketMessage(bl);

        NceTrafficController.instance().sendNceMessage(m, null);

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTurnout.class.getName());
}

/* @(#)NceTurnout.java */

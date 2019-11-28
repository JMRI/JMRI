package jmri.jmrix.mrc;

import java.util.Date;
import jmri.NmraPacket;
import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * New MRC implementation of the Turnout interface From Xpa+Modem implementation
 * of the Turnout interface.
 * <p>
 *
 * @author Paul Bender Copyright (C) 2004
 * @author Martin Wade Copyright (C) 2014
 * 
 */
public class MrcTurnout extends AbstractTurnout implements MrcTrafficListener {

    // Private data member to keep track of what turnout we control.
    int _number;
    MrcTrafficController tc = null;
    String prefix = "";

    /**
     * Mrc turnouts use any address allowed as an accessory decoder address on
     * the particular command station.
     * @param number turnout address value
     * @param tc traffic controller for connection
     * @param p system prefix for connection
     */
    public MrcTurnout(int number, MrcTrafficController tc, String p) {
        super(p + "T" + number);
        _number = number;
        if (_number < NmraPacket.accIdLowLimit || _number > NmraPacket.accIdHighLimit) {
            throw new IllegalArgumentException("Turnout value: " + _number 
                    + " not in the range " + NmraPacket.accIdLowLimit + " to " 
                    + NmraPacket.accIdHighLimit);
        }
        this.tc = tc;
        this.prefix = p + "T";
        tc.addTrafficListener(MrcInterface.TURNOUTS, this);
    }

    public int getNumber() {
        return _number;
    }

    /**
     * MRC turnouts can be inverted
     */
    @Override
    public boolean canInvert() {
        return true;
    }

    // Handle a request to change state by sending a formatted DCC packet
    @Override
    protected void forwardCommandChangeToLayout(int s) {
        // sort out states
        if ((s & Turnout.CLOSED) != 0) {
            // first look for the double case, which we can't handle
            if ((s & Turnout.THROWN) != 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN " + s); //IN18N
                return;
            } else {
                // send a CLOSED command
                forwardToCommandStation(true ^ getInverted());
            }
        } else {
            // send a THROWN command
            forwardToCommandStation(false ^ getInverted());
        }
    }

    void forwardToCommandStation(boolean state) {
        MrcMessage m = null;
        if (_number < 1000) {
            m = MrcMessage.getSwitchMsg(_number, state);
        } else {
            m = MrcMessage.getRouteMsg((_number - 1000), state);
        }
        tc.sendMrcMessage(m);
    }

    @Override
    public void notifyRcv(Date timestamp, MrcMessage m) {
        if (m.getMessageClass() != MrcInterface.TURNOUTS) {
            return;
        }
        if (m.getAccAddress() != getNumber()) {
            if (m.getElement(0) == MrcPackets.ROUTECONTROLPACKETCMD) {
                if ((m.getElement(4) + 1000) == getNumber()) {
                    if (m.getElement(6) == 0x00) {
                        newKnownState(jmri.Turnout.THROWN);
                    } else if (m.getElement(6) == 0x80) {
                        newKnownState(jmri.Turnout.CLOSED);
                    } else {
                        newKnownState(jmri.Turnout.UNKNOWN);
                    }
                }
            }
            return;
        }
        newKnownState(m.getAccState());
    }

    @Override
    public void notifyXmit(Date timestamp, MrcMessage m) {/* message(m); */

    }

    @Override
    public void notifyFailedXmit(Date timestamp, MrcMessage m) { /*message(m);*/ }

    @Override
    protected void turnoutPushbuttonLockout(boolean pushButtonLockout) {
    }

    private final static Logger log = LoggerFactory.getLogger(MrcTurnout.class);

}



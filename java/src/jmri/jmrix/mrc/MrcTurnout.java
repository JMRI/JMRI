// MrcTurnout.java

package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import jmri.implementation.AbstractTurnout;
import jmri.Turnout;

/**
 * New MRC implementation of the Turnout interface
 * From Xpa+Modem implementation of the Turnout interface.
 * <P>
 *
 * @author	Paul Bender Copyright (C) 2004
 * @author      Martin Wade  Copyright (C) 2014
 * @version	$Revision: 22821 $
 */
public class MrcTurnout extends AbstractTurnout implements MrcTrafficListener{

    // Private data member to keep track of what turnout we control.
    int _number;
    MrcTrafficController tc = null;
	String prefix = "";

    /**
     * Mrc turnouts use any address allowed as an accessory decoder address 
     * on the particular command station.
     */
    public MrcTurnout(int number, MrcTrafficController tc, String p) {
        super(p+"T"+number);
        _number = number;
        this.tc = tc;
    	this.prefix = p + "T";
        tc.addTrafficListener(MrcInterface.TURNOUTS, this);
    }
    

    public int getNumber() { return _number; }

    // Handle a request to change state by sending a formatted DCC packet
    protected void forwardCommandChangeToLayout(int s) {
        // sort out states
        if ( (s & Turnout.CLOSED) > 0) {
            // first look for the double case, which we can't handle
            if ( (s & Turnout.THROWN) > 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN "+s); //IN18N
                return;
            } else {
                // send a CLOSED command
                forwardToCommandStation(true);
            }
        } else {
            // send a THROWN command
            forwardToCommandStation(false);
        }
    }
    
    void forwardToCommandStation(boolean state){
        MrcMessage m = null;
        if(_number<1000)
            m=MrcMessage.getSwitchMsg(_number, state);
        else
            m=MrcMessage.getRouteMsg((_number-1000), state);
        tc.sendMrcMessage(m);
    }
    
    public void notifyRcv(Date timestamp, MrcMessage m) {
        if(m.getMessageClass()!=MrcInterface.TURNOUTS)
            return;
        if(m.getAccAddress()!=getNumber()){
            if(m.getElement(0)==MrcPackets.ROUTECONTROLPACKETCMD){
                if((m.getElement(4)+1000)==getNumber()){
                    if(m.getElement(6)==0x00){
                        newKnownState(jmri.Turnout.THROWN);
                    } else if (m.getElement(6)==0x80) {
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
    
    public void notifyXmit(Date timestamp, MrcMessage m) {/* message(m); */}
    public void notifyFailedXmit(Date timestamp, MrcMessage m) { /*message(m);*/ }

    protected void turnoutPushbuttonLockout(boolean pushButtonLockout) { }
    
    static Logger log = LoggerFactory.getLogger(MrcTurnout.class.getName());

}

/* @(#)MrcTurnout.java */

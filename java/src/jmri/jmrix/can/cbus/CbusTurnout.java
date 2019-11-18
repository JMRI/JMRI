package jmri.jmrix.can.cbus;

import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turnout for CBUS connections.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class CbusTurnout extends jmri.implementation.AbstractTurnout
        implements CanListener {

    CbusAddress addrThrown;   // go to thrown state
    CbusAddress addrClosed;   // go to closed state

    protected CbusTurnout(String prefix, String address, TrafficController tc) {
        super(prefix + "T" + address);
        this.tc = tc;
        init(address);

    }

    TrafficController tc;

    /**
     * Common initialization for both constructors.
     * <p>
     *
     */
    private void init(String address) {
        // build local addresses
        CbusAddress a = new CbusAddress(address);
        CbusAddress[] v = a.split();
        switch (v.length) {
            case 1:
                addrThrown = v[0];
                // need to complement here for addr 1
                // so address _must_ start with address + or -
                if (address.startsWith("+")) {
                    addrClosed = new CbusAddress("-" + address.substring(1));
                } else if (address.startsWith("-")) {
                    addrClosed = new CbusAddress("+" + address.substring(1));
                } else {
                    log.error("can't make 2nd event from systemname " + address);
                    return;
                }
                break;
            case 2:
                addrThrown = v[0];
                addrClosed = v[1];
                break;
            default:
                log.error("Can't parse CbusTurnout system name: {}", address);
                return;
        }
        // connect
        addTc(tc);
    }

    /**
     * Request an update on status by sending CBUS request message to thrown address.
     */
    @Override
    public void requestUpdateFromLayout() {
        CanMessage m;
        m = addrThrown.makeMessage(tc.getCanid());
        if (CbusOpCodes.isShortEvent(CbusMessage.getOpcode(m))) {
            m.setOpCode(CbusConstants.CBUS_ASRQ);
        }
        else {
            m.setOpCode(CbusConstants.CBUS_AREQ);
        }
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        tc.sendCanMessage(m, this);
        
        if (getFeedbackMode() == ONESENSOR || getFeedbackMode() == TWOSENSOR) {
            Sensor s1 = getFirstSensor();
            if (s1 != null) s1.requestUpdateFromLayout();
        }
        if (getFeedbackMode() == TWOSENSOR) {
            Sensor s2 = getSecondSensor();
            if (s2 != null) s2.requestUpdateFromLayout();
        }
    }
    
    /**
     * Handle a request to change state by sending CBUS events.
     *
     * @param s new state value
     */
    @Override
    protected void forwardCommandChangeToLayout(int s) {
        CanMessage m;
        if (s == Turnout.THROWN) {
            m = getAddrThrown();
            CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
            tc.sendCanMessage(m, this);
        } 
        if (s == Turnout.CLOSED) {
            m = getAddrClosed();
            CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
            tc.sendCanMessage(m, this);
        }
    }
    
    /**
     * Package method returning CanMessage for the Thrown Turnout Address
     */    
    public CanMessage getAddrThrown(){
        CanMessage m;
        if (getInverted()){
            m = addrClosed.makeMessage(tc.getCanid());
        } else {
            m = addrThrown.makeMessage(tc.getCanid());
        }
        return m;
    }
    
    /**
     * Package method returning CanMessage for the Closed Turnout Address
     */    
    public CanMessage getAddrClosed(){
        CanMessage m;
        if (getInverted()){
            m = addrThrown.makeMessage(tc.getCanid());
        } else {
            m = addrClosed.makeMessage(tc.getCanid());
        }
        return m;
    }
    
    // note we only respond to addrThrown matches, 
    // ie the left hand side of split of address "+5;+7"
    // there's a response expected from 5
    private void sendResponseToQuery(){
        CanMessage m = null;
        if (getCommandedState() == Turnout.THROWN) {
            m=getAddrThrown(); // has already had inverted status considered
            if (CbusMessage.isShort(m)) {
                if (CbusMessage.getEventType(m)==CbusConstants.EVENT_ON) {
                    m.setOpCode(CbusConstants.CBUS_ARSON);
                }
                else {
                    m.setOpCode(CbusConstants.CBUS_ARSOF);
                }
            } else {
                if (CbusMessage.getEventType(m)==CbusConstants.EVENT_ON) {
                    m.setOpCode(CbusConstants.CBUS_ARON);
                }
                else {
                    m.setOpCode(CbusConstants.CBUS_AROF);
                }
            }
        } else if (getCommandedState() == Turnout.CLOSED){
            m=getAddrThrown(); // has already had inverted status considered
            if (CbusMessage.isShort(m)) {
                if (CbusMessage.getEventType(m)==CbusConstants.EVENT_ON) {
                    m.setOpCode(CbusConstants.CBUS_ARSOF);
                }
                else {
                    m.setOpCode(CbusConstants.CBUS_ARSON);
                }
            } else {
                if (CbusMessage.getEventType(m)==CbusConstants.EVENT_ON) {
                    m.setOpCode(CbusConstants.CBUS_AROF);
                }
                else {
                    m.setOpCode(CbusConstants.CBUS_ARON);
                }
            }
        }
        if (m!=null) {
            CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
            tc.sendCanMessage(m, this);
        }
    }
    
    /**
     * {@inheritDoc}
     *
     * @see jmri.jmrix.can.CanListener#message(jmri.jmrix.can.CanMessage)
     */
    @Override
    public void message(CanMessage f) {
        if ( f.isExtended() || f.isRtr() ) {
            return;
        }
        if (addrThrown.match(f)) {
            int state = (!getInverted() ? THROWN : CLOSED);
            newCommandedState(state);
            if (_activeFeedbackType == DIRECT) {
                newKnownState(state);
            } else if (_activeFeedbackType == DELAYED) {
                newKnownState(INCONSISTENT);
                jmri.util.ThreadingUtil.runOnLayoutDelayed( () -> { newKnownState(state); },DELAYED_FEEDBACK_INTERVAL );
            }
        } else if (addrClosed.match(f)) {
            int state = (!getInverted() ? CLOSED : THROWN);
            newCommandedState(state);
            if (_activeFeedbackType == DIRECT) {
                newKnownState(state);
            } else if (_activeFeedbackType == DELAYED) {
                newKnownState(INCONSISTENT);
                jmri.util.ThreadingUtil.runOnLayoutDelayed( () -> { newKnownState(state); },DELAYED_FEEDBACK_INTERVAL );
            }
        }
    }
    
    /**
     * {@inheritDoc}
     *
     * @see jmri.jmrix.can.CanListener#reply(jmri.jmrix.can.CanReply)
     */
    @Override
    public void reply(CanReply f) {
        if ( f.isExtended() || f.isRtr() ) {
            return;
        }
        // convert response events to normal
        f = CbusMessage.opcRangeToStl(f);
        if (addrThrown.match(f)) {
            int state = (!getInverted() ? THROWN : CLOSED);
            newCommandedState(state);
            if (_activeFeedbackType == DIRECT) {
                newKnownState(state);
            } else if (_activeFeedbackType == DELAYED) {
                newKnownState(INCONSISTENT);
                jmri.util.ThreadingUtil.runOnLayoutDelayed( () -> { newKnownState(state); },DELAYED_FEEDBACK_INTERVAL );
            }
        } else if (addrClosed.match(f)) {
            int state = (!getInverted() ? CLOSED : THROWN);
            newCommandedState(state);
            if (_activeFeedbackType == DIRECT) {
                newKnownState(state);
            } else if (_activeFeedbackType == DELAYED) {
                newKnownState(INCONSISTENT);
                jmri.util.ThreadingUtil.runOnLayoutDelayed( () -> { newKnownState(state); },DELAYED_FEEDBACK_INTERVAL );
            }
        } else if (addrThrown.matchRequest(f)) {
            sendResponseToQuery();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void turnoutPushbuttonLockout(boolean locked) {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canInvert() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        tc.removeCanListener(this);
        super.dispose();
    }    
    
    
    private final static Logger log = LoggerFactory.getLogger(CbusTurnout.class);
}

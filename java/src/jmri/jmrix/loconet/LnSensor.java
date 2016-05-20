// LnSensor.java

package jmri.jmrix.loconet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.implementation.AbstractSensor;
import jmri.Sensor;

/**
 * Extend jmri.AbstractSensor for LocoNet layouts.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version         $Revision$
 */
public class LnSensor extends AbstractSensor implements LocoNetListener {

    private LnSensorAddress a;

    public LnSensor(String systemName, String userName, LnTrafficController tc, String prefix) {
        super(systemName, userName);
        this.tc = tc;
        init(systemName, prefix);
    }

    public LnSensor(String systemName, LnTrafficController tc, String prefix) {
        super(systemName);
        this.tc = tc;
        init(systemName, prefix);
    }

    LnTrafficController tc;
    
    /**
     * Common initialization for both constructors
     */
    private void init(String systemName, String prefix) {
        // store address forms
        a = new LnSensorAddress(systemName, prefix);
        if (log.isDebugEnabled()) log.debug("create address "+a);

        // At construction, register for messages
        tc.addLocoNetListener(~0, this);
    }

    /**
     * request an update on status by sending a loconet message
     */
    public void requestUpdateFromLayout() {
        // the only known way to do this from LocoNet is to request the
        // status of _all_ devices, which is here considered too
        // heavyweight.  Perhaps this is telling us we need
        // a "update all" in the SensorManager (and/or TurnoutManager)
        // interface?
    }

    /**
     * User request to set the state, which means that we broadcast that to
     * all listeners by putting it out on LocoNet.
     * In turn, the code in this class should use setOwnState to handle
     * internal sets and bean notifies.
     * @param s
     * @throws JmriException
     */
    public void setKnownState(int s) throws jmri.JmriException {
        // send OPC_INPUT_REP with new state to this address
        LocoNetMessage l = new LocoNetMessage(4);
        l.setOpCode(LnConstants.OPC_INPUT_REP);
        a.insertAddress(l);
        // set state
        if ((s==Sensor.ACTIVE)^ _inverted) {
            l.setElement(2, l.getElement(2)|0x10);
        } // otherwise is already OK
        l.setElement(2, l.getElement(2)|0x40);
        // send
        tc.sendLocoNetMessage(l);
    }

    /**
     * implementing classes will typically have a function/listener to get
     * updates from the layout, which will then call
     *      public void firePropertyChange(String propertyName,
     *      					Object oldValue,
     *                                          Object newValue)
     * _once_ if anything has changed state (or set the commanded state directly)
     * @param l
     */
    public void message(LocoNetMessage l) {
        // parse message type
        switch (l.getOpCode()) {
            case LnConstants.OPC_INPUT_REP: {               /* page 9 of Loconet PE */
                int sw1 = l.getElement(1);
                int sw2 = l.getElement(2);
                if (a.matchAddress(sw1, sw2)) {
                    // save the state
                    boolean state = ((sw2 & 0x10) != 0) ^ _inverted;
                    if (log.isDebugEnabled())
                        log.debug("INPUT_REP received with valid address, old state "
                                    +getRawState()+" new packet "+state);
                    if ( state && getRawState() != Sensor.ACTIVE) {
                        if (log.isDebugEnabled()) log.debug("Set ACTIVE");
                        setOwnState(Sensor.ACTIVE);
                    } else if ( (!state) && getRawState() != Sensor.INACTIVE) {
                        if (log.isDebugEnabled()) log.debug("Set INACTIVE");
                        setOwnState(Sensor.INACTIVE);
                    }
                }
                return;
            }
            default:
                return;
        }
        // reach here only in error
    }

    public void dispose() {
        tc.removeLocoNetListener(~0, this);
        super.dispose();
    }

    static Logger log = LoggerFactory.getLogger(LnSensor.class.getName());

}

/* @(#)LnSensor.java */

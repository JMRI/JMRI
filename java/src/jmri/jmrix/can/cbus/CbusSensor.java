package jmri.jmrix.can.cbus;

import java.beans.PropertyChangeEvent;
import jmri.Reporter;
import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusOpCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractSensor for CBUS controls.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class CbusSensor extends AbstractSensor implements CanListener {

    CbusAddress addrActive;    // go to active state
    CbusAddress addrInactive;  // go to inactive state

    public CbusSensor(String prefix, String address, TrafficController tc) {
        super(prefix + "S" + address);
        this.tc = tc;
        init(address);
    }
    
    Reporter reporter = null;
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
                addrActive = v[0];
                // need to complement here for addr 1
                // so address _must_ start with address + or -
                if (address.startsWith("+")) {
                    addrInactive = new CbusAddress("-" + address.substring(1));
                } else if (address.startsWith("-")) {
                    addrInactive = new CbusAddress("+" + address.substring(1));
                } else {
                    log.error("can't make 2nd event from systemname " + address);
                    return;
                }
                break;
            case 2:
                addrActive = v[0];
                addrInactive = v[1];
                break;
            default:
                log.error("Can't parse CbusSensor system name: " + address);
                return;
        }
        // connect
        addTc(tc);
    }

    /**
     * Request an update on status by sending CBUS request message to active address.
     */
    @Override
    public void requestUpdateFromLayout() {
        CanMessage m;
        m = addrActive.makeMessage(tc.getCanid());
        int opc = CbusMessage.getOpcode(m);
        if (CbusOpCodes.isShortEvent(opc)) {
            m.setOpCode(CbusConstants.CBUS_ASRQ);
        }
        else {
            m.setOpCode(CbusConstants.CBUS_AREQ);
        }
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        tc.sendCanMessage(m, this);
    }

    /**
     * User request to set the state, which means that we broadcast that to all
     * listeners by putting it out on CBUS. In turn, the code in this class
     * should use setOwnState to handle internal sets and bean notifies.
     * Unknown state does not send a message to CBUS but updates 
     * internal sensor state, enabling user test of Start of Day / Logix.
     *
     */
    @Override
    public void setKnownState(int s) throws jmri.JmriException {
        CanMessage m;
        if (s == Sensor.ACTIVE) {
            if (getInverted()){
                m = addrInactive.makeMessage(tc.getCanid());
                setOwnState(Sensor.ACTIVE);
            } else {
                m = addrActive.makeMessage(tc.getCanid());
                setOwnState(Sensor.ACTIVE);
            }
            CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
            tc.sendCanMessage(m, this);
        } else if (s == Sensor.INACTIVE) {
            if (getInverted()){
                m = addrActive.makeMessage(tc.getCanid());
                setOwnState(Sensor.INACTIVE);                
            } else {
                m = addrInactive.makeMessage(tc.getCanid());
                setOwnState(Sensor.INACTIVE);
            }
            CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
            tc.sendCanMessage(m, this);
        }
        if (s == Sensor.UNKNOWN){
            setOwnState(Sensor.UNKNOWN);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canInvert() {
        return true;
    }    
    
    /**
     * Package method returning CanMessage for the Active Sensor Address
     */    
    public CanMessage getAddrActive(){
        CanMessage m;
        if (getInverted()){
            m = addrInactive.makeMessage(tc.getCanid());              
        } else {
            m = addrActive.makeMessage(tc.getCanid());
        }
        return m;
    }
    
    /**
     * Package method returning CanMessage for the Inactive Sensor Address
     */    
    public CanMessage getAddrInactive(){
        CanMessage m;
        if (getInverted()){
            m = addrActive.makeMessage(tc.getCanid());              
        } else {
            m = addrInactive.makeMessage(tc.getCanid());
        }
        return m;
    }    
    
    /**
     * Track layout status from messages being sent to CAN
     *
     */
    @Override
    public void message(CanMessage f) {
        if ( f.isExtended() || f.isRtr() ) {
            return;
        }
        if (addrActive.match(f)) {
            setOwnState(!getInverted() ? Sensor.ACTIVE : Sensor.INACTIVE);
        } else if (addrInactive.match(f)) {
            setOwnState(!getInverted() ? Sensor.INACTIVE : Sensor.ACTIVE);
        }
    }

    /**
     * Event status from messages being received from CAN
     *
     */
    @Override
    public void reply(CanReply f) {
        if ( f.isExtended() || f.isRtr() ) {
            return;
        }
        // convert response events to normal
        f = CbusMessage.opcRangeToStl(f);
        if (addrActive.match(f)) {
            setOwnState(!getInverted() ? Sensor.ACTIVE : Sensor.INACTIVE);
        } else if (addrInactive.match(f)) {
            setOwnState(!getInverted() ? Sensor.INACTIVE : Sensor.ACTIVE);
        }
    }
    
    /**
     * {@inheritDoc}
     *
     * When a reporter is attached to the sensor, the sensor will go
     * active when ID tags are present ( assuming Sensor not inverted ),
     * inactive when no ID tags are present, ie all previously announced
     * ID tags have since been announced by other reporters.
     */
    @Override
    public void setReporter(Reporter er) {
        reporter = er;
        if (reporter!=null) {
            log.debug("attached to reporter",reporter);
            reporter.addPropertyChangeListener((PropertyChangeEvent e) -> {
                log.debug("Report {} property {} new value {}",reporter, e.getPropertyName(), e.getNewValue());
                if (e.getPropertyName().equals("state")) {
                    try {
                        if ( (int) e.getNewValue()==jmri.IdTag.SEEN) {
                            setKnownState(Sensor.ACTIVE); // setKnownState does any inversion
                        } else {
                            setKnownState(Sensor.INACTIVE); // setKnownState does any inversion
                        }
                    } catch (jmri.JmriException ex) {
                        log.error("Reporter {} unable to change sensor status",reporter);
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reporter getReporter() {
        return reporter;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        tc.removeCanListener(this);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(CbusSensor.class);

}

package jmri.jmrix.can.cbus;

import java.beans.PropertyChangeEvent;

import jmri.Disposable;
import jmri.JmriException;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.managers.AbstractPowerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PowerManager implementation for controlling CBUS layout power.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Andrew Crosland Copyright (C) 2009, 2021
 */
public class CbusPowerManager extends AbstractPowerManager<CanSystemConnectionMemo> implements CanListener, Disposable {

    private TrafficController tc;
    
    protected int progPower;

    public CbusPowerManager(CanSystemConnectionMemo memo) {
        super(memo);
        // connect to the TrafficManager
        tc = memo.getTrafficController();
        addTc(tc);
    }

    @Override
    public void setPower(int v) throws JmriException {
        int old = power;
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v == ON) {
            // send "Enable main track"
            tc.sendCanMessage(CbusMessage.getRequestTrackOn(tc.getCanid()), this);
        } else if (v == OFF) {
            // send "Kill main track"
            tc.sendCanMessage(CbusMessage.getRequestTrackOff(tc.getCanid()), this);
        }
        firePowerPropertyChange(old, power);
    }

    /**
     * Notification to JMRI of main track power state. Does not send to Layout.
     * Only used to bypass having the PowerManager respond to messages from the
     * command station because I don't know why the PowerManager should not do
     * the job the PowerManager API was created to do in the CBus package.
     *
     * @param newPower New Power Status
     */
    public void updatePower(int newPower) {
        int oldPower = power;
        if (oldPower != newPower) {
            power = newPower;
            firePowerPropertyChange(oldPower, power);
        }
    }

    @Override
    public boolean isProgTrackPowerSupported() {
        return memo.isProgTrackPowerIndependent();
    }
 
    /**
     * Get the current Command Station Node Number
     * 
     * Only looks for CS 0
     * @return the NN or default if no CS known
     */
    private int getCsNn() {
        int _csNN = CbusConstants.DEFAULT_CS_NN;
        CbusNodeTableDataModel cs =  jmri.InstanceManager.getNullableDefault(CbusNodeTableDataModel.class);
        if (cs != null) {
            CbusNode csnode = cs.getCsByNum(0);
            if (csnode!=null) {
                _csNN = csnode.getNodeNumber();
            }
        } else {
            log.info("Unable to fetch Master Command Station from Node Manager, using default NN {}", CbusConstants.DEFAULT_CS_NN);
        }
        return _csNN;
    }
    
    @Override
    public void setProgTrackPower(int v) throws JmriException {
        if (isProgTrackPowerSupported()) {
            int old = progPower;
            progPower = UNKNOWN; // while waiting for reply
            checkTC();
            int csNN = getCsNn();

            if (v == ON) {
                // send "Enable prog track"
                tc.sendCanMessage(CbusMessage.getRequestTrackOnEvent(tc.getCanid(), csNN, CbusConstants.PROG_TRACK), this);
            } else if (v == OFF) {
                // send "Kill prog track"
                tc.sendCanMessage(CbusMessage.getRequestTrackOffEvent(tc.getCanid(), csNN, CbusConstants.PROG_TRACK), this);
            }
            fireProgPowerPropertyChange(old, progPower);
        }
    }
 
    @Override
    public int getProgTrackPower() {
        if (isProgTrackPowerSupported()) {
            return progPower;
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Listen for changes to prog track power.
     * 
     * Main track is monitored in super class
     * @param e 
     */
    public void propertyChange(PropertyChangeEvent e) {
        if (isProgTrackPowerSupported()) {
            if (PROGPOWER.equals(e.getPropertyName())) {
                int newProgPowerState = getPower();
                if (newProgPowerState != power) {
                    power = newProgPowerState;
                }
            }
        }
    }

    /**
     * Fires a {@link java.beans.PropertyChangeEvent} for the programming track
     * power state using property name "progpower".
     *
     * @param old the old power state
     * @param current the new power state
     */
    protected final void fireProgPowerPropertyChange(int old, int current) {
        firePropertyChange(PROGPOWER, old, current);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        removeTc(tc);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use CbusPowerManager after dispose");
        }
    }

    // to listen for status changes from Cbus system
    @Override
    public void reply(CanReply m) {
        if (m.extendedOrRtr()) {
            return;
        }
        int csNN = getCsNn();
        if (CbusMessage.isTrackOffEvent(m, csNN) || CbusMessage.isTrackOnEvent(m, csNN)) {
            int old = progPower;
            if (CbusMessage.isTrackOffEvent(m, csNN)) {
                progPower = OFF;
            } else {
                progPower = OFF;
            }
            fireProgPowerPropertyChange(old, progPower);
        } else {
            int old = power;
            if (CbusMessage.isTrackOff(m)) {
                power = OFF;
            } else if (CbusMessage.isTrackOn(m)) {
                power = ON;
            } else if (CbusMessage.isArst(m)) {
                // Some CBUS command stations (e.g. CANCMD) will turn on the track
                // power at start up, before sending ARST (System reset). Others,
                // e.g., SPROG CBUS hardware can selectively turn on the track power
                // so we selectively check for ARST here, based on connection settings.
                if (memo.powerOnArst()) {
                    power = ON;
                }
            }
            firePowerPropertyChange(old, power);
        }
    }

    /**
     * Does not listen to outgoing messages. {@inheritDoc}
     */
    @Override
    public void message(CanMessage m) {
        // do nothing
    }

    private final static Logger log = LoggerFactory.getLogger(CbusPowerManager.class);
}

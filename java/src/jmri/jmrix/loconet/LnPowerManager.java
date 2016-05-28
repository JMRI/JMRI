// LnPowerManager.java
package jmri.jmrix.loconet;

import jmri.JmriException;
import jmri.PowerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PowerManager implementation for controlling layout power
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version $Revision$
 */
public class LnPowerManager
        extends jmri.managers.AbstractPowerManager
        implements PowerManager, LocoNetListener {

    public LnPowerManager(LocoNetSystemConnectionMemo memo) {
        super(memo);
        // standard LocoNet - connect
        if (memo.getLnTrafficController() == null) {
            log.error("Power Manager Created, yet there is no Traffic Controller");
            return;
        }
        this.tc = memo.getLnTrafficController();
        tc.addLocoNetListener(~0, this);

        updateTrackPowerStatus();  // this delays a while then reads slot 0 to get current track status
    }

    protected int power = UNKNOWN;

    public void setPower(int v) throws JmriException {
        power = UNKNOWN;

        checkTC();
        if (v == ON) {
            // send GPON
            LocoNetMessage l = new LocoNetMessage(2);
            l.setOpCode(LnConstants.OPC_GPON);
            tc.sendLocoNetMessage(l);
        } else if (v == OFF) {
            // send GPOFF
            LocoNetMessage l = new LocoNetMessage(2);
            l.setOpCode(LnConstants.OPC_GPOFF);
            tc.sendLocoNetMessage(l);
        }

        firePropertyChange("Power", null, null);
    }

    public int getPower() {
        return power;
    }

    // these next three public methods have been added so that other classes
    // do not need to reference the static final values "ON", "OFF", and "UKNOWN".
    public boolean isPowerOn() {
        return (power == ON);
    }

    public boolean isPowerOff() {
        return (power == OFF);
    }

    public boolean isPowerUnknown() {
        return (power == UNKNOWN);
    }

    // to free resources when no longer used
    public void dispose() {
        if (tc != null) {
            tc.removeLocoNetListener(~0, this);
        }
        tc = null;
    }

    LnTrafficController tc = null;

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("Use power manager after dispose");
        }
    }

    // to listen for status changes from LocoNet
    public void message(LocoNetMessage m) {
        if (m.getOpCode() == LnConstants.OPC_GPON) {
            power = ON;
            firePropertyChange("Power", null, null);
        } else if (m.getOpCode() == LnConstants.OPC_GPOFF) {
            power = OFF;
            firePropertyChange("Power", null, null);
        } else if (m.getOpCode() == LnConstants.OPC_SL_RD_DATA) {
            // grab the track status any time that a slot read of a "normal" slot passes thru.
            // Ignore "reserved" and "master control" slots in slot numbers 120-127
            if ((m.getElement(1) == 0x0E) && (m.getElement(2) < 120)) {
                int slotTrackStatus
                        = ((m.getElement(7) & LnConstants.GTRK_POWER) == LnConstants.GTRK_POWER) ? ON : OFF;
                if (power != slotTrackStatus) {
                    // fire a property change only if slot status is DIFFERENT 
                    // from current local status
                    power = slotTrackStatus; // update local track status from slot info
                    firePropertyChange("Power", null, null);
                }
            }
        }
    }

    /**
     * Creates a thread which delays and then queries slot 0 to get the current
     * track status. The LnListener will see the slot read data and use the
     * current track status to update the LnPowerManager's internal track power
     * state info.
     */
    private void updateTrackPowerStatus() {
        LnTrackStatusUpdateThread thread = new LnTrackStatusUpdateThread(tc);
        thread.start();
    }

    /**
     * Class providing a thread to delay then query slot 0. The LnPowerManager
     * can use the resulting OPC_SL_RD_DATA message to update its view of the
     * current track status.
     */
    static class LnTrackStatusUpdateThread extends Thread {

        private LnTrafficController tc;

        /**
         * Constructs the thread
         *
         * @param tc LocoNetTrafficController which can be used to send the
         *           LocoNet message.
         */
        public LnTrackStatusUpdateThread(LnTrafficController tc) {
            this.tc = tc;
        }

        /**
         * Runs the thread - Waits a while (to allow the managers to initialize)
         * then sends a query of slot 0 so that the power manager can inspect
         * the {@code "<trk>"} byte.
         */
        public void run() {
            // wait a little bit to allow power manager to be initialized
            try {
                // Delay 200 mSec to allow init of traffic controller, listeners.
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
            }
            LocoNetMessage msg = new LocoNetMessage(4);
            msg.setOpCode(LnConstants.OPC_RQ_SL_DATA);
            msg.setElement(1, 0);
            msg.setElement(2, 0);
            tc.sendLocoNetMessage(msg);
        }
    }
    private final static Logger log = LoggerFactory.getLogger(LnPowerManager.class.getName());
}

/* @(#)LnPowerManager.java */

package jmri.jmrix.loconet.pr2;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LnOpsModeProgrammer;
import jmri.jmrix.loconet.LnPowerManager;
import jmri.jmrix.loconet.LnPr2ThrottleManager;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.SlotManager;

/**
 * PowerManager implementation for controlling layout power via PR2.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class LnPr2PowerManager extends LnPowerManager {

    public LnPr2PowerManager(LocoNetSystemConnectionMemo memo) {
        super(memo);
        this.tc = memo.getLnTrafficController();
        this.memo = memo;
    }

    LnTrafficController tc;
    LocoNetSystemConnectionMemo memo;

    @Override
    public void setPower(int v) throws JmriException {
        power = UNKNOWN;

        // Instead of GPON/GPOFF, PR2 uses ops-mode writes to CV 128 for control
        if (v == ON) {
            // get current active address
            DccLocoAddress activeAddress = ((LnPr2ThrottleManager) InstanceManager.throttleManagerInstance()).getActiveAddress();
            if (activeAddress != null) {
                pm = new LnOpsModeProgrammer(memo, activeAddress.getNumber(), activeAddress.isLongAddress());
                checkOpsProg();

                // set bit 1 in CV 128
                pm.writeCV("128", 1, null);
                power = ON;
                firePropertyChange("Power", null, null); // NOI18N
                // start making sure that the power is refreshed
                if (timer == null) {
                    timer = new javax.swing.Timer(2 * 1000, new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            refresh();
                        }
                    });
                    timer.setInitialDelay(2 * 1000);
                    timer.setRepeats(true);     // in case we run by
                }
                timer.start();
            }
        } else if (v == OFF) {
            if (timer != null) {
                timer.stop();
            }

            // get current active address
            DccLocoAddress activeAddress = ((LnPr2ThrottleManager) InstanceManager.throttleManagerInstance()).getActiveAddress();
            if (activeAddress != null) {
                pm = new LnOpsModeProgrammer(memo, activeAddress.getNumber(), activeAddress.isLongAddress());
                checkOpsProg();

                // reset bit 1 in CV 128
                pm.writeCV("128", 0, null);
                power = OFF;
            }
        }
        // notify of change
        firePropertyChange("Power", null, null); // NOI18N
    }

    void refresh() {
        // send inquiry message to keep power alive
        LocoNetMessage msg = new LocoNetMessage(2);
        msg.setOpCode(LnConstants.OPC_GPBUSY);
        tc.sendLocoNetMessage(msg);
    }

    LnOpsModeProgrammer pm = null;

    private void checkOpsProg() throws JmriException {
        if (pm == null) {
            throw new JmriException("Use PR2 power manager after dispose"); // NOI18N
        }
    }

    // to listen for status changes from LocoNet
    @Override
    public void message(LocoNetMessage m) {
        if (m.getOpCode() == LnConstants.OPC_GPON) {
            power = ON;
            firePropertyChange("Power", null, null); // NOI18N
        } else if (m.getOpCode() == LnConstants.OPC_GPOFF) {
            power = OFF;
            if (timer != null) {
                // Protect against uninitialized timer, for case where some other
                // LocoNet agent issues OPC_GPOFF before JMRI initializes its timer.
                // A NPE was seen, before protected added, with the DCS52.
                timer.stop();
            }
            firePropertyChange("Power", null, null); // NOI18N
        } else if (m.getOpCode() == LnConstants.OPC_WR_SL_DATA) {
            // if this is a service mode write, drop out of power on mode
            if ((m.getElement(1) == 0x0E)
                    && (m.getElement(2) == 0x7C)
                    && ((m.getElement(3) & 0x04) == 0x00)) {
                // go to power off due to service mode op
                if (power == ON) {
                    power = OFF;
                    if (timer != null) {
                        timer.stop();
                    }
                    firePropertyChange("Power", null, null); // NOI18N
                }
            }
        } else if ( // check for status showing going off
                (m.getOpCode() == LnConstants.OPC_PEER_XFER)
                && (m.getElement(1) == 0x10)
                && (m.getElement(2) == 0x22)
                && (m.getElement(3) == 0x22)
                && (m.getElement(4) == 0x01)) {  // PR2 form
            int[] data = m.getPeerXfrData();
            if ((data[2] & 0x40) != 0x40) {
                // dropped off
                if (power == ON) {
                    power = OFF;
                    if (timer != null) {
                        timer.stop();
                    }
                    firePropertyChange("Power", null, null); // NOI18N
                }
            }
        }
    }
    
    /**
     * Returns false to indicate PR2 does not implement an "IDLE" power state.
     * @return false
     */
    @Override
    public boolean implementsIdle() {
        return false;
    }

    // timer support to send updates & keep power alive
    javax.swing.Timer timer = null;
}


package jmri.jmrix.zimo;

import static jmri.jmrix.zimo.Mx1Message.ACKREP1;

import jmri.JmriException;
import jmri.managers.AbstractPowerManager;

/**
 * PowerManager implementation for controlling layout power.
 *
 * @author Bob Jacobsen Copyright (C) 2001
  *
 * Adapted by Sip Bosch for use with zimo Mx-1
 *
 */
public class Mx1PowerManager extends AbstractPowerManager<Mx1SystemConnectionMemo> implements Mx1Listener {

    Mx1TrafficController tc = null;

    public Mx1PowerManager(Mx1SystemConnectionMemo memo) {
        super(memo);
        // connect to the TrafficManager
        this.tc = memo.getMx1TrafficController();
        tc.addMx1Listener(~0, this);
    }

    @Override
    public void setPower(int v) throws JmriException {
        int old = power;
        power = UNKNOWN;
        if (tc.getProtocol() == Mx1Packetizer.ASCII) {
            checkTC();
            if (v == ON) {
                // send GPON
                Mx1Message m = new Mx1Message(3);
                m.setElement(0, 0x53);
                m.setElement(1, 0x45);
                tc.sendMx1Message(m, this);
            } else if (v == OFF) {
                // send GPOFF
                Mx1Message m = new Mx1Message(3);
                m.setElement(0, 0x53);
                m.setElement(1, 0x41);
                tc.sendMx1Message(m, this);
            }
            // request status
            Mx1Message m = new Mx1Message(2);
            m.setElement(0, 0x5A);
            tc.sendMx1Message(m, this);
        } else {
            if (v == ON) {
                tc.sendMx1Message(Mx1Message.setPowerOn(), this);
            } else if (v == OFF) {
                tc.sendMx1Message(Mx1Message.setPowerOff(), this);
            }
            if (memo.getConnectionType() == Mx1SystemConnectionMemo.MXULF) {
                //MXULF doesn't return the correct status of the track power, so we have to assume it has been set                
                power = v;
            } else {
                tc.sendMx1Message(Mx1Message.getTrackStatus(), this);
            }
        }
        firePowerPropertyChange(old, power);
    }

    // to free resources when no longer used
    @Override
    public void dispose() throws JmriException {
        tc.removeMx1Listener(~0, this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use PowerManager after dispose");
        }
    }

    // to listen for status changes from net
    @Override
    public void message(Mx1Message m) {
        if (tc.getProtocol() == Mx1Packetizer.ASCII) {
            int old = power;
            if (m.getElement(0) == 0x5a) {
                if ((m.getElement(2) & 0x02) == 0x02) {
                    power = ON;
                } else {
                    power = OFF;
                }
            } else if (m.getMessageType() == ACKREP1 && m.getPrimaryMessage() == Mx1Message.TRACKCTL) {
                if ((m.getElement(4) & 0x02) == 0x02) {
                    power = OFF;
                } else {
                    power = ON;
                }
            }
            firePowerPropertyChange(old, power);
        }
    }

}




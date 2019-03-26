package jmri.jmrix.zimo;

import static jmri.jmrix.zimo.Mx1Message.ACKREP1;

import java.beans.PropertyChangeListener;

import jmri.JmriException;
import jmri.PowerManager;

/**
 * PowerManager implementation for controlling layout power.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
  *
 * Adapted by Sip Bosch for use with zimo Mx-1
 *
 */
public class Mx1PowerManager implements PowerManager, Mx1Listener {

    public Mx1PowerManager(Mx1SystemConnectionMemo memo) {
        // connect to the TrafficManager
        this.tc = memo.getMx1TrafficController();
        tc.addMx1Listener(~0, this);
        this.memo = memo;
    }

    @Override
    public String getUserName() {
        return "Mx1";
    }
    Mx1SystemConnectionMemo memo;
    int power = UNKNOWN;

    @Override
    public void setPower(int v) throws JmriException {
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
        firePropertyChange("Power", null, null);
    }

    @Override
    public int getPower() {
        return power;
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

    // to hear of changes
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    @Override
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    @Override
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /** {@inheritDoc} */
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    /** {@inheritDoc} */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    /** {@inheritDoc} */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    /** {@inheritDoc} */
    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    Mx1TrafficController tc = null;

    // to listen for status changes from net
    @Override
    public void message(Mx1Message m) {
        if (tc.getProtocol() == Mx1Packetizer.ASCII) {
            if (m.getElement(0) == 0x5a) {
                if ((m.getElement(2) & 0x02) == 0x02) {
                    power = ON;
                    firePropertyChange("Power", null, null);
                } else {
                    power = OFF;
                    firePropertyChange("Power", null, null);
                }
            } else {
                if (m.getMessageType() == ACKREP1 && m.getPrimaryMessage() == Mx1Message.TRACKCTL) {
                    if ((m.getElement(4) & 0x02) == 0x02) {
                        power = OFF;
                        firePropertyChange("Power", null, null);
                    } else {
                        power = ON;
                        firePropertyChange("Power", null, null);
                    }

                }

            }

        }
    }

}




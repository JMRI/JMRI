package jmri.jmrix.marklin;

import jmri.JmriException;
import jmri.PowerManager;

import java.beans.PropertyChangeListener;
import jmri.managers.AbstractPowerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PowerManager implementation for controlling layout power.
 *
 * @author Kevin Dickerson (C) 2012
 */
public class MarklinPowerManager extends AbstractPowerManager<MarklinSystemConnectionMemo> implements MarklinListener {

    MarklinTrafficController tc;

    public MarklinPowerManager(MarklinTrafficController etc) {
        super(etc.adaptermemo);
        // connect to the TrafficManager
        tc = etc;
        tc.addMarklinListener(this);

    }

    @Override
    public void setPower(int v) throws JmriException {
        int old = power;
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v == ON) {
            // send message to turn on
            MarklinMessage l = MarklinMessage.getEnableMain();
            tc.sendMarklinMessage(l, this);
        } else if (v == OFF) {
            // send message to turn off
            MarklinMessage l = MarklinMessage.getKillMain();
            tc.sendMarklinMessage(l, this);
        }
        firePowerPropertyChange(old, power);
    }

    // to free resources when no longer used
    @Override
    public void dispose() throws JmriException {
        tc.removeMarklinListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use MarklinPowerManager after dispose");
        }
    }

    // to listen for status changes from Marklin system
    @Override
    public void reply(MarklinReply m) {
        int old = power;
        // power message?
        if (m.getPriority() == MarklinConstants.PRIO_1 && m.getCommand() == MarklinConstants.SYSCOMMANDSTART && m.getAddress() == 0x0000) {
            switch (m.getElement(9)) {
                case MarklinConstants.CMDGOSYS:
                    power = ON;
                    break;
                case MarklinConstants.CMDSTOPSYS:
                case MarklinConstants.CMDHALTSYS:
                    power = OFF;
                    break;
                default:
                    log.warn("Unknown sub command {}", m.getElement(9));
            }
            firePowerPropertyChange(old, power);
        }
    }

    @Override
    public void message(MarklinMessage m) {
        // messages are ignored
    }

    private final static Logger log = LoggerFactory.getLogger(MarklinPowerManager.class);

}

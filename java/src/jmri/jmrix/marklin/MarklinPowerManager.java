package jmri.jmrix.marklin;

import jmri.JmriException;
import jmri.jmrix.marklin.mcan.classify.Cs2ClassifiedFrame;
import jmri.jmrix.marklin.mcan.cs2.Cs2SystemCommands;
import jmri.managers.AbstractPowerManager;

/**
 * PowerManager implementation for controlling layout power.
 *
 * @author Kevin Dickerson (C) 2012
 */
public class MarklinPowerManager extends AbstractPowerManager<MarklinSystemConnectionMemo> implements MarklinListener {

    private MarklinTrafficController tc;

    public MarklinPowerManager(MarklinTrafficController etc) {
        super(etc.adaptermemo);
        // connect to the TrafficManager
        tc = etc;
        tc.addMarklinListener(MarklinPowerManager.this);

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
        Cs2ClassifiedFrame frame = MarklinCanBridge.parse(m);
        if (frame == null) {
            return;
        }
        if (frame instanceof Cs2SystemCommands.SystemGoFrame) {
            power = ON;
            firePowerPropertyChange(old, power);
        } else if (frame instanceof Cs2SystemCommands.SystemStopFrame
                || frame instanceof Cs2SystemCommands.SystemHaltFrame) {
            power = OFF;
            firePowerPropertyChange(old, power);
        }
    }

    @Override
    public void message(MarklinMessage m) {
        // messages are ignored
    }

}

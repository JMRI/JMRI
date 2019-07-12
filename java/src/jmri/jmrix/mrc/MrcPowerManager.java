package jmri.jmrix.mrc;

import java.util.Date;
import jmri.JmriException;
import jmri.PowerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PowerManager implementation for controlling layout power
 * <p>
 * Some of the message formats used in this class are Copyright MRC, Inc. and
 * used with permission as part of the JMRI project. That permission does not
 * extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Mrc Inc
 * for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * 
 */
public class MrcPowerManager
        extends jmri.managers.AbstractPowerManager
        implements MrcTrafficListener {

    public MrcPowerManager(MrcSystemConnectionMemo memo) {
        super(memo);
        // standard Mrc - connect
        if (memo.getMrcTrafficController() == null) {
            log.error("PowerManager Created, yet there is no Traffic Controller");
            return;
        }
        this.tc = memo.getMrcTrafficController();
        tc.addTrafficListener(MrcInterface.POWER, this);

    }

    protected int power = UNKNOWN;

    @Override
    public void setPower(int v) throws JmriException {
        //power = UNKNOWN;
        int old = power;

        checkTC();
        if (v == ON) {
            MrcMessage l = MrcMessage.setPowerOn();
            tc.sendMrcMessage(l);
        } else if (v == OFF) {
            MrcMessage l = MrcMessage.setPowerOff();
            tc.sendMrcMessage(l);
        }
        power = v;
        firePropertyChange("Power", old, power); //IN18N
    }

    @Override
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
    @Override
    public void dispose() {
        if (tc != null) {
            tc.removeTrafficListener(MrcInterface.POWER, this);
        }
        tc = null;
    }

    MrcTrafficController tc = null;

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("Use power manager after dispose"); //IN18N
        }
    }

    @Override
    public void notifyRcv(Date timestamp, MrcMessage m) { /*message(m);*/ }

    @Override
    public void notifyXmit(Date timestamp, MrcMessage m) {/* message(m);*/ }

    @Override
    public void notifyFailedXmit(Date timestamp, MrcMessage m) { /*message(m);*/ }

    private final static Logger log = LoggerFactory.getLogger(MrcPowerManager.class);
}



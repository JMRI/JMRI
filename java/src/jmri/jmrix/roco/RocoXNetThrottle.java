package jmri.jmrix.roco;

import jmri.LocoAddress;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to a Roco XpressNet
 * connection.
 *
 * @author Paul Bender (C) 2015
 * @author Giorgio Terdina (C) 2007
 */
public class RocoXNetThrottle extends jmri.jmrix.lenz.XNetThrottle {

    /**
     * Constructor
     */
    public RocoXNetThrottle(XNetSystemConnectionMemo memo, XNetTrafficController controller) {
        super(memo,controller);
    }

    /**
     * Constructor
     */
    public RocoXNetThrottle(XNetSystemConnectionMemo memo, LocoAddress address, XNetTrafficController controller) {
        super(memo,address,controller);
    }

    // The Roco doesn't support setting the momentary/continuous status of
    // functions, so override the sending of all momentary/continuous 
    // from the parent class.
    @Override
    protected void sendMomentaryFunctionGroup1() {
        log.debug("Command station does not support Momentary functions");
    }

    @Override
    protected void sendMomentaryFunctionGroup2() {
        log.debug("Command station does not support Momentary functions");
    }

    @Override
    protected void sendMomentaryFunctionGroup3() {
        log.debug("Command station does not support Momentary functions");
    }

    @Override
    protected void sendMomentaryFunctionGroup4() {
        log.debug("Command station does not support Momentary functions");
    }

    @Override
    protected void sendMomentaryFunctionGroup5() {
        log.debug("Command station does not support Momentary functions");
    }

    // also prevent requesting the momentary status information
    @Override
    synchronized protected void sendFunctionStatusInformationRequest() {
        log.debug("Command station does not support Momentary functions");
    }

    @Override
    synchronized protected void sendFunctionHighMomentaryStatusRequest() {
        log.debug("Command station does not support Momentary functions");
    }

    // The Roco Doesn't support the XpressNet directed emergency stop
    // instruction, so override sendEmergencyStop in the parent, and
    // just send speed step 0.
    @Override
    protected void sendEmergencyStop(){
       setSpeedSetting(0);
    }

    /**
     * Dispose when finished with this object. After this, further usage of this
     * Throttle object will result in a JmriException.
     * <p>
     * This is quite problematic, because a using object doesn't know when it's
     * the last user.
     */
    @Override
    protected void throttleDispose() {
        active = false;
        stopStatusTimer();
        finishRecord();
    }
 
    // register for notification
    private final static Logger log = LoggerFactory.getLogger(RocoXNetThrottle.class);

}

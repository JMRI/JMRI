package jmri.jmrix.dccpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to current meter from the DCC++ Base Station
 *
 * @author Mark Underwood (C) 2015
 */
public class DCCppMultiMeter extends jmri.implementation.AbstractMultiMeter implements DCCppListener {

    private DCCppTrafficController tc = null;

    public DCCppMultiMeter(DCCppSystemConnectionMemo memo) {
        super(DCCppConstants.METER_INTERVAL_MS);
        tc = memo.getDCCppTrafficController();

        // TODO: For now this is OK since the traffic controller
        // ignores filters and sends out all updates, but
        // at some point this will have to be customized.
        tc.addDCCppListener(DCCppInterface.THROTTLE, this);

        //is_enabled = false;
        initTimer();

        log.debug("DCCppMultiMeter constructor called");

    }

    public void setDCCppTrafficController(DCCppTrafficController controller) {
        tc = controller;
    }

    @Override
    public void message(DCCppReply r) {
        log.debug("DCCppMultiMeter received reply: {}", r.toString());
        if (r.isCurrentReply()) {
            setCurrent(((r.getCurrentInt() * 1.0f) / (DCCppConstants.MAX_CURRENT * 1.0f)) * 100.0f );  // return as percentage.
        }

    }

    @Override
    public void message(DCCppMessage m) {
    }

    @Override
    protected void requestUpdateFromLayout() {
        tc.sendDCCppMessage(DCCppMessage.makeReadTrackCurrentMsg(), this);
    }

    @Override
    public void initializeHardwareMeter() {
        // Connect to the hardware.
    }

    @Override
    // Handle a timeout notification
    public String getHardwareMeterName() {
        return ("DCC++");
    }

    @Override
    public boolean hasCurrent() {
        return true;
    }

    @Override
    public boolean hasVoltage() {
        return false;
    }

    @Override
    public CurrentUnits getCurrentUnits() {
        return  CurrentUnits.CURRENT_UNITS_PERCENTAGE;
    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        log.debug("Notified of timeout on message {}, {} retries available.", msg.toString(), msg.getRetries());
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppMultiMeter.class);

}

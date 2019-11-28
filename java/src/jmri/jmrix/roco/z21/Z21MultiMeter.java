package jmri.jmrix.roco.z21;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to voltage and current readings from the Roco Z21 
 *
 * @author Mark Underwood (C) 2015
 * @author Paul Bender (C) 2017
 */
public class Z21MultiMeter extends jmri.implementation.AbstractMultiMeter implements Z21Listener {

    private Z21TrafficController tc = null;
    private Z21SystemConnectionMemo _memo = null;
    private boolean enabled = false;  // disable by default; prevent polling when not being used.

    public Z21MultiMeter(Z21SystemConnectionMemo memo) {
        super(-1); // no timer, since we already poll for this information. 
        _memo = memo;
        tc = _memo.getTrafficController();

        tc.addz21Listener(this);

        log.debug("Z21MultiMeter constructor called");

    }

    public void setZ21TrafficController(Z21TrafficController controller) {
        tc = controller;
    }

    @Override 
    public void enable(){
        enabled = true;
        RocoZ21CommandStation cs = _memo.getRocoZ21CommandStation();
        cs.setSystemStatusMessagesFlag(true);
        tc.sendz21Message(Z21Message.getLanSetBroadcastFlagsRequestMessage(cs.getZ21BroadcastFlags()),this);
    }

    @Override 
    public void disable(){
        enabled = false;
        RocoZ21CommandStation cs = _memo.getRocoZ21CommandStation();
        cs.setSystemStatusMessagesFlag(false);
        tc.sendz21Message(Z21Message.getLanSetBroadcastFlagsRequestMessage(cs.getZ21BroadcastFlags()),this);
    }

    @Override
    public void message(Z21Message m) {
    }

    @Override
    public void reply(Z21Reply r) {
        log.debug("Z21MultiMeter received reply: {}", r.toString());
        if (r.isSystemDataChangedReply()) {
            setCurrent(r.getSystemDataMainCurrent() * 1.0f);
            setVoltage(r.getSystemDataVCCVoltage() * 1.0f);
        }

    }

    @Override
    protected void requestUpdateFromLayout() {
        if( enabled ) {
            tc.sendz21Message(Z21Message.getLanSystemStateDataChangedRequestMessage(), this);
        }
    }

    @Override
    public void initializeHardwareMeter() {
    }

    @Override
    // Handle a timeout notification
    public String getHardwareMeterName() {
        return (_memo.getUserName());
    }

    @Override
    public boolean hasCurrent() {
        return true;
    }

    @Override
    public boolean hasVoltage() {
        return true;
    }

    @Override
    public CurrentUnits getCurrentUnits() {
        return  CurrentUnits.CURRENT_UNITS_MILLIAMPS;
    }

    private final static Logger log = LoggerFactory.getLogger(Z21MultiMeter.class);

}

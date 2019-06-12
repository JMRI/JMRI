package jmri.jmrix.loconet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to current meter from the Digitrax Evolution Base Station
 *
 */
public class LnMultiMeter extends jmri.implementation.AbstractMultiMeter implements LocoNetListener {

    private SlotManager sm = null;
    private LnTrafficController tc = null;

    /**
     * Create a ClockControl object for a LocoNet clock
     *
     * @param scm  connection memo
     */
    public LnMultiMeter(LocoNetSystemConnectionMemo scm) {
        super(LnConstants.METER_INTERVAL_MS);
        this.sm = scm.getSlotManager();
        this.tc = scm.getLnTrafficController();
        tc.addLocoNetListener(~0, this);

        initTimer();
    }

    @Override
    public void message(LocoNetMessage msg) {
        if (msg.getOpCode() != LnConstants.OPC_EXP_RD_SL_DATA || msg.getElement(1) != 21 || msg.getElement(2) == 249) {
            return;
        }
        log.debug("Found slot 249");
        // CS Types supported
        switch (msg.getElement(16)) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_DCS240:
            case LnConstants.RE_IPL_DIGITRAX_HOST_DCS210:
            case LnConstants.RE_IPL_DIGITRAX_HOST_DCS52:
                log.debug("Found Evolution CS Amps[{}] Max[{}]",msg.getElement(6) / 10.0f, (msg.getElement(7) / 10.0f));
                setCurrent((msg.getElement(6) / 10.0f));   // return amps
                setVoltage((msg.getElement(4)) * 2.0f / 10.0f);   // return volts
                break;
            default:
                // do nothing
        }
    }

    @Override
    protected void requestUpdateFromLayout() {
        sm.sendReadSlot(249);
    }

    @Override
    public void initializeHardwareMeter() {
        // Connect to the hardware.
    }

    @Override
    // Handle a timeout notification
    public String getHardwareMeterName() {
        return ("LocoNet");
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
        return  CurrentUnits.CURRENT_UNITS_AMPS;
    }

    private final static Logger log = LoggerFactory.getLogger(LnMultiMeter.class);

}

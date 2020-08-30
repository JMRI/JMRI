package jmri.jmrix.loconet;

import jmri.*;
import jmri.implementation.DefaultMeter;
import jmri.implementation.MeterUpdateTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to current meter from the Digitrax Evolution Base Station
 *
 */
// public class LnMeterGroup {
// public class LnMeterGroup extends jmri.implementation.DefaultMeterGroup {
public class LnMeterGroup extends jmri.implementation.DefaultMeterGroup implements LocoNetListener {

    private SlotManager sm = null;
    private LnTrafficController tc = null;
    private final MeterUpdateTask updateTask;
    private final Meter currentMeter;
    private final Meter voltageMeter;

    /**
     * Create a ClockControl object for a LocoNet clock
     *
     * @param scm  connection memo
     */
    public LnMeterGroup(LocoNetSystemConnectionMemo scm) {
        super("LVCommandStation");
//        super(LnConstants.METER_INTERVAL_MS);
        this.sm = scm.getSlotManager();
        this.tc = scm.getLnTrafficController();
        
        updateTask = new MeterUpdateTask(10000, 100) {
            @Override
            public void requestUpdateFromLayout() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        
        currentMeter = new DefaultMeter("LVCommandStationCurrent", Meter.Unit.NoPrefix, 0, 5.0, 0.1, updateTask);
        voltageMeter = new DefaultMeter("LVCommandStationVoltage", Meter.Unit.NoPrefix, 0, 30.0, 0.5, updateTask);
        
        InstanceManager.getDefault(MeterManager.class).register(currentMeter);
        
        addMeter(MeterGroup.CurrentMeter, MeterGroup.CurrentMeterDescr, currentMeter);
        addMeter(MeterGroup.VoltageMeter, MeterGroup.VoltageMeterDescr, voltageMeter);
        
        tc.addLocoNetListener(~0, this);

        updateTask.initTimer();
    }

    @Override
    public void message(LocoNetMessage msg) {
        try {
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
        } catch (JmriException e) {
            log.error("exception thrown by setCurrent or setVoltage", e);
        }
    }

    private void setCurrent(double value) throws JmriException {
        MeterInfo mi = getMeterByName(MeterGroup.CurrentMeter);
        if (mi != null) {
            mi.getMeter().setCommandedAnalogValue(value);
        } else {
            log.error("The current meter does not exists");
        }
    }

    private void setVoltage(double value) throws JmriException {
        MeterInfo mi = getMeterByName(MeterGroup.VoltageMeter);
        if (mi != null) {
            mi.getMeter().setCommandedAnalogValue(value);
        } else {
            log.error("The voltage meter does not exists");
        }
    }

    @Override
    public void requestUpdateFromLayout() {
        sm.sendReadSlot(249);
    }
/*
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
*/
    private final static Logger log = LoggerFactory.getLogger(LnMeterGroup.class);

}

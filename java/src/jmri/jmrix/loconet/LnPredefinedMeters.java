package jmri.jmrix.loconet;

import jmri.*;
import jmri.implementation.DefaultMeter;
import jmri.implementation.MeterUpdateTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to current and voltage meter from some LocoNet command stations
 *
 * @author Steve G           Copyright (C) 2019
 * @author Bob Jacobsen      Copyright (C) 2019
 * @author Egbert Boerse     Copyright (C) 2019
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class LnPredefinedMeters implements LocoNetListener {

    private SlotManager sm = null;
    private LnTrafficController tc = null;
    private final MeterUpdateTask updateTask;
    private final Meter currentMeter;
    private final Meter voltageMeter;

    /**
     * Create a LnPredefinedMeters object
     *
     * @param scm  connection memo
     */
    public LnPredefinedMeters(LocoNetSystemConnectionMemo scm) {
        
        this.sm = scm.getSlotManager();
        this.tc = scm.getLnTrafficController();
        
        updateTask = new MeterUpdateTask(LnConstants.METER_INTERVAL_MS) {
            @Override
            public void requestUpdateFromLayout() {
                sm.sendReadSlot(249);
            }
        };
        
        currentMeter = new DefaultMeter.DefaultCurrentMeter(
                scm.getSystemPrefix() + "V" + "CommandStationCurrent",
                Meter.Unit.NoPrefix, 0, 12.7, 0.1, updateTask);
        
        voltageMeter = new DefaultMeter.DefaultVoltageMeter(
                scm.getSystemPrefix() + "V" + "CommandStationVoltage",
                Meter.Unit.NoPrefix, 0, 25.4, 0.2, updateTask);
        
        InstanceManager.getDefault(MeterManager.class).register(currentMeter);
        InstanceManager.getDefault(MeterManager.class).register(voltageMeter);
        
        tc.addLocoNetListener(~0, this);
        
        updateTask.initTimer();
    }

    @Override
    public void message(LocoNetMessage msg) {
        try {
            if (msg.getOpCode() != LnConstants.OPC_EXP_RD_SL_DATA
                    || msg.getElement(1) != 21
                    || msg.getElement(2) != 1
                    || msg.getElement(3) != 0x79) {
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
        currentMeter.setCommandedAnalogValue(value);
    }

    private void setVoltage(double value) throws JmriException {
        voltageMeter.setCommandedAnalogValue(value);
    }

    public void dispose() {
        updateTask.disable(currentMeter);
        updateTask.disable(voltageMeter);
        InstanceManager.getDefault(MeterManager.class).deregister(currentMeter);
        InstanceManager.getDefault(MeterManager.class).deregister(voltageMeter);
        updateTask.dispose(currentMeter);
        updateTask.dispose(voltageMeter);
    }

    public void requestUpdateFromLayout() {
        sm.sendReadSlot(249);
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(LnPredefinedMeters.class);

}

package jmri.jmrix.roco.z21;

import jmri.*;
import jmri.implementation.DefaultMeter;
import jmri.implementation.MeterUpdateTask;

/**
 * Provide access to voltage and current readings from the Roco Z21 
 *
 * @author Mark Underwood    Copyright (C) 2015
 * @author Paul Bender       Copyright (C) 2017
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class Z21PredefinedMeters {

    private Z21TrafficController tc;
    private Z21SystemConnectionMemo _memo;
    private final MeterUpdateTask updateTask;
    private final Meter currentMeter;
    private final Meter voltageMeter;
    private boolean enabled = false;  // disable by default; prevent polling when not being used.

    public Z21PredefinedMeters(Z21SystemConnectionMemo memo) {
        
        _memo = memo;
        tc = _memo.getTrafficController();
        
        updateTask = new UpdateTask(-1);
        
        currentMeter = new DefaultMeter.DefaultCurrentMeter(
                memo.getSystemPrefix() + "V" + "CommandStationCurrent",
                Meter.Unit.Milli, 0, 10000.0, 100, updateTask);
        
        voltageMeter = new DefaultMeter.DefaultVoltageMeter(
                memo.getSystemPrefix() + "V" + "CommandStationVoltage",
                Meter.Unit.Milli, 0, 50000.0, 500, updateTask);
        
        InstanceManager.getDefault(MeterManager.class).register(currentMeter);
        InstanceManager.getDefault(MeterManager.class).register(voltageMeter);
        
        log.debug("Z21MultiMeter constructor called");

    }

    public void setZ21TrafficController(Z21TrafficController controller) {
        tc = controller;
    }

    public void dispose() {
        updateTask.disable(currentMeter);
        updateTask.disable(voltageMeter);
        InstanceManager.getDefault(MeterManager.class).deregister(currentMeter);
        InstanceManager.getDefault(MeterManager.class).deregister(voltageMeter);
        updateTask.dispose(currentMeter);
        updateTask.dispose(voltageMeter);
    }


    private class UpdateTask extends MeterUpdateTask implements Z21Listener {
    
        public UpdateTask(int interval) {
            super(interval);
            tc.addz21Listener(this);
        }
    
        @Override 
        public void enable(){
            enabled = true;
            RocoZ21CommandStation cs = _memo.getRocoZ21CommandStation();
            cs.setSystemStatusMessagesFlag(true);
            tc.sendz21Message(Z21Message.getLanSetBroadcastFlagsRequestMessage(cs.getZ21BroadcastFlags()),this);
            super.enable();
        }

        @Override 
        public void disable(){
            if (!enabled) return;
            super.disable();
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
                try {
                    setCurrent(r.getSystemDataMainCurrent() * 1.0f);
                    setVoltage(r.getSystemDataVCCVoltage() * 1.0f);
                } catch (JmriException e) {
                    log.error("exception thrown by setCurrent or setVoltage", e);
                }
            }
        }

        private void setCurrent(double value) throws JmriException {
            currentMeter.setCommandedAnalogValue(value);
        }

        private void setVoltage(double value) throws JmriException {
            voltageMeter.setCommandedAnalogValue(value);
        }

        @Override
        public void requestUpdateFromLayout() {
            if( enabled ) {
                tc.sendz21Message(Z21Message.getLanSystemStateDataChangedRequestMessage(), this);
            }
        }
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Z21PredefinedMeters.class);

}

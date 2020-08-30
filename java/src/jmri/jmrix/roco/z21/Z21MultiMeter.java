package jmri.jmrix.roco.z21;

import jmri.*;
import jmri.implementation.DefaultMeter;
import jmri.implementation.MeterUpdateTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to voltage and current readings from the Roco Z21 
 *
 * @author Mark Underwood (C) 2015
 * @author Paul Bender (C) 2017
 */
public class Z21MultiMeter extends jmri.implementation.DefaultMeterGroup {

    private Z21TrafficController tc;
    private Z21SystemConnectionMemo _memo;
    private final MeterUpdateTask updateTask;
    private final Meter currentMeter;
    private final Meter voltageMeter;
    private boolean enabled = false;  // disable by default; prevent polling when not being used.

    public Z21MultiMeter(Z21SystemConnectionMemo memo) {
        super("XVCommandStation");
//        super(-1); // no timer, since we already poll for this information. 
        _memo = memo;
        tc = _memo.getTrafficController();
        
        updateTask = new UpdateTask(-1, 0);
        
        currentMeter = new DefaultMeter("CBUSVoltageMeter", Meter.Unit.Milli, 0, 10000.0, 100, updateTask);
        voltageMeter = new DefaultMeter("CBUSCurrentMeter", Meter.Unit.Milli, 0, 50.0, 0.5, updateTask);
        
        InstanceManager.getDefault(MeterManager.class).register(currentMeter);
        InstanceManager.getDefault(MeterManager.class).register(voltageMeter);
        
        addMeter(MeterGroup.CurrentMeter, MeterGroup.CurrentMeterDescr, currentMeter);
        addMeter(MeterGroup.VoltageMeter, MeterGroup.VoltageMeterDescr, voltageMeter);

        log.debug("Z21MultiMeter constructor called");

    }

    public void setZ21TrafficController(Z21TrafficController controller) {
        tc = controller;
    }

/*
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
*/

    private class UpdateTask extends MeterUpdateTask implements Z21Listener {
    
        public UpdateTask(int interval, int minTimeBetweenUpdates) {
            super(interval, minTimeBetweenUpdates);
            tc.addz21Listener(this);
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
                try {
                    setCurrent(r.getSystemDataMainCurrent() * 1.0f);
                    setVoltage(r.getSystemDataVCCVoltage() * 1.0f);
                } catch (JmriException e) {
                    log.error("exception thrown by setCurrent or setVoltage", e);
                }
            }
        }

        @Override
        public void requestUpdateFromLayout() {
            if( enabled ) {
                tc.sendz21Message(Z21Message.getLanSystemStateDataChangedRequestMessage(), this);
            }
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(Z21MultiMeter.class);

}

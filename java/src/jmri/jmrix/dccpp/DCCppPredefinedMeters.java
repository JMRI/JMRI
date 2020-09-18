package jmri.jmrix.dccpp;

import jmri.*;
import jmri.implementation.DefaultMeter;
import jmri.implementation.MeterUpdateTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to current meter from the DCC++ Base Station
 *
 * @author Mark Underwood    Copyright (C) 2015
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class DCCppPredefinedMeters implements DCCppListener {

    private DCCppTrafficController tc = null;
    private final MeterUpdateTask updateTask;
    private final Meter currentMeter;

    public DCCppPredefinedMeters(DCCppSystemConnectionMemo memo) {
        
        tc = memo.getDCCppTrafficController();

        updateTask = new MeterUpdateTask() {
            @Override
            public void requestUpdateFromLayout() {
                tc.sendDCCppMessage(DCCppMessage.makeReadTrackCurrentMsg(), DCCppPredefinedMeters.this);
            }
        };
        
        currentMeter = new DefaultMeter.DefaultCurrentMeter(
                memo.getSystemPrefix() + "V" + "BaseStationCurrent",
                Meter.Unit.Percent, 0, 100.0, 1.0, updateTask);
        
        InstanceManager.getDefault(MeterManager.class).register(currentMeter);
        
        // TODO: For now this is OK since the traffic controller
        // ignores filters and sends out all updates, but
        // at some point this will have to be customized.
        tc.addDCCppListener(DCCppInterface.THROTTLE, this);

        //is_enabled = false;
        updateTask.initTimer();

        log.debug("DCCppMultiMeter constructor called");
    }

    public void setDCCppTrafficController(DCCppTrafficController controller) {
        tc = controller;
    }

    @Override
    public void message(DCCppReply r) {
        log.debug("DCCppMultiMeter received reply: {}", r.toString());
        if (r.isCurrentReply()) {
            try {
                currentMeter.setCommandedAnalogValue(((r.getCurrentInt() * 1.0f) / (DCCppConstants.MAX_CURRENT * 1.0f)) * 100.0f );  // return as percentage.
            } catch (JmriException e) {
                log.error("exception thrown when set current", e);
            }
        }
    }

    @Override
    public void message(DCCppMessage m) {
        // Do nothing
    }
    
    public void dispose() {
        updateTask.disable(currentMeter);
        InstanceManager.getDefault(MeterManager.class).deregister(currentMeter);
        updateTask.dispose(currentMeter);
    }
    
    // Handle a timeout notification
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        log.debug("Notified of timeout on message {}, {} retries available.", msg.toString(), msg.getRetries());
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppPredefinedMeters.class);

}

package jmri.jmrix.dccpp;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Meter;
import jmri.MeterManager;
import jmri.implementation.DefaultMeter;
import jmri.implementation.MeterUpdateTask;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to current and voltage meters from the DCC++ Base Station
 *   Creates meters based on values sent from command station
 *   User can create new meters in the sketch.
 *
 * @author Mark Underwood    Copyright (C) 2015
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class DCCppPredefinedMeters implements DCCppListener {

    private DCCppTrafficController tc = null;
    private final MeterUpdateTask updateTask;
    private String systemPrefix = null;
    private char beanType;
    private HashMap<String, Meter> meters = new HashMap<String, Meter>(2); //keep track of defined meters

    public DCCppPredefinedMeters(DCCppSystemConnectionMemo memo) {
        log.debug("Constructor called");

        systemPrefix = memo.getSystemPrefix();
        beanType = InstanceManager.getDefault(MeterManager.class).typeLetter();
        tc = memo.getDCCppTrafficController();

        updateTask = new MeterUpdateTask(10000, 10000) {
            @Override
            public void requestUpdateFromLayout() {
                if (tc.getCommandStation().isCurrentListSupported()) {
                    tc.sendDCCppMessage(DCCppMessage.makeCurrentValuesMsg(), DCCppPredefinedMeters.this);
                } else {
                    tc.sendDCCppMessage(DCCppMessage.makeReadTrackCurrentMsg(), DCCppPredefinedMeters.this);
                }
            }
        };

        // TODO: For now this is OK since the traffic controller
        // ignores filters and sends out all updates, but
        // at some point this will have to be customized.
        tc.addDCCppListener(DCCppInterface.CS_INFO, this);

        updateTask.initTimer();

        //NOTE: since we may not have the version back yet, send the old AND new requests below
        
        //request one 'c' reply to set up the meters
        tc.sendDCCppMessage(DCCppMessage.makeReadTrackCurrentMsg(), DCCppPredefinedMeters.this);

        // send <=> to get track list and <JG> to get current maximums
        tc.sendDCCppMessage(DCCppMessage.makeTrackManagerRequestMsg(), DCCppPredefinedMeters.this);
        tc.sendDCCppMessage(DCCppMessage.makeCurrentMaxesMsg(), DCCppPredefinedMeters.this);
    }

    public void setDCCppTrafficController(DCCppTrafficController controller) {
        tc = controller;
    }

    /* handle new Meter replies and original current replies
     *   creates meters if first time this name is encountered
     *   uses new MeterReply message format from DCC-EX
     *   also supports original "current percent" meter from
     *   older DCC++                                           */
    @Override
    public void message(DCCppReply r) {

        if (r.isCurrentMaxesReply()) {
            //create a meter for each Track TODO: ignore duplicate jGs
            for (int t = 0; t <= r.getCurrentMaxesList().size(); t = t+1) {
                Meter newMeter;
                String sysName = systemPrefix + beanType + "_Track_" + t;
                double maxValue = r.getCurrentMaxesList().get(t);
                log.debug("Adding new current meter '{}'", sysName);
                newMeter = new DefaultMeter.DefaultCurrentMeter(
                        sysName, jmri.Meter.Unit.Milli, -1, maxValue, 1.0, updateTask);
                //store meter by incoming name for lookup later
                meters.put(sysName, newMeter);
                InstanceManager.getDefault(MeterManager.class).register(newMeter);
            }            
            return;
        }
        
        if (r.isCurrentValuesReply()) {
            //update the meter for each Track
            for (int t = 0; t <= r.getCurrentValuesList().size(); t = t+1) {
                String sysName = systemPrefix + beanType + "_Track_" + t;
                //set the newValue for the meter
                Meter meter = meters.get(sysName);
                double meterValue = r.getCurrentValuesList().get(t);
                log.debug("Setting value for '{}' to {}" , sysName, meterValue);
                try {
                    meter.setCommandedAnalogValue(meterValue);
                } catch (JmriException e) {
                    log.error("exception thrown when setting meter '{}' value {}", sysName, meterValue, e);
                }
            }            
            return;
        }
        
        //bail if other message types received
        if (!r.isCurrentReply() && !r.isMeterReply()) return;

        log.debug("Handling reply: '{}'", r);

        //assume old-style current message and default name and settings
        String meterName = "CurrentPct";
        double meterValue = 0.0;
        String meterType = DCCppConstants.CURRENT;
        Meter.Unit meterUnit = Meter.Unit.Percent;
        double minValue = 0.0;
        double maxValue = 100.0;
        double resolution = 0.1;
        double warnValue = 100.0; //TODO: use when Meter updated to take advantage of it

        //use settings from message if Meter reply
        if (r.isMeterReply()) {
            meterName = r.getMeterName();
            meterValue= r.getMeterValue();
            meterType = r.getMeterType();
            minValue  = r.getMeterMinValue();
            maxValue  = r.getMeterMaxValue();
            resolution= r.getMeterResolution();
            meterUnit = r.getMeterUnit();
            warnValue = r.getMeterWarnValue();
        }

        //create, store and register the meter if not yet defined
        if (!meters.containsKey(meterName)) {
            log.debug("Adding new meter '{}' of type '{}' with unit '{}' {}",
                    meterName, meterType, meterUnit, warnValue);
            Meter newMeter;
            String sysName = systemPrefix + beanType + meterType + "_" + meterName;
            if (meterType.equals(DCCppConstants.VOLTAGE)) {
                newMeter = new DefaultMeter.DefaultVoltageMeter(
                        sysName, meterUnit, minValue, maxValue, resolution, updateTask);
            } else {
                newMeter = new DefaultMeter.DefaultCurrentMeter(
                        sysName, meterUnit, minValue, maxValue, resolution, updateTask);
            }
            //store meter by incoming name for lookup later
            meters.put(meterName, newMeter);
            InstanceManager.getDefault(MeterManager.class).register(newMeter);
        }

        //calculate percentage meter value if original current reply message type received
        if (r.isCurrentReply()) {
            meterValue = ((r.getCurrentInt() * 1.0f) / (DCCppConstants.MAX_CURRENT * 1.0f)) * 100.0f ;
        }

        //set the newValue for the meter
        Meter meter = meters.get(meterName);
        log.debug("Setting value for '{}' to {}" , meterName, meterValue);
        try {
            meter.setCommandedAnalogValue(meterValue);
        } catch (JmriException e) {
            log.error("exception thrown when setting meter '{}' value {}", meterName, meterValue, e);
        }
    }

    @Override
    public void message(DCCppMessage m) {
        // Do nothing
    }

    /* dispose of all defined meters             */
    /* NOTE: I don't know if this is ever called */
    public void dispose() {
        meters.forEach((k, v) -> {
            log.debug("disposing '{}'", k);
            updateTask.disable(v);
            InstanceManager.getDefault(MeterManager.class).deregister(v);
            updateTask.dispose(v);
        });
    }

    // Handle message timeout notification, no retry
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        log.debug("Notified of timeout on message '{}', not retrying", msg);
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppPredefinedMeters.class);

}

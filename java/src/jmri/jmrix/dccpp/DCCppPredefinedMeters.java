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
 * Provide access to current meters from the DCC++ Base Station
 *   Creates meters based on values sent from command station
 *
 * @author Mark Underwood    Copyright (C) 2015
 * @author Daniel Bergqvist  Copyright (C) 2020
 * @author mstevetodd        Copyright (C) 2025
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

        updateTask = new MeterUpdateTask(0, 10000) {
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

        //request one 'c' reply to set up the meters
        if (!tc.getCommandStation().isCurrentListSupported()) {
            tc.sendDCCppMessage(DCCppMessage.makeReadTrackCurrentMsg(), DCCppPredefinedMeters.this);
        }

        // send <JG> to get current maximums, response used to build list of Meters (no check here as version might not be ready yet)
        tc.sendDCCppMessage(DCCppMessage.makeCurrentMaxesMsg(), DCCppPredefinedMeters.this);

        updateTask.initTimer();
        
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
            //create a meter for each Track           
            for (int t = 0; t <= r.getCurrentMaxesList().size()-1; t++) {
                Integer maxValue = r.getCurrentMaxesList().get(t);
                Meter newMeter;
                String sysName = systemPrefix + beanType + t;
                if (meters.get(sysName) == null) {
                    String mode = tc.getCommandStation().getTrackMode(t);
                    String userName = "Track " + String.valueOf((char)('A'+t)) + " " + mode + " (" + systemPrefix + ")";
                    log.debug("Adding new current meter {} ({})", sysName, userName);
                    newMeter = new DefaultMeter.DefaultCurrentMeter(
                            sysName, jmri.Meter.Unit.Milli, -5.0, maxValue, 1.0, updateTask);
                    newMeter.setUserName(userName);
                    //store and register new Meter
                    meters.put(sysName, newMeter);
                    InstanceManager.getDefault(MeterManager.class).register(newMeter);
                } else {
                    log.debug("not creating duplicate meter '{}'", sysName);
                }
            }            
            return;
        }
        
        if (r.isCurrentValuesReply()) {
            //update the meter for each Track
            for (int t = 0; t <= r.getCurrentValuesList().size()-1; t++) {
                String sysName = systemPrefix + beanType + t;
                //set the newValue for the meter
                Meter meter = meters.get(sysName);
                Integer meterValue = Math.max(r.getCurrentValuesList().get(t), 0); //get the value, ignore negative values
                log.debug("Setting value for '{}' to {}" , sysName, meterValue);
                try {
                    meter.setCommandedAnalogValue(meterValue);
                } catch (JmriException e) {
                    log.error("exception thrown when setting meter '{}' to value {}", sysName, meterValue, e);
                }
            }            
            return;
        }
        
        if (r.isTrackManagerReply()) {
            //recalculate the username since mode may have changed 
            int trackNum = r.getTrackManagerLetter() - 'A'; //get track number from track letter
            String userName = "Track " + r.getTrackManagerLetter() + " " + r.getTrackManagerMode() + " (" + systemPrefix + ")";
            String sysName = systemPrefix + beanType + trackNum;
            Meter meter = meters.get(sysName);
            if (meter != null) {
                log.debug("Updating username for current meter {} to '{}'", sysName, userName);
                meter.setUserName(userName); //TODO: fix Meter to redraw title for this change
            }
            return;
        }
        
        //bail if other message types received
        if (!r.isCurrentReply() && !r.isMeterReply()) return;

        //also stop processing the older replies if the newer lists are supported
        if (tc.getCommandStation().isCurrentListSupported()) return;

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

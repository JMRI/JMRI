package jmri.jmrix.dccpp;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.Timebase;
import jmri.implementation.DefaultClockControl;

/**
 * Class providing Clock Control to the DCC-EX client.
 * Does nothing unless "Synchronize Internal Fast Clock and DCC-EX Fast Clock" is enabled.
 * If "Time Source" is "Internal Computer Clock", send any changes to Fast Clock Rate or Time
 *   to the command station.
 * If "Time Source" is "DCC-EX Fast Clock", listen for incoming Time messages and set the Fast
 *   Clock from these. Ignores incoming Rate messages.
 * 
 * @author mstevetodd 2023
 */
public class DCCppClockControl extends DefaultClockControl implements DCCppListener {

    DCCppSystemConnectionMemo _memo = null;
    DCCppTrafficController _tc = null;
    Timebase timebase;
    Calendar _cal;    
    java.beans.PropertyChangeListener minuteChangeListener;
    boolean isRunning; //track clock's pause state (Note: timebase.isRun() is updated too late) 
    final static long MSECPERHOUR = 3600000;
    final static long MSECPERMINUTE = 60000;

    public DCCppClockControl(DCCppSystemConnectionMemo memo) {
        log.trace("DCCppClockControl (DCCppSystemConnectionMemo {})", memo); // NOI18N
        
        _memo = memo;
        _tc = _memo.getDCCppTrafficController();
        _tc.addDCCppListener(DCCppInterface.CS_INFO, this);
        _cal = Calendar.getInstance();

        timebase = InstanceManager.getNullableDefault(jmri.Timebase.class);
        if (timebase == null) {
            log.error("No Internal Timebase Instance"); // NOI18N
            return;
        }
        // Create a timebase listener for the Minute change events
        minuteChangeListener = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                log.trace("minuteChangeListener propertyChange for '{}' from '{}' to '{}'",e.getPropertyName(),e.getOldValue(),e.getNewValue());                                
                setTime(timebase.getTime()); 
            }
        };
        timebase.addMinuteChangeListener(minuteChangeListener);
        
    }

    /**
     * Get name of hardware clock, shown in UI
     */
    @Override
    public String getHardwareClockName() {
        log.trace("getHardwareClockName()"); // NOI18N
        return ("DCC-EX Fast Clock");
    }

    /**
     * Send the new fastclock rate to CS if internal is master AND synchronize enabled
     *   send rate of zero if clock is not running
     *   Note: fastclock rate and time are in a single message
     */
    @Override
    public void setRate(double newRate) {
        log.trace("setRate({})", (int)newRate); // NOI18N
        if (timebase.getInternalMaster() && timebase.getSynchronize()) {
            _cal.setTime(timebase.getTime());
            int minutes = _cal.get(Calendar.HOUR)*60+_cal.get(Calendar.MINUTE);
            if (!isRunning) newRate = 0; //send rate of zero if clock is not running                
            _tc.sendDCCppMessage(DCCppMessage.makeClockSetMsg(minutes, (int)newRate), null);
        }
        return;
    }
    public void setRate() {
        setRate(timebase.getRate());
    }

    @Override
    public double getRate() {
        log.trace("getRate()"); // NOI18N
        //request that CS return the time (and the rate as they're in same message)
        _tc.sendDCCppMessage(DCCppMessage.makeClockRequestTimeMsg(), null);
        return timebase.getRate();
    }

    /**
     * Send the new fast clock time to CS if internal is master AND synchronize enabled
     *   Note: fastclock rate and time are in a single message
     */
    @Override
    public void setTime(Date newTimestamp) {
        log.trace("setTime({})", newTimestamp); // NOI18N
        if (timebase.getInternalMaster() && timebase.getSynchronize()) {
            _cal.setTime(newTimestamp);
            int minutes = _cal.get(Calendar.HOUR)*60+_cal.get(Calendar.MINUTE);
            _tc.sendDCCppMessage(DCCppMessage.makeClockSetMsg(minutes, (int)timebase.getRate()), null);
        }
        return;
    }
    public void setTime() {
        setTime(timebase.getTime());
    }

    @Override
    public Date getTime() {
        log.trace("getTime()"); // NOI18N
        // send get time message
        _tc.sendDCCppMessage(DCCppMessage.makeClockRequestTimeMsg(), null); // <JC>
        // return the current time without waiting for response... (?)
        return timebase.getTime();
    }

    /**
     * Pause, unpause and initialize fast clock
     */
    @Override
    public void startHardwareClock(Date now) {
        log.trace("startHardwareClock({})", now); // NOI18N
        isRunning = true;
        setRate(); //notify the CS
        return;
    }
    @Override
    public void stopHardwareClock() {
        log.trace("stopHardwareClock()"); // NOI18N
        isRunning = false;
        setRate(); //notify the CS
        return;
    }
    @Override
    public void initializeHardwareClock(double rate, Date now, boolean getTime) {
        log.trace("initializeHardwareClock(rate={}, time={}, getTime={}) sync={}, internal={}", 
                rate, now, getTime, timebase.getSynchronize(), timebase.getInternalMaster()); // NOI18N
        isRunning = timebase.getRun();
        if (timebase.getSynchronize()) {
            if (timebase.getInternalMaster()) {
                setRate(); //notify the CS of time and rate           
            } else {
                getRate(); //request time and rate from CS
            }
        }
    }    

    /**
     * Prevent user entry of a fractional rate, since DCC-EX only supports integer rates
     */
    @Override
    public boolean requiresIntegerRate() {
        log.trace("requiresIntegerRate() returns true"); // NOI18N
        return true;
    }

    /* handle incoming clock-related messages 
     * update the time ONLY if synchronize is enabled AND DCC-EX is Master 
     * Ignore the incoming rate, since JMRI only supports changing rate for Internal Masters */
    
    @SuppressWarnings("deprecation")
    @Override
    public void message(DCCppReply msg) {
        log.trace("message(DCCppReply {})", msg); // NOI18N
        if (msg.isClockReply() && timebase.getSynchronize() && timebase.getMasterName().equals(getHardwareClockName())) {
            log.trace("Clock message(DCCppReply {}), time={}, rate={}", msg, msg.getClockMinutesString(), msg.getClockRateString()); // NOI18N
            
            //set the new time from message
            Date today = timebase.getTime(); //current timestamp
            long ms = today.getTime();                //get current timestamp in msecs
            ms -= today.getHours() * MSECPERHOUR;     //subtract out current hours
            ms -= today.getMinutes() * MSECPERMINUTE; //subtract out current minutes
            ms += msg.getClockMinutesInt() * MSECPERMINUTE; //add in new minutes from message
            timebase.setTime(new Date(ms));  //set the fastclock from this msecs value
        }
    }

    /* process outgoing messages and retries, not needed for DCC-EX */
    @Override
    public void message(DCCppMessage msg) {
        log.trace("message(DCCppMessage {})", msg); // NOI18N
    }
    /* if timeout, don't resend */
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        log.trace("notifyTimeout(DCCppMessage {})", msg); // NOI18N        
    }  

    private final static Logger log = LoggerFactory.getLogger(DCCppClockControl.class);
}



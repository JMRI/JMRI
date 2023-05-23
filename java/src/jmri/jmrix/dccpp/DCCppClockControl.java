package jmri.jmrix.dccpp;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.Timebase;
import jmri.implementation.DefaultClockControl;

/**
 * Class providing DCCpp Clock Control to the DCCpp client.
 * @author mstevetodd 2023
 */
public class DCCppClockControl extends DefaultClockControl {

    DCCppSystemConnectionMemo _memo = null;
    DCCppTrafficController _tc = null;
    Timebase timebase;
    java.beans.PropertyChangeListener minuteChangeListener;
    boolean isRunning; //track pause state 

    public DCCppClockControl(DCCppSystemConnectionMemo memo) {
        log.debug("DCCppClockControl (DCCppSystemConnectionMemo memo)"); // NOI18N
        
        _memo = memo;
        _tc = _memo.getDCCppTrafficController();
        timebase = InstanceManager.getNullableDefault(jmri.Timebase.class);
        if (timebase == null) {
            log.error("No Internal Timebase Instance"); // NOI18N
            return;
        }
        // Create a timebase listener for the Minute change events
        minuteChangeListener = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                setTime(timebase.getTime()); 
            }
        };
        timebase.addMinuteChangeListener(minuteChangeListener);
        
//        setRate(); //send current fastclock time and rate to CS (same message)
        
//        _tc.addTrafficListener(DCCppInterface.CLOCK, this);

    }

    /**
     * Operational instance variables (not saved between runs)
     */
    /**
     * Get name of hardware clock
     */
    @Override
    public String getHardwareClockName() {
        log.debug("getHardwareClockName()"); // NOI18N
        return ("DCC-EX Fast Clock");
    }

    /**
     * Send the current fastclock rate to CS
     *   Note: fastclock rate and time are in a single message
     */
    public void setRate() {
        setRate(timebase.getRate());
    }

    /**
     * Send the new fastclock rate to CS if internal is master AND synchronize enabled
     *   send rate of zero if clock is not running
     *   Note: fastclock rate and time are in a single message
     */
    @Override
    public void setRate(double newRate) {
        log.debug("setRate({})", (int)newRate); // NOI18N
        if (timebase.getInternalMaster() && timebase.getSynchronize()) {
            Date currentTimestamp = timebase.getTime();
            int minutes = currentTimestamp.getHours()*60+currentTimestamp.getMinutes();
            if (!isRunning) newRate = 0; //send rate of zero if clock is not running                
            _tc.sendDCCppMessage(DCCppMessage.makeClockSetMsg(minutes, (int)newRate), null);
        }
        return;
    }

    @Override
    public double getRate() {
        log.debug("getRate()"); // NOI18N
        //request that CS return the time (and the rate as they're in same message)
        _tc.sendDCCppMessage(DCCppMessage.makeClockRequestTimeMsg(), null);
        return timebase.getRate();
    }

    /**
     * Send current fast clock time to CS
     */
    public void setTime() {
        setTime(timebase.getTime());
    }

    /**
     * Send the new fast clock time to CS if internal is master AND synchronize enabled
     */
    @Override
    public void setTime(Date newTimestamp) {
        log.debug("setTime({})", newTimestamp); // NOI18N
        if (timebase.getInternalMaster() && timebase.getSynchronize()) {
            @SuppressWarnings("deprecation")
            int minutes = newTimestamp.getHours()*60+newTimestamp.getMinutes();
            _tc.sendDCCppMessage(DCCppMessage.makeClockSetMsg(minutes), null);
        }
        return;
    }

    @Override
    public Date getTime() {
        log.debug("getTime()"); // NOI18N
        // send get time message
        _tc.sendDCCppMessage(DCCppMessage.makeClockRequestTimeMsg(), null);
        // return the current time without waiting for response... (?)
        return timebase.getTime();
    }

    /**
     * Pause, unpause and initialize fast clock
     */
    @Override
    public void startHardwareClock(Date now) {
        log.debug("startHardwareClock()"); // NOI18N
        isRunning = true;
        setRate(); //notify the CS
        return;
    }
    @Override
    public void stopHardwareClock() {
        log.debug("stopHardwareClock()"); // NOI18N
        isRunning = false;
        setRate(); //notify the CS
        return;
    }
    @Override
    public void initializeHardwareClock(double rate, Date now, boolean getTime) {
        isRunning = timebase.getRun();
//        setRate(rate);
//        setTime(now);
    }    

    /**
     * Prevent user entry of a fractional rate, since DCC-EX only supports integer rates
     */
    @Override
    public boolean requiresIntegerRate() {
        log.debug("requiresIntegerRate() returns true"); // NOI18N
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppClockControl.class);
   
}



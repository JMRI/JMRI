
package jmri.jmrit.withrottle;

import java.beans.PropertyChangeListener;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.Timebase;

/**
 * Fast Clock interface for Wi-Fi throttles.
 * <p>
 * Fast Clock display on devices will be synchronized with hardware or software
 * clock. Time is UTC seconds on Wi-Fi devices, Local milliseconds in JMRI.
 *
 * @author Brett Hoffman Copyright (C) 2018
 */
public class FastClockController extends AbstractController {

    private final Timebase fastClock;
    private final PropertyChangeListener timeAndRateListener;
    
    public FastClockController() {
               
        fastClock = InstanceManager.getDefault(jmri.Timebase.class);
        timeAndRateListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                //skip minutes updates on this listener, handled in minuteListener
                log.trace("timeAndRateListener propertyChange for '{}' from '{}' to '{}'",e.getPropertyName(),e.getOldValue(),e.getNewValue());                
                if (!e.getPropertyName().equals("minutes")) {
                    sendFastTimeAndRate();
                }
            }
        };
        
        isValid = true;
        
        fastClock.addPropertyChangeListener(timeAndRateListener);
    }
    
    @Override
    boolean verifyCreation() {
        return isValid;
    }

    @Override
    void handleMessage(String message, DeviceServer deviceServer) {
        throw new UnsupportedOperationException("Not used.");
    }

    @Override
    void register() {
        throw new UnsupportedOperationException("Not used.");
    }

    @Override
    void deregister() {
        // cancel callback to update time
        fastClock.removePropertyChangeListener(timeAndRateListener);
    }
    
    
    /**
     * Fast clock should not have a time zone.
     * <p>
     * Add the offset to give straight UTC value,
     *  based on the fastclock's datetime
     * @return Time, as UTC in seconds
     */
    private long getAdjustedTime() {
        long fastClockTime = fastClock.getTime().getTime(); //fastclock time as milliseconds
        int timeZoneOffset = TimeZone.getDefault().getOffset(fastClockTime); //timezone offset in milliseconds
        return ((fastClockTime + timeZoneOffset) / 1000); //returned adjusted time in seconds
    }
       
    /**
     * Send Time and Rate.
     * <p>
     * Time on device will update to the value that is sent and rate will allow 
     * Fast Clock to keep its own time. A rate == 0 will tell the device to 
     * stop the clock.
     */
    public void sendFastTimeAndRate() {
        //  Send the time and run rate whether running or not
        if (listeners != null) {
            for (ControllerInterface listener : listeners) {
                listener.sendPacketToDevice("PFT" + getAdjustedTime() + "<;>" + fastClock.userGetRate());
            }
            if (!fastClock.getRun()) {
                //  Not running, send rate of 0
                //  This will stop a running clock without changing stored rate
                for (ControllerInterface listener : listeners) {
                    listener.sendPacketToDevice("PFT" + getAdjustedTime() + "<;>" + 0.0);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(FastClockController.class);
}

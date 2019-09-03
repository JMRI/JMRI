
package jmri.jmrit.withrottle;

import java.beans.PropertyChangeListener;
import java.util.TimeZone;
import jmri.InstanceManager;
import jmri.Timebase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    //  To correct for local time
    private final int timeZoneOffset;
    private final PropertyChangeListener minuteListener;
    private final PropertyChangeListener rateListener;
    
    //  Number of real minutes between re-sync of time
    private static final short UPDATE_MINUTES = 5;
    //  Essentially, rate times minutes desired between re-sync
    private static short updateMinsSetpoint;
    private short updateMinuteCount = 0;

    public FastClockController() {
        
        fastClock = InstanceManager.getDefault(jmri.Timebase.class);
        timeZoneOffset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
        minuteListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                sendFastTime();
            }
        };
        rateListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                setReSyncSetpoint();
                sendFastRate();
            }
        };
        
        isValid = true;
        
        updateMinsSetpoint((short)(fastClock.userGetRate() * UPDATE_MINUTES));
        setReSyncSetpoint();
        // request callback to update time
        fastClock.addMinuteChangeListener(minuteListener);
        fastClock.addPropertyChangeListener(rateListener);
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
        fastClock.removeMinuteChangeListener(minuteListener);
        fastClock.removePropertyChangeListener(rateListener);
    }
    
    
    /**
     * Fast clock should not have a time zone.
     * <p>
     * Remove the offset to give straight UTC value.
     * @return Time with offset removed
     */
    private long getAdjustedTime() {
        return ((fastClock.getTime().getTime() + timeZoneOffset) / 1000);
    }
    
    /**
     * Send just time.
     * <p>
     * Use to synchronize time on Wi-Fi devices to nearest second. Send no rate.
     */
    public void sendFastTime() {
        updateMinuteCount++;
        if (updateMinuteCount >= updateMinsSetpoint) {
            if (listeners != null) {
                for (ControllerInterface listener : listeners) {
                    listener.sendPacketToDevice("PFT" + getAdjustedTime());
                }
            }
            updateMinuteCount = 0;
        }
    }
    
    /**
     * Send Time and Rate.
     * <p>
     * Time on device will update to the value that is sent and rate will allow 
     * Fast Clock to keep its own time. A rate == 0 will tell the device to 
     * stop the clock.
     */
    public void sendFastRate() {
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

    private static void updateMinsSetpoint(short newVal) {
        updateMinsSetpoint = newVal;
    }

    private void setReSyncSetpoint() {
        updateMinsSetpoint((short)(fastClock.userGetRate() * UPDATE_MINUTES));
    }

    // private final static Logger log = LoggerFactory.getLogger(FastClockController.class);
}

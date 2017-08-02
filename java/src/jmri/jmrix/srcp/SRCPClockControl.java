package jmri.jmrix.srcp;

import java.util.Date;
import jmri.InstanceManager;
import jmri.implementation.DefaultClockControl;

/**
 * Class providing SRCP Clock Control to the SRCP client.
 *
 * @author	Paul Bender Copyright (C) 2014
 */
public class SRCPClockControl extends DefaultClockControl {

    SRCPBusConnectionMemo _memo = null;
    SRCPTrafficController _tc = null;

    public SRCPClockControl(SRCPBusConnectionMemo memo) {
        _memo = memo;
        _tc = _memo.getTrafficController();
    }

    /**
     * Operational instance variables (not saved between runs)
     */
    /**
     * Get name of hardware clock
     */
    @Override
    public String getHardwareClockName() {
        return ("SRCP Fast Clock");
    }

    /**
     * Get and set the rate of the fast clock Note: The rate is an integer that
     * multiplies the wall clock For example, a rate of 4 specifies that the
     * fast clock runs 4 times faster than the wall clock. For the default
     * implementation, setRate is ignored, and getRate returns the rate of the
     * internal clock;
     */
    @Override
    public void setRate(double newRate) {
        String text = "INIT " + _memo.getBus() + " TIME 1 " + newRate;
        // create and send the message itself
        _tc.sendSRCPMessage(new SRCPMessage(text), null);
        return;
    }

    @Override
    public double getRate() {
        // There is no way to request the current rate from the
        // server, so we need to watch for rate in return messages
        // and save the value.
        return InstanceManager.getDefault(jmri.Timebase.class).getRate();
    }

    /**
     * Set and get the fast clock time For the default implementation,set time
     * is ignored and getTime returns the time of the internal clock;
     */
    @Override
    public void setTime(Date now) {
        // prepare to format the date as <JulDay> <Hour> <Minute> <Seconds>
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyDDD hh mm ss");
        String text = "SET " + _memo.getBus() + " TIME " + sdf.format(now);
        // create and send the message itself
        _tc.sendSRCPMessage(new SRCPMessage(text), null);
        return;
    }

    @Override
    public Date getTime() {
        // this requests the time, but it doesn't actually send
        // the time from the server to the clock yet.
        String text = "GET " + _memo.getBus() + " TIME";
        // create and send the message itself
        _tc.sendSRCPMessage(new SRCPMessage(text), null);

        return InstanceManager.getDefault(jmri.Timebase.class).getTime();
    }

    /**
     * Start and stop hardware fast clock Many hardware fast clocks continue to
     * run indefinitely. This is provided for the case where the hardware clock
     * can be stopped and started.
     */
    @Override
    public void startHardwareClock(Date now) {
        setTime(now);
        return;
    }

    @Override
    public void stopHardwareClock() {
        return;
    }

    /**
     * Initialize the hardware fast clock Note: When the hardware clock control
     * receives this, it should initialize those clock settings that are
     * available on the hardware clock. Default implementation is to ignore this
     * request.
     */
    @Override
    public void initializeHardwareClock(double rate, Date now, boolean getTime) {
        return;
    }
}



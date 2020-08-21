package jmri.implementation;

import java.util.Date;
import javax.annotation.CheckForNull;
import jmri.ClockControl;
import jmri.InstanceManager;

/**
 * Class providing default logic of the ClockControl interface.
 *
 * Hardware systems that have fast clocks should "extend DefaultClockControl"
 * and override the appropriate methods.
 *
 * This class provides default implementations of ClockControl methods that are
 * not needed in the hardware implementation if one exists, or for those systems
 * with no hardware fast clock.
 *
 * @author Dave Duchamp Copyright (C) 2007
 */
public class DefaultClockControl implements ClockControl {

    public DefaultClockControl() {

    }

    /**
     * Operational instance variables (not saved between runs)
     */
    /**
     * Get Status of the Fast Clock.
     * Potentially unused?
     * {@inheritDoc}
     */
    @Override
    public int getStatus() {
        return 0;
    }

    /**
     * Get name of hardware clock.
     * <p>
     * If there is no hardware clock, this method returns null.
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public String getHardwareClockName() {
        return null;
    }

    /**
     * Returns true if hardware clock accuracy can be corrected using the
     * computer clock. 
     * <p>
     * Hardware implementations should override this and return
     * true if they can correct their hardware clock.
     * {@inheritDoc}
     */
    @Override
    public boolean canCorrectHardwareClock() {
        return false;
    }

    /**
     * Returns true if hardware clock can be set to 12 or 24 hour display from
     * JMRI software.
     * <p>
     * Default implementation is to return false.
     * {@inheritDoc}
     */
    @Override
    public boolean canSet12Or24HourClock() {
        return false;
    }

    /**
     * Default implementation returns false.
     * <p>
     * If an integer rate is required by the
     * hardware, this method should be overridden.
     * {@inheritDoc}
     */
    @Override
    public boolean requiresIntegerRate() {
        return false;
    }

    /**
     * For the default implementation, setRate is ignored.
     * {@inheritDoc}
     */
    @Override
    public void setRate(double newRate) {
    }

    /**
     * Default implementation returns the rate of the internal clock.
     * {@inheritDoc}
     */
    @Override
    public double getRate() {
        return InstanceManager.getDefault(jmri.Timebase.class).getRate();
    }

    /**
     * For the default implementation, set time is ignored.
     * {@inheritDoc}
     */
    @Override
    public void setTime(Date now) {
    }

    /**
     * Default implementation returns InstanceM default jmri.Timebase getTime().
     * ie. the time of the internal clock.
     * {@inheritDoc}
     */
    @Override
    public Date getTime() {
        return InstanceManager.getDefault(jmri.Timebase.class).getTime();
    }

    /**
     * Default implementation is to call SetTime to now.
     * {@inheritDoc}
     */
    @Override
    public void startHardwareClock(Date now) {
        setTime(now);
    }

    /**
     * Default implementation is to ignore.
     * {@inheritDoc}
     */
    @Override
    public void stopHardwareClock() {
    }

    /**
     * Default implementation is to ignore this request.
     * {@inheritDoc}
     */
    @Override
    public void initializeHardwareClock(double rate, Date now, boolean getTime) {
    }
}

package jmri.jmrix.openlcb;

import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.protocols.TimeBroadcastConsumer;
import org.openlcb.protocols.TimeProtocol;

import java.util.Date;

import jmri.implementation.DefaultClockControl;

/**
 * Implementation of the ClockControl interface for JMRI using the OpenLCB clock listener or generator.
 *
 * @author Balazs Racz, 2018
 */

public class OlcbClockControl extends DefaultClockControl {
    public OlcbClockControl(OlcbInterface iface, boolean isMaster) {
        consumer = new TimeBroadcastConsumer(iface, TimeProtocol.DEFAULT_CLOCK);
        hardwareClock = consumer;
    }

    @Override
    public String getHardwareClockName() {
        if (consumer != null) {
            NodeID clockDef = consumer.getClockID();
            String clockName;
            if (clockDef.equals(TimeProtocol.DEFAULT_CLOCK)) {
                clockName = Bundle.getMessage("OlcbClockDefault");
            } else if (clockDef.equals(TimeProtocol.DEFAULT_RT_CLOCK)) {
                clockName = Bundle.getMessage("OlcbClockDefaultRT");
            } else if (clockDef.equals(TimeProtocol.ALT_CLOCK_1)) {
                clockName = Bundle.getMessage("OlcbClockAlt1");
            } else if (clockDef.equals(TimeProtocol.ALT_CLOCK_2)) {
                clockName = Bundle.getMessage("OlcbClockAlt2");
            } else {
                clockName = Bundle.getMessage("OlcbClockCustom", clockDef.toString());
            }
            return Bundle.getMessage("OlcbClockListenerFor", clockName);
        }
        return super.getHardwareClockName();
    }

    @Override
    public boolean canCorrectHardwareClock() {
        return false;
    }

    @Override
    public boolean canSet12Or24HourClock() {
        return false;
    }

    @Override
    public boolean requiresIntegerRate() {
        return false;
    }

    @Override
    public double getRate() {
        return hardwareClock.getRate();
    }

    @Override
    public Date getTime() {
        return new Date(hardwareClock.getTimeInMsec());
    }

    @Override
    public void stopHardwareClock() {
        hardwareClock.requestStop();
    }

    @Override
    public void startHardwareClock(Date now) {
        hardwareClock.requestSetTime(now.getTime());
        hardwareClock.requestStart();
    }

    @Override
    public void setRate(double newRate) {
        hardwareClock.requestSetRate(newRate);
    }

    @Override
    public void setTime(Date now) {
        hardwareClock.requestSetTime(now.getTime());
    }

    @Override
    public void initializeHardwareClock(double rate, Date now, boolean getTime) {
        if (!getTime) {
            hardwareClock.requestSetTime(now.getTime());
        }
        if (rate == 0) {
            hardwareClock.requestStop();
        } else {
            hardwareClock.requestSetRate(rate);
            hardwareClock.requestStart();
        }
    }

    /// This is the interface to the clock generator or consumer.
    private TimeProtocol hardwareClock;
    /// If we instantiated a clock consumer, this is the object.
    private TimeBroadcastConsumer consumer;

    public void dispose() {
        if (consumer != null) {
            consumer.dispose();
        }
    }
}

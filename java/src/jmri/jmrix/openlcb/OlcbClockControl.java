package jmri.jmrix.openlcb;

import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.protocols.TimeBroadcastConsumer;
import org.openlcb.protocols.TimeBroadcastGenerator;
import org.openlcb.protocols.TimeProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import jmri.Timebase;
import jmri.TimebaseRateException;
import jmri.implementation.DefaultClockControl;
import jmri.util.ThreadingUtil;

/**
 * Implementation of the ClockControl interface for JMRI using the OpenLCB clock listener or generator.
 *
 * @author Balazs Racz, 2018
 */

public class OlcbClockControl extends DefaultClockControl {
    public OlcbClockControl(OlcbInterface iface, NodeID clockID, boolean isMaster) {
        this.clockId = clockID;
        if (isMaster) {
            generator = new TimeBroadcastGenerator(iface, clockID);
            hardwareClock = generator;
        } else {
            consumer = new TimeBroadcastConsumer(iface, clockID);
            hardwareClock = consumer;
        }
        jmriClock = jmri.InstanceManager.getDefault(jmri.Timebase.class);
        listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                clockUpdate(propertyChangeEvent.getPropertyName(), propertyChangeEvent.getNewValue());
            }
        };
        hardwareClock.addPropertyChangeListener(listener);
    }

    public void dispose() {
        hardwareClock.removePropertyChangeListener(listener);
        if (consumer != null) {
            consumer.dispose();
            consumer = null;
            hardwareClock = null;
        }
    }

    /// Called when the layout sends an update to state, for example when someone else operates a
    /// clock controlling node.
    private void clockUpdate(String property, Object newValue) {
        if (property.equals(TimeProtocol.PROP_RUN_UPDATE)) {
            jmriClock.setRun(hardwareClock.isRunning());
        } else if (property.equals(TimeProtocol.PROP_RATE_UPDATE)) {
            try {
                jmriClock.userSetRate(hardwareClock.getRate());
            } catch (TimebaseRateException e) {
                log.warn("Failed to set OpenLCB rate to internal clock.");
            }
        } else if (property.equals(TimeProtocol.PROP_TIME_UPDATE)) {
            jmriClock.setTime(new Date(hardwareClock.getTimeInMsec()));
        }
    }

    @Override
    public String getHardwareClockName() {
        String clockName;
        if (clockId.equals(TimeProtocol.DEFAULT_CLOCK)) {
            clockName = Bundle.getMessage("OlcbClockDefault");
        } else if (clockId.equals(TimeProtocol.DEFAULT_RT_CLOCK)) {
            clockName = Bundle.getMessage("OlcbClockDefaultRT");
        } else if (clockId.equals(TimeProtocol.ALT_CLOCK_1)) {
            clockName = Bundle.getMessage("OlcbClockAlt1");
        } else if (clockId.equals(TimeProtocol.ALT_CLOCK_2)) {
            clockName = Bundle.getMessage("OlcbClockAlt2");
        } else {
            clockName = Bundle.getMessage("OlcbClockCustom", clockId.toString());
        }
        if (consumer != null) {
            return Bundle.getMessage("OlcbClockListenerFor", clockName);
        } else {
            return Bundle.getMessage("OlcbClockGeneratorFor", clockName);
        }
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
        // OpenLCB rates are 0.25 resolution, so we use half of that as minimum threshold.
        if (Math.abs(hardwareClock.getRate() - newRate) > 0.12) {
            hardwareClock.requestSetRate(newRate);
        } else if (Math.abs(hardwareClock.getRate() - newRate) > 0.0001) {
            // Trigger update notification that we rejected the change, but not inline.
            ThreadingUtil.runOnLayoutDelayed(new ThreadingUtil.ThreadAction() {
                @Override
                public void run() {
                    clockUpdate(TimeProtocol.PROP_RATE_UPDATE, null);
                }
            }, 50);
        }

    }

    @Override
    public void setTime(Date now) {
        hardwareClock.requestSetTime(now.getTime());
    }

    @Override
    public void initializeHardwareClock(double rate, Date now, boolean getTime) {
        if (!getTime) {
            hardwareClock.requestSetTime(now.getTime());
            if (rate == 0) {
                hardwareClock.requestStop();
            } else {
                hardwareClock.requestSetRate(rate);
                hardwareClock.requestStart();
            }
        } else {
            hardwareClock.requestQuery();
        }
    }

    /// Stores instance to the JMRI clock master.
    private Timebase jmriClock;
    /// This is the interface to the clock generator or consumer.
    private TimeProtocol hardwareClock;
    /// The clock identifier on the OpenLCB bus.
    private NodeID clockId;
    /// If we instantiated a clock consumer, this is the object.
    private TimeBroadcastConsumer consumer;
    /// If we instantiated a generator, this is the object
    private TimeBroadcastGenerator generator;
    /// The listener registered for the hardwareClock.
    private PropertyChangeListener listener;

    private final static Logger log = LoggerFactory.getLogger(OlcbClockControl.class);
}

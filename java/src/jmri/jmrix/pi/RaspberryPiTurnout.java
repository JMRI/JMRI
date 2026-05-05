package jmri.jmrix.pi;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.exception.IOException;

import jmri.implementation.AbstractTurnout;
import jmri.jmrix.pi.simulator.GpioPinDigitalOutputSimulator;
import jmri.jmrix.pi.simulator.GpioSimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turnout interface to RaspberryPi GPIO pins.
 * <p>
 * Uses Pi4J 3.x on real hardware, or the JMRI-internal
 * {@link GpioSimulator} when in simulator mode.
 * <p>
 * <b>Pin numbering:</b> the numeric part of the system name is the BCM
 * (Broadcom) GPIO number. For example, system name {@code "PT2"} addresses
 * BCM GPIO 2.
 *
 * @author Paul Bender Copyright (C) 2015
 */
public class RaspberryPiTurnout extends AbstractTurnout implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /** Pi4J digital output — non-null only in production (non-simulator) mode. */
    private transient DigitalOutput pi4jPin = null;

    /** JMRI simulator output pin — non-null only in simulator mode. */
    private transient GpioPinDigitalOutputSimulator simPin = null;

    /** Pi4J registry key for this pin, used to shut it down individually. */
    private String pinId = null;

    public RaspberryPiTurnout(String systemName) {
        super(systemName);
        log.trace("Provisioning turnout '{}'", systemName);
        init(systemName);
    }

    public RaspberryPiTurnout(String systemName, String userName) {
        super(systemName, userName);
        log.trace("Provisioning turnout '{}' with username '{}'", systemName, userName);
        init(systemName);
    }

    /**
     * Common initialisation for all constructors.
     * <p>
     * Compare {@link RaspberryPiSensor}
     */
    private void init(String systemName) {
        log.debug("Provisioning turnout {}", systemName);
        int address = Integer.parseInt(getSystemName().substring(getSystemName().lastIndexOf("T") + 1));
        pinId = "jmri-rpi-turnout-" + address;

        if (RaspberryPiAdapter.isSimulator()) {
            simPin = GpioSimulator.getInstance().provisionDigitalOutputPin(address, systemName);
        } else {
            Context ctx = RaspberryPiAdapter.getSharedContext();
            if (ctx == null) {
                String msg = Bundle.getMessage("PinNameNotValid", "GPIO " + address, systemName);
                log.error(msg);
                throw new IllegalArgumentException(msg);
            }
            try {
                pi4jPin = ctx.create(
                    DigitalOutput.newConfigBuilder(ctx)
                        .id(pinId)
                        .name(systemName)
                        .address(address)
                        .shutdown(DigitalState.LOW)
                        .initial(DigitalState.LOW)
                        .build()
                );
            } catch (RuntimeException re) {
                log.error("Provisioning turnout {} failed with: {}", systemName, re.getMessage());
                throw new IllegalArgumentException(re.getMessage());
            }
        }
    }

    // support inversion for RPi turnouts
    @Override
    public boolean canInvert() {
        return true;
    }

    /**
     * {@inheritDoc}
     * Sets the GPIO pin high or low to represent CLOSED or THROWN.
     */
    @Override
    protected void forwardCommandChangeToLayout(int newState) {
        try {
            if (newState == CLOSED) {
                log.debug("Setting turnout '{}' to CLOSED", getSystemName());
                if (!getInverted()) {
                    if (pi4jPin != null) pi4jPin.high(); else simPin.high();
                } else {
                    if (pi4jPin != null) pi4jPin.low(); else simPin.low();
                }
            } else if (newState == THROWN) {
                log.debug("Setting turnout '{}' to THROWN", getSystemName());
                if (!getInverted()) {
                    if (pi4jPin != null) pi4jPin.low(); else simPin.low();
                } else {
                    if (pi4jPin != null) pi4jPin.high(); else simPin.high();
                }
            }
        } catch (IOException ex) {
            log.error("Error setting turnout {}: {}", getSystemName(), ex.getMessage());
        }
    }

    @Override
    public void dispose() {
        if (pi4jPin != null) {
            try {
                Context ctx = RaspberryPiAdapter.getSharedContext();
                if (ctx != null) {
                    ctx.shutdown(pinId);
                }
            } catch (Exception ex) {
                log.trace("Pin {} not found during dispose — already removed?", pinId);
            }
            pi4jPin = null;
        } else if (simPin != null) {
            int address = Integer.parseInt(
                getSystemName().substring(getSystemName().lastIndexOf("T") + 1));
            GpioSimulator.getInstance().unprovisionOutputPin(address);
            simPin = null;
        }
        super.dispose();
    }

    @Override
    protected void turnoutPushbuttonLockout(boolean locked) {
    }

    private static final Logger log = LoggerFactory.getLogger(RaspberryPiTurnout.class);

}

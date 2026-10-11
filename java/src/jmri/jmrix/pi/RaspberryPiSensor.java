package jmri.jmrix.pi;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;

import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import jmri.jmrix.pi.simulator.GpioPinDigitalInputSimulator;
import jmri.jmrix.pi.simulator.GpioSimulator;

/**
 * Sensor interface for RaspberryPi GPIO pins.
 * <p>
 * Uses Pi4J 3.x on real hardware, or the JMRI-internal
 * {@link GpioSimulator} when in simulator mode.
 * <p>
 * <b>Pin numbering:</b> the numeric part of the system name is the BCM
 * (Broadcom) GPIO number. For example, system name {@code "PS4"} addresses
 * BCM GPIO 4.
 *
 * @author Paul Bender Copyright (C) 2003-2017
 */
public class RaspberryPiSensor extends AbstractSensor {

    /** Pi4J digital input — non-null only in production (non-simulator) mode. */
    private DigitalInput pi4jPin = null;

    /** JMRI simulator input pin — non-null only in simulator mode. */
    private GpioPinDigitalInputSimulator simPin = null;

    /** Pi4J registry key for this pin, used to shut it down individually. */
    private String pinId = null;

    private com.pi4j.io.gpio.digital.PullResistance pi4jPull = com.pi4j.io.gpio.digital.PullResistance.PULL_DOWN;
    private jmri.Sensor.PullResistance pull = jmri.Sensor.PullResistance.PULL_DOWN;

    public RaspberryPiSensor(String systemName, String userName) {
        super(systemName, userName);
        init(systemName, jmri.Sensor.PullResistance.PULL_DOWN);
    }

    public RaspberryPiSensor(String systemName, String userName, jmri.Sensor.PullResistance p) {
        super(systemName, userName);
        init(systemName, p);
    }

    public RaspberryPiSensor(String systemName) {
        super(systemName);
        init(systemName, jmri.Sensor.PullResistance.PULL_DOWN);
    }

    public RaspberryPiSensor(String systemName, jmri.Sensor.PullResistance p) {
        super(systemName);
        init(systemName, p);
    }

    /**
     * Common initialisation for all constructors.
     * <p>
     * Compare {@link RaspberryPiTurnout}
     */
    private void init(String systemName, jmri.Sensor.PullResistance pRes) {
        log.debug("Provisioning sensor {}", systemName);
        pull = pRes;
        pi4jPull = toPi4JPull(pRes);
        int address = Integer.parseInt(systemName.substring(systemName.lastIndexOf("S") + 1));
        pinId = "jmri-rpi-sensor-" + address;

        if (RaspberryPiAdapter.isSimulator()) {
            simPin = GpioSimulator.getInstance().provisionDigitalInputPin(address, systemName);
        } else {
            Context ctx = RaspberryPiAdapter.getSharedContext();
            if (ctx == null) {
                String msg = Bundle.getMessage("PinNameNotValid", "GPIO " + address, systemName);
                log.error(msg);
                throw new IllegalArgumentException(msg);
            }
            try {
                pi4jPin = ctx.create(
                    DigitalInput.newConfigBuilder(ctx)
                        .id(pinId)
                        .name(systemName)
                        .address(address)
                        .pull(pi4jPull)
                        .build()
                );
                pi4jPin.addListener(event -> setStateBeforeInvert(event.state().isHigh()));
            } catch (RuntimeException re) {
                log.error("Provisioning sensor {} failed with: {}", systemName, re.getMessage());
                throw new IllegalArgumentException(re.getMessage());
            }
        }
        requestUpdateFromLayout();
    }

    private static com.pi4j.io.gpio.digital.PullResistance toPi4JPull(jmri.Sensor.PullResistance pr) {
        if (pr == jmri.Sensor.PullResistance.PULL_UP)   return com.pi4j.io.gpio.digital.PullResistance.PULL_UP;
        if (pr == jmri.Sensor.PullResistance.PULL_DOWN) return com.pi4j.io.gpio.digital.PullResistance.PULL_DOWN;
        return com.pi4j.io.gpio.digital.PullResistance.OFF;
    }

    /**
     * Request an update on status by reading the current pin state.
     */
    @Override
    public void requestUpdateFromLayout() {
        if (pi4jPin != null) {
            setStateBeforeInvert(pi4jPin.state() == DigitalState.HIGH);
        } else if (simPin != null) {
            setStateBeforeInvert(simPin.isHigh());
        }
    }

    private void setStateBeforeInvert(boolean high) {
        if (high) {
            setOwnState(!getInverted() ? Sensor.ACTIVE : Sensor.INACTIVE);
        } else {
            setOwnState(!getInverted() ? Sensor.INACTIVE : Sensor.ACTIVE);
        }
    }

    /**
     * Set the pull resistance.
     * <p>
     * Note: Pi4J 3.x does not support changing pull resistance after a pin is
     * provisioned. This call updates the cached value but has no effect on
     * already-provisioned hardware pins.
     *
     * @param r the new PullResistance value
     */
    @Override
    public void setPullResistance(jmri.Sensor.PullResistance r) {
        pull = r;
        pi4jPull = toPi4JPull(r);
        if (!RaspberryPiAdapter.isSimulator()) {
            log.warn("Changing pull resistance after pin provisioning is not supported in Pi4J 3.x");
        }
    }

    /**
     * Get the pull resistance.
     *
     * @return the currently configured pull resistance
     */
    @Override
    public jmri.Sensor.PullResistance getPullResistance() {
        return pull;
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
                getSystemName().substring(getSystemName().lastIndexOf("S") + 1));
            GpioSimulator.getInstance().unprovisionInputPin(address);
            simPin = null;
        }
        super.dispose();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RaspberryPiSensor.class);

}

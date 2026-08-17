package jmri.jmrix.pi.simulator;

import java.util.HashMap;
import java.util.Map;

/**
 * Simulate Raspberry Pi GPIO — pure JMRI implementation, no Pi4J dependency.
 * <p>
 * Maintains a registry of provisioned input and output pins keyed by BCM
 * address so that {@link jmri.jmrix.pi.RaspberryPiSensor} and
 * {@link jmri.jmrix.pi.RaspberryPiTurnout} can share state.
 *
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class GpioSimulator {

    private static GpioSimulator instance = new GpioSimulator();

    private final Map<Integer, GpioPinDigitalInputSimulator> inputPins = new HashMap<>();
    private final Map<Integer, GpioPinDigitalOutputSimulator> outputPins = new HashMap<>();

    public static GpioSimulator getInstance() {
        return instance;
    }

    private GpioSimulator() {
    }

    /**
     * Provision (or replace) a simulated digital input pin.
     *
     * @param address BCM pin address
     * @param name    descriptive name (unused internally)
     * @return the new pin instance
     */
    public GpioPinDigitalInputSimulator provisionDigitalInputPin(int address, String name) {
        GpioPinDigitalInputSimulator pin = new GpioPinDigitalInputSimulator();
        inputPins.put(address, pin);
        return pin;
    }

    /**
     * Provision (or replace) a simulated digital output pin.
     *
     * @param address BCM pin address
     * @param name    descriptive name (unused internally)
     * @return the new pin instance
     */
    public GpioPinDigitalOutputSimulator provisionDigitalOutputPin(int address, String name) {
        GpioPinDigitalOutputSimulator pin = new GpioPinDigitalOutputSimulator();
        outputPins.put(address, pin);
        return pin;
    }

    /** Remove a provisioned input pin (called from sensor dispose). */
    public void unprovisionInputPin(int address) {
        inputPins.remove(address);
    }

    /** Remove a provisioned output pin (called from turnout dispose). */
    public void unprovisionOutputPin(int address) {
        outputPins.remove(address);
    }

    /** Remove all provisioned pins (called from adapter shutdown). */
    public void shutdown() {
        inputPins.clear();
        outputPins.clear();
    }

    /**
     * Reset pin registry for testing. Preserves the singleton instance but
     * clears all provisioned pins so that the next test starts clean.
     */
    public void reset() {
        inputPins.clear();
        outputPins.clear();
    }
}

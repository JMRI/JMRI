package jmri.jmrix.pi.simulator;

/**
 * Simulates a digital output GPIO pin for the JMRI Raspberry Pi simulator.
 * Pure JMRI implementation — no Pi4J dependency.
 *
 * @author Daniel Bergqvist Copyright (C) 2022
 * @author Bob Jacobsen Copyright (C) 2023
 */
public class GpioPinDigitalOutputSimulator {

    private boolean high = false;

    public void high() {
        high = true;
    }

    public void low() {
        high = false;
    }

    public void setState(boolean h) {
        high = h;
    }

    public boolean isHigh() {
        return high;
    }
}

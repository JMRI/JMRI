package jmri.jmrix.pi.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Simulates a digital input GPIO pin for the JMRI Raspberry Pi simulator.
 * Pure JMRI implementation — no Pi4J dependency.
 * <p>
 * The initial state is HIGH so that sensors constructed in tests start as
 * {@code Sensor.ACTIVE}, preserving the behaviour of the former
 * {@code PiGpioProviderScaffold} which returned {@code PinState.HIGH}.
 *
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class GpioPinDigitalInputSimulator {

    /** Starts HIGH to match legacy scaffold behaviour in tests. */
    private boolean high = true;

    private final List<Consumer<Boolean>> listeners = new ArrayList<>();

    public boolean isHigh() {
        return high;
    }

    /**
     * Change the simulated pin state and notify all registered listeners.
     *
     * @param high {@code true} = HIGH, {@code false} = LOW
     */
    public void setState(boolean high) {
        this.high = high;
        for (Consumer<Boolean> listener : new ArrayList<>(listeners)) {
            listener.accept(high);
        }
    }

    public void addListener(Consumer<Boolean> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<Boolean> listener) {
        listeners.remove(listener);
    }
}

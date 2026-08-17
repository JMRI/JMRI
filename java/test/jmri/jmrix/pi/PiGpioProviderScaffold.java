package jmri.jmrix.pi;

import jmri.jmrix.pi.simulator.GpioSimulator;

/**
 * Test helper that enables the JMRI Raspberry Pi GPIO simulator so that
 * unit tests can run on non-Pi hardware without any Pi4J hardware providers.
 * <p>
 * Usage in JUnit 5:
 * <pre>
 *     private PiGpioProviderScaffold scaffold;
 *
 *     {@literal @}BeforeEach
 *     public void setUp() {
 *         scaffold = new PiGpioProviderScaffold();
 *         // … create sensors / turnouts …
 *     }
 *
 *     {@literal @}AfterEach
 *     public void tearDown() {
 *         scaffold.shutdown();
 *     }
 * </pre>
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PiGpioProviderScaffold {

    public PiGpioProviderScaffold() {
        RaspberryPiAdapter.setIsSimulator(true);
    }

    /**
     * Reset the JMRI GPIO simulator state.
     * Call this in {@code @AfterEach} to clean up all provisioned pins and
     * allow the next test to start fresh.
     */
    public void shutdown() {
        GpioSimulator.getInstance().reset();
    }
}

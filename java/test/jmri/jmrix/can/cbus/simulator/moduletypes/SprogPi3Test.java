package jmri.jmrix.can.cbus.simulator.moduletypes;

import org.junit.jupiter.api.BeforeEach;

/**
 * Tests for SprogPi3.
 * @author Steve Young Copyright (C) 2022
 */
public class SprogPi3Test extends SimModuleProviderTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        provider = new SprogPi3();
    }

}

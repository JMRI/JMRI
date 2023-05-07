package jmri.jmrix.can.cbus.simulator.moduletypes;

import org.junit.jupiter.api.BeforeEach;

/**
 * Tests for SprogPiSprog3Plus.
 * 
 * Tests for SPROG 3 Plus, Pi-SPROG 3v2 and Pi-SPROG 3 Plus CBUS simulation module
 * 
 * @author Steve Young Copyright (C) 2022
 */
public class SprogPiSprog3PlusTest extends SimModuleProviderTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        provider = new SprogPiSprog3Plus();
    }

}

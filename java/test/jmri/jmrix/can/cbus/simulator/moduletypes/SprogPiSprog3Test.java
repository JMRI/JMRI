package jmri.jmrix.can.cbus.simulator.moduletypes;

import org.junit.jupiter.api.BeforeEach;

/**
 * Tests for SprogPiSprog3.
 * 
 * Tests for SPROG DCC Pi-SPROG 3 (not v2) CBUS simulation module
 * 
 * @author Steve Young Copyright (C) 2022
 */
public class SprogPiSprog3Test extends SimModuleProviderTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        provider = new SprogPiSprog3();
    }

}

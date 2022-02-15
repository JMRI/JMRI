package jmri.jmrix.can.cbus.simulator.moduletypes;

import org.junit.jupiter.api.BeforeEach;

/**
 * Tests for CbusMax.
 * @author Steve Young Copyright (C) 2022
 */
public class CbusMaxTest extends SimModuleProviderTestBase {
    
    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        provider = new CbusMax();
    }

}

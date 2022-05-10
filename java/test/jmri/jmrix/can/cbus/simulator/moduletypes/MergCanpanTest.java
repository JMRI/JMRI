package jmri.jmrix.can.cbus.simulator.moduletypes;

import org.junit.jupiter.api.BeforeEach;

/**
 * Tests for MergCanpan.
 * @author Steve Young Copyright (C) 2022
 */
public class MergCanpanTest extends SimModuleProviderTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        provider = new MergCanpan();
    }

}

package jmri.jmrix.bidib.simulator;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the BiDiBSimulatorAdapter class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBSimulatorAdapterTest {

    @Test
    public void testCTor() {
        BiDiBSimulatorAdapter t = new BiDiBSimulatorAdapter();
        Assertions.assertNotNull(t, "exists");
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

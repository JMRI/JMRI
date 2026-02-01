package jmri.jmrix.grapevine.simulator;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SimulatorAdapter class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SimulatorAdapterTest {

    @Test
    public void testGrapevineSimulatorAdapterConstructor(){
        Assertions.assertNotNull( new SimulatorAdapter(), "SimulatorAdapter constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
   }

    @AfterEach
    public void tearDown(){
        JUnitUtil.tearDown();
    }

}

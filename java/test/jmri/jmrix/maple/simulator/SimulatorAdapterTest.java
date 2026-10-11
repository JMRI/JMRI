package jmri.jmrix.maple.simulator;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for simulator SimulatorAdapter class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class SimulatorAdapterTest {

    @Test
    public void testMapleSimulatorAdapterConstructor(){
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

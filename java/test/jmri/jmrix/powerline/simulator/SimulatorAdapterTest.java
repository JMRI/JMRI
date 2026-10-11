package jmri.jmrix.powerline.simulator;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SimulatorAdapter class.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2025
 **/
public class SimulatorAdapterTest {

    @Test
    public void powerlineSimAdapterConstructorTest(){
        SimulatorAdapter sa = new SimulatorAdapter();
        Assertions.assertNotNull( sa, "SimulatorAdapter constructor");
        sa.dispose();
    }

    @Test
    public void testOpenPowerlineSimAdapterPort() {
        SimulatorAdapter sa = new SimulatorAdapter();
        String result = sa.openPort("portName", "appName");
        Assertions.assertNull(result);
        Assertions.assertNotNull(sa.getInputStream());
        Assertions.assertNotNull(sa.getOutputStream());
        sa.dispose();
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

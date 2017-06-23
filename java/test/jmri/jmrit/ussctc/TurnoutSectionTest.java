package jmri.jmrit.ussctc;

import org.junit.*;

import jmri.util.*;

/**
 * Tests for TurnoutSection class in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
  */
public class TurnoutSectionTest {

    @Test
    public void testConstruction() {
        new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", codeline);
    }
 
    CodeLine codeline = new CodeLine("Code Sequencer Start", "IT101", "IT102", "IT103", "IT104");
            
    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}

package jmri.jmrit.ussctc;

import org.junit.*;

import jmri.util.*;

import java.util.*;

/**
 * Tests for SignalHeadSection class in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
  */
public class SignalHeadSectionTest {

    @Test
    public void testConstruction() {
        new SignalHeadSection(new ArrayList<String>(), new ArrayList<String>(),   // empty
                        "Sec 1 Sign 1 L", "Sec 1 Sign 1 C", "Sec 1 Sign 1 R", 
                         "Sec 1 Sign 1 L", "Sec 1 Sign 1 R",
                        codeline);
    }
 
    CodeLine codeline = new CodeLine("Code Sequencer Start", "IT101", "IT102", "IT103", "IT104");
            
    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}

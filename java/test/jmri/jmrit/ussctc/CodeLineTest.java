package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for CodeLine class in the jmri.jmrit.ussctc package
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class CodeLineTest {

    @Test
    public void testConstruction() {
        new CodeLine("Code Indication Start", "Code Send Start", "IT101", "IT102", "IT103", "IT104");
    }
        
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

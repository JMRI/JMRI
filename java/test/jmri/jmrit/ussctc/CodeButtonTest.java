package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for CodeButton class in the jmri.jmrit.ussctc package
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class CodeButtonTest {

    @Test
    public void testConstruction() {
        new CodeButton("IS21", "IS22");
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

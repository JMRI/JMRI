package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for CodeButton class in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class CodeButtonTest {

    @Test
    public void testConstruction() {
        new CodeButton("IS21", "IS22");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

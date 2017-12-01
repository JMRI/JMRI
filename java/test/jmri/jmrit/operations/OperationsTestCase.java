package jmri.jmrit.operations;

import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import junit.framework.TestCase;

/**
 * Common setup and tear down for operation tests.
 *
 * @author Dan Boudreau Copyright (C) 2015
 * 
 */
public class OperationsTestCase extends TestCase {

    public OperationsTestCase(String s) {
        super(s);
    }

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();

        // Set things up outside of operations
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initIdTagManager();
        JUnitUtil.initShutDownManager();
        JUnitUtil.resetProfileManager();
        JUnitOperationsUtil.resetOperationsManager();
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        JUnitUtil.tearDown();
    }
}

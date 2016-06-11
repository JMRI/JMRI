package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test simple functioning of UserInterface
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class UserInterfaceTest extends TestCase {

    public void testCtor() {
        UserInterface panel = new UserInterface(){
           @Override
           public void createServerThread(){
           }
        };

        Assert.assertNotNull("exists", panel );
        panel.dispose();
    }

    // from here down is testing infrastructure
    public UserInterfaceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", UserInterfaceTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(UserInterfaceTest.class);
        return suite;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initDefaultUserMessagePreferences();
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
        JUnitUtil.resetInstanceManager();
    }
}

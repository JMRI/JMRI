package jmri.jmrit.throttle;

import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test simple functioning of SmallPowerManagerButton
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SmallPowerManagerButtonTest extends TestCase {

    public void testCtor() {
        SmallPowerManagerButton panel = new SmallPowerManagerButton();
        Assert.assertNotNull("exists", panel );
    }

    // from here down is testing infrastructure
    public SmallPowerManagerButtonTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SmallPowerManagerButtonTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SmallPowerManagerButtonTest.class);
        return suite;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }
}

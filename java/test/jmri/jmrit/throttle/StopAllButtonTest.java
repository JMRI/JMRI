package jmri.jmrit.throttle;

import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test simple functioning of StopAllButton
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class StopAllButtonTest extends TestCase {

    public void testCtor() {
        StopAllButton panel = new StopAllButton();
        Assert.assertNotNull("exists", panel );
    }

    // from here down is testing infrastructure
    public StopAllButtonTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", StopAllButtonTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(StopAllButtonTest.class);
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

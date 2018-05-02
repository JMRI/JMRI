package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Test simple functioning of ThrottleController
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ThrottleControllerTest extends TestCase {

    public void testCtor() {
        ThrottleController panel = new ThrottleController();
        Assert.assertNotNull("exists", panel );
    }

    // from here down is testing infrastructure
    public ThrottleControllerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ThrottleControllerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ThrottleControllerTest.class);
        return suite;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        JUnitUtil.setUp();
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }
}

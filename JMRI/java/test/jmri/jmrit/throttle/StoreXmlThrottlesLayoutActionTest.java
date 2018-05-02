package jmri.jmrit.throttle;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Test simple functioning of StoreXmlThrottlesLayoutAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class StoreXmlThrottlesLayoutActionTest extends TestCase {

    public void testCtor() {
        StoreXmlThrottlesLayoutAction panel = new StoreXmlThrottlesLayoutAction();
        Assert.assertNotNull("exists", panel );
    }

    // from here down is testing infrastructure
    public StoreXmlThrottlesLayoutActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", StoreXmlThrottlesLayoutActionTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(StoreXmlThrottlesLayoutActionTest.class);
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

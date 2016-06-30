package jmri.jmrit.withrottle;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test simple functioning of WiThrottlesListModel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class WiThrottlesListModelTest extends TestCase {

    public void testCtor() {
        java.util.ArrayList<DeviceServer> al = new java.util.ArrayList<DeviceServer>(); 
        WiThrottlesListModel panel = new WiThrottlesListModel(al);
        Assert.assertNotNull("exists", panel );
    }

    // from here down is testing infrastructure
    public WiThrottlesListModelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", WiThrottlesListModelTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(WiThrottlesListModelTest.class);
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

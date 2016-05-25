package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test simple functioning of DeviceServer
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class DeviceServerTest extends TestCase {

    public void testCtor() {
        java.net.Socket s = new java.net.Socket();
        FacelessServer f = new FacelessServer(){
           @Override
           public void createServerThread(){
           }
        };
        DeviceServer panel = new DeviceServer(s,f);
        jmri.util.JUnitAppender.assertErrorMessage("Stream creation failed (DeviceServer)");
        Assert.assertNotNull("exists", panel );
    }

    // from here down is testing infrastructure
    public DeviceServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DeviceServerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DeviceServerTest.class);
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

package jmri.util.zeroconf;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the ZeroConfService class
 *
 * @author Paul Bender Copyright (C) 2014
 */
public class ZeroConfServiceTest extends TestCase {

    public void testCreate() {
        ZeroConfService zcs = ZeroConfService.create("_http._tcp.local.", 12345);
        Assert.assertNotNull(zcs);
    }

    // from here down is testing infrastructure
    public ZeroConfServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ZeroConfServiceTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ZeroConfServiceTest.class);
        return suite;
    }

}
